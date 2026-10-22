package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.HudAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    @Inject(method = "extractItemHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
        ordinal = 1))
    private void reanimated$selectionPush(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        float dx = HudAnim.selectionOffset(client.player.getInventory().getSelectedSlot());
        if (dx == 0f) return;
        context.pose().pushMatrix();
        context.pose().translate(dx, 0f);
        reanimated$selPushed = true;
    }

    @Inject(method = "extractItemHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
        ordinal = 1, shift = At.Shift.AFTER))
    private void reanimated$selectionPop(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (reanimated$selPushed) {
            context.pose().popMatrix();
            reanimated$selPushed = false;
        }
    }

    @Inject(method = "extractSelectedItemName", at = @At("HEAD"))
    private void reanimated$namePush(GuiGraphicsExtractor context, CallbackInfo ci) {
        reanimated$nameProgress = HudAnim.nameProgress(this.toolHighlightTimer);
        if (reanimated$nameProgress >= 1f) return;
        context.pose().pushMatrix();
        context.pose().translate(0f, HudAnim.nameRise(reanimated$nameProgress));
        reanimated$namePushed = true;
    }

    @Inject(method = "extractSelectedItemName", at = @At("RETURN"))
    private void reanimated$namePop(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (reanimated$namePushed) {
            context.pose().popMatrix();
            reanimated$namePushed = false;
        }
    }

    /**
     * Прозрачность названия в начале появления. Ниже порога ваниль считает текст
     * «почти прозрачным» и рисует его непрозрачным — поэтому снизу ограничиваем.
     */
    @ModifyArg(method = "extractSelectedItemName", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"),
        index = 5)
    private int reanimated$nameAlpha(int color) {
        if (reanimated$nameProgress >= 1f) return color;
        int a = Math.max(8, Math.round((color >>> 24) * reanimated$nameProgress));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
