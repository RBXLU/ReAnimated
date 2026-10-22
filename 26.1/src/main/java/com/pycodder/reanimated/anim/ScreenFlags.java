package com.pycodder.reanimated.anim;

import java.util.Set;

/**
 * Свойства класса экрана, которые не меняются за время игры: чат ли это, экран ли
 * это из ванили, меню паузы, экран-«однодневка» вроде подключения к миру.
 *
 * Выяснять их прямо в кадре — обходом иерархии классов с {@code getSimpleName()} на
 * каждом шаге — дорого: на экране с тремя десятками виджетов это повторялось бы
 * десятки раз за кадр. Поэтому ответы считаются один раз на класс и живут в
 * {@link ClassValue}: он потокобезопасен и не держит класс от выгрузки вместе с
 * модом, который его загрузил.
 *
 * Ванильные экраны узнаются по самому классу ({@link #registerVanilla}), а имя класса —
 * запасной признак. Имя надёжно лишь там, где игра работает с понятными именами: в
 * разработке, на NeoForge и на 26.x. В обычной сборке Fabric 1.21.x классы игры
 * называются {@code class_433}, {@code class_435} и так далее, и по имени их не узнать.
 * Для модовых экранов имя остаётся рабочим признаком: их классы не переименовываются.
 */
public final class ScreenFlags {

    private static volatile Set<Class<?>> pauseClasses = Set.of();
    private static volatile Set<Class<?>> chatClasses = Set.of();
    private static volatile Set<Class<?>> transientClasses = Set.of();
    private static volatile Set<Class<?>> pvpClasses = Set.of();

    /**
     * Ванильные экраны по типу — для сборок, где их имена в игре не читаются. Вызывается
     * при запуске клиента, до первого экрана: ответы {@link #of} кэшируются навсегда.
     */
    public static void registerVanilla(Set<Class<?>> pause, Set<Class<?>> chat, Set<Class<?>> transientScreens) {
        pauseClasses = Set.copyOf(pause);
        chatClasses = Set.copyOf(chat);
        transientClasses = Set.copyOf(transientScreens);
    }

    /**
     * Экраны, которые в PvP-режиме открываются без анимации: база всех контейнеров
     * (инвентарь, сундуки, печи — и модовые тоже, они наследуют её) и экран смерти.
     */
    public static void registerPvp(Set<Class<?>> pvp) {
        pvpClasses = Set.copyOf(pvp);
    }

    private static final ClassValue<ScreenFlags> CACHE = new ClassValue<>() {
        @Override
        protected ScreenFlags computeValue(Class<?> type) {
            return new ScreenFlags(type);
        }
    };

    /** Чат: мод его не трогает — там своя анимация и свои моды поверх неё. */
    public final boolean chat;
    /** Экран из самой игры, а не из мода. */
    public final boolean vanilla;
    /** Меню паузы: в yarn {@code GameMenuScreen}, в Mojmap {@code PauseScreen}. */
    public final boolean pause;
    /** Экран, который живёт доли секунды и сам сменяет себя (подключение, загрузка). */
    public final boolean transient_;
    /** Экран, который в бою должен открываться мгновенно: инвентарь, контейнер, экран смерти. */
    public final boolean pvp;

    private ScreenFlags(Class<?> cls) {
        boolean chat = false, pause = false, temp = false, pvp = false;
        for (Class<?> k = cls; k != null && k != Object.class; k = k.getSuperclass()) {
            if (chatClasses.contains(k)) chat = true;
            if (pauseClasses.contains(k)) pause = true;
            if (transientClasses.contains(k)) temp = true;
            if (pvpClasses.contains(k)) pvp = true;
            String n = k.getSimpleName();
            if ("ChatScreen".equals(n)) chat = true;
            if ("GameMenuScreen".equals(n) || "PauseScreen".equals(n)) pause = true;
            if ("HandledScreen".equals(n) || "AbstractContainerScreen".equals(n) || "DeathScreen".equals(n)) pvp = true;
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
        this.pvp = pvp;
        this.vanilla = cls.getName().startsWith("net.minecraft.");
    }

    public static ScreenFlags of(Object screen) {
        return screen == null ? null : CACHE.get(screen.getClass());
    }
}
