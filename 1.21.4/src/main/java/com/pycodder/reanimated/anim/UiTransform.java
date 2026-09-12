package com.pycodder.reanimated.anim;

import net.minecraft.client.util.math.MatrixStack;

public final class UiTransform {
    private UiTransform() {}

    public static void forward(MatrixStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(sx, sy, 0f);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    public static void inverse(MatrixStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(-sx, -sy, 0f);
    }

    public static void pivotScale(MatrixStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }

    public static void pushPopup(MatrixStack m, float px, float py, float scale, float dy) {
        m.push();
        m.translate(0f, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    public static void popPopup(MatrixStack m) {
        m.pop();
    }

    public static boolean enforce(MatrixStack m, float width, float height, boolean container) {
        org.joml.Matrix4f p = m.peek().getPositionMatrix();
        if (!isIdentity(p.m00(), p.m01(), p.m10(), p.m11(), p.m30(), p.m31())) {
            return false;
        }
        m.push();
        forward(m, width, height, container);
        return true;
    }

    public static void release(MatrixStack m) {
        m.pop();
    }

    private static boolean isIdentity(float m00, float m01, float m10, float m11, float tx, float ty) {
        return Math.abs(m00 - 1f) < 1.0E-4F && Math.abs(m11 - 1f) < 1.0E-4F
                && Math.abs(m01) < 1.0E-4F && Math.abs(m10) < 1.0E-4F
                && Math.abs(tx) < 1.0E-3F && Math.abs(ty) < 1.0E-3F;
    }

    private static final org.joml.Matrix4f BASE = new org.joml.Matrix4f();
    private static boolean baseSaved = false;

    public static void saveBase(MatrixStack m) {
        BASE.set(m.peek().getPositionMatrix());
        baseSaved = true;
    }

    public static boolean pushBase(MatrixStack m) {
        if (!baseSaved) return false;
        m.push();
        m.peek().getPositionMatrix().set(BASE);
        return true;
    }

    public static void popBase(MatrixStack m) {
        m.pop();
    }

    public static void pushAppear(MatrixStack m, float px, float py, float scale, float dx, float dy) {
        m.push();
        m.translate(dx, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    public static void popAppear(MatrixStack m) {
        m.pop();
    }
}
