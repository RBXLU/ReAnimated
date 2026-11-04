package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.api.AnimationFilter;
import com.pycodder.reanimated.api.AnimationState;
import com.pycodder.reanimated.api.CustomPreset;
import com.pycodder.reanimated.api.PresetMotion;
import com.pycodder.reanimated.api.ReAnimatedApi;
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
    /** Начальный масштаб пресета «Pop»: экран буквально выскакивает из точки. */
    private static final float POP_SCALE = 0.6f;

    static {
        // Расширения спрашивают состояние через ReAnimatedApi, а считает его Anim.
        // Связываем их здесь, а не в точке входа мода: у Fabric и NeoForge она разная,
        // а этот класс есть в каждой сборке.
        ReAnimatedApi.setStateSupplier(Anim::apiState);
    }

    /** Время (millis) открытия текущего отрисовываемого экрана. 0 — нет анимации. */
    public static long currentOpenTime = 0L;

    // --- Время кадра ---
    // Все слои анимации считаются от «сейчас», и за кадр это «сейчас» спрашивалось
    // десятки раз: по разу на каждый виджет в каскаде плюс на каждый слой экрана.
    // Отсчёт берётся один раз на кадр — заодно все слои гарантированно считаются от
    // одного момента, а не расползаются на доли миллисекунды внутри кадра.

    private static long frameTime = 0L;
    private static boolean frameActive = false;
    /**
     * Номер кадра. Служит ключом кэшей, которые живут ровно один кадр: сравнивать
     * время нельзя — два кадра подряд легко попадают в одну миллисекунду, и второй
     * получил бы чужой ответ.
     */
    private static long frameId = 0L;

    /** Начало кадра экрана: фиксирует момент, от которого считаются все анимации. */
    public static void beginFrame() {
        frameTime = System.currentTimeMillis();
        frameActive = true;
        frameId++;
    }

    /** Конец кадра экрана. */
    public static void endFrame() {
        frameActive = false;
    }

    /**
     * «Сейчас» для расчётов. Внутри кадра — зафиксированный момент, снаружи (например
     * при отрисовке уведомлений, которые рисуются мимо экрана) — настоящие часы.
     */
    public static long now() {
        return frameActive ? frameTime : System.currentTimeMillis();
    }

    /** Экран, который сейчас отрисовывается. Нужен событиям API. */
    public static Object currentScreen = null;
    /** Контейнерный ли текущий экран — для снимка состояния, отдаваемого расширениям. */
    public static boolean currentIsContainer = false;

    /** Количество элементов каскада на текущем экране (выставляет ScreenMixin при init). */
    public static int cascadeCount = 1;

    /**
     * Является ли текущий отрисовываемый экран меню паузы. Выставляется вместе с
     * {@link #currentOpenTime} и переключает расчёты на отдельный набор настроек
     * ({@code pause*}): меню паузы открывают чаще любого другого экрана, и общая
     * длительность там ощущается затянутой.
     */
    public static boolean currentIsPause = false;

    /**
     * Настройки текущего отрисовываемого экрана, если для него что-то задано отдельно
     * ({@code ScreenRulesScreen}). Выставляется вместе с {@link #currentOpenTime}.
     * null — экран анимируется общими настройками.
     */
    public static ScreenOverride currentOverride = null;

    /**
     * С какой стороны пришёл текущий экран при переходе: +1 справа, -1 слева, 0 — не
     * переходом (экран открыт сам по себе). Выставляет {@code ScreenMixin} из
     * {@link #enterDir}, когда экран отрисовывается впервые.
     */
    public static int currentEnterDir = 0;

    // --- Переход между экранами ---
    // Уход старого экрана и приход нового — одна анимация, разорванная настоящей
    // сменой экрана посередине. Пока играет уход, setScreen(next) отложен, а цель
    // лежит здесь; ScreenMixin выполняет его по завершении и передаёт новому экрану
    // направление входа.

    /** Экран, на который переключимся, когда доиграет уход. null — уходим в мир. */
    public static Object pendingScreen = null;
    /** Куда уезжает уходящий экран: +1 вправо, -1 влево, 0 — перехода нет. */
    public static int exitDir = 0;
    /** Направление входа для следующего экрана — его заберёт первый же кадр нового экрана. */
    public static int enterDir = 0;

    // Остаток незавершённой анимации входа на момент начала перехода. Если щёлкать по
    // меню быстрее, чем они доигрывают, экран уходит не с «нуля», а с той точки, где
    // его застали, и остаток плавно рассасывается за время перехода — иначе на старте
    // ухода был бы виден скачок на всю дистанцию появления.
    private static float exitResidualX = 0f;
    private static float exitResidualY = 0f;
    private static float exitResidualScale = 1f;

    /**
     * Время (millis) начала анимации закрытия экрана. 0 — экран не закрывается.
     * Пока это время установлено, {@code MinecraftClientMixin} отменяет реальный
     * {@code setScreen(null)} и держит старый экран на отрисовке — {@link #slideY}
     * и {@link #scale} доигрывают ту же кривую открытия в обратном направлении.
     */
    public static long closeStartTime = 0L;
    /** "Открытость" (0..1) экрана в момент, когда началось закрытие — точка старта реверса. */
    private static float closeStartProgress = 0f;
    /**
     * Насколько экран был «набран» целиком к началу закрытия (0..1) — максимум из
     * открытости пресета и открытости каскада кнопок. На него умножается окно
     * закрытия: экран, закрытый на середине появления, и уходить должен вдвое
     * быстрее, а не отыгрывать полную длительность, стоя почти на месте.
     */
    private static float closeOpenness = 1f;
    /** То же для профиля, но во времени (мс): точка таймлайна, с которой отматываем назад. */
    private static float closeStartElapsedMs = 0f;
    /**
     * true между отменой настоящего {@code setScreen(null)} в {@code MinecraftClientMixin}
     * и его повторным (уже пропущенным) вызовом из {@code ScreenMixin} по завершении анимации.
     */
    public static volatile boolean bypassClose = false;

    /**
     * Экран, чьё закрытие в мир уже доиграло: настоящий {@code setScreen(null)} для него
     * выполнит начало СЛЕДУЮЩЕГО кадра, до того как экран что-либо нарисует.
     *
     * Так в пределах одного кадра у всех одинаковое состояние. Если закрыть экран прямо
     * на выходе из его отрисовки, всё, что игра рисует в этом кадре после экрана, видит
     * «экрана нет», хотя сам экран в кадре уже был. Blur+ именно так проверяет,
     * дорисовывать ли своё угасание фона, — и положил бы второй слой размытия и
     * затемнения поверх только что нарисованной панели (тёмная вспышка в конце закрытия).
     */
    public static Object closeReadyScreen = null;

    /**
     * Контекст отрисовки GUI текущего кадра экрана. Нужен там, где метод игры контекста
     * не получает, а нарисовать всё-таки надо: размытие фона в 1.21.1–1.21.5 зовётся
     * без него (см. ScreenBackgroundMixin). Тип у разных версий свой — отсюда Object.
     */
    public static Object frameGraphics = null;

    public static boolean isClosing() {
        return closeStartTime > 0L;
    }

    /**
     * Экран уходит не в мир, а на другой экран. Во время перехода все остальные слои
     * ухода отключены: пресет, каскад кнопок и затухание фона играть нечему — экран
     * целиком уезжает в сторону и через мгновение его сменит новый. Иначе навигация
     * по меню стоила бы полной длительности закрытия плюс перехода, а на стыке был
     * бы виден скачок недоигранной обратной анимации.
     */
    public static boolean isTransitioning() {
        return closeStartTime > 0L && exitDir != 0;
    }

    /** Запускает обратную анимацию закрытия с текущей видимой точки открытия. */
    public static void beginClose(boolean container) {
        if (exitDir == 0 && ReAnimatedApi.hasListeners()) ReAnimatedApi.fireClose(currentScreen, null);
        closeStartProgress = progress(container);
        // Точку старта реверса ограничиваем полной длительностью: иначе у экрана,
        // открытого 10 секунд назад, реверс большую часть времени стоял бы на месте.
        AnimProfile p = profile();
        float openedMs = currentOpenTime <= 0L ? 0f : (now() - currentOpenTime);
        float total = p.totalMs(cascadeCount);
        closeStartElapsedMs = Math.min(openedMs, total);
        float profileOpenness = profileLayerActive(container) && total > 0f
            ? Easing.clamp01(openedMs / total) : 0f;
        closeOpenness = Math.max(closeStartProgress, profileOpenness);
        closeStartTime = now();
    }

    public static void finishClose() {
        boolean wasClosing = closeStartTime > 0L;
        closeStartTime = 0L;
        closeStartProgress = 0f;
        closeStartElapsedMs = 0f;
        closeOpenness = 1f;
        closeReadyScreen = null;
        pendingScreen = null;
        exitDir = 0;
        exitResidualX = 0f;
        exitResidualY = 0f;
        exitResidualScale = 1f;
        if (wasClosing && ReAnimatedApi.hasListeners()) ReAnimatedApi.fireFinish(currentScreen);
    }

    /**
     * Начинает переход на другой экран: текущий доигрывает закрытие и заодно уезжает
     * в сторону {@code dir}, а {@code next} потом входит с противоположной.
     */
    public static void beginTransition(Object next, int dir) {
        // Замер до смены состояния: дальше isTransitioning() обнулит эти слои.
        exitResidualX = slideX(false);
        exitResidualY = slideY(false);
        exitResidualScale = scale(false);
        beginClose(false);
        pendingScreen = next;
        exitDir = dir;
        enterDir = -dir;
        if (ReAnimatedApi.hasListeners()) ReAnimatedApi.fireClose(currentScreen, next);
    }

    /**
     * Сбрасывает направление входа, не дожидаясь нового экрана. Нужно там, где переход
     * отменили: иначе направление подхватил бы следующий открытый экран, к переходу
     * уже никак не относящийся, и въехал бы сбоку без причины.
     */
    public static void clearEnterDir() {
        enterDir = 0;
    }

    /** Забирает направление входа для только что открытого экрана (одноразово). */
    public static int takeEnterDir() {
        int d = enterDir;
        enterDir = 0;
        return d;
    }

    /**
     * Экран, который снова стал текущим: на него вернулись, например, кнопкой «Готово»
     * из его же подменю. Он уже рисовался, и без перезапуска встал бы на место без
     * анимации, а направление входа так и лежало бы до следующего экрана.
     * {@code ScreenMixin} забирает его на первом кадре и запускает появление заново.
     */
    public static Object restartScreen = null;

    // --- Закрытие: ОДНО окно на все слои ---
    // Пресет двигает экран целиком, профиль каскадит кнопки. У закрытия одно общее окно
    // на все слои — самая долгая из ОДИНОЧНЫХ длительностей (пресет и шаг профиля, без
    // хвоста каскада). Все слои отматываются назад внутри него и заканчивают
    // одновременно, а экран закрывается ровно в тот момент, когда последняя анимация
    // встала на место. Хвост каскада (задержка последней кнопки) в окно не входит: иначе
    // экран с десятком кнопок висел бы почти полсекунды после того, как всё приехало.

    /**
     * Играет ли для текущего экрана слой ЭКРАНА (пресет). Отдельным методом, потому что
     * это же условие решает, откладывать ли настоящее закрытие ({@code MinecraftClientMixin}) —
     * иначе набор «что включено» пришлось бы держать в двух местах.
     */
    public static boolean presetLayerActive(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return enabled(c, container) && preset(c, container) != UiPreset.NONE;
    }

    /**
     * Играет ли покнопочный слой (профиль/Студия). У контейнеров — нет: панель, слоты
     * и модель игрока ведёт слой ЭКРАНА, каскадить там нечего, а закрытие инвентаря
     * из-за этого слоя ждало впустую.
     */
    public static boolean profileLayerActive(boolean container) {
        return !container && profile().enabled;
    }

    /** Есть ли что проигрывать при закрытии. false — закрываем мгновенно, без задержки. */
    public static boolean closeWouldAnimate(boolean container) {
        return presetLayerActive(container) || profileLayerActive(container);
    }

    /**
     * Полная длительность закрытия (мс) — общая для всех слоёв. 0 — анимировать нечего.
     */
    private static float closeWindowMs(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float base = 0f;
        if (presetLayerActive(container)) base = duration(c, container) * 1000f;
        if (profileLayerActive(container)) base = Math.max(base, Math.max(1, profile().durationMs));
        if (base <= 0f) return 0f;
        return base * Easing.clamp01(closeOpenness);
    }

    /** Насколько закрытие уже проиграно по ЧАСАМ (0..1), без сглаживания. */
    private static float closeNorm(boolean container) {
        float d = closeWindowMs(container);
        if (d <= 0f) return 1f;
        return Easing.clamp01((now() - closeStartTime) / d);
    }

    /**
     * Сглаженный ход закрытия (0..1). Им гасится «открытость» экрана, и от него
     * зависит, как уход выглядит.
     *
     * Кривая применяется к самому ходу закрытия, а не к линейно гаснущей «открытости»:
     * все кривые здесь замедляются к концу (Out Cubic у меню, Out Back у контейнеров), и
     * открытие, проигранное задом наперёд, начинало бы уход почти с нулевой скорости —
     * панель стояла бы на месте, а всё движение сваливалось бы в последние кадры. Так
     * панель трогается с места сразу и приезжает к концу окна, а затемнение гаснет
     * вместе с ней.
     */
    private static float closeEase(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return easingFor(c, container).forExit().apply(closeNorm(container));
    }

    /**
     * Дольше этого срока экран не задерживается ни при каких настройках. Пока
     * закрытие не завершено, экран остаётся текущим и глотает setScreen(null) —
     * если бы окно закрытия почему-то не истекло, интерфейс завис бы закрытым.
     * Предел заведомо больше самой долгой анимации (40 тиков = 2 с).
     */
    private static final long CLOSE_WATCHDOG_MS = 2500L;

    /** true, когда окно закрытия истекло — пора выполнить настоящую смену экрана. */
    public static boolean closeFinished(boolean container) {
        if (!isClosing()) return true;
        long spent = now() - closeStartTime;
        if (spent >= CLOSE_WATCHDOG_MS) return true;
        // Переход: ждём только его — остальные слои во время перехода не играют.
        if (exitDir != 0) return spent >= transitionDurationMs();
        float window = closeWindowMs(container);
        if (window <= 0f) return true;
        return spent >= window;
    }

    // --- Профиль анимации (редактор профиля). Когда включён — перекрывает пресеты. ---

    public static AnimProfile profile() {
        // Свой профиль кнопок экрана (или его мода) сильнее общего.
        ScreenOverride o = currentOverride;
        if (o != null && o.profile != null) return o.profile;
        return ReAnimatedConfig.get().profile;
    }

    /**
     * Позиция текущего экрана на таймлайне профиля (мс): растёт при открытии и
     * отматывается назад к нулю при закрытии — так закрытие получается точным
     * реверсом открытия, включая порядок каскада.
     */
    public static float profileElapsedMs() {
        if (isTransitioning()) return Float.MAX_VALUE; // кнопки стоят на месте и уезжают с экраном
        if (isClosing()) {
            // Окно берём по текущему экрану: у контейнера оно своё, и кнопки внутри
            // него должны встать на место ровно тогда же, когда уедет панель. Ход —
            // сглаженный (см. closeEase): иначе каскад стоял бы на месте всю первую
            // половину ухода, как это было с самой панелью.
            return closeStartElapsedMs * (1f - closeEase(currentIsContainer));
        }
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return now() - currentOpenTime;
    }

    /** Сглаженный прогресс шага каскада {@code slot} для текущего экрана. */
    public static float profileEase(int slot) {
        return profile().progress(profileElapsedMs(), slot);
    }

    /** "Виртуальный" прогресс открытия во время закрытия: едет от текущей точки к 0. */
    private static float virtualOpenProgress(boolean container) {
        // clamp01: кривые с отскоком (Out Back) дают больше единицы, и без ограничения
        // открытость ушла бы в минус — панель проскочила бы мимо точки, с которой
        // начинала появляться.
        return Easing.clamp01(closeStartProgress * (1f - closeEase(container)));
    }

    public static float elapsed(long now) {
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        return (now - currentOpenTime) / 1000f;
    }

    // Часы логотипа: для какого открытия экрана они заведены и когда логотип стал виден.
    private static long logoOpenTime = 0L;
    private static long logoShownAt = 0L;

    /**
     * Сколько секунд логотип виден на текущем экране; Float.MAX_VALUE, если экрана нет.
     *
     * Отсчёт идёт от первого кадра, где логотип не прозрачен, а не от открытия экрана:
     * при запуске игры главный экран сначала рисуется под заставкой загрузки, а логотип
     * проявляется ещё через секунду, и анимация успевала доиграть, пока её никто не
     * видел. Пока логотип прозрачен, возвращается 0 — анимация ждёт в начальной точке.
     */
    public static float logoElapsed(long now, float alpha) {
        if (currentOpenTime <= 0L) return Float.MAX_VALUE;
        if (logoOpenTime != currentOpenTime) {
            logoOpenTime = currentOpenTime;
            logoShownAt = 0L;
        }
        if (logoShownAt == 0L) {
            if (alpha <= 0f) return 0f;
            logoShownAt = now;
        }
        return (now - logoShownAt) / 1000f;
    }

    private static float duration(ReAnimatedConfig c, boolean container) {
        // Единая скорость для всего UI, задаётся в тиках (20 тиков = 1 сек).
        // Исключения — меню паузы и экраны со своими настройками.
        ScreenOverride o = currentOverride;
        if (o != null && o.speedTicks > 0) return o.speedTicks / 20f;
        int ticks = isPause(container) ? c.pauseSpeedTicks : c.animationSpeedTicks;
        return Math.max(1, ticks) / 20f;
    }

    /** Считать ли текущий экран меню паузы. Контейнером меню паузы не бывает. */
    private static boolean isPause(boolean container) {
        return currentIsPause && !container;
    }

    /** Пресет для текущего экрана: у меню паузы может быть свой, иначе общий. */
    private static UiPreset preset(ReAnimatedConfig c, boolean container) {
        // Настройка конкретного экрана сильнее и общего пресета, и пресета меню паузы:
        // её задали именно для этого экрана.
        ScreenOverride o = currentOverride;
        if (o != null && o.preset != null && o.preset != UiPreset.INHERIT) {
            return o.preset;
        }
        if (isPause(container) && c.pausePreset != null && c.pausePreset != UiPreset.INHERIT) {
            return c.pausePreset;
        }
        UiPreset general = c.uiPreset == null || c.uiPreset == UiPreset.INHERIT ? UiPreset.DEFAULT : c.uiPreset;
        return resolveCustom(general, c);
    }

    /**
     * Пресет из расширения существует, только пока стоит добавивший его мод. Если мод
     * убрали, экран не должен открываться рывком — возвращаем поведение по умолчанию,
     * а выбор в конфиге сохраняем: мод вернут — вернётся и анимация.
     */
    private static UiPreset resolveCustom(UiPreset preset, ReAnimatedConfig c) {
        if (preset != UiPreset.CUSTOM) return preset;
        return customPreset(c) == null ? UiPreset.DEFAULT : UiPreset.CUSTOM;
    }

    /** Выбранная анимация расширения: у экрана может быть своя, иначе общая. */
    private static CustomPreset customPreset(ReAnimatedConfig c) {
        ScreenOverride o = currentOverride;
        String id = null;
        if (o != null && o.preset == UiPreset.CUSTOM && o.customPresetId != null && !o.customPresetId.isEmpty()) {
            id = o.customPresetId;
        } else if (c.customPresetId != null && !c.customPresetId.isEmpty()) {
            id = c.customPresetId;
        }
        return ReAnimatedApi.preset(id);
    }

    // Положение по чужой анимации спрашивается тремя разными местами (сдвиг по X, по Y
    // и масштаб) — считаем его один раз на кадр, чтобы не дёргать чужой код втрое чаще
    // необходимого.
    private static CustomPreset motionOwner = null;
    private static long motionStamp = -1L;
    private static float motionProgress = -1f;
    private static PresetMotion motionCache = PresetMotion.IDENTITY;

    private static PresetMotion customMotion(ReAnimatedConfig c, boolean container) {
        CustomPreset cp = customPreset(c);
        if (cp == null) return PresetMotion.IDENTITY;
        float p = presetProgress(container);
        long stamp = now();
        if (cp == motionOwner && stamp == motionStamp && p == motionProgress) {
            return motionCache;
        }
        PresetMotion m = ReAnimatedApi.motionOf(cp, p);
        motionOwner = cp;
        motionStamp = stamp;
        motionProgress = p;
        motionCache = m;
        return m;
    }

    /**
     * Меню паузы ли это. Экран определяем по имени класса, а не по типу: в yarn это
     * {@code GameMenuScreen}, в Mojang-маппингах (26.x) — {@code PauseScreen}, и так
     * один и тот же {@code Anim} работает в обеих средах.
     */
    public static boolean isPauseScreen(Object screen) {
        ScreenFlags f = ScreenFlags.of(screen);
        return f != null && f.pause;
    }

    /**
     * Нужно ли анимировать данный экран. Чат исключён всегда (конфликт с модами
     * анимации чата). В режиме "только ванильные" пропускаем экраны из модов
     * (класс не из пакета {@code net.minecraft.}). Строковые проверки — чтобы
     * {@code Anim} не зависел от версионных имён классов экранов.
     */
    // Ответ «анимируем ли этот экран» за кадр спрашивается по нескольку раз (обёртка
    // экрана, фон, затемнение, каждый фрейм-список), и каждый раз это поход в карту
    // настроек экранов, опрос фильтров расширений и разбор флагов класса. Внутри кадра
    // ответ измениться не может — держим последний.
    private static Object animCacheScreen = null;
    private static long animCacheFrame = -1L;
    private static boolean animCacheValue = false;

    public static boolean shouldAnimate(Object screen) {
        if (screen == null) return false;
        if (frameActive && animCacheFrame == frameId && animCacheScreen == screen) {
            return animCacheValue;
        }
        boolean result = computeShouldAnimate(screen);
        if (frameActive) {
            animCacheScreen = screen;
            animCacheFrame = frameId;
            animCacheValue = result;
        }
        return result;
    }

    private static boolean computeShouldAnimate(Object screen) {
        ScreenFlags f = ScreenFlags.of(screen);
        // PvP-режим: инвентарь, контейнеры и экран смерти открываются и закрываются
        // мгновенно, что бы ни было задано для них отдельно, — ради этого его и включают.
        if (f.pvp && ReAnimatedConfig.get().pvpMode) return false;
        // Экран мог быть включён или выключен персонально — это решение игрока, оно
        // сильнее всего остального, включая правила расширений.
        ScreenOverride o = ScreenRules.get(screen);
        if (o != null && o.animation == Toggle.OFF) return false;
        if (o != null && o.animation == Toggle.ON) return !f.chat;
        // Правило из другого мода: автор экрана знает про него то, чего не знает ReAnimated.
        if (ReAnimatedApi.hasFilters()) {
            AnimationFilter.Result r = ReAnimatedApi.askFilters(screen);
            if (r == AnimationFilter.Result.SKIP) return false;
            if (r == AnimationFilter.Result.ANIMATE) return true;
        }
        if (f.chat) return false;
        return ReAnimatedConfig.get().animateModdedScreens || f.vanilla;
    }

    /** Нормализованный прогресс 0..1 (без сглаживания). */
    private static float progress(boolean container) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float e = elapsed(now());
        if (e == Float.MAX_VALUE) return 1f;
        return Easing.clamp01(e / Math.max(0.01f, duration(c, container)));
    }

    private static boolean enabled(ReAnimatedConfig c, boolean container) {
        ScreenOverride o = currentOverride;
        if (o != null && o.animation == Toggle.OFF) return false;
        if (o != null && o.animation == Toggle.ON) return true;
        if (container) return c.containerEnabled;
        return isPause(container) ? c.pauseEnabled : c.screenOpenEnabled;
    }

    /** Прогресс появления экрана: при закрытии отматывается назад к нулю. */
    private static float presetProgress(boolean container) {
        return isClosing() ? virtualOpenProgress(container) : progress(container);
    }

    /** Траектория для текущего экрана: у контейнеров и меню паузы свои настройки. */
    private static EasingType easingFor(ReAnimatedConfig c, boolean container) {
        ScreenOverride o = currentOverride;
        if (o != null && o.easing != null) return o.easing;
        if (container) return c.containerEasing;
        return isPause(container) ? c.pauseEasing : c.screenOpenEasing;
    }

    /** Дистанция выезда для текущего экрана. */
    private static float distanceFor(ReAnimatedConfig c, boolean container) {
        ScreenOverride o = currentOverride;
        if (o != null && o.distance >= 0f) return o.distance;
        if (container) return c.containerDistance;
        return isPause(container) ? c.pauseDistance : c.screenOpenDistance;
    }

    // --- Кэш слоёв на кадр ---
    // Сдвиг и масштаб за кадр спрашиваются десятками: их зовёт каждая обёртка матрицы
    // (экран, фон, панель контейнера, книга рецептов, каждый фрейм-список), а
    // transformActive внутри себя считает сразу все три. Каждый такой вызов заново
    // разворачивал пресет, настройки конкретного экрана и кривую — и всё ради ответа,
    // который внутри кадра не может измениться: время кадра зафиксировано.
    //
    // Поэтому три слоя считаются ОДИН раз на кадр на каждый вид экрана (обычный и
    // контейнер — они считаются по разным настройкам). Вне кадра (уведомления рисуются
    // мимо экрана, закрытие начинается из обработчика ввода) кэш не используется вовсе.
    private static final long[] LAYER_FRAME = {-1L, -1L};
    private static final float[] LAYER_X = new float[2];
    private static final float[] LAYER_Y = new float[2];
    private static final float[] LAYER_SCALE = new float[2];

    private static int layers(boolean container) {
        int i = container ? 1 : 0;
        if (frameActive && LAYER_FRAME[i] == frameId) return i;
        LAYER_X[i] = computeSlideX(container);
        LAYER_Y[i] = computeSlideY(container);
        LAYER_SCALE[i] = computeScale(container);
        if (frameActive) LAYER_FRAME[i] = frameId;
        return i;
    }

    /**
     * Текущий вертикальный сдвиг (px). Ненулевой у пресетов, которые двигают экран
     * по вертикали, и у анимаций из расширений.
     */
    public static float slideY(boolean container) {
        return LAYER_Y[layers(container)];
    }

    /** Текущий горизонтальный сдвиг (px): пресет плюс переход между экранами. */
    public static float slideX(boolean container) {
        return LAYER_X[layers(container)];
    }

    /**
     * Текущий масштаб относительно центра: масштаб пресета, умноженный на масштаб
     * перехода между экранами. Слои независимы, поэтому «вырастание из фона» и
     * переход-зум складываются, а не отменяют друг друга.
     */
    public static float scale(boolean container) {
        return LAYER_SCALE[layers(container)];
    }

    private static float computeSlideY(boolean container) {
        if (!container && isTransitioning()) {
            // Уход переходом: своего вертикального движения нет, но недоигранный вход
            // надо доводить до нуля, а не обрывать.
            return exitResidualY * (1f - transitionEase());
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container)) return 0f;
        UiPreset preset = preset(c, container);
        if (preset == UiPreset.CUSTOM) return customMotion(c, container).offsetY;
        if (!preset.isVertical()) return 0f;
        float p = presetProgress(container);
        // SLIDE_DOWN — то же движение, только экран приходит сверху.
        float dir = preset == UiPreset.SLIDE_DOWN ? -1f : 1f;
        return (1f - easingFor(c, container).apply(p)) * distanceFor(c, container) * dir;
    }

    /**
     * Есть ли прямо сейчас видимая трансформация (идёт открытие/закрытие). Если нет —
     * миксины вообще не трогают матрицу (без push/pop), это основная оптимизация: когда
     * экран просто открыт и неподвижен, мод не добавляет ни одной операции над матрицей.
     */
    public static boolean transformActive(boolean container) {
        // Только слой ЭКРАНА (пресет) и переход между экранами. Покнопочный слой
        // (профиль) двигает матрицу сам в ClickableWidgetMixin и здесь не участвует.
        int i = layers(container);
        return LAYER_Y[i] != 0f || LAYER_X[i] != 0f || LAYER_SCALE[i] != 1f;
    }

    /**
     * Насколько «набрано» затемнение фона (0..1). 1 — ровно ванильное, и тогда миксин
     * не вмешивается вовсе.
     *
     * В гейт отложенного закрытия ({@code MinecraftClientMixin}) этот слой намеренно НЕ
     * входит: затемнение видно только когда за экраном мир, а гейт про это не знает —
     * иначе на главном меню закрытие тормозило бы ради невидимой анимации. Пока играет
     * пресет или каскад кнопок, затухание идёт с ними в один такт (та же длительность).
     */
    public static float backgroundFade(boolean container) {
        // На переходе между экранами затемнение остаётся набранным: мигать ему незачем,
        // за обоими экранами один и тот же мир. Это касается обеих половин перехода —
        // и уходящего экрана, и пришедшего: для пришедшего затемнение уже нарисовал
        // предыдущий, и повторное нарастание выглядело бы морганием.
        if (!container && (isTransitioning() || (currentEnterDir != 0 && !isClosing()))) return 1f;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        ScreenOverride o = currentOverride;
        if (!(o == null ? c.bgFadeEnabled : o.background.resolve(c.bgFadeEnabled))) return 1f;
        float p;
        if (isClosing()) {
            p = Easing.clamp01(closeStartProgress * (1f - closeEase(container)));
        } else {
            p = progress(container);
        }
        return Easing.outCubic(p);
    }

    private static float computeScale(boolean container) {
        return presetScale(container) * transitionScale(container);
    }

    /** Масштаб от пресета появления. 1.0 для DEFAULT/NONE. */
    private static float presetScale(boolean container) {
        if (!container && isTransitioning()) {
            // Как и со сдвигом: недоигранный масштаб входа доводим до единицы.
            return 1f + (exitResidualScale - 1f) * (1f - transitionEase());
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        UiPreset preset = preset(c, container);
        if (!enabled(c, container)) return 1f;
        if (preset == UiPreset.CUSTOM) return customMotion(c, container).scale;
        if (!preset.isScale()) return 1f;
        float p = presetProgress(container);
        if (preset == UiPreset.FROM_BACKGROUND) {
            return Easing.lerp(FROM_BACKGROUND_SCALE, 1f, Easing.outBack(p));
        }
        if (preset == UiPreset.POP) {
            return Easing.lerp(POP_SCALE, 1f, Easing.outBack(p));
        }
        // FROM_FOREGROUND
        return Easing.lerp(FROM_FOREGROUND_SCALE, 1f, Easing.outCubic(p));
    }

    // --- Переход между экранами ---

    /** Насколько меняется масштаб экрана за переход (доля). */
    private static final float TRANSITION_ZOOM = 0.06f;

    /** Длительность перехода (мс). */
    private static float transitionDurationMs() {
        return Math.max(1, ReAnimatedConfig.get().transitionSpeedTicks) / 20f * 1000f;
    }

    /** Выбран ли стиль перехода. Включены ли переходы для пары экранов — {@link #transitionAllowed}. */
    private static boolean transitionStyleActive() {
        TransitionStyle st = ReAnimatedConfig.get().transitionStyle;
        return st != null && st != TransitionStyle.NONE;
    }

    /**
     * Можно ли уходить с этого экрана (или приходить на него) переходом: у экрана и у
     * его мода может быть своё решение поверх общего.
     */
    public static boolean transitionAllowed(Object screen) {
        if (!transitionStyleActive()) return false;
        boolean general = ReAnimatedConfig.get().transitionEnabled;
        ScreenOverride o = ScreenRules.get(screen);
        return o == null ? general : o.transitions.resolve(general);
    }

    /** Играет ли закрытие текущего экрана: его настройка поверх общей. */
    public static boolean closeAnimationEnabled() {
        boolean general = ReAnimatedConfig.get().closeAnimationEnabled;
        ScreenOverride o = currentOverride;
        return o == null ? general : o.close.resolve(general);
    }

    /** PvP-режим: инвентарь и контейнеры мгновенно, интерфейс в бою не догоняет. */
    public static boolean pvp() {
        return ReAnimatedConfig.get().pvpMode;
    }

    /**
     * Прогресс перехода 0..1 и его фаза.
     * @return положительное значение — уход (0 -> 1), отрицательное — приход (0 -> 1
     *         с обратным знаком направления), {@link Float#NaN} — перехода нет
     */
    private static float transitionPhase(boolean container) {
        // Начат ли переход, решалось по экранам при смене (transitionAllowed): здесь он
        // доигрывается, даже если у этого экрана переходы с тех пор выключили.
        if (container || !transitionStyleActive()) return Float.NaN;
        float d = transitionDurationMs();
        long now = now();
        if (isClosing()) {
            // Уход: экран уезжает от точки покоя. Если экран закрывается не переходом
            // (просто в мир), горизонтального слоя нет вовсе.
            if (exitDir == 0) return Float.NaN;
            return Easing.clamp01((now - closeStartTime) / d);
        }
        if (currentEnterDir == 0 || currentOpenTime <= 0L) return Float.NaN;
        return -Easing.clamp01((now - currentOpenTime) / d);
    }

    private static float computeSlideX(boolean container) {
        return presetSlideX(container) + transitionSlideX(container);
    }

    /** Горизонтальный сдвиг от самого пресета (боковые выезды и анимации расширений). */
    private static float presetSlideX(boolean container) {
        if (!container && isTransitioning()) {
            return exitResidualX * (1f - transitionEase());
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!enabled(c, container)) return 0f;
        UiPreset preset = preset(c, container);
        if (preset == UiPreset.CUSTOM) return customMotion(c, container).offsetX;
        if (!preset.isHorizontal()) return 0f;
        float p = presetProgress(container);
        float dir = preset == UiPreset.SLIDE_LEFT ? -1f : 1f;
        return (1f - easingFor(c, container).apply(p)) * distanceFor(c, container) * dir;
    }

    /** Горизонтальный сдвиг от перехода между экранами. */
    private static float transitionSlideX(boolean container) {
        float phase = transitionPhase(container);
        if (Float.isNaN(phase)) return 0f;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.transitionStyle.isSlide()) return 0f;
        if (phase >= 0f) {
            return c.transitionEasing.apply(phase) * c.transitionDistance * exitDir;
        }
        return (1f - c.transitionEasing.apply(-phase)) * c.transitionDistance * currentEnterDir;
    }

    /** Сглаженный прогресс ухода переходом; 0 вне перехода. */
    private static float transitionEase() {
        if (!isTransitioning()) return 0f;
        float d = transitionDurationMs();
        ReAnimatedConfig c = ReAnimatedConfig.get();
        return c.transitionEasing.apply(Easing.clamp01((now() - closeStartTime) / d));
    }

    /**
     * Масштабный множитель перехода. Шаг вглубь отдаляет уходящий экран и выводит
     * новый «с переднего плана», возврат — наоборот.
     */
    public static float transitionScale(boolean container) {
        float phase = transitionPhase(container);
        if (Float.isNaN(phase)) return 1f;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.transitionStyle.isZoom()) return 1f;
        if (phase >= 0f) {
            float target = exitDir < 0 ? 1f - TRANSITION_ZOOM : 1f + TRANSITION_ZOOM;
            return Easing.lerp(1f, target, c.transitionEasing.apply(phase));
        }
        float from = currentEnterDir > 0 ? 1f + TRANSITION_ZOOM : 1f - TRANSITION_ZOOM;
        return Easing.lerp(from, 1f, c.transitionEasing.apply(-phase));
    }

    /**
     * Можно ли переключаться на этот экран через переход. Экраны, которые появляются
     * по ходу подключения к миру, живут доли секунды и меняют друг друга сами —
     * задерживать их ради анимации нельзя: игрок увидит подвисший «Подключение...»
     * поверх уже загруженного мира. Проверка по имени класса, чтобы не тянуть в
     * {@code Anim} версионные имена экранов.
     */
    public static boolean isVolatileScreen(Object screen) {
        ScreenFlags f = ScreenFlags.of(screen);
        return f == null || f.transient_;
    }

    /**
     * Снимок для расширений ({@code ReAnimatedApi.state()}). Собирается только по
     * запросу: пока никто не спрашивает, ни одного лишнего расчёта не делается.
     */
    public static AnimationState apiState() {
        boolean container = currentIsContainer;
        if (!transformActive(container)) return AnimationState.IDLE;
        return new AnimationState(true, isClosing(), isTransitioning(),
            presetProgress(container), slideX(container), slideY(container), scale(container));
    }

    /** Сообщает расширениям, что экран начал появляться. */
    public static void onScreenOpened(Object screen) {
        if (ReAnimatedApi.hasListeners()) ReAnimatedApi.fireOpen(screen);
    }

    // --- Совместимость со старым кодом (логотип и т.п.) ---
    public static float screenSlide() {
        return slideY(false);
    }

    public static float containerSlide() {
        return slideY(true);
    }
}
