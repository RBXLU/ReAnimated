package com.pycodder.reanimated.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoLetters;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.ScreenOverride;
import com.pycodder.reanimated.anim.TransitionStyle;
import com.pycodder.reanimated.anim.UiPreset;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Конфигурация мода. Хранится в config/reanimated.json. */
public class ReAnimatedConfig {

    // --- Профиль анимации из редактора. Пока profile.enabled = false, всё ниже
    //     работает как раньше; когда включён — профиль задаёт анимацию открытия
    //     целиком (смещение, масштаб, прозрачность, каскад) вместо пресета. ---
    public AnimProfile profile = new AnimProfile();

    // --- Пресет анимации появления UI (единый для меню и контейнеров) ---
    public UiPreset uiPreset = UiPreset.DEFAULT;

    // --- Анимация из расширения, если выбран пресет CUSTOM. Идентификатор вида
    //     modid:name; пустая строка — расширение не выбрано. Пресет и идентификатор
    //     хранятся раздельно, чтобы выбор пережил временное отсутствие мода. ---
    public String customPresetId = "";

    // --- Скорость всех анимаций в тиках (20 тиков = 1 сек). Меньше = быстрее. ---
    public int animationSpeedTicks = 6;

    // --- PvP-режим: инвентарь, контейнеры и экран смерти открываются и закрываются
    //     мгновенно, подсветка слота и рамка хотбара не догоняют, подсказки и книга
    //     рецептов появляются сразу. Остальные меню анимируются как обычно. ---
    public boolean pvpMode = false;

    // --- Анимировать экраны из модов? false = только ванильные (net.minecraft.*) ---
    public boolean animateModdedScreens = true;

    // --- Анимация закрытия (обратная сьемка анимации открытия) ---
    public boolean closeAnimationEnabled = true;

    // --- Появление экранов меню (заголовки + кнопки выезжают снизу вместе) ---
    public boolean screenOpenEnabled = true;
    public float screenOpenDuration = 0.35f;   // сек
    public float screenOpenDistance = 16f;     // px
    public EasingType screenOpenEasing = EasingType.OUT_CUBIC;

    // --- Контейнеры (печь/сундук/инвентарь): панель выезжает, блюр стоит ---
    public boolean containerEnabled = true;
    public float containerDuration = 0.45f;
    public float containerDistance = 30f;
    public EasingType containerEasing = EasingType.OUT_BACK;

    // --- Затемнение фона за экраном: ваниль включает его мгновенно, здесь оно
    //     нарастает синхронно с выездом панели и гаснет на закрытии. Видно только
    //     в игре — на главном меню за экраном панорама, а не затемнённый мир. ---
    public boolean bgFadeEnabled = true;

    // --- Списки (моды в Mod Menu, сервера, миры, ресурспаки). Обрезка (scissor)
    //     следует за матрицей (DrawContextScissorMixin), поэтому список едет вместе
    //     с экраном, а строки появляются по очереди сверху вниз. ---
    public boolean listsEnabled = true;
    public AnimProfile profileLists = defaultListsProfile();

    /** Профиль по умолчанию для строк списка: короткий подъём снизу, быстрый каскад. */
    private static AnimProfile defaultListsProfile() {
        AnimProfile p = new AnimProfile();
        p.enabled = true;
        p.durationMs = 260;
        p.offsetX = 0f;
        p.offsetY = 14f;
        p.scaleX = 1f;
        p.scaleY = 1f;
        p.initialAlpha = 1f;        // строки списка гасить нечем — только сдвиг
        p.cascadeDelayMs = 28;
        p.cascadeOrder = com.pycodder.reanimated.anim.CascadeOrder.TOP_TO_BOTTOM;
        p.pivot = com.pycodder.reanimated.anim.PivotPoint.CENTER;
        p.easing = EasingType.OUT_CUBIC;
        return p;
    }

