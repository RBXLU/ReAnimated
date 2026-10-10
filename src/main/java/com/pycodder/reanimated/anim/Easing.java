package com.pycodder.reanimated.anim;

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

    public static float inOutCubic(float t) {
        t = clamp01(t);
        if (t < 0.5f) return 4f * t * t * t;
        float f = -2f * t + 2f;
        return 1f - f * f * f / 2f;
    }

    public static float outElastic(float t) {
        t = clamp01(t);
        if (t == 0f || t == 1f) return t;
        final float c4 = (float) (2.0 * Math.PI / 3.0);
        return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0 - 0.75) * c4) + 1.0);
    }

    public static float outBounce(float t) {
        t = clamp01(t);
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

    public static float press(float t) {
        t = clamp01(t);
        final float down = 0.25f;
        if (t < down) {
            return outCubic(t / down);
        }
        return 1f - outBack((t - down) / (1f - down));
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
