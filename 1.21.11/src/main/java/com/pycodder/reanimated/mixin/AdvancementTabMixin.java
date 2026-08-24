package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import org.joml.Matrix3x2fStack;
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
    private void reanimated$begin(DrawContext context) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.tabsEnabled) {
            return;
        }
        AnimProfile p = c.profileTabs;
        if (!p.enabled) {
            return;
        }
        if (!Anim.shouldAnimate(MinecraftClient.getInstance().currentScreen)) {
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

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int winY = (mc.getWindow().getScaledHeight() - REANIMATED$WINDOW_HEIGHT) / 2;

        context.enableScissor(0, 0, sw, winY);
        Matrix3x2fStack m = context.getMatrices();
        m.pushMatrix();
        m.translate(0f, dy);
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(DrawContext context) {
        if (reanimated$pushed) {
            context.getMatrices().popMatrix();
            context.disableScissor();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;IIIIZ)V", at = @At("HEAD"))
    private void reanimated$bgHead(DrawContext context, int x, int y, int w, int h, boolean selected, CallbackInfo ci) {
        reanimated$begin(context);
    }

    @Inject(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;IIIIZ)V", at = @At("RETURN"))
    private void reanimated$bgTail(DrawContext context, int x, int y, int w, int h, boolean selected, CallbackInfo ci) {
        reanimated$end(context);
    }

    @Inject(method = "drawIcon(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"))
    private void reanimated$iconHead(DrawContext context, int x, int y, CallbackInfo ci) {
        reanimated$begin(context);
    }

    @Inject(method = "drawIcon(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("RETURN"))
    private void reanimated$iconTail(DrawContext context, int x, int y, CallbackInfo ci) {
        reanimated$end(context);
    }
}
