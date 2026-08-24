package com.pycodder.reanimated.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fixes clipped content lagging behind during the animation. */
@Mixin(GuiGraphicsExtractor.class)
public abstract class DrawContextScissorMixin {
    @Shadow public abstract Matrix3x2fStack pose();

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$scissorX1(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$scissorY1(int v) {
        return reanimated$mapY(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$scissorX2(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$scissorY2(int v) {
        return reanimated$mapY(v);
    }

    @Unique
    private int reanimated$mapX(int v) {
        Matrix3x2fStack m = pose();
        if (m.m00() == 1f && m.m20() == 0f) {
            return v;
        }
        return Math.round(m.m00() * v + m.m20());
    }

    @Unique
    private int reanimated$mapY(int v) {
        Matrix3x2fStack m = pose();
        if (m.m11() == 1f && m.m21() == 0f) {
            return v;
        }
        return Math.round(m.m11() * v + m.m21());
    }
}
