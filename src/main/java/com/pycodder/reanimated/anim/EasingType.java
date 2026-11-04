package com.pycodder.reanimated.anim;

/** Тип траектории (кривой сглаживания) анимации — выбирается в настройках. */
public enum EasingType {
    LINEAR("Linear"),
    OUT_CUBIC("Out Cubic"),
    OUT_BACK("Out Back (bounce)"),
    OUT_EXPO("Out Expo"),
    // Значения добавляются только в конец: в конфиге траектория хранится именем,
    // а в списках настроек — порядковым номером, и перестановка сбила бы выбор.
    IN_OUT_CUBIC("In-Out Cubic"),
    OUT_ELASTIC("Out Elastic (spring)"),
    OUT_BOUNCE("Out Bounce");

    public final String display;

    EasingType(String display) {
        this.display = display;
    }

    /**
     * Кривая, которой отмеряется УХОД экрана.
     *
     * Отскок и пружина описывают ПРИЛЁТ: они специально проскакивают цель и
     * возвращаются к ней. Если такой кривой гасить «открытость» экрана при закрытии,
     * она уводит её за ноль — панель одним кадром прыгает на всю дистанцию и стоит там
     * до конца окна. Поэтому у ухода они заменяются ближайшей монотонной кривой, а у
     * появления отскок остаётся.
     */
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
