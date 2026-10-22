package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.WidgetAnim;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
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
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {

    @Shadow protected double value;

    /** Пока с последнего перетаскивания прошло меньше этого, ручка догоняет быстрее. */
    @Unique private static final long REANIMATED$DRAG_MS = 120L;

    @Unique private final WidgetAnim.Follower reanimated$handle = new WidgetAnim.Follower();
    @Unique private long reanimated$dragTime = 0L;
    @Unique private boolean reanimated$pushed = false;

    private SliderWidgetMixin(int x, int y, int width, int height, Text message) {
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

    /** Второй вызов drawGuiTexture в renderWidget — ручка. */
    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
        ordinal = 1))
    private void reanimated$handlePush(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean dragging = System.currentTimeMillis() - reanimated$dragTime < REANIMATED$DRAG_MS;
        float shown = reanimated$handle.step((float) this.value, dragging);
        float dx = (shown - (float) this.value) * (this.width - 8);
        if (Math.abs(dx) < 0.01f) return;
        context.getMatrices().push();
        context.getMatrices().translate(dx, 0f, 0f);
        reanimated$pushed = true;
    }

    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
        ordinal = 1, shift = At.Shift.AFTER))
    private void reanimated$handlePop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.getMatrices().pop();
            reanimated$pushed = false;
        }
    }
}
