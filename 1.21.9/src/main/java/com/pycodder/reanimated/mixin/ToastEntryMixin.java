package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.ToastAnim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.toast.ToastManager$Entry")
public abstract class ToastEntryMixin {

    @Shadow private float visibleWidthPortion;

    @Shadow Toast.Visibility visibility;

    @Unique private float reanimated$originalPortion = Float.NaN;

    @Inject(method = "draw", at = @At("HEAD"))
    private void reanimated$toastHead(DrawContext context, int index, CallbackInfo ci) {
        if (!ReAnimatedConfig.get().toastEnabled) return;
        reanimated$originalPortion = this.visibleWidthPortion;
        this.visibleWidthPortion = ToastAnim.remap(
            reanimated$originalPortion, this.visibility == Toast.Visibility.HIDE);
    }

    @Inject(method = "draw", at = @At("RETURN"))
    private void reanimated$toastTail(DrawContext context, int index, CallbackInfo ci) {
        if (Float.isNaN(reanimated$originalPortion)) return;
        this.visibleWidthPortion = reanimated$originalPortion;
        reanimated$originalPortion = Float.NaN;
    }
}
