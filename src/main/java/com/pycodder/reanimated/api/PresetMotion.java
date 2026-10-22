package com.pycodder.reanimated.api;

/**
 * Положение экрана в один момент анимации: сдвиг в пикселях и масштаб относительно
 * центра. Из таких значений собирается любая анимация появления — своя в том числе.
 *
 * Значения «на месте» — {@code 0, 0, 1}: экран стоит там, где его нарисовала бы ваниль.
 */
public final class PresetMotion {

    /** Экран на своём месте — конец любой анимации появления. */
    public static final PresetMotion IDENTITY = new PresetMotion(0f, 0f, 1f);

    public final float offsetX;
    public final float offsetY;
    public final float scale;

    public PresetMotion(float offsetX, float offsetY, float scale) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
    }

    /** Только сдвиг. */
    public static PresetMotion offset(float x, float y) {
        return new PresetMotion(x, y, 1f);
    }

    /** Только масштаб вокруг центра экрана. */
    public static PresetMotion scaled(float scale) {
        return new PresetMotion(0f, 0f, scale);
    }

    public boolean isIdentity() {
        return offsetX == 0f && offsetY == 0f && scale == 1f;
    }
}
