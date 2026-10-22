package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
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

/**
 * Плавное появление вкладок креативного инвентаря: вкладки выезжают из-за плашки по очереди.
 *
 * По ряду задаётся направление: верхние вкладки выезжают СНИЗУ (из-за верхнего края панели
 * вверх), нижние — СВЕРХУ (из-за нижнего края вниз). Пока вкладка «за плашкой», она обрезается
 * (scissor по краю панели) — иконки/фон не видны, пока не выедут. Порядок каскада — по столбцу.
 *
 * Эпоха 1.21.1–1.21.3 (NeoForge / Mojmap): 3D PoseStack, scissor кладётся по видимой панели
 * благодаря {@code DrawContextScissorMixin}. Идея — из EaseGUI (Weyne1, LGPLv3), переписано
 * под системы ReAnimated.
 */
@Mixin(CreativeModeInventoryScreen.class)
public class CreativeInventoryScreenMixin {

    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "renderTabButton(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/CreativeModeTab;)V", at = @At("HEAD"))
    private void reanimated$iconHead(GuiGraphics graphics, CreativeModeTab group, CallbackInfo ci) {
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
            return; // вкладка на месте
        }

        boolean top = group.row() == CreativeModeTab.Row.TOP;
        // Верхние выезжают снизу (+, всплывают), нижние — сверху (−, опускаются).
        float dy = (1f - eased) * p.offsetY;
        if (!top) dy = -dy;

        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        PanelBounds panel = (PanelBounds) (Object) this;
        // Флашим уже накопленную сетку предметов ДО обрезки, иначе она попала бы под scissor.
        graphics.flush();
        // Обрезка «за плашкой»: показываем вкладку только вне прямоугольника панели.
        // enableScissor мапится матрицей (DrawContextScissorMixin) → ляжет по видимой панели.
        if (top) {
            graphics.enableScissor(0, 0, sw, panel.reanimated$panelTop());
        } else {
            graphics.enableScissor(0, panel.reanimated$panelBottom(), sw, sh);
        }
        PoseStack m = graphics.pose();
        m.pushPose();
        com.pycodder.reanimated.anim.UiTransform.translate(m, 0f, dy);
        reanimated$pushed = true;
    }

    @Inject(method = "renderTabButton(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/CreativeModeTab;)V", at = @At("RETURN"))
    private void reanimated$iconTail(GuiGraphics graphics, CreativeModeTab group, CallbackInfo ci) {
        if (reanimated$pushed) {
            graphics.flush(); // отрисовать вкладку (обрезанную) под scissor
            graphics.pose().popPose();
            com.pycodder.reanimated.anim.OwnTransform.pop();
            graphics.disableScissor();
            reanimated$pushed = false;
        }
    }
}
