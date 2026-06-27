package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.function.Consumer;

/** Экран настроек мода: слайдеры/переключатели для каждой анимации + ссылка на автора. */
public class ReAnimatedConfigScreen extends GameOptionsScreen {

    private static final String AUTHOR_URL = "https://modrinth.com/user/pycodder";
    private static final Text CREDIT = Text.literal("mod by @pycodder");

    public ReAnimatedConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("reanimated.config.title"));
    }

    @Override
    protected void addOptions() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        this.body.addAll(
            // --- Меню (заголовки + кнопки) ---
            toggle("reanimated.opt.screen_enabled", c.screenOpenEnabled, v -> c.screenOpenEnabled = v),
            slider("reanimated.opt.screen_duration", 0.05, 1.5, c.screenOpenDuration, " s", v -> c.screenOpenDuration = v.floatValue()),
            slider("reanimated.opt.screen_distance", 0, 80, c.screenOpenDistance, " px", v -> c.screenOpenDistance = v.floatValue()),
            easing("reanimated.opt.screen_easing", c.screenOpenEasing, v -> c.screenOpenEasing = v),

            // --- Контейнеры ---
            toggle("reanimated.opt.container_enabled", c.containerEnabled, v -> c.containerEnabled = v),
            slider("reanimated.opt.container_duration", 0.05, 1.5, c.containerDuration, " s", v -> c.containerDuration = v.floatValue()),
            slider("reanimated.opt.container_distance", 0, 120, c.containerDistance, " px", v -> c.containerDistance = v.floatValue()),
            easing("reanimated.opt.container_easing", c.containerEasing, v -> c.containerEasing = v),

            // --- Наведение на кнопку ---
            toggle("reanimated.opt.hover_enabled", c.hoverEnabled, v -> c.hoverEnabled = v),
            slider("reanimated.opt.hover_scale", 0.0, 0.3, c.hoverScale, "", v -> c.hoverScale = v.floatValue()),
            slider("reanimated.opt.hover_speed", 2, 30, c.hoverSpeed, "", v -> c.hoverSpeed = v.floatValue()),

            // --- Подсветка слота ---
            toggle("reanimated.opt.slot_enabled", c.slotHighlightEnabled, v -> c.slotHighlightEnabled = v),
            slider("reanimated.opt.slot_speed", 4, 40, c.slotHighlightSpeed, "", v -> c.slotHighlightSpeed = v.floatValue()),

            // --- Логотип ---
            toggle("reanimated.opt.logo_enabled", c.logoEnabled, v -> c.logoEnabled = v),
            slider("reanimated.opt.logo_duration", 0.1, 2.0, c.logoDuration, " s", v -> c.logoDuration = v.floatValue()),
            easing("reanimated.opt.logo_easing", c.logoEasing, v -> c.logoEasing = v)
        );
    }

    private SimpleOption<Boolean> toggle(String key, boolean current, Consumer<Boolean> setter) {
        return SimpleOption.ofBoolean(key, current, v -> {
            setter.accept(v);
            ReAnimatedConfig.get().save();
        });
    }

    private SimpleOption<Double> slider(String key, double min, double max, float current, String unit, Consumer<Double> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + String.format("%.2f", value) + unit)),
            SimpleOption.DoubleSliderCallbacks.INSTANCE.withModifier(
                d -> min + d * (max - min),
                v -> (v - min) / (max - min)
            ),
            (double) current,
            v -> {
                setter.accept(v);
                ReAnimatedConfig.get().save();
            }
        );
    }

    private SimpleOption<Integer> easing(String key, EasingType current, Consumer<EasingType> setter) {
        return new SimpleOption<>(
            key,
            SimpleOption.emptyTooltip(),
            (optionText, value) -> Text.translatable(key).append(Text.literal(": " + EasingType.values()[value].display)),
            new SimpleOption.ValidatingIntSliderCallbacks(0, EasingType.values().length - 1),
            current.ordinal(),
            v -> {
                setter.accept(EasingType.values()[v]);
                ReAnimatedConfig.get().save();
            }
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        boolean hovered = reanimated$overCredit(mouseX, mouseY);
        context.drawTextWithShadow(this.textRenderer, CREDIT, 4, this.height - 12, hovered ? 0xFF88CCFF : 0xFF5599DD);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (click.button() == 0 && reanimated$overCredit((int) click.x(), (int) click.y())) {
            Util.getOperatingSystem().open(URI.create(AUTHOR_URL));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean reanimated$overCredit(int mouseX, int mouseY) {
        int w = this.textRenderer.getWidth(CREDIT);
        return mouseX >= 4 && mouseX <= 4 + w && mouseY >= this.height - 13 && mouseY <= this.height - 1;
    }
}
