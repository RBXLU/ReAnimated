package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ScreenRules {
    private ScreenRules() {}

    private static final int MAX_SEEN = 200;

    private static final Set<String> SEEN = new LinkedHashSet<>();

    public static void remember(Object screen) {
        if (screen == null) return;
        String key = key(screen);
        synchronized (SEEN) {
            SEEN.remove(key);
            SEEN.add(key);
            while (SEEN.size() > MAX_SEEN) {
                SEEN.remove(SEEN.iterator().next());
            }
        }
    }

    public static String key(Object screen) {
        return screen.getClass().getName();
    }

    public static String displayName(String key) {
        int dot = key.lastIndexOf('.');
        String name = dot < 0 ? key : key.substring(dot + 1);
        return name.replace('$', '.');
    }

    public static boolean isModded(String key) {
        return !key.startsWith("net.minecraft.");
    }

    public static ScreenOverride get(Object screen) {
        if (screen == null) return null;
        Map<String, ScreenOverride> map = ReAnimatedConfig.get().screenOverrides;
        if (map == null || map.isEmpty()) return null;
        return map.get(key(screen));
    }

    public static ScreenOverride getOrCreate(String key) {
        Map<String, ScreenOverride> map = ReAnimatedConfig.get().screenOverrides;
        ScreenOverride o = map.get(key);
        if (o == null) {
            o = new ScreenOverride();
            map.put(key, o);
        }
        return o;
    }

    public static void prune() {
        ReAnimatedConfig.get().screenOverrides.entrySet().removeIf(e ->
            e.getValue() == null || e.getValue().isDefault());
    }

    public static List<String> listForEditor() {
        List<String> out = new ArrayList<>(ReAnimatedConfig.get().screenOverrides.keySet());
        List<String> seen;
        synchronized (SEEN) {
            seen = new ArrayList<>(SEEN);
        }
        for (int i = seen.size() - 1; i >= 0; i--) {
            String key = seen.get(i);
            if (!out.contains(key)) out.add(key);
        }
        return out;
    }
}
