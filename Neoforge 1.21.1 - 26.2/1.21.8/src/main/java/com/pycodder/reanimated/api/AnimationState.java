package com.pycodder.reanimated.api;

public final class AnimationState {

    public static final AnimationState IDLE = new AnimationState(false, false, false, 1f, 0f, 0f, 1f);

    private final boolean animating;
    private final boolean closing;
    private final boolean transitioning;
    private final float progress;
    private final float offsetX;
    private final float offsetY;
    private final float scale;

    public AnimationState(boolean animating, boolean closing, boolean transitioning,
                          float progress, float offsetX, float offsetY, float scale) {
        this.animating = animating;
        this.closing = closing;
        this.transitioning = transitioning;
        this.progress = progress;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
    }

    public boolean isAnimating() {
        return animating;
    }

    public boolean isClosing() {
        return closing;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public float progress() {
        return progress;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public float scale() {
        return scale;
    }
}
