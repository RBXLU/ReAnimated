package com.pycodder.reanimated.config;

import org.joml.Matrix3x2fStack;
import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.PivotPoint;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Animation Studio: a separate screen for building your own open/close animation from scratch. */
public class AnimationStudioScreen extends Screen {
    private static final int ROW_H = 23;
    private static final int HEADER_H = 15;
    private static final int PREVIEW_ELEMENTS = 3;
    private static final int PREVIEW_PAUSE = 700;

    private final Screen parent;
    private final AnimProfile working;

    /** Tools list entry: either a heading label or a full-width widget. */
    private final class Item {
        private final Component header;
        private final AbstractWidget widget;
        private int y;

        private Item(Component header, AbstractWidget widget) {
            this.header = header;
            this.widget = widget;
        }

        private int height() {
            return header != null ? HEADER_H : ROW_H;
        }
    }

    private final List<Item> items = new ArrayList<>();

    private int listX;
    private int listW;
    private int listTop;
    private int listBottom;

    private int rightX;
    private int rightW;
    private int graphTop;
    private int graphBottom;
    private int openTop;
    private int openBottom;
    private int closeTop;
    private int closeBottom;

    private double scroll = 0;
    private long previewStart = System.currentTimeMillis();

    public AnimationStudioScreen(Screen parent) {
        super(Component.translatable("reanimated.studio.title"));
        this.parent = parent;
        this.working = ReAnimatedConfig.get().profile.copy();
    }

