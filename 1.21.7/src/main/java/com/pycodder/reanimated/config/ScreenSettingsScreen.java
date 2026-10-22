package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.ScreenRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Все настройки одного экрана или всех экранов одного мода. Открывается из списка
 * экранов или клавишей «Настроить этот экран» прямо с нужного экрана.
 *
 * Строки и их значения описывает {@link ScreenSettingsModel}, здесь — только кнопки.
 */
public class ScreenSettingsScreen extends GameOptionsScreen {

    private final ScreenSettingsModel model;
    private final Map<String, ButtonWidget> buttons = new LinkedHashMap<>();

    public ScreenSettingsScreen(Screen parent, String key) {
        super(parent, MinecraftClient.getInstance().options, title(key));
        this.model = new ScreenSettingsModel(key);
    }

    private static Text title(String key) {
        String name = ScreenRules.displayName(key);
        return ScreenRules.isModKey(key)
            ? Text.translatable("reanimated.screens.mod_title", name)
            : Text.literal(name);
    }

    @Override
    protected void addOptions() {
        this.buttons.clear();
        Text hint = Text.translatable(this.model.isMod() ? "reanimated.screens.mod_hint" : "reanimated.screens.screen_hint");
        this.body.addWidgetEntry(new TextWidget(hint.copy().formatted(Formatting.GRAY), this.textRenderer), null);

        String[] left = ScreenSettingsModel.LEFT;
        String[] right = ScreenSettingsModel.RIGHT;
        for (int i = 0; i < Math.max(left.length, right.length); i++) {
            this.body.addWidgetEntry(
                i < left.length ? button(left[i]) : null,
                i < right.length && this.model.has(right[i]) ? button(right[i]) : null);
        }
        refresh();
    }

    private ButtonWidget button(String id) {
        ButtonWidget b = ButtonWidget.builder(Text.empty(), w -> this.press(id))
            .dimensions(0, 0, 150, 20)
            .tooltip(Tooltip.of(Text.translatable(ScreenSettingsModel.tooltipKey(id))))
            .build();
        this.buttons.put(id, b);
        return b;
    }

    private void press(String id) {
        switch (id) {
            case ScreenSettingsModel.EDIT_BUTTONS -> {
                AnimProfile own = this.model.ownProfile();
                if (own != null) {
                    this.client.setScreen(new AnimProfileEditorScreen(this, own, this.model::commit));
                }
                return;
            }
            case ScreenSettingsModel.RESET -> this.model.reset();
            case ScreenSettingsModel.MOD_RULE -> {
                String mod = this.model.modKey();
                if (mod != null) this.client.setScreen(new ScreenSettingsScreen(this, mod));
                return;
            }
            default -> this.model.click(id);
        }
        refresh();
    }

    /** Подписи и доступность читаются заново: одна настройка влияет на другие. */
    private void refresh() {
        for (Map.Entry<String, ButtonWidget> e : this.buttons.entrySet()) {
            e.getValue().setMessage(text(this.model.label(e.getKey())));
            e.getValue().active = this.model.active(e.getKey());
        }
    }

    private static Text text(ScreenSettingsModel.Label l) {
        MutableText t = Text.translatable(l.nameKey());
        if (l.valueKey() != null) {
            t.append(": ").append(Text.translatable(l.valueKey()));
        } else if (l.valueText() != null) {
            t.append(": " + l.valueText());
        }
        return t;
    }
}
