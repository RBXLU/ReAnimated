package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.ToastAnim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.toast.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Выезд уведомлений (достижения, рецепты, системные сообщения) по выбранной кривой.
 *
 * В этой версии игры доля видимой ширины не хранится полем, а считается на каждый кадр
 * ({@code getDisappearProgress}) — поэтому кривая подменяется прямо в возвращаемом
 * значении. Тайминги остаются ванильными, меняется только характер движения.
 */
@Mixin(targets = "net.minecraft.client.toast.ToastManager$Entry")
public abstract class ToastEntryMixin {

    @Shadow private Toast.Visibility visibility;

    @Inject(method = "getDisappearProgress", at = @At("RETURN"), cancellable = true)
    private void reanimated$toastCurve(long time, CallbackInfoReturnable<Float> cir) {
        if (!ReAnimatedConfig.get().toastEnabled) return;
        cir.setReturnValue(ToastAnim.remap(cir.getReturnValueF(),
            this.visibility == Toast.Visibility.HIDE));
    }
}
