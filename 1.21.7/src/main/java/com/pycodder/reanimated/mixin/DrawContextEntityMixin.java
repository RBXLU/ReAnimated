package com.pycodder.reanimated.mixin;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DrawContext.class)
public abstract class DrawContextEntityMixin {

    @Shadow public abstract Matrix3x2fStack getMatrices();

    @ModifyVariable(method = {"addEntity",
        "addPlayerSkin",
        "addBookModel",
        "addBannerResult",
        "addSign",
        "addProfilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$pipX1(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = {"addEntity",
        "addPlayerSkin",
        "addBookModel",
        "addBannerResult",
        "addSign",
        "addProfilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$pipY1(int v) {
        return reanimated$mapY(v);
    }

    @ModifyVariable(method = {"addEntity",
        "addPlayerSkin",
        "addBookModel",
        "addBannerResult",
        "addSign",
        "addProfilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$pipX2(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = {"addEntity",
        "addPlayerSkin",
        "addBookModel",
        "addBannerResult",
        "addSign",
        "addProfilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$pipY2(int v) {
        return reanimated$mapY(v);
    }

    @ModifyVariable(method = {"addEntity",
        "addPlayerSkin",
        "addBookModel",
        "addSign"}, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float reanimated$pipScale(float v) {
        Matrix3x2fStack m = getMatrices();
        float s = (m.m00() + m.m11()) * 0.5f;
        return s == 1f ? v : v * s;
    }

    @Unique
    private int reanimated$mapX(int v) {
        Matrix3x2fStack m = getMatrices();
        if (m.m00() == 1f && m.m20() == 0f) {
            return v;
        }
        return Math.round(m.m00() * v + m.m20());
    }

    @Unique
    private int reanimated$mapY(int v) {
        Matrix3x2fStack m = getMatrices();
        if (m.m11() == 1f && m.m21() == 0f) {
            return v;
        }
        return Math.round(m.m11() * v + m.m21());
    }
}
