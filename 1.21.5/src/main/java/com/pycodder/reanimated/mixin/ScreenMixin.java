package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Базовая анимация появления любого экрана.
 *
 * Весь экран (фон + виджеты + ЛЮБОЙ текст) рисуется внутри
 * {@code renderWithTooltip} -> {@code this.render()}. Оборачивая renderWithTooltip
 * одной трансформацией (сдвиг и/или масштаб от центра — по выбранному пресету),
 * гарантируем, что текст, кнопки и модель игрока появляются строго вместе.
 *
 * Фон (панорама/блюр) рисуется в {@code renderBackground} — мы применяем к нему
 * ОБРАТНУЮ трансформацию, чтобы он оставался неподвижным при любом пресете.
 * Для контейнеров renderBackground переопределён в HandledScreen — там свой
 * обработчик ({@code HandledScreenMixin}).
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
    private void reanimated$onInit(MinecraftClient client, int width, int height, CallbackInfo ci) {
        reanimated$openTime = System.currentTimeMillis();
    }

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof HandledScreen;
    }

    /** Прямая трансформация: сдвиг по Y и масштаб от центра экрана. */
    @Unique
    private void reanimated$applyForward(MatrixStack m, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy, 0f);
        if (sc != 1f) {
            float cx = this.width / 2f;
            float cy = this.height / 2f;
            m.translate(cx, cy, 0f);
            m.scale(sc, sc, 1f);
            m.translate(-cx, -cy, 0f);
        }
    }

    /** Обратная трансформация (для фона): сначала обратный масштаб, потом обратный сдвиг. */
    @Unique
    private void reanimated$applyInverse(MatrixStack m, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            float cx = this.width / 2f;
            float cy = this.height / 2f;
            m.translate(cx, cy, 0f);
            m.scale(1f / sc, 1f / sc, 1f);
            m.translate(-cx, -cy, 0f);
        }
        m.translate(0f, -sy, 0f);
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void reanimated$wrapHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Anim.currentOpenTime = reanimated$openTime;
        boolean container = reanimated$isContainer();
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            MatrixStack m = context.getMatrices();
            m.push();
            reanimated$applyForward(m, container);
        }
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            context.getMatrices().pop();
        }
        reanimated$maybeFinishClose();
    }

    /** По завершении обратной анимации выполняет отложенный настоящий setScreen(null). */
    @Unique
    private void reanimated$maybeFinishClose() {
        if (!Anim.isClosing()) return;
        if (!Anim.closeFinished(reanimated$isContainer())) return;
        Anim.finishClose();
        Anim.bypassClose = true;
        net.minecraft.client.MinecraftClient.getInstance().setScreen(null);
        Anim.bypassClose = false;
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Контейнеры сами возвращают фон на место (HandledScreenMixin). Если тронуть его
        // ещё и здесь (в 1.21.5+ HandledScreen.renderBackground зовёт super.renderBackground),
        // получится двойная обратная трансформация — блюр «уезжает» вместе с панелью.
        reanimated$bgPushed = !reanimated$isContainer() && Anim.transformActive(false) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            MatrixStack m = context.getMatrices();
            m.push();
            reanimated$applyInverse(m, false);
        }
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            context.getMatrices().pop();
        }
    }
}
