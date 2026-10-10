package com.pycodder.reanimated.anim;

public interface CascadeTarget {
    void reanimated$setCascade(int rank, int count);

    int reanimated$cascadeRank();

    int reanimated$cascadeCount();

    float reanimated$currentAlpha();
}
