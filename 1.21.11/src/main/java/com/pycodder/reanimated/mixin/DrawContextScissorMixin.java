package com.pycodder.reanimated.mixin;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Чинит «отставание» обрезаемого содержимого во время анимации.
 *
 * Длинный лейбл кнопки ваниль рисует через {@code drawScrollableText} — с обрезкой
 * по {@code enableScissor}; так же обрезается окно модели игрока в инвентаре. Но
 * прямоугольник scissor считается по СЫРЫМ логическим координатам и НЕ учитывает
 * матрицу отрисовки: содержимое едет по матрице, а обрезка стоит на месте и режет
 * его — визуально «текст/модель отстают».
 *
 * Здесь прогоняем прямоугольник через ту же матрицу. Версия для 1.21.6+: матрица
 * 2D-аффинная ({@link Matrix3x2fStack}), масштаб в m00/m11, смещение в m20/m21.
 * При единичной матрице — точный no-op, поведение ванили не меняется.
 */
@Mixin(DrawContext.class)
public abstract class DrawContextScissorMixin {

    @Shadow public abstract Matrix3x2fStack getMatrices();

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$scissorX1(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$scissorY1(int v) {
        return reanimated$mapY(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$scissorX2(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$scissorY2(int v) {
        return reanimated$mapY(v);
    }

    @Unique
    private int reanimated$mapX(int v) {
        Matrix3x2fStack m = getMatrices();
        if (m.m00() == 1f && m.m20() == 0f) {
            return v; // по X ничего не меняется — не трогаем
        }
        return Math.round(m.m00() * v + m.m20());
    }

    @Unique
    private int reanimated$mapY(int v) {
        Matrix3x2fStack m = getMatrices();
        if (m.m11() == 1f && m.m21() == 0f) {
            return v; // по Y ничего не меняется — не трогаем
        }
        return Math.round(m.m11() * v + m.m21());
    }
}
