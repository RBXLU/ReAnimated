package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.ReAnimatedKeys;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Клавиша «Настроить этот экран» внутри любого экрана. */
@Mixin(Screen.class)
public abstract class ScreenKeysMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void reanimated$screenKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ReAnimatedKeys.onScreenKey((Screen) (Object) this, keyCode, scanCode)) cir.setReturnValue(true);
    }
}
