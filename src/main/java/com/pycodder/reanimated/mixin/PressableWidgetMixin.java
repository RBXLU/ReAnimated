package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pycodder.reanimated.anim.CascadeTarget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Затемнение ТЕКСТА кнопки вместе с рамкой.
 *
 * Рамку ваниль рисует с явным ARGB-цветом (alpha уже внутри), а ТЕКСТ — цветом,
 * у которого alpha в старших битах. В этой среде альфа текста фактически не
 * применяется: рамка гаснет, текст остаётся непрозрачным («текст отстаёт»).
 *
 * Здесь на время отрисовки текста выставляем шейдер-цвет в alpha виджета —
 * текст гаснет тем же путём, что и рамка, — и сразу возвращаем обратно.
 * В 1.21.2+ обёртки {@code DrawContext.setShaderColor} нет, поэтому зовём
 * {@link RenderSystem} напрямую. При alpha=1 ничего не трогаем вовсе.
 */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin {

    @Inject(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/widget/PressableWidget;drawMessage("
               + "Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"))
    private void reanimated$fadeTextBegin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float a = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        if (a < 1f) {
            RenderSystem.setShaderColor(1f, 1f, 1f, a);
        }
    }

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void reanimated$fadeTextEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float a = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        if (a < 1f) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }
}
