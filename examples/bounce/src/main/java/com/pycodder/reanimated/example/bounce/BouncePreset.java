package com.pycodder.reanimated.example.bounce;

import com.pycodder.reanimated.api.CustomPreset;
import com.pycodder.reanimated.api.PresetMotion;

/**
 * «Прыжок»: экран падает сверху, ударяется о своё место и пару раз подпрыгивает,
 * затухая, а на ударах слегка сплющивается.
 *
 * Прогресс приходит линейным — вся кривая живёт здесь, поэтому расширение полностью
 * управляет характером движения и ни от каких настроек ReAnimated не зависит.
 */
public final class BouncePreset implements CustomPreset {

    public static final String ID = "reanimated_bounce:jump";

    /** Высота, с которой падает экран (px интерфейса). */
    private static final float HEIGHT = 56f;
    /** Насколько экран сплющивается в момент удара (доля). */
    private static final float SQUASH = 0.06f;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Jump";
    }

    @Override
    public PresetMotion motionAt(float progress) {
        float p = clamp01(progress);
        float landed = outBounce(p);
        // Экран приходит сверху: смещение отрицательное и гаснет к концу.
        float offsetY = -(1f - landed) * HEIGHT;
        // Сплющивание привязано к скорости приближения к месту: у пола оно максимально,
        // в верхней точке прыжка его нет.
        float impact = Math.max(0f, landed - previousBounce(p));
        float scale = 1f - SQUASH * impact * 6f;
        return new PresetMotion(0f, offsetY, Math.max(0.85f, scale));
    }

    /** Отскок мяча: удар о место, затем всё более слабые подскоки. */
    private static float outBounce(float t) {
        final float n1 = 7.5625f;
        final float d1 = 2.75f;
        if (t < 1f / d1) {
            return n1 * t * t;
        } else if (t < 2f / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5f / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        }
        t -= 2.625f / d1;
        return n1 * t * t + 0.984375f;
    }

    /** Значение кривой чуть раньше — по разнице видно, падаем мы сейчас или взлетаем. */
    private static float previousBounce(float t) {
        return outBounce(Math.max(0f, t - 0.03f));
    }

    private static float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }
}
