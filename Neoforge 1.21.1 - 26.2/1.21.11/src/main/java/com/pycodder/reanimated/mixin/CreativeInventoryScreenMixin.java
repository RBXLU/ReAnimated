package com.pycodder.reanimated.mixin;

import org.joml.Matrix3x2fStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.PanelBounds;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Creative inventory tabs fade in: tabs slide out from behind the panel one by one. */
@Mixin(CreativeModeInventoryScreen.class)
public class CreativeInventoryScreenMixin {
    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "renderTabButton(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/item/CreativeModeTab;)V", at = @At("HEAD"))
    private void reanimated$iconHead(GuiGraphics graphics, int mouseX, int mouseY, CreativeModeTab group, CallbackInfo ci) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.tabsEnabled) {
            return;
        }
        AnimProfile p = c.profileTabs;
        if (!p.enabled) {
            return;
        }
        if (!Anim.shouldAnimate(Minecraft.getInstance().screen)) {
            return;
        }
        float e = Anim.elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) {
            return;
        }
        float elapsedMs = e * 1000f;
        float eased = p.progress(elapsedMs, group.column());
        if (p.identityAt(eased)) {
            return;
        }

        boolean top = group.row() == CreativeModeTab.Row.TOP;
        float dy = (1f - eased) * p.offsetY;
        if (!top) dy = -dy;

        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        PanelBounds panel = (PanelBounds) (Object) this;
        if (top) {
            graphics.enableScissor(0, 0, sw, panel.reanimated$panelTop());
        } else {
            graphics.enableScissor(0, panel.reanimated$panelBottom(), sw, sh);
        }
        Matrix3x2fStack m = graphics.pose();
        m.pushMatrix();
        m.translate(0f, dy);
        reanimated$pushed = true;
    }

    @Inject(method = "renderTabButton(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/item/CreativeModeTab;)V", at = @At("RETURN"))
    private void reanimated$iconTail(GuiGraphics graphics, int mouseX, int mouseY, CreativeModeTab group, CallbackInfo ci) {
        if (reanimated$pushed) {
            graphics.pose().popMatrix();
            graphics.disableScissor();
            reanimated$pushed = false;
        }
    }
}
