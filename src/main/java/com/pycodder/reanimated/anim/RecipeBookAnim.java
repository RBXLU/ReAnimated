package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/**
 * Появление окна книги рецептов. Ваниль показывает его мгновенно: панель уже стоит,
 * и рядом просто возникает вторая. Здесь окно вырастает из-под панели — масштаб от
 * её левого края плюс короткий сдвиг вбок.
 *
 * Момент появления берётся у самого виджета: он живёт столько же, сколько экран,
 * поэтому анимация играет и когда книгу раскрывают кнопкой, и когда экран открылся
 * с уже раскрытой книгой (виджет создан заново — значит, для игрока это появление).
 */
public final class RecipeBookAnim {
    private RecipeBookAnim() {}

    /**
     * Сырой прогресс появления 0..1; 1 — анимация закончилась или выключена.
     * Сглаживание отдельно ({@link #eased}): кривые с отскоком выходят за единицу,
     * и по ним нельзя понять, что анимация доиграла.
     */
    public static float progress(long openedAt) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.recipeBookEnabled || c.pvpMode || openedAt == 0L) return 1f;
        float duration = Math.max(1f, c.recipeBookDurationMs);
        return Easing.clamp01((Anim.now() - openedAt) / duration);
    }

    /** Прогресс по выбранной кривой — может выйти за единицу, это отскок. */
    public static float eased(float progress) {
        return ReAnimatedConfig.get().recipeBookEasing.apply(progress);
    }

    /** Масштаб окна по сглаженному прогрессу. */
    public static float scale(float eased) {
        float from = ReAnimatedConfig.get().recipeBookScale;
        return from + (1f - from) * eased;
    }

    /** Сдвиг влево, который окно отыгрывает к концу появления. */
    public static float offsetX(float eased) {
        return -ReAnimatedConfig.get().recipeBookOffsetX * (1f - eased);
    }
}
