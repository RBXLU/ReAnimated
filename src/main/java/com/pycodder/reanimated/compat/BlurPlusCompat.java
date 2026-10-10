package com.pycodder.reanimated.compat;

import com.pycodder.reanimated.ReAnimatedClient;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class BlurPlusCompat {
    private BlurPlusCompat() {}

    private static final String BLUR_CLASS = "eu.midnightdust.blur.Blur";

    private static boolean resolved = false;
    private static Class<?> blur = null;
    private static Class<?> handleFor = null;
    private static MethodHandle renderBackground = null;
    private static boolean broken = false;

    public static boolean present() {
        resolve();
        return blur != null;
    }

    public static boolean renderBackground(Object graphics) {
        if (broken || graphics == null || !present()) return false;
        MethodHandle h = handle(graphics.getClass());
        if (h == null) return false;
        try {
            h.invoke(graphics);
            return true;
        } catch (Throwable t) {
            broken = true;
            ReAnimatedClient.LOGGER.warn("[ReAnimated] Blur+ background call failed — closing falls back to the vanilla background", t);
            return false;
        }
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            blur = Class.forName(BLUR_CLASS, false, BlurPlusCompat.class.getClassLoader());
            ReAnimatedClient.LOGGER.info("[ReAnimated] Blur+ detected — the background behind screens is left to Blur+");
        } catch (Throwable t) {
            blur = null;
        }
    }

    private static MethodHandle handle(Class<?> graphicsClass) {
        if (handleFor == graphicsClass) return renderBackground;
        handleFor = graphicsClass;
        renderBackground = null;
        for (Method m : blur.getMethods()) {
            if (!m.getName().equals("renderBackground")) continue;
            if (!Modifier.isStatic(m.getModifiers()) || m.getParameterCount() != 1) continue;
            if (!m.getParameterTypes()[0].isAssignableFrom(graphicsClass)) continue;
            try {
                renderBackground = MethodHandles.publicLookup().unreflect(m);
            } catch (Throwable t) {
                renderBackground = null;
            }
            break;
        }
        if (renderBackground == null) {
            ReAnimatedClient.LOGGER.info("[ReAnimated] This Blur+ version has no Blur.renderBackground — closing keeps the vanilla background");
        }
        return renderBackground;
    }
}
