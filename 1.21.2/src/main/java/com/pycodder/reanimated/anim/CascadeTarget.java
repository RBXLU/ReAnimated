package com.pycodder.reanimated.anim;

/**
 * Реализуется миксином в {@code ClickableWidget}: место виджета в каскаде.
 *
 * Ранг — номер виджета в порядке сверху вниз, присваивается один раз в
 * {@code ScreenMixin} при инициализации экрана. Слот (номер шага каскада)
 * считается из ранга уже во время отрисовки — {@link AnimProfile#slotFor(int, int)},
 * поэтому смена порядка каскада в настройках действует сразу.
 */
public interface CascadeTarget {
    void reanimated$setCascade(int rank, int count);

    /** -1 — виджет не участвует в каскаде (не является элементом анимируемого экрана). */
    int reanimated$cascadeRank();

    int reanimated$cascadeCount();

    /** Текущая прозрачность виджета (поле {@code alpha}) — для затемнения текста кнопки. */
    float reanimated$currentAlpha();
}
