package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Базовая анимация появления любого экрана — слой ЭКРАНА (пресет).
 * NeoForge / Mojmap, эпоха 1.21.1–1.21.5 (PoseStack).
 *
 * Время открытия и раздача рангов каскада делаются при первой отрисовке —
 * намеренно НЕ через инъекцию в init(...): сигнатура этого метода менялась
 * между версиями (в 1.21.11 это уже init(int,int)), а ленивый вариант
 * версионно-независим. Ранги переназначаются при смене размера экрана —
 * после ресайза виджеты пересоздаются, и без этого каскад бы пропал.
 *
 * Весь экран (фон + виджеты + ЛЮБОЙ текст) рисуется внутри
 * {@code renderWithTooltip} -> {@code this.render()}. Оборачивая его одной
 * трансформацией (сдвиг и/или масштаб от центра — по выбранному пресету),
 * гарантируем, что текст, кнопки и модель игрока появляются строго вместе.
 *
 * Фон (панорама/блюр) рисуется в {@code renderBackground} — к нему применяем
 * ОБРАТНУЮ трансформацию, чтобы он оставался неподвижным при любом пресете.
 * Для контейнеров renderBackground переопределён в AbstractContainerScreen —
 * там свой обработчик ({@code HandledScreenMixin}).
 */
/*
 * priority = 1500 (по умолчанию 1000) — сознательно ВЫШЕ обычного.
 *
 * Оборачиваем отрисовку экрана: push на HEAD, pop на RETURN. Порядок инжекторов
 * разных модов в одной точке задаётся порядком применения миксинов, а он идёт по
 * возрастанию приоритета: чей приоритет выше, тот на HEAD выполняется ПЕРВЫМ, а на
 * RETURN — ПОСЛЕДНИМ. То есть наша обёртка становится самой внешней, и всё, что
 * другие моды дорисовывают к экрану из своих хуков, попадает внутрь трансформации
 * и едет вместе с экраном.
 *
 * Без этого оверлеи вроде списка предметов EMI/JEI/REI (их рисуют из хука на RETURN
 * того же метода) оставались снаружи: наш pop успевал раньше, панель стояла на месте,
 * пока экран выезжал.
 */
@Mixin(value = Screen.class, priority = 1500)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;
    @Unique private int reanimated$pauseFlag = -1;
    @Unique private int reanimated$lastW = -1;
    @Unique private int reanimated$lastH = -1;

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof AbstractContainerScreen;
    }

    /** Меню паузы у экрана не меняется — определяем один раз, а не каждый кадр. */
    @Unique
    private boolean reanimated$isPause() {
        if (reanimated$pauseFlag < 0) {
            reanimated$pauseFlag = Anim.isPauseScreen(this) ? 1 : 0;
        }
        return reanimated$pauseFlag == 1;
    }

    /**
     * Раздаёт виджетам экрана ранг сверху вниз — из него каждый виджет считает
     * свой шаг каскада. Порядок каскада применяется уже при отрисовке, поэтому
     * его смена в настройках видна сразу.
     */
    @Unique
    private void reanimated$assignCascade() {
        Anim.cascadeCount = 1;
        // Без ранга виджет не каскадируется вовсе — так экраны, которые мод не
        // анимирует (чат, экраны модов в режиме "только ванильные"), остаются нетронутыми.
        if (!Anim.shouldAnimate(this)) {
            return;
        }
        Screen self = (Screen) (Object) this;
        List<AbstractWidget> widgets = new ArrayList<>();
        for (GuiEventListener e : self.children()) {
            // Фреймы-списки (сервера/миры/ресурспаки/список опций) мод не анимирует
            // вовсе — они и шага в каскаде не занимают. См. ClickableWidgetMixin.
            if (e instanceof AbstractWidget w && w.visible && !(e instanceof AbstractContainerWidget)) {
                widgets.add(w);
            }
        }
        widgets.sort(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX));
        int count = widgets.size();
        for (int i = 0; i < count; i++) {
            ((CascadeTarget) widgets.get(i)).reanimated$setCascade(i, count);
        }
        Anim.cascadeCount = Math.max(1, count);
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void reanimated$wrapHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$openTime == 0L) {
            reanimated$openTime = System.currentTimeMillis();
        }
        // Первый кадр или ресайз — виджеты пересозданы, раздаём ранги заново.
        if (this.width != reanimated$lastW || this.height != reanimated$lastH) {
            reanimated$lastW = this.width;
            reanimated$lastH = this.height;
            reanimated$assignCascade();
        }
        Anim.currentOpenTime = reanimated$openTime;
        Anim.currentIsPause = reanimated$isPause();
        boolean container = reanimated$isContainer();
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            PoseStack m = graphics.pose();
            m.pushPose();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            graphics.pose().popPose();
        }
        reanimated$maybeFinishClose();
    }

    /** По завершении обратной анимации выполняет отложенный настоящий setScreen(null). */
    @Unique
    private void reanimated$maybeFinishClose() {
        if (!Anim.isClosing()) return;
        if (!Anim.closeFinished(reanimated$isContainer())) return;
        Anim.finishClose();
        Anim.bypassClose = true;
        Minecraft.getInstance().setScreen(null);
        Anim.bypassClose = false;
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Контейнеры сами возвращают фон на место (HandledScreenMixin). Если тронуть его
        // ещё и здесь (в 1.21.5+ AbstractContainerScreen.renderBackground зовёт super),
        // получится двойная обратная трансформация — блюр «уезжает» вместе с панелью.
        reanimated$bgPushed = !reanimated$isContainer() && Anim.transformActive(false) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            PoseStack m = graphics.pose();
            m.pushPose();
            UiTransform.inverse(m, this.width, this.height, false);
        }
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            graphics.pose().popPose();
        }
    }

    /** Цвета ванильного затемнения: градиент сверху вниз, оба — чёрный с разной прозрачностью. */
    @Unique private static final int REANIMATED$DIM_TOP = 0xC0101010;
    @Unique private static final int REANIMATED$DIM_BOTTOM = 0xD0101010;

    /**
     * Плавное появление затемнения за экраном. Ваниль включает его мгновенно — панель
     * красиво выезжает на фоне, который «щёлкнул». Здесь тот же градиент рисуется с
     * прозрачностью по кривой открытия, а на закрытии гаснет обратно.
     *
     * Пока анимация не идёт, ванильный метод отрабатывает как обычно — мод не трогает
     * ни один кадр статичного экрана и не мешает модам, меняющим затемнение.
     */
    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$dimFade(GuiGraphics graphics, CallbackInfo ci) {
        if (!Anim.shouldAnimate(this)) return;
        float k = Anim.backgroundFade(reanimated$isContainer());
        if (k >= 1f) return;

        ci.cancel();
        if (k <= 0.004f) return; // ещё неотличимо от прозрачного — не рисуем вовсе
        graphics.fillGradient(0, 0, this.width, this.height,
            reanimated$dim(REANIMATED$DIM_TOP, k), reanimated$dim(REANIMATED$DIM_BOTTOM, k));
    }

    @Unique
    private static int reanimated$dim(int argb, float k) {
        int a = Math.round(((argb >>> 24) & 0xFF) * k);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
