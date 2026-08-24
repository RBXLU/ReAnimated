package com.pycodder.reanimated.config;

import com.pycodder.reanimated.anim.AnimProfile;
import com.pycodder.reanimated.anim.CascadeOrder;
import com.pycodder.reanimated.anim.EasingType;
import com.pycodder.reanimated.anim.PivotPoint;
import com.pycodder.reanimated.anim.UiTransform;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import org.joml.Matrix3x2fStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Animation profile editor: parameters on the left, a live three-element preview on the right playing exactly the animation being configured. */
public class AnimProfileEditorScreen extends Screen {
    private static final int ROW_H = 22;
    private static final int LIST_W = 200;
    private static final int LABEL_W = 100;
    private static final int GAP = 40;
    private static final int PREVIEW_W = 150;
    private static final int PREVIEW_ELEMENTS = 3;
    private static final int PREVIEW_MARGIN = 20;
    private static final int PREVIEW_PAUSE = 900;

    private static final Component PARAMS = Component.translatable("reanimated.editor.params");
    private static final Component PREVIEW = Component.translatable("reanimated.editor.preview");

    private final Screen parent;
    private final AnimProfile working;

    private final List<Row> rows = new ArrayList<>();
    private final List<Button> previewButtons = new ArrayList<>();

    private int listX;
    private int listTop;
    private int listBottom;
    private int previewX;
    private double scroll = 0;
    private long previewStart = System.currentTimeMillis();

    public AnimProfileEditorScreen(Screen parent) {
        super(Component.translatable("reanimated.editor.title"));
        this.parent = parent;
        this.working = ReAnimatedConfig.get().profile.copy();
    }

    /** List row: a label on the left with widgets on the right, or a full-width button. */
    private final class Row {
        private final Component label;
        private final List<AbstractWidget> widgets = new ArrayList<>();
        private int y;

        private Row(Component label) {
            this.label = label;
        }

        private void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            if (label != null) {
                context.fill(listX, y, listX + LABEL_W, y + 18, 0x33FFFFFF);
                context.centeredText(font, label, listX + LABEL_W / 2, y + 5, 0xFFDDDDDD);
            }
            for (AbstractWidget w : widgets) {
                if (w.visible) {
                    w.extractRenderState(context, mouseX, mouseY, delta);
                }
            }
        }

