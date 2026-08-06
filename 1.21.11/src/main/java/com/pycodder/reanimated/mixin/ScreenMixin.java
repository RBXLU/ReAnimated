package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
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
 * Базовая анимация появления любого экрана — слой ЭКРАНА (пресет).
 * Версия для 1.21.8+ : Matrix3x2fStack + ЛЕНИВАЯ инициализация.
 *
 * Время открытия и раздача рангов каскада делаются при первой отрисовке —
 * намеренно НЕ через инъекцию в init(MinecraftClient,int,int): новый Loom не
 * перемапливает явный дескриптор с типом MinecraftClient, что ломало мод без refmap.
 * Ранги переназначаются при смене размера экрана (после ресайза виджеты
 * пересоздаются, и без этого каскад бы пропал).
 *
 * Весь экран оборачивается одной трансформацией (сдвиг/масштаб по пресету), фон
 * возвращается обратной трансформацией. Контейнеры — в HandledScreenMixin.
 */
@Mixin(Screen.class)
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
        return ((Object) this) instanceof HandledScreen;
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
        List<ClickableWidget> widgets = new ArrayList<>();
        for (Element e : self.children()) {
            // Фреймы-списки (сервера/миры/ресурспаки/список опций) мод не анимирует
            // вовсе — они и шага в каскаде не занимают. См. ClickableWidgetMixin.
            if (e instanceof ClickableWidget w && w.visible && !(e instanceof ContainerWidget)) {
                widgets.add(w);
            }
        }
        widgets.sort(Comparator.comparingInt(ClickableWidget::getY).thenComparingInt(ClickableWidget::getX));
        int count = widgets.size();
        for (int i = 0; i < count; i++) {
            ((CascadeTarget) widgets.get(i)).reanimated$setCascade(i, count);
        }
        Anim.cascadeCount = Math.max(1, count);
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void reanimated$wrapHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
            Matrix3x2fStack m = context.getMatrices();
            m.pushMatrix();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            context.getMatrices().popMatrix();
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
        net.minecraft.client.MinecraftClient.getInstance().setScreen(null);
        Anim.bypassClose = false;
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Контейнеры сами возвращают фон на место (HandledScreenMixin). Если тронуть его
        // ещё и здесь (HandledScreen.renderBackground зовёт super.renderBackground),
        // получится двойная обратная трансформация — блюр «уезжает» вместе с панелью.
        reanimated$bgPushed = !reanimated$isContainer() && Anim.transformActive(false) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            Matrix3x2fStack m = context.getMatrices();
            m.pushMatrix();
            UiTransform.inverse(m, this.width, this.height, false);
        }
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            context.getMatrices().popMatrix();
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
    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$dimFade(DrawContext context, CallbackInfo ci) {
        if (!Anim.shouldAnimate(this)) return;
        float k = Anim.backgroundFade(reanimated$isContainer());
        if (k >= 1f) return;

        ci.cancel();
        if (k <= 0.004f) return; // ещё неотличимо от прозрачного — не рисуем вовсе
        context.fillGradient(0, 0, this.width, this.height,
            reanimated$dim(REANIMATED$DIM_TOP, k), reanimated$dim(REANIMATED$DIM_BOTTOM, k));
    }

    @Unique
    private static int reanimated$dim(int argb, float k) {
        int a = Math.round(((argb >>> 24) & 0xFF) * k);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
