package com.pycodder.reanimated.mixin;

import org.joml.Matrix3x2fStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Логотип "Minecraft" "вырастает" с лёгким отскоком (NeoForge / Mojmap — LogoRenderer.renderLogo). */
@Mixin(LogoRenderer.class)
public class LogoDrawerMixin {

    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V", at = @At("HEAD"))
    private void reanimated$preDraw(GuiGraphics graphics, int screenWidth, float alpha, CallbackInfo ci) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.logoEnabled) {
            return;
        }

        float elapsed = Anim.elapsed(System.currentTimeMillis());
        if (elapsed == Float.MAX_VALUE) {
            return;
        }
        float p = elapsed / Math.max(0.01f, c.logoDuration);
        if (p >= 1f) {
            return;
        }

        float scaleY = c.logoEasing.apply(p);
        float scaleX = com.pycodder.reanimated.anim.Easing.outCubic(Math.min(1f, p * 1.4f));

        float cx = screenWidth / 2f;
        float cy = 50f;

        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        matrices.scale(scaleX, scaleY);
        matrices.translate(-cx, -cy);
        reanimated$pushed = true;
    }

    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V", at = @At("RETURN"))
    private void reanimated$postDraw(GuiGraphics graphics, int screenWidth, float alpha, CallbackInfo ci) {
        if (reanimated$pushed) {
            graphics.pose().popMatrix();
            reanimated$pushed = false;
        }
    }
}
