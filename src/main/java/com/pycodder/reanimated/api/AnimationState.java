package com.pycodder.reanimated.api;

/**
 * Снимок того, что мод делает с интерфейсом прямо сейчас. Нужен расширениям и
 * другим UI-модам, чтобы двигать свои оверлеи вместе с экраном, а не рядом с ним.
 *
 * Значения соответствуют моменту вызова {@link ReAnimatedApi#state()} и дальше не
 * меняются.
 */
public final class AnimationState {

    /** Ничего не происходит: экран стоит на месте. */
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

    /** Идёт ли анимация: если false, остальные значения — «на месте». */
    public boolean isAnimating() {
        return animating;
    }

    /** Экран закрывается (анимация играет в обратную сторону). */
    public boolean isClosing() {
        return closing;
    }

    /** Идёт переход на другой экран, а не обычное открытие или закрытие. */
    public boolean isTransitioning() {
        return transitioning;
    }

    /** Прогресс текущей анимации, 0..1. */
    public float progress() {
        return progress;
    }

    /** Горизонтальный сдвиг экрана в пикселях интерфейса. */
    public float offsetX() {
        return offsetX;
    }

    /** Вертикальный сдвиг экрана в пикселях интерфейса. */
    public float offsetY() {
        return offsetY;
    }

    /** Масштаб экрана относительно его центра. */
    public float scale() {
        return scale;
    }
}
