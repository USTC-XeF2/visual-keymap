package dev.xef2.visualkeymap.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xef2.visualkeymap.KeymapSnapshot;
import dev.xef2.visualkeymap.VisualKeymap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ImportPreviewScreen extends OptionsSubScreen {

    private static final int ENTRY_HEIGHT = 36;

    private final List<KeymapSnapshot.ImportMatch> matches;
    private ImportListWidget listWidget;

    public ImportPreviewScreen(Screen parent, List<KeymapSnapshot.ImportMatch> matches) {
        super(parent, Minecraft.getInstance().options, VisualKeymap.getTranslatedComponent("gui.import_preview_title", matches.size()));
        this.matches = new ArrayList<>(matches);
    }

    @Override
    protected void addContents() {
        this.listWidget = this.layout.addToContents(new ImportListWidget(
                this.minecraft, 0, 0, 0, ENTRY_HEIGHT
        ), LayoutSettings::alignVerticallyTop);
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(4));
        footer.addChild(Button.builder(
                VisualKeymap.getTranslatedComponent("gui.import_confirm"),
                _ -> {
                    for (var match : this.matches) {
                        if (match.selected) {
                            match.binding.setBoundKeys(match.getKeysFromEntry());
                        }
                    }
                    VisualKeymap.saveKeyBindings();
                    if (this.lastScreen instanceof VisualKeymapScreen vks) {
                        vks.refreshKeyBindings();
                    }
                    this.minecraft.setScreenAndShow(this.lastScreen);
                }
        ).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> this.onClose()).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.listWidget.updateSizeAndPosition(this.width, this.layout.getContentHeight(), this.layout.getHeaderHeight());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.lastScreen);
    }

    private class ImportListWidget extends ContainerObjectSelectionList<ImportListWidget.ImportEntry> {

        ImportListWidget(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
            updateEntries();
        }

        void updateEntries() {
            this.clearEntries();
            for (var match : matches) {
                this.addEntry(new ImportEntry(match));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(this.width - 40, 500);
        }

        @Override
        public int getRowLeft() {
            return (this.width - this.getRowWidth()) / 2;
        }

        class ImportEntry extends ContainerObjectSelectionList.Entry<ImportEntry> {

            private final KeymapSnapshot.ImportMatch match;
            private final Checkbox checkbox;
            private final StringWidget nameWidget;
            private final StringWidget changeWidget;

            ImportEntry(KeymapSnapshot.ImportMatch match) {
                this.match = match;

                this.checkbox = Checkbox.builder(Component.empty(), minecraft.font)
                        .selected(match.selected)
                        .onValueChange((_, selected) -> match.selected = selected)
                        .build();

                MutableComponent name = match.binding.getDisplayName().copy();
                this.nameWidget = new StringWidget(0, ENTRY_HEIGHT, name, minecraft.font);
                this.nameWidget.setTooltip(Tooltip.create(match.binding.getTooltip()));

                Component changeText = buildChangeText();
                this.changeWidget = new StringWidget(0, ENTRY_HEIGHT, changeText, minecraft.font);
            }

            private Component buildChangeText() {
                Component oldKeys = match.binding.getBoundKeysLocalizedText();

                List<Integer> newKeyCodes = match.entry.keys();
                Component newKeys;
                if (newKeyCodes.isEmpty()) {
                    newKeys = Component.translatable("key.keyboard.unknown");
                } else {
                    MutableComponent text = Component.empty();
                    for (int i = 0; i < newKeyCodes.size(); i++) {
                        if (i > 0) text.append(" + ");
                        text.append(InputConstants.Type.KEYSYM.getOrCreate(newKeyCodes.get(i)).getDisplayName());
                    }
                    newKeys = text;
                }

                return Component.empty()
                        .append(oldKeys)
                        .append(Component.literal(" → ").withStyle(ChatFormatting.GRAY))
                        .append(newKeys.copy().withStyle(ChatFormatting.GREEN));
            }

            @Override
            public void extractContent(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float f) {
                int y = this.getContentY();

                this.checkbox.setX(this.getContentX());
                this.checkbox.setY(y + (ENTRY_HEIGHT - this.checkbox.getHeight()) / 2);
                this.checkbox.extractRenderState(graphics, mouseX, mouseY, f);

                int nameX = this.getContentX() + 24;
                this.nameWidget.setPosition(nameX, y);
                this.nameWidget.setMaxWidth(ImportListWidget.this.getRowWidth() / 2 - 24);
                this.nameWidget.extractRenderState(graphics, mouseX, mouseY, f);

                int changeX = nameX + ImportListWidget.this.getRowWidth() / 2;
                this.changeWidget.setPosition(changeX, y);
                this.changeWidget.setMaxWidth(ImportListWidget.this.getRowWidth() / 2 - 10);
                this.changeWidget.extractRenderState(graphics, mouseX, mouseY, f);
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() {
                return List.of(this.checkbox);
            }

            @Override
            public @NotNull List<? extends NarratableEntry> narratables() {
                return List.of(this.checkbox);
            }
        }
    }
}
