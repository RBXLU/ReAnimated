package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

/**
 * Состояние и расчёт анимаций появления. Значения берутся из конфига мода.
 *
 * {@link #currentOpenTime} выставляется в начале отрисовки экрана
 * ({@code ScreenMixin}) и означает момент открытия текущего экрана.
 *
 * Анимация описывается двумя параметрами, которые применяет миксин:
 *  - {@link #slideY(boolean)} — вертикальный сдвиг (px),
 *  - {@link #scale(boolean)} — масштаб относительно центра экрана.
 * Пресет ({@link UiPreset}) выбирает, какой из них работает. Фон/блюр всегда
 * остаётся на месте — миксин применяет обратную трансформацию.
 */
public final class Anim {
    private Anim() {}

    /** Начальный масштаб для пресета "из фона" (вырастает из центра). */
    private static final float FROM_BACKGROUND_SCALE = 0.75f;
    /** Начальный масштаб для пресета "с переднего плана" (влетает, уменьшаясь). */
    private static final float FROM_FOREGROUND_SCALE = 1.25f;

    /** Время (millis) открытия текущего отрисовываемого экрана. 0 — нет анимации. */
    public static long currentOpenTime = 0L;

    /** Количество элементов каскада на текущем экране (выставляет ScreenMixin при init). */
    public static int cascadeCount = 1;

    /**
     * Время (millis) начала анимации закрытия экрана. 0 — экран не закрывается.
     * Пока это время установлено, {@code MinecraftClientMixin} отменяет реальный
     * {@code setScreen(null)} и держит старый экран на отрисовке — {@link #slideY}
     * и {@link #scale} доигрывают ту же кривую открытия в обратном направлении.
     */
    public static long closeStartTime = 0L;
    /** "Открытость" (0..1) экрана в момент, когда началось закрытие — точка старта реверса. */
    private static float closeStartProgress = 0f;
    /** То же для профиля, но во времени (мс): точка таймлайна, с которой отматываем назад. */
    private static float closeStartElapsedMs = 0f;
    /**
     * true между отменой настоящего {@code setScreen(null)} в {@code MinecraftClientMixin}
     * и его повторным (уже пропущенным) вызовом из {@code ScreenMixin} по завершении анимации.
     */
    public static volatile boolean bypassClose = false;

    public static boolean isClosing() {
        return closeStartTime > 0L;
    }

    /** Запускает обратную анимацию закрытия с текущей видимой точки открытия. */
    public static void beginClose(boolean container) {
        closeStartProgress = progress(container);
        // Точку старта реверса ограничиваем полной длительностью: иначе у экрана,
        // открытого 10 секунд назад, реверс большую часть времени стоял бы на месте.
        AnimProfile p = profile();
        float openedMs = currentOpenTime <= 0L ? 0f : (System.currentTimeMillis() - currentOpenTime);
        closeStartElapsedMs = Math.min(openedMs, p.totalMs(cascadeCount));
        closeStartTime = System.currentTimeMillis();
    }

    public static void finishClose() {
        closeStartTime = 0L;
        closeStartProgress = 0f;
        closeStartElapsedMs = 0f;
    }

    // --- Два независимых слоя закрытия ---
    // Пресет двигает экран целиком, профиль каскадит кнопки. Оба реверсятся, каждый
    // в своём темпе, а настоящее закрытие ждёт, пока доиграет ДОЛЬШИЙ из активных.

    /** Длительность анимации ЭКРАНА (пресет), мс. 0 — пресет неактивен. */
    private static float presetDurationMs(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        boolean active = enabled(c, container) && c.uiPreset != UiPreset.NONE;
        return active ? duration(c, container) * 1000f : 0f;
    }

    /** Длительность каскада КНОПОК (профиль), мс. 0 — профиль выключен. */
    private static float profileDurationMs() {
        AnimProfile p = profile();
        return p.enabled ? p.totalMs(cascadeCount) : 0f;
    }

    private static float presetCloseNorm(boolean container) {
        float d = presetDurationMs(container);
        if (d <= 0f) return 1f;
        return Easing.clamp01((System.currentTimeMillis() - closeStartTime) / d);
    }

    private static float profileCloseNorm() {
        float d = profileDurationMs();
        if (d <= 0f) return 1f;
        return Easing.clamp01((System.currentTimeMillis() - closeStartTime) / d);
    }

