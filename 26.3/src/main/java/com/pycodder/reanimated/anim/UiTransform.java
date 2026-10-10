package com.pycodder.reanimated.anim;

import org.joml.Matrix3x2fStack;

public final class UiTransform {
    private UiTransform() {}

    public static void forward(Matrix3x2fStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(sx, sy);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    public static void inverse(Matrix3x2fStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(-sx, -sy);
    }

    public static void pivotScale(Matrix3x2fStack m, float px, float py, float sx, float sy) {
        m.translate(px, py);
        m.scale(sx, sy);
        m.translate(-px, -py);
    }

    public static void pushPopup(Matrix3x2fStack m, float px, float py, float scale, float dy) {
        m.pushMatrix();
        m.translate(0f, dy);
        if (scale != 1f) {
            m.translate(px, py);
            m.scale(scale, scale);
            m.translate(-px, -py);
        }
    }

    public static void popPopup(Matrix3x2fStack m) {
        m.popMatrix();
    }

    public static boolean enforce(Matrix3x2fStack m, float width, float height, boolean container) {
        if (!isIdentity(m.m00(), m.m01(), m.m10(), m.m11(), m.m20(), m.m21())) {
            return false;
        }
        m.pushMatrix();
        forward(m, width, height, container);
        return true;
    }

    public static void release(Matrix3x2fStack m) {
        m.popMatrix();
    }

    private static boolean isIdentity(float m00, float m01, float m10, float m11, float tx, float ty) {
        return Math.abs(m00 - 1f) < 1.0E-4F && Math.abs(m11 - 1f) < 1.0E-4F
                && Math.abs(m01) < 1.0E-4F && Math.abs(m10) < 1.0E-4F
                && Math.abs(tx) < 1.0E-3F && Math.abs(ty) < 1.0E-3F;
    }

    private static final org.joml.Matrix3x2f BASE = new org.joml.Matrix3x2f();
    private static boolean baseSaved = false;

    public static void saveBase(Matrix3x2fStack m) {
        BASE.set((org.joml.Matrix3x2fc) m);
        baseSaved = true;
    }

    public static boolean pushBase(Matrix3x2fStack m) {
        if (!baseSaved) return false;
        m.pushMatrix();
        m.set((org.joml.Matrix3x2fc) BASE);
        return true;
    }

    public static void popBase(Matrix3x2fStack m) {
        m.popMatrix();
    }

    public static void pushAppear(Matrix3x2fStack m, float px, float py, float scale, float dx, float dy) {
        m.pushMatrix();
        m.translate(dx, dy);
        if (scale != 1f) {
            pivotScale(m, px, py, scale, scale);
        }
    }

    public static void popAppear(Matrix3x2fStack m) {
        m.popMatrix();
    }
}
