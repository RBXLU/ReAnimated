package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.WindowRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Чинит краш "Scissor size must be >0" на Minecraft 26.2.
 *
 * Ваниль переводит область обрезки в пиксели кадра так:
 * <pre>
 *   top    = area.top()    * guiScale;                        // без ограничения
 *   bottom = min(area.bottom() * guiScale, window.height);     // с ограничением
 * </pre>
 * Нижняя граница ограничена размером окна, а верхняя — нет. Поэтому область, уехавшая
 * ниже экрана, даёт отрицательную высоту, и 26.2 роняет игру вместо пропуска отрисовки.
 *
 * Мод анимирует экраны сдвигом, и ванильная обрезка списков едет вместе с ними — на
 * невысоком экране (или сразу после переключения полноэкранного режима по F11) она может
 * выйти за нижнюю границу. Здесь повторяем ванильную арифметику и, если область
 * выродилась, пропускаем установку обрезки: рисовать в ней всё равно нечего.
 */
@Mixin(GuiRenderer.class)
public class ScissorClampMixin {

    @Inject(method = "enableScissor(Lnet/minecraft/client/gui/navigation/ScreenRectangle;Lcom/mojang/blaze3d/systems/RenderPass;)V",
            at = @At("HEAD"), cancellable = true)
    private void reanimated$skipDegenerateScissor(ScreenRectangle area, RenderPass pass, CallbackInfo ci) {
        if (area == null) {
            return;
        }
        WindowRenderState window;
        try {
            window = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
        } catch (Throwable ignored) {
            return; // не смогли проверить — не мешаем ванили
        }
        if (window == null || window.guiScale <= 0) {
            return;
        }

        int scale = window.guiScale;
        int left = area.left() * scale;
        int top = area.top() * scale;
        int right = Math.min(area.right() * scale, window.width);
        int bottom = Math.min(area.bottom() * scale, window.height);

        if (right - left <= 0 || bottom - top <= 0) {
            ci.cancel();
        }
    }
}
