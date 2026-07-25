package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.LogoLetters;
import com.pycodder.reanimated.anim.LogoStyle;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Анимация логотипа "Minecraft" на главном экране (эпоха 1.21.2–1.21.5 — MatrixStack,
 * blit через RenderLayer-функцию, без setShaderColor).
 *
 * Два стиля (config.logoStyle):
 *  - GROW    — родная анимация мода: логотип целиком "вырастает" с отскоком;
 *  - LETTERS — побуквенный каскад: девять букв влетают/проявляются по очереди
 *              (идея и текстуры букв — из EaseGUI, LGPLv3). В этом режиме ванильная
 *              отрисовка логотипа отменяется, буквы и edition-текст рисуем сами.
 *
 * Прозрачность передаётся ARGB-цветом в blit (setShaderColor в этой эпохе нет).
 */
@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {

    @Unique private boolean reanimated$pushed = false;

    @Unique private static Identifier[] reanimated$letters;

    /** Лениво собирает Identifier'ы букв (без статического инициализатора — безопаснее для миксина). */
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

    /** Белый цвет с заданной прозрачностью в формате ARGB. */
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

        // --- Стиль GROW ---
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

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(cx, cy, 0f);
        matrices.scale(scaleX, scaleY, 1f);
        matrices.translate(-cx, -cy, 0f);
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
        MatrixStack matrices = context.getMatrices();
        for (int i = 0; i < LogoLetters.COUNT; i++) {
            float eased = (elapsedMs == Float.MAX_VALUE) ? 1f : LogoLetters.easedFor(p, elapsedMs, i);
            float a = alpha * p.alphaAt(eased);
            boolean identity = p.identityAt(eased);

            if (!identity) {
                matrices.push();
                matrices.translate(p.offsetXAt(eased), p.offsetYAt(eased), 0f);
                matrices.translate(px, py, 0f);
                matrices.scale(p.scaleXAt(eased), p.scaleYAt(eased), 1f);
                matrices.translate(-px, -py, 0f);
            }
            context.drawTexture(RenderLayer::getGuiTextured, letters[i], boxX, boxY, 0f, 0f,
                    LogoLetters.LOGO_WIDTH, LogoLetters.LOGO_HEIGHT,
                    LogoLetters.LOGO_WIDTH, LogoLetters.LOGO_TEXTURE_HEIGHT, reanimated$argb(a));
            if (!identity) {
                matrices.pop();
            }
        }
        // edition-текст ("Java Edition")
        context.drawTexture(RenderLayer::getGuiTextured, LogoDrawer.EDITION_TEXTURE,
                screenWidth / 2 - 64, boxY + LogoLetters.LOGO_HEIGHT - 7, 0f, 0f,
                128, 14, 128, 16, reanimated$argb(alpha));
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("RETURN"))
    private void reanimated$postDraw(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.getMatrices().pop();
            reanimated$pushed = false;
        }
    }
}
