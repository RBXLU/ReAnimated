package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.HudAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HUD: рамка выбранного слота хотбара едет к нему, а не перескакивает, а название
 * предмета над хотбаром при смене коротко приподнимается и проявляется.
 *
 * Рамку двигает матрица вокруг одного ванильного вызова — так сдвиг дробный и
 * движение остаётся плавным при любом масштабе интерфейса.
 */
@Mixin(Gui.class)
public abstract class InGameHudMixin {

    @Shadow private int toolHighlightTimer;

    @Unique private boolean reanimated$selPushed = false;
    @Unique private boolean reanimated$namePushed = false;
    @Unique private float reanimated$nameProgress = 1f;

    /** Второй вызов blitSprite в renderHotbar — рамка выбранного слота. */
    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
        ordinal = 1))
    private void reanimated$selectionPush(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        float dx = HudAnim.selectionOffset(client.player.getInventory().selected);
        if (dx == 0f) return;
        context.pose().pushPose();
        context.pose().translate(dx, 0f, 0f);
        reanimated$selPushed = true;
    }

    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
        ordinal = 1, shift = At.Shift.AFTER))
    private void reanimated$selectionPop(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (reanimated$selPushed) {
            context.pose().popPose();
            reanimated$selPushed = false;
        }
    }

    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At("HEAD"))
    private void reanimated$namePush(GuiGraphics context, int yShift, CallbackInfo ci) {
        reanimated$nameProgress = HudAnim.nameProgress(this.toolHighlightTimer);
        if (reanimated$nameProgress >= 1f) return;
        context.pose().pushPose();
        context.pose().translate(0f, HudAnim.nameRise(reanimated$nameProgress), 0f);
        reanimated$namePushed = true;
    }

    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At("RETURN"))
    private void reanimated$namePop(GuiGraphics context, int yShift, CallbackInfo ci) {
        if (reanimated$namePushed) {
            context.pose().popPose();
            reanimated$namePushed = false;
        }
    }

    /**
     * Прозрачность названия в начале появления. Ниже порога ваниль считает текст
     * «почти прозрачным» и рисует его непрозрачным — поэтому снизу ограничиваем.
     */
    @ModifyArg(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I"),
        index = 5)
    private int reanimated$nameAlpha(int color) {
        if (reanimated$nameProgress >= 1f) return color;
        int a = Math.max(8, Math.round((color >>> 24) * reanimated$nameProgress));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
