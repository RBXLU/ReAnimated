package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.ScreenRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Все настройки одного экрана или всех экранов одного мода. Открывается из списка
 * экранов или клавишей «Настроить этот экран» прямо с нужного экрана.
 *
 * Строки и их значения описывает {@link ScreenSettingsModel}, здесь — только кнопки
 * в две колонки: слева появление, справа остальное.
 */
public class ScreenSettingsScreen extends Screen {

    private static final int COL = 150;
    private static final int GAP = 10;
    private static final int ROW = 22;

    private final Screen parent;
    private final ScreenSettingsModel model;
    private final Map<String, Button> buttons = new LinkedHashMap<>();

    public ScreenSettingsScreen(Screen parent, String key) {
        super(title(key));
        this.parent = parent;
        this.model = new ScreenSettingsModel(key);
    }

    private static Component title(String key) {
        String name = ScreenRules.displayName(key);
        return ScreenRules.isModKey(key)
            ? Component.translatable("reanimated.screens.mod_title", name)
            : Component.literal(name);
    }

    @Override
    protected void init() {
        this.buttons.clear();
        text(this.title, 12);
        text(Component.translatable(this.model.isMod() ? "reanimated.screens.mod_hint" : "reanimated.screens.screen_hint")
            .withStyle(ChatFormatting.GRAY), 26);

        int left = this.width / 2 - COL - GAP / 2;
        int right = this.width / 2 + GAP / 2;
        String[] l = ScreenSettingsModel.LEFT;
        String[] r = ScreenSettingsModel.RIGHT;
        int y = 44;
        for (int i = 0; i < Math.max(l.length, r.length); i++) {
            if (i < l.length) button(l[i], left, y);
            if (i < r.length && this.model.has(r[i])) button(r[i], right, y);
            y += ROW;
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(this.width / 2 - 100, this.height - 26, 200, 20).build());
        refresh();
    }

    private void text(Component text, int y) {
        int w = this.font.width(text);
        addRenderableWidget(new StringWidget((this.width - w) / 2, y, w, 9, text, this.font));
    }

    private void button(String id, int x, int y) {
        Button b = Button.builder(Component.empty(), w -> this.press(id))
            .bounds(x, y, COL, 20)
            .tooltip(Tooltip.create(Component.translatable(ScreenSettingsModel.tooltipKey(id))))
            .build();
        this.buttons.put(id, b);
        addRenderableWidget(b);
    }

    private void press(String id) {
        switch (id) {
            case ScreenSettingsModel.EDIT_BUTTONS -> {
                AnimProfile own = this.model.ownProfile();
                if (own != null) {
                    this.minecraft.setScreen(new AnimProfileEditorScreen(this, own, this.model::commit));
                }
                return;
            }
            case ScreenSettingsModel.RESET -> this.model.reset();
            case ScreenSettingsModel.MOD_RULE -> {
                String mod = this.model.modKey();
                if (mod != null) this.minecraft.setScreen(new ScreenSettingsScreen(this, mod));
                return;
            }
            default -> this.model.click(id);
        }
        refresh();
    }

    /** Подписи и доступность читаются заново: одна настройка влияет на другие. */
    private void refresh() {
        for (Map.Entry<String, Button> e : this.buttons.entrySet()) {
            e.getValue().setMessage(text(this.model.label(e.getKey())));
            e.getValue().active = this.model.active(e.getKey());
        }
    }

    private static Component text(ScreenSettingsModel.Label l) {
        MutableComponent t = Component.translatable(l.nameKey());
        if (l.valueKey() != null) {
            t.append(": ").append(Component.translatable(l.valueKey()));
        } else if (l.valueText() != null) {
            t.append(": " + l.valueText());
        }
        return t;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
