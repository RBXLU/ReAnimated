package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Minecraft 26.x: the widget's draw point is {@code extractRenderState}. */
@Mixin(AbstractWidget.class)
public abstract class ClickableWidgetMixin implements CascadeTarget {
    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow public abstract boolean isHovered();
    @Shadow protected float alpha;

    @Unique private float reanimated$hover = 0f;
    @Unique private long reanimated$lastTime = 0L;
    @Unique private int reanimated$pushed = 0;
    @Unique private float reanimated$savedAlpha = -1f;

    @Unique private int reanimated$rank = -1;
    @Unique private int reanimated$count = 0;

    @Override
    public void reanimated$setCascade(int rank, int count) {
        reanimated$rank = rank;
        reanimated$count = count;
    }

    @Override
    public int reanimated$cascadeRank() {
        return reanimated$rank;
    }

    @Override
    public int reanimated$cascadeCount() {
        return reanimated$count;
    }

    @Override
    public float reanimated$currentAlpha() {
        return this.alpha;
    }

    @Unique private static final float REANIMATED$MIN_ALPHA = 4f / 255f;

    @Unique
    private boolean reanimated$tooFaint() {
        AnimProfile p = ReAnimatedConfig.get().profile;
        if (!p.enabled || reanimated$rank < 0) {
            return false;
        }
        float own = Anim.profileEase(p.slotFor(reanimated$rank, reanimated$count));
        return this.alpha * p.alphaAt(own) < REANIMATED$MIN_ALPHA;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void reanimated$preRender(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$pushed = 0;
        reanimated$savedAlpha = -1f;
        if ((Object) this instanceof AbstractContainerWidget) {
            reanimated$freezeFrame(extractor);
            return;
        }
        if (reanimated$tooFaint()) {
            ci.cancel();
            return;
        }

        reanimated$applyProfile(extractor);
        reanimated$applyHover(extractor);
    }

    @Unique
    private void reanimated$freezeFrame(GuiGraphicsExtractor extractor) {
        Minecraft client = Minecraft.getInstance();
        if (!Anim.shouldAnimate(client.screen)) {
            return;
        }
        boolean container = client.screen instanceof AbstractContainerScreen;
        if (!Anim.transformActive(container)) {
            return;
        }
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.inverse(m, extractor.guiWidth(), extractor.guiHeight(), container);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void reanimated$postRender(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        while (reanimated$pushed > 0) {
            extractor.pose().popMatrix();
            reanimated$pushed--;
        }
        if (reanimated$savedAlpha >= 0f) {
            this.alpha = reanimated$savedAlpha;
            reanimated$savedAlpha = -1f;
        }
    }

    @Unique
    private void reanimated$applyProfile(GuiGraphicsExtractor extractor) {
        AnimProfile p = ReAnimatedConfig.get().profile;
        if (!p.enabled || reanimated$rank < 0) {
            return;
        }

        int slot = p.slotFor(reanimated$rank, reanimated$count);
        float own = Anim.profileEase(slot);

        float a = p.alphaAt(own);
        if (a < 1f) {
            reanimated$savedAlpha = this.alpha;
            this.alpha = this.alpha * a;
        }

        if (p.identityAt(own)) {
            return;
        }

        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        float px = getX() + getWidth() * p.pivot.fx;
        float py = getY() + getHeight() * p.pivot.fy;
        m.translate(p.offsetXAt(own), p.offsetYAt(own));
        UiTransform.pivotScale(m, px, py, p.scaleXAt(own), p.scaleYAt(own));
    }

    @Unique
    private void reanimated$applyHover(GuiGraphicsExtractor extractor) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.hoverEnabled) {
            return;
        }

        long now = System.currentTimeMillis();
        float dt = reanimated$lastTime == 0L ? 0f : (now - reanimated$lastTime) / 1000f;
        if (dt > 0.2f) dt = 0.2f;
        reanimated$lastTime = now;

        float target = isHovered() ? 1f : 0f;
        reanimated$hover = Easing.approach(reanimated$hover, target, dt, c.hoverSpeed);

        if (reanimated$hover < 0.001f) {
            return;
        }

        float scale = 1f + c.hoverScale * reanimated$hover;
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.pivotScale(m, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale, scale);
    }
}
