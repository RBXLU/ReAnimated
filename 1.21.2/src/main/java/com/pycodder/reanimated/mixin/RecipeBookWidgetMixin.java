package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.PanelBounds;
import com.pycodder.reanimated.anim.RecipeBookAnim;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {

    @Shadow public abstract boolean isOpen();

    @Unique private boolean reanimated$pushed = false;
    @Unique private boolean reanimated$appeared = false;
    @Unique private boolean reanimated$wasOpen = false;
    @Unique private long reanimated$openedAt = 0L;

    @Inject(method = "render", at = @At("HEAD"))
    private void reanimated$bookHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$pushed = false;
        reanimated$appeared = false;

        boolean open = this.isOpen();
        if (!open) {
            reanimated$wasOpen = false;
            return;
        }
        if (!reanimated$wasOpen) {
            reanimated$wasOpen = true;
            reanimated$openedAt = Anim.now();
        }

        Object screen = Anim.currentScreen;
        if (screen == null) return;
        boolean container = Anim.currentIsContainer;
        MatrixStack m = context.getMatrices();

        if (Anim.transformActive(container) && Anim.shouldAnimate(screen)) {
            Window window = MinecraftClient.getInstance().getWindow();
            reanimated$pushed = UiTransform.enforce(m,
                    window.getScaledWidth(), window.getScaledHeight(), container);
        }

        float p = RecipeBookAnim.progress(reanimated$openedAt);
        if (p >= 1f) return;
        float e = RecipeBookAnim.eased(p);
        float px, py;
        if (screen instanceof PanelBounds panel) {
            px = panel.reanimated$panelLeft();
            py = (panel.reanimated$panelTop() + panel.reanimated$panelBottom()) / 2f;
        } else {
            Window window = MinecraftClient.getInstance().getWindow();
            px = window.getScaledWidth() / 2f;
            py = window.getScaledHeight() / 2f;
        }
        UiTransform.pushAppear(m, px, py, RecipeBookAnim.scale(e), RecipeBookAnim.offsetX(e), 0f);
        reanimated$appeared = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$bookTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MatrixStack m = context.getMatrices();
        if (reanimated$appeared) {
            reanimated$appeared = false;
            UiTransform.popAppear(m);
        }
        if (reanimated$pushed) {
            reanimated$pushed = false;
            UiTransform.release(m);
        }
    }
}
