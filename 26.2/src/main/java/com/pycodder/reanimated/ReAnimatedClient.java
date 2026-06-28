package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get();
        LOGGER.info("[ReAnimated] UI animations loaded (Minecraft 26.x).");
    }
}
