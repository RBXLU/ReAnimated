package com.pycodder.reanimated.api;

public interface AnimationListener {

    default void onOpen(Object screen) {}

    default void onClose(Object screen, Object next) {}

    default void onFinish(Object screen) {}
}
