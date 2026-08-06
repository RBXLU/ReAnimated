package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Точка входа NeoForge. Анимации работают через миксины; здесь — загрузка конфига
 * и два независимых способа открыть настройки мода:
 *
 *  1. Кнопка «Config» в списке модов NeoForge ({@link IConfigScreenFactory}) — работает
 *     всегда, что бы ни стояло рядом. Это аналог интеграции с ModMenu на Fabric.
 *  2. Кнопка «ReAnimated settings» прямо на экране настроек графики — удобнее, но
 *     зависит от того, чей это экран и что на нём уже нарисовано.
 */
@Mod(value = "reanimated", dist = Dist.CLIENT)
public class ReAnimatedClient {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SuppressWarnings("unused") // ModContainer внедряет загрузчик
    public ReAnimatedClient(ModContainer container) {
        ReAnimatedConfig.get(); // загрузить/создать конфиг
        // Кнопка настроек в списке модов — не зависит ни от каких других модов.
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (c, parent) -> new ReAnimatedConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(ReAnimatedClient::onScreenInit);
        LOGGER.info("[ReAnimated] UI animations loaded (NeoForge).");
    }

    private static final int BUTTON_W = 140;
    private static final int BUTTON_H = 20;
    private static final int MARGIN = 6;

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof VideoSettingsScreen) && !reanimated$isModVideoScreen(screen)) {
            return;
        }

        // Место под кнопку не зашито в координатах: у Sodium и прочих замен экрана
        // графики свой макет, и фиксированная точка (6,6) ложилась поверх их вкладок.
        // Берём первое место из списка кандидатов, где нет чужих виджетов.
        int[] spot = reanimated$freeSpot(screen);
        if (spot == null) {
            // Свободного места нет — лучше ничего не рисовать, чем поверх чужого текста.
            // Настройки всё равно доступны из списка модов.
            LOGGER.debug("[ReAnimated] No free spot on {} — settings available via the mods list",
                    screen.getClass().getName());
            return;
        }

        event.addListener(Button.builder(
                        Component.translatable("reanimated.config.open"),
                        b -> Minecraft.getInstance().setScreen(new ReAnimatedConfigScreen(screen)))
                .bounds(spot[0], spot[1], BUTTON_W, BUTTON_H).build());
    }

    /**
     * Первый из углов экрана, где кнопка ни с чем не пересечётся, или {@code null}.
     * Порядок начинается с левого верхнего — там кнопка стояла раньше и на ванильном
     * экране это место свободно, так что ванильный вид не меняется.
     */
    private static int[] reanimated$freeSpot(Screen screen) {
        int right = screen.width - BUTTON_W - MARGIN;
        int bottom = screen.height - BUTTON_H - MARGIN;
        int[][] candidates = {
            {MARGIN, MARGIN},
            {MARGIN, bottom},
            {right, MARGIN},
            {right, bottom},
            {MARGIN, MARGIN + BUTTON_H + 4},
            {right, MARGIN + BUTTON_H + 4},
        };
        for (int[] c : candidates) {
            if (c[0] >= 0 && c[1] >= 0 && reanimated$isFree(screen, c[0], c[1])) {
                return c;
            }
        }
        return null;
    }

    /** Пересекается ли прямоугольник кнопки с каким-нибудь видимым виджетом экрана. */
    private static boolean reanimated$isFree(Screen screen, int x, int y) {
        for (GuiEventListener e : screen.children()) {
            if (!(e instanceof AbstractWidget w) || !w.visible) {
                continue;
            }
            boolean overlaps = x < w.getX() + w.getWidth() && w.getX() < x + BUTTON_W
                            && y < w.getY() + w.getHeight() && w.getY() < y + BUTTON_H;
            if (overlaps) {
                return false;
            }
        }
        return true;
    }

    /**
     * Экран видео-настроек, подменённый сторонним модом (Sodium, VulkanMod, Embeddium,
     * Iris и т.п.). Такие моды не наследуют ванильный {@code VideoSettingsScreen}, а
     * ставят на его место свой класс — поэтому {@code instanceof} их не ловит, и кнопка
     * мода просто не появлялась. Определяем по имени класса: так же, как на Fabric.
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
