package com.pycodder.reanimated.anim;

/** Order in which a screen's elements join the animation (the cascade). */
public enum CascadeOrder {
    BOTTOM_TO_TOP("Bottom to Top"),
    TOP_TO_BOTTOM("Top to Bottom"),
    SIMULTANEOUS("Simultaneous");

    public final String display;

    CascadeOrder(String display) {
        this.display = display;
    }
}
