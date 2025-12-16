package dev.xef2.visualkeymap.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class KeyboardWidget extends AbstractLayout {

    private static final int KEY_SPACING = 1;

    private int maxHeight;
    private final VisualKeymapScreen.SharedData sharedData;
    private final Function<InputConstants.Key, List<? extends KeyBinding>> bindingGetter;
    private final Consumer<InputConstants.Key> keySelector;

    private int keySize;
    private final KeyLayoutHelper.KeyboardLayout keyboardLayout;
    private final Map<KeyLayoutHelper.KeyLayout, KeyWidget> keyWidgetMap = new HashMap<>();

    public KeyboardWidget(
            int x, int y, int width, int maxHeight,
            boolean isFull, VisualKeymapScreen.SharedData sharedData,
            Function<InputConstants.Key, List<? extends KeyBinding>> bindingGetter,
            Consumer<InputConstants.Key> keySelector
    ) {
        super(x, y, width, 0);
        this.maxHeight = maxHeight;
        this.sharedData = sharedData;
        this.bindingGetter = bindingGetter;
        this.keySelector = keySelector;

        this.keyboardLayout = KeyLayoutHelper.getLayout(isFull);

        for (KeyLayoutHelper.KeyLayout keyLayout : this.keyboardLayout.keys()) {
            String translationKey = VisualKeymap.getTranslationKey("key." + keyLayout.translationKey());
            InputConstants.Key key = keyLayout.getKey();
            Component text = I18n.exists(translationKey) ?
                    Component.translatable(translationKey) :
                    key.getDisplayName();
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
    public void visitChildren(@NotNull Consumer<LayoutElement> consumer) {
        this.keyWidgetMap.values().forEach(consumer);
    }

    private int getSizeWithSpacing(double mult) {
        return (int) (mult * this.keySize + (mult - 1) * KEY_SPACING);
    }

    private void updateSizeAndHeight() {
        double totalCols = this.keyboardLayout.columns();
        double totalRows = this.keyboardLayout.rows();

        this.keySize = Mth.floor(Math.min(
                (this.width - (totalCols - 1) * KEY_SPACING) / totalCols,
                (this.maxHeight - (totalRows - 1) * KEY_SPACING) / totalRows
        ));
        this.height = getSizeWithSpacing(totalRows);
    }

    @Override
    public void arrangeElements() {
        int totalWidth = getSizeWithSpacing(this.keyboardLayout.columns());

        int startX = this.getX() + (this.width - totalWidth) / 2;
        int startY = this.getY();

        for (KeyLayoutHelper.KeyLayout keyLayout : this.keyboardLayout.keys()) {
            int width = getSizeWithSpacing(keyLayout.widthMult());
            int height = getSizeWithSpacing(keyLayout.heightMult());

            int x = startX + (int) (keyLayout.col() * (this.keySize + KEY_SPACING));
            int y = startY + (int) (keyLayout.row() * (this.keySize + KEY_SPACING));

            this.keyWidgetMap.get(keyLayout).setRectangle(width, height, x, y);
        }
    }

    @Environment(EnvType.CLIENT)
    private class KeyWidget extends AbstractButton {

        private static final int KEY_SINGLE_BOUNDED_COLOR = 0xFF006400;
        private static final int KEY_MULTI_UNIQUE_COLOR = 0xFFB8860B;
        private static final int KEY_MULTI_CONFLICT_COLOR = 0xFF8B0000;
        private static final int BORDER_THICKNESS = 1;
        private static final int MAX_DISPLAYED_BINDINGS = 5;

        private final InputConstants.Key key;
        private List<? extends KeyBinding> bindings;
        private boolean conflict;

        public KeyWidget(InputConstants.Key key, Component text) {
            super(0, 0, 0, 0, text);
            this.key = key;

            updateBindings();
        }

        public void updateBindings() {
            this.bindings = bindingGetter.apply(this.key);
            List<? extends KeyBinding> conflictBindings = KeyBinding.getConflictBindings(this.bindings).stream()
                    .flatMap(Collection::stream).toList();
            List<? extends KeyBinding> uniqueBindings = this.bindings.stream()
                    .filter(binding -> !conflictBindings.contains(binding))
                    .toList();
            this.conflict = !conflictBindings.isEmpty();

            MutableComponent tooltipText = Component.empty();
            tooltipText.append(this.key.getDisplayName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));

            for (KeyBinding conflictBinding : conflictBindings) {
                tooltipText.append("\n").append(conflictBinding.getDisplayName().withStyle(ChatFormatting.RED));
            }
            for (int i = 0; i < uniqueBindings.size(); i++) {
                tooltipText.append("\n");
                if (i < MAX_DISPLAYED_BINDINGS - conflictBindings.size()) {
                    tooltipText.append(uniqueBindings.get(i).getDisplayName());
                } else {
                    tooltipText.append(VisualKeymap.getTranslatedComponent(
                            "gui.tooltip.bindings_more",
                            uniqueBindings.size() - i
                    ).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                    break;
                }
            }

            this.setTooltip(Tooltip.create(tooltipText));
        }

        @Override
        public void onPress(@NotNull InputWithModifiers inputWithModifiers) {
            keySelector.accept(this.key);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        protected void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
            int borderColor;
            if (sharedData.selectedKeyCode != null && sharedData.selectedKeyCode == this.key.getValue()) {
                borderColor = CommonColors.WHITE;
            } else if (this.bindings.stream().anyMatch(
                    binding -> binding.containsSearchText(sharedData.searchText))) {
                borderColor = CommonColors.YELLOW;
            } else {
                borderColor = CommonColors.BLACK;
            }

            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

            int color;
            int bindCount = this.bindings.size();
            if (this.conflict) {
                color = KEY_MULTI_CONFLICT_COLOR;
            } else if (bindCount == 1) {
                color = KEY_SINGLE_BOUNDED_COLOR;
            } else if (bindCount >= 2) {
                color = KEY_MULTI_UNIQUE_COLOR;
            } else {
                color = CommonColors.DARK_GRAY;
            }

            int innerX = getX() + BORDER_THICKNESS;
            int innerY = getY() + BORDER_THICKNESS;
            int innerWidth = getWidth() - BORDER_THICKNESS * 2;
            int innerHeight = getHeight() - BORDER_THICKNESS * 2;
            guiGraphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, color);

            this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
        }
    }
}
