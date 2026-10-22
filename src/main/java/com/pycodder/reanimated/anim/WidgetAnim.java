package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/**
 * Анимации отдельных виджетов: ручка ползунка, чекбокс, рамка поля ввода.
 * Состояние живёт в самих виджетах (их миксинах), здесь — общая математика.
 */
public final class WidgetAnim {
    private WidgetAnim() {}

    /** Длительность «отскока» отметки в чекбоксе. */
    private static final float CHECK_MS = 240f;
    /** С какого масштаба отметка начинает отскок. */
    private static final float CHECK_FROM = 0.7f;
    /** Пока ползунок тянут мышью, ручка догоняет быстрее — иначе ощущается вязкость. */
    private static final float DRAG_BOOST = 2.5f;

    public static boolean enabled() {
        return ReAnimatedConfig.get().widgetsEnabled;
    }

    /** Масштаб отметки чекбокса через {@code sinceMs} после переключения; 1 — покой. */
    public static float checkScale(long sinceMs) {
        if (!enabled() || sinceMs < 0 || sinceMs >= CHECK_MS) return 1f;
        return CHECK_FROM + (1f - CHECK_FROM) * Easing.outBack(sinceMs / CHECK_MS);
    }

    /** Значение, которое плавно догоняет цель. Один экземпляр на виджет. */
    public static final class Follower {
        private float value = Float.NaN;
        private long time = 0L;

        /** Шаг к цели; {@code fast} — догонять быстрее (ползунок тянут мышью). */
        public float step(float target, boolean fast) {
            long now = Anim.now();
            if (!enabled() || Float.isNaN(value)) {
                value = target;
                time = now;
                return target;
            }
            float dt = Math.min(0.2f, (now - time) / 1000f);
            time = now;
            if (value != target) {
                float speed = ReAnimatedConfig.get().widgetSpeed * (fast ? DRAG_BOOST : 1f);
                value = Easing.approach(value, target, dt, speed);
                if (Math.abs(value - target) < 0.001f) value = target;
            }
            return value;
        }
    }
}
