package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

public final class TooltipAnim {
    private TooltipAnim() {}

    private static final long GAP_MS = 150L;

    private static long lastDrawTime = 0L;
    private static long startTime = 0L;
    private static int lastSize = -1;

    public static float progress(int componentCount) {
        long now = Anim.now();
        if (now - lastDrawTime > GAP_MS || componentCount != lastSize) {
            startTime = now;
            lastSize = componentCount;
        }
        lastDrawTime = now;
        float duration = Math.max(1f, ReAnimatedConfig.get().tooltipDurationMs);
        return Easing.clamp01((now - startTime) / duration);
    }
}
