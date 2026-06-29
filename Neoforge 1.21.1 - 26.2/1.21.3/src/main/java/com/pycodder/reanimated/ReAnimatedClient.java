package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа NeoForge. Анимации работают через миксины; здесь — загрузка конфига
 * и кнопка "ReAnimated settings" на экране Настройки графики (VideoSettingsScreen).
 */
@Mod(value = "reanimated", dist = Dist.CLIENT)
public class ReAnimatedClient {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ReAnimatedClient() {
        ReAnimatedConfig.get(); // загрузить/создать конфиг
        NeoForge.EVENT_BUS.addListener(ReAnimatedClient::onScreenInit);
        LOGGER.info("[ReAnimated] UI animations loaded (NeoForge).");
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof VideoSettingsScreen) {
            event.addListener(Button.builder(
                            Component.translatable("reanimated.config.open"),
                            b -> Minecraft.getInstance().setScreen(new ReAnimatedConfigScreen(screen)))
                    .bounds(6, 6, 140, 20).build());
        }
    }
}
