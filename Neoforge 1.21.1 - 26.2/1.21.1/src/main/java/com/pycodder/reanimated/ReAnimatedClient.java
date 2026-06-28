package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа NeoForge. Анимации полностью работают через миксины;
 * здесь только загружаем конфиг (config/reanimated.json) и пишем в лог.
 */
@Mod(value = "reanimated", dist = Dist.CLIENT)
public class ReAnimatedClient {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ReAnimatedClient() {
        ReAnimatedConfig.get(); // загрузить/создать конфиг
        LOGGER.info("[ReAnimated] UI animations loaded (NeoForge).");
    }
}
