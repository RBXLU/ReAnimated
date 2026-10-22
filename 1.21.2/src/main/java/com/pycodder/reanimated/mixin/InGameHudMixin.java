package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.HudAnim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
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
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow private int heldItemTooltipFade;

    @Unique private boolean reanimated$selPushed = false;
    @Unique private boolean reanimated$namePushed = false;
    @Unique private float reanimated$nameProgress = 1f;

    /** Второй вызов drawGuiTexture в renderHotbar — рамка выбранного слота. */
    @Inject(method = "renderHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
        ordinal = 1))
    private void reanimated$selectionPush(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        float dx = HudAnim.selectionOffset(client.player.getInventory().selectedSlot);
        if (dx == 0f) return;
        context.getMatrices().push();
        context.getMatrices().translate(dx, 0f, 0f);
        reanimated$selPushed = true;
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
        ordinal = 1, shift = At.Shift.AFTER))
    private void reanimated$selectionPop(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (reanimated$selPushed) {
            context.getMatrices().pop();
            reanimated$selPushed = false;
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"))
    private void reanimated$namePush(DrawContext context, CallbackInfo ci) {
        reanimated$nameProgress = HudAnim.nameProgress(this.heldItemTooltipFade);
        if (reanimated$nameProgress >= 1f) return;
        context.getMatrices().push();
        context.getMatrices().translate(0f, HudAnim.nameRise(reanimated$nameProgress), 0f);
        reanimated$namePushed = true;
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("RETURN"))
    private void reanimated$namePop(DrawContext context, CallbackInfo ci) {
        if (reanimated$namePushed) {
            context.getMatrices().pop();
            reanimated$namePushed = false;
        }
    }

    /**
     * Прозрачность названия в начале появления. Ниже порога ваниль считает текст
     * «почти прозрачным» и рисует его непрозрачным — поэтому снизу ограничиваем.
     */
    @ModifyArg(method = "renderHeldItemTooltip", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"),
        index = 5)
    private int reanimated$nameAlpha(int color) {
        if (reanimated$nameProgress >= 1f) return color;
        int a = Math.max(8, Math.round((color >>> 24) * reanimated$nameProgress));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
