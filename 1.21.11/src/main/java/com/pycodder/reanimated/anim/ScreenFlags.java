package com.pycodder.reanimated.anim;

public final class ScreenFlags {

    private static final ClassValue<ScreenFlags> CACHE = new ClassValue<>() {
        @Override
        protected ScreenFlags computeValue(Class<?> type) {
            return new ScreenFlags(type);
        }
    };

    public final boolean chat;
    public final boolean vanilla;
    public final boolean pause;
    public final boolean transient_;

    private ScreenFlags(Class<?> cls) {
        boolean chat = false, pause = false, temp = false;
        for (Class<?> k = cls; k != null && k != Object.class; k = k.getSuperclass()) {
            String n = k.getSimpleName();
            if ("ChatScreen".equals(n)) chat = true;
            if ("GameMenuScreen".equals(n) || "PauseScreen".equals(n)) pause = true;
            if (n.contains("Connect")
                || n.contains("Loading")
                || n.contains("Progress")
                || n.contains("DownloadingTerrain")
                || n.contains("Reconfigur")
                || n.contains("SaveLevel")
                || n.contains("Message")) {
                temp = true;
            }
        }
        this.chat = chat;
        this.pause = pause;
        this.transient_ = temp;
        this.vanilla = cls.getName().startsWith("net.minecraft.");
    }

    public static ScreenFlags of(Object screen) {
        return screen == null ? null : CACHE.get(screen.getClass());
    }
}
