package com.pycodder.reanimated.anim;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Трансформация ЭКРАНА (UI) целиком — задаётся ПРЕСЕТОМ ({@link UiPreset}).
 * Это «анимация UI»: экран/панель контейнера выезжает/масштабируется.
 *
 * Покнопочная «анимация кнопок» (профиль/Студия) — отдельный слой, он кладётся
 * ПОВЕРХ этой трансформации в {@code ClickableWidgetMixin} и здесь не участвует.
 * Так пресет (UI) и профиль (кнопки) работают вместе, а не исключают друг друга.
 *
 * Версия для 1.21.1–1.21.5 (NeoForge / Mojmap): GUI рисуется 3D-матрицей
 * {@link PoseStack} (pushPose/popPose, translate/scale в три аргумента).
 *
 * {@link #forward} едет вместе с содержимым экрана, {@link #inverse} возвращает
 * на место фон/блюр.
 *
 * Каждый метод, кроме внутренних {@code *Raw}, кладёт ту же трансформацию в
 * {@link OwnTransform} — по ней обрезка узнаёт, сколько сдвинули МЫ, не подхватывая
 * чужие сдвиги из матрицы. Значит на каждый такой вызов у места отрисовки должен
 * приходиться ровно один {@link OwnTransform#pop()} рядом с {@code matrices.pop()}.
 */
public final class UiTransform {
    private UiTransform() {}

    public static void forward(PoseStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(0f, sy, 0f);
        if (sc != 1f) {
            pivotScaleRaw(m, width / 2f, height / 2f, sc, sc);
        }
        // Та же математика одним шагом: сначала масштаб вокруг центра, потом сдвиг.
        OwnTransform.push(sc, sc, (1f - sc) * width / 2f, (1f - sc) * height / 2f + sy);
    }

    /** Обратная трансформация — строго в обратном порядке к {@link #forward}. */
    public static void inverse(PoseStack m, float width, float height, boolean container) {
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScaleRaw(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(0f, -sy, 0f);
        float inv = 1f / sc;
        OwnTransform.push(inv, inv, (1f - inv) * width / 2f, (1f - inv) * height / 2f - sy * inv);
    }

    public static void pivotScale(PoseStack m, float px, float py, float sx, float sy) {
        pivotScaleRaw(m, px, py, sx, sy);
        OwnTransform.push(sx, sy, px * (1f - sx), py * (1f - sy));
    }

    /**
     * Сдвиг плюс масштаб вокруг пивота одним уровнем — для покнопочного «входа»,
     * где обе части накладываются под один {@code matrices.push()}.
     */
    public static void offsetPivotScale(PoseStack m, float dx, float dy,
                                        float px, float py, float sx, float sy) {
        m.translate(dx, dy, 0f);
        pivotScaleRaw(m, px, py, sx, sy);
        OwnTransform.push(sx, sy, px * (1f - sx) + dx, py * (1f - sy) + dy);
    }

    /** Чистый сдвиг с учётом в {@link OwnTransform}. */
    public static void translate(PoseStack m, float dx, float dy) {
        m.translate(dx, dy, 0f);
        OwnTransform.push(1f, 1f, dx, dy);
    }

    /** Масштаб вокруг пивота без учёта — только для внутренних вызовов. */
    private static void pivotScaleRaw(PoseStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }
}
