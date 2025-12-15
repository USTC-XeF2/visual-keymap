package dev.xef2.visualkeymap.gui.widget;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.WrapperWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class KeyboardWidget extends WrapperWidget {

    private static final int KEY_SPACING = 1;

    private int maxHeight;
    private final VisualKeymapScreen.SharedData sharedData;
    private final Function<InputUtil.Key, List<? extends KeyBinding>> bindingGetter;
    private final Consumer<InputUtil.Key> keySelector;

    private int keySize;
    private final KeyLayoutHelper.KeyboardLayout keyboardLayout;
    private final Map<KeyLayoutHelper.KeyLayout, KeyWidget> keyWidgetMap = new HashMap<>();

    public KeyboardWidget(
            int x, int y, int width, int maxHeight,
            boolean isFull, VisualKeymapScreen.SharedData sharedData,
            Function<InputUtil.Key, List<? extends KeyBinding>> bindingGetter,
            Consumer<InputUtil.Key> keySelector
    ) {
        super(x, y, width, 0);
        this.maxHeight = maxHeight;
        this.sharedData = sharedData;
        this.bindingGetter = bindingGetter;
        this.keySelector = keySelector;

        this.keyboardLayout = KeyLayoutHelper.getLayout(isFull);

        for (KeyLayoutHelper.KeyLayout keyLayout : this.keyboardLayout.keys()) {
            String translationKey = VisualKeymap.getTranslationKey("key." + keyLayout.translationKey());
            InputUtil.Key key = keyLayout.getKey();
            Text text = I18n.hasTranslation(translationKey) ?
                    Text.translatable(translationKey) :
                    key.getLocalizedText();
            this.keyWidgetMap.put(keyLayout, new KeyWidget(key, text));
        }

        this.updateSizeAndHeight();
    }

    public void updateKeyBindings() {
        this.keyWidgetMap.values().forEach(KeyWidget::updateBindings);
    }

    public void setSize(int width, int maxHeight) {
        this.width = width;
        this.maxHeight = maxHeight;
        this.updateSizeAndHeight();
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
        this.keyWidgetMap.values().forEach(consumer);
    }

    private int getSizeWithSpacing(double mult) {
        return (int) (mult * this.keySize + (mult - 1) * KEY_SPACING);
    }

    private void updateSizeAndHeight() {
        double totalCols = this.keyboardLayout.columns();
        double totalRows = this.keyboardLayout.rows();

        this.keySize = MathHelper.floor(Math.min(
                (this.width - (totalCols - 1) * KEY_SPACING) / totalCols,
                (this.maxHeight - (totalRows - 1) * KEY_SPACING) / totalRows
        ));
        this.height = getSizeWithSpacing(totalRows);
    }

    @Override
    public void refreshPositions() {
        int totalWidth = getSizeWithSpacing(this.keyboardLayout.columns());

        int startX = this.getX() + (this.width - totalWidth) / 2;
        int startY = this.getY();

        for (KeyLayoutHelper.KeyLayout keyLayout : this.keyboardLayout.keys()) {
            int width = getSizeWithSpacing(keyLayout.widthMult());
            int height = getSizeWithSpacing(keyLayout.heightMult());

            int x = startX + (int) (keyLayout.col() * (this.keySize + KEY_SPACING));
            int y = startY + (int) (keyLayout.row() * (this.keySize + KEY_SPACING));

            this.keyWidgetMap.get(keyLayout).setDimensionsAndPosition(width, height, x, y);
        }
    }

    @Environment(EnvType.CLIENT)
    private class KeyWidget extends PressableWidget {

        private static final int KEY_SINGLE_BOUNDED_COLOR = 0xFF006400;
        private static final int KEY_MULTI_UNIQUE_COLOR = 0xFFB8860B;
        private static final int KEY_MULTI_CONFLICT_COLOR = 0xFF8B0000;
        private static final int BORDER_THICKNESS = 1;
        private static final int MAX_DISPLAYED_BINDINGS = 5;

        private final InputUtil.Key key;
        private List<? extends KeyBinding> bindings;
        private boolean conflict;

        public KeyWidget(InputUtil.Key key, Text text) {
            super(0, 0, 0, 0, text);
            this.key = key;

            updateBindings();
        }

        public void updateBindings() {
            this.bindings = bindingGetter.apply(this.key);
            this.conflict = !KeyBinding.getConflictBindings(this.bindings).isEmpty();

            MutableText tooltipText = Text.empty();
            tooltipText.append(this.key.getLocalizedText().copy().formatted(Formatting.BOLD, Formatting.GOLD));

            for (int i = 0; i < this.bindings.size(); i++) {
                tooltipText.append(Text.literal("\n"));
                if (i < MAX_DISPLAYED_BINDINGS) {
                    tooltipText.append(this.bindings.get(i).getDisplayName());
                } else {
                    tooltipText.append(VisualKeymap.getTranslationText(
                            "gui.bindings_more",
                            this.bindings.size() - MAX_DISPLAYED_BINDINGS
                    ).formatted(Formatting.ITALIC, Formatting.GRAY));
                    break;
                }
            }

            this.setTooltip(Tooltip.of(tooltipText));
        }

        @Override
        public void onPress(AbstractInput input) {
            keySelector.accept(this.key);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int borderColor;
            if (sharedData.selectedKeyCode != null && sharedData.selectedKeyCode == this.key.getCode()) {
                borderColor = Colors.WHITE;
            } else if (this.bindings.stream().anyMatch(
                    binding -> binding.containsSearchText(sharedData.searchText))) {
                borderColor = Colors.YELLOW;
            } else {
                borderColor = Colors.BLACK;
            }

            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

            int color;
            int bindCount = this.bindings.size();
            if (this.conflict) {
                color = KEY_MULTI_CONFLICT_COLOR;
            } else if (bindCount == 1) {
                color = KEY_SINGLE_BOUNDED_COLOR;
            } else if (bindCount >= 2) {
                color = KEY_MULTI_UNIQUE_COLOR;
            } else {
                color = Colors.DARK_GRAY;
            }

            int innerX = getX() + BORDER_THICKNESS;
            int innerY = getY() + BORDER_THICKNESS;
            int innerWidth = getWidth() - BORDER_THICKNESS * 2;
            int innerHeight = getHeight() - BORDER_THICKNESS * 2;
            context.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, color);

            this.drawMessage(context, MinecraftClient.getInstance().textRenderer, Colors.WHITE);
        }
    }
}
