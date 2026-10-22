package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.ScreenNav;
import com.pycodder.reanimated.anim.ScreenRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Отложенное закрытие экрана: вместо мгновенного {@code setScreen(null)} экран
 * ненадолго остаётся текущим и доигрывает анимацию открытия в обратном порядке
 * ({@link Anim#beginClose}), а по её завершении {@code ScreenMixin} сам вызывает
 * настоящий {@code setScreen(null)} (пропуская этот миксин через {@link Anim#bypassClose}).
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public Screen currentScreen;

    @Unique private String reanimated$closeName = null;
    @Unique private boolean reanimated$closeNameResolved = false;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void reanimated$onSetScreen(Screen screen, CallbackInfo ci) {
        if (Anim.bypassClose) return;

        if (screen != null) {
            reanimated$onOpenScreen(screen, ci);
            return;
        }

        // Уходим в мир — история переходов начинается заново.
        ScreenNav.clear();

        // Закрытие уже проигрывается. Контейнерное закрытие (инвентарь/печь/верстак)
        // шлёт setScreen(null) ДВАЖДЫ: сначала ClientPlayerEntity.closeScreen, затем
        // Screen.close. Глотаем повторные вызовы, иначе второй закрыл бы экран мгновенно,
        // без анимации. Настоящее закрытие выполнит ScreenMixin через bypassClose.
        if (Anim.isClosing()) {
            // Если играл переход на другой экран, а игрок закрыл интерфейс — закрытие
            // сильнее: цель перехода снимаем и по окончании уходим в мир, а направление
            // входа гасим, чтобы его не подхватил следующий открытый экран.
            Anim.pendingScreen = null;
            Anim.clearEnterDir();
            ci.cancel();
            return;
        }

        Screen current = this.currentScreen;
        if (current == null) return;
        if (!reanimated$safeToDefer(current)) return;

        boolean container = current instanceof HandledScreen;
        // Откладываем закрытие, если играет ЛЮБОЙ слой: пресет (экран) ИЛИ профиль
        // (кнопки). Иначе там, где открытие анимируется одним из слоёв, закрытие
        // молчало бы. Набор настроек выбирает сам Anim — у меню паузы он свой.
        Anim.currentIsPause = Anim.isPauseScreen(current);
        Anim.currentOverride = ScreenRules.get(current);
        // Закрытие могли выключить для всех экранов или только для этого.
        if (!Anim.closeAnimationEnabled()) return;
        // Экран, который мод не анимирует вовсе (чат, экраны модов в режиме «только
        // ванильные», экран, выключенный лично), закрывается мгновенно: отложенное
        // закрытие ему нечем заполнить, и он просто висел бы, ничего не показывая.
        if (!Anim.shouldAnimate(current)) return;
        // Ни одного слоя, который мог бы проиграть уход, — задерживать нечем.
        if (!Anim.closeWouldAnimate(container)) return;

        Anim.beginClose(container);
        ci.cancel();
    }

    /**
     * Открывается другой экран. Если переходы включены и оба экрана к этому пригодны,
     * настоящий {@code setScreen} откладывается: текущий экран доигрывает уход в
     * сторону, а {@code ScreenMixin} по его окончании переключает игру на новый.
     *
     * Во всех сомнительных случаях (контейнеры, экраны загрузки/подключения, экраны со
     * своим {@code close()}) переход не начинается и смена экрана идёт как в ванили —
     * задержка тут стоила бы дороже анимации.
     */
    @Unique
    private void reanimated$onOpenScreen(Screen next, CallbackInfo ci) {
        if (Anim.isClosing()) {
            // Уже играет уход. Если это уход на другой экран — просто меняем цель,
            // не перезапуская анимацию; иначе реверс закрытия больше не актуален.
            if (Anim.pendingScreen != null) {
                Anim.pendingScreen = next;
                ci.cancel();
            } else {
                Anim.finishClose();
            }
            return;
        }

        Screen current = this.currentScreen;
        if (current == null || current == next) return;
        // Переходы могли выключить (или включить) для одного из двух экранов.
        if (!Anim.transitionAllowed(current) || !Anim.transitionAllowed(next)) return;
        if (current instanceof HandledScreen || next instanceof HandledScreen) return;
        if (Anim.isVolatileScreen(current) || Anim.isVolatileScreen(next)) return;
        if (!Anim.shouldAnimate(current) || !Anim.shouldAnimate(next)) return;
        if (!reanimated$safeToDefer(current)) return;

        // Расчёты ухода ведутся для текущего экрана — его контекст и подставляем.
        Anim.currentIsPause = Anim.isPauseScreen(current);
        Anim.currentOverride = ScreenRules.get(current);

        // +1 — шаг вглубь: экран уезжает влево, новый приходит справа. -1 — возврат.
        int forward = ScreenNav.accept(current, next);
        Anim.beginTransition(next, -forward);
        ci.cancel();
    }

    /**
     * Можно ли держать этот экран открытым ради анимации закрытия.
     *
     * Нельзя, если экран переопределил close() сам: его код успевает отработать
     * ДО нашей отмены setScreen(null) — например, освободить кадровый буфер и
     * обнулить ссылку, — а мы после этого продолжаем рисовать экран ещё доли
     * секунды и получаем краш на первом же кадре. Так падал экран схемы из мода
     * simulated (NPE в DiagramScreen.renderFBO), и так упадёт любой экран,
     * убирающий за собой в close().
     *
     * Экранам с ванильным close() это не грозит: он ничего не разрушает, а
     * removed() (где ванильные экраны прибираются) вызывается из setScreen —
     * то есть только когда мы наконец пропустим настоящее закрытие.
     */
    @Unique
    private boolean reanimated$safeToDefer(Screen screen) {
        String name = reanimated$closeMethodName();
        if (name == null) return false;
        try {
            Class<?> owner = screen.getClass().getMethod(name).getDeclaringClass();
            return owner.getName().startsWith("net.minecraft.");
        } catch (Throwable t) {
            return false; // не смогли выяснить — не рискуем, закрываем мгновенно
        }
    }

    /**
     * Имя Screen.close() в текущей среде: yarn в разработке, intermediary в обычной
     * сборке, mojmap под Sinytra Connector. Рефлексия по строке не ремапится, поэтому
     * берём то имя, которое реально есть у класса. Определяется один раз.
     */
    @Unique
    private String reanimated$closeMethodName() {
        if (!reanimated$closeNameResolved) {
            reanimated$closeNameResolved = true;
            for (String candidate : new String[] {"method_25419", "close", "onClose"}) {
                try {
                    Screen.class.getMethod(candidate);
                    reanimated$closeName = candidate;
                    break;
                } catch (NoSuchMethodException ignored) {
                    // не эти маппинги — пробуем следующее имя
                }
            }
            if (reanimated$closeName == null) {
                ReAnimatedClient.LOGGER.warn(
                    "[ReAnimated] Screen.close() not found in this mapping environment — close animation disabled");
            }
        }
        return reanimated$closeName;
    }
}
