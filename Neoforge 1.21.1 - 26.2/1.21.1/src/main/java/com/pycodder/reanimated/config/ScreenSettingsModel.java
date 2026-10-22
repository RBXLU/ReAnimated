package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.ScreenOverride;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.anim.Toggle;
import com.pycodder.reanimated.anim.UiPreset;
import com.pycodder.reanimated.api.CustomPreset;
import com.pycodder.reanimated.api.ReAnimatedApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Что можно настроить у одного экрана (или у всех экранов мода) и как это
 * переключается. Без классов игры: экраны настроек на разных загрузчиках рисуют
 * одни и те же строки кнопками, а значения и переходы между ними живут здесь.
 *
 * Каждая настройка — кнопка, которая по клику перебирает значения по кругу, первым
 * из которых всегда стоит «как в общих». Запись в конфиге заводится при первом
 * изменении и убирается, как только снова ничего не меняет.
 */
public final class ScreenSettingsModel {

    public static final String ANIMATION = "animation";
    public static final String PRESET = "preset";
    public static final String SPEED = "speed";
    public static final String DISTANCE = "distance";
    public static final String EASING = "easing";
    public static final String BUTTONS = "buttons";
    public static final String CLOSE = "close";
    public static final String BACKGROUND = "background";
    public static final String TRANSITIONS = "transitions";

    /** Кнопки-действия: их обрабатывает сам экран настроек. */
    public static final String EDIT_BUTTONS = "edit_buttons";
    public static final String RESET = "reset";
    public static final String MOD_RULE = "mod_rule";

    /** Значения скорости в тиках; 0 — «как в общих». */
    private static final int[] SPEEDS = {0, 2, 3, 4, 6, 8, 10, 14, 20, 30};
    /** Значения дистанции в px; меньше нуля — «как в общих». */
    private static final int[] DISTANCES = {-1, 0, 4, 8, 12, 16, 24, 32, 48, 64, 96};

    /** Подпись кнопки: ключ названия и значение — либо ключом перевода, либо готовым текстом. */
    public record Label(String nameKey, String valueKey, String valueText) {}

    private final String key;

