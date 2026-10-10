package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.TooltipAnim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class DrawContextTooltipMixin {

    @Unique private boolean reanimated$tooltipPushed = false;

    @Inject(method = "tooltip", at = @At("HEAD"))
    private void reanimated$tooltipHead(Font font, List<ClientTooltipComponent> components,
                                        int x, int y, ClientTooltipPositioner positioner,
                                        Identifier texture,
                                        CallbackInfo ci) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.tooltipEnabled) return;

        float p = TooltipAnim.progress(components == null ? 0 : components.size());
        if (p >= 1f) return;

        float e = c.tooltipEasing.apply(p);
        float scale = Easing.lerp(c.tooltipScale, 1f, e);
        float dy = (1f - e) * c.tooltipOffsetY;
        UiTransform.pushPopup(((GuiGraphicsExtractor) (Object) this).pose(), x, y, scale, dy);
        reanimated$tooltipPushed = true;
    }

    @Inject(method = "tooltip", at = @At("RETURN"))
    private void reanimated$tooltipTail(Font font, List<ClientTooltipComponent> components,
                                        int x, int y, ClientTooltipPositioner positioner,
                                        Identifier texture,
                                        CallbackInfo ci) {
        if (!reanimated$tooltipPushed) return;
        reanimated$tooltipPushed = false;
        UiTransform.popPopup(((GuiGraphicsExtractor) (Object) this).pose());
    }
}
