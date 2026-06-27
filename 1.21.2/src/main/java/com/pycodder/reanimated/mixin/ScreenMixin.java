package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Базовая анимация появления любого экрана.
 *
 * Весь экран (фон + виджеты + ЛЮБОЙ текст: заголовки, подписи) рисуется внутри
 * {@code renderWithTooltip} -> {@code this.render()}. Оборачивая renderWithTooltip
 * одним сдвигом, мы гарантируем, что текст и кнопки выезжают строго вместе.
 *
 * Фон (панорама/блюр/затемнение) рисуется в {@code renderBackground} — его мы
 * возвращаем на место встречным сдвигом, чтобы он не дёргался.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Unique private long reanimated$openTime = 0L;

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
    private void reanimated$onInit(MinecraftClient client, int width, int height, CallbackInfo ci) {
        reanimated$openTime = System.currentTimeMillis();
    }

    // --- Сдвигаем весь экран целиком ---
    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void reanimated$wrapHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Anim.currentOpenTime = reanimated$openTime;
        float slide = ((Object) this instanceof HandledScreen) ? Anim.containerSlide() : Anim.screenSlide();
        MatrixStack m = context.getMatrices();
        m.push();
        m.translate(0f, slide, 0f);
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    // --- Фон меню остаётся на месте (встречный сдвиг).
    //     Для контейнеров renderBackground переопределён в HandledScreen, поэтому
    //     этот инжект на него НЕ распространяется — там свой обработчик. ---
    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MatrixStack m = context.getMatrices();
        m.push();
        m.translate(0f, -Anim.screenSlide(), 0f);
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}
