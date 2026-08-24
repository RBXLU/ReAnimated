package com.pycodder.reanimated.anim;

import org.joml.Matrix3x2fStack;

/** Transform of the whole SCREEN (UI), driven by the PRESET ({@link UiPreset}). */
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
