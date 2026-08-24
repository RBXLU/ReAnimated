package com.pycodder.reanimated.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pycodder.reanimated.ReAnimatedClient;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoLetters;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.UiPreset;
import net.neoforged.fml.loading.FMLPaths;

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

    public boolean bgFadeEnabled = true;

    public boolean listsEnabled = true;
    public AnimProfile profileLists = defaultListsProfile();

    private static AnimProfile defaultListsProfile() {
        AnimProfile p = new AnimProfile();
        p.enabled = true;
        p.durationMs = 260;
        p.offsetX = 0f;
        p.offsetY = 14f;
        p.scaleX = 1f;
        p.scaleY = 1f;
        p.initialAlpha = 1f;
        p.cascadeDelayMs = 28;
        p.cascadeOrder = com.pycodder.reanimated.anim.CascadeOrder.TOP_TO_BOTTOM;
        p.pivot = com.pycodder.reanimated.anim.PivotPoint.CENTER;
        p.easing = EasingType.OUT_CUBIC;
        return p;
    }

    public boolean pauseEnabled = true;
    public int pauseSpeedTicks = 4;
    public UiPreset pausePreset = UiPreset.INHERIT;
    public float pauseDistance = 12f;
    public EasingType pauseEasing = EasingType.OUT_CUBIC;

    public boolean hoverEnabled = true;
    public float hoverScale = 0.07f;
    public float hoverSpeed = 14f;

    public boolean pressEnabled = true;
    public float pressScale = 0.06f;
    public float pressDuration = 0.18f;

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
        if (pausePreset == null) pausePreset = UiPreset.INHERIT;
        if (pauseEasing == null) pauseEasing = EasingType.OUT_CUBIC;
        if (uiPreset == UiPreset.INHERIT) uiPreset = UiPreset.DEFAULT;
    }

    public String toShareString() {
        return SHARE_GSON.toJson(this);
    }

    public static boolean applyShareString(String text) {
        if (text == null || text.isBlank()) return false;
        ReAnimatedConfig parsed;
        try {
            parsed = GSON.fromJson(text.trim(), ReAnimatedConfig.class);
        } catch (Exception e) {
            return false;
        }
        if (parsed == null) return false;
        parsed.sanitize();

        ReAnimatedConfig target = get();
        for (java.lang.reflect.Field f : ReAnimatedConfig.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            try {
                f.set(target, f.get(parsed));
            } catch (IllegalAccessException ignored) {
            }
        }
        target.save();
        return true;
    }
}
