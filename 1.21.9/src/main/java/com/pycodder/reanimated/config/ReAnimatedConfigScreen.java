package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.UiPreset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.function.Consumer;

/** Mod settings screen: sliders and toggles for each animation, plus a link to the author. */
public class ReAnimatedConfigScreen extends GameOptionsScreen {
    private static final String AUTHOR_URL = "https://modrinth.com/user/pycodder";
    private static final String TESTED_URL = "https://github.com/RBXLU/ReAnimated/blob/main/testedmods.txt";
    private static final Text CREDIT = Text.literal("mod by @pycodder");
    private static final Text TESTED = Text.translatable("reanimated.opt.tested_mods");
    private static final Text EDITOR = Text.translatable("reanimated.opt.profile_editor");
    private static final Text STUDIO = Text.translatable("reanimated.opt.studio");
    private static final Text EXPORT = Text.translatable("reanimated.opt.export");
    private static final Text IMPORT = Text.translatable("reanimated.opt.import");

    private Text notice = null;
    private long noticeTime = 0L;
    private static final long NOTICE_MS = 4000L;

    public ReAnimatedConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("reanimated.config.title"));
    }

    private void exportSettings() {
        this.client.keyboard.setClipboard(ReAnimatedConfig.get().toShareString());
        this.showNotice("reanimated.opt.export.done", false);
    }

    private void importSettings() {
        String text = this.client.keyboard.getClipboard();
        if (ReAnimatedConfig.applyShareString(text)) {
            this.showNotice("reanimated.opt.import.done", false);
            Text saved = this.notice;
            long savedTime = this.noticeTime;
            this.clearAndInit();
            this.notice = saved;
            this.noticeTime = savedTime;
        } else {
            this.showNotice("reanimated.opt.import.failed", true);
        }
    }

    private void showNotice(String key, boolean error) {
        this.notice = Text.translatable(key).formatted(
            error ? net.minecraft.util.Formatting.RED : net.minecraft.util.Formatting.GREEN);
        this.noticeTime = System.currentTimeMillis();
    }

    @Override
    protected void addOptions() {
        ReAnimatedConfig c = ReAnimatedConfig.get();

        section("reanimated.section.tools");
        this.body.addWidgetEntry(
            ButtonWidget.builder(STUDIO, b -> this.client.setScreen(new AnimationStudioScreen(this)))
                .dimensions(0, 0, 150, 20).build(),
            ButtonWidget.builder(EDITOR, b -> this.client.setScreen(new AnimProfileEditorScreen(this)))
                .dimensions(0, 0, 150, 20).build());
        this.body.addWidgetEntry(
            ButtonWidget.builder(EXPORT, b -> this.exportSettings())
                .dimensions(0, 0, 150, 20).build(),
            ButtonWidget.builder(IMPORT, b -> this.importSettings())
                .dimensions(0, 0, 150, 20).build());
        this.body.addWidgetEntry(
            ButtonWidget.builder(TESTED, b -> Util.getOperatingSystem().open(URI.create(TESTED_URL)))
                .dimensions(0, 0, 150, 20).build(), null);

        section("reanimated.section.general");
        this.body.addAll(
            preset("reanimated.opt.preset", c.uiPreset, v -> c.uiPreset = v),
            ticks("reanimated.opt.speed_ticks", 1, 40, c.animationSpeedTicks, v -> c.animationSpeedTicks = v),
            scope("reanimated.opt.animate_scope", c.animateModdedScreens, v -> c.animateModdedScreens = v),
            toggle("reanimated.opt.close_enabled", c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v),
            toggle("reanimated.opt.bg_fade", c.bgFadeEnabled, v -> c.bgFadeEnabled = v),
            toggle("reanimated.opt.lists_enabled", c.listsEnabled, v -> c.listsEnabled = v)
        );

        section("reanimated.section.menu");
        this.body.addAll(
            toggle("reanimated.opt.screen_enabled", c.screenOpenEnabled, v -> c.screenOpenEnabled = v),
            slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = v.floatValue()),
            easing("reanimated.opt.screen_easing", c.screenOpenEasing, v -> c.screenOpenEasing = v)
        );

        section("reanimated.section.pause");
        this.body.addAll(
            toggle("reanimated.opt.pause_enabled", c.pauseEnabled, v -> c.pauseEnabled = v),
            pausePreset("reanimated.opt.pause_preset", c.pausePreset, v -> c.pausePreset = v),
            ticks("reanimated.opt.pause_speed_ticks", 1, 40, c.pauseSpeedTicks, v -> c.pauseSpeedTicks = v),
            slider("reanimated.opt.pause_distance", 0, 80, c.pauseDistance, " px", v -> c.pauseDistance = v.floatValue()),
            easing("reanimated.opt.pause_easing", c.pauseEasing, v -> c.pauseEasing = v)
        );

        section("reanimated.section.containers");
        this.body.addAll(
            toggle("reanimated.opt.container_enabled", c.containerEnabled, v -> c.containerEnabled = v),
            slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = v.floatValue()),
            easing("reanimated.opt.container_easing", c.containerEasing, v -> c.containerEasing = v)
        );

        section("reanimated.section.cursor");
        this.body.addAll(
            toggle("reanimated.opt.hover_enabled", c.hoverEnabled, v -> c.hoverEnabled = v),
            slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = v.floatValue()),
            slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = v.floatValue()),
            toggle("reanimated.opt.press_enabled", c.pressEnabled, v -> c.pressEnabled = v),
            slider("reanimated.opt.press_scale", 0.0, 0.3, c.pressScale, "", v -> c.pressScale = v.floatValue()),
            slider("reanimated.opt.press_duration", 0.05, 0.6, c.pressDuration, " s", v -> c.pressDuration = v.floatValue()),
            toggle("reanimated.opt.slot_enabled", c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v),
            slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = v.floatValue())
        );

        section("reanimated.section.logo");
        this.body.addAll(
            toggle("reanimated.opt.logo_enabled", c.logoEnabled, v -> c.logoEnabled = v),
            logoStyle("reanimated.opt.logo_style", c.logoStyle, v -> c.logoStyle = v),
            slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = v.floatValue()),
            easing("reanimated.opt.logo_easing", c.logoEasing, v -> c.logoEasing = v),
            intSlider("reanimated.opt.logo_letter_duration", 50, 1200, c.profileLogo.durationMs, " ms", v -> c.profileLogo.durationMs = v),
            intSlider("reanimated.opt.logo_letter_delay", 0, 300, c.profileLogo.cascadeDelayMs, " ms", v -> c.profileLogo.cascadeDelayMs = v),
            slider("reanimated.opt.logo_letter_offset", -60, 60, c.profileLogo.offsetY, " px", v -> c.profileLogo.offsetY = v.floatValue()),
            order("reanimated.opt.logo_letter_order", c.profileLogo.cascadeOrder, v -> c.profileLogo.cascadeOrder = v),
            easing("reanimated.opt.logo_letter_easing", c.profileLogo.easing, v -> c.profileLogo.easing = v)
        );

        section("reanimated.section.tabs");
        this.body.addAll(
            toggle("reanimated.opt.tabs_enabled", c.tabsEnabled, v -> c.tabsEnabled = v),
            intSlider("reanimated.opt.tabs_duration", 50, 1200, c.profileTabs.durationMs, " ms", v -> c.profileTabs.durationMs = v),
            intSlider("reanimated.opt.tabs_delay", 0, 300, c.profileTabs.cascadeDelayMs, " ms", v -> c.profileTabs.cascadeDelayMs = v),
            slider("reanimated.opt.tabs_offset", -120, 120, c.profileTabs.offsetY, " px", v -> c.profileTabs.offsetY = v.floatValue()),
            easing("reanimated.opt.tabs_easing", c.profileTabs.easing, v -> c.profileTabs.easing = v)
        );
    }

    private void section(String key) {
        net.minecraft.client.gui.widget.TextWidget t = new net.minecraft.client.gui.widget.TextWidget(
            Text.translatable(key).formatted(net.minecraft.util.Formatting.GOLD), this.textRenderer);
        this.body.addWidgetEntry(t, null);
    }

    private SimpleOption<Boolean> toggle(String key, boolean current, Consumer<Boolean> setter) {
        return SimpleOption.ofBoolean(key, current, v -> {
            setter.accept(v);
            ReAnimatedConfig.get().save();
        });
    }

    private SimpleOption<Double> slider(String key, double min, double max, float current, String unit, Consumer<Double> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + String.format("%.2f", value) + unit)),
            SimpleOption.DoubleSliderCallbacks.INSTANCE.withModifier(
                d -> min + d * (max - min),
                v -> (v - min) / (max - min)
            ),
            (double) current,
            v -> {
                setter.accept(v);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<LogoStyle> logoStyle(String key, LogoStyle current, Consumer<LogoStyle> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + value.display)),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                java.util.Arrays.asList(LogoStyle.values()),
                com.mojang.serialization.Codec.INT.xmap(i -> LogoStyle.values()[i], LogoStyle::ordinal)),
            current,
            value -> {
                setter.accept(value);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<CascadeOrder> order(String key, CascadeOrder current, Consumer<CascadeOrder> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + value.display)),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                java.util.Arrays.asList(CascadeOrder.values()),
                com.mojang.serialization.Codec.INT.xmap(i -> CascadeOrder.values()[i], CascadeOrder::ordinal)),
            current,
            value -> {
                setter.accept(value);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<Integer> intSlider(String key, int min, int max, int current, String unit, Consumer<Integer> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + value + unit)),
            new SimpleOption.ValidatingIntSliderCallbacks(min, max),
            current,
            v -> {
                setter.accept(v);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<UiPreset> preset(String key, UiPreset current, Consumer<UiPreset> setter) {
        return presetOption(key, UiPreset.MAIN, current, setter);
    }

    private SimpleOption<UiPreset> pausePreset(String key, UiPreset current, Consumer<UiPreset> setter) {
        return presetOption(key, UiPreset.values(), current, setter);
    }

    private SimpleOption<UiPreset> presetOption(String key, UiPreset[] values, UiPreset current, Consumer<UiPreset> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.literal(value.display),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                java.util.Arrays.asList(values),
                com.mojang.serialization.Codec.INT.xmap(i -> UiPreset.values()[i], UiPreset::ordinal)),
            current,
            value -> {
                setter.accept(value);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<Integer> ticks(String key, int min, int max, int current, Consumer<Integer> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + value + " t")),
            new SimpleOption.ValidatingIntSliderCallbacks(min, max),
            current,
            v -> {
                setter.accept(v);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<Boolean> scope(String key, boolean current, Consumer<Boolean> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            // CyclingButtonWidget already prefixes the option name; returning it again
            // rendered the button as "Animate: Animate: All UI".
            (optionText, value) -> Text.translatable(
                value ? "reanimated.opt.animate_scope.all" : "reanimated.opt.animate_scope.vanilla"),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                java.util.Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                com.mojang.serialization.Codec.BOOL),
            current,
            value -> {
                setter.accept(value);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<Integer> easing(String key, EasingType current, Consumer<EasingType> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + EasingType.values()[value].display)),
            new SimpleOption.ValidatingIntSliderCallbacks(0, EasingType.values().length - 1),
            current.ordinal(),
            v -> {
                setter.accept(EasingType.values()[v]);
                ReAnimatedConfig.get().save();
            }
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        boolean overCredit = reanimated$overCredit(mouseX, mouseY);
        context.drawTextWithShadow(this.textRenderer, CREDIT, 4, this.height - 12, overCredit ? 0xFF88CCFF : 0xFF5599DD);
        if (this.notice != null) {
            if (System.currentTimeMillis() - this.noticeTime > NOTICE_MS) {
                this.notice = null;
            } else {
                context.drawCenteredTextWithShadow(this.textRenderer, this.notice,
                    this.width / 2, this.height - 12, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (click.button() == 0 && reanimated$overCredit((int) click.x(), (int) click.y())) {
            Util.getOperatingSystem().open(URI.create(AUTHOR_URL));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean reanimated$overCredit(int mouseX, int mouseY) {
        int w = this.textRenderer.getWidth(CREDIT);
        return mouseX >= 4 && mouseX <= 4 + w && mouseY >= this.height - 13 && mouseY <= this.height - 1;
    }
}
