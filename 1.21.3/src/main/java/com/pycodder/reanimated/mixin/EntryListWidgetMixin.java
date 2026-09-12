package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin {

    @Shadow public abstract java.util.List<?> children();

    @Unique private static final int REANIMATED$MAX_SLOT = 14;

    @Unique private boolean reanimated$pushed = false;

    @Unique
    private boolean reanimated$rowsAnimating() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.listsEnabled || !c.profileLists.enabled) {
            return false;
        }
        float e = Anim.elapsed(Anim.now());
        if (e == Float.MAX_VALUE) {
            return false;
        }
        return e * 1000f < c.profileLists.totalMs(REANIMATED$MAX_SLOT + 1);
    }

    @Unique
    private void reanimated$begin(DrawContext context, int index) {
        reanimated$pushed = false;
        if (!Anim.shouldAnimate(MinecraftClient.getInstance().currentScreen)) {
            return;
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        AnimProfile p = c.profileLists;
        float e = Anim.elapsed(Anim.now());
        int count = Math.max(1, this.children().size());
        int rank = Math.max(0, index);
        int slot = Math.min(p.slotFor(rank, count), REANIMATED$MAX_SLOT);
        float eased = p.progress(e * 1000f, slot);
        if (p.identityAt(eased)) {
            return;
        }

        MatrixStack m = context.getMatrices();
        m.push();
        com.pycodder.reanimated.anim.UiTransform.translate(m, p.offsetXAt(eased), p.offsetYAt(eased));
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(DrawContext context) {
        if (reanimated$pushed) {
            context.getMatrices().pop();
            com.pycodder.reanimated.anim.OwnTransform.pop();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "renderEntry(Lnet/minecraft/client/gui/DrawContext;IIFIIIII)V", at = @At("HEAD"))
    private void reanimated$rowHead(DrawContext context, int mouseX, int mouseY, float delta,
                                    int index, int x, int y, int entryWidth, int entryHeight,
                                    CallbackInfo ci) {
        if (!reanimated$rowsAnimating()) {
            reanimated$pushed = false;
            return;
        }
        reanimated$begin(context, index);
    }

    @Inject(method = "renderEntry(Lnet/minecraft/client/gui/DrawContext;IIFIIIII)V", at = @At("RETURN"))
    private void reanimated$rowTail(DrawContext context, int mouseX, int mouseY, float delta,
                                    int index, int x, int y, int entryWidth, int entryHeight,
                                    CallbackInfo ci) {
        reanimated$end(context);
    }
}
