package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Контейнерные экраны (Minecraft 26.x). Сам выезд/масштаб панели делает ScreenMixin
 * (общая обёртка), а блюр остаётся на месте (встречный трансформ в extractBackground).
 * Здесь — плавно догоняющая курсор подсветка слота (рисуется в RETURN extractRenderState,
 * то есть внутри общего трансформа экрана — значит едет вместе со слотами).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin implements com.pycodder.reanimated.anim.PanelBounds {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Override
    public int reanimated$panelLeft() {
        return this.leftPos;
    }

    @Override
    public int reanimated$panelTop() {
        return this.topPos;
    }

    @Override
    public int reanimated$panelBottom() {
        return this.topPos + this.imageHeight;
    }
    @Shadow protected Slot hoveredSlot;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;
    /** Сдвинута ли сейчас ванильная подсветка: стек матриц надо вернуть на выходе. */
    @Unique private boolean reanimated$highlightPushed = false;

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
     * Задняя часть подсветки рисуется до предметов, передняя — после них; сдвигаются обе.
     */
    @Inject(method = "extractSlotHighlightBack", at = @At("HEAD"))
    private void reanimated$highlightBackHead(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        reanimated$pushHighlight(extractor, this.hoveredSlot);
    }

    @Inject(method = "extractSlotHighlightBack", at = @At("RETURN"))
    private void reanimated$highlightBackTail(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        reanimated$popHighlight(extractor);
    }

    @Inject(method = "extractSlotHighlightFront", at = @At("HEAD"))
    private void reanimated$highlightFrontHead(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        reanimated$pushHighlight(extractor, this.hoveredSlot);
    }

    @Inject(method = "extractSlotHighlightFront", at = @At("RETURN"))
    private void reanimated$highlightFrontTail(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        reanimated$popHighlight(extractor);
    }

    /**
     * Сдвигает ванильную подсветку слота {@code slot} к точке, где сейчас плавно едет
     * рамка. Когда рамка уже на месте, ничего не делает.
     */
    @Unique
    private void reanimated$pushHighlight(GuiGraphicsExtractor extractor, Slot slot) {
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
        UiTransform.pushAppear(extractor.pose(), 0f, 0f, 1f, dx, dy);
        reanimated$highlightPushed = true;
    }

    @Unique
    private void reanimated$popHighlight(GuiGraphicsExtractor extractor) {
        if (!reanimated$highlightPushed) return;
        reanimated$highlightPushed = false;
        UiTransform.popAppear(extractor.pose());
    }
}
