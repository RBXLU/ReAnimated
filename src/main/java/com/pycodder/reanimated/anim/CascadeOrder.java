package com.pycodder.reanimated.anim;

/** Порядок, в котором элементы экрана включаются в анимацию (каскад). */
public enum CascadeOrder {
    /** Нижний элемент стартует первым. */
    BOTTOM_TO_TOP("Bottom to Top"),
    /** Верхний элемент стартует первым. */
    TOP_TO_BOTTOM("Top to Bottom"),
    /** Каскада нет — все элементы стартуют одновременно. */
    SIMULTANEOUS("Simultaneous");

    public final String display;

    CascadeOrder(String display) {
        this.display = display;
    }
}
