package dev.xef2.visualkeymap.gui.widget;

import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class KeybindsListWidget extends ElementListWidget<KeybindsListWidget.Entry> {
    private static final int ROW_HEIGHT = 20;

    private final VisualKeymapScreen.SharedData sharedData;
    private final Consumer<KeyBinding> resetCallback;

    private List<? extends KeyBinding> keyBindings;

    public KeybindsListWidget(
            MinecraftClient client,
            int width,
            int height,
            int y,
            VisualKeymapScreen.SharedData sharedData,
            Consumer<KeyBinding> resetCallback
    ) {
        super(client, width, height, y, ROW_HEIGHT);
        this.sharedData = sharedData;
        this.resetCallback = resetCallback;
    }

    public void setKeyBindings(List<? extends KeyBinding> keyBindings) {
        this.keyBindings = keyBindings;

        this.clearEntries();
        for (KeyBinding keyBinding : keyBindings) {
            this.addEntry(new Entry(keyBinding));
        }
        this.setScrollY(0.0);

        this.updateAllEntries();
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    public void updateAllEntries() {
        List<List<KeyBinding>> conflictKeyBindings = KeyBinding.getConflictBindings(this.keyBindings);
        this.children().forEach(entry -> entry.update(conflictKeyBindings));
    }

    @Environment(EnvType.CLIENT)
    public class Entry extends ElementListWidget.Entry<Entry> {
        private static final Text RESET_TEXT = Text.translatable("controls.reset");
        private final KeyBinding binding;

        private final TextWidget nameWidget;
        private final ButtonWidget editButton;
        private final ButtonWidget resetButton;

        public Entry(final KeyBinding binding) {
            this.binding = binding;
            TextRenderer textRenderer = KeybindsListWidget.this.client.textRenderer;

            this.nameWidget = new TextWidget(
                    0,
                    ROW_HEIGHT,
                    binding.getDisplayName(),
                    textRenderer
            );
            this.nameWidget.setTooltip(Tooltip.of(binding.getDisplayName()));
            this.editButton = ButtonWidget.builder(binding.getBoundKeysLocalizedText(), (button) -> {
                        sharedData.selectedKeyBinding = binding;
                        updateAllEntries();
                    })
                    .dimensions(0, 0, 100, ROW_HEIGHT)
                    .build();
            this.resetButton = ButtonWidget.builder(RESET_TEXT, (button) -> {
                        resetCallback.accept(binding);
                        updateAllEntries();
                    })
                    .dimensions(0, 0, 50, ROW_HEIGHT)
                    .build();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int y = this.getContentY() - 2;

            int resetX = KeybindsListWidget.this.getScrollbarX() - this.resetButton.getWidth() - 10;
            this.resetButton.setPosition(resetX, y);
            this.resetButton.render(context, mouseX, mouseY, deltaTicks);

            int editX = resetX - 5 - this.editButton.getWidth();
            this.editButton.setPosition(editX, y);
            this.editButton.render(context, mouseX, mouseY, deltaTicks);

            int textX = this.getContentX();
            this.nameWidget.setPosition(textX, y);
            this.nameWidget.setMaxWidth(editX - textX - 5);
            this.nameWidget.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.nameWidget, this.editButton, this.resetButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(this.editButton, this.resetButton);
        }

        protected void update(List<List<KeyBinding>> conflictKeyBindings) {
            List<KeyBinding> conflictedBindings = this.binding.getKeyCodes().isEmpty() ? null : conflictKeyBindings
                    .stream()
                    .filter(list -> list.contains(this.binding))
                    .findFirst()
                    .orElse(null);

            if (conflictedBindings != null) {
                MutableText tooltipText = Text.empty();
                tooltipText.append(Text.translatable("visualkeymap.gui.conflict_title")
                        .formatted(Formatting.BOLD, Formatting.GOLD));
                for (KeyBinding binding : conflictedBindings) {
                    if (binding != this.binding) {
                        tooltipText.append(Text.literal("\n"))
                                .append(binding.getDisplayName());
                    }
                }
                this.editButton.setTooltip(Tooltip.of(tooltipText));
            } else {
                this.editButton.setTooltip(null);
            }

            MutableText keyText = this.binding.getBoundKeysLocalizedText().copy();
            if (sharedData.selectedKeyBinding == this.binding) {
                this.editButton.setMessage(Text.literal("> ")
                        .append(keyText.formatted(Formatting.WHITE)).append(" <")
                        .formatted(Formatting.YELLOW));
            } else if (conflictedBindings != null) {
                this.editButton.setMessage(keyText.formatted(Formatting.GOLD));
            } else {
                this.editButton.setMessage(keyText);
            }

            this.resetButton.active = !this.binding.isDefault();
        }
    }
}
