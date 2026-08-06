package com.pycodder.reanimated.anim;

/**
 * Готовый пресет анимации появления UI (единый для меню и контейнеров).
 * Фон/блюр при любом пресете остаётся на месте — анимируется только панель и контент.
 */
public enum UiPreset {
    /** Поведение по умолчанию — выезд снизу (использует слайдеры длительности/дистанции). */
    DEFAULT("Default"),
    /** Появление "из фона": панель вырастает из центра с лёгким отскоком. */
    FROM_BACKGROUND("From background"),
    /** Без анимации — обычное мгновенное открытие. */
    NONE("No animation"),
    /** Появление "с переднего плана": панель влетает, уменьшаясь до нормального размера. */
    FROM_FOREGROUND("From foreground"),
    /**
     * Не собственный пресет, а ссылка на общий ({@code uiPreset}). Существует только
     * для экранов со своими настройками — сейчас это меню паузы. Anim разворачивает
     * его в настоящий пресет до всех расчётов, дальше по коду INHERIT не встречается.
     * Стоит последним: {@link #MAIN} перечисляет значения общего пресета по порядку.
     */
    INHERIT("Same as general");

    /** Значения, доступные для ОБЩЕГО пресета — то есть все, кроме {@link #INHERIT}. */
    public static final UiPreset[] MAIN = {DEFAULT, FROM_BACKGROUND, NONE, FROM_FOREGROUND};

    public final String display;

    UiPreset(String display) {
        this.display = display;
    }

    public boolean isScale() {
        return this == FROM_BACKGROUND || this == FROM_FOREGROUND;
    }
}
