package com.pycodder.reanimated.anim;

import com.pycodder.reanimated.config.ReAnimatedConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Смена вкладки (создание мира, статистика и другие экраны на ванильных вкладках).
 *
 * Ваниль убирает виджеты старой вкладки и добавляет виджеты новой за один вызов —
 * старых после этого уже не нарисовать. Поэтому анимируется только приход: виджеты
 * новой вкладки въезжают со стороны, куда переключились, и проявляются.
 *
 * Объекты игры здесь хранятся как {@code Object}: этот пакет не зависит от классов
 * Minecraft, а вкладку, менеджер и виджет нужно только сравнивать.
 */
public final class TabAnim {
    private TabAnim() {}

    /** Порядок вкладок у каждого менеджера — из него берётся направление. */
    private static final Map<Object, List<?>> ORDER = Collections.synchronizedMap(new WeakHashMap<>());

    private static Set<Object> widgets = Collections.newSetFromMap(new WeakHashMap<>());
    private static long start = 0L;
    private static int dir = 1;

    public static void registerOrder(Object manager, List<?> tabs) {
        ORDER.put(manager, tabs);
    }

    /**
     * Вкладка сменилась: запоминаем её виджеты и направление. Первая вкладка экрана
     * ({@code oldTab == null}) не анимируется — экран и так появляется своей анимацией.
     */
    public static void begin(Object manager, Object oldTab, Object newTab, Iterable<?> newWidgets) {
        if (oldTab == null || !ReAnimatedConfig.get().tabSwitchEnabled) return;
        List<?> order = ORDER.get(manager);
        int from = order == null ? -1 : order.indexOf(oldTab);
        int to = order == null ? -1 : order.indexOf(newTab);
        dir = (from >= 0 && to >= 0 && to < from) ? -1 : 1;
        Set<Object> set = Collections.newSetFromMap(new WeakHashMap<>());
        for (Object w : newWidgets) set.add(w);
        widgets = set;
        start = System.currentTimeMillis();
    }

    /**
     * Прогресс прихода этого виджета 0..1 (уже с кривой), или {@link Float#NaN}, если
     * виджет не из только что открытой вкладки или анимация доиграла.
     */
    public static float progress(Object widget) {
        if (start == 0L || !widgets.contains(widget)) return Float.NaN;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float t = (Anim.now() - start) / (float) Math.max(1, c.tabSwitchDurationMs);
        if (t >= 1f || !c.tabSwitchEnabled) {
            start = 0L;
            widgets = Collections.newSetFromMap(new WeakHashMap<>());
            return Float.NaN;
        }
        return c.tabSwitchEasing.apply(Easing.clamp01(t));
    }

    /** Горизонтальный сдвиг при данном прогрессе, px. */
    public static float offsetX(float progress) {
        return dir * ReAnimatedConfig.get().tabSwitchDistance * (1f - progress);
    }
}
