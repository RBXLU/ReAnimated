package com.pycodder.reanimated.anim;

import org.joml.Matrix3x2fStack;

/**
 * Трансформация ЭКРАНА (UI) целиком — задаётся ПРЕСЕТОМ ({@link UiPreset}).
 * Это «анимация UI»: экран/панель контейнера выезжает/масштабируется.
 *
 * Покнопочная «анимация кнопок» (профиль/Студия) — отдельный слой, он кладётся
 * ПОВЕРХ этой трансформации в {@code ClickableWidgetMixin} и здесь не участвует.
 * Так пресет (UI) и профиль (кнопки) работают вместе, а не исключают друг друга.
 *
 * Версия для 1.21.6+: GUI рисуется 2D-матрицей {@link Matrix3x2fStack}
 * (translate/scale в два аргумента, pushMatrix/popMatrix).
 */
public final class UiTransform {
    private UiTransform() {}

    public static void forward(Matrix3x2fStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    /** Обратная трансформация — строго в обратном порядке к {@link #forward}. */
    public static void inverse(Matrix3x2fStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(0f, -sy);
    }

    public static void pivotScale(Matrix3x2fStack m, float px, float py, float sx, float sy) {
        m.translate(px, py);
        m.scale(sx, sy);
        m.translate(-px, -py);
    }
}
