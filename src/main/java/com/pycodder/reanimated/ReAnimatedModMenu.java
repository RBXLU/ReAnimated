package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Интеграция с ModMenu — кнопка настроек в списке модов ModMenu.
 * Работает независимо от экрана видео-настроек, поэтому конфиг доступен даже
 * с Sodium / VulkanMod и другими модами, заменяющими стандартный экран.
 * Если ModMenu не установлен, точка входа "modmenu" просто не вызывается.
 */
public class ReAnimatedModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ReAnimatedConfigScreen::new;
    }
}
