package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.api.AnimationFilter;
import com.pycodder.reanimated.api.AnimationState;
import com.pycodder.reanimated.api.CustomPreset;
import com.pycodder.reanimated.api.PresetMotion;
import com.pycodder.reanimated.api.ReAnimatedApi;
import com.pycodder.reanimated.config.ReAnimatedConfig;

public final class Anim {
    private Anim() {}

    private static final float FROM_BACKGROUND_SCALE = 0.75f;
    private static final float FROM_FOREGROUND_SCALE = 1.25f;
    private static final float POP_SCALE = 0.6f;

    static {
        ReAnimatedApi.setStateSupplier(Anim::apiState);
    }

    public static long currentOpenTime = 0L;

    private static long frameTime = 0L;
    private static boolean frameActive = false;
    private static long frameId = 0L;

    public static void beginFrame() {
        frameTime = System.currentTimeMillis();
        frameActive = true;
        frameId++;
    }

    public static void endFrame() {
        frameActive = false;
    }

    public static long now() {
        return frameActive ? frameTime : System.currentTimeMillis();
    }

    public static Object currentScreen = null;
    public static boolean currentIsContainer = false;

    public static int cascadeCount = 1;

    public static boolean currentIsPause = false;

    public static ScreenOverride currentOverride = null;

    public static int currentEnterDir = 0;

    public static Object pendingScreen = null;
    public static int exitDir = 0;
    public static int enterDir = 0;

    private static float exitResidualX = 0f;
    private static float exitResidualY = 0f;
    private static float exitResidualScale = 1f;

    public static long closeStartTime = 0L;
    private static float closeStartProgress = 0f;
    private static float closeOpenness = 1f;
    private static float closeStartElapsedMs = 0f;
    public static volatile boolean bypassClose = false;

    public static Object closeReadyScreen = null;

    public static Object frameGraphics = null;

    public static boolean isClosing() {
        return closeStartTime > 0L;
    }

    public static boolean isTransitioning() {
        return closeStartTime > 0L && exitDir != 0;
    }

    public static void beginClose(boolean container) {
        if (exitDir == 0 && ReAnimatedApi.hasListeners()) ReAnimatedApi.fireClose(currentScreen, null);
        closeStartProgress = progress(container);
        AnimProfile p = profile();
        float openedMs = currentOpenTime <= 0L ? 0f : (now() - currentOpenTime);
        float total = p.totalMs(cascadeCount);
        closeStartElapsedMs = Math.min(openedMs, total);
        float profileOpenness = profileLayerActive(container) && total > 0f
            ? Easing.clamp01(openedMs / total) : 0f;
        closeOpenness = Math.max(closeStartProgress, profileOpenness);
        closeStartTime = now();
    }

    public static void finishClose() {
        boolean wasClosing = closeStartTime > 0L;
        closeStartTime = 0L;
        closeStartProgress = 0f;
        closeStartElapsedMs = 0f;
        closeOpenness = 1f;
        closeReadyScreen = null;
        pendingScreen = null;
        exitDir = 0;
        exitResidualX = 0f;
        exitResidualY = 0f;
        exitResidualScale = 1f;
        if (wasClosing && ReAnimatedApi.hasListeners()) ReAnimatedApi.fireFinish(currentScreen);
    }

    public static void beginTransition(Object next, int dir) {
        exitResidualX = slideX(false);
        exitResidualY = slideY(false);
        exitResidualScale = scale(false);
        beginClose(false);
        pendingScreen = next;
        exitDir = dir;
        enterDir = -dir;
        if (ReAnimatedApi.hasListeners()) ReAnimatedApi.fireClose(currentScreen, next);
    }

    public static void clearEnterDir() {
        enterDir = 0;
    }

    public static int takeEnterDir() {
        int d = enterDir;
        enterDir = 0;
        return d;
    }

    public static boolean presetLayerActive(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return enabled(c, container) && preset(c, container) != UiPreset.NONE;
    }

    public static boolean profileLayerActive(boolean container) {
        return !container && profile().enabled;
    }

    public static boolean closeWouldAnimate(boolean container) {
        return presetLayerActive(container) || profileLayerActive(container);
    }

