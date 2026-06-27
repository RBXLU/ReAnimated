package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get(); // загрузить/создать конфиг

        // Кнопка "ReAnimated settings" на экране Настройки графики.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof VideoOptionsScreen) {
                ButtonWidget button = ButtonWidget.builder(
                        Text.translatable("reanimated.config.open"),
                        b -> client.setScreen(new ReAnimatedConfigScreen(screen)))
                    .dimensions(6, 6, 140, 20)
                    .build();
                Screens.getButtons(screen).add(button);
            }
        });

        LOGGER.info("[ReAnimated] UI animations loaded.");
    }
}
