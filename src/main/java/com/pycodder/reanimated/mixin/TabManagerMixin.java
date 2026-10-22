package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.TabAnim;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/** Смена вкладки: запоминаем старую вкладку и запускаем приход виджетов новой. */
@Mixin(TabManager.class)
public abstract class TabManagerMixin {

    @Shadow public abstract Tab getCurrentTab();

    @Unique private Tab reanimated$oldTab;

    @Inject(method = "setCurrentTab", at = @At("HEAD"))
    private void reanimated$before(Tab tab, boolean clickSound, CallbackInfo ci) {
        reanimated$oldTab = getCurrentTab();
    }

    @Inject(method = "setCurrentTab", at = @At("RETURN"))
    private void reanimated$after(Tab tab, boolean clickSound, CallbackInfo ci) {
        Tab old = reanimated$oldTab;
        reanimated$oldTab = null;
        if (old == tab) return;
        List<AbstractWidget> widgets = new ArrayList<>();
        tab.visitChildren(widgets::add);
        TabAnim.begin(this, old, tab, widgets);
    }
}
