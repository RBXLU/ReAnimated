package com.pycodder.reanimated.anim;

import com.mojang.blaze3d.vertex.PoseStack;

/** Transform of the whole SCREEN (UI), driven by the PRESET ({@link UiPreset}). */
public final class UiTransform {
    private UiTransform() {}

    public static void forward(PoseStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy, 0f);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    public static void inverse(PoseStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(0f, -sy, 0f);
    }

    public static void pivotScale(PoseStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }
}
