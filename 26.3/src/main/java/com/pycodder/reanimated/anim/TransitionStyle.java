package com.pycodder.reanimated.anim;

/**
 * Как выглядит переход между двумя экранами (старый уходит — новый приходит).
 * Отдельный слой поверх пресета: пресет отвечает за появление экрана «вообще»,
 * а стиль перехода — за направление движения при навигации туда/обратно.
 */
public enum TransitionStyle {
    /** Переходов нет — экран просто сменяется, как в ванили. */
    NONE("No transition"),
    /** Старый экран уезжает вбок, новый приезжает с противоположной стороны. */
    SLIDE("Slide sideways"),
    /** Старый экран отдаляется, новый выходит на него сверху (или наоборот на возврате). */
    ZOOM("Zoom"),
    /** Сдвиг и масштаб вместе. */
    SLIDE_ZOOM("Slide + zoom");

    public final String display;

    TransitionStyle(String display) {
        this.display = display;
    }

    public boolean isSlide() {
        return this == SLIDE || this == SLIDE_ZOOM;
    }

    public boolean isZoom() {
        return this == ZOOM || this == SLIDE_ZOOM;
    }
}
