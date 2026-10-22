package com.pycodder.reanimated.mixin;

import com.mojang.blaze3d.platform.Window;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.PanelBounds;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Контейнерные экраны (NeoForge / Mojmap). Весь экран (панель, слоты, МОДЕЛЬ ИГРОКА —
 * всё рисуется внутри renderWithTooltip) выезжает/масштабируется вместе через ScreenMixin.
 * Здесь:
 *  - возвращаем РАЗМЫТЫЙ ФОН на место ОБРАТНОЙ трансформацией (масштаб+сдвиг)
 *    вокруг renderBackground, снимая её прямо перед отрисовкой панели (renderBg) — так
 *    панель и модель игрока едут вместе со слотами, а блюр стоит;
 *  - рисуем плавно догоняющую курсор подсветку слота;
 *  - отдаём границы панели каскаду вкладок креатива ({@link PanelBounds}).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin implements PanelBounds {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow protected Slot hoveredSlot;

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

    @Unique private boolean reanimated$blurPushed = false;

    @Unique private float reanimated$slotX = Float.NaN;
    @Unique private float reanimated$slotY = Float.NaN;
    @Unique private long reanimated$slotTime = 0L;
    /** Сдвинута ли сейчас ванильная подсветка: стек матриц надо вернуть на выходе. */
    @Unique private boolean reanimated$highlightPushed = false;

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$blurPush(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$blurPushed = Anim.transformActive(true) && Anim.shouldAnimate(this);
        if (reanimated$blurPushed) {
            reanimated$blurPushed = UiTransform.pushBase(graphics.pose());
        }
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            shift = At.Shift.BEFORE))
    private void reanimated$blurPop(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$blurPushed) {
            UiTransform.popBase(graphics.pose());
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
     * всё, что другой мод кладёт вокруг ванильной подсветки (например, сдвиг сетки на
     * долю слота и обрезку при плавной прокрутке), достаётся и ей.
     * Задняя часть подсветки рисуется до предметов, передняя — после них; сдвигаются обе.
     */
    @Inject(method = "renderSlotHighlightBack", at = @At("HEAD"))
    private void reanimated$highlightBackHead(GuiGraphics graphics, CallbackInfo ci) {
        reanimated$pushHighlight(graphics, this.hoveredSlot);
    }

    @Inject(method = "renderSlotHighlightBack", at = @At("RETURN"))
    private void reanimated$highlightBackTail(GuiGraphics graphics, CallbackInfo ci) {
        reanimated$popHighlight(graphics);
    }

    @Inject(method = "renderSlotHighlightFront", at = @At("HEAD"))
    private void reanimated$highlightFrontHead(GuiGraphics graphics, CallbackInfo ci) {
        reanimated$pushHighlight(graphics, this.hoveredSlot);
    }

    @Inject(method = "renderSlotHighlightFront", at = @At("RETURN"))
    private void reanimated$highlightFrontTail(GuiGraphics graphics, CallbackInfo ci) {
        reanimated$popHighlight(graphics);
    }

    /**
     * Сдвигает ванильную подсветку слота {@code slot} к точке, где сейчас плавно едет
     * рамка. Когда рамка уже на месте, ничего не делает.
     */
    @Unique
    private void reanimated$pushHighlight(GuiGraphics graphics, Slot slot) {
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
        UiTransform.pushAppear(graphics.pose(), 0f, 0f, 1f, dx, dy);
        reanimated$highlightPushed = true;
    }

    @Unique
    private void reanimated$popHighlight(GuiGraphics graphics) {
        if (!reanimated$highlightPushed) return;
        reanimated$highlightPushed = false;
        UiTransform.popAppear(graphics.pose());
    }
}
