package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.ScreenNav;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.compat.BlurPlusCompat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 1500)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;
    @Unique private int reanimated$pauseFlag = -1;
    @Unique private int reanimated$enterDir = 0;
    @Unique private int reanimated$lastW = -1;
    @Unique private int reanimated$lastH = -1;

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof HandledScreen;
    }

    @Unique
    private boolean reanimated$isPause() {
        if (reanimated$pauseFlag < 0) {
            reanimated$pauseFlag = Anim.isPauseScreen(this) ? 1 : 0;
        }
        return reanimated$pauseFlag == 1;
    }

    @Unique
    private void reanimated$assignCascade() {
        Anim.cascadeCount = 1;
        if (!Anim.shouldAnimate(this)) {
            return;
        }
        Screen self = (Screen) (Object) this;
        List<ClickableWidget> widgets = new ArrayList<>();
        for (Element e : self.children()) {
            if (e instanceof ClickableWidget w && w.visible && !(e instanceof ContainerWidget)) {
                widgets.add(w);
            }
        }
        widgets.sort(Comparator.comparingInt(ClickableWidget::getY).thenComparingInt(ClickableWidget::getX));
        int count = widgets.size();
        for (int i = 0; i < count; i++) {
            ((CascadeTarget) widgets.get(i)).reanimated$setCascade(i, count);
        }
        Anim.cascadeCount = Math.max(1, count);
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"), cancellable = true)
    private void reanimated$wrapHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Anim.closeReadyScreen == this) {
            reanimated$closeNow(null);
            ci.cancel();
            return;
        }
        Anim.frameGraphics = context;
        Anim.beginFrame();
        if (reanimated$openTime == 0L) {
            reanimated$openTime = System.currentTimeMillis();
            reanimated$enterDir = reanimated$isContainer() ? 0 : Anim.takeEnterDir();
            ScreenRules.remember(this);
            Anim.onScreenOpened(this);
        }
        if (this.width != reanimated$lastW || this.height != reanimated$lastH) {
            reanimated$lastW = this.width;
            reanimated$lastH = this.height;
            reanimated$assignCascade();
        }
        Anim.currentOpenTime = reanimated$openTime;
        Anim.currentIsPause = reanimated$isPause();
        Anim.currentOverride = ScreenRules.get(this);
        Anim.currentEnterDir = reanimated$enterDir;
        Anim.currentScreen = this;
        Anim.currentIsContainer = reanimated$isContainer();
        boolean container = reanimated$isContainer();
        UiTransform.saveBase(context.getMatrices());
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            Matrix3x2fStack m = context.getMatrices();
            m.pushMatrix();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            context.getMatrices().popMatrix();
        }
        Anim.endFrame();
        reanimated$maybeFinishClose();
    }

    @Unique
    private void reanimated$maybeFinishClose() {
        if (!Anim.isClosing()) return;
        if (!Anim.closeFinished(reanimated$isContainer())) return;
        if (Anim.pendingScreen == null) {
            Anim.closeReadyScreen = this;
            return;
        }
        reanimated$closeNow(Anim.pendingScreen);
    }

    @Unique
    private void reanimated$closeNow(Object next) {
        Anim.finishClose();
        Anim.bypassClose = true;
        net.minecraft.client.MinecraftClient.getInstance().setScreen(next instanceof Screen s ? s : null);
        Anim.bypassClose = false;
        if (next == null) {
            ScreenNav.clear();
            Anim.clearEnterDir();
        }
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        reanimated$bgPushed = !reanimated$isContainer() && Anim.transformActive(false) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            reanimated$bgPushed = UiTransform.pushBase(context.getMatrices());
        }
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            UiTransform.popBase(context.getMatrices());
        }
    }

    @Unique private static final int REANIMATED$DIM_TOP = 0xC0101010;
    @Unique private static final int REANIMATED$DIM_BOTTOM = 0xD0101010;

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$dimFade(DrawContext context, CallbackInfo ci) {
        if (BlurPlusCompat.present()) return;
        if (!Anim.shouldAnimate(this)) return;
        float k = Anim.backgroundFade(reanimated$isContainer());
        if (k >= 1f) return;

        ci.cancel();
        if (k <= 0.004f) return;
        context.fillGradient(0, 0, this.width, this.height,
            reanimated$dim(REANIMATED$DIM_TOP, k), reanimated$dim(REANIMATED$DIM_BOTTOM, k));
    }

    @Unique
    private static int reanimated$dim(int argb, float k) {
        int a = Math.round(((argb >>> 24) & 0xFF) * k);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