        private boolean fullyVisible() {
            return y >= listTop && y + 20 <= listBottom;
        }
    }

    @Override
    protected void init() {
        rows.clear();
        previewButtons.clear();

        int totalW = LIST_W + GAP + PREVIEW_W;
        listX = (this.width - totalW) / 2;
        previewX = listX + LIST_W + GAP;
        listTop = 46;
        listBottom = this.height - 40;

        buildRows();
        layout();

        int previewTop = listTop + 30;
        for (int i = 0; i < PREVIEW_ELEMENTS; i++) {
            previewButtons.add(Button.builder(
                    Component.translatable("reanimated.editor.element", i + 1), b -> {})
                .pos(previewX, previewTop + i * 26).size(PREVIEW_W, 20).build());
        }

        int y = this.height - 28;
        addRenderableWidget(Button.builder(Component.translatable("reanimated.editor.save"), b -> save())
            .pos(this.width / 2 - 154, y).size(150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reanimated.editor.cancel"), b -> onClose())
            .pos(this.width / 2 + 4, y).size(150, 20).build());

        restartPreview();
    }

    private void buildRows() {
        cycleRow(() -> Component.translatable("reanimated.editor.animation")
                .append(": ").append(Component.translatable(working.enabled
                    ? "reanimated.editor.on" : "reanimated.editor.off")),
            () -> working.enabled = !working.enabled);

        fieldRow(Component.translatable("reanimated.editor.duration"),
            intField(LIST_W - LABEL_W, working.durationMs, v -> working.durationMs = Math.max(1, v)));

        fieldRow(Component.translatable("reanimated.editor.offset"),
            floatField(48, working.offsetX, v -> working.offsetX = v),
            floatField(48, working.offsetY, v -> working.offsetY = v));

        fieldRow(Component.translatable("reanimated.editor.scale"),
            floatField(48, working.scaleX, v -> working.scaleX = v),
            floatField(48, working.scaleY, v -> working.scaleY = v));

        fieldRow(Component.translatable("reanimated.editor.alpha"),
            floatField(LIST_W - LABEL_W, working.initialAlpha,
                v -> working.initialAlpha = Mth.clamp(v, 0f, 1f)));

        fieldRow(Component.translatable("reanimated.editor.cascade_delay"),
            intField(LIST_W - LABEL_W, working.cascadeDelayMs, v -> working.cascadeDelayMs = Math.max(0, v)));

        cycleRow(() -> Component.translatable("reanimated.editor.cascade_order")
                .append(": " + working.cascadeOrder.display),
            () -> working.cascadeOrder = next(working.cascadeOrder, CascadeOrder.values()));

        cycleRow(() -> Component.translatable("reanimated.editor.pivot")
                .append(": " + working.pivot.display),
            () -> working.pivot = next(working.pivot, PivotPoint.values()));

        cycleRow(() -> Component.translatable("reanimated.editor.easing")
                .append(": " + working.easing.display),
            () -> working.easing = next(working.easing, EasingType.values()));

        cycleRow(() -> Component.translatable("reanimated.editor.reset"), () -> {
            working.copyFrom(new AnimProfile());
            this.minecraft.execute(this::rebuildWidgets);
        });
    }

    private static <T extends Enum<T>> T next(T value, T[] values) {
        return values[(value.ordinal() + 1) % values.length];
    }

    private void cycleRow(Supplier<Component> message, Runnable onClick) {
        Row row = new Row(null);
        Button button = Button.builder(message.get(), b -> {
            onClick.run();
            b.setMessage(message.get());
            restartPreview();
        }).pos(listX, 0).size(LIST_W, 20).build();
        row.widgets.add(addWidget(button));
        rows.add(row);
    }

    private void fieldRow(Component label, EditBox... fields) {
        Row row = new Row(label);
        int x = listX + LABEL_W;
        for (EditBox f : fields) {
            f.setX(x);
            x += f.getWidth() + 4;
            row.widgets.add(addWidget(f));
        }
        rows.add(row);
    }

    private EditBox intField(int width, int value, Consumer<Integer> apply) {
        return numberField(width, String.valueOf(value), true, v -> apply.accept(Math.round(v)));
    }

    private EditBox floatField(int width, float value, Consumer<Float> apply) {
        return numberField(width, fmt(value), false, apply);
    }

    private EditBox numberField(int width, String text, boolean integer, Consumer<Float> apply) {
        EditBox field = new EditBox(this.font, listX, 0, width, 18, Component.empty());
        field.setMaxLength(8);
        field.setValue(text);
        field.setResponder(s -> {
            try {
                apply.accept(Float.parseFloat(s));
                restartPreview();
            } catch (NumberFormatException ignored) {
            }
        });
        return field;
    }

    private static String fmt(float v) {
        String s = String.format(Locale.ROOT, "%.3f", v);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s + "0" : s;
    }

    private void layout() {
        int offset = (int) Math.round(scroll);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.y = listTop + i * ROW_H - offset;
            boolean visible = row.y + 20 > listTop && row.y < listBottom;
            for (AbstractWidget w : row.widgets) {
                w.setY(row.y);
                w.visible = visible;
            }
        }
    }

    private int maxScroll() {
        return Math.max(0, rows.size() * ROW_H - (listBottom - listTop));
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
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        context.centeredText(font, PARAMS, listX + LIST_W / 2, 30, 0xFFAAAAAA);
        context.centeredText(font, PREVIEW, previewX + PREVIEW_W / 2, 30, 0xFFAAAAAA);

        context.fill(listX - 6, listTop - 4, listX + LIST_W + 6, listBottom + 4, 0xB0000000);
        context.enableScissor(listX - 6, listTop, listX + LIST_W + 6, listBottom);
        for (Row row : rows) {
            row.extractRenderState(context, mouseX, mouseY, delta);
        }
        context.disableScissor();

        renderScrollbar(context);
        renderPreview(context, delta);
    }

    private void renderScrollbar(GuiGraphicsExtractor context) {
        int max = maxScroll();
        if (max == 0) return;
        int viewH = listBottom - listTop;
        int x = listX + LIST_W + 1;
        int barH = Math.max(20, viewH * viewH / (rows.size() * ROW_H));
        int barY = listTop + (int) ((viewH - barH) * (scroll / max));
        context.fill(x, listTop, x + 4, listBottom, 0x55000000);
        context.fill(x, barY, x + 4, barY + barH, 0xFFAAAAAA);
    }

    private void renderPreview(GuiGraphicsExtractor context, float delta) {
        int total = working.totalMs(PREVIEW_ELEMENTS);
        float elapsed = working.enabled
            ? (System.currentTimeMillis() - previewStart) % (total + PREVIEW_PAUSE)
            : Float.MAX_VALUE;

        float pivotX = previewX + PREVIEW_W * working.pivot.fx;

        context.enableScissor(previewX - PREVIEW_MARGIN, listTop, previewX + PREVIEW_W + PREVIEW_MARGIN, listBottom);
        for (int i = 0; i < previewButtons.size(); i++) {
            Button button = previewButtons.get(i);
            float e = working.progress(elapsed, working.slotFor(i, PREVIEW_ELEMENTS));

            float pivotY = button.getY() + button.getHeight() * working.pivot.fy;

            Matrix3x2fStack m = context.pose();
            m.pushMatrix();
            m.translate(working.offsetXAt(e), working.offsetYAt(e));
            UiTransform.pivotScale(m, pivotX, pivotY, working.scaleXAt(e), working.scaleYAt(e));
            button.setAlpha(working.alphaAt(e));
            button.extractRenderState(context, -1, -1, delta);
            button.setAlpha(1f);
            m.popMatrix();
        }
        context.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= listX - 6 && mouseX <= listX + LIST_W + 6 && mouseY >= listTop && mouseY <= listBottom) {
            scroll = Mth.clamp(scroll - verticalAmount * (ROW_H / 2.0), 0, maxScroll());
            layout();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        if (event.x() >= previewX - PREVIEW_MARGIN && event.x() <= previewX + PREVIEW_W + PREVIEW_MARGIN
                && event.y() >= listTop && event.y() <= listBottom) {
            restartPreview();
            return true;
        }
        // Rows clipped by the scissor stay visible, but must not react to clicks:
        // the click would land in the row's hidden half, over the buttons below the list.
        List<AbstractWidget> hidden = new ArrayList<>();
        for (Row row : rows) {
            if (!row.fullyVisible()) {
                for (AbstractWidget w : row.widgets) {
                    if (w.visible) {
                        w.visible = false;
                        hidden.add(w);
                    }
                }
            }
        }
        boolean handled = super.mouseClicked(event, doubled);
        for (AbstractWidget w : hidden) {
            w.visible = true;
        }
        return handled;
    }
}
