package com.pycodder.reanimated.anim;

import com.mojang.blaze3d.vertex.PoseStack;

public final class UiTransform {
    private UiTransform() {}

    public static void forward(PoseStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(sx, sy, 0f);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    public static void inverse(PoseStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(-sx, -sy, 0f);
    }

    public static void pivotScale(PoseStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }

    public static void pushPopup(PoseStack m, float px, float py, float scale, float dy) {
        m.pushPose();
        m.translate(0f, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    public static void popPopup(PoseStack m) {
        m.popPose();
    }

    public static boolean enforce(PoseStack m, float width, float height, boolean container) {
        org.joml.Matrix4f p = m.last().pose();
        if (!isIdentity(p.m00(), p.m01(), p.m10(), p.m11(), p.m30(), p.m31())) {
            return false;
        }
        m.pushPose();
        forward(m, width, height, container);
        return true;
    }

    public static void release(PoseStack m) {
        m.popPose();
    }

    private static boolean isIdentity(float m00, float m01, float m10, float m11, float tx, float ty) {
        return Math.abs(m00 - 1f) < 1.0E-4F && Math.abs(m11 - 1f) < 1.0E-4F
                && Math.abs(m01) < 1.0E-4F && Math.abs(m10) < 1.0E-4F
                && Math.abs(tx) < 1.0E-3F && Math.abs(ty) < 1.0E-3F;
    }

    private static final org.joml.Matrix4f BASE = new org.joml.Matrix4f();
    private static boolean baseSaved = false;

    public static void saveBase(PoseStack m) {
        BASE.set(m.last().pose());
        baseSaved = true;
    }

    public static boolean pushBase(PoseStack m) {
        if (!baseSaved) return false;
        m.pushPose();
        m.last().pose().set(BASE);
        return true;
    }

    public static void popBase(PoseStack m) {
        m.popPose();
    }

    public static void pushAppear(PoseStack m, float px, float py, float scale, float dx, float dy) {
        m.pushPose();
        m.translate(dx, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    public static void popAppear(PoseStack m) {
        m.popPose();
    }
}
