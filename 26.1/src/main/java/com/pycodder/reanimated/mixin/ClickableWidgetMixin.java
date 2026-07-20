package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.config.ReAnimatedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 26.x: точка отрисовки виджета — {@code extractRenderState}.
 *
 * Фреймы-списки ({@code AbstractContainerWidget}) исключены полностью — они не едут,
 * не каскадируются и не увеличиваются под курсором (см. {@link #reanimated$freezeFrame}).
 * Для остальных виджетов здесь две независимые вещи:
 *
 * 1. Покнопочный «вход» — анимация КНОПОК (профиль/Студия). Работает ПОВЕРХ
 *    анимации экрана (пресета): каждая кнопка сама смещается/масштабируется/гаснет
 *    вокруг своего пивота по своему шагу каскада.
 *
 * 2. Плавное увеличение при наведении курсора (работает в любом режиме).
 */
@Mixin(AbstractWidget.class)
public abstract class ClickableWidgetMixin implements CascadeTarget {

    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow public abstract boolean isHovered();
    @Shadow protected float alpha;

    @Unique private float reanimated$hover = 0f;
    @Unique private long reanimated$lastTime = 0L;
    @Unique private int reanimated$pushed = 0;
    @Unique private float reanimated$savedAlpha = -1f;

    @Unique private int reanimated$rank = -1;
    @Unique private int reanimated$count = 0;

    @Override
    public void reanimated$setCascade(int rank, int count) {
        reanimated$rank = rank;
        reanimated$count = count;
    }

    @Override
    public int reanimated$cascadeRank() {
        return reanimated$rank;
    }

    @Override
    public int reanimated$cascadeCount() {
        return reanimated$count;
    }

    @Override
    public float reanimated$currentAlpha() {
        return this.alpha;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void reanimated$preRender(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        reanimated$pushed = 0;
        reanimated$savedAlpha = -1f;
        if ((Object) this instanceof AbstractContainerWidget) {
            reanimated$freezeFrame(extractor);
            return;
        }
        reanimated$applyProfile(extractor);
        reanimated$applyHover(extractor);
    }

    /**
     * Фрейм-список (сервера, миры, ресурспаки, список опций) мод не анимирует вообще:
     * его содержимое едет вместе с экраном, а обрезка и прокрутка считаются в экранных
     * координатах и остаются на месте — фрейм разъезжается сам с собой. Экран уже сдвинут
     * ScreenMixin'ом, поэтому здесь снимаем сдвиг обратно — ровно с этого виджета.
     */
    @Unique
    private void reanimated$freezeFrame(GuiGraphicsExtractor extractor) {
        Minecraft client = Minecraft.getInstance();
        if (!Anim.shouldAnimate(client.screen)) {
            return; // экран и так не анимируется — снимать нечего
        }
        boolean container = client.screen instanceof AbstractContainerScreen;
        if (!Anim.transformActive(container)) {
            return;
        }
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.inverse(m, extractor.guiWidth(), extractor.guiHeight(), container);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void reanimated$postRender(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        while (reanimated$pushed > 0) {
            extractor.pose().popMatrix();
            reanimated$pushed--;
        }
        if (reanimated$savedAlpha >= 0f) {
            this.alpha = reanimated$savedAlpha;
            reanimated$savedAlpha = -1f;
        }
    }

    @Unique
    private void reanimated$applyProfile(GuiGraphicsExtractor extractor) {
        AnimProfile p = ReAnimatedConfig.get().profile;
        // Ранга нет у виджетов вне анимируемого экрана — например, у превью
        // в редакторе профиля: оно рисует и анимирует себя само.
        if (!p.enabled || reanimated$rank < 0) {
            return;
        }

        int slot = p.slotFor(reanimated$rank, reanimated$count);
        float own = Anim.profileEase(slot);

        float a = p.alphaAt(own);
        if (a < 1f) {
            reanimated$savedAlpha = this.alpha;
            this.alpha = this.alpha * a;
        }

        if (p.identityAt(own)) {
            return; // кнопка уже на месте — матрицу не трогаем
        }

        // Полный покнопочный «вход» ПОВЕРХ трансформации экрана (пресета): пресет
        // двигает экран целиком, этот слой — кнопки; они складываются.
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        float px = getX() + getWidth() * p.pivot.fx;
        float py = getY() + getHeight() * p.pivot.fy;
        m.translate(p.offsetXAt(own), p.offsetYAt(own));
        UiTransform.pivotScale(m, px, py, p.scaleXAt(own), p.scaleYAt(own));
    }

    @Unique
    private void reanimated$applyHover(GuiGraphicsExtractor extractor) {
        ReAnimatedConfig c = ReAnimatedConfig.get();
        if (!c.hoverEnabled) {
            return;
        }

        long now = System.currentTimeMillis();
        float dt = reanimated$lastTime == 0L ? 0f : (now - reanimated$lastTime) / 1000f;
        if (dt > 0.2f) dt = 0.2f;
        reanimated$lastTime = now;

        float target = isHovered() ? 1f : 0f;
        reanimated$hover = Easing.approach(reanimated$hover, target, dt, c.hoverSpeed);

        if (reanimated$hover < 0.001f) {
            return; // не наведено — без накладных расходов
        }

        float scale = 1f + c.hoverScale * reanimated$hover;
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.pivotScale(m, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale, scale);
    }
}
