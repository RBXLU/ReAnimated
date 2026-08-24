package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.CascadeTarget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Dims the button TEXT along with its frame (NeoForge / Mojmap, 1.21.1 era). */
@Mixin(AbstractButton.class)
public abstract class PressableWidgetMixin {
    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;setColor(FFFF)V", ordinal = 1))
    private void reanimated$fadeText(GuiGraphics graphics, float r, float g, float b, float a) {
        float widgetAlpha = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        graphics.setColor(r, g, b, widgetAlpha);
    }
}
