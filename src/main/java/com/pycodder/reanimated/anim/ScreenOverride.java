package com.pycodder.reanimated.anim;

/**
 * Настройки одного конкретного экрана или всех экранов одного мода — то, что
 * перекрывает общие значения. Хранится в конфиге по полному имени класса экрана
 * (или по {@code mod:<id>}), редактируется в {@code ScreenSettingsScreen}.
 *
 * Значения «как общее» выражены нейтральными величинами ({@link Toggle#INHERIT},
 * {@link UiPreset#INHERIT}, {@code speedTicks == 0}, {@code distance < 0}, null), поэтому
 * пустой объект не меняет поведение и его не приходится отличать от отсутствующего.
 */
public class ScreenOverride {

    /**
     * Выключатель из конфигов до 1.8: false — экран не анимируется. Читается только
     * при загрузке и переносится в {@link #animation}.
     */
    public boolean animate = true;

    /** Анимировать ли экран: OFF — ни пресета, ни каскада; ON — даже если общие настройки против. */
    public Toggle animation = Toggle.INHERIT;

    /** Пресет только для этого экрана. INHERIT — брать общий. */
    public UiPreset preset = UiPreset.INHERIT;

    /** Скорость в тиках только для этого экрана. 0 — брать общую. */
    public int speedTicks = 0;

    /** Анимация из расширения для этого экрана — когда {@link #preset} равен CUSTOM. */
    public String customPresetId = "";

    /** Дистанция выезда, px. Меньше нуля — брать общую. */
    public float distance = -1f;

    /** Траектория появления. null — брать общую. */
    public EasingType easing = null;

    /** Анимация закрытия этого экрана. */
    public Toggle close = Toggle.INHERIT;

    /** Плавное затемнение фона за этим экраном. */
    public Toggle background = Toggle.INHERIT;

    /** Переходы на этот экран и с него. */
    public Toggle transitions = Toggle.INHERIT;

    /**
     * Свой профиль появления кнопок. null — общий; профиль с {@code enabled = false}
     * выключает каскад кнопок только на этом экране.
     */
    public AnimProfile profile = null;

    /** Понятное имя для списка: заголовок экрана или название мода. */
    public String name = "";

    /** Ничего не переопределяет — такой объект можно не хранить в конфиге. */
    public boolean isDefault() {
        return animate && (animation == null || animation == Toggle.INHERIT)
            && (preset == null || preset == UiPreset.INHERIT) && speedTicks <= 0
            && (customPresetId == null || customPresetId.isEmpty())
            && distance < 0f && easing == null
            && isInherit(close) && isInherit(background) && isInherit(transitions)
            && profile == null;
    }

    private static boolean isInherit(Toggle t) {
        return t == null || t == Toggle.INHERIT;
    }

    public void sanitize() {
        if (animation == null) animation = Toggle.INHERIT;
        if (!animate) {
            animation = Toggle.OFF;
            animate = true;
        }
        if (preset == null) preset = UiPreset.INHERIT;
        if (customPresetId == null) customPresetId = "";
        if (speedTicks < 0) speedTicks = 0;
        if (speedTicks > 40) speedTicks = 40;
        if (distance > 120f) distance = 120f;
        if (Float.isNaN(distance)) distance = -1f;
        if (close == null) close = Toggle.INHERIT;
        if (background == null) background = Toggle.INHERIT;
        if (transitions == null) transitions = Toggle.INHERIT;
        if (profile != null) profile.sanitize();
        if (name == null) name = "";
    }

    /**
     * Настройки экрана поверх настроек его мода: каждое значение берётся у экрана,
     * а там, где у него «как общее», — у мода.
     */
    public static ScreenOverride merge(ScreenOverride screen, ScreenOverride mod) {
        if (mod == null) return screen;
        if (screen == null) return mod;
        ScreenOverride o = new ScreenOverride();
        o.animation = Toggle.or(screen.animation, mod.animation);
        boolean ownPreset = screen.preset != null && screen.preset != UiPreset.INHERIT;
        o.preset = ownPreset ? screen.preset : mod.preset;
        o.customPresetId = ownPreset ? screen.customPresetId : mod.customPresetId;
        o.speedTicks = screen.speedTicks > 0 ? screen.speedTicks : mod.speedTicks;
        o.distance = screen.distance >= 0f ? screen.distance : mod.distance;
        o.easing = screen.easing != null ? screen.easing : mod.easing;
        o.close = Toggle.or(screen.close, mod.close);
        o.background = Toggle.or(screen.background, mod.background);
        o.transitions = Toggle.or(screen.transitions, mod.transitions);
        o.profile = screen.profile != null ? screen.profile : mod.profile;
        o.name = screen.name;
        return o;
    }
}
