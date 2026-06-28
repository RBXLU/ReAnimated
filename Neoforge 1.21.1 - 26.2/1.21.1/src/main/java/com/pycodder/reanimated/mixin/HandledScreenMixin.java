package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Контейнерные экраны (NeoForge / Mojmap). Весь экран уже выезжает снизу (ScreenMixin),
 * поэтому здесь:
 *  - возвращаем размытый ФОН на место (встречный сдвиг вокруг renderBackground),
 *    оставляя сдвиг для текстуры панели (renderBg) — панель едет, блюр стоит;
 *  - рисуем плавно догоняющую курсор подсветку слота.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected Slot hoveredSlot;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;

    // Встречный сдвиг для блюра: вернуть на место с HEAD до момента отрисовки панели.
    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$blurPush(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        PoseStack m = graphics.pose();
        m.pushPose();
        m.translate(0f, -Anim.containerSlide(), 0f);
    }

    // Перед самой панелью убираем встречный сдвиг — панель снова едет вместе со слотами.
    @Inject(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            shift = At.Shift.BEFORE))
    private void reanimated$blurPop(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        graphics.pose().popPose();
    }

    // Подсветка слота — рисуется, пока активен общий сдвиг экрана, значит едет со слотами.
    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$highlight(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.slotHighlightEnabled) {
            reanimated$slotX = Float.NaN;
            return;
        }

        Slot slot = this.hoveredSlot;
        if (slot == null) {
            reanimated$slotX = Float.NaN;
            reanimated$slotY = Float.NaN;
            return;
        }

        float targetX = this.leftPos + slot.x;
        float targetY = this.topPos + slot.y;

        long now = System.currentTimeMillis();
        float dt = reanimated$slotTime == 0L ? 0f : (now - reanimated$slotTime) / 1000f;
        if (dt > 0.2f) dt = 0.2f;
        reanimated$slotTime = now;

        if (Float.isNaN(reanimated$slotX)) {
            reanimated$slotX = targetX;
            reanimated$slotY = targetY;
        } else {
            reanimated$slotX = Easing.approach(reanimated$slotX, targetX, dt, c.slotHighlightSpeed);
            reanimated$slotY = Easing.approach(reanimated$slotY, targetY, dt, c.slotHighlightSpeed);
        }

        int ix = Math.round(reanimated$slotX);
        int iy = Math.round(reanimated$slotY);
        graphics.fill(ix, iy, ix + 16, iy + 16, 0x80FFFFFF);
    }
}
