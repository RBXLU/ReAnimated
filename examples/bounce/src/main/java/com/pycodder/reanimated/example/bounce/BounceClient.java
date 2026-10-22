package com.pycodder.reanimated.example.bounce;

import com.pycodder.reanimated.api.ReAnimatedApi;
import com.pycodder.reanimated.api.SettingsEntry;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Пример расширения ReAnimated.
 *
 * Делает две вещи: добавляет свою анимацию появления («прыжок») и кнопку в настройках
 * ReAnimated, которой эту анимацию можно выключить. Никаких миксинов, никакого доступа
 * к внутренностям мода — только его публичное API.
 */
public class BounceClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("reanimated-bounce");
    private static final BouncePreset PRESET = new BouncePreset();

    @Override
    public void onInitializeClient() {
        applyState();

        // Кнопка в секции «Расширения» экрана настроек ReAnimated. Надпись показывает
        // текущее состояние, нажатие переключает его — экран настроек пересобирается
        // сам, поэтому надпись сразу обновляется.
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

    /**
     * Включено — анимация зарегистрирована и её видно в списке пресетов; выключено —
     * снята с регистрации, и ReAnimated сам возвращает экраны к обычному пресету.
     */
    private static void applyState() {
        if (BounceConfig.isEnabled()) {
            ReAnimatedApi.registerPreset(PRESET);
        } else {
            ReAnimatedApi.unregisterPreset(BouncePreset.ID);
        }
    }
}
