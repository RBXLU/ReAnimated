package com.pycodder.reanimated.mixin;

import org.joml.Matrix3x2fStack;
import com.pycodder.reanimated.anim.Anim;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Базовая анимация появления любого экрана (NeoForge / Mojmap).
 *
 * Весь экран (фон + виджеты + любой текст) рисуется внутри
 * {@code renderWithTooltip} -> {@code this.render()}. Оборачивая renderWithTooltip
 * одним сдвигом, гарантируем, что текст и кнопки выезжают строго вместе.
 *
 * Фон (панорама/блюр/затемнение) рисуется в {@code renderBackground} — его возвращаем
 * на место встречным сдвигом, чтобы он не дёргался.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Unique private long reanimated$openTime = 0L;

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void reanimated$wrapHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$openTime == 0L) reanimated$openTime = System.currentTimeMillis();
        Anim.currentOpenTime = reanimated$openTime;
        float slide = ((Object) this instanceof AbstractContainerScreen) ? Anim.containerSlide() : Anim.screenSlide();
        Matrix3x2fStack m = graphics.pose();
        m.pushMatrix();
        m.translate(0f, slide);
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        graphics.pose().popMatrix();
    }

    // Фон меню остаётся на месте (встречный сдвиг). Для контейнеров renderBackground
    // переопределён в AbstractContainerScreen — там свой обработчик (HandledScreenMixin).
    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Matrix3x2fStack m = graphics.pose();
        m.pushMatrix();
        m.translate(0f, -Anim.screenSlide());
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        graphics.pose().popMatrix();
    }
}
