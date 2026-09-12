package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.ToastAnim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastComponent$ToastInstance")
public abstract class ToastEntryMixin {

    @Shadow private Toast.Visibility visibility;

    @Inject(method = "getVisibility", at = @At("RETURN"), cancellable = true)
    private void reanimated$toastCurve(long time, CallbackInfoReturnable<Float> cir) {
        if (!ReAnimatedConfig.get().toastEnabled) return;
        cir.setReturnValue(ToastAnim.remap(cir.getReturnValueF(),
            this.visibility == Toast.Visibility.HIDE));
    }
}
