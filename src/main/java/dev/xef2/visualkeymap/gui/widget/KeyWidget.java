package dev.xef2.visualkeymap.gui.widget;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class KeyWidget extends PressableWidget {

    private static final int KEY_BACKGROUND_COLOR = 0xFF444444;
    private static final int KEY_SINGLE_BOUNDED_COLOR = 0xFF006400;
    private static final int KEY_MULTI_UNIQUE_COLOR = 0xFFB8860B;
    private static final int KEY_MULTI_CONFLICT_COLOR = 0xFF8B0000;
    private static final int BORDER_THICKNESS = 1;
    private static final int MAX_DISPLAYED_BINDINGS = 5;

    private final InputUtil.Key key;
    private final VisualKeymapScreen.SharedData sharedData;
    private final Supplier<List<? extends KeyBinding>> bindingSupplier;
    private final Runnable keySelector;

    public KeyWidget(InputUtil.Key key, Text text, VisualKeymapScreen.SharedData sharedData,
                     Supplier<List<? extends KeyBinding>> bindingSupplier,
                     Runnable keySelector) {
        super(0, 0, 0, 0, text);
        this.key = key;
        this.sharedData = sharedData;
        this.bindingSupplier = bindingSupplier;
        this.keySelector = keySelector;

        setupTooltip();
    }

    public void setupTooltip() {
        List<? extends KeyBinding> bindings = this.bindingSupplier.get();
        if (!bindings.isEmpty()) {
            MutableText tooltipText = Text.empty();
            tooltipText.append(Text.translatable(VisualKeymap.getTranslationKey("gui.bindings_title")).formatted(Formatting.BOLD, Formatting.GOLD));
            for (int i = 0; i < bindings.size(); i++) {
                tooltipText.append(Text.literal("\n"));
                if (i < MAX_DISPLAYED_BINDINGS) {
                    tooltipText.append(bindings.get(i).getDisplayName());
                } else {
                    tooltipText.append(Text.translatable(
                            VisualKeymap.getTranslationKey("gui.bindings_more"),
                            bindings.size() - MAX_DISPLAYED_BINDINGS
                    ).formatted(Formatting.ITALIC, Formatting.GRAY));
                    break;
                }
            }
            this.setTooltip(Tooltip.of(tooltipText));
        }
    }

    @Override
    public void onPress(AbstractInput input) {
        this.keySelector.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    private static boolean hasConflict(List<? extends KeyBinding> bindings) {
        Set<List<Integer>> uniqueKeyCodes = new HashSet<>();

        for (KeyBinding binding : bindings) {
            List<Integer> keyCodes = binding.getKeyCodes();

            if (!uniqueKeyCodes.add(keyCodes)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. 获取绑定并确定颜色
        List<? extends KeyBinding> bindings = this.bindingSupplier.get();
        int bindCount = bindings.size();

        int color;
        if (bindCount == 1) {
            color = KEY_SINGLE_BOUNDED_COLOR;
        } else if (bindCount >= 2) {
            if (hasConflict(bindings)) {
                color = KEY_MULTI_CONFLICT_COLOR;
            } else {
                color = KEY_MULTI_UNIQUE_COLOR;
            }
        } else {
            color = KEY_BACKGROUND_COLOR;
        }

        int borderColor = this.sharedData.selectedKeyCode != null && this.sharedData.selectedKeyCode == this.key.getCode()
                ? Colors.WHITE : Colors.BLACK;

        // 2. 绘制边框
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

        // 3. 绘制按键背景
        int innerX = getX() + BORDER_THICKNESS;
        int innerY = getY() + BORDER_THICKNESS;
        int innerWidth = getWidth() - BORDER_THICKNESS * 2;
        int innerHeight = getHeight() - BORDER_THICKNESS * 2;
        context.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, color);

        // 4. 绘制按键文本
        this.drawLabel(context.getHoverListener(this, DrawContext.HoverType.NONE));
    }
}
