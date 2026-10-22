package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.compat.BlurPlusCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Фон за экраном на время закрытия — Blur+.
 *
 * Пока ReAnimated отыгрывает закрытие в мир, ванильный фон за экраном не рисуется, а
 * вместо него фон рисует сам Blur+ (BlurPlusCompat.renderBackground — тот же вызов,
 * которым он дорисовывает угасание, когда экрана уже нет). Blur+ решает, проявлять фон
 * или гасить, по тому, зовёт ли экран в этом кадре методы фона. Отменённый вызов до
 * него не доходит — он видит «у экрана фона больше нет» и гасит размытие и градиент
 * одновременно с уходом панели. Без этого фон держался бы в полную силу всё закрытие
 * и начинал бы гаснуть, только когда панель уже уехала.
 *
 * Перехват стоит на «листовых» методах фона (размытие, затемнение меню, затемнение в
 * игре), а не на общем методе фона: многие экраны переопределяют общий метод и зовут
 * листовые напрямую, мимо super (так устроено, например, меню паузы). И это ровно те
 * методы, на начале которых стоят хуки Blur+.
 *
 * Отдельный миксин — ради приоритета. На HEAD одного метода колбэки разных модов идут
 * в порядке применения миксинов: миксин с МЕНЬШИМ приоритетом применяется раньше, и
 * его колбэк оказывается дальше от тела метода — то есть срабатывает ПЕРВЫМ (проверено
 * по байткоду). У Blur+ приоритет по умолчанию (1000), у ScreenMixin — 1500, и с ним
 * Blur+ отметил бы фон раньше, чем мы отменим вызов. Здесь 500: наша отмена
 * срабатывает до Blur+.
 *
 * Только закрытие в мир: при переходе на другой экран фон у следующего такой же, и
 * гасить его незачем. Без Blur+ этот миксин ничего не делает.
 */
@Mixin(value = Screen.class, priority = 500)
public abstract class ScreenBackgroundMixin {

    @Unique
    private boolean reanimated$blurTakesOver(GuiGraphics graphics) {
        if (!Anim.isClosing() || Anim.isTransitioning()) return false;
        if (!BlurPlusCompat.present() || !Anim.shouldAnimate(this)) return false;
        // Фон — в неподвижной системе координат, как и ванильный (см. UiTransform.pushBase).
        // Blur+ сам следит, чтобы за кадр фон был нарисован один раз, поэтому звать его из
        // каждого отменённого метода безопасно.
        boolean pushed = UiTransform.pushBase(graphics.pose());
        boolean drawn = BlurPlusCompat.renderBackground(graphics);
        if (pushed) UiTransform.popBase(graphics.pose());
        return drawn;
    }

    @Inject(method = "renderBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$blurredBg(GuiGraphics graphics, CallbackInfo ci) {
        if (reanimated$blurTakesOver(graphics)) ci.cancel();
    }

    @Inject(method = "renderMenuBackground(Lnet/minecraft/client/gui/GuiGraphics;IIII)V", at = @At("HEAD"), cancellable = true)
    private void reanimated$menuBg(GuiGraphics graphics, int x, int y, int w, int h, CallbackInfo ci) {
        if (reanimated$blurTakesOver(graphics)) ci.cancel();
    }

    /** Экраны «в игре» (контейнеры без размытия и т.п.) рисуют фон этим методом. */
    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$transparentBg(GuiGraphics graphics, CallbackInfo ci) {
        if (reanimated$blurTakesOver(graphics)) ci.cancel();
    }
}