    @Override
    protected void init() {
        items.clear();

        int margin = 12;
        listX = margin;
        listW = 176;
        listTop = 40;
        listBottom = this.height - 40;

        rightX = listX + listW + 22;
        rightW = Math.max(120, this.width - rightX - margin);

        graphTop = listTop;
        int visualH = listBottom - listTop;
        graphBottom = graphTop + Math.max(48, (int) (visualH * 0.28));
        int previewArea = listBottom - (graphBottom + 12);
        openTop = graphBottom + 12;
        openBottom = openTop + previewArea / 2 - 6;
        closeTop = openBottom + 12;
        closeBottom = listBottom;

        buildItems();
        layout();

        int by = this.height - 28;
        addRenderableWidget(Button.builder(Component.translatable("reanimated.studio.save"), b -> save())
            .bounds(listX, by, listW / 2 - 3, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.studio.cancel"), b -> onClose())
            .bounds(listX + listW / 2 + 3, by, listW / 2 - 3, 20).build());

        restartPreview();
    }

    private void buildItems() {
        header("reanimated.studio.section_params");

        cycle(() -> Component.translatable("reanimated.studio.enabled")
                .append(": ").append(Component.translatable(working.enabled
                    ? "reanimated.studio.on" : "reanimated.studio.off")),
            () -> working.enabled = !working.enabled);

        slider("reanimated.studio.duration", " ms", 50, 1500, true, working.durationMs,
            v -> working.durationMs = Math.max(1, Math.round(v)));
        slider("reanimated.studio.offset_x", " px", -80, 80, true, working.offsetX,
            v -> working.offsetX = v);
        slider("reanimated.studio.offset_y", " px", -80, 80, true, working.offsetY,
            v -> working.offsetY = v);
        slider("reanimated.studio.scale_x", "", 0.2f, 2f, false, working.scaleX,
            v -> working.scaleX = v);
        slider("reanimated.studio.scale_y", "", 0.2f, 2f, false, working.scaleY,
            v -> working.scaleY = v);
        slider("reanimated.studio.alpha", "", 0f, 1f, false, working.initialAlpha,
            v -> working.initialAlpha = Mth.clamp(v, 0f, 1f));
        slider("reanimated.studio.cascade_delay", " ms", 0, 200, true, working.cascadeDelayMs,
            v -> working.cascadeDelayMs = Math.max(0, Math.round(v)));

        cycle(() -> Component.translatable("reanimated.studio.cascade_order")
                .append(": " + working.cascadeOrder.display),
            () -> working.cascadeOrder = next(working.cascadeOrder, CascadeOrder.values()));
        cycle(() -> Component.translatable("reanimated.studio.pivot")
                .append(": " + working.pivot.display),
            () -> working.pivot = next(working.pivot, PivotPoint.values()));
        cycle(() -> Component.translatable("reanimated.studio.easing")
                .append(": " + working.easing.display),
            () -> working.easing = next(working.easing, EasingType.values()));

        header("reanimated.studio.section_presets");
        preset("reanimated.studio.preset.slide_up",
            () -> set(0, 22, 1, 1, 0f, EasingType.OUT_CUBIC, 380, 45, CascadeOrder.BOTTOM_TO_TOP, PivotPoint.CENTER));
        preset("reanimated.studio.preset.slide_down",
            () -> set(0, -22, 1, 1, 0f, EasingType.OUT_CUBIC, 380, 45, CascadeOrder.TOP_TO_BOTTOM, PivotPoint.CENTER));
        preset("reanimated.studio.preset.fly_left",
            () -> set(48, 0, 1, 1, 0f, EasingType.OUT_EXPO, 420, 40, CascadeOrder.BOTTOM_TO_TOP, PivotPoint.LEFT));
        preset("reanimated.studio.preset.fly_right",
            () -> set(-48, 0, 1, 1, 0f, EasingType.OUT_EXPO, 420, 40, CascadeOrder.BOTTOM_TO_TOP, PivotPoint.RIGHT));
        preset("reanimated.studio.preset.zoom_in",
            () -> set(0, 0, 0.6f, 0.6f, 0f, EasingType.OUT_BACK, 420, 35, CascadeOrder.SIMULTANEOUS, PivotPoint.CENTER));
        preset("reanimated.studio.preset.zoom_out",
            () -> set(0, 0, 1.35f, 1.35f, 0f, EasingType.OUT_CUBIC, 360, 30, CascadeOrder.SIMULTANEOUS, PivotPoint.CENTER));
        preset("reanimated.studio.preset.fade",
            () -> set(0, 0, 1, 1, 0f, EasingType.LINEAR, 300, 30, CascadeOrder.BOTTOM_TO_TOP, PivotPoint.CENTER));
        preset("reanimated.studio.preset.pop",
            () -> set(0, 6, 0.7f, 0.7f, 0f, EasingType.OUT_BACK, 300, 25, CascadeOrder.BOTTOM_TO_TOP, PivotPoint.BOTTOM));
        preset("reanimated.studio.preset.reset",
            () -> working.copyFrom(new AnimProfile()));
    }

    private void set(float offX, float offY, float scX, float scY, float alpha,
                     EasingType easing, int duration, int delay, CascadeOrder order, PivotPoint pivot) {
        working.enabled = true;
        working.offsetX = offX;
        working.offsetY = offY;
        working.scaleX = scX;
        working.scaleY = scY;
        working.initialAlpha = alpha;
        working.easing = easing;
        working.durationMs = duration;
        working.cascadeDelayMs = delay;
        working.cascadeOrder = order;
        working.pivot = pivot;
    }

    private void header(String key) {
        items.add(new Item(Component.translatable(key), null));
    }

    private void cycle(Supplier<Component> message, Runnable onClick) {
        Button button = Button.builder(message.get(), b -> {
            onClick.run();
            b.setMessage(message.get());
            restartPreview();
        }).bounds(listX, 0, listW, 20).build();
        items.add(new Item(null, addWidget(button)));
    }

    private void preset(String key, Runnable apply) {
        Button button = Button.builder(Component.translatable(key), b -> {
            apply.run();
            restartPreview();
            this.minecraft.execute(this::rebuildWidgets);
        }).bounds(listX, 0, listW, 20).build();
        items.add(new Item(null, addWidget(button)));
    }

    private void slider(String key, String unit, float min, float max, boolean integer,
                        float current, Consumer<Float> setter) {
        FloatSlider s = new FloatSlider(Component.translatable(key), unit, min, max, integer, current, setter);
        items.add(new Item(null, addWidget(s)));
    }

    private static <T extends Enum<T>> T next(T value, T[] values) {
        return values[(value.ordinal() + 1) % values.length];
    }

    /** Floating-point slider that updates the preview live. */
    private final class FloatSlider extends AbstractSliderButton {
        private final Component label;
        private final String unit;
        private final float min;
        private final float max;
        private final boolean integer;
        private final Consumer<Float> setter;

        private FloatSlider(Component label, String unit, float min, float max, boolean integer,
                            float current, Consumer<Float> setter) {
            super(listX, 0, listW, 20, Component.empty(),
                Mth.clamp((current - min) / (max - min), 0f, 1f));
            this.label = label;
            this.unit = unit;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.setter = setter;
            updateMessage();
        }

        private float real() {
            float v = (float) (min + this.value * (max - min));
            return integer ? Math.round(v) : v;
        }

        @Override
        protected void updateMessage() {
            float v = real();
            String shown = integer ? String.valueOf((int) v) : fmt(v);
            setMessage(label.copy().append(": " + shown + unit));
        }

        @Override
        protected void applyValue() {
            setter.accept(real());
            restartPreview();
        }
    }

    private static String fmt(float v) {
        String s = String.format(Locale.ROOT, "%.2f", v);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s + "0" : s;
    }

    private void layout() {
        int offset = (int) Math.round(scroll);
        int y = listTop - offset;
        for (Item item : items) {
            item.y = y;
            if (item.widget != null) {
                item.widget.setY(y);
                item.widget.visible = y + 20 > listTop && y < listBottom;
            }
            y += item.height();
        }
    }

    private int contentHeight() {
        int h = 0;
        for (Item item : items) {
            h += item.height();
        }
        return h;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - (listBottom - listTop));
    }

