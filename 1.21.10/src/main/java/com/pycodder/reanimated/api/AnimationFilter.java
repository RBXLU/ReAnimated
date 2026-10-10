package com.pycodder.reanimated.api;

public interface AnimationFilter {

    enum Result {
        DEFAULT,
        ANIMATE,
        SKIP
    }

    Result test(Object screen);
}
