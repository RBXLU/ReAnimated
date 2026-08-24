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

/** Mod configuration. */
public class ReAnimatedConfig {
    public AnimProfile profile = new AnimProfile();

    public UiPreset uiPreset = UiPreset.DEFAULT;

    public int animationSpeedTicks = 6;

    public boolean animateModdedScreens = true;

    public boolean closeAnimationEnabled = true;

    public boolean screenOpenEnabled = true;
    public float screenOpenDuration = 0.35f;
    public float screenOpenDistance = 16f;
    public EasingType screenOpenEasing = EasingType.OUT_CUBIC;

    public boolean containerEnabled = true;
    public float containerDuration = 0.45f;
    public float containerDistance = 30f;
    public EasingType containerEasing = EasingType.OUT_BACK;

    public boolean hoverEnabled = true;
    public float hoverScale = 0.07f;
    public float hoverSpeed = 14f;

    public boolean slotHighlightEnabled = true;
    public float slotHighlightSpeed = 22f;

    public boolean logoEnabled = true;
    public LogoStyle logoStyle = LogoStyle.GROW;
    public float logoDuration = 0.6f;
    public EasingType logoEasing = EasingType.OUT_BACK;
    public AnimProfile profileLogo = LogoLetters.defaultProfile();

    public boolean tabsEnabled = true;
    public AnimProfile profileTabs = defaultTabsProfile();

    private static AnimProfile defaultTabsProfile() {
        AnimProfile p = new AnimProfile();
        p.enabled = true;
        p.durationMs = 340;
        p.offsetX = 0f;
        p.offsetY = 48f;
        p.scaleX = 1f;
        p.scaleY = 1f;
        p.initialAlpha = 0f;
        p.cascadeDelayMs = 55;
        p.cascadeOrder = com.pycodder.reanimated.anim.CascadeOrder.TOP_TO_BOTTOM;
        p.pivot = com.pycodder.reanimated.anim.PivotPoint.CENTER;
        p.easing = EasingType.OUT_CUBIC;
        return p;
    }

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
                    if (cfg.profile == null) cfg.profile = new AnimProfile();
                    cfg.profile.sanitize();
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