    private void restartPreview() {
        previewStart = System.currentTimeMillis();
    }

    private void save() {
        ReAnimatedConfig config = ReAnimatedConfig.get();
        config.profile.copyFrom(working);
        config.save();
        onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        graphics.drawCenteredString(font,
            Component.translatable("reanimated.studio.tools"), listX + listW / 2, 28, 0xFFAAAAAA);

        graphics.fill(listX - 6, listTop - 4, listX + listW + 6, listBottom + 4, 0xB0000000);
        graphics.enableScissor(listX - 6, listTop, listX + listW + 6, listBottom);
        for (Item item : items) {
            if (item.header != null) {
                if (item.y + HEADER_H > listTop && item.y < listBottom) {
                    graphics.drawString(font, item.header, listX, item.y + 3, 0xFFFFCC66);
                }
            } else if (item.widget.visible) {
                item.widget.render(graphics, mouseX, mouseY, delta);
            }
        }
        graphics.disableScissor();
        renderScrollbar(graphics);

        renderGraph(graphics);
        renderPreviews(graphics, delta);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int max = maxScroll();
        if (max == 0) return;
        int viewH = listBottom - listTop;
        int x = listX + listW + 7;
        int barH = Math.max(20, viewH * viewH / contentHeight());
        int barY = listTop + (int) ((viewH - barH) * (scroll / max));
        graphics.fill(x, listTop, x + 4, listBottom, 0x55000000);
        graphics.fill(x, barY, x + 4, barY + barH, 0xFFAAAAAA);
    }

    private void renderGraph(GuiGraphics graphics) {
        int pad = 6;
        int x0 = rightX + pad;
        int y0 = graphTop + pad + 8;
        int x1 = rightX + rightW - pad;
        int y1 = graphBottom - pad;
        graphics.fill(rightX, graphTop, rightX + rightW, graphBottom, 0xB0000000);
        graphics.drawString(font,
            Component.translatable("reanimated.studio.curve").append(": " + working.easing.display),
            rightX + pad, graphTop + 3, 0xFFDDDDDD);

        int gw = x1 - x0;
        int gh = y1 - y0;
        if (gw <= 2 || gh <= 2) return;

        final float vMin = -0.3f;
        final float vMax = 1.3f;

        int zeroY = valueToY(0f, y0, gh, vMin, vMax);
        int oneY = valueToY(1f, y0, gh, vMin, vMax);
        graphics.fill(x0, zeroY, x1, zeroY + 1, 0x40FFFFFF);
        graphics.fill(x0, oneY, x1, oneY + 1, 0x40FFFFFF);

        int prevY = -1;
        for (int px = 0; px <= gw; px++) {
            float t = px / (float) gw;
            float val = working.easing.apply(t);
            int py = valueToY(val, y0, gh, vMin, vMax);
            if (prevY >= 0) {
                int a = Math.min(prevY, py);
                int b = Math.max(prevY, py);
                graphics.fill(x0 + px, a, x0 + px + 1, b + 1, 0xFF66CCFF);
            } else {
                graphics.fill(x0 + px, py, x0 + px + 1, py + 1, 0xFF66CCFF);
            }
            prevY = py;
        }

        int total = working.totalMs(PREVIEW_ELEMENTS);
        float loop = loopElapsed(total);
        float openMs = Math.min(loop, working.durationMs);
        float tNow = Mth.clamp(openMs / Math.max(1, working.durationMs), 0f, 1f);
        int mx = x0 + Math.round(tNow * gw);
        graphics.fill(mx, y0, mx + 1, y1, 0x66FFFFFF);
        int my = valueToY(working.easing.apply(tNow), y0, gh, vMin, vMax);
        graphics.fill(mx - 1, my - 1, mx + 2, my + 2, 0xFFFFDD33);
    }

