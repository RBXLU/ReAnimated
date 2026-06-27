package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Базовая анимация появления любого экрана (1.21.6+ — 2D matrix-стек Matrix3x2fStack).
 *
 * Весь экран (фон + виджеты + ЛЮБОЙ текст) рисуется внутри
 * {@code renderWithTooltip} -> {@code this.render()}. Оборачивая renderWithTooltip
 * одним сдвигом, гарантируем, что текст и кнопки выезжают строго вместе.
 * Фон возвращаем на место встречным сдвигом в {@code renderBackground}.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Unique private long reanimated$openTime = 0L;

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
    private void reanimated$onInit(MinecraftClient client, int width, int height, CallbackInfo ci) {
        reanimated$openTime = System.currentTimeMillis();
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void reanimated$wrapHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Anim.currentOpenTime = reanimated$openTime;
        float slide = ((Object) this instanceof HandledScreen) ? Anim.containerSlide() : Anim.screenSlide();
        Matrix3x2fStack m = context.getMatrices();
        m.pushMatrix();
        m.translate(0f, slide);
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Matrix3x2fStack m = context.getMatrices();
        m.pushMatrix();
        m.translate(0f, -Anim.screenSlide());
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }
}
