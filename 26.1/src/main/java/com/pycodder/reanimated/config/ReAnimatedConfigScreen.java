package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.UiPreset;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * Экран настроек мода (Minecraft 26.x). В 26.x новая render-state система и у Screen
 * нет render(GuiGraphics) — поэтому рендер не переопределяем (заголовок не рисуем),
 * виджеты сами отрисовываются через addRenderableWidget. Только стабильные примитивы
 * (Button + свой слайдер), без OptionInstance/OptionsList и без mouseClicked.
 */
public class ReAnimatedConfigScreen extends Screen {

    private static final String AUTHOR_URL = "https://modrinth.com/user/pycodder";
    private static final String TESTED_URL = "https://github.com/RBXLU/ReAnimated/blob/main/testedmods.txt";
    private final Screen parent;
    /** Строка с результатом обмена настройками; создаётся в init(). */
    private StringWidget notice;

    public ReAnimatedConfigScreen(Screen parent) {
        super(Component.translatable("reanimated.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        // Элементы списка: String — заголовок секции (своя строка во всю ширину),
        // AbstractWidget — обычная опция (половина ширины, по две в строке).
        List<Object> rows = new ArrayList<>();

        rows.add("reanimated.section.general");
        rows.add(preset("reanimated.opt.preset", () -> c.uiPreset, v -> c.uiPreset = v));
        rows.add(ticksSlider("reanimated.opt.speed_ticks", 1, 40, c.animationSpeedTicks, v -> c.animationSpeedTicks = v));
        rows.add(scope("reanimated.opt.animate_scope", () -> c.animateModdedScreens, v -> c.animateModdedScreens = v));
        rows.add(toggle("reanimated.opt.close_enabled", () -> c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v));
        rows.add(toggle("reanimated.opt.bg_fade", () -> c.bgFadeEnabled, v -> c.bgFadeEnabled = v));

        rows.add("reanimated.section.menu");
        rows.add(toggle("reanimated.opt.screen_enabled", () -> c.screenOpenEnabled, v -> c.screenOpenEnabled = v));
        rows.add(slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = (float) v));
        rows.add(easing("reanimated.opt.screen_easing", () -> c.screenOpenEasing, v -> c.screenOpenEasing = v));

        rows.add("reanimated.section.pause");
        rows.add(toggle("reanimated.opt.pause_enabled", () -> c.pauseEnabled, v -> c.pauseEnabled = v));
        rows.add(pausePreset("reanimated.opt.pause_preset", () -> c.pausePreset, v -> c.pausePreset = v));
        rows.add(ticksSlider("reanimated.opt.pause_speed_ticks", 1, 40, c.pauseSpeedTicks, v -> c.pauseSpeedTicks = v));
        rows.add(slider("reanimated.opt.pause_distance", 0, 80, c.pauseDistance, " px", v -> c.pauseDistance = (float) v));
        rows.add(easing("reanimated.opt.pause_easing", () -> c.pauseEasing, v -> c.pauseEasing = v));

        rows.add("reanimated.section.containers");
        rows.add(toggle("reanimated.opt.container_enabled", () -> c.containerEnabled, v -> c.containerEnabled = v));
        rows.add(slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = (float) v));
        rows.add(easing("reanimated.opt.container_easing", () -> c.containerEasing, v -> c.containerEasing = v));

        rows.add("reanimated.section.cursor");
        rows.add(toggle("reanimated.opt.hover_enabled", () -> c.hoverEnabled, v -> c.hoverEnabled = v));
        rows.add(slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = (float) v));
        rows.add(slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = (float) v));
        rows.add(toggle("reanimated.opt.press_enabled", () -> c.pressEnabled, v -> c.pressEnabled = v));
        rows.add(slider("reanimated.opt.press_scale", 0.0, 0.3, c.pressScale, "", v -> c.pressScale = (float) v));
        rows.add(slider("reanimated.opt.press_duration", 0.05, 0.6, c.pressDuration, " s", v -> c.pressDuration = (float) v));
        rows.add(toggle("reanimated.opt.slot_enabled", () -> c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v));
        rows.add(slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = (float) v));

        rows.add("reanimated.section.logo");
        rows.add(toggle("reanimated.opt.logo_enabled", () -> c.logoEnabled, v -> c.logoEnabled = v));
        rows.add(logoStyle("reanimated.opt.logo_style", () -> c.logoStyle, v -> c.logoStyle = v));
        rows.add(slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = (float) v));
        rows.add(easing("reanimated.opt.logo_easing", () -> c.logoEasing, v -> c.logoEasing = v));
        rows.add(intSlider("reanimated.opt.logo_letter_duration", 50, 1200, c.profileLogo.durationMs, " ms", v -> c.profileLogo.durationMs = v));
        rows.add(intSlider("reanimated.opt.logo_letter_delay", 0, 300, c.profileLogo.cascadeDelayMs, " ms", v -> c.profileLogo.cascadeDelayMs = v));
        rows.add(slider("reanimated.opt.logo_letter_offset", -60, 60, c.profileLogo.offsetY, " px", v -> c.profileLogo.offsetY = (float) v));
        rows.add(order("reanimated.opt.logo_letter_order", () -> c.profileLogo.cascadeOrder, v -> c.profileLogo.cascadeOrder = v));
        rows.add(easing("reanimated.opt.logo_letter_easing", () -> c.profileLogo.easing, v -> c.profileLogo.easing = v));

        rows.add("reanimated.section.tabs");
        rows.add(toggle("reanimated.opt.tabs_enabled", () -> c.tabsEnabled, v -> c.tabsEnabled = v));
        rows.add(intSlider("reanimated.opt.tabs_duration", 50, 1200, c.profileTabs.durationMs, " ms", v -> c.profileTabs.durationMs = v));
        rows.add(intSlider("reanimated.opt.tabs_delay", 0, 300, c.profileTabs.cascadeDelayMs, " ms", v -> c.profileTabs.cascadeDelayMs = v));
        rows.add(slider("reanimated.opt.tabs_offset", -120, 120, c.profileTabs.offsetY, " px", v -> c.profileTabs.offsetY = (float) v));
        rows.add(easing("reanimated.opt.tabs_easing", () -> c.profileTabs.easing, v -> c.profileTabs.easing = v));

        int colW = 158, gap = 12, rowH = 20, vgap = 2, headerH = 13;
        int totalW = colW * 2 + gap;
        int startX = (this.width - totalW) / 2;
        int y = 30;
        int col = 0;
        for (Object item : rows) {
            if (item instanceof String key) {
                if (col != 0) {          // недобранную строку закрываем перед заголовком
                    y += rowH + vgap;
                    col = 0;
                }
                StringWidget header = new StringWidget(
                        Component.translatable(key).withStyle(ChatFormatting.GOLD), this.font);
                header.setWidth(totalW);
                header.setX(startX);
                header.setY(y);
                addRenderableWidget(header);
                y += headerH;
                continue;
            }
            AbstractWidget w = (AbstractWidget) item;
            w.setWidth(colW);
            w.setX(startX + col * (colW + gap));
            w.setY(y);
            addRenderableWidget(w);
            if (++col == 2) {
                col = 0;
                y += rowH + vgap;
            }
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("mod by @pycodder"),
                        b -> ConfirmLinkScreen.confirmLinkNow(this, AUTHOR_URL))
                .bounds(4, this.height - 24, 120, 16).build());

        addRenderableWidget(Button.builder(Component.translatable("reanimated.opt.tested_mods"),
                        b -> ConfirmLinkScreen.confirmLinkNow(this, TESTED_URL))
                .bounds(4, 4, 130, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.opt.profile_editor"),
                        b -> this.minecraft.setScreen(new AnimProfileEditorScreen(this)))
                .bounds(this.width - 134, 4, 130, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.opt.studio"),
                        b -> this.minecraft.setScreen(new AnimationStudioScreen(this)))
                .bounds(this.width - 134, 28, 130, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.opt.export"),
                        b -> this.exportSettings())
                .bounds(4, 28, 63, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.opt.import"),
                        b -> this.importSettings())
                .bounds(71, 28, 63, 20).build());
        this.notice = new StringWidget(Component.empty(), this.font);
        this.notice.setWidth(this.width - 8);
        this.notice.setX(4);
        this.notice.setY(this.height - 44);
        addRenderableWidget(this.notice);
    }

    /** Кладёт все настройки в буфер обмена одной строкой — можно передать другому игроку. */
    private void exportSettings() {
        this.minecraft.keyboardHandler.setClipboard(ReAnimatedConfig.get().toShareString());
        this.showNotice("reanimated.opt.export.done", false);
    }

    /**
     * Забирает настройки из буфера обмена. Экран после этого пересобирается: кнопки и
     * слайдеры читают значения в момент создания, иначе показывали бы старые.
     */
    private void importSettings() {
        String text = this.minecraft.keyboardHandler.getClipboard();
        if (ReAnimatedConfig.applyShareString(text)) {
            this.rebuildWidgets();
            this.showNotice("reanimated.opt.import.done", false);
        } else {
            this.showNotice("reanimated.opt.import.failed", true);
        }
    }

    /**
     * Показывает результат обмена настройками. В 26.x у экрана нет render(GuiGraphics),
     * поэтому сообщение живёт отдельным виджетом и висит до закрытия экрана, а не гаснет
     * по таймеру.
     */
    private void showNotice(String key, boolean error) {
        if (this.notice != null) {
            this.notice.setMessage(Component.translatable(key)
                .withStyle(error ? ChatFormatting.RED : ChatFormatting.GREEN));
        }
    }

    private Button toggle(String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(boolLabel(key, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            ReAnimatedConfig.get().save();
            b.setMessage(boolLabel(key, nv));
        }).bounds(0, 0, 158, 20).build();
    }

    private Button preset(String key, Supplier<UiPreset> get, Consumer<UiPreset> set) {
        // Общему пресету нечего наследовать — INHERIT в переборе не участвует.
        return presetOption(key, UiPreset.MAIN, get, set);
    }

    /** Пресет меню паузы: те же варианты плюс «как общий» (INHERIT). */
    private Button pausePreset(String key, Supplier<UiPreset> get, Consumer<UiPreset> set) {
        return presetOption(key, UiPreset.values(), get, set);
    }

    private Button presetOption(String key, UiPreset[] values, Supplier<UiPreset> get, Consumer<UiPreset> set) {
        return Button.builder(presetLabel(key, get.get()), b -> {
            int i = 0;
            for (int k = 0; k < values.length; k++) {
                if (values[k] == get.get()) i = k;
            }
            UiPreset nx = values[(i + 1) % values.length];
            set.accept(nx);
            ReAnimatedConfig.get().save();
            b.setMessage(presetLabel(key, nx));
        }).bounds(0, 0, 158, 20).build();
    }

    private Button logoStyle(String key, Supplier<LogoStyle> get, Consumer<LogoStyle> set) {
        return Button.builder(logoStyleLabel(key, get.get()), b -> {
            LogoStyle nx = LogoStyle.values()[(get.get().ordinal() + 1) % LogoStyle.values().length];
            set.accept(nx);
            ReAnimatedConfig.get().save();
            b.setMessage(logoStyleLabel(key, nx));
        }).bounds(0, 0, 158, 20).build();
    }

    private Button order(String key, Supplier<CascadeOrder> get, Consumer<CascadeOrder> set) {
        return Button.builder(orderLabel(key, get.get()), b -> {
            CascadeOrder nx = CascadeOrder.values()[(get.get().ordinal() + 1) % CascadeOrder.values().length];
            set.accept(nx);
            ReAnimatedConfig.get().save();
            b.setMessage(orderLabel(key, nx));
        }).bounds(0, 0, 158, 20).build();
    }

    private AbstractWidget intSlider(String key, int min, int max, int current, String unit, java.util.function.IntConsumer setter) {
        return new IntConfigSlider(key, min, max, current, unit, setter);
    }

    private Button easing(String key, Supplier<EasingType> get, Consumer<EasingType> set) {
        return Button.builder(easingLabel(key, get.get()), b -> {
            EasingType nx = EasingType.values()[(get.get().ordinal() + 1) % EasingType.values().length];
            set.accept(nx);
            ReAnimatedConfig.get().save();
            b.setMessage(easingLabel(key, nx));
        }).bounds(0, 0, 158, 20).build();
    }

    private AbstractWidget slider(String key, double min, double max, double current, String unit, DoubleConsumer setter) {
        return new ConfigSlider(key, min, max, current, unit, setter);
    }

    private AbstractWidget ticksSlider(String key, int min, int max, int current, java.util.function.IntConsumer setter) {
        return new IntConfigSlider(key, min, max, current, " t", setter);
    }

    private Button scope(String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(scopeLabel(key, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            ReAnimatedConfig.get().save();
            b.setMessage(scopeLabel(key, nv));
        }).bounds(0, 0, 158, 20).build();
    }

    private static Component scopeLabel(String key, boolean all) {
        return Component.translatable(key).append(Component.literal(": ")).append(
            Component.translatable(all ? "reanimated.opt.animate_scope.all" : "reanimated.opt.animate_scope.vanilla"));
    }

    private static Component boolLabel(String key, boolean on) {
        return Component.translatable(key).append(Component.literal(": " + (on ? "ON" : "OFF")));
    }

    private static Component easingLabel(String key, EasingType e) {
        return Component.translatable(key).append(Component.literal(": " + e.display));
    }

    private static Component presetLabel(String key, UiPreset p) {
        return Component.translatable(key).append(Component.literal(": " + p.display));
    }

    private static Component logoStyleLabel(String key, LogoStyle s) {
        return Component.translatable(key).append(Component.literal(": " + s.display));
    }

    private static Component orderLabel(String key, CascadeOrder o) {
        return Component.translatable(key).append(Component.literal(": " + o.display));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    /** Слайдер на стабильном AbstractSliderButton (value: 0..1). */
    private static class ConfigSlider extends AbstractSliderButton {
        private final String key;
        private final double min;
        private final double max;
        private final String unit;
        private final DoubleConsumer setter;

        ConfigSlider(String key, double min, double max, double current, String unit, DoubleConsumer setter) {
            super(0, 0, 158, 20, Component.empty(), (current - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + this.value * (max - min);
            setMessage(Component.translatable(key).append(Component.literal(": " + String.format("%.2f", val) + unit)));
        }

        @Override
        protected void applyValue() {
            double val = min + this.value * (max - min);
            setter.accept(val);
            ReAnimatedConfig.get().save();
        }
    }

    /** Целочисленный слайдер (тики). value: 0..1 -> [min,max]. */
    private static class IntConfigSlider extends AbstractSliderButton {
        private final String key;
        private final int min;
        private final int max;
        private final String unit;
        private final java.util.function.IntConsumer setter;

        IntConfigSlider(String key, int min, int max, int current, String unit, java.util.function.IntConsumer setter) {
            super(0, 0, 158, 20, Component.empty(), (double) (current - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.setter = setter;
            updateMessage();
        }

        private int intVal() {
            return (int) Math.round(min + this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(key).append(Component.literal(": " + intVal() + unit)));
        }

        @Override
        protected void applyValue() {
            setter.accept(intVal());
            ReAnimatedConfig.get().save();
        }
    }
}
