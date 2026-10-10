package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.compat.BlurPlusCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 500)
public abstract class ScreenBackgroundMixin {

    @Unique
    private boolean reanimated$blurTakesOver(GuiGraphicsExtractor extractor) {
        if (!Anim.isClosing() || Anim.isTransitioning()) return false;
        if (!BlurPlusCompat.present() || !Anim.shouldAnimate(this)) return false;
        boolean pushed = UiTransform.pushBase(extractor.pose());
        boolean drawn = BlurPlusCompat.renderBackground(extractor);
        if (pushed) UiTransform.popBase(extractor.pose());
        return drawn;
    }

    @Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$blurredBg(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        if (reanimated$blurTakesOver(extractor)) ci.cancel();
    }

    @Inject(method = "extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V",
            at = @At("HEAD"), cancellable = true)
    private void reanimated$menuBg(GuiGraphicsExtractor extractor, int x, int y, int w, int h, CallbackInfo ci) {
        if (reanimated$blurTakesOver(extractor)) ci.cancel();
    }

    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$transparentBg(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        if (reanimated$blurTakesOver(extractor)) ci.cancel();
    }
}
