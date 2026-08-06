package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;

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
 * Анимация появления любого экрана для Minecraft 26.x (рендеринг через
 * render-state extraction) — слой ЭКРАНА (пресет):
 *  - extractRenderStateWithTooltipAndSubtitles — внешняя точка (= renderWithTooltip),
 *    оборачиваем её общим трансформом (сдвиг/масштаб по пресету) → весь экран
 *    (текст + кнопки + панель) появляется вместе;
 *  - extractBackground — фон/блюр, возвращаем встречным трансформом, чтобы он стоял.
 *
 * Покнопочный слой (профиль/Студия) кладётся ПОВЕРХ в ClickableWidgetMixin — так
 * пресет и профиль складываются, а не исключают друг друга.
 *
 * Время открытия и ранги каскада выставляются лениво при первой отрисовке;
 * ранги переназначаются при смене размера экрана (виджеты пересоздаются).
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;
    @Unique private int reanimated$lastW = -1;
    @Unique private int reanimated$lastH = -1;

    @Unique private int reanimated$pauseFlag = -1;

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

    /** Раздаёт виджетам ранг сверху вниз — из него виджет считает свой шаг каскада. */
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
            // Фреймы-списки мод не анимирует вовсе — они и шага в каскаде не занимают.
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

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
    private void reanimated$wrapHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
            Matrix3x2fStack m = extractor.pose();
            m.pushMatrix();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            extractor.pose().popMatrix();
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
        net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);
        Anim.bypassClose = false;
    }

    @Inject(method = "extractBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean container = reanimated$isContainer();
        reanimated$bgPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            Matrix3x2fStack m = extractor.pose();
            m.pushMatrix();
            UiTransform.inverse(m, this.width, this.height, container);
        }
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            extractor.pose().popMatrix();
        }
    }

    /** Цвета ванильного затемнения: градиент сверху вниз, оба — чёрный с разной прозрачностью. */
    @Unique private static final int REANIMATED$DIM_TOP = 0xC0101010;
    @Unique private static final int REANIMATED$DIM_BOTTOM = 0xD0101010;

    /**
     * Плавное появление затемнения за экраном. Ваниль включает его мгновенно — панель
     * красиво выезжает на фоне, который «щёлкнул». Здесь тот же градиент выдаётся с
     * прозрачностью по кривой открытия, а на закрытии гаснет обратно.
     *
     * Пока анимация не идёт, ванильный метод отрабатывает как обычно — мод не трогает
     * ни один кадр статичного экрана и не мешает модам, меняющим затемнение.
     */
    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$dimFade(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        if (!Anim.shouldAnimate(this)) return;
        float k = Anim.backgroundFade(reanimated$isContainer());
        if (k >= 1f) return;

        ci.cancel();
        if (k <= 0.004f) return; // ещё неотличимо от прозрачного — не рисуем вовсе
        extractor.fillGradient(0, 0, this.width, this.height,
            reanimated$dim(REANIMATED$DIM_TOP, k), reanimated$dim(REANIMATED$DIM_BOTTOM, k));
    }

    @Unique
    private static int reanimated$dim(int argb, float k) {
        int a = Math.round(((argb >>> 24) & 0xFF) * k);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
