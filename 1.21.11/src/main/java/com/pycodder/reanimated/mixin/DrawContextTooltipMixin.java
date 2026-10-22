package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.TooltipAnim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Появление всплывающей подсказки: она коротко вырастает у курсора вместо того,
 * чтобы возникать мгновенно.
 *
 * Точка входа выбрана самая нижняя: все перегрузки — от простого текста до подсказки
 * предмета, и отложенная отрисовка тоже — в итоге сходятся сюда. Один хук вместо
 * десятка, и подсказки из модов (они зовут те же методы) анимируются заодно.
 *
 * Кадр без подсказки в состоянии не отражается, поэтому «когда она появилась»
 * считает {@link TooltipAnim} по разрывам между кадрами.
 */
@Mixin(DrawContext.class)
public abstract class DrawContextTooltipMixin {

    /** Наложена ли трансформация в этом вызове — снимать её нужно ровно столько же раз. */
    @Unique private boolean reanimated$tooltipPushed = false;

    @Inject(method = "drawTooltipImmediately", at = @At("HEAD"))
    private void reanimated$tooltipHead(TextRenderer textRenderer, List<TooltipComponent> components,
                                        int x, int y, TooltipPositioner positioner,
                                        Identifier texture,
                                        CallbackInfo ci) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.tooltipEnabled || c.pvpMode) return;

        float p = TooltipAnim.progress(components == null ? 0 : components.size());
        if (p >= 1f) return; // подсказка уже «доехала» — кадр не трогаем вовсе

        float e = c.tooltipEasing.apply(p);
        float scale = Easing.lerp(c.tooltipScale, 1f, e);
        float dy = (1f - e) * c.tooltipOffsetY;
        // Масштабируем вокруг точки, к которой подсказка привязана (курсор): так она
        // вырастает от предмета, а не из своего левого верхнего угла.
        UiTransform.pushPopup(((DrawContext) (Object) this).getMatrices(), x, y, scale, dy);
        reanimated$tooltipPushed = true;
    }

    @Inject(method = "drawTooltipImmediately", at = @At("RETURN"))
    private void reanimated$tooltipTail(TextRenderer textRenderer, List<TooltipComponent> components,
                                        int x, int y, TooltipPositioner positioner,
                                        Identifier texture,
                                        CallbackInfo ci) {
        if (!reanimated$tooltipPushed) return;
        reanimated$tooltipPushed = false;
        UiTransform.popPopup(((DrawContext) (Object) this).getMatrices());
    }
}
