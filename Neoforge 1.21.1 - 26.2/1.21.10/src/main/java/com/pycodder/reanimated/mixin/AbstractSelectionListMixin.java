package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Каскад строк списка: моды в Mod Menu, сервера, миры, ресурспаки, наборы данных.
 *
 * Экран целиком едет по пресету (ScreenMixin), а сам фрейм-список раньше намеренно
 * стоял на месте — см. {@code ClickableWidgetMixin.reanimated$freezeFrame}. Теперь
 * список едет вместе с экраном, а каждая его строка вдобавок поднимается на своё место
 * по очереди сверху вниз: строка сдвигается по профилю {@code profileLists} с задержкой
 * по её номеру. Выезжающая строка обрезается рамкой списка — обрезка следует за матрицей,
 * поэтому строки «выплывают» из-под края, а не поверх соседних экранов.
 *
 * Прозрачность не трогаем: строка списка — чужой виджет произвольного мода, поля alpha
 * у неё нет. Когда анимация отыграла ({@code identityAt}), матрица не трогается вовсе.
 */
@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {

    @Shadow public abstract java.util.List<?> children();

    @Unique private static final int REANIMATED$MAX_SLOT = 14;

    @Unique private boolean reanimated$pushed = false;

    @Unique
    private void reanimated$begin(GuiGraphics context, int index) {
        reanimated$pushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.listsEnabled) {
            return;
        }
        AnimProfile p = c.profileLists;
        if (!p.enabled) {
            return;
        }
        if (!Anim.shouldAnimate(Minecraft.getInstance().screen)) {
            return;
        }
        float e = Anim.elapsed(System.currentTimeMillis());
        if (e == Float.MAX_VALUE) {
            return; // экран открыт не через мод — анимировать нечего
        }
        int count = Math.max(1, this.children().size());
        int rank = Math.max(0, index);
        // Потолок каскада: в списке модов строк бывают сотни, и без него нижние ждали бы
        // своей очереди секундами после открытия экрана. Дальше REANIMATED$MAX_SLOT все
        // строки едут одной волной — на экране их всё равно видно не больше десятка.
        int slot = Math.min(p.slotFor(rank, count), REANIMATED$MAX_SLOT);
        float eased = p.progress(e * 1000f, slot);
        if (p.identityAt(eased)) {
            return; // строка уже на месте
        }

        Matrix3x2fStack m = context.pose();
        m.pushMatrix();
        m.translate(p.offsetXAt(eased), p.offsetYAt(eased));
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(GuiGraphics context) {
        if (reanimated$pushed) {
            context.pose().popMatrix();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/client/gui/GuiGraphics;IIFLnet/minecraft/client/gui/components/AbstractSelectionList$Entry;)V", at = @At("HEAD"))
    private void reanimated$rowHead(GuiGraphics context, int mouseX, int mouseY, float delta,
                                    @Coerce Object entry, CallbackInfo ci) {
        // В этой сигнатуре номера строки нет — берём его из самого списка.
        reanimated$begin(context, this.children().indexOf(entry));
    }

    @Inject(method = "renderItem(Lnet/minecraft/client/gui/GuiGraphics;IIFLnet/minecraft/client/gui/components/AbstractSelectionList$Entry;)V", at = @At("RETURN"))
    private void reanimated$rowTail(GuiGraphics context, int mouseX, int mouseY, float delta,
                                    @Coerce Object entry, CallbackInfo ci) {
        reanimated$end(context);
    }
}
