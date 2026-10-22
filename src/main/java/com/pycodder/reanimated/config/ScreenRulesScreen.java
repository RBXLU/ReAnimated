package com.pycodder.reanimated.config;

import com.pycodder.reanimated.ReAnimatedKeys;
import com.pycodder.reanimated.anim.ScreenRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Список экранов и модов, которым можно задать свои настройки. Каждый пункт открывает
 * {@link ScreenSettingsScreen}; настроенные подсвечены зелёным.
 *
 * Список составляется из экранов, которые игрок открывал в этом запуске, плюс тех,
 * для которых настройки уже заданы: перечислить все экраны сборки заранее неоткуда,
 * они появляются только когда их создаёт игра или мод. Свежие — сверху.
 */
public class ScreenRulesScreen extends GameOptionsScreen {

    private static final Text CLEAR = Text.translatable("reanimated.screens.clear");
    private static final Text MODS = Text.translatable("reanimated.screens.mods");
    private static final Text SCREENS = Text.translatable("reanimated.screens.list");

    private static final int WIDE = 310;

    public ScreenRulesScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("reanimated.screens.title"));
    }

    @Override
    protected void addOptions() {
        this.gray(Text.translatable("reanimated.screens.hint"));
        this.gray(Text.translatable("reanimated.screens.hint_key", ReAnimatedKeys.SCREEN_SETTINGS.getBoundKeyLocalizedText()));
        this.body.addWidgetEntry(
            ButtonWidget.builder(CLEAR, b -> this.clearAll()).dimensions(0, 0, 150, 20).build(), null);

        List<String> mods = ScreenRules.modsForEditor();
        if (!mods.isEmpty()) {
            this.header(MODS);
            for (int i = 0; i < mods.size(); i += 2) {
                this.body.addWidgetEntry(entry(mods.get(i), 150),
                    i + 1 < mods.size() ? entry(mods.get(i + 1), 150) : null);
            }
        }

        this.header(SCREENS);
        for (String key : ScreenRules.listForEditor()) {
            this.body.addWidgetEntry(entry(key, WIDE), null);
        }
    }

    private void gray(Text text) {
        this.body.addWidgetEntry(new TextWidget(text.copy().formatted(Formatting.GRAY), this.textRenderer), null);
    }

    private void header(Text text) {
        this.body.addWidgetEntry(new TextWidget(text.copy().formatted(Formatting.YELLOW), this.textRenderer), null);
    }

    /** Кнопка экрана или мода: настроенные — зелёные, экраны из модов — с пометкой. */
    private ButtonWidget entry(String key, int width) {
        Text name = Text.literal(ScreenRules.displayName(key))
            .formatted(ScreenRules.customized(key) ? Formatting.GREEN : Formatting.WHITE);
        if (ScreenRules.isModded(key)) {
            name = name.copy().append(Text.literal(" ").append(
                Text.translatable("reanimated.screens.modded").formatted(Formatting.DARK_GRAY)));
        }
        return ButtonWidget.builder(name, b -> this.client.setScreen(new ScreenSettingsScreen(this, key)))
            .dimensions(0, 0, width, 20).build();
    }

    private void clearAll() {
        ReAnimatedConfig.get().screenOverrides.clear();
        ReAnimatedConfig.get().save();
        this.clearAndInit(); // кнопки читают значения при создании — пересобираем список
    }
}
