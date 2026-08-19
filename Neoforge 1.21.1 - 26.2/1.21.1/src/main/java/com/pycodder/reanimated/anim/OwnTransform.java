package com.pycodder.reanimated.anim;

/**
 * Учёт ТОЛЬКО СВОИХ трансформаций интерфейса — отдельно от матрицы отрисовки.
 *
 * Нужен обрезке ({@code DrawContextScissorMixin}) на 1.21.1–1.21.3, где ваниль не
 * прогоняет прямоугольник {@code enableScissor} через матрицу. Читать саму матрицу
 * там нельзя: в ней лежит ВСЁ, что наложили в том числе чужие моды, а они пишут
 * координаты обрезки под ванильное поведение — «матрица не учитывается». Домножив
 * их ещё и на чужой сдвиг, мы уводим обрезку в сторону, и содержимое пропадает.
 * Так уже ломались список блоков в Rechiseled и сетка предметов в креативе при
 * плавной прокрутке (Smooth Scrolling): мод двигает матрицу, обрезку задаёт сырыми
 * координатами — и она уезжала вместе со скроллом.
 *
 * Поэтому здесь параллельно матрице копится только наш собственный сдвиг с
 * масштабом. Все трансформации мода — сдвиг и масштаб по осям, без поворота, так
 * что хватает четырёх чисел, а результат точен.
 *
 * Только для рендер-потока: интерфейс рисуется в один поток, синхронизация не нужна.
 */
public final class OwnTransform {

    private static final int MAX_DEPTH = 64;
    /** Сохранённые уровни: по четыре числа (sx, sy, tx, ty) на уровень. */
    private static final float[] STACK = new float[MAX_DEPTH * 4];

    private static int depth = 0;
    /** Сколько push'ей не поместилось в стек — чтобы pop не съел чужой уровень. */
    private static int overflow = 0;

    private static float sx = 1f;
    private static float sy = 1f;
    private static float tx = 0f;
    private static float ty = 0f;

    private OwnTransform() {
    }

    /**
     * Накладывает наш локальный сдвиг с масштабом поверх уже накопленного:
     * {@code x -> scaleX * x + transX} в системе координат текущего уровня.
     */
    public static void push(float scaleX, float scaleY, float transX, float transY) {
        if (depth >= MAX_DEPTH) {
            overflow++;
            return;
        }
        int i = depth * 4;
        STACK[i] = sx;
        STACK[i + 1] = sy;
        STACK[i + 2] = tx;
        STACK[i + 3] = ty;
        depth++;

        tx = sx * transX + tx;
        ty = sy * transY + ty;
        sx *= scaleX;
        sy *= scaleY;
    }

    public static void pop() {
        if (overflow > 0) {
            overflow--;
            return;
        }
        if (depth <= 0) {
            return;
        }
        depth--;
        int i = depth * 4;
        sx = STACK[i];
        sy = STACK[i + 1];
        tx = STACK[i + 2];
        ty = STACK[i + 3];
    }

    /**
     * Сброс в начале кадра. Страховка: если какая-то ветка вышла раньше времени и не
     * сняла свой уровень, рассинхрон не копится от кадра к кадру.
     */
    public static void reset() {
        depth = 0;
        overflow = 0;
        sx = 1f;
        sy = 1f;
        tx = 0f;
        ty = 0f;
    }

    /** Ничего своего не наложено — обрезку трогать не нужно вовсе. */
    public static boolean identity() {
        return sx == 1f && sy == 1f && tx == 0f && ty == 0f;
    }

    public static int mapX(int v) {
        return Math.round(sx * v + tx);
    }

    public static int mapY(int v) {
        return Math.round(sy * v + ty);
    }
}
