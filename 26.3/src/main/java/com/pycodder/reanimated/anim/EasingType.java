package com.pycodder.reanimated.anim;

public enum EasingType {
    LINEAR("Linear"),
    OUT_CUBIC("Out Cubic"),
    OUT_BACK("Out Back (bounce)"),
    OUT_EXPO("Out Expo"),
    IN_OUT_CUBIC("In-Out Cubic"),
    OUT_ELASTIC("Out Elastic (spring)"),
    OUT_BOUNCE("Out Bounce");

    public final String display;

    EasingType(String display) {
        this.display = display;
    }

    public EasingType forExit() {
        switch (this) {
            case OUT_BACK:
            case OUT_ELASTIC:
            case OUT_BOUNCE:
                return OUT_CUBIC;
            default:
                return this;
        }
    }

    public float apply(float t) {
        switch (this) {
            case LINEAR:    return Easing.clamp01(t);
            case OUT_BACK:  return Easing.outBack(t);
            case OUT_EXPO:  return Easing.outExpo(t);
            case IN_OUT_CUBIC: return Easing.inOutCubic(t);
            case OUT_ELASTIC:  return Easing.outElastic(t);
            case OUT_BOUNCE:   return Easing.outBounce(t);
            case OUT_CUBIC:
            default:        return Easing.outCubic(t);
        }
    }
}
