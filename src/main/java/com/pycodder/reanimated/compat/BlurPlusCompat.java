package com.pycodder.reanimated.compat;

import com.pycodder.reanimated.ReAnimatedClient;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Совместимость с Blur+ (Motschen, мод {@code blur}): он заменяет ванильный фон за
 * экраном — размывает мир, рисует свой градиент и плавно проявляет и гасит оба.
 *
 * Разделение простое: фоном за экраном распоряжается Blur+, панелью — ReAnimated.
 *
 * Как Blur+ решает, играть ли появление или угасание: в начале каждого кадра он
 * ставит обеим анимациям цель «погаснуть», а если экран в этом кадре зовёт ванильную
 * отрисовку фона — меняет её на «проявиться». Своего API у Blur+ нет, поэтому мы
 * договариваемся с ним через то же самое: пока ReAnimated отыгрывает закрытие, фон
 * экрана не вызывается, а рисует его сам Blur+ ({@code Blur.renderBackground} — тот
 * же вызов, которым Blur+ дорисовывает угасание, когда экрана уже нет). Blur+ видит
 * «фона нет» и начинает гаснуть ровно тогда, когда начинает уезжать панель, а не
 * после того, как она уже уехала.
 *
 * Всё через рефлексию: ни зависимости при сборке, ни требований к версии Blur+. Если
 * Blur+ нет или в его версии нет нужного метода — ReAnimated работает как без него.
 */
public final class BlurPlusCompat {
    private BlurPlusCompat() {}

    private static final String BLUR_CLASS = "eu.midnightdust.blur.Blur";

    private static boolean resolved = false;
    /** Класс Blur+ или null, если мода нет. */
    private static Class<?> blur = null;
    /** Для какого класса контекста отрисовки найден метод (он один на всю игру). */
    private static Class<?> handleFor = null;
    private static MethodHandle renderBackground = null;
    /** Вызов однажды упал — больше не пробуем, чтобы не сыпать ошибками каждый кадр. */
    private static boolean broken = false;

    /** Установлен ли Blur+. */
    public static boolean present() {
        resolve();
        return blur != null;
    }

    /**
     * Рисует фон за экраном руками Blur+ — размытие и градиент с его текущими, уже
     * анимированными значениями.
     *
     * @param graphics контекст отрисовки GUI этой версии игры
     * @return true, если Blur+ нарисовал фон; false — делегировать нельзя, ванильный
     *         фон надо рисовать как обычно
     */
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
            // Без инициализации: статический блок Blur+ отработает, когда до него дойдёт сама игра.
            blur = Class.forName(BLUR_CLASS, false, BlurPlusCompat.class.getClassLoader());
            ReAnimatedClient.LOGGER.info("[ReAnimated] Blur+ detected — the background behind screens is left to Blur+");
        } catch (Throwable t) {
            blur = null; // Blur+ не установлен
        }
    }

    /**
     * {@code Blur.renderBackground(контекст)}. Тип контекста у разных версий игры свой
     * ({@code GuiGraphics}, {@code GuiGraphicsExtractor}, {@code DrawContext}), поэтому
     * метод ищется по имени и по тому, принимает ли он наш контекст.
     */
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
