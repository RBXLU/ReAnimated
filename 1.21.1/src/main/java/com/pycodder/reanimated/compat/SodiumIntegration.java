package com.pycodder.reanimated.compat;

import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.anim.UiPreset;
import com.pycodder.reanimated.config.AnimProfileEditorScreen;
import com.pycodder.reanimated.config.AnimationStudioScreen;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import com.pycodder.reanimated.config.ReAnimatedConfigScreen;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Mod settings inside Sodium's own screen: a page in its left-hand list using its own controls, rather than a floating button over someone else's screen. */
public class SodiumIntegration implements ConfigEntryPoint {
    private static final ReAnimatedConfig DEFAULTS = new ReAnimatedConfig();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        ReAnimatedConfig c = ReAnimatedConfig.get();

        ModOptionsBuilder mod = builder.registerOwnModOptions()
            .setName("ReAnimated")
            .setVersion(modVersion());

        mod.addPage(general(builder, c));
        mod.addPage(menu(builder, c));
        mod.addPage(pause(builder, c));
        mod.addPage(containers(builder, c));
        mod.addPage(cursor(builder, c));
        mod.addPage(logo(builder, c));
        mod.addPage(tabs(builder, c));

        SodiumPageState.registered = true;
    }

    private static OptionPageBuilder general(ConfigBuilder b, ReAnimatedConfig c) {
        OptionGroupBuilder main = group(b, "reanimated.tab.general")
            .addOption(enumOpt(b, "ui_preset", "reanimated.opt.preset", UiPreset.class,
                enumSet(UiPreset.MAIN), p -> p.display, DEFAULTS.uiPreset,
                () -> c.uiPreset, v -> c.uiPreset = v))
            .addOption(intOpt(b, "speed_ticks", "reanimated.opt.speed_ticks", 1, 40, 1, TICKS,
                DEFAULTS.animationSpeedTicks, () -> c.animationSpeedTicks, v -> c.animationSpeedTicks = v))
            .addOption(boolOpt(b, "animate_scope", "reanimated.opt.animate_scope",
                DEFAULTS.animateModdedScreens, () -> c.animateModdedScreens, v -> c.animateModdedScreens = v))
            .addOption(boolOpt(b, "close_enabled", "reanimated.opt.close_enabled",
                DEFAULTS.closeAnimationEnabled, () -> c.closeAnimationEnabled, v -> c.closeAnimationEnabled = v))
            .addOption(boolOpt(b, "bg_fade", "reanimated.opt.bg_fade",
                DEFAULTS.bgFadeEnabled, () -> c.bgFadeEnabled, v -> c.bgFadeEnabled = v))
            .addOption(boolOpt(b, "lists_enabled", "reanimated.opt.lists_enabled",
                DEFAULTS.listsEnabled, () -> c.listsEnabled, v -> c.listsEnabled = v));

        OptionGroupBuilder tools = group(b, "reanimated.group.tools")
            .addOption(screenOpt(b, "studio", "reanimated.opt.studio",
                prev -> MinecraftClient.getInstance().setScreen(new AnimationStudioScreen(prev))))
            .addOption(screenOpt(b, "profile_editor", "reanimated.opt.profile_editor",
                prev -> MinecraftClient.getInstance().setScreen(new AnimProfileEditorScreen(prev))))
            .addOption(screenOpt(b, "own_screen", "reanimated.config.open",
                prev -> MinecraftClient.getInstance().setScreen(new ReAnimatedConfigScreen(prev))));

        return page(b, "reanimated.tab.general").addOptionGroup(main).addOptionGroup(tools);
    }

    private static OptionPageBuilder menu(ConfigBuilder b, ReAnimatedConfig c) {
        return page(b, "reanimated.tab.menu").addOptionGroup(group(b, "reanimated.tab.menu")
            .addOption(boolOpt(b, "screen_enabled", "reanimated.opt.screen_enabled",
                DEFAULTS.screenOpenEnabled, () -> c.screenOpenEnabled, v -> c.screenOpenEnabled = v))
            .addOption(intOpt(b, "screen_distance", "reanimated.opt.screen_distance", 0, 80, 1, PIXELS,
                Math.round(DEFAULTS.screenOpenDistance), () -> Math.round(c.screenOpenDistance),
                v -> c.screenOpenDistance = v))
            .addOption(easingOpt(b, "screen_easing", "reanimated.opt.screen_easing",
                DEFAULTS.screenOpenEasing, () -> c.screenOpenEasing, v -> c.screenOpenEasing = v)));
    }

    private static OptionPageBuilder pause(ConfigBuilder b, ReAnimatedConfig c) {
        return page(b, "reanimated.tab.pause").addOptionGroup(group(b, "reanimated.tab.pause")
            .addOption(boolOpt(b, "pause_enabled", "reanimated.opt.pause_enabled",
                DEFAULTS.pauseEnabled, () -> c.pauseEnabled, v -> c.pauseEnabled = v))
            .addOption(enumOpt(b, "pause_preset", "reanimated.opt.pause_preset", UiPreset.class,
                EnumSet.allOf(UiPreset.class), p -> p.display, DEFAULTS.pausePreset,
                () -> c.pausePreset, v -> c.pausePreset = v))
            .addOption(intOpt(b, "pause_speed_ticks", "reanimated.opt.pause_speed_ticks", 1, 40, 1, TICKS,
                DEFAULTS.pauseSpeedTicks, () -> c.pauseSpeedTicks, v -> c.pauseSpeedTicks = v))
            .addOption(intOpt(b, "pause_distance", "reanimated.opt.pause_distance", 0, 80, 1, PIXELS,
                Math.round(DEFAULTS.pauseDistance), () -> Math.round(c.pauseDistance),
                v -> c.pauseDistance = v))
            .addOption(easingOpt(b, "pause_easing", "reanimated.opt.pause_easing",
                DEFAULTS.pauseEasing, () -> c.pauseEasing, v -> c.pauseEasing = v)));
    }

    private static OptionPageBuilder containers(ConfigBuilder b, ReAnimatedConfig c) {
        return page(b, "reanimated.tab.containers").addOptionGroup(group(b, "reanimated.tab.containers")
            .addOption(boolOpt(b, "container_enabled", "reanimated.opt.container_enabled",
                DEFAULTS.containerEnabled, () -> c.containerEnabled, v -> c.containerEnabled = v))
            .addOption(intOpt(b, "container_distance", "reanimated.opt.container_distance", 0, 120, 1, PIXELS,
                Math.round(DEFAULTS.containerDistance), () -> Math.round(c.containerDistance),
                v -> c.containerDistance = v))
            .addOption(easingOpt(b, "container_easing", "reanimated.opt.container_easing",
                DEFAULTS.containerEasing, () -> c.containerEasing, v -> c.containerEasing = v)));
    }

    private static OptionPageBuilder cursor(ConfigBuilder b, ReAnimatedConfig c) {
        return page(b, "reanimated.tab.cursor").addOptionGroup(group(b, "reanimated.tab.cursor")
            .addOption(boolOpt(b, "hover_enabled", "reanimated.opt.hover_enabled",
                DEFAULTS.hoverEnabled, () -> c.hoverEnabled, v -> c.hoverEnabled = v))
            .addOption(intOpt(b, "hover_scale", "reanimated.opt.hover_scale", 0, 30, 1, PERCENT,
                percent(DEFAULTS.hoverScale), () -> percent(c.hoverScale), v -> c.hoverScale = v / 100f))
            .addOption(intOpt(b, "hover_speed", "reanimated.opt.hover_speed", 2, 30, 1, PLAIN,
                Math.round(DEFAULTS.hoverSpeed), () -> Math.round(c.hoverSpeed), v -> c.hoverSpeed = v))
            .addOption(boolOpt(b, "press_enabled", "reanimated.opt.press_enabled",
                DEFAULTS.pressEnabled, () -> c.pressEnabled, v -> c.pressEnabled = v))
            .addOption(intOpt(b, "press_scale", "reanimated.opt.press_scale", 0, 30, 1, PERCENT,
                percent(DEFAULTS.pressScale), () -> percent(c.pressScale), v -> c.pressScale = v / 100f))
            .addOption(intOpt(b, "press_duration", "reanimated.opt.press_duration", 50, 600, 10, MILLIS,
                millis(DEFAULTS.pressDuration), () -> millis(c.pressDuration), v -> c.pressDuration = v / 1000f))
            .addOption(boolOpt(b, "slot_enabled", "reanimated.opt.slot_enabled",
                DEFAULTS.slotHighlightEnabled, () -> c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v))
            .addOption(intOpt(b, "slot_speed", "reanimated.opt.slot_speed", 4, 40, 1, PLAIN,
                Math.round(DEFAULTS.slotHighlightSpeed), () -> Math.round(c.slotHighlightSpeed),
                v -> c.slotHighlightSpeed = v)));
    }

    private static OptionPageBuilder logo(ConfigBuilder b, ReAnimatedConfig c) {
        OptionGroupBuilder grow = group(b, "reanimated.tab.logo")
            .addOption(boolOpt(b, "logo_enabled", "reanimated.opt.logo_enabled",
                DEFAULTS.logoEnabled, () -> c.logoEnabled, v -> c.logoEnabled = v))
            .addOption(enumOpt(b, "logo_style", "reanimated.opt.logo_style", LogoStyle.class,
                EnumSet.allOf(LogoStyle.class), s -> s.display, DEFAULTS.logoStyle,
                () -> c.logoStyle, v -> c.logoStyle = v))
            .addOption(intOpt(b, "logo_duration", "reanimated.opt.logo_duration", 100, 2000, 50, MILLIS,
                millis(DEFAULTS.logoDuration), () -> millis(c.logoDuration), v -> c.logoDuration = v / 1000f))
            .addOption(easingOpt(b, "logo_easing", "reanimated.opt.logo_easing",
                DEFAULTS.logoEasing, () -> c.logoEasing, v -> c.logoEasing = v));

        OptionGroupBuilder letters = group(b, "reanimated.group.letters")
            .addOption(intOpt(b, "logo_letter_duration", "reanimated.opt.logo_letter_duration", 50, 1200, 10, MILLIS,
                DEFAULTS.profileLogo.durationMs, () -> c.profileLogo.durationMs, v -> c.profileLogo.durationMs = v))
            .addOption(intOpt(b, "logo_letter_delay", "reanimated.opt.logo_letter_delay", 0, 300, 5, MILLIS,
                DEFAULTS.profileLogo.cascadeDelayMs, () -> c.profileLogo.cascadeDelayMs,
                v -> c.profileLogo.cascadeDelayMs = v))
            .addOption(intOpt(b, "logo_letter_offset", "reanimated.opt.logo_letter_offset", -60, 60, 1, PIXELS,
                Math.round(DEFAULTS.profileLogo.offsetY), () -> Math.round(c.profileLogo.offsetY),
                v -> c.profileLogo.offsetY = v))
            .addOption(enumOpt(b, "logo_letter_order", "reanimated.opt.logo_letter_order", CascadeOrder.class,
                EnumSet.allOf(CascadeOrder.class), o -> o.display, DEFAULTS.profileLogo.cascadeOrder,
                () -> c.profileLogo.cascadeOrder, v -> c.profileLogo.cascadeOrder = v))
            .addOption(easingOpt(b, "logo_letter_easing", "reanimated.opt.logo_letter_easing",
                DEFAULTS.profileLogo.easing, () -> c.profileLogo.easing, v -> c.profileLogo.easing = v));

        return page(b, "reanimated.tab.logo").addOptionGroup(grow).addOptionGroup(letters);
    }

    private static OptionPageBuilder tabs(ConfigBuilder b, ReAnimatedConfig c) {
        return page(b, "reanimated.tab.tabs").addOptionGroup(group(b, "reanimated.tab.tabs")
            .addOption(boolOpt(b, "tabs_enabled", "reanimated.opt.tabs_enabled",
                DEFAULTS.tabsEnabled, () -> c.tabsEnabled, v -> c.tabsEnabled = v))
            .addOption(intOpt(b, "tabs_duration", "reanimated.opt.tabs_duration", 50, 1200, 10, MILLIS,
                DEFAULTS.profileTabs.durationMs, () -> c.profileTabs.durationMs, v -> c.profileTabs.durationMs = v))
            .addOption(intOpt(b, "tabs_delay", "reanimated.opt.tabs_delay", 0, 300, 5, MILLIS,
                DEFAULTS.profileTabs.cascadeDelayMs, () -> c.profileTabs.cascadeDelayMs,
                v -> c.profileTabs.cascadeDelayMs = v))
            .addOption(intOpt(b, "tabs_offset", "reanimated.opt.tabs_offset", -120, 120, 1, PIXELS,
                Math.round(DEFAULTS.profileTabs.offsetY), () -> Math.round(c.profileTabs.offsetY),
                v -> c.profileTabs.offsetY = v))
            .addOption(easingOpt(b, "tabs_easing", "reanimated.opt.tabs_easing",
                DEFAULTS.profileTabs.easing, () -> c.profileTabs.easing, v -> c.profileTabs.easing = v)));
    }

    private static OptionPageBuilder page(ConfigBuilder b, String key) {
        return b.createOptionPage().setName(Text.translatable(key));
    }

    private static OptionGroupBuilder group(ConfigBuilder b, String key) {
        return b.createOptionGroup().setName(Text.translatable(key));
    }

    private static OptionBuilder boolOpt(ConfigBuilder b, String id, String key,
                                         boolean def, BooleanSupplier get, Consumer<Boolean> set) {
        return b.createBooleanOption(id(id))
            .setName(Text.translatable(key))
            .setTooltip(tip(id))
            .setDefaultValue(def)
            .setBinding(set::accept, get::getAsBoolean)
            .setStorageHandler(SodiumIntegration::save);
    }

    private static OptionBuilder intOpt(ConfigBuilder b, String id, String key,
                                        int min, int max, int step, ControlValueFormatter fmt,
                                        int def, IntSupplier get, IntConsumer set) {
        return b.createIntegerOption(id(id))
            .setName(Text.translatable(key))
            .setTooltip(tip(id))
            .setRange(min, max, step)
            .setValueFormatter(fmt)
            .setDefaultValue(def)
            .setBinding(set::accept, get::getAsInt)
            .setStorageHandler(SodiumIntegration::save);
    }

    private static <E extends Enum<E>> OptionBuilder enumOpt(ConfigBuilder b, String id, String key,
                                                             Class<E> type, Set<E> allowed,
                                                             java.util.function.Function<E, String> display,
                                                             E def, Supplier<E> get, Consumer<E> set) {
        return b.createEnumOption(id(id), type)
            .setAllowedValues(allowed)
            .setElementNameProvider(v -> Text.literal(display.apply(v)))
            .setName(Text.translatable(key))
            .setTooltip(tip(id))
            .setDefaultValue(def)
            .setBinding(set::accept, get::get)
            .setStorageHandler(SodiumIntegration::save);
    }

    private static OptionBuilder easingOpt(ConfigBuilder b, String id, String key,
                                           EasingType def, Supplier<EasingType> get, Consumer<EasingType> set) {
        return enumOpt(b, id, key, EasingType.class, EnumSet.allOf(EasingType.class),
            e -> e.display, def, get, set);
    }

    private static OptionBuilder screenOpt(ConfigBuilder b, String id, String key, Consumer<Screen> open) {
        return b.createExternalButtonOption(id(id))
            .setName(Text.translatable(key))
            .setTooltip(tip(id))
            .setScreenConsumer(open::accept);
    }

    private static Text tip(String id) {
        return Text.translatable("reanimated.tip." + id);
    }

    private static Identifier id(String path) {
        return Identifier.of("reanimated", path);
    }

    private static String modVersion() {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
            .getModContainer("reanimated")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("");
    }

    private static void save() {
        ReAnimatedConfig.get().save();
    }

    private static <E extends Enum<E>> Set<E> enumSet(E[] values) {
        Set<E> set = EnumSet.noneOf(values[0].getDeclaringClass());
        java.util.Collections.addAll(set, values);
        return set;
    }

    private static int percent(float v) {
        return Math.round(v * 100f);
    }

    private static int millis(float seconds) {
        return Math.round(seconds * 1000f);
    }

    private static final ControlValueFormatter PLAIN = v -> Text.literal(Integer.toString(v));
    private static final ControlValueFormatter PIXELS = v -> Text.literal(v + " px");
    private static final ControlValueFormatter MILLIS = v -> Text.literal(v + " ms");
    private static final ControlValueFormatter PERCENT = v -> Text.literal(v + "%");
    private static final ControlValueFormatter TICKS = v -> Text.literal(v + " t");
}
