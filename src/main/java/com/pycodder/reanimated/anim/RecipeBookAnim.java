package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

public final class RecipeBookAnim {
    private RecipeBookAnim() {}

    public static float progress(long openedAt) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.recipeBookEnabled || openedAt == 0L) return 1f;
        float duration = Math.max(1f, c.recipeBookDurationMs);
        return Easing.clamp01((Anim.now() - openedAt) / duration);
    }

    public static float eased(float progress) {
        return ReAnimatedConfig.get().recipeBookEasing.apply(progress);
    }

    public static float scale(float eased) {
        float from = ReAnimatedConfig.get().recipeBookScale;
        return from + (1f - from) * eased;
    }

    public static float offsetX(float eased) {
        return -ReAnimatedConfig.get().recipeBookOffsetX * (1f - eased);
    }
}
