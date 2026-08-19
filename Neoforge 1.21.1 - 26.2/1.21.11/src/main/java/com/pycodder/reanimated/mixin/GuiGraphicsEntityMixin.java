package com.pycodder.reanimated.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Модель игрока в инвентаре (та, что следит за курсором) — вслед за панелью.
 *
 * С 1.21.6 ваниль рисует такие «картинки в картинке» (модель игрока, знамя в ткацком
 * станке, скин в настройках) не через матрицу интерфейса, а отдельным вызовом
 * {@code submitEntityRenderState}: в него уходят СЫРЫЕ экранные координаты прямоугольника и размер,
 * а текущая матрица игнорируется. Обрезку ваниль при этом матрицей прогоняет — поэтому
 * во время анимации модель оставалась стоять на месте и обрезалась наполовину, «дожидаясь»,
 * пока панель до неё доедет.
 *
 * Здесь прямоугольник и размер прогоняются через ту же матрицу, что и вся панель.
 * Наши трансформации — только сдвиг и масштаб по осям (без поворота), поэтому хватает
 * двух углов. При единичной матрице (анимации нет) это точный no-op.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsEntityMixin {

    @Shadow public abstract Matrix3x2fStack pose();

    @ModifyVariable(method = "submitEntityRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$entityX1(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = "submitEntityRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$entityY1(int v) {
        return reanimated$mapY(v);
    }

    @ModifyVariable(method = "submitEntityRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$entityX2(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = "submitEntityRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$entityY2(int v) {
        return reanimated$mapY(v);
    }

    /** Размер модели: масштабируется вместе с панелью (пресеты «из фона»/«с переднего плана»). */
    @ModifyVariable(method = "submitEntityRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float reanimated$entityScale(float v) {
        Matrix3x2fStack m = pose();
        float s = (m.m00() + m.m11()) * 0.5f;
        return s == 1f ? v : v * s;
    }

    @Unique
    private int reanimated$mapX(int v) {
        Matrix3x2fStack m = pose();
        if (m.m00() == 1f && m.m20() == 0f) {
            return v;
        }
        return Math.round(m.m00() * v + m.m20());
    }

    @Unique
    private int reanimated$mapY(int v) {
        Matrix3x2fStack m = pose();
        if (m.m11() == 1f && m.m21() == 0f) {
            return v;
        }
        return Math.round(m.m11() * v + m.m21());
    }
}
