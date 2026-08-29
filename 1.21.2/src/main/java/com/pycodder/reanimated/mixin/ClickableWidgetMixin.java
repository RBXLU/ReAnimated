package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** List frames ({@code ContainerWidget}) are neither scaled on hover nor cascaded per button here: they have their own per-row cascade. */
@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin implements CascadeTarget {
    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow public abstract boolean isHovered();
    @Shadow protected float alpha;

    @Unique private float reanimated$hover = 0f;
    @Unique private long reanimated$lastTime = 0L;
    @Unique private long reanimated$pressTime = 0L;
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

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void reanimated$preRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$pushed = 0;
        reanimated$savedAlpha = -1f;
        if ((Object) this instanceof net.minecraft.client.gui.widget.ContainerWidget) {
            reanimated$freezeFrame(context);
            return;
        }
        if (reanimated$tooFaint()) {
            ci.cancel();
            return;
        }

        reanimated$applyProfile(context);
        if (reanimated$isTextField()) {
            return;
        }
        reanimated$applyHover(context);
        reanimated$applyPress(context);
    }

    @Unique
    private boolean reanimated$isTextField() {
        return (Object) this instanceof net.minecraft.client.gui.widget.TextFieldWidget;
    }

    @Inject(method = "playDownSound", at = @At("HEAD"))
    private void reanimated$onDown(net.minecraft.client.sound.SoundManager soundManager, CallbackInfo ci) {
        if (ReAnimatedConfig.get().pressEnabled) {
            reanimated$pressTime = System.currentTimeMillis();
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void reanimated$onMouseDown(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue()) && ReAnimatedConfig.get().pressEnabled) {
            reanimated$pressTime = System.currentTimeMillis();
        }
    }

    @Unique
    private void reanimated$freezeFrame(DrawContext context) {
        if (ReAnimatedConfig.get().listsEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Anim.shouldAnimate(client.currentScreen)) {
            return;
        }
        boolean container = client.currentScreen instanceof HandledScreen;
        if (!Anim.transformActive(container)) {
            return;
        }
        Window window = client.getWindow();
        MatrixStack m = context.getMatrices();
        m.push();
        reanimated$pushed++;
        UiTransform.inverse(m, window.getScaledWidth(), window.getScaledHeight(), container);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$postRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        while (reanimated$pushed > 0) {
            context.getMatrices().pop();
            com.pycodder.reanimated.anim.OwnTransform.pop();
            reanimated$pushed--;
        }
        if (reanimated$savedAlpha >= 0f) {
            this.alpha = reanimated$savedAlpha;
            reanimated$savedAlpha = -1f;
        }
    }

    @Unique
    private void reanimated$applyProfile(DrawContext context) {
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

        MatrixStack m = context.getMatrices();
        m.push();
        reanimated$pushed++;
        float px = getX() + getWidth() * p.pivot.fx;
        float py = getY() + getHeight() * p.pivot.fy;
        UiTransform.offsetPivotScale(m, p.offsetXAt(own), p.offsetYAt(own),
            px, py, p.scaleXAt(own), p.scaleYAt(own));
    }

    @Unique
    private void reanimated$applyHover(DrawContext context) {
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
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        reanimated$pushed++;
        UiTransform.pivotScale(matrices, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale, scale);
    }

    @Unique
    private void reanimated$applyPress(DrawContext context) {
        if (reanimated$pressTime == 0L) {
            return;
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float durationMs = Math.max(0.01f, c.pressDuration) * 1000f;
        float t = (System.currentTimeMillis() - reanimated$pressTime) / durationMs;
        if (t >= 1f || !c.pressEnabled) {
            reanimated$pressTime = 0L;
            return;
        }

        float scale = 1f - c.pressScale * Easing.press(t);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        reanimated$pushed++;
        UiTransform.pivotScale(matrices, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale, scale);
    }
}
