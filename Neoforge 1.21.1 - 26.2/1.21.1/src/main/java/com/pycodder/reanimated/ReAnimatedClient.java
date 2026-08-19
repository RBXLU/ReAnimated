package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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
    /** Уже этого кнопку не ставим — надпись превратилась бы в бегущую строку. */
    private static final int BUTTON_MIN_W = 90;
    private static final int BUTTON_H = 20;
    private static final int MARGIN = 6;

    /** Найденное место под кнопку: координаты и ширина, ужатая под свободный зазор. */
    private record Spot(int x, int y, int width) {}

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof VideoSettingsScreen) && !reanimated$isModVideoScreen(screen)) {
            return;
        }

        // Место под кнопку не зашито в координатах: у Sodium и прочих замен экрана
        // графики свой макет, и фиксированная точка (6,6) ложилась поверх их вкладок.
        // Берём первую полосу у края экрана, где нет чужих виджетов.
        Spot spot = reanimated$freeSpot(screen);
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
                .bounds(spot.x(), spot.y(), spot.width(), BUTTON_H).build());
    }

    /**
     * Первое место у края экрана, где кнопка ни с чем не пересечётся, или {@code null}.
     * Порядок начинается с левого верхнего — там кнопка стояла раньше и на ванильном
     * экране это место свободно, так что ванильный вид не меняется.
     *
     * Ширина подгоняется под найденный зазор: у Sodium свободна только полоска слева
     * от его панели, и кнопка полной ширины туда уже не влезает.
     */
    private static Spot reanimated$freeSpot(Screen screen) {
        int[] rows = {MARGIN, screen.height - BUTTON_H - MARGIN, MARGIN + BUTTON_H + 4};
        for (int y : rows) {
            if (y < 0) {
                continue;
            }
            for (boolean left : new boolean[] {true, false}) {
                int span = reanimated$freeSpan(screen, y, left);
                if (span >= BUTTON_MIN_W) {
                    int w = Math.min(BUTTON_W, span);
                    return new Spot(left ? MARGIN : screen.width - MARGIN - w, y, w);
                }
            }
        }
        return null;
    }

    /**
     * Ширина свободного места в полосе высотой с кнопку: от левого края вправо до первого
     * чужого виджета ({@code left}) или от правого края влево до последнего ({@code !left}).
     */
    private static int reanimated$freeSpan(Screen screen, int y, boolean left) {
        int limit = left ? screen.width - MARGIN : MARGIN;
        for (GuiEventListener e : screen.children()) {
            int[] b = reanimated$bounds(e);
            if (b == null) {
                continue;
            }
            if (b[1] >= y + BUTTON_H || b[1] + b[3] <= y) {
                continue; // с полосой кнопки по вертикали не пересекается
            }
            if (left) {
                if (b[0] + b[2] > MARGIN) {
                    limit = Math.min(limit, b[0]);
                }
            } else if (b[0] < screen.width - MARGIN) {
                limit = Math.max(limit, b[0] + b[2]);
            }
        }
        return left ? limit - MARGIN : screen.width - MARGIN - limit;
    }

    /**
     * Границы виджета экрана: x, y, ширина, высота — или {@code null}, если он ничего
     * не занимает.
     *
     * Раньше здесь проверялись только наследники ванильного {@code AbstractWidget}, и
     * ровно поэтому кнопка ложилась поверх Sodium: его поиск, список страниц и кнопки
     * ванильный виджет НЕ наследуют (свой {@code AbstractWidget} из пакета мода), так что
     * экран казался пустым и место (6,6) — свободным. Iris, Embeddium и VulkanMod устроены
     * так же. Общий для всех слушателей {@code getRectangle()} видит и их.
     */
    private static int[] reanimated$bounds(GuiEventListener e) {
        if (e instanceof AbstractWidget w) {
            return w.visible ? new int[] {w.getX(), w.getY(), w.getWidth(), w.getHeight()} : null;
        }
        ScreenRectangle r = e.getRectangle();
        if (r == null || r.width() <= 0 || r.height() <= 0) {
            return null;
        }
        return new int[] {r.position().x(), r.position().y(), r.width(), r.height()};
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
