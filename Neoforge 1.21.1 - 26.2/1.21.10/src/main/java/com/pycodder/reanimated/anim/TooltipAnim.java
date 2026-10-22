package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/**
 * Появление всплывающей подсказки. Ваниль рисует её мгновенно; здесь подсказка
 * коротко «вырастает» из точки курсора.
 *
 * Подсказка не объект с жизненным циклом, а результат отрисовки текущего кадра —
 * запомнить момент её появления не у чего. Поэтому появление определяется по
 * разрыву: если в предыдущих кадрах подсказку не рисовали, значит она только что
 * появилась и анимация начинается заново. Переход курсора с предмета на предмет
 * без разрыва анимацию не перезапускает (подсказка «перетекает»), а вот смена
 * числа строк — перезапускает: это уже явно другая подсказка.
 */
public final class TooltipAnim {
    private TooltipAnim() {}

    /**
     * Сколько миллисекунд без подсказки считаем разрывом. Кадр при 20 fps — 50 мс,
     * так что порог заведомо выше одного пропущенного кадра и заведомо ниже паузы,
     * которую игрок воспринимает как «подсказка исчезла».
     */
    private static final long GAP_MS = 150L;

    private static long lastDrawTime = 0L;
    private static long startTime = 0L;
    private static int lastSize = -1;

    /**
     * Прогресс появления 0..1. Вызывается ровно один раз на отрисовку подсказки —
     * он же обновляет состояние.
     *
     * @param componentCount число строк/блоков подсказки — по нему различаем подсказки
     */
    public static float progress(int componentCount) {
        long now = System.currentTimeMillis();
        if (now - lastDrawTime > GAP_MS || componentCount != lastSize) {
            startTime = now;
            lastSize = componentCount;
        }
        lastDrawTime = now;
        float duration = Math.max(1f, ReAnimatedConfig.get().tooltipDurationMs);
        return Easing.clamp01((now - startTime) / duration);
    }
}
