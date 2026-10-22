package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.OwnTransform;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Чинит «отставание» содержимого во время анимации на 1.21.1–1.21.3 (NeoForge / Mojmap).
 *
 * Длинный лейбл кнопки (шире самой кнопки) ваниль рисует через
 * {@code drawScrollableText} — с обрезкой по {@code enableScissor}. Но scissor
 * в {@link GuiGraphics} до 1.21.4 считается по СЫРЫМ логическим координатам и
 * НЕ учитывает матрицу отрисовки. Когда мод анимирует кнопку (увеличение под
 * курсором, каскад, открытие/закрытие экрана), её фон и текст едут по матрице,
 * а scissor остаётся на месте и обрезает уезжающий текст. То же самое было с
 * моделью игрока в инвентаре. С 1.21.4 ваниль трансформирует прямоугольник сама,
 * поэтому там этого миксина нет.
 *
 * ⚠️ Сдвигаем прямоугольник на НАШУ трансформацию ({@link OwnTransform}), а не на
 * текущую матрицу. В матрице лежит и то, что наложили другие моды, а они задают
 * обрезку под ванильное поведение — «матрица не учитывается». Домножая их сдвиг
 * второй раз, мы уводили обрезку в сторону и содержимое пропадало: так ломался
 * список блоков в Rechiseled и сетка предметов в креативе при плавной прокрутке
 * (Smooth Scrolling двигает матрицу, а обрезку задаёт сырыми координатами).
 *
 * Когда своих трансформаций нет — это точный no-op, поведение ванили не меняется.
 */
@Mixin(GuiGraphics.class)
public abstract class DrawContextScissorMixin {

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$scissorX1(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$scissorY1(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapY(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$scissorX2(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$scissorY2(int v) {
        return OwnTransform.identity() ? v : OwnTransform.mapY(v);
    }
}
