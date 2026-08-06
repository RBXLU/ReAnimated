package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.platform.Window;
import org.joml.Matrix3x2fStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.PanelBounds;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
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
 * Контейнерные экраны (NeoForge / Mojmap). Весь экран (панель, слоты, МОДЕЛЬ ИГРОКА —
 * всё рисуется внутри renderWithTooltip) выезжает/масштабируется вместе через ScreenMixin.
 * Здесь:
 *  - возвращаем РАЗМЫТЫЙ ФОН на место ОБРАТНОЙ трансформацией (масштаб+сдвиг)
 *    вокруг renderBackground, снимая её прямо перед отрисовкой панели (renderBg) — так
 *    панель и модель игрока едут вместе со слотами, а блюр стоит;
 *  - рисуем плавно догоняющую курсор подсветку слота;
 *  - отдаём границы панели каскаду вкладок креатива ({@link PanelBounds}).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin implements PanelBounds {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow protected Slot hoveredSlot;

    @Override
    public int reanimated$panelTop() {
        return this.topPos;
    }

    @Override
    public int reanimated$panelBottom() {
        return this.topPos + this.imageHeight;
    }

    @Unique private boolean reanimated$blurPushed = false;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;

    /** Обратная трансформация фона — та же математика, что и у обычных экранов. */
    @Unique
    private void reanimated$applyInverse(Matrix3x2fStack m) {
        Window win = Minecraft.getInstance().getWindow();
        UiTransform.inverse(m, win.getGuiScaledWidth(), win.getGuiScaledHeight(), true);
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$blurPush(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$blurPushed = Anim.transformActive(true) && Anim.shouldAnimate(this);
        if (reanimated$blurPushed) {
            Matrix3x2fStack m = graphics.pose();
            m.pushMatrix();
            reanimated$applyInverse(m);
        }
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            shift = At.Shift.BEFORE))
    private void reanimated$blurPop(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$blurPushed) {
            graphics.pose().popMatrix();
        }
    }

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
