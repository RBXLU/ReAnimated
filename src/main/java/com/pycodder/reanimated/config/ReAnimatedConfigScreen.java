package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.api.CustomPreset;
import com.pycodder.reanimated.api.ReAnimatedApi;
import com.pycodder.reanimated.api.SettingsEntry;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.TransitionStyle;
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
    private static final Text SCREENS = Text.translatable("reanimated.opt.screens");

    /** Сообщение о результате обмена настройками и время его показа. */
    private Text notice = null;
    private long noticeTime = 0L;
    private static final long NOTICE_MS = 4000L;

    /** Состав списка изменился — пересобрать его в начале следующего кадра. */
    private boolean reanimated$rebuild = false;

    public ReAnimatedConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("reanimated.config.title"));
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

        // Порядок секций — по тому, как часто в них заглядывают: сверху то, что меняют
        // чаще всего, — общий вид анимации и список «что анимировать вообще», — а
        // подробные слайдеры каждого слоя разворачиваются переключателем в конце списка
        // (он же помнится в конфиге). Иначе пресет и скорость пришлось бы искать среди
        // шести десятков слайдеров тонкой настройки.
        section("reanimated.section.main");
        this.body.addAll(
            pvp(c),
            preset("reanimated.opt.preset", c.uiPreset, v -> c.uiPreset = v),
            ticks("reanimated.opt.speed_ticks", 1, 40, c.animationSpeedTicks, v -> c.animationSpeedTicks = v),
            scope("reanimated.opt.animate_scope", c.animateModdedScreens, v -> c.animateModdedScreens = v),
            toggle("reanimated.opt.close_enabled", c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v),
            toggle("reanimated.opt.bg_fade", c.bgFadeEnabled, v -> c.bgFadeEnabled = v)
        );

        // Один переключатель на слой: включить или выключить целиком, без цифр.
        // Именно за этим сюда заходят чаще всего — «убрать анимацию подсказок».
        section("reanimated.section.enable");
        this.body.addAll(
            toggle("reanimated.opt.screen_enabled", c.screenOpenEnabled, v -> c.screenOpenEnabled = v),
            toggle("reanimated.opt.pause_enabled", c.pauseEnabled, v -> c.pauseEnabled = v),
            toggle("reanimated.opt.container_enabled", c.containerEnabled, v -> c.containerEnabled = v),
            toggle("reanimated.opt.transition_enabled", c.transitionEnabled, v -> c.transitionEnabled = v),
            toggle("reanimated.opt.lists_enabled", c.listsEnabled, v -> c.listsEnabled = v),
            toggle("reanimated.opt.recipebook_enabled", c.recipeBookEnabled, v -> c.recipeBookEnabled = v),
            toggle("reanimated.opt.tooltip_enabled", c.tooltipEnabled, v -> c.tooltipEnabled = v),
            toggle("reanimated.opt.toast_enabled", c.toastEnabled, v -> c.toastEnabled = v),
            toggle("reanimated.opt.hover_enabled", c.hoverEnabled, v -> c.hoverEnabled = v),
            toggle("reanimated.opt.press_enabled", c.pressEnabled, v -> c.pressEnabled = v),
            toggle("reanimated.opt.slot_enabled", c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v),
            toggle("reanimated.opt.widgets_enabled", c.widgetsEnabled, v -> c.widgetsEnabled = v),
            toggle("reanimated.opt.hotbar_enabled", c.hotbarEnabled, v -> c.hotbarEnabled = v),
            toggle("reanimated.opt.hotbar_name_enabled", c.hotbarItemNameEnabled, v -> c.hotbarItemNameEnabled = v),
            toggle("reanimated.opt.logo_enabled", c.logoEnabled, v -> c.logoEnabled = v),
            toggle("reanimated.opt.tabs_enabled", c.tabsEnabled, v -> c.tabsEnabled = v),
            toggle("reanimated.opt.tab_switch_enabled", c.tabSwitchEnabled, v -> c.tabSwitchEnabled = v)
        );

        // Инструменты — секцией самого списка, а не кнопками по углам экрана: на GUI
        // scale 3–4 ширина падает ниже 558 px, и угловые кнопки наезжали бы на строки
        // настроек. В списке они прокручиваются вместе со всем остальным.
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
            ButtonWidget.builder(SCREENS, b -> this.client.setScreen(new ScreenRulesScreen(this)))
                .dimensions(0, 0, 150, 20).build(),
            ButtonWidget.builder(TESTED, b -> Util.getOperatingSystem().open(URI.create(TESTED_URL)))
                .dimensions(0, 0, 150, 20).build());

        reanimated$extensions();

        section("reanimated.section.details");
        this.body.addAll(expand("reanimated.opt.advanced", c.advancedSettings, v -> c.advancedSettings = v));
        if (!c.advancedSettings) {
            return; // всё ниже — тонкая настройка, её попросили не показывать
        }

        section("reanimated.section.menu");
        this.body.addAll(
            slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = v.floatValue()),
            easing("reanimated.opt.screen_easing", c.screenOpenEasing, v -> c.screenOpenEasing = v)
        );

        section("reanimated.section.pause");
        this.body.addAll(
            pausePreset("reanimated.opt.pause_preset", c.pausePreset, v -> c.pausePreset = v),
            ticks("reanimated.opt.pause_speed_ticks", 1, 40, c.pauseSpeedTicks, v -> c.pauseSpeedTicks = v),
            slider("reanimated.opt.pause_distance", 0, 80, c.pauseDistance, " px", v -> c.pauseDistance = v.floatValue()),
            easing("reanimated.opt.pause_easing", c.pauseEasing, v -> c.pauseEasing = v)
        );

        section("reanimated.section.containers");
        this.body.addAll(
            slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = v.floatValue()),
            easing("reanimated.opt.container_easing", c.containerEasing, v -> c.containerEasing = v)
        );

        section("reanimated.section.transitions");
        this.body.addAll(
            transitionStyle("reanimated.opt.transition_style", c.transitionStyle, v -> c.transitionStyle = v),
            ticks("reanimated.opt.transition_speed", 1, 20, c.transitionSpeedTicks, v -> c.transitionSpeedTicks = v),
            slider("reanimated.opt.transition_distance", 0, 120, c.transitionDistance, " px", v -> c.transitionDistance = v.floatValue()),
            easing("reanimated.opt.transition_easing", c.transitionEasing, v -> c.transitionEasing = v)
        );

        section("reanimated.section.popups");
        this.body.addAll(
            intSlider("reanimated.opt.recipebook_duration", 30, 500, c.recipeBookDurationMs, " ms", v -> c.recipeBookDurationMs = v),
            slider("reanimated.opt.recipebook_scale", 0.5, 1.0, c.recipeBookScale, "", v -> c.recipeBookScale = v.floatValue()),
            slider("reanimated.opt.recipebook_offset", 0, 40, c.recipeBookOffsetX, " px", v -> c.recipeBookOffsetX = v.floatValue()),
            easing("reanimated.opt.recipebook_easing", c.recipeBookEasing, v -> c.recipeBookEasing = v),
            intSlider("reanimated.opt.tooltip_duration", 30, 500, c.tooltipDurationMs, " ms", v -> c.tooltipDurationMs = v),
            slider("reanimated.opt.tooltip_scale", 0.5, 1.0, c.tooltipScale, "", v -> c.tooltipScale = v.floatValue()),
            slider("reanimated.opt.tooltip_offset", -20, 20, c.tooltipOffsetY, " px", v -> c.tooltipOffsetY = v.floatValue()),
            easing("reanimated.opt.tooltip_easing", c.tooltipEasing, v -> c.tooltipEasing = v),
            // Дольше ванильных 600 мс уведомление растянуть нельзя: мод переиспользует
            // ванильный таймер, а не заводит свой.
            intSlider("reanimated.opt.toast_duration", 100, 600, c.toastDurationMs, " ms", v -> c.toastDurationMs = v),
            easing("reanimated.opt.toast_easing", c.toastEasing, v -> c.toastEasing = v)
        );

        section("reanimated.section.cursor");
        this.body.addAll(
            slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = v.floatValue()),
            slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = v.floatValue()),
            slider("reanimated.opt.press_scale", 0.0, 0.3, c.pressScale, "", v -> c.pressScale = v.floatValue()),
            slider("reanimated.opt.press_duration", 0.05, 0.6, c.pressDuration, " s", v -> c.pressDuration = v.floatValue()),
            slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = v.floatValue()),
            slider("reanimated.opt.widget_speed", 4, 40, c.widgetSpeed, "", v -> c.widgetSpeed = v.floatValue())
        );

        section("reanimated.section.hud");
        this.body.addAll(
            slider("reanimated.opt.hotbar_speed", 4, 40, c.hotbarSpeed, "", v -> c.hotbarSpeed = v.floatValue())
        );

        section("reanimated.section.logo");
        this.body.addAll(
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
            intSlider("reanimated.opt.tabs_duration", 50, 1200, c.profileTabs.durationMs, " ms", v -> c.profileTabs.durationMs = v),
            intSlider("reanimated.opt.tabs_delay", 0, 300, c.profileTabs.cascadeDelayMs, " ms", v -> c.profileTabs.cascadeDelayMs = v),
            slider("reanimated.opt.tabs_offset", -120, 120, c.profileTabs.offsetY, " px", v -> c.profileTabs.offsetY = v.floatValue()),
            easing("reanimated.opt.tabs_easing", c.profileTabs.easing, v -> c.profileTabs.easing = v),
            intSlider("reanimated.opt.tab_switch_duration", 50, 600, c.tabSwitchDurationMs, " ms", v -> c.tabSwitchDurationMs = v),
            slider("reanimated.opt.tab_switch_distance", 0, 80, c.tabSwitchDistance, " px", v -> c.tabSwitchDistance = v.floatValue()),
            easing("reanimated.opt.tab_switch_easing", c.tabSwitchEasing, v -> c.tabSwitchEasing = v)
        );
    }

    /**
     * Переключатель, от которого зависит СОСТАВ списка. Пересобрать его прямо в
     * обработчике нельзя: клик в этот момент ещё идёт по тем самым виджетам, которые
     * мы заменим, — поэтому список перестраивается в начале следующего кадра.
     */
    private SimpleOption<Boolean> expand(String key, boolean current, Consumer<Boolean> setter) {
        return SimpleOption.ofBoolean(key, current, v -> {
            setter.accept(v);
            ReAnimatedConfig.get().save();
            this.reanimated$rebuild = true;
        });
    }

    /**
     * Секция расширений: анимации и кнопки, добавленные другими модами. Пока никто
     * ничего не добавил, секции нет вовсе — пустой заголовок в списке настроек только
     * сбивал бы с толку.
     */
    private void reanimated$extensions() {
        java.util.List<CustomPreset> presets = ReAnimatedApi.presets();
        java.util.List<SettingsEntry> entries = ReAnimatedApi.settingsEntries();
        if (presets.isEmpty() && entries.isEmpty()) return;

        section("reanimated.section.extensions");
        if (!presets.isEmpty()) {
            this.body.addAll(extensionPreset(presets));
        }
        for (int i = 0; i < entries.size(); i += 2) {
            SettingsEntry left = entries.get(i);
            SettingsEntry right = i + 1 < entries.size() ? entries.get(i + 1) : null;
            this.body.addWidgetEntry(extensionButton(left), right == null ? null : extensionButton(right));
        }
    }

    private ButtonWidget extensionButton(SettingsEntry entry) {
        return ButtonWidget.builder(Text.literal(entry.label()), b -> {
            ReAnimatedApi.openEntry(entry, this);
            // Кнопка расширения может не открывать экран, а что-то переключать — тогда
            // её надпись должна обновиться сразу. Пересобираем список, если расширение
            // не увело нас на свой экран.
            if (this.client != null && this.client.currentScreen == this) {
                this.clearAndInit();
            }
        }).dimensions(0, 0, 150, 20).build();
    }

    /**
     * Выбор анимации из расширения. Значение 0 — «не выбрана»: тогда работает обычный
     * пресет. Список берётся у API в момент открытия экрана, поэтому мод, добавивший
     * анимацию позже, появится здесь после переоткрытия настроек.
     */
    private SimpleOption<Integer> extensionPreset(java.util.List<CustomPreset> presets) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        int current = 0;
        if (c.uiPreset == UiPreset.CUSTOM) {
            for (int i = 0; i < presets.size(); i++) {
                if (presets.get(i).id().equals(c.customPresetId)) {
                    current = i + 1;
                    break;
                }
            }
        }
        return new SimpleOption<>(
            "reanimated.opt.custom_preset",
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable("reanimated.opt.custom_preset").append(Text.literal(": ")).append(
                value <= 0 || value > presets.size()
                    ? Text.translatable("reanimated.opt.custom_preset.off")
                    : Text.literal(presets.get(value - 1).displayName())),
            new SimpleOption.ValidatingIntSliderCallbacks(0, presets.size()),
            current,
            value -> {
                if (value <= 0 || value > presets.size()) {
                    // Снимаем выбор, но общий пресет трогаем, только если он и был чужим.
                    if (c.uiPreset == UiPreset.CUSTOM) c.uiPreset = UiPreset.DEFAULT;
                    c.customPresetId = "";
                } else {
                    c.uiPreset = UiPreset.CUSTOM;
                    c.customPresetId = presets.get(value - 1).id();
                }
                c.save();
            }
        );
    }

    /** Строка-заголовок секции в списке настроек (центрированный цветной текст, без взаимодействия). */
    private void section(String key) {
        net.minecraft.client.gui.widget.TextWidget t = new net.minecraft.client.gui.widget.TextWidget(
            Text.translatable(key).formatted(net.minecraft.util.Formatting.GOLD), this.textRenderer);
        this.body.addWidgetEntry(t, null);
    }

    /** PvP-режим — с подсказкой: по одному названию не понять, что именно он меняет. */
    private SimpleOption<Boolean> pvp(ReAnimatedConfig c) {
        return SimpleOption.ofBoolean("reanimated.opt.pvp_mode",
            SimpleOption.constantTooltip(Text.translatable("reanimated.tip.pvp_mode")),
            c.pvpMode, v -> {
                c.pvpMode = v;
                ReAnimatedConfig.get().save();
            });
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

    private SimpleOption<TransitionStyle> transitionStyle(String key, TransitionStyle current, Consumer<TransitionStyle> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + value.display)),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                java.util.Arrays.asList(TransitionStyle.values()),
                com.mojang.serialization.Codec.INT.xmap(i -> TransitionStyle.values()[i], TransitionStyle::ordinal)),
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
        if (this.reanimated$rebuild) {
            this.reanimated$rebuild = false;
            this.clearAndInit();
        }
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && reanimated$overCredit((int) mouseX, (int) mouseY)) {
            Util.getOperatingSystem().open(URI.create(AUTHOR_URL));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean reanimated$overCredit(int mouseX, int mouseY) {
        int w = this.textRenderer.getWidth(CREDIT);
        return mouseX >= 4 && mouseX <= 4 + w && mouseY >= this.height - 13 && mouseY <= this.height - 1;
    }
}
