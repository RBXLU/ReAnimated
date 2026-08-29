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

/** Cascade of list rows: Mod Menu entries, servers, worlds, resource packs, data packs. */
@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin {
    @Shadow public abstract java.util.List<?> children();

    @Unique private static final int REANIMATED$MAX_SLOT = 14;

    @Unique private boolean reanimated$pushed = false;

    @Unique
    private void reanimated$begin(DrawContext context, int index) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.listsEnabled) {
            return;
        }
        AnimProfile p = c.profileLists;
        if (!p.enabled) {
            return;
        }
        if (!Anim.shouldAnimate(MinecraftClient.getInstance().currentScreen)) {
            return;
        }
        float e = Anim.elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) {
            return;
        }
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
        reanimated$begin(context, index);
    }

    @Inject(method = "renderEntry(Lnet/minecraft/client/gui/DrawContext;IIFIIIII)V", at = @At("RETURN"))
    private void reanimated$rowTail(DrawContext context, int mouseX, int mouseY, float delta,
                                    int index, int x, int y, int entryWidth, int entryHeight,
                                    CallbackInfo ci) {
        reanimated$end(context);
    }
}
