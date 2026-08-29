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
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Mod settings screen (Minecraft 26.x). */
public class ReAnimatedConfigScreen extends Screen {
    private static final String AUTHOR_URL = "https://modrinth.com/user/pycodder";
    private static final String TESTED_URL = "https://github.com/RBXLU/ReAnimated/blob/main/testedmods.txt";

    private static final String[] TABS = {
        "reanimated.tab.general",
        "reanimated.tab.menu",
        "reanimated.tab.pause",
        "reanimated.tab.containers",
        "reanimated.tab.cursor",
        "reanimated.tab.logo",
        "reanimated.tab.tabs",
    };

    private final Screen parent;
    private StringWidget notice;
    private static int section = 0;

    public ReAnimatedConfigScreen(Screen parent) {
        super(Component.translatable("reanimated.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ReAnimatedConfig c = ReAnimatedConfig.get();

        Component author = Component.literal("mod by @pycodder");
        Component tested = Component.translatable("reanimated.opt.tested_mods");
        int authorW = this.font.width(author) + 10;
        int testedW = this.font.width(tested) + 10;
        addRenderableWidget(Button.builder(author,
                        b -> ConfirmLinkScreen.confirmLinkNow(this, AUTHOR_URL))
                .bounds(4, 4, authorW, 16).build());
        addRenderableWidget(Button.builder(tested,
                        b -> ConfirmLinkScreen.confirmLinkNow(this, TESTED_URL))
                .bounds(this.width - 4 - testedW, 4, testedW, 16).build());

        StringWidget titleWidget = new StringWidget(this.title, this.font);
        titleWidget.setWidth(this.width);
        titleWidget.setX(0);
        titleWidget.setY(24);
        addRenderableWidget(titleWidget);

        int tabW = Math.min(110, (this.width - 40) / 3);
        int tabGap = 6;
        int tabsRowW = tabW * 3 + tabGap * 2;
        int tabsX = (this.width - tabsRowW) / 2;
        int tabRows = (TABS.length + 2) / 3;
        for (int i = 0; i < TABS.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int idx = i;
            Button b = Button.builder(tabLabel(i), btn -> {
                section = idx;
                rebuildWidgets();
            }).bounds(tabsX + col * (tabW + tabGap), 38 + row * 22, tabW, 20).build();
            b.active = (i != section);
            addRenderableWidget(b);
        }

        List<AbstractWidget> rows = new ArrayList<>();
        switch (section) {
            case 1 -> {
                rows.add(toggle("reanimated.opt.screen_enabled", () -> c.screenOpenEnabled, v -> c.screenOpenEnabled = v));
                rows.add(slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = (float) v));
                rows.add(easing("reanimated.opt.screen_easing", () -> c.screenOpenEasing, v -> c.screenOpenEasing = v));
            }
            case 2 -> {
                rows.add(toggle("reanimated.opt.pause_enabled", () -> c.pauseEnabled, v -> c.pauseEnabled = v));
                rows.add(cycler("reanimated.opt.pause_preset", () -> c.pausePreset, v -> c.pausePreset = v,
                        UiPreset.values(), p -> p.display));
                rows.add(intSlider("reanimated.opt.pause_speed_ticks", 1, 40, c.pauseSpeedTicks, " t", v -> c.pauseSpeedTicks = v));
                rows.add(slider("reanimated.opt.pause_distance", 0, 80, c.pauseDistance, " px", v -> c.pauseDistance = (float) v));
                rows.add(easing("reanimated.opt.pause_easing", () -> c.pauseEasing, v -> c.pauseEasing = v));
            }
            case 3 -> {
                rows.add(toggle("reanimated.opt.container_enabled", () -> c.containerEnabled, v -> c.containerEnabled = v));
                rows.add(slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = (float) v));
                rows.add(easing("reanimated.opt.container_easing", () -> c.containerEasing, v -> c.containerEasing = v));
            }
            case 4 -> {
                rows.add(toggle("reanimated.opt.hover_enabled", () -> c.hoverEnabled, v -> c.hoverEnabled = v));
                rows.add(slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = (float) v));
                rows.add(slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = (float) v));
                rows.add(toggle("reanimated.opt.press_enabled", () -> c.pressEnabled, v -> c.pressEnabled = v));
                rows.add(slider("reanimated.opt.press_scale", 0.0, 0.3, c.pressScale, "", v -> c.pressScale = (float) v));
                rows.add(slider("reanimated.opt.press_duration", 0.05, 0.6, c.pressDuration, " s", v -> c.pressDuration = (float) v));
                rows.add(toggle("reanimated.opt.slot_enabled", () -> c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v));
                rows.add(slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = (float) v));
            }
            case 5 -> {
                rows.add(toggle("reanimated.opt.logo_enabled", () -> c.logoEnabled, v -> c.logoEnabled = v));
                rows.add(cycler("reanimated.opt.logo_style", () -> c.logoStyle, v -> c.logoStyle = v, LogoStyle.values(), s -> s.display));
                rows.add(slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = (float) v));
                rows.add(easing("reanimated.opt.logo_easing", () -> c.logoEasing, v -> c.logoEasing = v));
                rows.add(intSlider("reanimated.opt.logo_letter_duration", 50, 1200, c.profileLogo.durationMs, " ms", v -> c.profileLogo.durationMs = v));
                rows.add(intSlider("reanimated.opt.logo_letter_delay", 0, 300, c.profileLogo.cascadeDelayMs, " ms", v -> c.profileLogo.cascadeDelayMs = v));
                rows.add(slider("reanimated.opt.logo_letter_offset", -60, 60, c.profileLogo.offsetY, " px", v -> c.profileLogo.offsetY = (float) v));
                rows.add(cycler("reanimated.opt.logo_letter_order", () -> c.profileLogo.cascadeOrder,
                        v -> c.profileLogo.cascadeOrder = v, CascadeOrder.values(), o -> o.display));
                rows.add(easing("reanimated.opt.logo_letter_easing", () -> c.profileLogo.easing, v -> c.profileLogo.easing = v));
            }
            case 6 -> {
                rows.add(toggle("reanimated.opt.tabs_enabled", () -> c.tabsEnabled, v -> c.tabsEnabled = v));
                rows.add(intSlider("reanimated.opt.tabs_duration", 50, 1200, c.profileTabs.durationMs, " ms", v -> c.profileTabs.durationMs = v));
                rows.add(intSlider("reanimated.opt.tabs_delay", 0, 300, c.profileTabs.cascadeDelayMs, " ms", v -> c.profileTabs.cascadeDelayMs = v));
                rows.add(slider("reanimated.opt.tabs_offset", -120, 120, c.profileTabs.offsetY, " px", v -> c.profileTabs.offsetY = (float) v));
                rows.add(easing("reanimated.opt.tabs_easing", () -> c.profileTabs.easing, v -> c.profileTabs.easing = v));
            }
            default -> {
                rows.add(cycler("reanimated.opt.preset", () -> c.uiPreset, v -> c.uiPreset = v, UiPreset.MAIN, p -> p.display));
                rows.add(intSlider("reanimated.opt.speed_ticks", 1, 40, c.animationSpeedTicks, " t", v -> c.animationSpeedTicks = v));
                rows.add(scope("reanimated.opt.animate_scope", () -> c.animateModdedScreens, v -> c.animateModdedScreens = v));
                rows.add(toggle("reanimated.opt.close_enabled", () -> c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v));
                rows.add(toggle("reanimated.opt.bg_fade", () -> c.bgFadeEnabled, v -> c.bgFadeEnabled = v));
                rows.add(toggle("reanimated.opt.lists_enabled", () -> c.listsEnabled, v -> c.listsEnabled = v));
                rows.add(Button.builder(Component.translatable("reanimated.opt.export"),
                                b -> this.exportSettings())
                        .bounds(0, 0, 158, 20).build());
                rows.add(Button.builder(Component.translatable("reanimated.opt.import"),
                                b -> this.importSettings())
                        .bounds(0, 0, 158, 20).build());
                rows.add(Button.builder(Component.translatable("reanimated.opt.profile_editor"),
                                b -> this.minecraft.setScreen(new AnimProfileEditorScreen(this)))
                        .bounds(0, 0, 158, 20).build());
                rows.add(Button.builder(Component.translatable("reanimated.opt.studio"),
                                b -> this.minecraft.setScreen(new AnimationStudioScreen(this)))
                        .bounds(0, 0, 158, 20).build());
            }
        }

        int colW = Math.min(158, (this.width - 36) / 2);
        int gap = 12, rowH = 20, vgap = 3;
        int totalW = colW * 2 + gap;
        int startX = (this.width - totalW) / 2;
        int startY = 38 + tabRows * 22 + 6;
        for (int i = 0; i < rows.size(); i++) {
            int col = i % 2;
            int r = i / 2;
            AbstractWidget w = rows.get(i);
            w.setWidth(colW);
            w.setX(startX + col * (colW + gap));
            w.setY(startY + r * (rowH + vgap));
            addRenderableWidget(w);
        }

        this.notice = new StringWidget(Component.empty(), this.font);
        this.notice.setWidth(this.width - 8);
        this.notice.setX(4);
        this.notice.setY(this.height - 40);
        addRenderableWidget(this.notice);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 26, 200, 20).build());
    }

    private static Component tabLabel(int i) {
        return Component.translatable(TABS[i]);
    }

    private Button toggle(String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(boolLabel(key, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            ReAnimatedConfig.get().save();
            b.setMessage(boolLabel(key, nv));
        }).bounds(0, 0, 158, 20).build();
    }

    private Button scope(String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(scopeLabel(key, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            ReAnimatedConfig.get().save();
            b.setMessage(scopeLabel(key, nv));
        }).bounds(0, 0, 158, 20).build();
    }

    private <T extends Enum<T>> Button cycler(String key, Supplier<T> get, Consumer<T> set,
                                              T[] values, java.util.function.Function<T, String> display) {
        return Button.builder(enumLabel(key, display.apply(get.get())), b -> {
            int i = 0;
            for (int k = 0; k < values.length; k++) {
                if (values[k] == get.get()) i = k;
            }
            T nx = values[(i + 1) % values.length];
            set.accept(nx);
            ReAnimatedConfig.get().save();
            b.setMessage(enumLabel(key, display.apply(nx)));
        }).bounds(0, 0, 158, 20).build();
    }

    private Button easing(String key, Supplier<EasingType> get, Consumer<EasingType> set) {
        return cycler(key, get, set, EasingType.values(), e -> e.display);
    }

    private AbstractWidget slider(String key, double min, double max, double current, String unit, DoubleConsumer setter) {
        return new ConfigSlider(key, min, max, current, unit, setter);
    }

    private AbstractWidget intSlider(String key, int min, int max, int current, String unit, IntConsumer setter) {
        return new IntSlider(key, min, max, current, unit, setter);
    }

    private static Component boolLabel(String key, boolean on) {
        return Component.translatable(key).append(Component.literal(": " + (on ? "ON" : "OFF")));
    }

    private static Component scopeLabel(String key, boolean all) {
        return Component.translatable(key).append(Component.literal(": "))
                .append(Component.translatable(all ? "reanimated.opt.animate_scope.all" : "reanimated.opt.animate_scope.vanilla"));
    }

    private static Component enumLabel(String key, String display) {
        return Component.translatable(key).append(Component.literal(": " + display));
    }

    private void exportSettings() {
        this.minecraft.keyboardHandler.setClipboard(ReAnimatedConfig.get().toShareString());
        this.showNotice("reanimated.opt.export.done", false);
    }

    private void importSettings() {
        String text = this.minecraft.keyboardHandler.getClipboard();
        if (ReAnimatedConfig.applyShareString(text)) {
            this.rebuildWidgets();
            this.showNotice("reanimated.opt.import.done", false);
        } else {
            this.showNotice("reanimated.opt.import.failed", true);
        }
    }

    private void showNotice(String key, boolean error) {
        if (this.notice != null) {
            this.notice.setMessage(Component.translatable(key)
                .withStyle(error ? ChatFormatting.RED : ChatFormatting.GREEN));
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    /** Slider built on the stable AbstractSliderButton (value: 0..1). */
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

    /** Integer slider: durations in ms, animation speed in ticks. */
    private static class IntSlider extends AbstractSliderButton {
        private final String key;
        private final int min;
        private final int max;
        private final String unit;
        private final IntConsumer setter;

        IntSlider(String key, int min, int max, int current, String unit, IntConsumer setter) {
            super(0, 0, 158, 20, Component.empty(), (double) (current - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.setter = setter;
            updateMessage();
        }

        private int intValue() {
            return (int) Math.round(min + this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(key).append(Component.literal(": " + intValue() + unit)));
        }

        @Override
        protected void applyValue() {
            setter.accept(intValue());
            ReAnimatedConfig.get().save();
        }
    }
}
