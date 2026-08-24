package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.CascadeTarget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Dims the button TEXT along with its frame. */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin {
    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;setShaderColor(FFFF)V", ordinal = 1))
    private void reanimated$fadeText(DrawContext context, float r, float g, float b, float a) {
        float widgetAlpha = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        context.setShaderColor(r, g, b, widgetAlpha);
    }
}
