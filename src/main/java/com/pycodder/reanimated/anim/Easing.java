package com.pycodder.reanimated.anim;

/** Набор функций сглаживания (easing) для анимаций интерфейса. */
public final class Easing {
    private Easing() {}

    public static float clamp01(float t) {
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t;
    }

    /** Плавное замедление к концу. Хорошо для появления элементов. */
    public static float outCubic(float t) {
        t = clamp01(t);
        float f = 1f - t;
        return 1f - f * f * f;
    }

    /** Сильное замедление к концу. */
    public static float outExpo(float t) {
        t = clamp01(t);
        return t >= 1f ? 1f : 1f - (float) Math.pow(2.0, -10.0 * t);
    }

    /** Лёгкий "отскок" в конце — приятно для логотипа и контейнеров. */
    public static float outBack(float t) {
        t = clamp01(t);
        final float c1 = 1.70158f;
        final float c3 = c1 + 1f;
        float f = t - 1f;
        return 1f + c3 * f * f * f + c1 * f * f;
    }

    /** Разгон в начале и торможение в конце — ровное движение «из точки в точку». */
    public static float inOutCubic(float t) {
        t = clamp01(t);
        if (t < 0.5f) return 4f * t * t * t;
        float f = -2f * t + 2f;
        return 1f - f * f * f / 2f;
    }

    /**
     * Пружина: элемент проскакивает точку покоя и несколько раз затухающе
     * возвращается. Заметно живее отскока Out Back, поэтому хорош для коротких
     * анимаций — на длинных выглядит нарочито.
     */
    public static float outElastic(float t) {
        t = clamp01(t);
        if (t == 0f || t == 1f) return t;
        final float c4 = (float) (2.0 * Math.PI / 3.0);
        return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0 - 0.75) * c4) + 1.0);
    }

    /** Отскок как у мяча: элемент падает на место и подпрыгивает всё слабее. */
    public static float outBounce(float t) {
        t = clamp01(t);
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

    /**
     * Кривая нажатия кнопки: 0 -> 1 -> 0 за один проход {@code t} от 0 до 1.
     * Возвращает «глубину вдавливания» в долях: кнопка быстро уходит вниз
     * (первая четверть таймлайна) и заметно дольше возвращается с лёгким
     * перелётом вверх, отчего нажатие ощущается упругим, а не «дрожащим».
     */
    public static float press(float t) {
        t = clamp01(t);
        final float down = 0.25f;
        if (t < down) {
            return outCubic(t / down);
        }
        // Возврат: outBack, отражённый вниз — перелёт даёт кнопке «отпружинить».
        return 1f - outBack((t - down) / (1f - down));
    }

    /** Кадронезависимое экспоненциальное приближение current -> target. */
    public static float approach(float current, float target, float dtSeconds, float speed) {
        float rate = 1f - (float) Math.exp(-speed * dtSeconds);
        if (rate < 0f) rate = 0f;
        if (rate > 1f) rate = 1f;
        return current + (target - current) * rate;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
