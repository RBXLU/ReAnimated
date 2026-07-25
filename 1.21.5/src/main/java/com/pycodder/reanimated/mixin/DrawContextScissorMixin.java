package com.pycodder.reanimated.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Чинит «отставание» текста кнопок во время анимации.
 *
 * Длинный лейбл кнопки (шире самой кнопки) ваниль рисует через
 * {@code drawScrollableText} — с обрезкой по {@code enableScissor}. Но scissor
 * в {@link DrawContext} считается по СЫРЫМ логическим координатам виджета и
 * НЕ учитывает матрицу отрисовки. Когда мод анимирует кнопку (увеличение под
 * курсором, каскад, открытие/закрытие экрана), её фон и текст едут по матрице,
 * а scissor остаётся на месте и обрезает уезжающий текст — визуально «текст
 * отстаёт и не анимируется вместе с кнопкой».
 *
 * Здесь мы прогоняем прямоугольник scissor через ту же матрицу (наши
 * трансформации — только сдвиг + масштаб по осям, без поворота, поэтому двух
 * углов достаточно и результат точен). Когда матрица единичная (анимации нет) —
 * это точный no-op, поведение ванили не меняется.
 */
@Mixin(DrawContext.class)
public abstract class DrawContextScissorMixin {

    @Shadow public abstract MatrixStack getMatrices();

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
        Matrix4f m = getMatrices().peek().getPositionMatrix();
        if (m.m00() == 1f && m.m30() == 0f) {
            return v; // по X ничего не меняется — не трогаем
        }
        return Math.round(m.m00() * v + m.m30());
    }

    @Unique
    private int reanimated$mapY(int v) {
        Matrix4f m = getMatrices().peek().getPositionMatrix();
        if (m.m11() == 1f && m.m31() == 0f) {
            return v; // по Y ничего не меняется — не трогаем
        }
        return Math.round(m.m11() * v + m.m31());
    }
}
