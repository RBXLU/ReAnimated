package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.LogoLetters;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** "Minecraft" logo animation on the title screen (1.21.6–1.21.11 era: Matrix3x2fStack). */
@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {
    @Unique private boolean reanimated$pushed = false;

    @Unique private static Identifier[] reanimated$letters;

    @Unique
    private static Identifier[] reanimated$letters() {
        Identifier[] a = reanimated$letters;
        if (a == null) {
            a = new Identifier[LogoLetters.COUNT];
            for (int i = 0; i < LogoLetters.COUNT; i++) {
                a[i] = Identifier.of("reanimated", "textures/gui/title/letters/" + LogoLetters.FILES[i] + ".png");
            }
            reanimated$letters = a;
        }
        return a;
    }

    @Unique
    private static int reanimated$argb(float alpha) {
        int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f);
        return (a << 24) | 0xFFFFFF;
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("HEAD"), cancellable = true)
    private void reanimated$preDraw(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.logoEnabled) {
            return;
        }

        if (c.logoStyle == LogoStyle.LETTERS) {
            reanimated$drawLetters(context, screenWidth, alpha, c);
            ci.cancel();
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

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        matrices.scale(scaleX, scaleY);
        matrices.translate(-cx, -cy);
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$drawLetters(DrawContext context, int screenWidth, float alpha, ReAnimatedConfig c) {
        AnimProfile p = c.profileLogo;
        float e = Anim.elapsed(System.currentTimeMillis());
        float elapsedMs = (e == Float.MAX_VALUE) ? Float.MAX_VALUE : e * 1000f;

        int boxX = screenWidth / 2 - LogoLetters.LOGO_WIDTH / 2;
        int boxY = LogoLetters.LOGO_BASE_Y;
        float px = boxX + LogoLetters.LOGO_WIDTH * p.pivot.fx;
        float py = boxY + LogoLetters.LOGO_HEIGHT * p.pivot.fy;

        Identifier[] letters = reanimated$letters();
        Matrix3x2fStack matrices = context.getMatrices();
        for (int i = 0; i < LogoLetters.COUNT; i++) {
            float eased = (elapsedMs == Float.MAX_VALUE) ? 1f : LogoLetters.easedFor(p, elapsedMs, i);
            float a = alpha * p.alphaAt(eased);
            boolean identity = p.identityAt(eased);

            if (!identity) {
                matrices.pushMatrix();
                matrices.translate(p.offsetXAt(eased), p.offsetYAt(eased));
                matrices.translate(px, py);
                matrices.scale(p.scaleXAt(eased), p.scaleYAt(eased));
                matrices.translate(-px, -py);
            }
            context.drawTexture(RenderPipelines.GUI_TEXTURED, letters[i], boxX, boxY, 0f, 0f,
                    LogoLetters.LOGO_WIDTH, LogoLetters.LOGO_HEIGHT,
                    LogoLetters.LOGO_WIDTH, LogoLetters.LOGO_TEXTURE_HEIGHT, reanimated$argb(a));
            if (!identity) {
                matrices.popMatrix();
            }
        }
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LogoDrawer.EDITION_TEXTURE,
                screenWidth / 2 - 64, boxY + LogoLetters.LOGO_HEIGHT - 7, 0f, 0f,
                128, 14, 128, 16, reanimated$argb(alpha));
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("RETURN"))
    private void reanimated$postDraw(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.getMatrices().popMatrix();
            reanimated$pushed = false;
        }
    }
}
