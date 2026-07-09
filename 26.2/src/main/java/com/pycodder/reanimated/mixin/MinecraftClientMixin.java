package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiPreset;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Отложенное закрытие экрана: вместо мгновенного {@code setScreen(null)} экран
 * ненадолго остаётся текущим и доигрывает анимацию открытия в обратном порядке
 * ({@link Anim#beginClose}), а по её завершении {@code ScreenMixin} сам вызывает
 * настоящий {@code setScreen(null)} (пропуская этот миксин через {@link Anim#bypassClose}).
 *
 * В Minecraft 26.2 хранение текущего экрана и {@code setScreen(Screen)} переехали
 * с {@code Minecraft} на {@code Gui} ({@code Minecraft.setScreenAndShow} лишь
 * делегирует туда) — поэтому здесь миксин цепляется к {@code Gui}, а не к
 * {@code Minecraft}, как в более старых версиях.
 */
@Mixin(Gui.class)
public abstract class MinecraftClientMixin {

    @Shadow private Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void reanimated$onSetScreen(Screen screen, CallbackInfo ci) {
        if (Anim.bypassClose) return;

        if (screen != null) {
            // Открывается другой экран — реверс старого больше не актуален.
            if (Anim.isClosing()) Anim.finishClose();
            return;
        }

        // Закрытие уже проигрывается. Контейнерное закрытие (инвентарь/печь/верстак)
        // шлёт setScreen(null) ДВАЖДЫ: сначала ClientPlayerEntity.closeScreen, затем
        // Screen.close. Глотаем повторные вызовы, иначе второй закрыл бы экран мгновенно,
        // без анимации. Настоящее закрытие выполнит ScreenMixin через bypassClose.
        if (Anim.isClosing()) {
            ci.cancel();
            return;
        }

        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.closeAnimationEnabled) return;

        Screen current = this.screen;
        if (current == null) return;

        boolean container = current instanceof AbstractContainerScreen;
        boolean enabledFlag = container ? c.containerEnabled : c.screenOpenEnabled;
        if (!enabledFlag || c.uiPreset == UiPreset.NONE) return;

        Anim.beginClose(container);
        ci.cancel();
    }
}
