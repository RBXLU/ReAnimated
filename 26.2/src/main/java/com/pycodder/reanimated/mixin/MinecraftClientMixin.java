package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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
 *
 * В Minecraft 26.2 хранение текущего экрана и {@code setScreen(Screen)} переехали
 * с {@code Minecraft} на {@code Gui} ({@code Minecraft.setScreenAndShow} лишь
 * делегирует туда) — поэтому здесь миксин цепляется к {@code Gui}, а не к
 * {@code Minecraft}, как в более старых версиях.
 */
@Mixin(Gui.class)
public abstract class MinecraftClientMixin {

    @Shadow private Screen screen;

    @Unique private String reanimated$closeName = null;
    @Unique private boolean reanimated$closeNameResolved = false;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void reanimated$onSetScreen(Screen screen, CallbackInfo ci) {
        if (Anim.bypassClose) return;

        if (screen != null) {
            // Открывается другой экран — реверс старого больше не актуален.
            if (Anim.isClosing()) Anim.finishClose();
            return;
        }

        // Закрытие уже проигрывается. Контейнерное закрытие (инвентарь/печь/верстак)
        // шлёт setScreen(null) ДВАЖДЫ: сначала ClientPlayerEntity.closeScreen, затем
        // Screen.close. Глотаем повторные вызовы, иначе второй закрыл бы экран мгновенно,
        // без анимации. Настоящее закрытие выполнит ScreenMixin через bypassClose.
        if (Anim.isClosing()) {
            ci.cancel();
            return;
        }

        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.closeAnimationEnabled) return;

        Screen current = this.screen;
        if (current == null) return;
        if (!reanimated$safeToDefer(current)) return;

        boolean container = current instanceof AbstractContainerScreen;
        Anim.currentIsPause = Anim.isPauseScreen(current);
        boolean presetActive = Anim.presetLayerActive(container);
        boolean profileActive = c.profile.enabled;
        if (!presetActive && !profileActive) return;

        Anim.beginClose(container);
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
