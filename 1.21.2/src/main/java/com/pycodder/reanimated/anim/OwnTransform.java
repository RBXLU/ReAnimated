package com.pycodder.reanimated.anim;

/** Tracks ONLY this mod's own UI transforms, separately from the drawing matrix. */
public final class OwnTransform {
    private static final int MAX_DEPTH = 64;
    private static final float[] STACK = new float[MAX_DEPTH * 4];

    private static int depth = 0;
    private static int overflow = 0;

    private static float sx = 1f;
    private static float sy = 1f;
    private static float tx = 0f;
    private static float ty = 0f;

    private OwnTransform() {
    }

    public static void push(float scaleX, float scaleY, float transX, float transY) {
        if (depth >= MAX_DEPTH) {
            overflow++;
            return;
        }
        int i = depth * 4;
        STACK[i] = sx;
        STACK[i + 1] = sy;
        STACK[i + 2] = tx;
        STACK[i + 3] = ty;
        depth++;

        tx = sx * transX + tx;
        ty = sy * transY + ty;
        sx *= scaleX;
        sy *= scaleY;
    }

    public static void pop() {
        if (overflow > 0) {
            overflow--;
            return;
        }
        if (depth <= 0) {
            return;
        }
        depth--;
        int i = depth * 4;
        sx = STACK[i];
        sy = STACK[i + 1];
        tx = STACK[i + 2];
        ty = STACK[i + 3];
    }

    public static void reset() {
        depth = 0;
        overflow = 0;
        sx = 1f;
        sy = 1f;
        tx = 0f;
        ty = 0f;
    }

    public static boolean identity() {
        return sx == 1f && sy == 1f && tx == 0f && ty == 0f;
    }

    public static int mapX(int v) {
        return Math.round(sx * v + tx);
    }

    public static int mapY(int v) {
        return Math.round(sy * v + ty);
    }
}
