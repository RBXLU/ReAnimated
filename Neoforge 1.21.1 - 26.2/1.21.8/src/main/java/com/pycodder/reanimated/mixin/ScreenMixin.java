package com.pycodder.reanimated.mixin;

import org.joml.Matrix3x2fStack;
import com.pycodder.reanimated.anim.Anim;
import com.pycodder.reanimated.anim.ScreenNav;
import com.pycodder.reanimated.anim.ScreenRules;
import com.pycodder.reanimated.anim.CascadeTarget;
import com.pycodder.reanimated.anim.UiTransform;
import com.pycodder.reanimated.compat.BlurPlusCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Базовая анимация появления любого экрана — слой ЭКРАНА (пресет).
 * NeoForge / Mojmap, эпоха 1.21.6+ (Matrix3x2fStack).
 *
 * Время открытия и раздача рангов каскада делаются при первой отрисовке —
 * намеренно НЕ через инъекцию в init(...): сигнатура этого метода менялась
 * между версиями (в 1.21.11 это уже init(int,int)), а ленивый вариант
 * версионно-независим. Ранги переназначаются при смене размера экрана —
 * после ресайза виджеты пересоздаются, и без этого каскад бы пропал.
 *
 * Весь экран (фон + виджеты + ЛЮБОЙ текст) рисуется внутри
 * {@code renderWithTooltip} -> {@code this.render()}. Оборачивая его одной
 * трансформацией (сдвиг и/или масштаб от центра — по выбранному пресету),
 * гарантируем, что текст, кнопки и модель игрока появляются строго вместе.
 *
 * Фон (панорама/блюр) рисуется в {@code renderBackground} — к нему применяем
 * ОБРАТНУЮ трансформацию, чтобы он оставался неподвижным при любом пресете.
 * Для контейнеров renderBackground переопределён в AbstractContainerScreen —
 * там свой обработчик ({@code HandledScreenMixin}).
 */
/*
 * priority = 1500 (по умолчанию 1000) — сознательно ВЫШЕ обычного.
 *
 * Оборачиваем отрисовку экрана: push на HEAD, pop на RETURN. Порядок инжекторов
 * разных модов в одной точке задаётся порядком применения миксинов, а он идёт по
 * возрастанию приоритета, и колбэк миксина, применённого позже, оказывается ближе к
 * телу метода. Поэтому с высоким приоритетом наш pop на RETURN выполняется ПОСЛЕДНИМ
 * — и всё, что другие моды дорисовывают к экрану из своих хуков на RETURN, попадает
 * внутрь трансформации и едет вместе с экраном. (На HEAD, наоборот, наш колбэк идёт
 * после чужих — это проверено по байткоду; где нужно опередить чужой HEAD, стоит
 * отдельный миксин с низким приоритетом, см. ScreenBackgroundMixin.)
 *
 * Без этого оверлеи вроде списка предметов EMI/JEI/REI (их рисуют из хука на RETURN
 * того же метода) оставались бы снаружи: наш pop успевал бы раньше, и панель
 * стояла бы на месте, пока экран выезжает.
 */
