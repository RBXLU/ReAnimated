package com.pycodder.reanimated.mixin;

import org.joml.Matrix3x2fStack;
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
 * NeoForge / Mojmap, эпоха 1.21.10+ (Matrix3x2fStack, renderWithTooltipAndSubtitles).
 *
 * Время открытия и раздача рангов каскада делаются при первой отрисовке —
 * намеренно НЕ через инъекцию в init(...): сигнатура этого метода менялась
 * между версиями (в 1.21.11 это уже init(int,int)), а ленивый вариант
 * версионно-независим. Ранги переназначаются при смене размера экрана —
 * после ресайза виджеты пересоздаются, и без этого каскад бы пропал.
 *
 * Весь экран (фон + виджеты + ЛЮБОЙ текст) рисуется внутри
 * {@code renderWithTooltipAndSubtitles} -> {@code this.render()}. Оборачивая его одной
 * трансформацией (сдвиг и/или масштаб от центра — по выбранному пресету),
 * гарантируем, что текст, кнопки и модель игрока появляются строго вместе.
 *
 * Фон (панорама/блюр) рисуется в {@code renderBackground} — к нему применяем
 * ОБРАТНУЮ трансформацию, чтобы он оставался неподвижным при любом пресете.
 * Для контейнеров renderBackground переопределён в AbstractContainerScreen —
 * там свой обработчик ({@code HandledScreenMixin}).
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

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof AbstractContainerScreen;
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

    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("HEAD"))
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
        boolean container = reanimated$isContainer();
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            Matrix3x2fStack m = graphics.pose();
            m.pushMatrix();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            graphics.pose().popMatrix();
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
            Matrix3x2fStack m = graphics.pose();
            m.pushMatrix();
            UiTransform.inverse(m, this.width, this.height, false);
        }
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            graphics.pose().popMatrix();
        }
    }
}
