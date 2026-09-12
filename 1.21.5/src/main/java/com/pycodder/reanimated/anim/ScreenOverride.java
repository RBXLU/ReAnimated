package com.pycodder.reanimated.anim;

public class ScreenOverride {

    public boolean animate = true;

    public UiPreset preset = UiPreset.INHERIT;

    public int speedTicks = 0;

    public String customPresetId = "";

    public boolean isDefault() {
        return animate && (preset == null || preset == UiPreset.INHERIT) && speedTicks <= 0
            && (customPresetId == null || customPresetId.isEmpty());
    }

    public void sanitize() {
        if (preset == null) preset = UiPreset.INHERIT;
        if (customPresetId == null) customPresetId = "";
        if (speedTicks < 0) speedTicks = 0;
        if (speedTicks > 40) speedTicks = 40;
    }
}
