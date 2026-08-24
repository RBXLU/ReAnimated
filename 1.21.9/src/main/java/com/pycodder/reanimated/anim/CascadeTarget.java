package com.pycodder.reanimated.anim;

/** Implemented by a mixin on {@code ClickableWidget}: the widget's place in the cascade. */
public interface CascadeTarget {
    void reanimated$setCascade(int rank, int count);

    int reanimated$cascadeRank();

    int reanimated$cascadeCount();

    float reanimated$currentAlpha();
}
