package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.compat.BlurPlusCompat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 500)
public abstract class ScreenBackgroundMixin {

    @Unique
    private boolean reanimated$blurTakesOver(DrawContext context) {
        if (!Anim.isClosing() || Anim.isTransitioning()) return false;
        if (!BlurPlusCompat.present() || !Anim.shouldAnimate(this)) return false;
        boolean pushed = UiTransform.pushBase(context.getMatrices());
        boolean drawn = BlurPlusCompat.renderBackground(context);
        if (pushed) UiTransform.popBase(context.getMatrices());
        return drawn;
    }

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void reanimated$blurredBg(DrawContext context, CallbackInfo ci) {
        if (reanimated$blurTakesOver(context)) ci.cancel();
    }

    @Inject(method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;IIII)V", at = @At("HEAD"), cancellable = true)
    private void reanimated$menuBg(DrawContext context, int x, int y, int w, int h, CallbackInfo ci) {
        if (reanimated$blurTakesOver(context)) ci.cancel();
    }

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$transparentBg(DrawContext context, CallbackInfo ci) {
        if (reanimated$blurTakesOver(context)) ci.cancel();
    }
}
