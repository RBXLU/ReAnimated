package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.pycodder.reanimated.anim.WidgetAnim;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Рамка поля ввода загорается и гаснет плавно: ваниль меняет обычную рамку на
 * подсвеченную мгновенно, здесь обычная рисуется всегда, а подсвеченная ложится
 * поверх с прозрачностью по фокусу.
 */
@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin extends AbstractWidget {

    @Shadow @Final private static WidgetSprites SPRITES;

    @Unique private final WidgetAnim.Follower reanimated$focus = new WidgetAnim.Follower();
    @Unique private float reanimated$glow = 0f;

    private TextFieldWidgetMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @ModifyArg(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
        ordinal = 0), index = 1)
    private Identifier reanimated$baseFrame(Identifier id) {
        reanimated$glow = reanimated$focus.step(isFocused() ? 1f : 0f, false);
        if (!WidgetAnim.enabled()) return id;
        return SPRITES.get(isActive(), false);
    }

    @Inject(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
        ordinal = 0, shift = At.Shift.AFTER))
    private void reanimated$glowFrame(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!WidgetAnim.enabled() || reanimated$glow < 0.01f) return;
        RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
        context.blitSprite(pipeline, SPRITES.get(isActive(), true),
            getX(), getY(), getWidth(), getHeight(), (Math.round(reanimated$glow * 255f) << 24) | 0x00FFFFFF);
    }
}
