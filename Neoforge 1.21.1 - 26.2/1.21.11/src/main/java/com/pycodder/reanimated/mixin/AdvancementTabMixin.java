package com.pycodder.reanimated.mixin;

import org.joml.Matrix3x2fStack;
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

/**
 * Плавное появление вкладок экрана достижений: вкладки выезжают из-за окна по очереди.
 *
 * Вкладки достижений — это верхний ряд (над окном): выезжают СНИЗУ (из-за верхнего края окна
 * вверх). Пока вкладка «за окном», она обрезается (scissor по верхнему краю окна) — фон/иконка
 * не видны, пока не выедут. (Тип вкладки {@code AdvancementTabType} package-private и недоступен,
 * поэтому все вкладки трактуем как верхние — это и есть обычный случай.)
 *
 * Оборачиваем обе точки отрисовки КНОПКИ-вкладки — фон ({@code drawTab}) и иконку
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
            return; // вкладка на месте
        }

        // Верхний ряд: выезжают снизу (+), поднимаясь на место.
        float dy = (1f - eased) * p.offsetY;

        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int winY = (mc.getWindow().getGuiScaledHeight() - REANIMATED$WINDOW_HEIGHT) / 2; // окно центрировано

        // Обрезка по верхнему краю окна: вкладку видно только над окном.
        graphics.enableScissor(0, 0, sw, winY);
        Matrix3x2fStack m = graphics.pose();
        m.pushMatrix();
        m.translate(0f, dy);
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(GuiGraphics graphics) {
        if (reanimated$pushed) {
            graphics.pose().popMatrix();
            graphics.disableScissor();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "drawTab(Lnet/minecraft/client/gui/GuiGraphics;IIIIZ)V", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphics graphics, int x, int y, int w, int h, boolean selected, CallbackInfo ci) {
        reanimated$begin(graphics);
    }

    @Inject(method = "drawTab(Lnet/minecraft/client/gui/GuiGraphics;IIIIZ)V", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphics graphics, int x, int y, int w, int h, boolean selected, CallbackInfo ci) {
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
