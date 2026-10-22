package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.WidgetAnim;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ручка ползунка догоняет значение, а не перескакивает к месту клика. Пока ползунок
 * тянут мышью, она догоняет быстрее — иначе ощущается вязкость.
 */
@Mixin(AbstractSliderButton.class)
public abstract class SliderWidgetMixin extends AbstractWidget {

    @Shadow protected double value;

    /** Пока с последнего перетаскивания прошло меньше этого, ручка догоняет быстрее. */
    @Unique private static final long REANIMATED$DRAG_MS = 120L;

    @Unique private final WidgetAnim.Follower reanimated$handle = new WidgetAnim.Follower();
    @Unique private long reanimated$dragTime = 0L;
    @Unique private boolean reanimated$pushed = false;

    private SliderWidgetMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /**
     * Ползунок тянут мышью. Аргументы не нужны, поэтому их нет: так обработчик один и
     * тот же для старой сигнатуры onDrag (четыре double) и новой (Click и два double).
     */
    @Inject(method = "onDrag", at = @At("HEAD"))
    private void reanimated$onDrag(CallbackInfo ci) {
        reanimated$dragTime = System.currentTimeMillis();
    }

    /** Второй вызов blitSprite в renderWidget — ручка. */
    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V",
        ordinal = 1))
    private void reanimated$handlePush(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean dragging = System.currentTimeMillis() - reanimated$dragTime < REANIMATED$DRAG_MS;
        float shown = reanimated$handle.step((float) this.value, dragging);
        float dx = (shown - (float) this.value) * (this.width - 8);
        if (Math.abs(dx) < 0.01f) return;
        context.pose().pushMatrix();
        context.pose().translate(dx, 0f);
        reanimated$pushed = true;
    }

    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V",
        ordinal = 1, shift = At.Shift.AFTER))
    private void reanimated$handlePop(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.pose().popMatrix();
            reanimated$pushed = false;
        }
    }
}
