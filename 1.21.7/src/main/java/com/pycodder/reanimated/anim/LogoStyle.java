package com.pycodder.reanimated.anim;

/** Animation style of the "Minecraft" logo on the title screen. */
public enum LogoStyle {
    GROW("Grow"),
    LETTERS("Letters cascade");

    public final String display;

    LogoStyle(String display) {
        this.display = display;
    }
}
