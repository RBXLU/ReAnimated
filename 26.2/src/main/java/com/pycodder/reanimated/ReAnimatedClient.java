package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get();

        // Кнопка "ReAnimated settings" на экране Настройки графики.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof VideoSettingsScreen) {
                Button button = Button.builder(
                        Component.translatable("reanimated.config.open"),
                        b -> client.setScreenAndShow(new ReAnimatedConfigScreen(screen)))
                    .bounds(6, 6, 140, 20).build();
                Screens.getWidgets(screen).add(button);
            }
        });

        LOGGER.info("[ReAnimated] UI animations loaded (Minecraft 26.x).");
    }
}
