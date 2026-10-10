package com.pycodder.reanimated.api;

public interface CustomPreset {

    String id();

    String displayName();

    PresetMotion motionAt(float progress);
}