@Mixin(value = Screen.class, priority = 1500)
public abstract class ScreenMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Unique private long reanimated$openTime = 0L;
    @Unique private boolean reanimated$fwdPushed = false;
    @Unique private boolean reanimated$bgPushed = false;
    @Unique private int reanimated$pauseFlag = -1;
    /** С какой стороны экран пришёл при переходе. 0 — открыт не переходом. */
    @Unique private int reanimated$enterDir = 0;

    /**
     * Подпись экрана для списка настроек. У инвентаря заголовок — «Создание»
     * (это заголовок сетки крафта), поэтому его подписываем сами.
     */
    @Unique
    private String reanimated$title() {
        if ((Object) this instanceof InventoryScreen) return Component.translatable("container.inventory").getString();
        Component title = ((Screen) (Object) this).getTitle();
        return title == null ? null : title.getString();
    }
    @Unique private int reanimated$lastW = -1;
    @Unique private int reanimated$lastH = -1;

    @Unique
    private boolean reanimated$isContainer() {
        return ((Object) this) instanceof AbstractContainerScreen;
    }

    /** Меню паузы у экрана не меняется — определяем один раз, а не каждый кадр. */
    @Unique
    private boolean reanimated$isPause() {
        if (reanimated$pauseFlag < 0) {
            reanimated$pauseFlag = Anim.isPauseScreen(this) ? 1 : 0;
        }
        return reanimated$pauseFlag == 1;
    }

    /**
     * Раздаёт виджетам экрана ранг сверху вниз — из него каждый виджет считает
     * свой шаг каскада. Порядок каскада применяется уже при отрисовке, поэтому
     * его смена в настройках видна сразу.
     */
    @Unique
    private void reanimated$assignCascade() {
        Anim.cascadeCount = 1;
        // Без ранга виджет не каскадируется вовсе — так экраны, которые мод не
        // анимирует (чат, экраны модов в режиме "только ванильные"), остаются нетронутыми.
        if (!Anim.shouldAnimate(this)) {
            return;
        }
        Screen self = (Screen) (Object) this;
        List<AbstractWidget> widgets = new ArrayList<>();
        for (GuiEventListener e : self.children()) {
            // Фреймы-списки (сервера/миры/ресурспаки/список опций) мод не анимирует
            // вовсе — они и шага в каскаде не занимают. См. ClickableWidgetMixin.
            if (e instanceof AbstractWidget w && w.visible && !(e instanceof AbstractContainerWidget)) {
                widgets.add(w);
            }
        }
        widgets.sort(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX));
        int count = widgets.size();
        for (int i = 0; i < count; i++) {
            ((CascadeTarget) widgets.get(i)).reanimated$setCascade(i, count);
        }
        Anim.cascadeCount = Math.max(1, count);
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"), cancellable = true)
    private void reanimated$wrapHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Закрытие этого экрана доиграло в прошлом кадре — выполняем его сейчас, до того
        // как экран что-либо нарисует, и сам экран в этот кадр уже не рисуем
        // (см. Anim.closeReadyScreen). Ничего в стек матриц мы ещё не положили.
        if (Anim.closeReadyScreen == this) {
            reanimated$closeNow(null);
            ci.cancel();
            return;
        }
        Anim.frameGraphics = graphics;
        Anim.beginFrame();
        // Экран показали снова — он появляется заново, как только что открытый.
        if (Anim.restartScreen == this) {
            Anim.restartScreen = null;
            reanimated$openTime = 0L;
            reanimated$lastW = -1;
        }
        if (reanimated$openTime == 0L) {
            reanimated$openTime = System.currentTimeMillis();
            // Направление перехода выставил уходивший экран — забираем его один раз,
            // чтобы следующий экран его уже не подхватил.
            reanimated$enterDir = reanimated$isContainer() ? 0 : Anim.takeEnterDir();
            // Экран попадает в список редактора настроек только после того, как игрок
            // его открыл: перечислить все экраны сборки заранее неоткуда.
            ScreenRules.remember(this, reanimated$title());
            Anim.onScreenOpened(this);
        }
        // Первый кадр или ресайз — виджеты пересозданы, раздаём ранги заново.
        if (this.width != reanimated$lastW || this.height != reanimated$lastH) {
            reanimated$lastW = this.width;
            reanimated$lastH = this.height;
            reanimated$assignCascade();
        }
        Anim.currentOpenTime = reanimated$openTime;
        Anim.currentIsPause = reanimated$isPause();
        Anim.currentOverride = ScreenRules.get(this);
        Anim.currentEnterDir = reanimated$enterDir;
        Anim.currentScreen = this;
        Anim.currentIsContainer = reanimated$isContainer();
        boolean container = reanimated$isContainer();
        // Матрица кадра до нашей трансформации: по ней фон и блюр вернутся
        // на место, ничего не обращая (см. UiTransform.pushBase).
        UiTransform.saveBase(graphics.pose());
        reanimated$fwdPushed = Anim.transformActive(container) && Anim.shouldAnimate(this);
        if (reanimated$fwdPushed) {
            Matrix3x2fStack m = graphics.pose();
            m.pushMatrix();
            UiTransform.forward(m, this.width, this.height, container);
        }
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void reanimated$wrapTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$fwdPushed) {
            graphics.pose().popMatrix();
        }
        Anim.endFrame();
        reanimated$maybeFinishClose();
    }

    /**
     * По завершении обратной анимации выполняет отложенную смену экрана.
     *
     * Переход на другой экран меняет его сразу, на выходе из отрисовки: следующий экран
     * должен появиться уже в следующем кадре, иначе между ними был бы кадр без экрана.
     * А закрытие в мир откладывается до начала следующего кадра (Anim.closeReadyScreen):
     * посреди кадра нельзя, иначе всё, что рисуется после экрана, видит «экрана нет».
     */
    @Unique
    private void reanimated$maybeFinishClose() {
        if (!Anim.isClosing()) return;
        if (!Anim.closeFinished(reanimated$isContainer())) return;
        if (Anim.pendingScreen == null) {
            Anim.closeReadyScreen = this;
            return;
        }
        reanimated$closeNow(Anim.pendingScreen);
    }

    /** Настоящая смена экрана: на {@code next} или, если он null, — в мир. */
    @Unique
    private void reanimated$closeNow(Object next) {
        Anim.finishClose();
        Anim.bypassClose = true;
        Minecraft.getInstance().setScreen(next instanceof Screen s ? s : null);
        Anim.bypassClose = false;
        if (next == null) {
            ScreenNav.clear();      // вернулись в игру — история переходов не нужна
            Anim.clearEnterDir();   // и направление входа тоже: перехода не было
        }
    }

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void reanimated$bgHead(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Контейнеры сами возвращают фон на место (HandledScreenMixin). Если тронуть его
        // ещё и здесь (в 1.21.5+ AbstractContainerScreen.renderBackground зовёт super),
        // получится двойная обратная трансформация — блюр «уезжает» вместе с панелью.
        reanimated$bgPushed = !reanimated$isContainer() && Anim.transformActive(false) && Anim.shouldAnimate(this);
        if (reanimated$bgPushed) {
            reanimated$bgPushed = UiTransform.pushBase(graphics.pose());
        }
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void reanimated$bgTail(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (reanimated$bgPushed) {
            UiTransform.popBase(graphics.pose());
        }
    }

    /** Цвета ванильного затемнения: градиент сверху вниз, оба — чёрный с разной прозрачностью. */
    @Unique private static final int REANIMATED$DIM_TOP = 0xC0101010;
    @Unique private static final int REANIMATED$DIM_BOTTOM = 0xD0101010;

    /**
     * Плавное появление затемнения за экраном. Ваниль включает его мгновенно — панель
     * красиво выезжает на фоне, который «щёлкнул». Здесь тот же градиент рисуется с
     * прозрачностью по кривой открытия, а на закрытии гаснет обратно.
     *
     * Пока анимация не идёт, ванильный метод отрабатывает как обычно — мод не трогает
     * ни один кадр статичного экрана и не мешает модам, меняющим затемнение.
     */
    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void reanimated$dimFade(GuiGraphics graphics, CallbackInfo ci) {
        // С Blur+ затемнением за экраном распоряжается он: у него своё появление и своё
        // угасание и свой градиент, а наша отмена этого метода подменила бы его градиент
        // ванильным на всё время анимации. На закрытии фон ему отдаёт ScreenBackgroundMixin.
        if (BlurPlusCompat.present()) return;
        if (!Anim.shouldAnimate(this)) return;
        float k = Anim.backgroundFade(reanimated$isContainer());
        if (k >= 1f) return;

        ci.cancel();
        if (k <= 0.004f) return; // ещё неотличимо от прозрачного — не рисуем вовсе
        graphics.fillGradient(0, 0, this.width, this.height,
            reanimated$dim(REANIMATED$DIM_TOP, k), reanimated$dim(REANIMATED$DIM_BOTTOM, k));
    }

    @Unique
    private static int reanimated$dim(int argb, float k) {
        int a = Math.round(((argb >>> 24) & 0xFF) * k);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
