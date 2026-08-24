package com.pycodder.reanimated.anim;

/** Letter-by-letter cascade of the "Minecraft" logo. */
public final class LogoLetters {
    private LogoLetters() {}

    public static final String[] FILES = { "m", "i", "n", "e", "c", "r", "a", "f", "t" };
    public static final int COUNT = 9;

    public static final int LOGO_WIDTH = 256;
    public static final int LOGO_HEIGHT = 44;
    public static final int LOGO_BASE_Y = 30;
    public static final int LOGO_TEXTURE_HEIGHT = 64;

    public static int slot(AnimProfile p, int i) {
        if (p.cascadeOrder == CascadeOrder.SIMULTANEOUS) return 0;
        return p.cascadeOrder == CascadeOrder.BOTTOM_TO_TOP ? (COUNT - 1 - i) : i;
    }

    public static float easedFor(AnimProfile p, float elapsedMs, int i) {
        return p.progress(elapsedMs, slot(p, i));
    }

    public static float totalMs(AnimProfile p) {
        return p.totalMs(COUNT);
    }

    public static AnimProfile defaultProfile() {
        AnimProfile p = new AnimProfile();
        p.enabled = true;
        p.durationMs = 350;
        p.offsetX = 0f;
        p.offsetY = 18f;
        p.scaleX = 1f;
        p.scaleY = 1f;
        p.initialAlpha = 0f;
        p.cascadeDelayMs = 55;
        p.cascadeOrder = CascadeOrder.TOP_TO_BOTTOM;
        p.pivot = PivotPoint.CENTER;
        p.easing = EasingType.OUT_BACK;
        return p;
    }
}
