package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Логотип "Minecraft" красиво "вырастает" с лёгким отскоком при запуске игры. */
@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {

    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("HEAD"))
    private void reanimated$preDraw(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {
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
            return; // анимация закончилась — без трансформаций
        }

        float scaleY = c.logoEasing.apply(p);
        float scaleX = com.pycodder.reanimated.anim.Easing.outCubic(Math.min(1f, p * 1.4f));

        float cx = screenWidth / 2f;
        float cy = 50f;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(cx, cy, 0f);
        matrices.scale(scaleX, scaleY, 1f);
        matrices.translate(-cx, -cy, 0f);
        reanimated$pushed = true;
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("RETURN"))
    private void reanimated$postDraw(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.getMatrices().pop();
            reanimated$pushed = false;
        }
    }
}
