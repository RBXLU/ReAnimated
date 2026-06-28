package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Анимация появления любого экрана для Minecraft 26.x (новый рендеринг через
 * render-state extraction). Аналог старой схемы:
 *  - extractRenderStateWithTooltipAndSubtitles — внешняя точка (= renderWithTooltip),
 *    оборачиваем её одним сдвигом → весь экран (текст + кнопки) выезжает вместе;
 *  - extractBackground — фон/блюр, возвращаем встречным сдвигом, чтобы он стоял.
 *
 * GuiGraphicsExtractor.pose() возвращает тот же Matrix3x2fStack, что и раньше
 * GuiGraphics.getMatrices(), поэтому техника трансформаций переносится 1:1.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Unique private long reanimated$openTime = 0L;

    @Unique
    private float reanimated$slide() {
        return ((Object) this instanceof AbstractContainerScreen) ? Anim.containerSlide() : Anim.screenSlide();
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
    private void reanimated$wrapHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$openTime == 0L) {
            reanimated$openTime = System.currentTimeMillis();
        }
        Anim.currentOpenTime = reanimated$openTime;
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        m.translate(0f, reanimated$slide());
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        extractor.pose().popMatrix();
    }

    @Inject(method = "extractBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        m.translate(0f, -reanimated$slide());
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        extractor.pose().popMatrix();
    }
}
