package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get(); // загрузить/создать конфиг

        // Кнопка "ReAnimated settings" на экране настроек графики.
        // Ловим и ванильный VideoOptionsScreen, и экраны-замены от Sodium / VulkanMod /
        // Embeddium / Iris и других модов (определяем по имени класса), чтобы настройки
        // мода отображались прямо в их меню. Плюс кнопка доступна через ModMenu.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof VideoOptionsScreen || reanimated$isModVideoScreen(screen)) {
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

    /**
     * Экран видео-настроек, добавленный сторонним модом (Sodium, VulkanMod, Embeddium,
     * Iris и т.п.). Такие моды заменяют ванильный экран графики своим классом, поэтому
     * определяем его по имени класса — так кнопка настроек мода появляется в их меню.
     */
    private static boolean reanimated$isModVideoScreen(Screen screen) {
        String name = screen.getClass().getName().toLowerCase(Locale.ROOT);
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
