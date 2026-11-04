package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.ToastAnim;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Выезд уведомлений (достижения, рецепты, системные сообщения) по выбранной кривой.
 *
 * Ваниль хранит долю ширины, на которую уведомление уже вылезло на экран, и считает её
 * жёстко: квадрат от времени за 600 мс. Мод не заводит своего таймера, а на время
 * отрисовки подменяет это значение пересчитанным ({@link ToastAnim#remap}) и сразу
 * возвращает исходное обратно — так ванильный расчёт остаётся единственным хозяином
 * таймингов, а пересчёт не может наложиться сам на себя, сколько бы раз за кадр ни
 * рисовали уведомление.
 */
@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public abstract class ToastEntryMixin {

    @Shadow private float visiblePortion;

    @Shadow private Toast.Visibility visibility;

    /** Ванильное значение на время отрисовки. NaN — подмены не было. */
    @Unique private float reanimated$originalPortion = Float.NaN;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void reanimated$toastHead(GuiGraphicsExtractor extractor, int index, CallbackInfo ci) {
        if (!ReAnimatedConfig.get().toastEnabled) return;
        reanimated$originalPortion = this.visiblePortion;
        this.visiblePortion = ToastAnim.remap(
            reanimated$originalPortion, this.visibility == Toast.Visibility.HIDE);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void reanimated$toastTail(GuiGraphicsExtractor extractor, int index, CallbackInfo ci) {
        if (Float.isNaN(reanimated$originalPortion)) return;
        this.visiblePortion = reanimated$originalPortion;
        reanimated$originalPortion = Float.NaN;
    }
}