    // --- Меню паузы (Esc). Свой набор настроек: этот экран открывают чаще всех,
    //     и та же длительность, что у обычных меню, там ощущается затянутой.
    //     pausePreset = INHERIT означает «как общий пресет». ---
    public boolean pauseEnabled = true;
    public int pauseSpeedTicks = 4;
    public UiPreset pausePreset = UiPreset.INHERIT;
    public float pauseDistance = 12f;
    public EasingType pauseEasing = EasingType.OUT_CUBIC;

    // --- Переход между экранами. Ваниль меняет экран мгновенно: главное меню
    //     исчезает, настройки появляются. Здесь уходящий экран уезжает в сторону
    //     (или отдаляется), а новый приходит с противоположной, причём направление
    //     зависит от того, идём мы вглубь или возвращаемся назад. ---
    public boolean transitionEnabled = true;
    public TransitionStyle transitionStyle = TransitionStyle.SLIDE;
    public int transitionSpeedTicks = 3;       // 20 тиков = 1 сек
    public float transitionDistance = 24f;     // px
    public EasingType transitionEasing = EasingType.OUT_CUBIC;

    // --- Всплывающие подсказки: ваниль рисует их мгновенно, здесь подсказка
    //     коротко вырастает у курсора. ---
    public boolean tooltipEnabled = true;
    public int tooltipDurationMs = 130;
    public float tooltipScale = 0.92f;         // масштаб в начале появления
    public float tooltipOffsetY = 5f;          // стартовый сдвиг вниз, px
    public EasingType tooltipEasing = EasingType.OUT_CUBIC;

    // --- Окно книги рецептов. Ваниль показывает и убирает его мгновенно; здесь оно
    //     вырастает из-под панели контейнера — и когда его открывают кнопкой, и
    //     когда экран открылся с уже раскрытой книгой. ---
    public boolean recipeBookEnabled = true;
    public int recipeBookDurationMs = 170;
    public float recipeBookScale = 0.85f;      // масштаб в начале появления
    public float recipeBookOffsetX = 12f;      // стартовый сдвиг влево, px
    public EasingType recipeBookEasing = EasingType.OUT_CUBIC;

    // --- Уведомления (достижения, рецепты, системные). Ваниль двигает их по жёсткой
    //     кривой за фиксированные 600 мс; здесь та же анимация идёт по выбранной
    //     траектории и за выбранное время (дольше ванильного — нельзя). ---
    public boolean toastEnabled = true;
    public int toastDurationMs = 380;
    public EasingType toastEasing = EasingType.OUT_BACK;

    // --- Показывать ли в настройках подробные слайдеры каждого слоя. Выключено:
    //     экран настроек открывается коротким — общий вид, список «что анимировать»
    //     и инструменты, — а тонкая настройка разворачивается по желанию. К 1.7
    //     слайдеров набралось столько, что нужное тонуло в списке. ---
    public boolean advancedSettings = false;

    // --- Настройки отдельных экранов: ключ — полное имя класса экрана. Пустая карта
    //     означает «все экраны по общим настройкам». ---
    public java.util.Map<String, ScreenOverride> screenOverrides = new java.util.LinkedHashMap<>();

    // --- Наведение на кнопку: плавное увеличение ---
    public boolean hoverEnabled = true;
    public float hoverScale = 0.07f;           // доля (0.07 = +7%)
    public float hoverSpeed = 14f;             // скорость

    // --- Нажатие на кнопку: кнопка вдавливается и отпружинивает обратно ---
    public boolean pressEnabled = true;
    public float pressScale = 0.06f;           // доля (0.06 = -6% в нижней точке)
    public float pressDuration = 0.18f;        // сек

    // --- Подсветка слота в инвентаре, плавно следует за курсором ---
    public boolean slotHighlightEnabled = true;
    public float slotHighlightSpeed = 22f;

    // --- Хотбар: рамка выбранного слота едет к нему, а не перескакивает; название
    //     предмета над хотбаром при смене коротко приподнимается и проявляется. ---
    public boolean hotbarEnabled = true;
    public float hotbarSpeed = 18f;
    public boolean hotbarItemNameEnabled = true;

