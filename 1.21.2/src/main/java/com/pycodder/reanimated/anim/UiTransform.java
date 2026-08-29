package com.pycodder.reanimated.anim;

import net.minecraft.client.util.math.MatrixStack;

/** Transform of the whole SCREEN (UI), driven by the PRESET ({@link UiPreset}). */
public final class UiTransform {
    private UiTransform() {}

    public static void forward(MatrixStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy, 0f);
        if (sc != 1f) {
            pivotScaleRaw(m, width / 2f, height / 2f, sc, sc);
        }
        OwnTransform.push(sc, sc, (1f - sc) * width / 2f, (1f - sc) * height / 2f + sy);
    }

    public static void inverse(MatrixStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScaleRaw(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(0f, -sy, 0f);
        float inv = 1f / sc;
        OwnTransform.push(inv, inv, (1f - inv) * width / 2f, (1f - inv) * height / 2f - sy * inv);
    }

    public static void pivotScale(MatrixStack m, float px, float py, float sx, float sy) {
        pivotScaleRaw(m, px, py, sx, sy);
        OwnTransform.push(sx, sy, px * (1f - sx), py * (1f - sy));
    }

    public static void offsetPivotScale(MatrixStack m, float dx, float dy,
                                        float px, float py, float sx, float sy) {
        m.translate(dx, dy, 0f);
        pivotScaleRaw(m, px, py, sx, sy);
        OwnTransform.push(sx, sy, px * (1f - sx) + dx, py * (1f - sy) + dy);
    }

    public static void translate(MatrixStack m, float dx, float dy) {
        m.translate(dx, dy, 0f);
        OwnTransform.push(1f, 1f, dx, dy);
    }

    private static void pivotScaleRaw(MatrixStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }
}
