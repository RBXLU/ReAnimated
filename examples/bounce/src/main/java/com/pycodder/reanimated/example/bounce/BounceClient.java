package com.pycodder.reanimated.example.bounce;

import com.pycodder.reanimated.api.ReAnimatedApi;
import com.pycodder.reanimated.api.SettingsEntry;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BounceClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("reanimated-bounce");
    private static final BouncePreset PRESET = new BouncePreset();

    @Override
    public void onInitializeClient() {
        applyState();

        ReAnimatedApi.registerSettingsEntry(new SettingsEntry() {
            @Override
            public String id() {
                return "reanimated_bounce:toggle";
            }

            @Override
            public String label() {
                return BounceConfig.isEnabled() ? "Bounce: ON" : "Bounce: OFF";
            }

            @Override
            public void open(Object parent) {
                BounceConfig.setEnabled(!BounceConfig.isEnabled());
                applyState();
            }
        });

        LOGGER.info("[bounce] Расширение подключено к ReAnimated API v{}", ReAnimatedApi.API_VERSION);
    }

    private static void applyState() {
        if (BounceConfig.isEnabled()) {
            ReAnimatedApi.registerPreset(PRESET);
        } else {
            ReAnimatedApi.unregisterPreset(BouncePreset.ID);
        }
    }
}
