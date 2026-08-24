package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

/** Base open animation for any screen on Minecraft 26.x, the SCREEN layer (preset), drawn through render-state extraction. */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;
    @Unique private int reanimated$lastW = -1;
    @Unique private int reanimated$lastH = -1;

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof AbstractContainerScreen;
    }

    @Unique
    private void reanimated$assignCascade() {
        Anim.cascadeCount = 1;
        if (!Anim.shouldAnimate(this)) {
            return;
        }
        Screen self = (Screen) (Object) this;
        List<AbstractWidget> widgets = new ArrayList<>();
        for (GuiEventListener e : self.children()) {
            if (e instanceof AbstractWidget w && w.visible && !(e instanceof AbstractContainerWidget)) {
                widgets.add(w);
            }
        }
        widgets.sort(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX));
        int count = widgets.size();
        for (int i = 0; i < count; i++) {
            ((CascadeTarget) widgets.get(i)).reanimated$setCascade(i, count);
        }
        Anim.cascadeCount = Math.max(1, count);
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
    private void reanimated$wrapHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$openTime == 0L) {
            reanimated$openTime = System.currentTimeMillis();
        }
        if (this.width != reanimated$lastW || this.height != reanimated$lastH) {
            reanimated$lastW = this.width;
            reanimated$lastH = this.height;
            reanimated$assignCascade();
        }
        Anim.currentOpenTime = reanimated$openTime;
        boolean container = reanimated$isContainer();
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            Matrix3x2fStack m = extractor.pose();
            m.pushMatrix();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            extractor.pose().popMatrix();
        }
        reanimated$maybeFinishClose();
    }

    @Unique
    private void reanimated$maybeFinishClose() {
        if (!Anim.isClosing()) return;
        if (!Anim.closeFinished(reanimated$isContainer())) return;
        Anim.finishClose();
        Anim.bypassClose = true;
        net.minecraft.client.Minecraft.getInstance().setScreen(null);
        Anim.bypassClose = false;
    }

    @Inject(method = "extractBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean container = reanimated$isContainer();
        reanimated$bgPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            Matrix3x2fStack m = extractor.pose();
            m.pushMatrix();
            UiTransform.inverse(m, this.width, this.height, container);
        }
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            extractor.pose().popMatrix();
        }
    }
}
