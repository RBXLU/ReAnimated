package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.WidgetAnim;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Квадрат чекбокса при переключении коротко отскакивает от уменьшенного размера. */
@Mixin(Checkbox.class)
public abstract class CheckboxWidgetMixin extends AbstractWidget {

    @Unique private long reanimated$toggled = 0L;
    @Unique private boolean reanimated$pushed = false;

    private CheckboxWidgetMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "onPress", at = @At("RETURN"))
    private void reanimated$onToggle(CallbackInfo ci) {
        reanimated$toggled = System.currentTimeMillis();
    }

    /** Первый вызов blitSprite в renderWidget — сам квадрат с отметкой. */
    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
        ordinal = 0))
    private void reanimated$boxPush(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$toggled == 0L) return;
        float s = WidgetAnim.checkScale(System.currentTimeMillis() - reanimated$toggled);
        if (s == 1f) {
            reanimated$toggled = 0L;
            return;
        }
        float half = this.height / 2f;
        float cx = getX() + half, cy = getY() + half;
        context.pose().pushPose();
        context.pose().translate(cx, cy, 0f);
        context.pose().scale(s, s, 1f);
        context.pose().translate(-cx, -cy, 0f);
        reanimated$pushed = true;
    }

    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
        ordinal = 0, shift = At.Shift.AFTER))
    private void reanimated$boxPop(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.pose().popPose();
            reanimated$pushed = false;
        }
    }
}
