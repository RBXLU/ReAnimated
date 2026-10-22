package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.CascadeTarget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Затемнение ТЕКСТА кнопки вместе с рамкой.
 *
 * Ваниль рисует рамку через {@code setShaderColor(alpha)}, затем СБРАСЫВАЕТ
 * шейдер-цвет в непрозрачный {@code (1,1,1,1)} и рисует текст, полагаясь только
 * на вершинный alpha. В ряде сред вершинный alpha у GUI-текста не применяется —
 * рамка гаснет, а текст остаётся непрозрачным (баг «текст отстаёт от кнопок»).
 *
 * Здесь вместо сброса выставляем шейдер-цвет в alpha виджета — текст гаснет тем
 * же путём, что и рамка. При alpha=1 это ровно {@code (1,1,1,1)}, как в ванили,
 * поэтому обычные кнопки не меняются. Утечку шейдер-цвета снимает
 * {@code ClickableWidgetMixin.postRender} (сброс в {@code (1,1,1,1)}).
 */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin {

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;setShaderColor(FFFF)V", ordinal = 1))
    private void reanimated$fadeText(DrawContext context, float r, float g, float b, float a) {
        float widgetAlpha = ((CascadeTarget) (Object) this).reanimated$currentAlpha();
        context.setShaderColor(r, g, b, widgetAlpha);
    }
}
