package com.pycodder.reanimated.anim;

/**
 * Побуквенный каскад логотипа "Minecraft".
 *
 * Каждая буква — отдельная текстура-канвас размером с весь логотип (1024×256,
 * 4× супер-сэмпл ванильной текстуры 256×64): буква нарисована на своём месте,
 * остальное прозрачно. Наложение всех девяти = целый логотип. Каскад получается
 * из того, что каждая буква едет по своей задержке, а трансформация (сдвиг/масштаб)
 * общая — вокруг центра бокса логотипа, ровно как у покнопочного каскада UI.
 *
 * Идея и текстуры букв — из мода EaseGUI (Weyne1), LGPLv3. Математика переписана
 * под системы ReAnimated и переиспользует {@link AnimProfile}.
 *
 * Вся математика собрана здесь (без обращений к Minecraft), чтобы все версии/эпохи
 * рендеринга считали каскад одним и тем же кодом, а миксин лишь применял результат.
 */
public final class LogoLetters {
    private LogoLetters() {}

    /** Базовые имена файлов букв в порядке слева направо: M I N E C R A F T. */
    public static final String[] FILES = { "m", "i", "n", "e", "c", "r", "a", "f", "t" };
    public static final int COUNT = 9;

    // --- Ванильные размеры логотипа (одинаковы во всех эпохах, подтверждено по jar'ам). ---
    /** Ширина бокса логотипа на экране (px). */
    public static final int LOGO_WIDTH = 256;
    /** Высота бокса логотипа на экране (px). */
    public static final int LOGO_HEIGHT = 44;
    /** Отступ логотипа сверху (px) — LogoDrawer.LOGO_BASE_Y / LogoRenderer.DEFAULT_HEIGHT_OFFSET. */
    public static final int LOGO_BASE_Y = 30;
    /** Нормативная высота текстуры (ваниль 256×64) — для расчёта UV при блите. */
    public static final int LOGO_TEXTURE_HEIGHT = 64;

    /**
     * Номер шага каскада для буквы с горизонтальным индексом {@code i} (0..8, слева направо).
     * Порядок каскада переиспользует {@link CascadeOrder} горизонтально:
     *  TOP_TO_BOTTOM → слева направо, BOTTOM_TO_TOP → справа налево, SIMULTANEOUS → все разом.
     */
    public static int slot(AnimProfile p, int i) {
        if (p.cascadeOrder == CascadeOrder.SIMULTANEOUS) return 0;
        return p.cascadeOrder == CascadeOrder.BOTTOM_TO_TOP ? (COUNT - 1 - i) : i;
    }

    /** Сглаженный прогресс 0..1(+) конкретной буквы к моменту {@code elapsedMs}. */
    public static float easedFor(AnimProfile p, float elapsedMs, int i) {
        return p.progress(elapsedMs, slot(p, i));
    }

    /** Полная длительность всего каскада логотипа (мс) — когда доиграла последняя буква. */
    public static float totalMs(AnimProfile p) {
        return p.totalMs(COUNT);
    }

    /** Профиль по умолчанию для каскада логотипа: буквы влетают снизу с лёгким отскоком, слева направо. */
    public static AnimProfile defaultProfile() {
        AnimProfile p = new AnimProfile();
        p.enabled = true;
        p.durationMs = 350;
        p.offsetX = 0f;
        p.offsetY = 18f;
        p.scaleX = 1f;
        p.scaleY = 1f;
        p.initialAlpha = 0f;
        p.cascadeDelayMs = 55;
        p.cascadeOrder = CascadeOrder.TOP_TO_BOTTOM; // слева направо
        p.pivot = PivotPoint.CENTER;
        p.easing = EasingType.OUT_BACK;
        return p;
    }
}