    private static float closeWindowMs(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float base = 0f;
        if (presetLayerActive(container)) base = duration(c, container) * 1000f;
        if (profileLayerActive(container)) base = Math.max(base, Math.max(1, profile().durationMs));
        if (base <= 0f) return 0f;
        return base * Easing.clamp01(closeOpenness);
    }

    private static float closeNorm(boolean container) {
        float d = closeWindowMs(container);
        if (d <= 0f) return 1f;
        return Easing.clamp01((now() - closeStartTime) / d);
    }

    private static float closeEase(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return easingFor(c, container).forExit().apply(closeNorm(container));
    }

    private static final long CLOSE_WATCHDOG_MS = 2500L;

    public static boolean closeFinished(boolean container) {
        if (!isClosing()) return true;
        long spent = now() - closeStartTime;
        if (spent >= CLOSE_WATCHDOG_MS) return true;
        if (exitDir != 0) return spent >= transitionDurationMs();
        float window = closeWindowMs(container);
        if (window <= 0f) return true;
        return spent >= window;
    }

    public static AnimProfile profile() {
        return ReAnimatedConfig.get().profile;
    }

    public static float profileElapsedMs() {
        if (isTransitioning()) return Float.MAX_VALUE;
        if (isClosing()) {
            return closeStartElapsedMs * (1f - closeEase(currentIsContainer));
        }
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return now() - currentOpenTime;
    }

    public static float profileEase(int slot) {
        return profile().progress(profileElapsedMs(), slot);
    }

    private static float virtualOpenProgress(boolean container) {
        return Easing.clamp01(closeStartProgress * (1f - closeEase(container)));
    }

    public static float elapsed(long now) {
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return (now - currentOpenTime) / 1000f;
    }

    private static float duration(ReAnimatedConfig c, boolean container) {
        ScreenOverride o = currentOverride;
        if (o != null && o.speedTicks > 0) return o.speedTicks / 20f;
        int ticks = isPause(container) ? c.pauseSpeedTicks : c.animationSpeedTicks;
        return Math.max(1, ticks) / 20f;
    }

    private static boolean isPause(boolean container) {
        return currentIsPause && !container;
    }

    private static UiPreset preset(ReAnimatedConfig c, boolean container) {
        ScreenOverride o = currentOverride;
        if (o != null && o.preset != null && o.preset != UiPreset.INHERIT) {
            return o.preset;
        }
        if (isPause(container) && c.pausePreset != null && c.pausePreset != UiPreset.INHERIT) {
            return c.pausePreset;
        }
        UiPreset general = c.uiPreset == null || c.uiPreset == UiPreset.INHERIT ? UiPreset.DEFAULT : c.uiPreset;
        return resolveCustom(general, c);
    }

    private static UiPreset resolveCustom(UiPreset preset, ReAnimatedConfig c) {
        if (preset != UiPreset.CUSTOM) return preset;
        return customPreset(c) == null ? UiPreset.DEFAULT : UiPreset.CUSTOM;
    }

    private static CustomPreset customPreset(ReAnimatedConfig c) {
        ScreenOverride o = currentOverride;
        String id = null;
        if (o != null && o.preset == UiPreset.CUSTOM && o.customPresetId != null && !o.customPresetId.isEmpty()) {
            id = o.customPresetId;
        } else if (c.customPresetId != null && !c.customPresetId.isEmpty()) {
            id = c.customPresetId;
        }
        return ReAnimatedApi.preset(id);
    }

    private static CustomPreset motionOwner = null;
    private static long motionStamp = -1L;
    private static float motionProgress = -1f;
    private static PresetMotion motionCache = PresetMotion.IDENTITY;

    private static PresetMotion customMotion(ReAnimatedConfig c, boolean container) {
        CustomPreset cp = customPreset(c);
        if (cp == null) return PresetMotion.IDENTITY;
        float p = presetProgress(container);
        long stamp = now();
        if (cp == motionOwner && stamp == motionStamp && p == motionProgress) {
            return motionCache;
        }
        PresetMotion m = ReAnimatedApi.motionOf(cp, p);
        motionOwner = cp;
        motionStamp = stamp;
        motionProgress = p;
        motionCache = m;
        return m;
    }

    public static boolean isPauseScreen(Object screen) {
        ScreenFlags f = ScreenFlags.of(screen);
        return f != null && f.pause;
    }

    private static Object animCacheScreen = null;
    private static long animCacheFrame = -1L;
    private static boolean animCacheValue = false;

