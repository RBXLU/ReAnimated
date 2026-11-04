package com.pycodder.reanimated.mixin;

import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.Easing;
import com.pycodder.reanimated.anim.TabAnim;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft 26.x: точка отрисовки виджета — {@code extractRenderState}.
 *
 * Фреймы-списки ({@code AbstractContainerWidget}) здесь не увеличиваются под курсором и не
 * каскадируются покнопочно — у них свой каскад по строкам (EntryListWidgetMixin) (см. {@link #reanimated$freezeFrame}).
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
    /** Момент нажатия на эту кнопку; 0 — анимация вдавливания не играет. */
    @Unique private long reanimated$pressTime = 0L;
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

    /**
     * Порог видимости. Ниже него ваниль (TextRenderer.tweakTransparency) считает цвет текста
     * "почти прозрачным" и делает надпись ПОЛНОСТЬЮ непрозрачной: рамка уже невидима, а текст
     * вспыхивает сплошным. Поэтому настолько погасший виджет не рисуем вовсе.
     */
    @Unique private static final float REANIMATED$MIN_ALPHA = 4f / 255f;

    /**
     * Шаг каскада этого виджета на текущем кадре. {@link Float#NaN} — профиль выключен
     * или виджет вне каскада.
     *
     * Считается один раз за кадр в {@link #reanimated$tooFaint()}, а
     * {@link #reanimated$applyProfile} берёт готовое значение.
     */
    @Unique private float reanimated$ease = Float.NaN;

    @Unique
    private boolean reanimated$tooFaint() {
        AnimProfile p = Anim.profile();
        if (!p.enabled || reanimated$rank < 0) {
            reanimated$ease = Float.NaN;
            return false;
        }
        reanimated$ease = Anim.profileEase(p.slotFor(reanimated$rank, reanimated$count));
        return this.alpha * p.alphaAt(reanimated$ease) < REANIMATED$MIN_ALPHA;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void reanimated$preRender(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        reanimated$pushed = 0;
        reanimated$savedAlpha = -1f;
        if ((Object) this instanceof AbstractContainerWidget) {
            reanimated$applyTabSwitch(extractor);
            reanimated$freezeFrame(extractor);
            return;
        }
        // Отмена отрисовки — до любых push: на отменённом вызове RETURN не наступит.
        if (reanimated$tooFaint()) {
            ci.cancel();
            return;
        }
        reanimated$applyTabSwitch(extractor);

        reanimated$applyProfile(extractor);
        if (reanimated$isTextField()) {
            return; // поле ввода под курсором не трогаем — см. reanimated$isTextField
        }
        reanimated$applyHover(extractor);
        reanimated$applyPress(extractor);
    }

    /**
     * Поля ввода из наведения и нажатия исключены. Масштаб — чисто визуальный: клики,
     * курсор и выделение текста ваниль считает по НЕмасштабированным координатам, поэтому
     * увеличенное поле «плывёт» под мышью и попасть в нужный символ становится нечем.
     * Каскад появления ({@code applyProfile}) полям оставлен — он отыгрывает один раз
     * при открытии экрана и с вводом не пересекается.
     */
    @Unique
    private boolean reanimated$isTextField() {
        return (Object) this instanceof net.minecraft.client.gui.components.EditBox;
    }

    /**
     * Отметка нажатия. Цепляемся к звуку клика, а не к {@code onPress}: ваниль играет
     * его и при клике мышью, и при нажатии Enter/Пробела на выбранной кнопке, и делает
     * это уже после проверок active/visible. Так вдавливание срабатывает ровно тогда,
     * когда кнопка действительно сработала.
     */
    @Inject(method = "playDownSound", at = @At("HEAD"))
    private void reanimated$onDown(net.minecraft.client.sounds.SoundManager soundManager, CallbackInfo ci) {
        if (ReAnimatedConfig.get().pressEnabled) {
            reanimated$pressTime = System.currentTimeMillis();
        }
    }

    /**
     * Второй триггер нажатия — сам клик мышью. Одного {@code playDownSound} мало:
     * ваниль переопределяет его пустым у слайдеров (они не щёлкают) и своим у полей
     * ввода, так что до базовой реализации — а значит и до нас — вызов не доходит и
     * ползунок «проседал» бы только от клавиатуры. Отмечаем на RETURN и только если
     * виджет клик действительно принял.
     */
    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void reanimated$onMouseDown(net.minecraft.client.input.MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue()) && ReAnimatedConfig.get().pressEnabled) {
            reanimated$pressTime = System.currentTimeMillis();
        }
    }

    /**
     * Возврат фрейма-списка (сервера, миры, ресурспаки, список опций, список модов)
     * на место — только когда анимация списков ВЫКЛЮЧЕНА. Экран уже сдвинут
     * ScreenMixin'ом, поэтому здесь снимаем сдвиг обратно, ровно с этого виджета.
     *
     * Исторически список замораживался всегда: его содержимое ехало вместе с экраном,
     * а обрезка (scissor) считалась в экранных координатах и оставалась на месте, из-за
     * чего фрейм разъезжался сам с собой. С 1.3 обрезка следует за матрицей
     * (DrawContextScissorMixin; в 1.21.6+ и 26.x это делает уже сама ваниль), поэтому
     * по умолчанию список едет вместе со всем экраном.
     */
    @Unique
    private void reanimated$freezeFrame(GuiGraphicsExtractor extractor) {
        // Списки едут вместе с экраном, а их строки появляются каскадом
        // (EntryListWidgetMixin). Заморозка осталась запасным вариантом — на случай,
        // если игрок выключил анимацию списков в настройках.
        if (ReAnimatedConfig.get().listsEnabled) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (!Anim.shouldAnimate(client.gui.screen())) {
            return; // экран и так не анимируется — снимать нечего
        }
        boolean container = client.gui.screen() instanceof AbstractContainerScreen;
        if (!Anim.transformActive(container)) {
            return;
        }
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.inverse(m, extractor.guiWidth(), extractor.guiHeight(), container);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void reanimated$postRender(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        while (reanimated$pushed > 0) {
            extractor.pose().popMatrix();
            reanimated$pushed--;
        }
        if (reanimated$savedAlpha >= 0f) {
            this.alpha = reanimated$savedAlpha;
            reanimated$savedAlpha = -1f;
        }
    }

    /**
     * Приход виджета только что открытой вкладки (TabAnim): сдвиг со стороны, куда
     * переключились, и проявление. Фреймы-списки только сдвигаются — прозрачность
     * виджета на их строки не действует.
     */
    @Unique
    private void reanimated$applyTabSwitch(GuiGraphicsExtractor extractor) {
        float p = TabAnim.progress(this);
        if (Float.isNaN(p)) return;
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        m.translate(TabAnim.offsetX(p), 0f);
        if (!((Object) this instanceof AbstractContainerWidget)) {
            if (reanimated$savedAlpha < 0f) reanimated$savedAlpha = this.alpha;
            this.alpha = this.alpha * Math.max(0.02f, Easing.clamp01(p));
        }
    }

    @Unique
    private void reanimated$applyProfile(GuiGraphicsExtractor extractor) {
        AnimProfile p = Anim.profile();
        // Ранга нет у виджетов вне анимируемого экрана — например, у превью
        // в редакторе профиля: оно рисует и анимирует себя само.
        if (!p.enabled || reanimated$rank < 0) {
            return;
        }

        // Шаг каскада уже посчитан в reanimated$tooFaint() этого же кадра.
        float own = Float.isNaN(reanimated$ease)
            ? Anim.profileEase(p.slotFor(reanimated$rank, reanimated$count))
            : reanimated$ease;

        float a = p.alphaAt(own);
        if (a < 1f) {
            if (reanimated$savedAlpha < 0f) reanimated$savedAlpha = this.alpha;
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

        // Время берём общее для кадра: столько раз за кадр дёргать системные часы
        // незачем, и заодно все виджеты сглаживаются от одного и того же момента.
        long now = Anim.now();
        float dt = reanimated$lastTime == 0L ? 0f : (now - reanimated$lastTime) / 1000f;
        if (dt > 0.2f) dt = 0.2f;
        reanimated$lastTime = now;

        float target = isHovered() ? 1f : 0f;
        if (reanimated$hover != target) {
            reanimated$hover = Easing.approach(reanimated$hover, target, dt, c.hoverSpeed);
            // Приближение асимптотическое и само в цель не приходит — дотягиваем вручную,
            // иначе проверка выше не сработала бы никогда и Math.exp считался бы каждый
            // кадр на каждый виджет экрана, даже когда все давно стоят на месте.
            if (Math.abs(reanimated$hover - target) < 0.001f) reanimated$hover = target;
        }

        if (reanimated$hover < 0.001f) {
            return; // не наведено — без накладных расходов
        }

        float scale = 1f + c.hoverScale * reanimated$hover;
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.pivotScale(m, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale, scale);
    }

    /**
     * Вдавливание при нажатии: кнопка быстро уменьшается и упруго возвращается
     * ({@link Easing#press}). Играет поверх наведения — они складываются, поэтому
     * нажатая кнопка остаётся увеличенной под курсором и «проседает» из этого состояния.
     */
    @Unique
    private void reanimated$applyPress(GuiGraphicsExtractor extractor) {
        if (reanimated$pressTime == 0L) {
            return;
        }
        ReAnimatedConfig c = ReAnimatedConfig.get();
        float durationMs = Math.max(0.01f, c.pressDuration) * 1000f;
        float t = (System.currentTimeMillis() - reanimated$pressTime) / durationMs;
        if (t >= 1f || !c.pressEnabled) {
            reanimated$pressTime = 0L; // отыграла (или выключили в настройках)
            return;
        }

        float scale = 1f - c.pressScale * Easing.press(t);
        Matrix3x2fStack m = extractor.pose();
        m.pushMatrix();
        reanimated$pushed++;
        UiTransform.pivotScale(m, getX() + getWidth() / 2f, getY() + getHeight() / 2f, scale, scale);
    }
}
