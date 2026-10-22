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

/**
 * Плавное появление вкладок экрана достижений: вкладки выезжают из-за окна по очереди.
 *
 * Вкладки достижений — это верхний ряд (над окном): выезжают СНИЗУ (из-за верхнего края окна
 * вверх). Пока вкладка «за окном», она обрезается (scissor по верхнему краю окна) — фон/иконка
 * не видны, пока не выедут. (Тип вкладки {@code AdvancementTabType} package-private и недоступен,
 * поэтому все вкладки трактуем как верхние — это и есть обычный случай.)
 *
 * Оборачиваем обе точки отрисовки КНОПКИ-вкладки — фон ({@code drawBackground}) и иконку
 * ({@code drawIcon}) — одним сдвигом по её {@code getIndex()}. Эффект «появления» даёт обрезка
 * за окном, без альфы (код одинаков на всех эпохах). Идея — из EaseGUI (Weyne1, LGPLv3).
 */
@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {

    /** Высота окна достижений (AdvancementsScreen.WINDOW_HEIGHT). */
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
            return; // вкладка на месте
        }

        // Верхний ряд: выезжают снизу (+), поднимаясь на место.
        float dy = (1f - eased) * p.offsetY;

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int winY = (mc.getWindow().getScaledHeight() - REANIMATED$WINDOW_HEIGHT) / 2; // окно центрировано

        // Обрезка по верхнему краю окна: вкладку видно только над окном.
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
