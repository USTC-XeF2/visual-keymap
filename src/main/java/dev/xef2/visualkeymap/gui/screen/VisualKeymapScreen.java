package dev.xef2.visualkeymap.gui.screen;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.widget.KeybindsListWidget;
import dev.xef2.visualkeymap.gui.widget.KeyboardWidget;
import dev.xef2.visualkeymap.mixin.ThreePartsLayoutWidgetAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class VisualKeymapScreen extends GameOptionsScreen {

    private List<? extends KeyBinding> keyBindings = List.of();
    private final SharedData sharedData = new SharedData();
    private final List<InputUtil.Key> pressedKeys = new ArrayList<>();

    private KeyboardWidget keyboardWidget;
    private KeybindsListWidget keybindsListWidget;

    public VisualKeymapScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, VisualKeymap.getTranslationText("gui.keymap_title"));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void initHeader() {
        DirectionalLayoutWidget header = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        header.getMainPositioner().alignHorizontalCenter();
        header.add(new TextWidget(this.title, this.textRenderer));
        TextFieldWidget searchBox = header.add(new TextFieldWidget(this.textRenderer, 0, 0, 200, 15, Text.empty()));
        searchBox.setPlaceholder(VisualKeymap.getTranslationText("gui.search_hint").fillStyle(TextFieldWidget.SEARCH_STYLE));
        searchBox.setChangedListener(search -> {
            this.sharedData.searchText = search;
            this.keybindsListWidget.createEntries();
        });
        this.layout.setHeaderHeight((int) (12.0 + 9.0 + 15.0));
    }

    @Override
    protected void initBody() {
        this.keyBindings = VisualKeymap.getKeyBindings();

        this.keyboardWidget = this.layout.addBody(new KeyboardWidget(
                0, 0, 0, 0,
                true, sharedData,
                this::getBindingsForKey, this::setSelectedKey
        ), Positioner::alignTop);
        this.keybindsListWidget = this.layout.addBody(new KeybindsListWidget(
                this.client, 0, 0, 0, sharedData,
                k -> {
                    k.resetToDefault();
                    this.keyboardWidget.updateKeyBindings();
                }
        ), Positioner::alignBottom);
        this.keybindsListWidget.setKeyBindings(this.getUnboundBindings());
    }

    private List<? extends KeyBinding> getUnboundBindings() {
        return this.keyBindings.stream()
                .filter(binding -> binding.getKeyCodes().isEmpty())
                .toList();
    }

    private List<? extends KeyBinding> getBindingsForKey(InputUtil.Key key) {
        return this.keyBindings.stream()
                .filter(binding -> binding.getFullKeyCodes().contains(key.getCode()))
                .toList();
    }

    private void setSelectedKey(InputUtil.Key key) {
        int keyCode = key.getCode();
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
    protected void refreshWidgetPositions() {
        int cHeight = this.layout.getContentHeight();
        int keyboardPadding = 5;

        ((ThreePartsLayoutWidgetAccessor) this.layout).getBody().setMinHeight(cHeight);
        this.keyboardWidget.setSize(this.width - keyboardPadding * 2, cHeight / 2);
        this.keybindsListWidget.position(
                this.width, cHeight / 2 - keyboardPadding, this.layout.getHeaderHeight() + cHeight / 2 + keyboardPadding);
        super.refreshWidgetPositions();
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
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.sharedData.selectedKeyBinding != null) {
            this.pressedKeys.add(InputUtil.Type.MOUSE.createFromCode(click.button()));
            this.setKeyBinding(true);
            return true;
        } else {
            return super.mouseClicked(click, doubled);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        InputUtil.Key key = InputUtil.fromKeyCode(input);
        if (this.sharedData.selectedKeyBinding != null && !this.pressedKeys.contains(key)) {
            if (!input.isEscape()) {
                this.pressedKeys.add(key);
            }
            this.setKeyBinding(input.isEscape());
            return true;
        } else {
            return super.keyPressed(input);
        }
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        if (this.sharedData.selectedKeyBinding != null) {
            this.setKeyBinding(true);
            return true;
        } else {
            return super.keyReleased(input);
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
