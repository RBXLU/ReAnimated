package com.pycodder.reanimated.example.bounce;

import com.pycodder.reanimated.api.CustomPreset;
import com.pycodder.reanimated.api.PresetMotion;

public final class BouncePreset implements CustomPreset {

    public static final String ID = "reanimated_bounce:jump";

    private static final float HEIGHT = 56f;
    private static final float SQUASH = 0.06f;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Jump";
    }

    @Override
    public PresetMotion motionAt(float progress) {
        float p = clamp01(progress);
        float landed = outBounce(p);
        float offsetY = -(1f - landed) * HEIGHT;
        float impact = Math.max(0f, landed - previousBounce(p));
        float scale = 1f - SQUASH * impact * 6f;
        return new PresetMotion(0f, offsetY, Math.max(0.85f, scale));
    }

    private static float outBounce(float t) {
        final float n1 = 7.5625f;
        final float d1 = 2.75f;
        if (t < 1f / d1) {
            return n1 * t * t;
        } else if (t < 2f / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5f / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        }
        t -= 2.625f / d1;
        return n1 * t * t + 0.984375f;
    }

    private static float previousBounce(float t) {
        return outBounce(Math.max(0f, t - 0.03f));
    }

    private static float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }
}
