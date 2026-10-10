package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.ToastAnim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public abstract class ToastEntryMixin {

    @Shadow private float visiblePortion;

    @Shadow private Toast.Visibility visibility;

    @Unique private float reanimated$originalPortion = Float.NaN;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void reanimated$toastHead(GuiGraphicsExtractor extractor, int index, CallbackInfo ci) {
        if (!ReAnimatedConfig.get().toastEnabled) return;
        reanimated$originalPortion = this.visiblePortion;
        this.visiblePortion = ToastAnim.remap(
            reanimated$originalPortion, this.visibility == Toast.Visibility.HIDE);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void reanimated$toastTail(GuiGraphicsExtractor extractor, int index, CallbackInfo ci) {
        if (Float.isNaN(reanimated$originalPortion)) return;
        this.visiblePortion = reanimated$originalPortion;
        reanimated$originalPortion = Float.NaN;
    }
}
