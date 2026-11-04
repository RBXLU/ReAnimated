package com.pycodder.reanimated.mixin.compat;

import com.pycodder.reanimated.anim.Anim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Совместимость с EMI: пока экран едет по анимации, предметы в боковых колонках
 * рисуются обычным путём, а не пачкой.
 *
 * Ради скорости EMI складывает модели предметов боковых колонок в один запечённый
 * буфер и рисует его напрямую, матрицей {@code RenderSystem.getModelViewMatrix()}.
 * Это ДРУГАЯ матрица: трансформации интерфейса живут в матрице {@code DrawContext} и
 * до запечённого буфера не доходят. Поэтому рамки слотов, подсветка и фон панели
 * ехали вместе с экраном, а сами модели предметов оставались стоять — колонки
 * выглядели разъехавшимися, а подсказка появлялась там, где предмет на самом деле.
 *
 * У EMI уже есть непакетный путь — он рисует через {@code DrawContext} и матрицу
 * учитывает. На время анимации переключаемся на него: {@code isEnabled()} отвечает
 * «нет», {@code render()} уходит в ветку без пачки, а {@code draw()} ничего не рисует,
 * так что двойной отрисовки не будет. Флаг постоянен в пределах кадра (время кадра у
 * мода кэшируется), поэтому пачка не может распасться посередине.
 *
 * Как только анимация закончилась, EMI снова пакует предметы — в покое мод не
 * отнимает у него ни кадра.
 *
 * {@code @Pseudo} — класс чужой и может отсутствовать: без EMI миксин просто не
 * применяется, {@code require = 0} терпит и переименование метода в будущих версиях.
 */
@Pseudo
@Mixin(targets = "dev.emi.emi.screen.StackBatcher", remap = false)
public abstract class EmiStackBatcherMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void reanimated$unbatchWhileAnimating(CallbackInfoReturnable<Boolean> cir) {
        Object screen = Anim.currentScreen;
        if (screen == null) return;
        if (!Anim.transformActive(Anim.currentIsContainer)) return;
        if (!Anim.shouldAnimate(screen)) return;
        cir.setReturnValue(false);
    }
}
