package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Container screens (Minecraft 26.x). */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin implements com.pycodder.reanimated.anim.PanelBounds {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Override
    public int reanimated$panelTop() {
        return this.topPos;
    }

    @Override
    public int reanimated$panelBottom() {
        return this.topPos + this.imageHeight;
    }
    @Shadow protected Slot hoveredSlot;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void reanimated$highlight(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
        extractor.fill(ix, iy, ix + 16, iy + 16, 0x80FFFFFF);
    }
}
