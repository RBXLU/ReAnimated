package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/** State and math behind the open animations. */
public final class Anim {
    private Anim() {}

    private static final float FROM_BACKGROUND_SCALE = 0.75f;
    private static final float FROM_FOREGROUND_SCALE = 1.25f;

    public static long currentOpenTime = 0L;

    public static int cascadeCount = 1;

    public static boolean currentIsPause = false;

    public static long closeStartTime = 0L;
    private static float closeStartProgress = 0f;
    private static float closeStartElapsedMs = 0f;
    public static volatile boolean bypassClose = false;

    public static boolean isClosing() {
        return closeStartTime > 0L;
    }

    public static void beginClose(boolean container) {
        closeStartProgress = progress(container);
        AnimProfile p = profile();
        float openedMs = currentOpenTime <= 0L ? 0f : (System.currentTimeMillis() - currentOpenTime);
        closeStartElapsedMs = Math.min(openedMs, p.totalMs(cascadeCount));
        closeStartTime = System.currentTimeMillis();
    }

    public static void finishClose() {
        closeStartTime = 0L;
        closeStartProgress = 0f;
        closeStartElapsedMs = 0f;
    }

    private static float presetDurationMs(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return presetLayerActive(container) ? duration(c, container) * 1000f : 0f;
    }

    public static boolean presetLayerActive(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return enabled(c, container) && preset(c, container) != UiPreset.NONE;
    }

    private static float profileDurationMs() {
        AnimProfile p = profile();
        return p.enabled ? p.totalMs(cascadeCount) : 0f;
    }

    private static float presetCloseNorm(boolean container) {
        float d = presetDurationMs(container);
        if (d <= 0f) return 1f;
        return Easing.clamp01((System.currentTimeMillis() - closeStartTime) / d);
    }

    private static float profileCloseNorm() {
        float d = profileDurationMs();
        if (d <= 0f) return 1f;
        return Easing.clamp01((System.currentTimeMillis() - closeStartTime) / d);
    }

    public static boolean closeFinished(boolean container) {
        if (!isClosing()) return true;
        float max = Math.max(presetDurationMs(container), profileDurationMs());
        if (max <= 0f) return true;
        return (System.currentTimeMillis() - closeStartTime) >= max;
    }

    public static AnimProfile profile() {
        return ReAnimatedConfig.get().profile;
    }

    public static float profileElapsedMs() {
        if (isClosing()) {
            return closeStartElapsedMs * (1f - profileCloseNorm());
        }
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return System.currentTimeMillis() - currentOpenTime;
    }

    public static float profileEase(int slot) {
        return profile().progress(profileElapsedMs(), slot);
    }

    private static float virtualOpenProgress(boolean container) {
        return closeStartProgress * (1f - presetCloseNorm(container));
    }

    public static float elapsed(long now) {
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return (now - currentOpenTime) / 1000f;
    }

    private static float duration(ReAnimatedConfig c, boolean container) {
        int ticks = isPause(container) ? c.pauseSpeedTicks : c.animationSpeedTicks;
        return Math.max(1, ticks) / 20f;
    }

    private static boolean isPause(boolean container) {
        return currentIsPause && !container;
    }

    private static UiPreset preset(ReAnimatedConfig c, boolean container) {
        if (isPause(container) && c.pausePreset != null && c.pausePreset != UiPreset.INHERIT) {
            return c.pausePreset;
        }
        return c.uiPreset == null || c.uiPreset == UiPreset.INHERIT ? UiPreset.DEFAULT : c.uiPreset;
    }

    public static boolean isPauseScreen(Object screen) {
        if (screen == null) return false;
        for (Class<?> k = screen.getClass(); k != null && k != Object.class; k = k.getSuperclass()) {
            String n = k.getSimpleName();
            if ("GameMenuScreen".equals(n) || "PauseScreen".equals(n)) return true;
        }
        return false;
    }

    public static boolean shouldAnimate(Object screen) {
        if (screen == null) return false;
        Class<?> cls = screen.getClass();
        for (Class<?> k = cls; k != null && k != Object.class; k = k.getSuperclass()) {
            if ("ChatScreen".equals(k.getSimpleName())) return false;
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.animateModdedScreens && !cls.getName().startsWith("net.minecraft.")) return false;
        return true;
    }

    private static float progress(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float e = elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) return 1f;
        return Easing.clamp01(e / Math.max(0.01f, duration(c, container)));
    }

    private static boolean enabled(ReAnimatedConfig c, boolean container) {
        if (container) return c.containerEnabled;
        return isPause(container) ? c.pauseEnabled : c.screenOpenEnabled;
    }

    public static float slideY(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container) || preset(c, container) != UiPreset.DEFAULT) return 0f;
        float p = isClosing() ? virtualOpenProgress(container) : progress(container);
        EasingType easing;
        float distance;
        if (container) {
            easing = c.containerEasing;
            distance = c.containerDistance;
        } else if (isPause(container)) {
            easing = c.pauseEasing;
            distance = c.pauseDistance;
        } else {
            easing = c.screenOpenEasing;
            distance = c.screenOpenDistance;
        }
        return (1f - easing.apply(p)) * distance;
    }

    public static boolean transformActive(boolean container) {
        return slideY(container) != 0f || scale(container) != 1f;
    }

    public static float backgroundFade(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.bgFadeEnabled) return 1f;
        float p;
        if (isClosing()) {
            float d = Math.max(1f, duration(c, container) * 1000f);
            float norm = Easing.clamp01((System.currentTimeMillis() - closeStartTime) / d);
            p = closeStartProgress * (1f - norm);
        } else {
            p = progress(container);
        }
        return Easing.outCubic(p);
    }

    public static float scale(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        UiPreset preset = preset(c, container);
        if (!enabled(c, container) || !preset.isScale()) return 1f;
        float p = isClosing() ? virtualOpenProgress(container) : progress(container);
        if (preset == UiPreset.FROM_BACKGROUND) {
            return Easing.lerp(FROM_BACKGROUND_SCALE, 1f, Easing.outBack(p));
        }
        return Easing.lerp(FROM_FOREGROUND_SCALE, 1f, Easing.outCubic(p));
    }

    public static float screenSlide() {
        return slideY(false);
    }

    public static float containerSlide() {
        return slideY(true);
    }
}
