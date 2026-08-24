package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.OwnTransform;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fixes content lagging behind during the animation on 1.21.1–1.21.3 (NeoForge / Mojmap). */
@Mixin(GuiGraphics.class)
public abstract class DrawContextScissorMixin {
    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$scissorX1(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$scissorY1(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapY(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$scissorX2(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$scissorY2(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapY(v);
    }
}
