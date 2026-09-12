package com.pycodder.reanimated.api;

public final class PresetMotion {

    public static final PresetMotion IDENTITY = new PresetMotion(0f, 0f, 1f);

    public final float offsetX;
    public final float offsetY;
    public final float scale;

    public PresetMotion(float offsetX, float offsetY, float scale) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
    }

    public static PresetMotion offset(float x, float y) {
        return new PresetMotion(x, y, 1f);
    }

    public static PresetMotion scaled(float scale) {
        return new PresetMotion(0f, 0f, scale);
    }

    public boolean isIdentity() {
        return offsetX == 0f && offsetY == 0f && scale == 1f;
    }
}
