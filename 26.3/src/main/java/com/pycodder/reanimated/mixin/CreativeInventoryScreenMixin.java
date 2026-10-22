package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.PanelBounds;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Плавное появление вкладок креативного инвентаря (Minecraft 26.x — render-state extraction).
 *
 * Верхние вкладки выезжают снизу, нижние — сверху; пока вкладка «за плашкой», её обрезает
 * scissor по краю панели. Порядок каскада — по столбцу. В extraction-модели scissor и матрица
 * записываются на каждый элемент, поэтому флаш не нужен (в отличие от 1.21.1). Границы панели —
 * через {@link PanelBounds} (реализует {@code HandledScreenMixin} на {@code AbstractContainerScreen}).
 */
@Mixin(CreativeModeInventoryScreen.class)
public class CreativeInventoryScreenMixin {

    @Unique private boolean reanimated$pushed = false;

    @Inject(method = "extractTabButton(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/item/CreativeModeTab;)V", at = @At("HEAD"))
    private void reanimated$tabHead(GuiGraphicsExtractor context, int mouseX, int mouseY, CreativeModeTab tab, CallbackInfo ci) {
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
        float eased = p.progress(e * 1000f, tab.column());
        if (p.identityAt(eased)) {
            return;
        }
        boolean top = tab.row() == CreativeModeTab.Row.TOP;
        float dy = (1f - eased) * p.offsetY;
        if (!top) dy = -dy;

        int sw = context.guiWidth();
        int sh = context.guiHeight();
        PanelBounds panel = (PanelBounds) (Object) this;
        // Полоса за краем панели, в которой вкладке разрешено показываться.
        int clipTop = top ? 0 : panel.reanimated$panelBottom();
        int clipBottom = top ? panel.reanimated$panelTop() : sh;
        // Пустая или вывернутая полоса (панель прижата к краю экрана) — scissor такое не принимает
        // и падает "Scissor size must be >0". Тогда просто не анимируем вкладку.
        if (sw <= 0 || clipBottom <= clipTop) {
            return;
        }
        context.enableScissor(0, clipTop, sw, clipBottom);
        Matrix3x2fStack m = context.pose();
        m.pushMatrix();
        m.translate(0f, dy);
        reanimated$pushed = true;
    }

    @Inject(method = "extractTabButton(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/item/CreativeModeTab;)V", at = @At("RETURN"))
    private void reanimated$tabTail(GuiGraphicsExtractor context, int mouseX, int mouseY, CreativeModeTab tab, CallbackInfo ci) {
        if (reanimated$pushed) {
            context.pose().popMatrix();
            context.disableScissor();
            reanimated$pushed = false;
        }
    }
}