    public ScreenSettingsModel(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public boolean isMod() {
        return ScreenRules.isModKey(key);
    }

    /** Ключ правила мода этого экрана, если мод известен и это не само правило мода. */
    public String modKey() {
        return isMod() ? null : ScreenRules.modOfKey(key);
    }

    /** Настройки по порядку: слева — появление, справа — остальное. */
    public static final String[] LEFT = {ANIMATION, PRESET, SPEED, DISTANCE, EASING, BUTTONS};
    public static final String[] RIGHT = {CLOSE, BACKGROUND, TRANSITIONS, EDIT_BUTTONS, RESET, MOD_RULE};

    /** Есть ли такая строка у этого экрана: у правила мода нет ссылки на правило мода. */
    public boolean has(String id) {
        return !MOD_RULE.equals(id) || modKey() != null;
    }

    private ScreenOverride current() {
        ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
        return o == null ? new ScreenOverride() : o;
    }

    public boolean customized() {
        return ScreenRules.customized(key);
    }

    /** Можно ли сейчас нажать кнопку. */
    public boolean active(String id) {
        ScreenOverride o = current();
        return switch (id) {
            case EDIT_BUTTONS -> o.profile != null && o.profile.enabled;
            case RESET -> customized();
            // Без анимации экрана остальное ни на что не влияет.
            case PRESET, SPEED, DISTANCE, EASING, BUTTONS, CLOSE, BACKGROUND, TRANSITIONS -> o.animation != Toggle.OFF;
            default -> true;
        };
    }

    public Label label(String id) {
        ScreenOverride o = current();
        return switch (id) {
            case ANIMATION -> toggle("reanimated.screens.animate", o.animation);
            case PRESET -> presetLabel(o);
            case SPEED -> o.speedTicks <= 0 ? inherit("reanimated.screens.speed")
                : new Label("reanimated.screens.speed", null, o.speedTicks + " t");
            case DISTANCE -> o.distance < 0f ? inherit("reanimated.screens.distance")
                : new Label("reanimated.screens.distance", null, Math.round(o.distance) + " px");
            case EASING -> o.easing == null ? inherit("reanimated.screens.easing")
                : new Label("reanimated.screens.easing", null, o.easing.display);
            case BUTTONS -> o.profile == null ? inherit("reanimated.screens.buttons")
                : new Label("reanimated.screens.buttons",
                    o.profile.enabled ? "reanimated.screens.buttons_own" : "reanimated.screens.off", null);
            case CLOSE -> toggle("reanimated.screens.close", o.close);
            case BACKGROUND -> toggle("reanimated.screens.background", o.background);
            case TRANSITIONS -> toggle("reanimated.screens.transitions", o.transitions);
            case EDIT_BUTTONS -> new Label("reanimated.screens.edit_buttons", null, null);
            case RESET -> new Label(isMod() ? "reanimated.screens.reset_mod" : "reanimated.screens.reset", null, null);
            case MOD_RULE -> new Label("reanimated.screens.mod_rule", null, ScreenRules.displayName(modKey()));
            default -> new Label(id, null, null);
        };
    }

    /** Подсказка к строке — ключ перевода. */
    public static String tooltipKey(String id) {
        return "reanimated.screens.tip." + id;
    }

    private static Label inherit(String nameKey) {
        return new Label(nameKey, "reanimated.screens.inherit", null);
    }

    private static Label toggle(String nameKey, Toggle t) {
        return switch (t == null ? Toggle.INHERIT : t) {
            case INHERIT -> inherit(nameKey);
            case ON -> new Label(nameKey, "reanimated.screens.on", null);
            case OFF -> new Label(nameKey, "reanimated.screens.off", null);
        };
    }

    private static Label presetLabel(ScreenOverride o) {
        if (o.preset == null || o.preset == UiPreset.INHERIT) return inherit("reanimated.screens.preset");
        if (o.preset == UiPreset.CUSTOM) {
            CustomPreset cp = ReAnimatedApi.preset(o.customPresetId);
            return new Label("reanimated.screens.preset", null, cp == null ? UiPreset.CUSTOM.display : cp.displayName());
        }
        return new Label("reanimated.screens.preset", null, o.preset.display);
    }

    /** Нажатие на настройку: следующее значение по кругу. Действия обрабатывает экран. */
    public void click(String id) {
        ScreenOverride o = ScreenRules.getOrCreate(key);
        switch (id) {
            case ANIMATION -> o.animation = o.animation.next();
            case PRESET -> nextPreset(o);
            case SPEED -> o.speedTicks = SPEEDS[(indexOf(SPEEDS, o.speedTicks) + 1) % SPEEDS.length];
            case DISTANCE -> o.distance = DISTANCES[(indexOf(DISTANCES, Math.round(o.distance)) + 1) % DISTANCES.length];
            case EASING -> {
                EasingType[] all = EasingType.values();
                o.easing = o.easing == null ? all[0] : (o.easing.ordinal() + 1 < all.length ? all[o.easing.ordinal() + 1] : null);
            }
            case BUTTONS -> {
                // Как в общих → свой (копия общего, сразу включённый) → выключен → как в общих.
                if (o.profile == null) {
                    o.profile = ReAnimatedConfig.get().profile.copy();
                    o.profile.enabled = true;
                } else if (o.profile.enabled) {
                    o.profile.enabled = false;
                } else {
                    o.profile = null;
                }
            }
            case CLOSE -> o.close = o.close.next();
            case BACKGROUND -> o.background = o.background.next();
            case TRANSITIONS -> o.transitions = o.transitions.next();
            default -> { }
        }
        commit();
    }

    /** Профиль кнопок для редактора; null, если у экрана своего профиля нет. */
    public AnimProfile ownProfile() {
        ScreenOverride o = ReAnimatedConfig.get().screenOverrides.get(key);
        return o == null ? null : o.profile;
    }

    public void reset() {
        ReAnimatedConfig.get().screenOverrides.remove(key);
        ReAnimatedConfig.get().save();
    }

    /** Сохраняет изменения и убирает запись, если она снова ничего не меняет. */
    public void commit() {
        ScreenRules.prune();
        ReAnimatedConfig.get().save();
    }

    /** Пресеты по кругу: «как в общих», обычные, затем анимации из расширений. */
    private static void nextPreset(ScreenOverride o) {
        List<Object> values = new ArrayList<>();
        values.add(UiPreset.INHERIT);
        for (UiPreset p : UiPreset.MAIN) values.add(p);
        for (CustomPreset cp : ReAnimatedApi.presets()) values.add(cp.id());
        Object cur = o.preset == UiPreset.CUSTOM ? o.customPresetId : o.preset;
        int i = values.indexOf(cur);
        Object next = values.get((i + 1) % values.size());
        if (next instanceof UiPreset p) {
            o.preset = p;
            o.customPresetId = "";
        } else {
            o.preset = UiPreset.CUSTOM;
            o.customPresetId = (String) next;
        }
    }

    private static int indexOf(int[] values, int v) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == v) return i;
        }
        return 0;
    }
}
