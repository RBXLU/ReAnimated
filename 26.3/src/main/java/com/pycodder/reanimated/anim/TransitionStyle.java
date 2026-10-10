package com.pycodder.reanimated.anim;

public enum TransitionStyle {
    NONE("No transition"),
    SLIDE("Slide sideways"),
    ZOOM("Zoom"),
    SLIDE_ZOOM("Slide + zoom");

    public final String display;

    TransitionStyle(String display) {
        this.display = display;
    }

    public boolean isSlide() {
        return this == SLIDE || this == SLIDE_ZOOM;
    }

    public boolean isZoom() {
        return this == ZOOM || this == SLIDE_ZOOM;
    }
}
