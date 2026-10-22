package com.pycodder.reanimated.config;

import com.pycodder.reanimated.ReAnimatedKeys;
import com.pycodder.reanimated.anim.ScreenRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Список экранов и модов, которым можно задать свои настройки. Каждый пункт открывает
 * {@link ScreenSettingsScreen}; настроенные подсвечены зелёным.
 *
 * Список составляется из экранов, которые игрок открывал в этом запуске, плюс тех,
 * для которых настройки уже заданы: перечислить все экраны сборки заранее неоткуда,
 * они появляются только когда их создаёт игра или мод. Свежие — сверху.
 *
 * Экран самодостаточный, как и остальные экраны мода на Mojmap: только кнопки и
 * ручная раскладка, без OptionsList и прокрутки — вместо неё постраничный список,
 * поэтому один и тот же код работает на всех поддерживаемых версиях.
 */
public class ScreenRulesScreen extends Screen {

    private static final int ROW = 22;
    private static final int TOP = 58;

    private final Screen parent;
    /** Открытая страница списка; статическая, чтобы возврат на экран не сбрасывал её. */
    private static int page = 0;

    public ScreenRulesScreen(Screen parent) {
        super(Component.translatable("reanimated.screens.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        text(this.title, 12);
        text(Component.translatable("reanimated.screens.hint").withStyle(ChatFormatting.GRAY), 26);
        text(Component.translatable("reanimated.screens.hint_key",
            ReAnimatedKeys.SCREEN_SETTINGS.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY), 38);

        // Сначала правила модов, затем отдельные экраны.
        List<String> keys = new ArrayList<>(ScreenRules.modsForEditor());
        keys.addAll(ScreenRules.listForEditor());
        int perPage = Math.max(1, (this.height - 60 - TOP) / ROW);
        int pages = Math.max(1, (keys.size() + perPage - 1) / perPage);
        if (page >= pages) page = pages - 1;
        if (page < 0) page = 0;

        int w = Math.min(310, this.width - 40);
        int x = (this.width - w) / 2;
        int y = TOP;
        for (int i = page * perPage; i < Math.min(keys.size(), (page + 1) * perPage); i++) {
            String key = keys.get(i);
            addRenderableWidget(Button.builder(label(key), b -> this.minecraft.setScreen(new ScreenSettingsScreen(this, key)))
                .bounds(x, y, w, 20).build());
            y += ROW;
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

    /** Строка текста по центру экрана. */
    private void text(Component text, int y) {
        int w = this.font.width(text);
        addRenderableWidget(new StringWidget((this.width - w) / 2, y, w, 9, text, this.font));
    }

    /** Подпись пункта: моды помечены, настроенные — зелёные. */
    private static Component label(String key) {
        MutableComponent name = Component.literal(ScreenRules.displayName(key));
        if (ScreenRules.isModKey(key)) {
            name = Component.translatable("reanimated.screens.mod_title", ScreenRules.displayName(key));
        } else if (ScreenRules.isModded(key)) {
            name = name.append(Component.literal(" ").append(
                Component.translatable("reanimated.screens.modded").withStyle(ChatFormatting.DARK_GRAY)));
        }
        return name.withStyle(ScreenRules.customized(key) ? ChatFormatting.GREEN : ChatFormatting.WHITE);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }
}
