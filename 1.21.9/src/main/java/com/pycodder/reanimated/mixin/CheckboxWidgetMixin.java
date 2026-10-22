package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.WidgetAnim;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Квадрат чекбокса при переключении коротко отскакивает от уменьшенного размера. */
@Mixin(CheckboxWidget.class)
public abstract class CheckboxWidgetMixin extends ClickableWidget {

    @Unique private long reanimated$toggled = 0L;
    @Unique private boolean reanimated$pushed = false;

    private CheckboxWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "onPress", at = @At("RETURN"))
    private void reanimated$onToggle(AbstractInput input, CallbackInfo ci) {
        reanimated$toggled = System.currentTimeMillis();
    }

    /** Первый вызов drawGuiTexture в renderWidget — сам квадрат с отметкой. */
    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V",
        ordinal = 0))
    private void reanimated$boxPush(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$toggled == 0L) return;
        float s = WidgetAnim.checkScale(System.currentTimeMillis() - reanimated$toggled);
        if (s == 1f) {
            reanimated$toggled = 0L;
            return;
        }
        float half = this.height / 2f;
        float cx = getX() + half, cy = getY() + half;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cx, cy);
        context.getMatrices().scale(s, s);
        context.getMatrices().translate(-cx, -cy);
        reanimated$pushed = true;
    }

    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V",
        ordinal = 0, shift = At.Shift.AFTER))
    private void reanimated$boxPop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.getMatrices().popMatrix();
            reanimated$pushed = false;
        }
    }
}
