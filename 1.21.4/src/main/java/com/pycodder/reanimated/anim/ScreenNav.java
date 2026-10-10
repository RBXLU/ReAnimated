package com.pycodder.reanimated.anim;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class ScreenNav {
    private ScreenNav() {}

    private static final int MAX_DEPTH = 16;

    private static final Deque<WeakReference<Object>> STACK = new ArrayDeque<>();

    public static int accept(Object current, Object next) {
        if (next != null && contains(next)) {
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

    public static void clear() {
        STACK.clear();
    }

    private static boolean contains(Object screen) {
        for (Iterator<WeakReference<Object>> it = STACK.iterator(); it.hasNext(); ) {
            Object s = deref(it.next());
            if (s == null) {
                it.remove();
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
