package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.PanelBounds;
import com.pycodder.reanimated.anim.RecipeBookAnim;
import com.pycodder.reanimated.anim.UiTransform;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Окно книги рецептов: плавное появление и страховка общей трансформации экрана.
 *
 * 1. Появление. Ваниль показывает окно мгновенно — здесь оно вырастает из-под
 *    панели контейнера ({@link RecipeBookAnim}).
 * 2. Страховка. Ваниль рисует книгу внутри отрисовки экрана, под общим трансформом
 *    мода, и обычно трогать тут нечего. Но моды на интерфейс выносят отрисовку в
 *    свой слой с чистой матрицей: тогда инвентарь уезжает по анимации, а книга
 *    стоит на месте. {@code UiTransform.enforce} накладывает трансформацию только
 *    если матрица действительно оказалась единичной, поэтому в обычном случае
 *    ничего не делает и ни с кем не конфликтует.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookWidgetMixin {

    @Shadow public abstract boolean isVisible();

    @Unique private boolean reanimated$pushed = false;
    @Unique private boolean reanimated$appeared = false;
    @Unique private boolean reanimated$wasOpen = false;
    @Unique private long reanimated$openedAt = 0L;

    @Inject(method = "render", at = @At("HEAD"))
    private void reanimated$bookHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$pushed = false;
        reanimated$appeared = false;

        boolean open = this.isVisible();
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
        PoseStack m = graphics.pose();

        if (Anim.transformActive(container) && Anim.shouldAnimate(screen)) {
            Window window = Minecraft.getInstance().getWindow();
            reanimated$pushed = UiTransform.enforce(m,
                    window.getGuiScaledWidth(), window.getGuiScaledHeight(), container);
        }

        float p = RecipeBookAnim.progress(reanimated$openedAt);
        if (p >= 1f) return;
        float e = RecipeBookAnim.eased(p);
        // Пивот — левый край панели: окно раскрывается «из-под» неё, а не из воздуха.
        float px, py;
        if (screen instanceof PanelBounds panel) {
            px = panel.reanimated$panelLeft();
            py = (panel.reanimated$panelTop() + panel.reanimated$panelBottom()) / 2f;
        } else {
            Window window = Minecraft.getInstance().getWindow();
            px = window.getGuiScaledWidth() / 2f;
            py = window.getGuiScaledHeight() / 2f;
        }
        UiTransform.pushAppear(m, px, py, RecipeBookAnim.scale(e), RecipeBookAnim.offsetX(e), 0f);
        reanimated$appeared = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$bookTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        PoseStack m = graphics.pose();
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
