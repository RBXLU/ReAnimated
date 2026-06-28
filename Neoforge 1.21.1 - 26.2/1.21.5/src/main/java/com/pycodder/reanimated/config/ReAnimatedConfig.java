package com.pycodder.reanimated.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.EasingType;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Конфигурация мода. Хранится в config/reanimated.json. */
public class ReAnimatedConfig {

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
    public float logoDuration = 0.6f;
    public EasingType logoEasing = EasingType.OUT_BACK;

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
        return FMLPaths.CONFIGDIR.get().resolve("reanimated.json");
    }

    private static ReAnimatedConfig load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                ReAnimatedConfig cfg = GSON.fromJson(r, ReAnimatedConfig.class);
                if (cfg != null) {
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
