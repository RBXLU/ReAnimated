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

/** Экран настроек мода: слайдеры/переключатели для каждой анимации + ссылка на автора. */
public class ReAnimatedConfigScreen extends GameOptionsScreen {

    private static final String AUTHOR_URL = "https://modrinth.com/user/pycodder";
    private static final String TESTED_URL = "https://github.com/RBXLU/ReAnimated/blob/main/testedmods.txt";
    private static final Text CREDIT = Text.literal("mod by @pycodder");
    private static final Text TESTED = Text.translatable("reanimated.opt.tested_mods");
    private static final Text EDITOR = Text.translatable("reanimated.opt.profile_editor");
    private static final Text STUDIO = Text.translatable("reanimated.opt.studio");
    private static final Text EXPORT = Text.translatable("reanimated.opt.export");
    private static final Text IMPORT = Text.translatable("reanimated.opt.import");

    /** Сообщение о результате обмена настройками и время его показа. */
    private Text notice = null;
    private long noticeTime = 0L;
    private static final long NOTICE_MS = 4000L;

    public ReAnimatedConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("reanimated.config.title"));
    }

    @Override
    protected void init() {
        super.init();
        // Настоящая кнопка (а не кликабельный текст) — гарантированно ловит клик и открывает список.
        this.addDrawableChild(ButtonWidget.builder(TESTED,
                        b -> Util.getOperatingSystem().open(URI.create(TESTED_URL)))
                .dimensions(4, 4, 120, 20).build());
        this.addDrawableChild(ButtonWidget.builder(EDITOR,
                        b -> this.client.setScreen(new AnimProfileEditorScreen(this)))
                .dimensions(this.width - 124, 4, 120, 20).build());
        this.addDrawableChild(ButtonWidget.builder(STUDIO,
                        b -> this.client.setScreen(new AnimationStudioScreen(this)))
                .dimensions(this.width - 124, 28, 120, 20).build());
        this.addDrawableChild(ButtonWidget.builder(EXPORT, b -> this.exportSettings())
                .dimensions(4, 28, 58, 20).build());
        this.addDrawableChild(ButtonWidget.builder(IMPORT, b -> this.importSettings())
                .dimensions(66, 28, 58, 20).build());
    }

    /** Кладёт все настройки в буфер обмена одной строкой — можно передать другому игроку. */
    private void exportSettings() {
        this.client.keyboard.setClipboard(ReAnimatedConfig.get().toShareString());
        this.showNotice("reanimated.opt.export.done", false);
    }

    /**
     * Забирает настройки из буфера обмена. Экран после этого пересобирается: слайдеры
     * читают значения в момент создания, иначе они показывали бы старые.
     */
    private void importSettings() {
        String text = this.client.keyboard.getClipboard();
        if (ReAnimatedConfig.applyShareString(text)) {
            this.showNotice("reanimated.opt.import.done", false);
            Text saved = this.notice;
            long savedTime = this.noticeTime;
            this.clearAndInit();
            this.notice = saved;       // clearAndInit() создаёт опции заново, сообщение переживает это
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

        section("reanimated.section.general");
        this.body.addAll(
            preset("reanimated.opt.preset", c.uiPreset, v -> c.uiPreset = v),
            ticks("reanimated.opt.speed_ticks", 1, 40, c.animationSpeedTicks, v -> c.animationSpeedTicks = v),
            scope("reanimated.opt.animate_scope", c.animateModdedScreens, v -> c.animateModdedScreens = v),
            toggle("reanimated.opt.close_enabled", c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v),
            toggle("reanimated.opt.bg_fade", c.bgFadeEnabled, v -> c.bgFadeEnabled = v)
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
            // Стиль GROW — родная анимация "вырастания"
            slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = v.floatValue()),
            easing("reanimated.opt.logo_easing", c.logoEasing, v -> c.logoEasing = v),
            // Стиль LETTERS — побуквенный каскад
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

    /** Строка-заголовок секции в списке настроек (центрированный цветной текст, без взаимодействия). */
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

    /** Слайдер целочисленного значения с произвольной единицей. */
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
        // Общему пресету нечего наследовать — INHERIT в списке не показываем.
        return presetOption(key, UiPreset.MAIN, current, setter);
    }

    /** Пресет меню паузы: то же самое плюс вариант «как общий» (INHERIT). */
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

    /** Слайдер целочисленной скорости в тиках. */
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

    /** Переключатель "Весь интерфейс / Только ванильные". true = анимировать и моды. */
    private SimpleOption<Boolean> scope(String key, boolean current, Consumer<Boolean> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": ")).append(
                Text.translatable(value ? "reanimated.opt.animate_scope.all" : "reanimated.opt.animate_scope.vanilla")),
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