    /** true, когда доиграл дольший из активных слоёв — пора выполнить настоящий setScreen(null). */
    public static boolean closeFinished(boolean container) {
        if (!isClosing()) return true;
        float max = Math.max(presetDurationMs(container), profileDurationMs());
        if (max <= 0f) return true;
        return (System.currentTimeMillis() - closeStartTime) >= max;
    }

    // --- Профиль анимации (редактор профиля). Когда включён — перекрывает пресеты. ---

    public static AnimProfile profile() {
        return ReAnimatedConfig.get().profile;
    }

    /**
     * Позиция текущего экрана на таймлайне профиля (мс): растёт при открытии и
     * отматывается назад к нулю при закрытии — так закрытие получается точным
     * реверсом открытия, включая порядок каскада.
     */
    public static float profileElapsedMs() {
        if (isClosing()) {
            return closeStartElapsedMs * (1f - profileCloseNorm());
        }
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return System.currentTimeMillis() - currentOpenTime;
    }

    /** Сглаженный прогресс шага каскада {@code slot} для текущего экрана. */
    public static float profileEase(int slot) {
        return profile().progress(profileElapsedMs(), slot);
    }

    /** "Виртуальный" прогресс открытия во время закрытия: едет от текущей точки к 0. */
    private static float virtualOpenProgress(boolean container) {
        return closeStartProgress * (1f - presetCloseNorm(container));
    }

    public static float elapsed(long now) {
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return (now - currentOpenTime) / 1000f;
    }

    private static float duration(ReAnimatedConfig c, boolean container) {
        // Единая скорость для всего UI, задаётся в тиках (20 тиков = 1 сек).
        return Math.max(1, c.animationSpeedTicks) / 20f;
    }

    /**
     * Нужно ли анимировать данный экран. Чат исключён всегда (конфликт с модами
     * анимации чата). В режиме "только ванильные" пропускаем экраны из модов
     * (класс не из пакета {@code net.minecraft.}). Строковые проверки — чтобы
     * {@code Anim} не зависел от версионных имён классов экранов.
     */
    public static boolean shouldAnimate(Object screen) {
        if (screen == null) return false;
        Class<?> cls = screen.getClass();
        for (Class<?> k = cls; k != null && k != Object.class; k = k.getSuperclass()) {
            if ("ChatScreen".equals(k.getSimpleName())) return false;
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.animateModdedScreens && !cls.getName().startsWith("net.minecraft.")) return false;
        return true;
    }

    /** Нормализованный прогресс 0..1 (без сглаживания). */
    private static float progress(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float e = elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) return 1f;
        return Easing.clamp01(e / Math.max(0.01f, duration(c, container)));
    }

    private static boolean enabled(ReAnimatedConfig c, boolean container) {
        return container ? c.containerEnabled : c.screenOpenEnabled;
    }

    /** Текущий вертикальный сдвиг (px). Ненулевой только для пресета DEFAULT. */
    public static float slideY(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container) || c.uiPreset != UiPreset.DEFAULT) return 0f;
        float p = isClosing() ? virtualOpenProgress(container) : progress(container);
        EasingType easing = container ? c.containerEasing : c.screenOpenEasing;
        float distance = container ? c.containerDistance : c.screenOpenDistance;
        return (1f - easing.apply(p)) * distance;
    }

    /**
     * Есть ли прямо сейчас видимая трансформация (идёт открытие/закрытие). Если нет —
     * миксины вообще не трогают матрицу (без push/pop), это основная оптимизация: когда
     * экран просто открыт и неподвижен, мод не добавляет ни одной операции над матрицей.
     */
    public static boolean transformActive(boolean container) {
        // Только слой ЭКРАНА (пресет). Покнопочный слой (профиль) двигает матрицу
        // сам в ClickableWidgetMixin и в трансформации экрана не участвует.
        return slideY(container) != 0f || scale(container) != 1f;
    }

    /** Текущий масштаб относительно центра. 1.0 для DEFAULT/NONE. */
    public static float scale(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container) || !c.uiPreset.isScale()) return 1f;
        float p = isClosing() ? virtualOpenProgress(container) : progress(container);
        if (c.uiPreset == UiPreset.FROM_BACKGROUND) {
            return Easing.lerp(FROM_BACKGROUND_SCALE, 1f, Easing.outBack(p));
        }
        // FROM_FOREGROUND
        return Easing.lerp(FROM_FOREGROUND_SCALE, 1f, Easing.outCubic(p));
    }

    // --- Совместимость со старым кодом (логотип и т.п.) ---
    public static float screenSlide() {
        return slideY(false);
    }

    public static float containerSlide() {
        return slideY(true);
    }
}
