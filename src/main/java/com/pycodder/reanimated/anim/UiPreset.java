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
    INHERIT("Same as general"),
    // Значения добавляются только в конец: пресет хранится в конфиге именем, а в
    // списках настроек выбирается по порядковому номеру.
    /** Выезд сверху — зеркало {@link #DEFAULT}. */
    SLIDE_DOWN("Slide down"),
    /** Выезд слева. */
    SLIDE_LEFT("Slide from left"),
    /** Выезд справа. */
    SLIDE_RIGHT("Slide from right"),
    /** Резкое «выскакивание» из точки: сильный масштаб с отскоком, без сдвига. */
    POP("Pop"),
    /**
     * Анимация из другого мода. Какая именно — хранится отдельно
     * ({@code customPresetId}), а сам список даёт {@code ReAnimatedApi}. Если мод,
     * добавивший её, убрали, экран открывается пресетом по умолчанию.
     */
    CUSTOM("Extension");

    /** Значения, доступные для ОБЩЕГО пресета — то есть все, кроме {@link #INHERIT}. */
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

    /** Пресеты, двигающие экран по горизонтали. */
    public boolean isHorizontal() {
        return this == SLIDE_LEFT || this == SLIDE_RIGHT;
    }

    /** Пресеты, двигающие экран по вертикали. */
    public boolean isVertical() {
        return this == DEFAULT || this == SLIDE_DOWN;
    }
}
