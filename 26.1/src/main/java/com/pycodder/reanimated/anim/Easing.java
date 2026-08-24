package com.pycodder.reanimated.anim;

/** Easing functions used by the UI animations. */
public final class Easing {
    private Easing() {}

    public static float clamp01(float t) {
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t;
    }

    public static float outCubic(float t) {
        t = clamp01(t);
        float f = 1f - t;
        return 1f - f * f * f;
    }

    public static float outExpo(float t) {
        t = clamp01(t);
        return t >= 1f ? 1f : 1f - (float) Math.pow(2.0, -10.0 * t);
    }

    public static float outBack(float t) {
        t = clamp01(t);
        final float c1 = 1.70158f;
        final float c3 = c1 + 1f;
        float f = t - 1f;
        return 1f + c3 * f * f * f + c1 * f * f;
    }

    public static float approach(float current, float target, float dtSeconds, float speed) {
        float rate = 1f - (float) Math.exp(-speed * dtSeconds);
        if (rate < 0f) rate = 0f;
        if (rate > 1f) rate = 1f;
        return current + (target - current) * rate;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
