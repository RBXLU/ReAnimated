package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.compat.BlurPlusCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 500)
public abstract class ScreenBackgroundMixin {

    @Unique
    private boolean reanimated$blurTakesOver(GuiGraphics graphics) {
        if (!Anim.isClosing() || Anim.isTransitioning()) return false;
        if (!BlurPlusCompat.present() || !Anim.shouldAnimate(this)) return false;
        boolean pushed = UiTransform.pushBase(graphics.pose());
        boolean drawn = BlurPlusCompat.renderBackground(graphics);
        if (pushed) UiTransform.popBase(graphics.pose());
        return drawn;
    }

    @Inject(method = "renderBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$blurredBg(float delta, CallbackInfo ci) {
        if (Anim.frameGraphics instanceof GuiGraphics graphics && reanimated$blurTakesOver(graphics)) ci.cancel();
    }

    @Inject(method = "renderMenuBackground(Lnet/minecraft/client/gui/GuiGraphics;IIII)V", at = @At("HEAD"), cancellable = true)
    private void reanimated$menuBg(GuiGraphics graphics, int x, int y, int w, int h, CallbackInfo ci) {
        if (reanimated$blurTakesOver(graphics)) ci.cancel();
    }

    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$transparentBg(GuiGraphics graphics, CallbackInfo ci) {
        if (reanimated$blurTakesOver(graphics)) ci.cancel();
    }
}
