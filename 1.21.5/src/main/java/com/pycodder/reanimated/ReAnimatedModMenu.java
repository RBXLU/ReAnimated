package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** ModMenu integration: the settings button in ModMenu's mod list. */
public class ReAnimatedModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ReAnimatedConfigScreen::new;
    }
}
