package com.pycodder.reanimated.mixin.compat;

import com.pycodder.reanimated.anim.Anim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.emi.emi.screen.StackBatcher", remap = false)
public abstract class EmiStackBatcherMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void reanimated$unbatchWhileAnimating(CallbackInfoReturnable<Boolean> cir) {
        Object screen = Anim.currentScreen;
        if (screen == null) return;
        if (!Anim.transformActive(Anim.currentIsContainer)) return;
        if (!Anim.shouldAnimate(screen)) return;
        cir.setReturnValue(false);
    }
}
