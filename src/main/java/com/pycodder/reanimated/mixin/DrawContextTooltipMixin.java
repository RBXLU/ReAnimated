package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.TooltipAnim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
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
@Mixin(GuiGraphics.class)
public abstract class DrawContextTooltipMixin {

    /** Наложена ли трансформация в этом вызове — снимать её нужно ровно столько же раз. */
    @Unique private boolean reanimated$tooltipPushed = false;

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void reanimated$tooltipHead(Font font,
                                        List<ClientTooltipComponent> components,
                                        int x,
                                        int y,
                                        ClientTooltipPositioner positioner,
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
        UiTransform.pushPopup(((GuiGraphics) (Object) this).pose(), x, y, scale, dy);
        reanimated$tooltipPushed = true;
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void reanimated$tooltipTail(Font font,
                                        List<ClientTooltipComponent> components,
                                        int x,
                                        int y,
                                        ClientTooltipPositioner positioner,
                                        CallbackInfo ci) {
        if (!reanimated$tooltipPushed) return;
        reanimated$tooltipPushed = false;
        UiTransform.popPopup(((GuiGraphics) (Object) this).pose());
    }
}
