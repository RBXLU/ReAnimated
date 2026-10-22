package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/**
 * Анимации HUD: рамка выбранного слота хотбара и название предмета над ним.
 *
 * Рамка хранит свою позицию в слотах, а не в пикселях: тогда смена размера окна
 * сдвигает хотбар целиком, а рамка не «едет» за ним через весь экран.
 */
public final class HudAnim {
    private HudAnim() {}

    /** Сколько длится подъём названия предмета. */
    private static final float NAME_MS = 180f;
    /** На сколько пикселей название приподнимается в начале. */
    private static final float NAME_RISE = 4f;

    private static float slot = Float.NaN;
    private static long slotTime = 0L;

    private static int lastFade = 0;
    private static long nameStart = 0L;

    /**
     * Смещение рамки выбранного слота от её ванильного места, в пикселях GUI.
     * Ноль — рамка уже на месте (или анимация выключена).
     */
    public static float selectionOffset(int selected) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        long now = Anim.now();
        if (!c.hotbarEnabled || c.pvpMode || Float.isNaN(slot)) {
            slot = selected;
            slotTime = now;
            return 0f;
        }
        float dt = Math.min(0.2f, (now - slotTime) / 1000f);
        slotTime = now;
        if (slot != selected) {
            slot = Easing.approach(slot, selected, dt, c.hotbarSpeed);
            if (Math.abs(slot - selected) < 0.01f) slot = selected;
        }
        return (slot - selected) * 20f;
    }

    /**
     * Прогресс появления названия предмета 0..1. Ваниль перезапускает таймер
     * названия, когда в руке оказывается другой предмет, — по росту этого таймера
     * и понимаем, что название появилось заново.
     */
    public static float nameProgress(int fade) {
        if (fade > lastFade) nameStart = Anim.now();
        lastFade = fade;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.hotbarItemNameEnabled || c.pvpMode || nameStart == 0L) return 1f;
        float t = (Anim.now() - nameStart) / NAME_MS;
        return t >= 1f ? 1f : Easing.outCubic(Easing.clamp01(t));
    }

    /** Сдвиг названия вниз в начале появления, px. */
    public static float nameRise(float progress) {
        return (1f - progress) * NAME_RISE;
    }
}
