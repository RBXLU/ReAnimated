package com.pycodder.reanimated.api;

/**
 * Уведомления о жизненном цикле анимации экрана. Вызываются в потоке отрисовки,
 * поэтому тяжёлую работу из них лучше не делать.
 *
 * Экран передаётся как {@code Object}: у мода одна кодовая база на разные версии
 * игры и разные маппинги, а тип {@code Screen} в них называется по-разному.
 * Приводите к своему типу через {@code instanceof}.
 */
public interface AnimationListener {

    /** Экран начал появляться. */
    default void onOpen(Object screen) {}

    /** Экран начал закрываться — либо в мир, либо переходом на {@code next} (может быть null). */
    default void onClose(Object screen, Object next) {}

    /** Анимация экрана доиграла: он либо полностью на месте, либо уже сменён. */
    default void onFinish(Object screen) {}
}
