package dev.xef2.visualkeymap.gui.widget;

import dev.xef2.visualkeymap.ModConfig;
import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class KeybindsListWidget extends ContainerObjectSelectionList<KeybindsListWidget.@NotNull Entry> {
    private static final int ROW_HEIGHT = 20;

    private final VisualKeymapScreen.SharedData sharedData;
    private final Consumer<KeyBinding> resetCallback;

    private List<? extends KeyBinding> keyBindings;

    public KeybindsListWidget(
            Minecraft client,
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
        this.keyBindings = new ArrayList<>(keyBindings);
        this.createEntries();
    }

    public void createEntries() {
        List<List<KeyBinding>> conflictKeyBindings = KeyBinding.getConflictBindings(this.keyBindings);

        List<? extends KeyBinding> sortedKeyBindings = this.keyBindings.stream()
                .sorted(getComparator(conflictKeyBindings))
                .toList();

        this.clearEntries();
        for (KeyBinding keyBinding : sortedKeyBindings) {
            this.addEntry(new dev.xef2.visualkeymap.gui.widget.KeybindsListWidget.Entry(keyBinding));
        }
        this.setScrollAmount(0.0);

        this.updateAllEntries();
    }

    private Comparator<KeyBinding> getComparator(List<List<KeyBinding>> conflictKeyBindings) {
        ModConfig modConfig = ModConfig.getInstance();

        Comparator<KeyBinding> comparator = Comparator.comparingInt(
                binding -> binding.containsSearchText(this.sharedData.searchText) ? 0 : 1
        );

        if (modConfig.prioritizeConflictingKeybinds) {
            comparator = comparator.thenComparingInt(
                    binding -> conflictKeyBindings.stream().anyMatch(list -> list.contains(binding)) ? 0 : 1
            );
        }

        comparator = switch (modConfig.sortMode) {
            case BOUND_KEY -> comparator.thenComparing(KeybindsListWidget::compareBoundKeys);
            case DISPLAY_NAME ->
                    comparator.thenComparing(binding -> binding.getDisplayName().getString(), String.CASE_INSENSITIVE_ORDER);
            case DEFAULT -> comparator;
        };

        return comparator;
    }

    private static int compareBoundKeys(KeyBinding first, KeyBinding second) {
        int result = compareIntLists(first.getKeyCodes(), second.getKeyCodes());
        if (result != 0) {
            return result;
        }
        return compareIntLists(first.getModifierKeyCodes(), second.getModifierKeyCodes());
    }

    private static int compareIntLists(List<Integer> first, List<Integer> second) {
        int minSize = Math.min(first.size(), second.size());
        for (int i = 0; i < minSize; i++) {
            int result = Integer.compare(first.get(i), second.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(first.size(), second.size());
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
    public class Entry extends ContainerObjectSelectionList.Entry<KeybindsListWidget.@NotNull Entry> {
        private static final Component RESET_TEXT = Component.translatable("controls.reset");
        private final KeyBinding binding;

        private final StringWidget nameWidget;
        private final Button editButton;
        private final Button resetButton;

        public Entry(final KeyBinding binding) {
            this.binding = binding;
            Font font = KeybindsListWidget.this.minecraft.font;

            this.nameWidget = new StringWidget(
                    0,
                    ROW_HEIGHT,
                    binding.getDisplayName(),
                    font
            );
            this.nameWidget.setTooltip(Tooltip.create(binding.getTooltip()));
            this.editButton = Button.builder(binding.getBoundKeysLocalizedText(), (_) -> {
                        sharedData.selectedKeyBinding = binding;
                        updateAllEntries();
                    })
                    .bounds(0, 0, 100, ROW_HEIGHT)
                    .build();
            this.resetButton = Button.builder(RESET_TEXT, (_) -> {
                        resetCallback.accept(binding);
                        updateAllEntries();
                    })
                    .bounds(0, 0, 50, ROW_HEIGHT)
                    .build();
        }

        @Override
        public void extractContent(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float f) {
            int y = this.getContentY() - 2;

            int resetX = KeybindsListWidget.this.scrollBarX() - this.resetButton.getWidth() - 10;
            this.resetButton.setPosition(resetX, y);
            this.resetButton.extractRenderState(graphics, mouseX, mouseY, f);

            int editX = resetX - 5 - this.editButton.getWidth();
            this.editButton.setPosition(editX, y);
            this.editButton.extractRenderState(graphics, mouseX, mouseY, f);

            int textX = this.getContentX();
            this.nameWidget.setPosition(textX, y);
            this.nameWidget.setMaxWidth(editX - textX - 5);
            this.nameWidget.extractRenderState(graphics, mouseX, mouseY, f);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(this.nameWidget, this.editButton, this.resetButton);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(this.editButton, this.resetButton);
        }

        protected void update(List<List<KeyBinding>> conflictKeyBindings) {
            if (this.binding.containsSearchText(sharedData.searchText)) {
                this.nameWidget.setMessage(this.binding.getDisplayName().withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
            } else {
                this.nameWidget.setMessage(this.binding.getDisplayName());
            }

            List<KeyBinding> conflictedBindings = this.binding.getKeyCodes().isEmpty() ? null : conflictKeyBindings
                    .stream()
                    .filter(list -> list.contains(this.binding))
                    .findFirst()
                    .orElse(null);

            if (conflictedBindings != null) {
                MutableComponent tooltipText = Component.empty();
                tooltipText.append(VisualKeymap.getTranslatedComponent("gui.tooltip.conflict")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
                for (KeyBinding binding : conflictedBindings) {
                    if (binding != this.binding) {
                        tooltipText.append("\n").append(binding.getDisplayName());
                    }
                }
                this.editButton.setTooltip(Tooltip.create(tooltipText));
            } else {
                this.editButton.setTooltip(null);
            }

            MutableComponent keyText = this.binding.getBoundKeysLocalizedText().copy();
            if (sharedData.selectedKeyBinding == this.binding) {
                this.editButton.setMessage(Component.literal("> ")
                        .append(keyText.withStyle(ChatFormatting.WHITE)).append(" <")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (conflictedBindings != null) {
                this.editButton.setMessage(keyText.withStyle(ChatFormatting.GOLD));
            } else {
                this.editButton.setMessage(keyText);
            }

            this.resetButton.active = !this.binding.isDefault();
        }
    }
}
