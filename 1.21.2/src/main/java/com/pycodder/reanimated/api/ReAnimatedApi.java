package com.pycodder.reanimated.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class ReAnimatedApi {
    private ReAnimatedApi() {}

    public static final int API_VERSION = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger("reanimated-api");

    private static final Map<String, CustomPreset> PRESETS = new ConcurrentHashMap<>();
    private static final List<CustomPreset> PRESET_ORDER = new CopyOnWriteArrayList<>();
    private static final List<AnimationFilter> FILTERS = new CopyOnWriteArrayList<>();
    private static final List<AnimationListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<SettingsEntry> ENTRIES = new CopyOnWriteArrayList<>();

    private static volatile Supplier<AnimationState> stateSupplier = () -> AnimationState.IDLE;

    public static void registerPreset(CustomPreset preset) {
        if (preset == null || preset.id() == null || preset.id().isEmpty()) {
            LOGGER.warn("[ReAnimated] Extension tried to register a preset without an id — ignored");
            return;
        }
        CustomPreset old = PRESETS.put(preset.id(), preset);
        if (old != null) {
            PRESET_ORDER.remove(old);
        }
        PRESET_ORDER.add(preset);
        LOGGER.info("[ReAnimated] Preset registered: {}", preset.id());
    }

    public static void unregisterPreset(String id) {
        if (id == null) return;
        CustomPreset removed = PRESETS.remove(id);
        if (removed != null) {
            PRESET_ORDER.remove(removed);
            LOGGER.info("[ReAnimated] Preset unregistered: {}", id);
        }
    }

    public static CustomPreset preset(String id) {
        return id == null ? null : PRESETS.get(id);
    }

    public static List<CustomPreset> presets() {
        return Collections.unmodifiableList(new ArrayList<>(PRESET_ORDER));
    }

    public static PresetMotion motionOf(CustomPreset preset, float progress) {
        if (preset == null) return PresetMotion.IDENTITY;
        try {
            PresetMotion m = preset.motionAt(progress);
            return m == null ? PresetMotion.IDENTITY : m;
        } catch (Throwable t) {
            LOGGER.error("[ReAnimated] Preset {} failed, falling back to no motion", preset.id(), t);
            return PresetMotion.IDENTITY;
        }
    }

    public static void registerFilter(AnimationFilter filter) {
        if (filter != null) FILTERS.add(filter);
    }

    public static AnimationFilter.Result askFilters(Object screen) {
        if (FILTERS.isEmpty()) return AnimationFilter.Result.DEFAULT;
        for (AnimationFilter f : FILTERS) {
            try {
                AnimationFilter.Result r = f.test(screen);
                if (r != null && r != AnimationFilter.Result.DEFAULT) return r;
            } catch (Throwable t) {
                LOGGER.error("[ReAnimated] Animation filter failed", t);
            }
        }
        return AnimationFilter.Result.DEFAULT;
    }

    public static boolean hasFilters() {
        return !FILTERS.isEmpty();
    }

    public static void addListener(AnimationListener listener) {
        if (listener != null) LISTENERS.add(listener);
    }

    public static void removeListener(AnimationListener listener) {
        LISTENERS.remove(listener);
    }

    public static void fireOpen(Object screen) {
        for (AnimationListener l : LISTENERS) {
            try {
                l.onOpen(screen);
            } catch (Throwable t) {
                LOGGER.error("[ReAnimated] Animation listener failed on open", t);
            }
        }
    }

    public static void fireClose(Object screen, Object next) {
        for (AnimationListener l : LISTENERS) {
            try {
                l.onClose(screen, next);
            } catch (Throwable t) {
                LOGGER.error("[ReAnimated] Animation listener failed on close", t);
            }
        }
    }

    public static void fireFinish(Object screen) {
        for (AnimationListener l : LISTENERS) {
            try {
                l.onFinish(screen);
            } catch (Throwable t) {
                LOGGER.error("[ReAnimated] Animation listener failed on finish", t);
            }
        }
    }

    public static boolean hasListeners() {
        return !LISTENERS.isEmpty();
    }

    public static AnimationState state() {
        try {
            AnimationState s = stateSupplier.get();
            return s == null ? AnimationState.IDLE : s;
        } catch (Throwable t) {
            return AnimationState.IDLE;
        }
    }

    public static void setStateSupplier(Supplier<AnimationState> supplier) {
        if (supplier != null) stateSupplier = supplier;
    }

    public static void registerSettingsEntry(SettingsEntry entry) {
        if (entry == null || entry.id() == null) return;
        for (SettingsEntry e : ENTRIES) {
            if (entry.id().equals(e.id())) return;
        }
        ENTRIES.add(entry);
    }

    public static List<SettingsEntry> settingsEntries() {
        return Collections.unmodifiableList(new ArrayList<>(ENTRIES));
    }

    public static void openEntry(SettingsEntry entry, Object parent) {
        try {
            entry.open(parent);
        } catch (Throwable t) {
            LOGGER.error("[ReAnimated] Settings entry {} failed to open", entry.id(), t);
        }
    }
}
