package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.UiPreset;
import net.minecraft.client.gui.GuiGraphics;
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
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Экран настроек мода (NeoForge / Mojmap). Самодостаточный: только стабильные
 * примитивы (Button + свой слайдер), без OptionInstance/OptionsList — поэтому
 * один код работает на 1.21.1–1.21.11.
 *
 * Настройки разбиты на СЕКЦИИ (как в 1.4.0 на Fabric), но вместо прокручиваемого
 * списка здесь ряд кнопок-вкладок сверху: за раз показывается одна секция.
 * Прокрутку намеренно не делаем — mouseScrolled/mouseClicked пришлось бы трогать,
 * а их сигнатуры менялись между версиями.
 *
 * Раз прокрутки нет, раскладка обязана подстраиваться под размер экрана: число
 * колонок вкладок, число колонок и высота строк подбираются так, чтобы секция
 * целиком поместилась между вкладками и кнопкой «Готово» — вплоть до 320×240.
 *
 * Ссылки "mod by @pycodder" и "Протестированные моды" — кнопки через
 * ConfirmLinkScreen.confirmLinkNow (портируется без изменений на все версии).
 */
public class ReAnimatedConfigScreen extends Screen {

    private static final String AUTHOR_URL = "https://modrinth.com/user/pycodder";
    private static final String TESTED_URL = "https://github.com/RBXLU/ReAnimated/blob/main/testedmods.txt";

    /** Ключи вкладок в порядке отображения. */
    private static final String[] TABS = {
        "reanimated.tab.general",
        "reanimated.tab.menu",
        "reanimated.tab.pause",
        "reanimated.tab.containers",
        "reanimated.tab.cursor",
        "reanimated.tab.logo",
        "reanimated.tab.tabs",
    };

    // --- Метрики раскладки ---
    /** Отступ от боковых краёв экрана. */
    private static final int SIDE = 12;
    private static final int TABS_TOP = 38;
    private static final int TAB_ROW_H = 22;
    private static final int TAB_GAP = 6;
    private static final int TAB_MIN_W = 70;
    private static final int TAB_MAX_W = 110;
    private static final int COL_GAP = 12;
    /** Потолок ширины строки. 158 (как было) резало почти все подписи в бегущую строку. */
    private static final int COL_MAX_W = 240;
    /**
     * Варианты сетки строк — столбцы, высота строки, зазор — по убыванию «красоты».
     * Берётся первый, в который секция целиком помещается между вкладками и «Готово».
     */
    private static final int[][] GRIDS = {
        {2, 20, 3}, {2, 20, 2}, {2, 18, 2}, {3, 20, 3}, {3, 18, 2}, {3, 16, 1},
    };

    private final Screen parent;
    /** Результат обмена настройками и момент показа; гаснет сам. */
    private net.minecraft.network.chat.Component notice = null;
    private long noticeTime = 0L;
    private static final long NOTICE_MS = 4000L;
    /** Выбранная секция. Статическое — чтобы при возврате из Студии/Редактора остаться на месте. */
    private static int section = 0;

