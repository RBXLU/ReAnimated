package com.pycodder.reanimated.anim;

/** Ready-made UI open-animation preset, shared by menus and containers. */
public enum UiPreset {
    DEFAULT("Default"),
    FROM_BACKGROUND("From background"),
    NONE("No animation"),
    FROM_FOREGROUND("From foreground");

    public final String display;

    UiPreset(String display) {
        this.display = display;
    }

    public boolean isScale() {
        return this == FROM_BACKGROUND || this == FROM_FOREGROUND;
    }
}
