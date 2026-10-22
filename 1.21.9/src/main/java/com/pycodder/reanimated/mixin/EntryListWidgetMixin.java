package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
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
 * Экран целиком едет по пресету (ScreenMixin), и фрейм-список едет вместе
 * с ним (запасной вариант с заморозкой — {@code ClickableWidgetMixin.reanimated$freezeFrame}),
 * а каждая его строка вдобавок поднимается на своё место
 * по очереди сверху вниз: строка сдвигается по профилю {@code profileLists} с задержкой
 * по её номеру. Выезжающая строка обрезается рамкой списка — обрезка следует за матрицей,
 * поэтому строки «выплывают» из-под края, а не поверх соседних экранов.
 *
 * Прозрачность не трогаем: строка списка — чужой виджет произвольного мода, поля alpha
 * у неё нет. Когда анимация отыграла ({@code identityAt}), матрица не трогается вовсе.
 */
@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin {

    @Shadow public abstract java.util.List<?> children();

    @Unique private static final int REANIMATED$MAX_SLOT = 14;

    @Unique private boolean reanimated$pushed = false;

    /**
     * Идёт ли ещё каскад строк. Проверка стоит перед всем остальным, потому что
     * «остальное» на длинных списках недёшево: в одной из сигнатур ванили номера
     * строки нет и его приходится искать перебором по списку — на списке модов это
     * сотни сравнений на каждую видимую строку каждый кадр, и так всё время, пока
     * экран открыт, хотя каскад давно доиграл.
     */
    @Unique
    private boolean reanimated$rowsAnimating() {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.listsEnabled || !c.profileLists.enabled) {
            return false;
        }
        float e = Anim.elapsed(Anim.now());
        if (e == Float.MAX_VALUE) {
            return false; // экран открыт не через мод — анимировать нечего
        }
        // Позже этого момента все строки заведомо на месте: дальше потолка каскада
        // задержка не растёт.
        return e * 1000f < c.profileLists.totalMs(REANIMATED$MAX_SLOT + 1);
    }

    @Unique
    private void reanimated$begin(DrawContext context, int index) {
        reanimated$pushed = false;
        if (!Anim.shouldAnimate(MinecraftClient.getInstance().currentScreen)) {
            return;
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        AnimProfile p = c.profileLists;
        float e = Anim.elapsed(Anim.now());
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

        Matrix3x2fStack m = context.getMatrices();
        m.pushMatrix();
        m.translate(p.offsetXAt(eased), p.offsetYAt(eased));
        reanimated$pushed = true;
    }

    @Unique
    private void reanimated$end(DrawContext context) {
        if (reanimated$pushed) {
            context.getMatrices().popMatrix();
            reanimated$pushed = false;
        }
    }

    @Inject(method = "renderEntry(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/client/gui/widget/EntryListWidget$Entry;)V", at = @At("HEAD"))
    private void reanimated$rowHead(DrawContext context, int mouseX, int mouseY, float delta,
                                    @Coerce Object entry, CallbackInfo ci) {
        // В этой сигнатуре номера строки нет — ищем его перебором, но только пока
        // каскад идёт: без этого перебор шёл бы на каждый кадр открытого экрана.
        if (!reanimated$rowsAnimating()) {
            reanimated$pushed = false;
            return;
        }
        reanimated$begin(context, this.children().indexOf(entry));
    }

    @Inject(method = "renderEntry(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/client/gui/widget/EntryListWidget$Entry;)V", at = @At("RETURN"))
    private void reanimated$rowTail(DrawContext context, int mouseX, int mouseY, float delta,
                                    @Coerce Object entry, CallbackInfo ci) {
        reanimated$end(context);
    }
}
