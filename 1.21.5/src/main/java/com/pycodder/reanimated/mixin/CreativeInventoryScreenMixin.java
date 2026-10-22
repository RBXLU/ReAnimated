package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import com.pycodder.reanimated.anim.PanelBounds;
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
 * Эпоха 1.21.1: 3D MatrixStack, прозрачность через setShaderColor (изолирована флашем от
 * батча предметов). Идея — из EaseGUI (Weyne1, LGPLv3), переписано под системы ReAnimated.
 */
@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {

    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "renderTabIcon(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemGroup;)V", at = @At("HEAD"))
    private void reanimated$iconHead(DrawContext context, ItemGroup group, CallbackInfo ci) {
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
        float eased = p.progress(elapsedMs, group.getColumn());
        if (p.identityAt(eased)) {
            return; // вкладка на месте
        }

        boolean top = group.getRow() == ItemGroup.Row.TOP;
        // Верхние выезжают снизу (+, всплывают), нижние — сверху (−, опускаются).
        float dy = (1f - eased) * p.offsetY;
        if (!top) dy = -dy;

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        PanelBounds panel = (PanelBounds) (Object) this;
        // Флашим уже накопленную сетку предметов ДО обрезки, иначе она попала бы под scissor.
        context.draw();
        // Обрезка «за плашкой»: показываем вкладку только вне прямоугольника панели.
        // enableScissor ваниль (1.21.4+) сама мапит текущей матрицей → ляжет по видимой панели.
        if (top) {
            context.enableScissor(0, 0, sw, panel.reanimated$panelTop());
        } else {
            context.enableScissor(0, panel.reanimated$panelBottom(), sw, sh);
        }
        MatrixStack m = context.getMatrices();
        m.push();
        m.translate(0f, dy, 0f);
        reanimated$pushed = true;
    }

    @Inject(method = "renderTabIcon(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemGroup;)V", at = @At("RETURN"))
    private void reanimated$iconTail(DrawContext context, ItemGroup group, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.draw(); // отрисовать вкладку (обрезанную) под scissor
            context.getMatrices().pop();
            context.disableScissor();
            reanimated$pushed = false;
        }
    }
}