    private static int valueToY(float val, int y0, int gh, float vMin, float vMax) {
        float f = (val - vMin) / (vMax - vMin);
        return y0 + Math.round((1f - f) * gh);
    }

    private void renderPreviews(GuiGraphics graphics, float delta) {
        int total = working.totalMs(PREVIEW_ELEMENTS);
        float loop = loopElapsed(total);
        float openMs = working.enabled ? loop : Float.MAX_VALUE;
        float closeMs = working.enabled ? (total - Math.min(loop, total)) : Float.MAX_VALUE;

        renderPreviewPanel(graphics, delta, "reanimated.studio.open", openTop, openBottom, openMs);
        renderPreviewPanel(graphics, delta, "reanimated.studio.close", closeTop, closeBottom, closeMs);
    }

    private float loopElapsed(int total) {
        if (!working.enabled) return 0f;
        long period = (long) total + PREVIEW_PAUSE;
        return (System.currentTimeMillis() - previewStart) % period;
    }

    private void renderPreviewPanel(GuiGraphics graphics, float delta, String labelKey,
                                    int top, int bottom, float elapsedMs) {
        graphics.fill(rightX, top, rightX + rightW, bottom, 0xB0000000);
        graphics.drawString(font, Component.translatable(labelKey), rightX + 6, top + 3, 0xFFDDDDDD);

        int innerTop = top + 16;
        int innerBottom = bottom - 6;
        int band = Math.max(1, innerBottom - innerTop);
        int step = band / PREVIEW_ELEMENTS;
        int panelW = Math.min(rightW - 16, 108);
        int panelH = Math.max(10, Math.min(step - 4, 24));
        int panelX = rightX + (rightW - panelW) / 2;

        graphics.enableScissor(rightX, innerTop, rightX + rightW, innerBottom);
        for (int i = 0; i < PREVIEW_ELEMENTS; i++) {
            int py = innerTop + i * step + Math.max(0, (step - panelH) / 2);
            float e = working.progress(elapsedMs, working.slotFor(i, PREVIEW_ELEMENTS));

            float pivotX = panelX + panelW * working.pivot.fx;
            float pivotY = py + panelH * working.pivot.fy;

            Matrix3x2fStack m = graphics.pose();
            m.pushMatrix();
            m.translate(working.offsetXAt(e), working.offsetYAt(e));
            UiTransform.pivotScale(m, pivotX, pivotY, working.scaleXAt(e), working.scaleYAt(e));
            drawUiElement(graphics, panelX, py, panelW, panelH, working.alphaAt(e));
            m.popMatrix();
        }
        graphics.disableScissor();
    }

    private void drawUiElement(GuiGraphics graphics, int x, int y, int w, int h, float alpha) {
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, argb(0x2B2B2B, alpha));
        graphics.fill(x, y, x + w, y + h, argb(0xC6C6C6, alpha));
        graphics.fill(x, y, x + w, y + 6, argb(0x8B8B8B, alpha));
        int sy = y + 9;
        for (int s = 0; s < 4; s++) {
            int sx = x + 4 + s * 11;
            if (sx + 8 > x + w || sy + 8 > y + h) break;
            graphics.fill(sx, sy, sx + 8, sy + 8, argb(0x373737, alpha));
        }
    }

    private static int argb(int rgb, float alpha) {
        int a = Math.round(Mth.clamp(alpha, 0f, 1f) * 255f);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= listX - 6 && mouseX <= listX + listW + 6 && mouseY >= listTop && mouseY <= listBottom) {
            scroll = Mth.clamp(scroll - verticalAmount * (ROW_H / 2.0), 0, maxScroll());
            layout();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= rightX && mouseX <= rightX + rightW && mouseY >= openTop && mouseY <= closeBottom) {
            restartPreview();
            return true;
        }
        // Rows clipped by the scissor stay visible, but must not react to clicks:
        // the click would land in the row's hidden half, over the buttons below the list.
        List<AbstractWidget> hidden = new ArrayList<>();
        for (Item item : items) {
            if (item.widget != null && item.widget.visible
                    && !(item.y >= listTop && item.y + 20 <= listBottom)) {
                item.widget.visible = false;
                hidden.add(item.widget);
            }
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        for (AbstractWidget w : hidden) {
            w.visible = true;
        }
        return handled;
    }
}
