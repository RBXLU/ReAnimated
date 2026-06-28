package com.pycodder.reanimated.anim;

/** Тип траектории (кривой сглаживания) анимации — выбирается в настройках. */
public enum EasingType {
    LINEAR("Linear"),
    OUT_CUBIC("Out Cubic"),
    OUT_BACK("Out Back (bounce)"),
    OUT_EXPO("Out Expo");

    public final String display;

    EasingType(String display) {
        this.display = display;
    }

    public float apply(float t) {
        switch (this) {
            case LINEAR:    return Easing.clamp01(t);
            case OUT_BACK:  return Easing.outBack(t);
            case OUT_EXPO:  return Easing.outExpo(t);
            case OUT_CUBIC:
            default:        return Easing.outCubic(t);
        }
    }
}
