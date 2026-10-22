package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Контейнерные экраны. Весь экран (панель, слоты, МОДЕЛЬ ИГРОКА — всё рисуется
 * внутри renderWithTooltip) выезжает/масштабируется вместе через ScreenMixin.
 * Здесь:
 *  - возвращаем РАЗМЫТЫЙ ФОН на место ОБРАТНОЙ трансформацией (масштаб+сдвиг)
 *    вокруг renderBackground, снимая её прямо перед отрисовкой панели — так
 *    панель и модель игрока едут вместе со слотами, а блюр стоит;
 *  - рисуем плавно догоняющую курсор подсветку слота.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin implements com.pycodder.reanimated.anim.PanelBounds {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected Slot focusedSlot;

    @Override
    public int reanimated$panelLeft() {
        return this.x;
    }

    @Override
    public int reanimated$panelTop() {
        return this.y;
    }

    @Override
    public int reanimated$panelBottom() {
        return this.y + this.backgroundHeight;
    }

    @Unique private boolean reanimated$blurPushed = false;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;
    /** Сдвинута ли сейчас ванильная подсветка: стек матриц надо вернуть на выходе. */
    @Unique private boolean reanimated$highlightPushed = false;

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$blurPush(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$blurPushed = Anim.transformActive(true) && Anim.shouldAnimate(this);
        if (reanimated$blurPushed) {
            reanimated$blurPushed = UiTransform.pushBase(context.getMatrices());
        }
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V",
            shift = At.Shift.BEFORE))
    private void reanimated$blurPop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$blurPushed) {
            UiTransform.popBase(context.getMatrices());
        }
    }

    /*
     * Плавная подсветка слота — это ванильная подсветка, сдвинутая туда, где сейчас едет
     * рамка. Свою поверх мы не рисуем: иначе под курсором лежали бы сразу две подсветки —
     * ванильная, которая перескакивает, и наша, которая её догоняет, — и слот выходил
     * почти белым. Так подсветка выглядит ровно как в ванили или в ресурспаке (у паков
     * в стиле OreUI это рамка с обводкой), только переезжает плавно.
     *
     * Ваниль рисует подсветку в координатах ПАНЕЛИ ({@code slot.x, slot.y}): матрица к
     * этому моменту уже сдвинута на угол контейнера. Наш сдвиг ложится внутрь, поэтому
     * всё, что другой мод кладёт вокруг ванильной подсветки (Smooth Scrolling при
     * прокрутке креатива сдвигает сетку на долю слота и обрезает её), достаётся и ей.
     *
     * В 1.21.1 отдельного метода подсветки ещё нет: ваниль зовёт статический
     * {@code drawSlotHighlight(context, slot.x, slot.y, 0)} из цикла по слотам, и сдвиг
     * кладётся вокруг этого вызова.
     */
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/gui/DrawContext;III)V"))
    private void reanimated$highlightHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$pushHighlight(context, this.focusedSlot);
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/gui/DrawContext;III)V",
            shift = At.Shift.AFTER))
    private void reanimated$highlightTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$popHighlight(context);
    }

    /**
     * Подсветку ваниль зовёт только для слота под курсором. Если такого слота в кадре
     * не было, рамку надо погасить здесь — иначе при следующем наведении она приехала
     * бы из давно покинутого места.
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void reanimated$highlightReset(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.focusedSlot == null || !ReAnimatedConfig.get().slotHighlightEnabled) {
            reanimated$slotX = Float.NaN;
            reanimated$slotY = Float.NaN;
        }
    }

    /**
     * Сдвигает ванильную подсветку слота {@code slot} к точке, где сейчас плавно едет
     * рамка. Когда рамка уже на месте, ничего не делает.
     */
    @Unique
    private void reanimated$pushHighlight(DrawContext context, Slot slot) {
        reanimated$highlightPushed = false;
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.slotHighlightEnabled || c.pvpMode) {
            reanimated$slotX = Float.NaN;
            return;
        }
        if (slot == null) {
            reanimated$slotX = Float.NaN;
            reanimated$slotY = Float.NaN;
            return;
        }

        long now = Anim.now();
        float dt = reanimated$slotTime == 0L ? 0f : (now - reanimated$slotTime) / 1000f;
        if (dt > 0.2f) dt = 0.2f;
        reanimated$slotTime = now;

        if (Float.isNaN(reanimated$slotX)) {
            reanimated$slotX = slot.x;
            reanimated$slotY = slot.y;
        } else {
            reanimated$slotX = Easing.approach(reanimated$slotX, slot.x, dt, c.slotHighlightSpeed);
            reanimated$slotY = Easing.approach(reanimated$slotY, slot.y, dt, c.slotHighlightSpeed);
        }

        int dx = Math.round(reanimated$slotX) - slot.x;
        int dy = Math.round(reanimated$slotY) - slot.y;
        if (dx == 0 && dy == 0) return;
        UiTransform.pushAppear(context.getMatrices(), 0f, 0f, 1f, dx, dy);
        reanimated$highlightPushed = true;
    }

    @Unique
    private void reanimated$popHighlight(DrawContext context) {
        if (!reanimated$highlightPushed) return;
        reanimated$highlightPushed = false;
        UiTransform.popAppear(context.getMatrices());
    }
}
