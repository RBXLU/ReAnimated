package com.pycodder.reanimated.anim;

/**
 * Профиль анимации появления интерфейса — то, что настраивается в
 * {@code AnimProfileEditorScreen} и применяется к реальным экранам.
 *
 * Пока {@link #enabled} = false, работает старая схема (пресет + слайдеры).
 * Когда профиль включён, он полностью задаёт анимацию открытия: смещение,
 * начальный масштаб, прозрачность и каскад по элементам.
 *
 * Вся математика собрана здесь, чтобы превью в редакторе и настоящие экраны
 * считались одним и тем же кодом и гарантированно выглядели одинаково.
 */
public class AnimProfile {

    public boolean enabled = false;
    public int durationMs = 400;
    public float offsetX = 0f;
    public float offsetY = 15f;
    public float scaleX = 1f;
    public float scaleY = 1f;
    public float initialAlpha = 0f;
    public int cascadeDelayMs = 45;
    public CascadeOrder cascadeOrder = CascadeOrder.BOTTOM_TO_TOP;
    public PivotPoint pivot = PivotPoint.CENTER;
    public EasingType easing = EasingType.OUT_CUBIC;

    /** Минимальный масштаб: нулевой схлопнул бы матрицу и сломал обратную трансформацию фона. */
    private static final float MIN_SCALE = 0.01f;

    /** Номер шага каскада для виджета с рангом {@code rank} (ранг — сверху вниз). */
    public int slotFor(int rank, int count) {
        if (rank < 0 || count <= 1 || cascadeOrder == CascadeOrder.SIMULTANEOUS) return 0;
        return cascadeOrder == CascadeOrder.BOTTOM_TO_TOP ? (count - 1 - rank) : rank;
    }

    public int delayFor(int slot) {
        if (slot <= 0 || cascadeOrder == CascadeOrder.SIMULTANEOUS) return 0;
        return Math.max(0, cascadeDelayMs) * slot;
    }

    /** Полная длительность анимации экрана из {@code count} элементов (мс). */
    public int totalMs(int count) {
        return Math.max(1, durationMs) + delayFor(Math.max(0, count - 1));
    }

    /** Сглаженный прогресс 0..1 (OUT_BACK может дать >1 — это отскок) для шага каскада. */
    public float progress(float elapsedMs, int slot) {
        float t = (elapsedMs - delayFor(slot)) / Math.max(1, durationMs);
        return easing.apply(Easing.clamp01(t));
    }

    public float offsetXAt(float eased) {
        return (1f - eased) * offsetX;
    }

    public float offsetYAt(float eased) {
        return (1f - eased) * offsetY;
    }

    public float scaleXAt(float eased) {
        return Math.max(MIN_SCALE, Easing.lerp(scaleX, 1f, eased));
    }

    public float scaleYAt(float eased) {
        return Math.max(MIN_SCALE, Easing.lerp(scaleY, 1f, eased));
    }

    public float alphaAt(float eased) {
        return Easing.clamp01(Easing.lerp(initialAlpha, 1f, eased));
    }

    /** true, если при таком прогрессе трансформация ничего не меняет (можно не трогать матрицу). */
    public boolean identityAt(float eased) {
        return offsetXAt(eased) == 0f && offsetYAt(eased) == 0f
            && scaleXAt(eased) == 1f && scaleYAt(eased) == 1f;
    }

    /** Подставляет значения по умолчанию вместо null'ов после чтения старого/битого конфига. */
    public void sanitize() {
        if (cascadeOrder == null) cascadeOrder = CascadeOrder.BOTTOM_TO_TOP;
        if (pivot == null) pivot = PivotPoint.CENTER;
        if (easing == null) easing = EasingType.OUT_CUBIC;
        durationMs = Math.max(1, durationMs);
        cascadeDelayMs = Math.max(0, cascadeDelayMs);
        initialAlpha = Easing.clamp01(initialAlpha);
    }

    public AnimProfile copy() {
        AnimProfile p = new AnimProfile();
        p.copyFrom(this);
        return p;
    }

    public void copyFrom(AnimProfile o) {
        this.enabled = o.enabled;
        this.durationMs = o.durationMs;
        this.offsetX = o.offsetX;
        this.offsetY = o.offsetY;
        this.scaleX = o.scaleX;
        this.scaleY = o.scaleY;
        this.initialAlpha = o.initialAlpha;
        this.cascadeDelayMs = o.cascadeDelayMs;
        this.cascadeOrder = o.cascadeOrder;
        this.pivot = o.pivot;
        this.easing = o.easing;
    }
}
