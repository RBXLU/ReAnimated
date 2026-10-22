package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.ReAnimatedKeys;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Клавиша «Настроить этот экран» внутри любого экрана. */
@Mixin(Screen.class)
public abstract class ScreenKeysMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void reanimated$screenKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ReAnimatedKeys.onScreenKey((Screen) (Object) this, event)) cir.setReturnValue(true);
    }
}
