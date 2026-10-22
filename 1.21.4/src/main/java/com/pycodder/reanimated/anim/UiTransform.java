package com.pycodder.reanimated.anim;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Трансформация ЭКРАНА (UI) целиком — задаётся ПРЕСЕТОМ ({@link UiPreset}).
 * Это «анимация UI»: экран/панель контейнера выезжает/масштабируется.
 *
 * Покнопочная «анимация кнопок» (профиль/Студия) — отдельный слой, он кладётся
 * ПОВЕРХ этой трансформации в {@code ClickableWidgetMixin} и здесь не участвует.
 * Так пресет (UI) и профиль (кнопки) работают вместе, а не исключают друг друга.
 *
 * {@link #forward} едет вместе с содержимым экрана, {@link #inverse} возвращает
 * на место фон/блюр.
 */
public final class UiTransform {
    private UiTransform() {}

    public static void forward(MatrixStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        m.translate(sx, sy, 0f);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, sc, sc);
        }
    }

    /** Обратная трансформация — строго в обратном порядке к {@link #forward}. */
    public static void inverse(MatrixStack m, float width, float height, boolean container) {
        float sx = Anim.slideX(container);
        float sy = Anim.slideY(container);
        float sc = Anim.scale(container);
        if (sc != 1f) {
            pivotScale(m, width / 2f, height / 2f, 1f / sc, 1f / sc);
        }
        m.translate(-sx, -sy, 0f);
    }

    public static void pivotScale(MatrixStack m, float px, float py, float sx, float sy) {
        m.translate(px, py, 0f);
        m.scale(sx, sy, 1f);
        m.translate(-px, -py, 0f);
    }

    /**
     * Появление всплывающей подсказки: масштаб вокруг точки курсора плюс короткий
     * подъём. Матричный тип у GUI менялся между версиями игры, поэтому вся работа с
     * ним живёт здесь, а миксины передают сюда {@code context.getMatrices()} и не
     * называют его тип.
     *
     * Учёт в {@code OwnTransform} здесь не нужен: подсказка рисуется поверх всего и
     * обрезкой не пользуется, а лишняя запись сломала бы баланс стека.
     */
    public static void pushPopup(MatrixStack m, float px, float py, float scale, float dy) {
        m.push();
        m.translate(0f, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    /** Снимает трансформацию, наложенную {@link #pushPopup}. */
    public static void popPopup(MatrixStack m) {
        m.pop();
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
    public static boolean enforce(MatrixStack m, float width, float height, boolean container) {
        org.joml.Matrix4f p = m.peek().getPositionMatrix();
        if (!isIdentity(p.m00(), p.m01(), p.m10(), p.m11(), p.m30(), p.m31())) {
            return false;
        }
        m.push();
        forward(m, width, height, container);
        return true;
    }

    /** Снимает трансформацию, наложенную {@link #enforce}. */
    public static void release(MatrixStack m) {
        m.pop();
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
    public static void saveBase(MatrixStack m) {
        BASE.set(m.peek().getPositionMatrix());
        baseSaved = true;
    }

    /** Ставит матрицу обратно в запомненное состояние. @return true, если положили уровень в стек */
    public static boolean pushBase(MatrixStack m) {
        if (!baseSaved) return false;
        m.push();
        m.peek().getPositionMatrix().set(BASE);
        return true;
    }

    public static void popBase(MatrixStack m) {
        m.pop();
    }

    /** Появление окна: сдвиг плюс масштаб вокруг пивота одним уровнем стека. */
    public static void pushAppear(MatrixStack m, float px, float py, float scale, float dx, float dy) {
        m.push();
        m.translate(dx, dy, 0f);
        if (scale != 1f) {
            m.translate(px, py, 0f);
            m.scale(scale, scale, 1f);
            m.translate(-px, -py, 0f);
        }
    }

    public static void popAppear(MatrixStack m) {
        m.pop();
    }
}
