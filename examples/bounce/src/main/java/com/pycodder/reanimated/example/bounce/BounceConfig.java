package com.pycodder.reanimated.example.bounce;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class BounceConfig {
    private BounceConfig() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("reanimated-bounce");
    private static final String KEY = "enabled";

    private static boolean enabled = true;
    private static boolean loaded = false;

    public static boolean isEnabled() {
        if (!loaded) load();
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        loaded = true;
        save();
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("reanimated_bounce.properties");
    }

    private static void load() {
        loaded = true;
        Path p = path();
        if (!Files.exists(p)) return;
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(p)) {
            props.load(r);
            enabled = Boolean.parseBoolean(props.getProperty(KEY, "true"));
        } catch (IOException e) {
            LOGGER.warn("[bounce] Не удалось прочитать настройки, остаёмся на значении по умолчанию", e);
        }
    }

    private static void save() {
        Properties props = new Properties();
        props.setProperty(KEY, Boolean.toString(enabled));
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            try (Writer w = Files.newBufferedWriter(p)) {
                props.store(w, "ReAnimated: Bounce");
            }
        } catch (IOException e) {
            LOGGER.warn("[bounce] Не удалось сохранить настройки", e);
        }
    }
}
