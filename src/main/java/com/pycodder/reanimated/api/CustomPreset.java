package com.pycodder.reanimated.api;

/**
 * Своя анимация появления экрана, добавленная другим модом.
 *
 * Мод описывает не кадры, а позицию экрана в произвольный момент анимации:
 * {@link #motionAt(float)} получает прогресс 0..1 и возвращает сдвиг и масштаб.
 * Всё остальное — длительность, реверс при закрытии, взаимодействие с каскадом
 * кнопок и переходами — берёт на себя сам мод.
 *
 * <pre>{@code
 * ReAnimatedApi.registerPreset(new CustomPreset() {
 *     public String id() { return "mymod:swoop"; }
 *     public String displayName() { return "Swoop"; }
 *     public PresetMotion motionAt(float progress) {
 *         float e = 1f - (1f - progress) * (1f - progress);
 *         return new PresetMotion((1f - e) * 40f, 0f, 0.9f + 0.1f * e);
 *     }
 * });
 * }</pre>
 *
 * Прогресс уже сглажен выбранной игроком траекторией не будет: он линеен, кривая —
 * часть самой анимации. Так расширение полностью управляет характером движения.
 */
public interface CustomPreset {

    /** Идентификатор вида {@code modid:name}. По нему пресет запоминается в конфиге. */
    String id();

    /** Название для списка в настройках. */
    String displayName();

    /**
     * Где находится экран в момент {@code progress} (0 — начало появления, 1 — на месте).
     * При закрытии мод сам проигрывает эту же кривую в обратную сторону.
     */
    PresetMotion motionAt(float progress);
}
