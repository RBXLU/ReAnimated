package com.pycodder.reanimated;

import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.compat.FabricModLookup;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get();

        // Правила «на все экраны мода»: мод экрана узнаём по файлу, из которого загружен его класс.
        ScreenRules.setModLookup(new FabricModLookup());
        ReAnimatedKeys.register();

        // Кнопка "ReAnimated settings" на экране Настройки графики.
        // Ловим и ванильный VideoSettingsScreen, и экраны-замены от Sodium / VulkanMod /
        // Embeddium / Iris и других модов (определяем по имени класса), чтобы настройки
        // мода отображались прямо в их меню.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof VideoSettingsScreen || reanimated$isModVideoScreen(screen)) {
                Button button = Button.builder(
                        Component.translatable("reanimated.config.open"),
                        b -> client.setScreenAndShow(new ReAnimatedConfigScreen(screen)))
                    .bounds(6, 6, 140, 20).build();
                Screens.getWidgets(screen).add(button);
            }
        });

        LOGGER.info("[ReAnimated] UI animations loaded (Minecraft 26.x).");
    }

    /**
     * Экран видео-настроек, добавленный сторонним модом (Sodium, VulkanMod, Embeddium,
     * Iris и т.п.). Такие моды заменяют ванильный экран графики своим классом, поэтому
     * определяем его по имени класса — так кнопка настроек мода появляется в их меню.
     */
    private static boolean reanimated$isModVideoScreen(Screen screen) {
        String name = screen.getClass().getName().toLowerCase(Locale.ROOT);
        // Если Sodium показывает наши настройки своей страницей (Sodium 0.8+), кнопка
        // поверх его экрана только мешает — она дублирует то, что уже есть в списке слева.
        if (com.pycodder.reanimated.compat.SodiumPageState.registered && name.contains("sodium")) {
            return false;
        }
        boolean fromKnownMod = name.contains("sodium")
                || name.contains("vulkanmod")
                || name.contains("embeddium")
                || name.contains("rubidium")
                || name.contains("magnesium")
                || name.contains("iris");
        boolean looksLikeSettings = name.contains("video")
                || name.contains("option")
                || name.contains("setting")
                || name.contains("graphic");
        return fromKnownMod && looksLikeSettings;
    }
}