    public static boolean shouldAnimate(Object screen) {
        if (screen == null) return false;
        if (frameActive && animCacheFrame == frameId && animCacheScreen == screen) {
            return animCacheValue;
        }
        boolean result = computeShouldAnimate(screen);
        if (frameActive) {
            animCacheScreen = screen;
            animCacheFrame = frameId;
            animCacheValue = result;
        }
        return result;
    }

    private static boolean computeShouldAnimate(Object screen) {
        ScreenOverride o = ScreenRules.get(screen);
        if (o != null && !o.animate) return false;
        if (ReAnimatedApi.hasFilters()) {
            AnimationFilter.Result r = ReAnimatedApi.askFilters(screen);
            if (r == AnimationFilter.Result.SKIP) return false;
            if (r == AnimationFilter.Result.ANIMATE) return true;
        }
        ScreenFlags f = ScreenFlags.of(screen);
        if (f.chat) return false;
        return ReAnimatedConfig.get().animateModdedScreens || f.vanilla;
    }

    private static float progress(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float e = elapsed(now());
        if (e == Float.MAX_VALUE) return 1f;
        return Easing.clamp01(e / Math.max(0.01f, duration(c, container)));
    }

    private static boolean enabled(ReAnimatedConfig c, boolean container) {
        ScreenOverride o = currentOverride;
        if (o != null && !o.animate) return false;
        if (container) return c.containerEnabled;
        return isPause(container) ? c.pauseEnabled : c.screenOpenEnabled;
    }

    private static float presetProgress(boolean container) {
        return isClosing() ? virtualOpenProgress(container) : progress(container);
    }

    private static EasingType easingFor(ReAnimatedConfig c, boolean container) {
        if (container) return c.containerEasing;
        return isPause(container) ? c.pauseEasing : c.screenOpenEasing;
    }

    private static float distanceFor(ReAnimatedConfig c, boolean container) {
        if (container) return c.containerDistance;
        return isPause(container) ? c.pauseDistance : c.screenOpenDistance;
    }

    private static final long[] LAYER_FRAME = {-1L, -1L};
    private static final float[] LAYER_X = new float[2];
    private static final float[] LAYER_Y = new float[2];
    private static final float[] LAYER_SCALE = new float[2];

    private static int layers(boolean container) {
        int i = container ? 1 : 0;
        if (frameActive && LAYER_FRAME[i] == frameId) return i;
        LAYER_X[i] = computeSlideX(container);
        LAYER_Y[i] = computeSlideY(container);
        LAYER_SCALE[i] = computeScale(container);
        if (frameActive) LAYER_FRAME[i] = frameId;
        return i;
    }

    public static float slideY(boolean container) {
        return LAYER_Y[layers(container)];
    }

    public static float slideX(boolean container) {
        return LAYER_X[layers(container)];
    }

    public static float scale(boolean container) {
        return LAYER_SCALE[layers(container)];
    }

