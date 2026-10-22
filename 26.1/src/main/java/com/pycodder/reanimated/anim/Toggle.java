package com.pycodder.reanimated.anim;

/**
 * Переключатель в настройках отдельного экрана: взять общее значение, включить или
 * выключить только для этого экрана.
 */
public enum Toggle {
    INHERIT,
    ON,
    OFF;

    /** Значение для экрана: своё, если задано, иначе общее. */
    public boolean resolve(boolean general) {
        return this == ON || (this != OFF && general);
    }

    public Toggle next() {
        Toggle[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    /** Своё значение, если задано, иначе запасное. Для сложения правил экрана и мода. */
    public static Toggle or(Toggle own, Toggle fallback) {
        return own == null || own == INHERIT ? (fallback == null ? INHERIT : fallback) : own;
    }
}
