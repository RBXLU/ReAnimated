package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/**
 * Выезд уведомления (toast) сбоку экрана.
 *
 * Ваниль уже двигает уведомление, но по жёстко заданной кривой: доля видимой ширины
 * считается как {@code f * f} от времени с начала показа за фиксированные 600 мс —
 * то есть с медленным стартом и резкой остановкой. Мод не заводит своего таймера, а
 * перекладывает ту же самую точку таймлайна на выбранную кривую: из ванильной доли
 * извлекается линейное время, к нему применяется easing и длительность из настроек,
 * и результат возвращается обратно ванильному коду.
 *
 * Такой пересчёт не трогает ни момент появления, ни момент исчезновения уведомления —
 * меняется только характер движения.
 */
public final class ToastAnim {
    private ToastAnim() {}

    /** Длительность выезда в ванили (мс). Дольше неё анимацию растянуть нельзя. */
    public static final int VANILLA_MS = 600;

    /**
     * Насколько уведомлению позволено «перелететь» точку покоя. Кривые с отскоком
     * (Out Back) дают больше 1 — уведомление заезжает чуть глубже в экран и
     * возвращается. Ограничение не даёт ему уехать за середину экрана на широких
     * уведомлениях.
     */
    private static final float MAX_OVERSHOOT = 1.15f;

    /**
     * Пересчитывает ванильную долю видимой ширины по настройкам мода.
     *
     * @param portion доля из ванили: 1 — уведомление полностью на экране
     * @param hiding  true, если уведомление уезжает обратно
     */
    public static float remap(float portion, boolean hiding) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        // Ваниль: portion = f*f при показе и 1 - f*f при скрытии, где f = t/600.
        // Обратный ход даёт линейное время, единое для обоих направлений.
        float squared = Easing.clamp01(hiding ? 1f - portion : portion);
        float linear = (float) Math.sqrt(squared);

        // Своя длительность — то же линейное время, просто быстрее доходит до единицы.
        int duration = Math.min(VANILLA_MS, Math.max(1, c.toastDurationMs));
        float t = Easing.clamp01(linear * VANILLA_MS / duration);

        float eased = c.toastEasing.apply(t);
        if (eased > MAX_OVERSHOOT) eased = MAX_OVERSHOOT;
        if (eased < 0f) eased = 0f;
        return hiding ? 1f - eased : eased;
    }
}
