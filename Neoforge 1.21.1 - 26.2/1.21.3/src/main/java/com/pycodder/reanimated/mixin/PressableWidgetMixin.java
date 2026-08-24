package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pycodder.reanimated.anim.CascadeTarget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Dims the button TEXT along with its frame (NeoForge / Mojmap, 1.21.2–1.21.5 era). */
@Mixin(AbstractButton.class)
public abstract class PressableWidgetMixin {
    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/AbstractButton;renderString("
               + "Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private void reanimated$fadeTextBegin(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float a = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        if (a < 1f) {
            RenderSystem.setShaderColor(1f, 1f, 1f, a);
        }
    }

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void reanimated$fadeTextEnd(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float a = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        if (a < 1f) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }
}
