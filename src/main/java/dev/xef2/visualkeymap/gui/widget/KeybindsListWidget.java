package dev.xef2.visualkeymap.gui.widget;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.ModConfig;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
        Comparator<KeyBinding> keyBindingComparator = Comparator
                .<KeyBinding>comparingInt(binding -> binding.containsSearchText(sharedData.searchText)
                        ? -1 : 0)
                .thenComparingInt(binding -> ModConfig.getInstance().prioritizeConflictingKeybinds
                        && conflictKeyBindings.stream().anyMatch(list -> list.contains(binding))
                        ? -1 : 0);
        List<? extends KeyBinding> sortedKeyBindings = this.keyBindings.stream()
                .sorted(keyBindingComparator)
                .toList();

        this.clearEntries();
        for (KeyBinding keyBinding : sortedKeyBindings) {
            this.addEntry(new dev.xef2.visualkeymap.gui.widget.KeybindsListWidget.Entry(keyBinding));
        }
        this.setScrollAmount(0.0);

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
            this.editButton = Button.builder(binding.getBoundKeysLocalizedText(), (button) -> {
                        sharedData.selectedKeyBinding = binding;
                        updateAllEntries();
                    })
                    .bounds(0, 0, 100, ROW_HEIGHT)
                    .build();
            this.resetButton = Button.builder(RESET_TEXT, (button) -> {
                        resetCallback.accept(binding);
                        updateAllEntries();
                    })
                    .bounds(0, 0, 50, ROW_HEIGHT)
                    .build();
        }

        @Override
        public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float f) {
            int y = this.getContentY() - 2;

            int resetX = KeybindsListWidget.this.scrollBarX() - this.resetButton.getWidth() - 10;
            this.resetButton.setPosition(resetX, y);
            this.resetButton.render(guiGraphics, mouseX, mouseY, f);

            int editX = resetX - 5 - this.editButton.getWidth();
            this.editButton.setPosition(editX, y);
            this.editButton.render(guiGraphics, mouseX, mouseY, f);

            int textX = this.getContentX();
            this.nameWidget.setPosition(textX, y);
            this.nameWidget.setMaxWidth(editX - textX - 5);
            this.nameWidget.render(guiGraphics, mouseX, mouseY, f);
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
