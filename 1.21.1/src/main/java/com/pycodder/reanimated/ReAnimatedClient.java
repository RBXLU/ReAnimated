package com.pycodder.reanimated;

import com.pycodder.reanimated.anim.ScreenFlags;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.compat.FabricModLookup;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get(); // загрузить/создать конфиг

        // В обычной сборке классы игры называются class_XXX, и меню паузы, чат и экраны
        // загрузки по имени не узнаются. Отдаём их ScreenFlags по самим классам — ссылки
        // на классы Loom переименовывает вместе со всем остальным кодом.
        ScreenFlags.registerVanilla(
            Set.of(GameMenuScreen.class),
            Set.of(ChatScreen.class),
            Set.of(ConnectScreen.class, LevelLoadingScreen.class, ProgressScreen.class,
                ReconfiguringScreen.class, MessageScreen.class, DownloadingTerrainScreen.class));
        ScreenFlags.registerPvp(Set.of(HandledScreen.class, DeathScreen.class));

        // Правила «на все экраны мода»: мод экрана узнаём по файлу, из которого загружен его класс.
        ScreenRules.setModLookup(new FabricModLookup());
        ReAnimatedKeys.register();

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
