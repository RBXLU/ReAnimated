package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.ScreenOverride;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.anim.UiPreset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class ScreenRulesScreen extends GameOptionsScreen {

    private static final Text CLEAR = Text.translatable("reanimated.screens.clear");
    private static final Text HINT = Text.translatable("reanimated.screens.hint");

    public ScreenRulesScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("reanimated.screens.title"));
    }

    @Override
    protected void addOptions() {
        this.body.addWidgetEntry(
            new TextWidget(HINT.copy().formatted(Formatting.GRAY), this.textRenderer), null);
        this.body.addWidgetEntry(
            ButtonWidget.builder(CLEAR, b -> this.clearAll()).dimensions(0, 0, 150, 20).build(), null);

        List<String> keys = ScreenRules.listForEditor();
        for (String key : keys) {
            ScreenOverride current = ReAnimatedConfig.get().screenOverrides.get(key);
            this.header(key, current);
            this.body.addAll(
                animate(key, current == null || current.animate),
                preset(key, current == null ? UiPreset.INHERIT : current.preset),
                speed(key, current == null ? 0 : current.speedTicks)
            );
        }
    }

    private void header(String key, ScreenOverride current) {
        Text name = Text.literal(ScreenRules.displayName(key))
            .formatted(current == null || current.isDefault() ? Formatting.GOLD : Formatting.GREEN);
        if (ScreenRules.isModded(key)) {
            name = name.copy().append(Text.literal(" ").append(
                Text.translatable("reanimated.screens.modded").formatted(Formatting.DARK_GRAY)));
        }
        this.body.addWidgetEntry(new TextWidget(name, this.textRenderer), null);
    }

    private void clearAll() {
        ReAnimatedConfig.get().screenOverrides.clear();
        ReAnimatedConfig.get().save();
        this.clearAndInit();
    }

    private void apply(String key, Consumer<ScreenOverride> change) {
        change.accept(ScreenRules.getOrCreate(key));
        ScreenRules.prune();
        ReAnimatedConfig.get().save();
    }

    private SimpleOption<Boolean> animate(String key, boolean current) {
        return SimpleOption.ofBoolean("reanimated.screens.animate", current,
            v -> apply(key, o -> o.animate = v));
    }

    private SimpleOption<UiPreset> preset(String key, UiPreset current) {
        return new SimpleOption<>(
            "reanimated.screens.preset",
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable("reanimated.screens.preset")
                .append(Text.literal(": " + value.display)),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                reanimated$presetValues(),
                com.mojang.serialization.Codec.INT.xmap(i -> UiPreset.values()[i], UiPreset::ordinal)),
            current == null ? UiPreset.INHERIT : current,
            value -> apply(key, o -> o.preset = value));
    }

    private static java.util.List<UiPreset> reanimated$presetValues() {
        java.util.List<UiPreset> values = new java.util.ArrayList<>();
        values.add(UiPreset.INHERIT);
        java.util.Collections.addAll(values, UiPreset.MAIN);
        return values;
    }

    private SimpleOption<Integer> speed(String key, int current) {
        return new SimpleOption<>(
            "reanimated.screens.speed",
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable("reanimated.screens.speed").append(
                value <= 0
                    ? Text.literal(": ").append(Text.translatable("reanimated.screens.inherit"))
                    : Text.literal(": " + value + " t")),
            new SimpleOption.ValidatingIntSliderCallbacks(0, 40),
            Math.max(0, current),
            value -> apply(key, o -> o.speedTicks = value));
    }
}
