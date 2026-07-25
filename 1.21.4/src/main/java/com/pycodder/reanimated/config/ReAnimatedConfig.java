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

    // --- Наведение на кнопку: плавное увеличение ---
    public boolean hoverEnabled = true;
    public float hoverScale = 0.07f;           // доля (0.07 = +7%)
    public float hoverSpeed = 14f;             // скорость

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
                    // Конфиг мог быть записан версией без профиля, а неизвестные
                    // значения enum'ов Gson молча превращает в null.
                    if (cfg.profile == null) cfg.profile = new AnimProfile();
                    cfg.profile.sanitize();
                    // Конфиг мог быть записан версией без логотип-каскада.
                    if (cfg.profileLogo == null) cfg.profileLogo = LogoLetters.defaultProfile();
                    cfg.profileLogo.sanitize();
                    if (cfg.logoStyle == null) cfg.logoStyle = LogoStyle.GROW;
                    if (cfg.profileTabs == null) cfg.profileTabs = defaultTabsProfile();
                    cfg.profileTabs.sanitize();
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
}
