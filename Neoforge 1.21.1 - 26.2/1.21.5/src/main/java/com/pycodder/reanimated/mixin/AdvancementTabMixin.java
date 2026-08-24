package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Advancements screen tabs fade in: tabs slide out from behind the window one by one. */
@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {
    @Unique private static final int REANIMATED$WINDOW_HEIGHT = 140;

    @Shadow public abstract int getIndex();

    @Unique private boolean reanimated$pushed = false;

    @Unique
    private void reanimated$begin(GuiGraphics graphics) {
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
        float eased = p.progress(elapsedMs, getIndex());
        if (p.identityAt(eased)) {
            return;
        }

        float dy = (1f - eased) * p.offsetY;

        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int winY = (mc.getWindow().getGuiScaledHeight() - REANIMATED$WINDOW_HEIGHT) / 2;

        graphics.flush();
        graphics.enableScissor(0, 0, sw, winY);
        PoseStack m = graphics.pose();
        m.pushPose();
        m.translate(0f, dy, 0f);
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(GuiGraphics graphics) {
        if (reanimated$pushed) {
            graphics.flush();
            graphics.pose().popPose();
            graphics.disableScissor();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "drawTab(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphics graphics, int x, int y, boolean selected, CallbackInfo ci) {
        reanimated$begin(graphics);
    }

    @Inject(method = "drawTab(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphics graphics, int x, int y, boolean selected, CallbackInfo ci) {
        reanimated$end(graphics);
    }

    @Inject(method = "drawIcon(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("HEAD"))
    private void reanimated$iconHead(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        reanimated$begin(graphics);
    }

    @Inject(method = "drawIcon(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("RETURN"))
    private void reanimated$iconTail(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        reanimated$end(graphics);
    }
}
