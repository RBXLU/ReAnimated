package com.pycodder.reanimated;

import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ReAnimatedClient implements ClientModInitializer {
    public static final String MOD_ID = "reanimated";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReAnimatedConfig.get();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof VideoOptionsScreen || reanimated$isModVideoScreen(screen)) {
                int[] spot = reanimated$freeSpot(screen);
                if (spot == null) {
                    LOGGER.debug("[ReAnimated] No free spot on {} - settings available via the mods list",
                            screen.getClass().getName());
                    return;
                }
                ButtonWidget button = ButtonWidget.builder(
                        Text.translatable("reanimated.config.open"),
                        b -> client.setScreen(new ReAnimatedConfigScreen(screen)))
                    .dimensions(spot[0], spot[1], spot[2], BUTTON_H)
                    .build();
                Screens.getButtons(screen).add(button);
            }
        });

        LOGGER.info("[ReAnimated] UI animations loaded.");
    }

    private static boolean reanimated$isModVideoScreen(Screen screen) {
        String name = screen.getClass().getName().toLowerCase(Locale.ROOT);
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

    private static final int BUTTON_W = 140;
    /** Below this the label would turn into scrolling text, so no button is placed. */
    private static final int BUTTON_MIN_W = 90;
    private static final int BUTTON_H = 20;
    private static final int MARGIN = 6;

    /** First edge strip where the button collides with nothing: x, y, width. Null when there is none. */
    private static int[] reanimated$freeSpot(Screen screen) {
        int[] rows = {MARGIN, screen.height - BUTTON_H - MARGIN, MARGIN + BUTTON_H + 4};
        for (int y : rows) {
            if (y < 0) {
                continue;
            }
            for (boolean left : new boolean[] {true, false}) {
                int span = reanimated$freeSpan(screen, y, left);
                if (span < BUTTON_MIN_W) {
                    continue;
                }
                int w = Math.min(BUTTON_W, span);
                int x = left ? MARGIN : screen.width - MARGIN - w;
                if (reanimated$occupied(screen, x, y, w)) {
                    continue;
                }
                return new int[] {x, y, w};
            }
        }
        return null;
    }

    /** Free width in the button-high strip: from the left edge rightwards, or from the right edge leftwards. */
    private static int reanimated$freeSpan(Screen screen, int y, boolean left) {
        int limit = left ? screen.width - MARGIN : MARGIN;
        for (Element e : screen.children()) {
            int[] b = reanimated$bounds(e);
            if (b == null) {
                continue;
            }
            if (b[1] >= y + BUTTON_H || b[1] + b[3] <= y) {
                continue;
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

    /** Bounds of a screen element, or null when it reports none. */
    private static int[] reanimated$bounds(Element e) {
        if (e instanceof ClickableWidget w) {
            return w.visible ? new int[] {w.getX(), w.getY(), w.getWidth(), w.getHeight()} : null;
        }
        ScreenRect r = e.getNavigationFocus();
        if (r == null || r.width() <= 0 || r.height() <= 0) {
            return null;
        }
        return new int[] {r.getLeft(), r.getTop(), r.width(), r.height()};
    }

    /**
     * Last resort for widgets that report no bounds at all. Sodium 0.6 draws its own widgets
     * and implements only isMouseOver, so the candidate rectangle is probed point by point.
     */
    private static boolean reanimated$occupied(Screen screen, int x, int y, int w) {
        for (Element e : screen.children()) {
            if (reanimated$bounds(e) != null) {
                continue;
            }
            for (int px = x + 2; px <= x + w - 2; px += 8) {
                for (int py = y + 2; py <= y + BUTTON_H - 2; py += 8) {
                    try {
                        if (e.isMouseOver(px, py)) {
                            return true;
                        }
                    } catch (Throwable ignored) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
