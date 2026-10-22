package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pycodder.reanimated.anim.WidgetAnim;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget {

    @Shadow @Final private static ButtonTextures TEXTURES;

    @Unique private final WidgetAnim.Follower reanimated$focus = new WidgetAnim.Follower();
    @Unique private float reanimated$glow = 0f;

    private TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
        ordinal = 0), index = 0)
    private Identifier reanimated$baseFrame(Identifier id) {
        reanimated$glow = reanimated$focus.step(isFocused() ? 1f : 0f, false);
        if (!WidgetAnim.enabled()) return id;
        return TEXTURES.get(isNarratable(), false);
    }

    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
        ordinal = 0, shift = At.Shift.AFTER))
    private void reanimated$glowFrame(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!WidgetAnim.enabled() || reanimated$glow < 0.01f) return;
        context.setShaderColor(1f, 1f, 1f, reanimated$glow);
        RenderSystem.enableBlend();
        context.drawGuiTexture(TEXTURES.get(isNarratable(), true), getX(), getY(), getWidth(), getHeight());
        context.setShaderColor(1f, 1f, 1f, 1f);
    }
}
