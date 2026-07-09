package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Анимация появления любого экрана для Minecraft 26.x (новый рендеринг через
 * render-state extraction). Аналог старой схемы:
 *  - extractRenderStateWithTooltipAndSubtitles — внешняя точка (= renderWithTooltip),
 *    оборачиваем её общим трансформом (сдвиг/масштаб по пресету) → весь экран
 *    (текст + кнопки + панель) появляется вместе;
 *  - extractBackground — фон/блюр, возвращаем встречным трансформом, чтобы он стоял.
 *
 * Пресет ({@link Anim}/{@code UiPreset}) выбирает сдвиг снизу (DEFAULT) или масштаб
 * относительно центра (FROM_BACKGROUND / FROM_FOREGROUND).
 * GuiGraphicsExtractor.pose() возвращает тот же Matrix3x2fStack, что и раньше
 * GuiGraphics.getMatrices(), поэтому техника трансформаций переносится 1:1.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof AbstractContainerScreen;
    }

    @Unique
    private void reanimated$applyForward(Matrix3x2fStack m, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy);
        if (sc != 1f) {
            float cx = this.width / 2f;
            float cy = this.height / 2f;
            m.translate(cx, cy);
            m.scale(sc, sc);
            m.translate(-cx, -cy);
        }
    }

    @Unique
    private void reanimated$applyInverse(Matrix3x2fStack m, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            float cx = this.width / 2f;
            float cy = this.height / 2f;
            m.translate(cx, cy);
            m.scale(1f / sc, 1f / sc);
            m.translate(-cx, -cy);
        }
        m.translate(0f, -sy);
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
    private void reanimated$wrapHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$openTime == 0L) {
            reanimated$openTime = System.currentTimeMillis();
        }
        Anim.currentOpenTime = reanimated$openTime;
        boolean container = reanimated$isContainer();
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            Matrix3x2fStack m = extractor.pose();
            m.pushMatrix();
            reanimated$applyForward(m, container);
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
        net.minecraft.client.Minecraft.getInstance().setScreen(null);
        Anim.bypassClose = false;
    }

    @Inject(method = "extractBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean container = reanimated$isContainer();
        reanimated$bgPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            Matrix3x2fStack m = extractor.pose();
            m.pushMatrix();
            reanimated$applyInverse(m, container);
        }
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            extractor.pose().popMatrix();
        }
    }
}
