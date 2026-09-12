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

@Mixin(DrawContext.class)
public abstract class DrawContextTooltipMixin {

    @Unique private boolean reanimated$tooltipPushed = false;

    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V", at = @At("HEAD"))
    private void reanimated$tooltipHead(TextRenderer textRenderer, List<TooltipComponent> components,
                                        int x, int y, TooltipPositioner positioner,
                                        Identifier texture,
                                        CallbackInfo ci) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.tooltipEnabled) return;

        float p = TooltipAnim.progress(components == null ? 0 : components.size());
        if (p >= 1f) return;

        float e = c.tooltipEasing.apply(p);
        float scale = Easing.lerp(c.tooltipScale, 1f, e);
        float dy = (1f - e) * c.tooltipOffsetY;
        UiTransform.pushPopup(((DrawContext) (Object) this).getMatrices(), x, y, scale, dy);
        reanimated$tooltipPushed = true;
    }

    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V", at = @At("RETURN"))
    private void reanimated$tooltipTail(TextRenderer textRenderer, List<TooltipComponent> components,
                                        int x, int y, TooltipPositioner positioner,
                                        Identifier texture,
                                        CallbackInfo ci) {
        if (!reanimated$tooltipPushed) return;
        reanimated$tooltipPushed = false;
        UiTransform.popPopup(((DrawContext) (Object) this).getMatrices());
    }
}
