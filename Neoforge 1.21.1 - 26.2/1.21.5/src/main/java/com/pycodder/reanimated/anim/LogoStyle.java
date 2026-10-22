package com.pycodder.reanimated.anim;

/** Стиль анимации логотипа "Minecraft" на главном экране. */
public enum LogoStyle {
    /** Родная анимация мода: логотип целиком "вырастает" с отскоком. */
    GROW("Grow"),
    /** Побуквенный каскад: каждая буква MINECRAFT влетает/проявляется по очереди. */
    LETTERS("Letters cascade");

    public final String display;

    LogoStyle(String display) {
        this.display = display;
    }
}
