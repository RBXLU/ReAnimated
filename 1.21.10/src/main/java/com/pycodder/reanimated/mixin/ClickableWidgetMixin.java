package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Плавное увеличение кнопки при наведении и плавный возврат (1.21.6+ — Matrix3x2fStack).
 * Появление кнопок снизу делает ScreenMixin.
 */
@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin {

    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow public abstract boolean isHovered();

    @Unique private float reanimated$hover = 0f;
    @Unique private long reanimated$lastTime = 0L;
    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void reanimated$preRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        reanimated$pushed = false;
        if (!c.hoverEnabled) {
            return;
        }
        // Не масштабируем контейнеры-фреймы (списки серверов/миров и т.п.):
        // у них масштаб смещает клики. Только обычные кнопки/слайдеры.
        if ((Object) this instanceof net.minecraft.client.gui.widget.ContainerWidget) {
            reanimated$pushed = false;
            return;
        }

        long now = System.currentTimeMillis();
        float dt = reanimated$lastTime == 0L ? 0f : (now - reanimated$lastTime) / 1000f;
        if (dt > 0.2f) dt = 0.2f;
        reanimated$lastTime = now;

        float target = isHovered() ? 1f : 0f;
        reanimated$hover = Easing.approach(reanimated$hover, target, dt, c.hoverSpeed);

        if (reanimated$hover < 0.001f) {
            return;
        }

        float scale = 1f + c.hoverScale * reanimated$hover;
        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        matrices.scale(scale, scale);
        matrices.translate(-cx, -cy);
        reanimated$pushed = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$postRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.getMatrices().popMatrix();
            reanimated$pushed = false;
        }
    }
}
