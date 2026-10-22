package com.pycodder.reanimated.mixin;

import com.google.common.collect.ImmutableList;
import com.pycodder.reanimated.anim.TabAnim;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Полоса вкладок сообщает их порядок: сам менеджер вкладок порядка не знает, а по
 * нему решается, с какой стороны въезжает содержимое новой вкладки.
 */
@Mixin(TabNavigationWidget.class)
public abstract class TabNavigationWidgetMixin {

    @Shadow @Final private TabManager tabManager;
    @Shadow @Final private ImmutableList<Tab> tabs;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reanimated$registerOrder(CallbackInfo ci) {
        TabAnim.registerOrder(this.tabManager, this.tabs);
    }
}
