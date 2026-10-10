package com.pycodder.reanimated.anim;

public enum UiPreset {
    DEFAULT("Default"),
    FROM_BACKGROUND("From background"),
    NONE("No animation"),
    FROM_FOREGROUND("From foreground"),
    INHERIT("Same as general"),
    SLIDE_DOWN("Slide down"),
    SLIDE_LEFT("Slide from left"),
    SLIDE_RIGHT("Slide from right"),
    POP("Pop"),
    CUSTOM("Extension");

    public static final UiPreset[] MAIN = {
        DEFAULT, SLIDE_DOWN, SLIDE_LEFT, SLIDE_RIGHT, FROM_BACKGROUND, FROM_FOREGROUND, POP, NONE
    };

    public final String display;

    UiPreset(String display) {
        this.display = display;
    }

    public boolean isScale() {
        return this == FROM_BACKGROUND || this == FROM_FOREGROUND || this == POP;
    }

    public boolean isHorizontal() {
        return this == SLIDE_LEFT || this == SLIDE_RIGHT;
    }

    public boolean isVertical() {
        return this == DEFAULT || this == SLIDE_DOWN;
    }
}
