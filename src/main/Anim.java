package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

public final class Anim {
    private Anim() {}

    public static long currentOpenTime = 0L;

    public static float elapsed(long now) {
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return (now - currentOpenTime) / 1000f;
    }

    public static float screenSlide() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.screenOpenEnabled) return 0f;
        return slide(c.screenOpenDuration, c.screenOpenDistance, c.screenOpenEasing);
    }

    public static float containerSlide() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.containerEnabled) return 0f;
        return slide(c.containerDuration, c.containerDistance, c.containerEasing);
    }

    private static float slide(float duration, float distance, EasingType easing) {
        float e = elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) return 0f;
        float p = Easing.clamp01(e / Math.max(0.01f, duration));
        return (1f - easing.apply(p)) * distance;
    }
}
