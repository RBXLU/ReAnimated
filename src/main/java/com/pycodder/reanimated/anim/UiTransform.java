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
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(sx, sy, 0f);
        if (sc != 1f) {
            pivotScaleRaw(m, width / 2f, height / 2f, sc, sc);
        }
        // Та же математика одним шагом: сначала масштаб вокруг центра, потом сдвиг.
        OwnTransform.push(sc, sc, (1f - sc) * width / 2f + sx, (1f - sc) * height / 2f + sy);
    }

    /** Обратная трансформация — строго в обратном порядке к {@link #forward}. */
    public static void inverse(PoseStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScaleRaw(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(-sx, -sy, 0f);
        float inv = 1f / sc;
        OwnTransform.push(inv, inv, (1f - inv) * width / 2f - sx * inv, (1f - inv) * height / 2f - sy * inv);
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

    /**
     * Появление всплывающей подсказки: масштаб вокруг точки курсора плюс короткий
     * подъём. Матричный тип у GUI менялся между версиями игры, поэтому вся работа с
     * ним живёт здесь, а миксины передают сюда матрицу и не называют её тип.
     *
     * Учёт в {@code OwnTransform} здесь не нужен: подсказка рисуется поверх всего
     * и обрезкой не пользуется, а лишняя запись сломала бы баланс стека.
     */
    public static void pushPopup(PoseStack m, float px, float py, float scale, float dy) {
        m.pushPose();
        m.translate(0f, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    /** Снимает трансформацию, наложенную {@link #pushPopup}. */
    public static void popPopup(PoseStack m) {
        m.popPose();
    }

    /**
     * Страховка на случай, когда до части интерфейса наша трансформация не доходит.
     *
     * Ваниль рисует книгу рецептов внутри отрисовки экрана, то есть под общим
     * трансформом, — но моды на интерфейс умеют выносить её отрисовку в свой слой с
     * чистой матрицей. Тогда панель контейнера уезжает по анимации, а книга стоит на
     * месте (ровно это видно в сборках на 250+ модов).
     *
     * Поэтому здесь трансформация накладывается САМА, но ТОЛЬКО если матрица
     * оказалась единичной, — значит наш трансформ действительно потерян. Если он на
     * месте или другой мод поставил свой, не трогаем ничего: лишний сдвиг сломал бы
     * им раскладку.
     *
     * @return true, если матрица была положена в стек — тогда нужен {@link #release}.
     */
    public static boolean enforce(PoseStack m, float width, float height, boolean container) {
        org.joml.Matrix4f p = m.last().pose();
        if (!isIdentity(p.m00(), p.m01(), p.m10(), p.m11(), p.m30(), p.m31())) {
            return false;
        }
        m.pushPose();
        forward(m, width, height, container);
        return true;
    }

    /** Снимает трансформацию, наложенную {@link #enforce}. */
    public static void release(PoseStack m) {
        m.popPose();
        OwnTransform.pop();
    }

    /** Единичная ли матрица: масштаб 1, без сдвига и перекоса. */
    private static boolean isIdentity(float m00, float m01, float m10, float m11, float tx, float ty) {
        return Math.abs(m00 - 1f) < 1.0E-4F && Math.abs(m11 - 1f) < 1.0E-4F
                && Math.abs(m01) < 1.0E-4F && Math.abs(m10) < 1.0E-4F
                && Math.abs(tx) < 1.0E-3F && Math.abs(ty) < 1.0E-3F;
    }

    /*
     * Фон и блюр рисуются РОВНО так же, как без мода.
     *
     * Встречной трансформацией (сдвиг и масштаб в обратном порядке) это не делается:
     * её математику пришлось бы повторять вручную, и любая ошибка в ней (например,
     * потерянный горизонтальный сдвиг) уводит блюр вместе с окном. Вместо этого в
     * начале кадра запоминается матрица ДО наложения трансформации экрана, а перед
     * отрисовкой фона она просто ставится обратно — повторять и обращать нечего,
     * ошибиться негде.
     */
    private static final org.joml.Matrix4f BASE = new org.joml.Matrix4f();
    private static boolean baseSaved = false;

    /** Запоминает матрицу кадра до того, как экран получил свою трансформацию. */
    public static void saveBase(PoseStack m) {
        BASE.set(m.last().pose());
        baseSaved = true;
    }

    /** Ставит матрицу обратно в запомненное состояние. @return true, если положили уровень в стек */
    public static boolean pushBase(PoseStack m) {
        if (!baseSaved) return false;
        m.pushPose();
        m.last().pose().set(BASE);
        OwnTransform.pushReset();
        return true;
    }

    public static void popBase(PoseStack m) {
        m.popPose();
        OwnTransform.pop();
    }

    /** Появление окна: сдвиг плюс масштаб вокруг пивота одним уровнем стека. */
    public static void pushAppear(PoseStack m, float px, float py, float scale, float dx, float dy) {
        m.pushPose();
        m.translate(dx, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
        OwnTransform.push(scale, scale, px * (1f - scale) + dx, py * (1f - scale) + dy);
    }

    public static void popAppear(PoseStack m) {
        m.popPose();
        OwnTransform.pop();
    }
}
