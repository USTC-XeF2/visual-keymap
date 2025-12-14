package dev.xef2.visualkeymap.integration;

import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.api.VisualKeymapApi;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MaLiLibIntegration implements VisualKeymapApi<MaLiLibIntegration.MaLiLibKeyBinding> {

    @Override
    public List<MaLiLibKeyBinding> getKeyBindings() {
        return InputEventHandler.getKeybindManager().getKeybindCategories().stream()
                .flatMap(category -> category.getHotkeys().stream().map(
                        hotkey -> new MaLiLibKeyBinding(category, hotkey)
                ))
                .toList();
    }

    @Override
    public void save() {
        ((ConfigManager) ConfigManager.getInstance()).saveAllConfigs();
        InputEventHandler.getKeybindManager().updateUsedKeys();
    }

    public static class MaLiLibKeyBinding extends KeyBinding {

        private final IHotkey hotkey;

        public MaLiLibKeyBinding(KeybindCategory category, IHotkey hotkey) {
            super(Text.of(category.getModName()), Text.of(hotkey.getConfigGuiDisplayName()), 4);
            this.hotkey = hotkey;
        }

        @Override
        public List<Integer> getKeyCodes() {
            return this.hotkey.getKeybind().getKeys().stream()
                    .map(key -> key < -1 ? key + 100 : key)
                    .toList();
        }

        @Override
        protected List<String> getSearchableStrings() {
            ArrayList<String> strings = new ArrayList<>(super.getSearchableStrings());
            strings.add(this.hotkey.getName());
            return strings;
        }

        @Override
        public void setBoundKeys(List<InputUtil.Key> keys) {
            IKeybind keybind = this.hotkey.getKeybind();
            keybind.clearKeys();
            keys.stream().map(InputUtil.Key::getCode)
                    .map(code -> code >= 0 && code <= 7 ? code - 100 : code)
                    .forEach(keybind::addKey);
        }

        @Override
        public boolean isDefault() {
            return this.hotkey.getStringValue().equals(this.hotkey.getDefaultStringValue());
        }

        @Override
        public void resetToDefault() {
            this.hotkey.resetToDefault();
        }
    }
}