    private static float computeSlideY(boolean container) {
        if (!container && isTransitioning()) {
            return exitResidualY * (1f - transitionEase());
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container)) return 0f;
        UiPreset preset = preset(c, container);
        if (preset == UiPreset.CUSTOM) return customMotion(c, container).offsetY;
        if (!preset.isVertical()) return 0f;
        float p = presetProgress(container);
        float dir = preset == UiPreset.SLIDE_DOWN ? -1f : 1f;
        return (1f - easingFor(c, container).apply(p)) * distanceFor(c, container) * dir;
    }

    public static boolean transformActive(boolean container) {
        int i = layers(container);
        return LAYER_Y[i] != 0f || LAYER_X[i] != 0f || LAYER_SCALE[i] != 1f;
    }

    public static float backgroundFade(boolean container) {
        if (!container && (isTransitioning() || (currentEnterDir != 0 && !isClosing()))) return 1f;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.bgFadeEnabled) return 1f;
        float p;
        if (isClosing()) {
            p = Easing.clamp01(closeStartProgress * (1f - closeEase(container)));
        } else {
            p = progress(container);
        }
        return Easing.outCubic(p);
    }

    private static float computeScale(boolean container) {
        return presetScale(container) * transitionScale(container);
    }

    private static float presetScale(boolean container) {
        if (!container && isTransitioning()) {
            return 1f + (exitResidualScale - 1f) * (1f - transitionEase());
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        UiPreset preset = preset(c, container);
        if (!enabled(c, container)) return 1f;
        if (preset == UiPreset.CUSTOM) return customMotion(c, container).scale;
        if (!preset.isScale()) return 1f;
        float p = presetProgress(container);
        if (preset == UiPreset.FROM_BACKGROUND) {
            return Easing.lerp(FROM_BACKGROUND_SCALE, 1f, Easing.outBack(p));
        }
        if (preset == UiPreset.POP) {
            return Easing.lerp(POP_SCALE, 1f, Easing.outBack(p));
        }
        return Easing.lerp(FROM_FOREGROUND_SCALE, 1f, Easing.outCubic(p));
    }

    private static final float TRANSITION_ZOOM = 0.06f;

    private static float transitionDurationMs() {
        return Math.max(1, ReAnimatedConfig.get().transitionSpeedTicks) / 20f * 1000f;
    }

    public static boolean transitionLayerActive() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return c.transitionEnabled && c.transitionStyle != null && c.transitionStyle != TransitionStyle.NONE;
    }

    private static float transitionPhase(boolean container) {
        if (container || !transitionLayerActive()) return Float.NaN;
        float d = transitionDurationMs();
        long now = now();
        if (isClosing()) {
            if (exitDir == 0) return Float.NaN;
            return Easing.clamp01((now - closeStartTime) / d);
        }
        if (currentEnterDir == 0 || currentOpenTime <= 0L) return Float.NaN;
        return -Easing.clamp01((now - currentOpenTime) / d);
    }

    private static float computeSlideX(boolean container) {
        return presetSlideX(container) + transitionSlideX(container);
    }

    private static float presetSlideX(boolean container) {
        if (!container && isTransitioning()) {
            return exitResidualX * (1f - transitionEase());
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container)) return 0f;
        UiPreset preset = preset(c, container);
        if (preset == UiPreset.CUSTOM) return customMotion(c, container).offsetX;
        if (!preset.isHorizontal()) return 0f;
        float p = presetProgress(container);
        float dir = preset == UiPreset.SLIDE_LEFT ? -1f : 1f;
        return (1f - easingFor(c, container).apply(p)) * distanceFor(c, container) * dir;
    }

    private static float transitionSlideX(boolean container) {
        float phase = transitionPhase(container);
        if (Float.isNaN(phase)) return 0f;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.transitionStyle.isSlide()) return 0f;
        if (phase >= 0f) {
            return c.transitionEasing.apply(phase) * c.transitionDistance * exitDir;
        }
        return (1f - c.transitionEasing.apply(-phase)) * c.transitionDistance * currentEnterDir;
    }

    private static float transitionEase() {
        if (!isTransitioning()) return 0f;
        float d = transitionDurationMs();
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return c.transitionEasing.apply(Easing.clamp01((now() - closeStartTime) / d));
    }

    public static float transitionScale(boolean container) {
        float phase = transitionPhase(container);
        if (Float.isNaN(phase)) return 1f;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.transitionStyle.isZoom()) return 1f;
        if (phase >= 0f) {
            float target = exitDir < 0 ? 1f - TRANSITION_ZOOM : 1f + TRANSITION_ZOOM;
            return Easing.lerp(1f, target, c.transitionEasing.apply(phase));
        }
        float from = currentEnterDir > 0 ? 1f + TRANSITION_ZOOM : 1f - TRANSITION_ZOOM;
        return Easing.lerp(from, 1f, c.transitionEasing.apply(-phase));
    }

    public static boolean isVolatileScreen(Object screen) {
        ScreenFlags f = ScreenFlags.of(screen);
        return f == null || f.transient_;
    }

    public static AnimationState apiState() {
        boolean container = currentIsContainer;
        if (!transformActive(container)) return AnimationState.IDLE;
        return new AnimationState(true, isClosing(), isTransitioning(),
            presetProgress(container), slideX(container), slideY(container), scale(container));
    }

    public static void onScreenOpened(Object screen) {
        if (ReAnimatedApi.hasListeners()) ReAnimatedApi.fireOpen(screen);
    }

    public static float screenSlide() {
        return slideY(false);
    }

    public static float containerSlide() {
        return slideY(true);
    }
}
