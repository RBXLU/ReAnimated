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

/**
 * Точка входа для расширений ReAnimated.
 *
 * Всё, что здесь есть, рассчитано на другие моды: добавить свою анимацию появления,
 * подсказать моду, что конкретный экран трогать не надо (или, наоборот, надо),
 * узнать, куда прямо сейчас сдвинут интерфейс, и повесить свою кнопку в настройки.
 *
 * <pre>{@code
 * // в инициализации своего мода
 * ReAnimatedApi.registerPreset(new MySwoopPreset());
 * ReAnimatedApi.registerFilter(screen -> screen instanceof MyFragileScreen
 *         ? AnimationFilter.Result.SKIP : AnimationFilter.Result.DEFAULT);
 * }</pre>
 *
 * Класс не тянет за собой ни классов игры, ни классов загрузчика: одни и те же
 * вызовы работают на Fabric, Quilt и NeoForge и на всех поддерживаемых версиях.
 * Регистрировать можно в любой момент — списки читаются при отрисовке.
 *
 * Расширения изолированы: исключение из чужого кода логируется и гасится, а не
 * роняет игру и не ломает анимацию остальных экранов.
 */
public final class ReAnimatedApi {
    private ReAnimatedApi() {}

    /** Версия самого API. Растёт, только когда в него что-то добавляют. */
    public static final int API_VERSION = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger("reanimated-api");

    private static final Map<String, CustomPreset> PRESETS = new ConcurrentHashMap<>();
    private static final List<CustomPreset> PRESET_ORDER = new CopyOnWriteArrayList<>();
    private static final List<AnimationFilter> FILTERS = new CopyOnWriteArrayList<>();
    private static final List<AnimationListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<SettingsEntry> ENTRIES = new CopyOnWriteArrayList<>();

    /** Поставщик состояния; ставит сам мод при запуске. */
    private static volatile Supplier<AnimationState> stateSupplier = () -> AnimationState.IDLE;

    // ------------------------------------------------------------ анимации

    /**
     * Добавляет свою анимацию появления. Она появится в списке пресетов в настройках
     * мода и в настройках отдельных экранов.
     *
     * Повторная регистрация того же {@link CustomPreset#id()} заменяет предыдущую —
     * так расширение может перезагрузить свою анимацию, не перезапуская игру.
     */
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

    /**
     * Снимает анимацию с регистрации. Нужно расширению, которое умеет выключаться:
     * снятая анимация пропадает из списка пресетов, а экраны, где её выбрали,
     * возвращаются к обычному пресету — выбор при этом остаётся в конфиге и оживёт,
     * когда расширение зарегистрирует её снова.
     */
    public static void unregisterPreset(String id) {
        if (id == null) return;
        CustomPreset removed = PRESETS.remove(id);
        if (removed != null) {
            PRESET_ORDER.remove(removed);
            LOGGER.info("[ReAnimated] Preset unregistered: {}", id);
        }
    }

    /** Зарегистрированная анимация по идентификатору или null. */
    public static CustomPreset preset(String id) {
        return id == null ? null : PRESETS.get(id);
    }

    /** Все добавленные анимации, в порядке регистрации. */
    public static List<CustomPreset> presets() {
        return Collections.unmodifiableList(new ArrayList<>(PRESET_ORDER));
    }

    /**
     * Положение экрана по чужой анимации. Исключение внутри расширения гасится —
     * экран просто останется на месте.
     */
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

    // ------------------------------------------------------------ правила

    /** Добавляет правило «анимировать ли экран». Спрашиваются в порядке регистрации. */
    public static void registerFilter(AnimationFilter filter) {
        if (filter != null) FILTERS.add(filter);
    }

    /** Первый определённый ответ правил; DEFAULT — никто не высказался. */
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

    /** Есть ли вообще правила — чтобы не ходить в них на каждом кадре впустую. */
    public static boolean hasFilters() {
        return !FILTERS.isEmpty();
    }

    // ------------------------------------------------------------ события

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

    /** Есть ли слушатели — события не собираются, пока их никто не ждёт. */
    public static boolean hasListeners() {
        return !LISTENERS.isEmpty();
    }

    // ------------------------------------------------------------ состояние

    /** Что мод делает с интерфейсом прямо сейчас. */
    public static AnimationState state() {
        try {
            AnimationState s = stateSupplier.get();
            return s == null ? AnimationState.IDLE : s;
        } catch (Throwable t) {
            return AnimationState.IDLE;
        }
    }

    /** Ставится самим модом при запуске; расширениям вызывать не нужно. */
    public static void setStateSupplier(Supplier<AnimationState> supplier) {
        if (supplier != null) stateSupplier = supplier;
    }

    // ------------------------------------------------------------ настройки

    /** Добавляет кнопку расширения в настройки мода. */
    public static void registerSettingsEntry(SettingsEntry entry) {
        if (entry == null || entry.id() == null) return;
        for (SettingsEntry e : ENTRIES) {
            if (entry.id().equals(e.id())) return; // уже добавлена
        }
        ENTRIES.add(entry);
    }

    /** Кнопки расширений для экрана настроек. */
    public static List<SettingsEntry> settingsEntries() {
        return Collections.unmodifiableList(new ArrayList<>(ENTRIES));
    }

    /** Открывает экран расширения, гася его ошибки. */
    public static void openEntry(SettingsEntry entry, Object parent) {
        try {
            entry.open(parent);
        } catch (Throwable t) {
            LOGGER.error("[ReAnimated] Settings entry {} failed to open", entry.id(), t);
        }
    }
}