    // --- Смена вкладки (создание мира, статистика и другие экраны на ванильных
    //     вкладках): содержимое новой вкладки въезжает со стороны, куда переключились. ---
    public boolean tabSwitchEnabled = true;
    public int tabSwitchDurationMs = 220;
    public float tabSwitchDistance = 20f;
    public EasingType tabSwitchEasing = EasingType.OUT_CUBIC;

    // --- Виджеты: ручка ползунка догоняет значение, чекбокс отскакивает при
    //     переключении, рамка поля ввода плавно загорается при фокусе. ---
    public boolean widgetsEnabled = true;
    public float widgetSpeed = 16f;

    // --- Логотип "Minecraft" на главном экране ---
    public boolean logoEnabled = true;
    // Стиль: GROW — родная анимация (логотип целиком вырастает); LETTERS — побуквенный каскад.
    public LogoStyle logoStyle = LogoStyle.GROW;
    // Параметры родного стиля GROW.
    public float logoDuration = 0.6f;
    public EasingType logoEasing = EasingType.OUT_BACK;
    // Профиль побуквенного каскада (стиль LETTERS): длительность, сдвиг, масштаб, альфа,
    // задержка и направление каскада, easing. Редактируется отдельно от профиля UI.
    public AnimProfile profileLogo = LogoLetters.defaultProfile();

    // --- Каскад вкладок (достижения + креативный инвентарь) ---
    // Вкладки появляются по очереди слева направо (сдвиг + прозрачность по индексу).
    public boolean tabsEnabled = true;
    public AnimProfile profileTabs = defaultTabsProfile();

    /** Профиль по умолчанию для каскада вкладок: лёгкий подъём + проявление, слева направо. */
    private static AnimProfile defaultTabsProfile() {
        AnimProfile p = new AnimProfile();
        p.enabled = true;
        p.durationMs = 340;
        p.offsetX = 0f;
        p.offsetY = 48f;            // выезжают снизу «из глубины» — крупный старт-сдвиг вверх
        p.scaleX = 1f;
        p.scaleY = 1f;
        p.initialAlpha = 0f;
        p.cascadeDelayMs = 55;
        p.cascadeOrder = com.pycodder.reanimated.anim.CascadeOrder.TOP_TO_BOTTOM; // слева направо
        p.pivot = com.pycodder.reanimated.anim.PivotPoint.CENTER;
        p.easing = EasingType.OUT_CUBIC;
        return p;
    }

    // ------------------------------------------------------------------

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    /** Для обмена настройками: то же самое, но одной строкой — удобно вставлять в чат. */
    private static final Gson SHARE_GSON = new Gson();
    private static ReAnimatedConfig INSTANCE;

