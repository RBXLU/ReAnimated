package com.pycodder.reanimated.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * «Картинки в картинке» — вслед за панелью.
 *
 * Ваниль рисует такие вставки не через матрицу интерфейса, а отдельными вызовами:
 * в них уходят СЫРЫЕ экранные координаты прямоугольника и размер, а текущая матрица
 * игнорируется. Обрезку ваниль при этом матрицей прогоняет — поэтому во время
 * анимации вставка оставалась стоять на месте и обрезалась наполовину, «дожидаясь»,
 * пока панель до неё доедет.
 *
 * На 26.x таких вызовов пять (табличка в редакторе рисуется уже не вставкой — метода
 * для неё у GuiGraphicsExtractor нет): модель игрока, книга в столе зачарований,
 * знамя в ткацком станке, превью скина в настройках и др. Через матрицу проходят все
 * пять.
 *
 * У всех последние четыре {@code int} — прямоугольник вставки, поэтому обработчики
 * общие. Размер есть не везде: там, где есть, это первый {@code float}, и он
 * масштабируется вместе с панелью (пресеты «из фона» и «с переднего плана»).
 *
 * Наши трансформации — только сдвиг и масштаб по осям (без поворота), поэтому хватает
 * двух углов. При единичной матрице (анимации нет) это точный no-op.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsEntityMixin {

    @Shadow public abstract Matrix3x2fStack pose();

    @ModifyVariable(method = {"entity",
        "skin",
        "book",
        "bannerPattern",
        "profilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int reanimated$pipX1(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = {"entity",
        "skin",
        "book",
        "bannerPattern",
        "profilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int reanimated$pipY1(int v) {
        return reanimated$mapY(v);
    }

    @ModifyVariable(method = {"entity",
        "skin",
        "book",
        "bannerPattern",
        "profilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int reanimated$pipX2(int v) {
        return reanimated$mapX(v);
    }

    @ModifyVariable(method = {"entity",
        "skin",
        "book",
        "bannerPattern",
        "profilerChart"}, at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int reanimated$pipY2(int v) {
        return reanimated$mapY(v);
    }

    /**
     * Размер вставки. Перечислены только те вызовы, где первый {@code float} —
     * действительно масштаб: у знамени и графика профилировщика размера в аргументах
     * нет вовсе, и лишний обработчик там менял бы совсем другое значение.
     */
    @ModifyVariable(method = {"entity",
        "skin",
        "book"}, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float reanimated$pipScale(float v) {
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