    public ReAnimatedConfigScreen(Screen parent) {
        super(Component.translatable("reanimated.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ReAnimatedConfig c = ReAnimatedConfig.get();

        // --- Ссылки сверху ---
        // Ширина по надписи, а не фиксированная: "Протестированные моды" в 116 px не
        // влезала и ваниль обрезала её прямо посреди слова.
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

        // --- Кнопки-вкладки ---
        // Колонок столько, сколько влезает по ширине: на обычном экране семь вкладок
        // ложатся в два ряда вместо трёх и освобождают целый ряд под сами настройки.
        int tabCols = Math.max(2, Math.min(4, (this.width - 2 * SIDE + TAB_GAP) / (TAB_MIN_W + TAB_GAP)));
        int tabW = Math.min(TAB_MAX_W, (this.width - 2 * SIDE - (tabCols - 1) * TAB_GAP) / tabCols);
        // Сколько рядов вкладок реально получилось — из этого считается верх списка опций.
        int tabRows = (TABS.length + tabCols - 1) / tabCols;
        for (int i = 0; i < TABS.length; i++) {
            int row = i / tabCols;
            int col = i % tabCols;
            // Последний ряд бывает неполным — центрируем его отдельно, иначе одинокая
            // вкладка висела бы у левого края под серединой предыдущего ряда.
            int inRow = Math.min(tabCols, TABS.length - row * tabCols);
            int rowW = inRow * tabW + (inRow - 1) * TAB_GAP;
            int idx = i;
            Button b = Button.builder(tabLabel(i), btn -> {
                section = idx;
                rebuildWidgets();
            }).bounds((this.width - rowW) / 2 + col * (tabW + TAB_GAP),
                    TABS_TOP + row * TAB_ROW_H, tabW, 20).build();
            b.active = (i != section); // текущая вкладка не нажимается — так видно, где мы
            addRenderableWidget(b);
        }

        // --- Строки выбранной секции ---
        List<AbstractWidget> rows = new ArrayList<>();
        switch (section) {
            case 1 -> { // Меню
                rows.add(toggle("reanimated.opt.screen_enabled", () -> c.screenOpenEnabled, v -> c.screenOpenEnabled = v));
                rows.add(slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = (float) v));
                rows.add(easing("reanimated.opt.screen_easing", () -> c.screenOpenEasing, v -> c.screenOpenEasing = v));
            }
            case 2 -> { // Меню паузы
                rows.add(toggle("reanimated.opt.pause_enabled", () -> c.pauseEnabled, v -> c.pauseEnabled = v));
                rows.add(cycler("reanimated.opt.pause_preset", () -> c.pausePreset, v -> c.pausePreset = v,
                        UiPreset.values(), p -> p.display));
                rows.add(intSlider("reanimated.opt.pause_speed_ticks", 1, 40, c.pauseSpeedTicks, " t", v -> c.pauseSpeedTicks = v));
                rows.add(slider("reanimated.opt.pause_distance", 0, 80, c.pauseDistance, " px", v -> c.pauseDistance = (float) v));
                rows.add(easing("reanimated.opt.pause_easing", () -> c.pauseEasing, v -> c.pauseEasing = v));
            }
            case 3 -> { // Контейнеры
                rows.add(toggle("reanimated.opt.container_enabled", () -> c.containerEnabled, v -> c.containerEnabled = v));
                rows.add(slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = (float) v));
                rows.add(easing("reanimated.opt.container_easing", () -> c.containerEasing, v -> c.containerEasing = v));
            }
            case 4 -> { // Курсор
                rows.add(toggle("reanimated.opt.hover_enabled", () -> c.hoverEnabled, v -> c.hoverEnabled = v));
                rows.add(slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = (float) v));
                rows.add(slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = (float) v));
                rows.add(toggle("reanimated.opt.press_enabled", () -> c.pressEnabled, v -> c.pressEnabled = v));
                rows.add(slider("reanimated.opt.press_scale", 0.0, 0.3, c.pressScale, "", v -> c.pressScale = (float) v));
                rows.add(slider("reanimated.opt.press_duration", 0.05, 0.6, c.pressDuration, " s", v -> c.pressDuration = (float) v));
                rows.add(toggle("reanimated.opt.slot_enabled", () -> c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v));
                rows.add(slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = (float) v));
            }
            case 5 -> { // Логотип
                rows.add(toggle("reanimated.opt.logo_enabled", () -> c.logoEnabled, v -> c.logoEnabled = v));
                rows.add(cycler("reanimated.opt.logo_style", () -> c.logoStyle, v -> c.logoStyle = v, LogoStyle.values(), s -> s.display));
                // Стиль GROW — родная анимация "вырастания"
                rows.add(slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = (float) v));
                rows.add(easing("reanimated.opt.logo_easing", () -> c.logoEasing, v -> c.logoEasing = v));
                // Стиль LETTERS — побуквенный каскад
                rows.add(intSlider("reanimated.opt.logo_letter_duration", 50, 1200, c.profileLogo.durationMs, " ms", v -> c.profileLogo.durationMs = v));
                rows.add(intSlider("reanimated.opt.logo_letter_delay", 0, 300, c.profileLogo.cascadeDelayMs, " ms", v -> c.profileLogo.cascadeDelayMs = v));
                rows.add(slider("reanimated.opt.logo_letter_offset", -60, 60, c.profileLogo.offsetY, " px", v -> c.profileLogo.offsetY = (float) v));
                rows.add(cycler("reanimated.opt.logo_letter_order", () -> c.profileLogo.cascadeOrder,
                        v -> c.profileLogo.cascadeOrder = v, CascadeOrder.values(), o -> o.display));
                rows.add(easing("reanimated.opt.logo_letter_easing", () -> c.profileLogo.easing, v -> c.profileLogo.easing = v));
            }
            case 6 -> { // Вкладки
                rows.add(toggle("reanimated.opt.tabs_enabled", () -> c.tabsEnabled, v -> c.tabsEnabled = v));
                rows.add(intSlider("reanimated.opt.tabs_duration", 50, 1200, c.profileTabs.durationMs, " ms", v -> c.profileTabs.durationMs = v));
                rows.add(intSlider("reanimated.opt.tabs_delay", 0, 300, c.profileTabs.cascadeDelayMs, " ms", v -> c.profileTabs.cascadeDelayMs = v));
                rows.add(slider("reanimated.opt.tabs_offset", -120, 120, c.profileTabs.offsetY, " px", v -> c.profileTabs.offsetY = (float) v));
                rows.add(easing("reanimated.opt.tabs_easing", () -> c.profileTabs.easing, v -> c.profileTabs.easing = v));
            }
            default -> { // Общее
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

        // Ниже последнего ряда вкладок, а не по константе: раньше здесь стояло 86, при
        // семи вкладках третий ряд лез на первую строку настроек.
        int rowsTop = TABS_TOP + tabRows * TAB_ROW_H + 6;
        int doneY = this.height - 26;
        // Полоса, в которую обязаны уложиться все строки секции: от вкладок до «Готово».
        int avail = Math.max(20, doneY - 4 - rowsTop);

        // Сетка подбирается под эту полосу, а не задаётся жёстко. Раньше было наглухо
        // два столбца по 20 px: на экране 427×240 «Общее» (11 строк) не влезало, нижний
        // ряд уезжал под кнопку «Готово» и за край экрана.
        int[] grid = GRIDS[GRIDS.length - 1];
        for (int[] g : GRIDS) {
            int lines = (rows.size() + g[0] - 1) / g[0];
            if (lines * (g[1] + g[2]) - g[2] <= avail) {
                grid = g;
                break;
            }
        }
        int cols = grid[0], rowH = grid[1], vgap = grid[2];

        int lines = (rows.size() + cols - 1) / cols;
        int colW = Math.min(COL_MAX_W, (this.width - 2 * SIDE - (cols - 1) * COL_GAP) / cols);
        int totalW = colW * cols + COL_GAP * (cols - 1);
        int startX = (this.width - totalW) / 2;
        // Блок настроек центрируется в свободной полосе: иначе на большом экране всё
        // жалось к верхнему краю, а снизу оставалась пустота в пол-экрана.
        int blockH = lines * (rowH + vgap) - vgap;
        int startY = rowsTop + Math.max(0, (avail - blockH) / 2);
        for (int i = 0; i < rows.size(); i++) {
            int col = i % cols;
            int r = i / cols;
            AbstractWidget w = rows.get(i);
            w.setWidth(colW);
            w.setHeight(rowH);
            w.setX(startX + col * (colW + COL_GAP));
            w.setY(startY + r * (rowH + vgap));
            addRenderableWidget(w);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 100, doneY, 200, 20).build());
    }

    private static Component tabLabel(int i) {
        return Component.translatable(TABS[i]);
    }

    // ------------------------------------------------------------------ строки

    private Button toggle(String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(boolLabel(key, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            ReAnimatedConfig.get().save();
            b.setMessage(boolLabel(key, nv));
        }).bounds(0, 0, 158, 20).build();
    }

    /** Переключатель "Весь интерфейс / Только ванильные". true = анимировать и моды. */
    private Button scope(String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(scopeLabel(key, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            ReAnimatedConfig.get().save();
            b.setMessage(scopeLabel(key, nv));
        }).bounds(0, 0, 158, 20).build();
    }

    /** Кнопка-перебор значений enum по кругу с подписью из {@code display}. */
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
            Component saved = Component.translatable("reanimated.opt.import.done")
                .withStyle(net.minecraft.ChatFormatting.GREEN);
            this.rebuildWidgets();   // пересборка сбрасывает сообщение — ставим его после
            this.notice = saved;
            this.noticeTime = System.currentTimeMillis();
        } else {
            this.showNotice("reanimated.opt.import.failed", true);
        }
    }

    private void showNotice(String key, boolean error) {
        this.notice = Component.translatable(key).withStyle(
            error ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.GREEN);
        this.noticeTime = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        g.drawCenteredString(this.font, this.title, this.width / 2, 26, 0xFFFFFFFF);
        if (this.notice != null) {
            if (System.currentTimeMillis() - this.noticeTime > NOTICE_MS) {
                this.notice = null;
            } else {
                // Подложка: сообщение живёт 4 секунды поверх нижнего ряда настроек,
                // и без неё на тесном экране текст сливался с надписями кнопок.
                int noticeY = this.height - 40;
                int noticeW = this.font.width(this.notice);
                g.fill(this.width / 2 - noticeW / 2 - 3, noticeY - 2,
                        this.width / 2 + noticeW / 2 + 3, noticeY + 10, 0xC0000000);
                g.drawCenteredString(this.font, this.notice, this.width / 2, noticeY, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
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

    /** Целочисленный слайдер (длительности в мс, скорость анимации в тиках). */
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
