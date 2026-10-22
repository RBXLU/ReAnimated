package com.pycodder.reanimated.anim;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * История переходов между экранами — по ней определяется направление перехода:
 * вглубь («Настройки» из главного меню) или назад («Готово» обратно в главное меню).
 *
 * Ванильный {@code Screen} не хранит родителя в общем виде: у одних экранов поле
 * {@code parent} есть, у других нет, у модовых — как придётся. Поэтому направление
 * берём из фактического порядка открытия: если новый экран уже лежит в стеке — это
 * возврат, иначе — шаг вглубь.
 *
 * Ссылки слабые: держать экраны живыми ради истории незачем.
 */
public final class ScreenNav {
    private ScreenNav() {}

    private static final int MAX_DEPTH = 16;

    /** Экраны, из которых уходили вглубь. Текущего экрана в стеке нет. */
    private static final Deque<WeakReference<Object>> STACK = new ArrayDeque<>();

    /**
     * Регистрирует переход {@code current -> next} и возвращает его направление.
     *
     * @return +1 — вглубь (новый экран), -1 — назад (возврат к уже открывавшемуся)
     */
    public static int accept(Object current, Object next) {
        if (next != null && contains(next)) {
            // Возврат: снимаем всё, что было открыто поверх целевого экрана, и его самого —
            // он становится текущим, а в стеке остаются только его предки.
            while (!STACK.isEmpty()) {
                Object top = deref(STACK.pop());
                if (top == next) break;
            }
            return -1;
        }
        if (current != null) {
            STACK.push(new WeakReference<>(current));
            while (STACK.size() > MAX_DEPTH) {
                STACK.removeLast();
            }
        }
        return 1;
    }

    /** Возврат в игру (или на титульный экран) — история больше не нужна. */
    public static void clear() {
        STACK.clear();
    }

    private static boolean contains(Object screen) {
        for (Iterator<WeakReference<Object>> it = STACK.iterator(); it.hasNext(); ) {
            Object s = deref(it.next());
            if (s == null) {
                it.remove(); // экран уже собран сборщиком мусора
                continue;
            }
            if (s == screen) return true;
        }
        return false;
    }

    private static Object deref(WeakReference<Object> ref) {
        return ref == null ? null : ref.get();
    }
}
