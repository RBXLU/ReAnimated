package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin implements com.pycodder.reanimated.anim.PanelBounds {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected Slot focusedSlot;

    @Override
    public int reanimated$panelLeft() {
        return this.x;
    }

    @Override
    public int reanimated$panelTop() {
        return this.y;
    }

    @Override
    public int reanimated$panelBottom() {
        return this.y + this.backgroundHeight;
    }

    @Unique private boolean reanimated$blurPushed = false;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$blurPush(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$blurPushed = Anim.transformActive(true) && Anim.shouldAnimate(this);
        if (reanimated$blurPushed) {
            reanimated$blurPushed = UiTransform.pushBase(context.getMatrices());
        }
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V",
            shift = At.Shift.BEFORE))
    private void reanimated$blurPop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$blurPushed) {
            UiTransform.popBase(context.getMatrices());
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/gui/DrawContext;III)V",
            shift = At.Shift.AFTER))
    private void reanimated$highlight(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$drawHighlight(context);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$highlightReset(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.focusedSlot == null || !ReAnimatedConfig.get().slotHighlightEnabled) {
            reanimated$slotX = Float.NaN;
            reanimated$slotY = Float.NaN;
        }
    }

    @Unique
    private void reanimated$drawHighlight(DrawContext context) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.slotHighlightEnabled) {
            reanimated$slotX = Float.NaN;
            return;
        }

        Slot slot = this.focusedSlot;
        if (slot == null) {
            reanimated$slotX = Float.NaN;
            reanimated$slotY = Float.NaN;
            return;
        }

        float targetX = slot.x;
        float targetY = slot.y;

        long now = Anim.now();
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
        context.fill(ix, iy, ix + 16, iy + 16, 0x80FFFFFF);
    }
}
