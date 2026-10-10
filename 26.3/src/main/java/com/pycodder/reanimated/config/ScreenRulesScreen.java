package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.ScreenOverride;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.anim.UiPreset;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScreenRulesScreen extends Screen {

    private static final UiPreset[] PRESETS = reanimated$presetValues();

    private static UiPreset[] reanimated$presetValues() {
        UiPreset[] main = UiPreset.MAIN;
        UiPreset[] out = new UiPreset[main.length + 1];
        out[0] = UiPreset.INHERIT;
        System.arraycopy(main, 0, out, 1, main.length);
        return out;
    }

    private static final int[] SPEEDS = {0, 2, 3, 4, 6, 8, 10, 14, 20, 30};

    private static final int ROWS_PER_PAGE = 7;

    private final Screen parent;
    private static int page = 0;

    public ScreenRulesScreen(Screen parent) {
        super(Component.translatable("reanimated.screens.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<String> keys = ScreenRules.listForEditor();
        int pages = Math.max(1, (keys.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        if (page >= pages) page = pages - 1;
        if (page < 0) page = 0;

        int colGap = 8;
        int colW = Math.min(200, (this.width - 40 - colGap) / 2);
        int speedW = Math.min(110, colW);
        int totalW = colW + colGap + speedW;
        int startX = (this.width - totalW) / 2;
        int y = 44;

        for (int i = page * ROWS_PER_PAGE; i < Math.min(keys.size(), (page + 1) * ROWS_PER_PAGE); i++) {
            String key = keys.get(i);
            addRenderableWidget(presetButton(key, startX, y, colW));
            addRenderableWidget(speedButton(key, startX + colW + colGap, y, speedW));
            y += 22;
        }

        int navY = this.height - 52;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            page--;
            rebuildWidgets();
        }).bounds(this.width / 2 - 130, navY, 20, 20).build());
        addRenderableWidget(Button.builder(
            Component.literal((page + 1) + " / " + pages), b -> {}).bounds(this.width / 2 - 105, navY, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            page++;
            rebuildWidgets();
        }).bounds(this.width / 2 - 40, navY, 20, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.screens.clear"), b -> {
            ReAnimatedConfig.get().screenOverrides.clear();
            ReAnimatedConfig.get().save();
            rebuildWidgets();
        }).bounds(this.width / 2 - 10, navY, 140, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(this.width / 2 - 100, this.height - 26, 200, 20).build());
    }

    private Button presetButton(String key, int x, int y, int w) {
        return Button.builder(presetLabel(key), b -> {
            ScreenOverride o = ScreenRules.getOrCreate(key);
            int i = 0;
            UiPreset cur = o.animate ? (o.preset == null ? UiPreset.INHERIT : o.preset) : UiPreset.INHERIT;
            for (int k = 0; k < PRESETS.length; k++) {
                if (PRESETS[k] == cur) i = k;
            }
            int next = o.animate ? i + 1 : 0;
            if (next >= PRESETS.length) {
                o.animate = false;
                o.preset = UiPreset.INHERIT;
            } else {
                o.animate = true;
                o.preset = PRESETS[next];
            }
            ScreenRules.prune();
            ReAnimatedConfig.get().save();
            b.setMessage(presetLabel(key));
        }).bounds(x, y, w, 20).build();
    }

    private Button speedButton(String key, int x, int y, int w) {
        return Button.builder(speedLabel(key), b -> {
            ScreenOverride o = ScreenRules.getOrCreate(key);
            int i = 0;
            for (int k = 0; k < SPEEDS.length; k++) {
                if (SPEEDS[k] == o.speedTicks) i = k;
            }
            o.speedTicks = SPEEDS[(i + 1) % SPEEDS.length];
            ScreenRules.prune();
            ReAnimatedConfig.get().save();
            b.setMessage(speedLabel(key));
        }).bounds(x, y, w, 20).build();
    }

    private Component presetLabel(String key) {
        ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
        String value;
        if (o == null || (o.animate && (o.preset == null || o.preset == UiPreset.INHERIT))) {
            value = "—";
        } else if (!o.animate) {
            value = "OFF";
        } else {
            value = o.preset.display;
        }
        return Component.literal(ScreenRules.displayName(key) + ": " + value);
    }

    private Component speedLabel(String key) {
        ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
        int ticks = o == null ? 0 : o.speedTicks;
        return Component.translatable("reanimated.screens.speed").append(
            Component.literal(ticks <= 0 ? ": —" : ": " + ticks + " t"));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }
}
