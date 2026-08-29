package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.WindowRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fixes the "Scissor size must be >0" crash on Minecraft 26.2. */
@Mixin(GuiRenderer.class)
public class ScissorClampMixin {
    @Inject(method = "enableScissor(Lnet/minecraft/client/gui/navigation/ScreenRectangle;Lcom/mojang/blaze3d/systems/RenderPass;)V",
            at = @At("HEAD"), cancellable = true)
    private void reanimated$skipDegenerateScissor(ScreenRectangle area, RenderPass pass, CallbackInfo ci) {
        if (area == null) {
            return;
        }
        WindowRenderState window;
        try {
            window = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
        } catch (Throwable ignored) {
            return;
        }
        if (window == null || window.guiScale <= 0) {
            return;
        }

        int scale = window.guiScale;
        int left = area.left() * scale;
        int top = area.top() * scale;
        int right = Math.min(area.right() * scale, window.width);
        int bottom = Math.min(area.bottom() * scale, window.height);

        if (right - left <= 0 || bottom - top <= 0) {
            ci.cancel();
        }
    }
}
