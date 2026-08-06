package com.pycodder.reanimated.anim;

/** Точка, относительно которой масштабируется интерфейс. Доли от ширины/высоты области. */
public enum PivotPoint {
    CENTER("Center", 0.5f, 0.5f),
    TOP("Top", 0.5f, 0f),
    BOTTOM("Bottom", 0.5f, 1f),
    LEFT("Left", 0f, 0.5f),
    RIGHT("Right", 1f, 0.5f);

    public final String display;
    public final float fx;
    public final float fy;

    PivotPoint(String display, float fx, float fy) {
        this.display = display;
        this.fx = fx;
        this.fy = fy;
    }
}
