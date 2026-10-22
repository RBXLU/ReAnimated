package com.pycodder.reanimated;

import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.config.ScreenSettingsScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Клавиша «Настроить этот экран»: открывает настройки того экрана, на котором её
 * нажали. Обычные клавиши игра опрашивает только в мире, поэтому нажатие ловится в
 * самом экране ({@code ScreenKeysMixin}).
 */
public final class ReAnimatedKeys {
    private ReAnimatedKeys() {}

    private static final String CATEGORY = "key.category.reanimated.main";

    public static final KeyBinding SCREEN_SETTINGS = new KeyBinding(
        "key.reanimated.screen_settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7, CATEGORY);

    public static void register() {
        KeyBindingHelper.registerKeyBinding(SCREEN_SETTINGS);
    }

    /** true — нажатие наше, экран его обрабатывать не должен. */
    public static boolean onScreenKey(Screen screen, int keyCode, int scanCode) {
        if (!SCREEN_SETTINGS.matchesKey(keyCode, scanCode)) return false;
        return open(screen);
    }

    private static boolean open(Screen screen) {
        // Свои экраны настроек не настраиваются с самих себя.
        if (screen.getClass().getName().startsWith("com.pycodder.reanimated.config.")) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        String key = ScreenRules.key(screen);
        Screen parent = screen;
        // Контейнер нельзя оставить открытым под другим экраном: сервер считает его
        // открытым, а вернуться в него как в обычный экран нельзя. Закрываем как
        // положено, после настроек — обратно в мир.
        if (screen instanceof HandledScreen<?> && client.player != null) {
            client.player.closeHandledScreen();
            parent = null;
        }
        client.setScreen(new ScreenSettingsScreen(parent, key));
        return true;
    }
}
