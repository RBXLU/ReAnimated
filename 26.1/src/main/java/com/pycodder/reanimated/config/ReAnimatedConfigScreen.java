package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.UiPreset;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
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

    public ReAnimatedConfigScreen(Screen parent) {
        super(Component.translatable("reanimated.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        List<AbstractWidget> rows = new ArrayList<>();

        rows.add(preset("reanimated.opt.preset", () -> c.uiPreset, v -> c.uiPreset = v));
        rows.add(ticksSlider("reanimated.opt.speed_ticks", 1, 40, c.animationSpeedTicks, v -> c.animationSpeedTicks = v));
        rows.add(scope("reanimated.opt.animate_scope", () -> c.animateModdedScreens, v -> c.animateModdedScreens = v));
        rows.add(toggle("reanimated.opt.close_enabled", () -> c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v));
        rows.add(toggle("reanimated.opt.screen_enabled", () -> c.screenOpenEnabled, v -> c.screenOpenEnabled = v));
        rows.add(slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = (float) v));
        rows.add(easing("reanimated.opt.screen_easing", () -> c.screenOpenEasing, v -> c.screenOpenEasing = v));

        rows.add(toggle("reanimated.opt.container_enabled", () -> c.containerEnabled, v -> c.containerEnabled = v));
        rows.add(slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = (float) v));
        rows.add(easing("reanimated.opt.container_easing", () -> c.containerEasing, v -> c.containerEasing = v));

        rows.add(toggle("reanimated.opt.hover_enabled", () -> c.hoverEnabled, v -> c.hoverEnabled = v));
        rows.add(slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = (float) v));
        rows.add(slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = (float) v));

        rows.add(toggle("reanimated.opt.slot_enabled", () -> c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v));
        rows.add(slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = (float) v));

        rows.add(toggle("reanimated.opt.logo_enabled", () -> c.logoEnabled, v -> c.logoEnabled = v));
        rows.add(slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = (float) v));
        rows.add(easing("reanimated.opt.logo_easing", () -> c.logoEasing, v -> c.logoEasing = v));

        int colW = 158, gap = 12, rowH = 20, vgap = 3;
        int totalW = colW * 2 + gap;
        int startX = (this.width - totalW) / 2;
        int startY = 32;
        for (int i = 0; i < rows.size(); i++) {
            int col = i % 2;
            int r = i / 2;
            AbstractWidget w = rows.get(i);
            w.setWidth(colW);
            w.setX(startX + col * (colW + gap));
            w.setY(startY + r * (rowH + vgap));
            addRenderableWidget(w);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("mod by @pycodder"),
                        b -> ConfirmLinkScreen.confirmLinkNow(this, AUTHOR_URL))
                .bounds(4, this.height - 24, 120, 16).build());

        addRenderableWidget(Button.builder(Component.translatable("reanimated.opt.tested_mods"),
                        b -> ConfirmLinkScreen.confirmLinkNow(this, TESTED_URL))
                .bounds(4, 4, 130, 20).build());
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
        return Button.builder(presetLabel(key, get.get()), b -> {
            UiPreset nx = UiPreset.values()[(get.get().ordinal() + 1) % UiPreset.values().length];
            set.accept(nx);
            ReAnimatedConfig.get().save();
            b.setMessage(presetLabel(key, nx));
        }).bounds(0, 0, 158, 20).build();
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
        return new IntConfigSlider(key, min, max, current, setter);
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
        private final java.util.function.IntConsumer setter;

        IntConfigSlider(String key, int min, int max, int current, java.util.function.IntConsumer setter) {
            super(0, 0, 158, 20, Component.empty(), (double) (current - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        private int intVal() {
            return (int) Math.round(min + this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(key).append(Component.literal(": " + intVal() + " t")));
        }

        @Override
        protected void applyValue() {
            setter.accept(intVal());
            ReAnimatedConfig.get().save();
        }
    }
}
