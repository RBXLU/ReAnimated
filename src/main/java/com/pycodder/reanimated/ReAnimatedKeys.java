package com.pycodder.reanimated;

import com.mojang.blaze3d.platform.InputConstants;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.config.ScreenSettingsScreen;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;

/**
 * Клавиша «Настроить этот экран»: открывает настройки того экрана, на котором её
 * нажали. Обычные клавиши игра опрашивает только в мире, поэтому нажатие ловится в
 * самом экране ({@code ScreenKeysMixin}).
 */
public final class ReAnimatedKeys {
    private ReAnimatedKeys() {}

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("reanimated", "main"));

    public static final KeyMapping SCREEN_SETTINGS = new KeyMapping(
        "key.reanimated.screen_settings", InputConstants.KEY_F7, CATEGORY);

    public static void register() {
        KeyMappingHelper.registerKeyMapping(SCREEN_SETTINGS);
    }

    /** true — нажатие наше, экран его обрабатывать не должен. */
    public static boolean onScreenKey(Screen screen, KeyEvent event) {
        if (!SCREEN_SETTINGS.matches(event)) return false;
        return open(screen);
    }

    private static boolean open(Screen screen) {
        // Свои экраны настроек не настраиваются с самих себя.
        if (screen.getClass().getName().startsWith("com.pycodder.reanimated.config.")) return false;
        Minecraft client = Minecraft.getInstance();
        String key = ScreenRules.key(screen);
        Screen parent = screen;
        // Контейнер нельзя оставить открытым под другим экраном: сервер считает его
        // открытым, а вернуться в него как в обычный экран нельзя. Закрываем как
        // положено, после настроек — обратно в мир.
        if (screen instanceof AbstractContainerScreen<?> && client.player != null) {
            client.player.closeContainer();
            parent = null;
        }
        client.gui.setScreen(new ScreenSettingsScreen(parent, key));
        return true;
    }
}
