package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

public final class ToastAnim {
    private ToastAnim() {}

    public static final int VANILLA_MS = 600;

    private static final float MAX_OVERSHOOT = 1.15f;

    public static float remap(float portion, boolean hiding) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float squared = Easing.clamp01(hiding ? 1f - portion : portion);
        float linear = (float) Math.sqrt(squared);

        int duration = Math.min(VANILLA_MS, Math.max(1, c.toastDurationMs));
        float t = Easing.clamp01(linear * VANILLA_MS / duration);

        float eased = c.toastEasing.apply(t);
        if (eased > MAX_OVERSHOOT) eased = MAX_OVERSHOOT;
        if (eased < 0f) eased = 0f;
        return hiding ? 1f - eased : eased;
    }
}
