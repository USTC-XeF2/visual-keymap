package dev.xef2.visualkeymap.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.ModConfig;
import dev.xef2.visualkeymap.gui.widget.KeybindsListWidget;
import dev.xef2.visualkeymap.gui.widget.KeyboardWidget;
import dev.xef2.visualkeymap.mixin.HeaderAndFooterLayoutAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class VisualKeymapScreen extends OptionsSubScreen {

    private List<? extends KeyBinding> keyBindings = List.of();
    private final SharedData sharedData = new SharedData();
    private final List<InputConstants.Key> pressedKeys = new ArrayList<>();

    private KeyboardWidget keyboardWidget;
    private KeybindsListWidget keybindsListWidget;

    public VisualKeymapScreen(Screen parent, Options options) {
        super(parent, options, VisualKeymap.getTranslatedComponent("gui.keymap_title"));
    }

    @Override
    protected void addTitle() {
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(4));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(this.title, this.font));
        EditBox searchBox = header.addChild(new EditBox(this.font, 200, 15, Component.empty()));
        searchBox.setHint(VisualKeymap.getTranslatedComponent("gui.search_hint").withStyle(EditBox.SEARCH_HINT_STYLE));
        searchBox.setResponder(searchText -> {
            this.sharedData.searchText = searchText;
            this.keybindsListWidget.createEntries();
        });
        this.layout.setHeaderHeight((int) (12.0 + 9.0 + 15.0));
    }

    @Override
    protected void addContents() {
        this.keyBindings = VisualKeymap.getKeyBindings();

        this.keyboardWidget = this.layout.addToContents(new KeyboardWidget(
                0, 0, 0, 0,
                ModConfig.getInstance().showNumpad, sharedData,
                this::getBindingsForKey, this::setSelectedKey
        ), LayoutSettings::alignVerticallyTop);
        this.keybindsListWidget = this.layout.addToContents(new KeybindsListWidget(
                this.minecraft, 0, 0, 0, sharedData,
                k -> {
                    k.resetToDefault();
                    this.keyboardWidget.updateKeyBindings();
                }
        ), LayoutSettings::alignVerticallyBottom);
        this.keybindsListWidget.setKeyBindings(this.getUnboundBindings());
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(VisualKeymap.getTranslatedComponent("gui.open_config"),
                button -> this.minecraft.setScreen(
                        new ConfigScreen(new VisualKeymapScreen(this.lastScreen, this.options), this.options))
        ).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
    }

    private List<? extends KeyBinding> getUnboundBindings() {
        return this.keyBindings.stream()
                .filter(binding -> binding.getKeyCodes().isEmpty())
                .toList();
    }

    private List<? extends KeyBinding> getBindingsForKey(InputConstants.Key key) {
        return this.keyBindings.stream()
                .filter(binding -> binding.getFullKeyCodes().contains(key.getValue()))
                .toList();
    }

    private void setSelectedKey(InputConstants.Key key) {
        int keyCode = key.getValue();
        if (this.sharedData.selectedKeyCode != null && this.sharedData.selectedKeyCode == keyCode) {
            this.sharedData.selectedKeyCode = null;
            this.keybindsListWidget.setKeyBindings(this.getUnboundBindings());
        } else {
            this.sharedData.selectedKeyCode = keyCode;
            this.keybindsListWidget.setKeyBindings(this.getBindingsForKey(key));
        }
        this.sharedData.selectedKeyBinding = null;
    }

    @Override
    protected void repositionElements() {
        int contentHeight = this.layout.getContentHeight();
        int keyboardPadding = 5;

        ((HeaderAndFooterLayoutAccessor) this.layout).getContentsFrame().setMinHeight(contentHeight);
        this.keyboardWidget.setSize(this.width - keyboardPadding * 2, contentHeight / 2);
        this.keybindsListWidget.updateSizeAndPosition(
                this.width, contentHeight / 2 - keyboardPadding, this.layout.getHeaderHeight() + contentHeight / 2 + keyboardPadding);
        super.repositionElements();
    }

    private void setKeyBinding(boolean ended) {
        this.sharedData.selectedKeyBinding.setBoundKeys(this.pressedKeys);
        if (ended || this.pressedKeys.size() == this.sharedData.selectedKeyBinding.getMaxBoundKeys()) {
            this.sharedData.selectedKeyBinding = null;
            this.pressedKeys.clear();
        }
        this.keyboardWidget.updateKeyBindings();
        this.keybindsListWidget.updateAllEntries();
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (this.sharedData.selectedKeyBinding != null) {
            this.pressedKeys.add(InputConstants.Type.MOUSE.getOrCreate(mouseButtonEvent.button()));
            this.setKeyBinding(true);
            return true;
        } else {
            return super.mouseClicked(mouseButtonEvent, bl);
        }
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        InputConstants.Key key = InputConstants.getKey(keyEvent);
        if (this.sharedData.selectedKeyBinding != null && !this.pressedKeys.contains(key)) {
            if (!keyEvent.isEscape()) {
                this.pressedKeys.add(key);
            }
            this.setKeyBinding(keyEvent.isEscape());
            return true;
        } else {
            return super.keyPressed(keyEvent);
        }
    }

    @Override
    public boolean keyReleased(@NotNull KeyEvent keyEvent) {
        if (this.sharedData.selectedKeyBinding != null) {
            this.setKeyBinding(true);
            return true;
        } else {
            return super.keyReleased(keyEvent);
        }
    }

    @Override
    public void removed() {
        VisualKeymap.saveKeyBindings();
        super.removed();
    }

    public static class SharedData {
        public String searchText = "";
        public Integer selectedKeyCode = null;
        public KeyBinding selectedKeyBinding = null;
    }
}
