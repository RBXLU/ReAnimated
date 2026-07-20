package com.pycodder.reanimated.anim;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Трансформация ЭКРАНА (UI) целиком — задаётся ПРЕСЕТОМ ({@link UiPreset}).
 * Это «анимация UI»: экран/панель контейнера выезжает/масштабируется.
 *
 * Покнопочная «анимация кнопок» (профиль/Студия) — отдельный слой, он кладётся
 * ПОВЕРХ этой трансформации в {@code ClickableWidgetMixin} и здесь не участвует.
 * Так пресет (UI) и профиль (кнопки) работают вместе, а не исключают друг друга.
 *
 * {@link #forward} едет вместе с содержимым экрана, {@link #inverse} возвращает
 * на место фон/блюр.
 */
public final class UiTransform {
    private UiTransform() {}

    public static void forward(MatrixStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy, 0f);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    /** Обратная трансформация — строго в обратном порядке к {@link #forward}. */
    public static void inverse(MatrixStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(0f, -sy, 0f);
    }

    public static void pivotScale(MatrixStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }
}