    public static ReAnimatedConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("reanimated.json");
    }

    private static ReAnimatedConfig load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                ReAnimatedConfig cfg = GSON.fromJson(r, ReAnimatedConfig.class);
                if (cfg != null) {
                    cfg.sanitize();
                    return cfg;
                }
            } catch (Exception e) {
                ReAnimatedClient.LOGGER.warn("[ReAnimated] Failed to read config, using defaults", e);
            }
        }
        ReAnimatedConfig cfg = new ReAnimatedConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            try (Writer w = Files.newBufferedWriter(p)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            ReAnimatedClient.LOGGER.warn("[ReAnimated] Failed to save config", e);
        }
    }

    /**
     * Подставляет значения по умолчанию вместо отсутствующих. Нужно и при чтении файла
     * (конфиг мог быть записан старой версией мода), и при импорте чужих настроек:
     * незнакомое значение enum'а Gson молча превращает в null, а отсутствующий объект
     * так и остаётся null — без этого первый же кадр падал бы с NPE.
     */
    private void sanitize() {
        if (profile == null) profile = new AnimProfile();
        profile.sanitize();
        if (profileLogo == null) profileLogo = LogoLetters.defaultProfile();
        profileLogo.sanitize();
        if (profileTabs == null) profileTabs = defaultTabsProfile();
        profileTabs.sanitize();
        if (profileLists == null) profileLists = defaultListsProfile();
        profileLists.sanitize();
        if (logoStyle == null) logoStyle = LogoStyle.GROW;
        if (uiPreset == null) uiPreset = UiPreset.DEFAULT;
        if (screenOpenEasing == null) screenOpenEasing = EasingType.OUT_CUBIC;
        if (containerEasing == null) containerEasing = EasingType.OUT_BACK;
        if (logoEasing == null) logoEasing = EasingType.OUT_BACK;
        // Конфиг мог быть записан версией без настроек меню паузы.
        if (pausePreset == null) pausePreset = UiPreset.INHERIT;
        if (pauseEasing == null) pauseEasing = EasingType.OUT_CUBIC;
        // Общий пресет ссылаться сам на себя не может — так настройки открывались бы мгновенно.
        if (uiPreset == UiPreset.INHERIT) uiPreset = UiPreset.DEFAULT;
        // Конфиг мог быть записан версией без переходов, подсказок и уведомлений.
        if (transitionStyle == null) transitionStyle = TransitionStyle.SLIDE;
        if (transitionEasing == null) transitionEasing = EasingType.OUT_CUBIC;
        if (transitionSpeedTicks < 1) transitionSpeedTicks = 1;
        if (tooltipEasing == null) tooltipEasing = EasingType.OUT_CUBIC;
        if (tooltipDurationMs < 1) tooltipDurationMs = 1;
        if (toastEasing == null) toastEasing = EasingType.OUT_BACK;
        if (toastDurationMs < 1) toastDurationMs = 1;
        if (recipeBookEasing == null) recipeBookEasing = EasingType.OUT_CUBIC;
        if (recipeBookDurationMs < 1) recipeBookDurationMs = 1;
        // Конфиг мог быть записан версией без хотбара, смены вкладок и виджетов.
        if (tabSwitchEasing == null) tabSwitchEasing = EasingType.OUT_CUBIC;
        if (tabSwitchDurationMs < 1) tabSwitchDurationMs = 1;
        if (hotbarSpeed <= 0f) hotbarSpeed = 18f;
        if (widgetSpeed <= 0f) widgetSpeed = 16f;
        if (customPresetId == null) customPresetId = "";
        if (screenOverrides == null) {
            screenOverrides = new java.util.LinkedHashMap<>();
        } else {
            // Значение могло прийти из чужого конфига — пустые записи выбрасываем,
            // остальные приводим к допустимым значениям.
            screenOverrides.values().removeIf(java.util.Objects::isNull);
            screenOverrides.values().forEach(ScreenOverride::sanitize);
        }
    }

    // --- Обмен настройками ---

    /** Все настройки одной строкой — для кнопки «Копировать настройки». */
    public String toShareString() {
        return SHARE_GSON.toJson(this);
    }

    /**
     * Применяет настройки из строки, скопированной у другого игрока (или прямо из
     * {@code reanimated.json} — формат тот же). Значения переносятся в уже живущий
     * экземпляр конфига, а не подменяют его: открытые экраны держат ссылку на него.
     *
     * @return true, если строка разобрана и применена
     */
    public static boolean applyShareString(String text) {
        if (text == null || text.isBlank()) return false;
        ReAnimatedConfig parsed;
        try {
            parsed = GSON.fromJson(text.trim(), ReAnimatedConfig.class);
        } catch (Exception e) {
            return false; // не JSON или JSON не той формы
        }
        if (parsed == null) return false;
        parsed.sanitize();

        ReAnimatedConfig target = get();
        for (java.lang.reflect.Field f : ReAnimatedConfig.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            try {
                f.set(target, f.get(parsed));
            } catch (IllegalAccessException ignored) {
                // поле недоступно — оставляем текущее значение
            }
        }
        target.save();
        return true;
    }
}
