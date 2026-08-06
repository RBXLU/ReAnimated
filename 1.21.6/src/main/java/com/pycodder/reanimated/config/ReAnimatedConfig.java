package com.pycodder.reanimated.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoLetters;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.UiPreset;
import net.fabricmc.loader.api.FabricLoader;

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

    // --- Скорость всех анимаций в тиках (20 тиков = 1 сек). Меньше = быстрее. ---
    public int animationSpeedTicks = 6;

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

    // --- Меню паузы (Esc). Свой набор настроек: этот экран открывают чаще всех,
    //     и та же длительность, что у обычных меню, там ощущается затянутой.
    //     pausePreset = INHERIT означает «как общий пресет». ---
    public boolean pauseEnabled = true;
    public int pauseSpeedTicks = 4;
    public UiPreset pausePreset = UiPreset.INHERIT;
    public float pauseDistance = 12f;
    public EasingType pauseEasing = EasingType.OUT_CUBIC;

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
        return FabricLoader.getInstance().getConfigDir().resolve("reanimated.json");
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
