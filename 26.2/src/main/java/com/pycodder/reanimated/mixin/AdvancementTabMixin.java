package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Плавное появление вкладок достижений (Minecraft 26.x — render-state extraction).
 *
 * Верхний ряд: вкладки выезжают снизу из-за верхнего края окна; пока «за окном» — обрезаются
 * scissor. Оборачиваем {@code extractTab} (фон) и {@code extractIcon} (иконку) одним сдвигом по
 * {@code getIndex()}. Тип вкладки (AdvancementTabType) недоступен, поэтому все вкладки — верхние
 * (обычный случай). Флаш не нужен: в extraction-модели scissor записывается на элемент.
 */
@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {

    /** Высота окна достижений (AdvancementsScreen.WINDOW_HEIGHT). */
    @Unique private static final int REANIMATED$WINDOW_HEIGHT = 140;

    @Shadow public abstract int getIndex();

    @Unique private boolean reanimated$pushed = false;

    @Unique
    private void reanimated$begin(GuiGraphicsExtractor context) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.tabsEnabled) {
            return;
        }
        AnimProfile p = c.profileTabs;
        if (!p.enabled) {
            return;
        }
        float e = Anim.elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) {
            return;
        }
        float eased = p.progress(e * 1000f, getIndex());
        if (p.identityAt(eased)) {
            return;
        }
        float dy = (1f - eased) * p.offsetY;

        int sw = context.guiWidth();
        int winY = (context.guiHeight() - REANIMATED$WINDOW_HEIGHT) / 2; // окно центрировано
        // Окно во весь экран — полосы над ним нет; scissor нулевой высоты падает
        // "Scissor size must be >0", поэтому просто не анимируем вкладку.
        if (sw <= 0 || winY <= 0) {
            return;
        }
        context.enableScissor(0, 0, sw, winY);
        Matrix3x2fStack m = context.pose();
        m.pushMatrix();
        m.translate(0f, dy);
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(GuiGraphicsExtractor context) {
        if (reanimated$pushed) {
            context.pose().popMatrix();
            context.disableScissor();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "extractTab(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIZ)V", at = @At("HEAD"))
    private void reanimated$tabHead(GuiGraphicsExtractor context, int x, int y, int w, int h, boolean selected, CallbackInfo ci) {
        reanimated$begin(context);
    }

    @Inject(method = "extractTab(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIZ)V", at = @At("RETURN"))
    private void reanimated$tabTail(GuiGraphicsExtractor context, int x, int y, int w, int h, boolean selected, CallbackInfo ci) {
        reanimated$end(context);
    }

    @Inject(method = "extractIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", at = @At("HEAD"))
    private void reanimated$iconHead(GuiGraphicsExtractor context, int x, int y, CallbackInfo ci) {
        reanimated$begin(context);
    }

    @Inject(method = "extractIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", at = @At("RETURN"))
    private void reanimated$iconTail(GuiGraphicsExtractor context, int x, int y, CallbackInfo ci) {
        reanimated$end(context);
    }
}
