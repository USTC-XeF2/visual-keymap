package dev.xef2.visualkeymap.integration;

import dev.terminalmc.commandkeys.CommandKeys;
import dev.terminalmc.commandkeys.config.Config;
import dev.terminalmc.commandkeys.config.Keybind;
import dev.terminalmc.commandkeys.config.Macro;
import dev.terminalmc.commandkeys.config.Profile;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.api.VisualKeymapApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class CommandKeysIntegration implements VisualKeymapApi<CommandKeysIntegration.CommandKeysKeyBinding> {

    @Override
    public List<CommandKeysKeyBinding> getKeyBindings() {
        Profile currProfile = CommandKeys.profile();
        return currProfile.macroMap.entries().stream()
                .map(entry -> new CommandKeysKeyBinding(
                        currProfile, entry.getValue(), entry.getKey()
                ))
                .toList();
    }

    @Override
    public void save() {
        Config.save();
    }

    public static class CommandKeysKeyBinding extends KeyBinding {

        private final Profile profile;
        private final Macro macro;
        private final Keybind keybind;

        public CommandKeysKeyBinding(
                Profile profile,
                Macro macro,
                Keybind keybind
        ) {
            super(
                    Text.translatable("key.category.commandkeys.main"),
                    Text.translatable("option.commandkeys.profile", profile.name),
                    2
            );
            this.profile = profile;
            this.macro = macro;
            this.keybind = keybind;
        }

        @Override
        public List<Integer> getKeyCodes() {
            return Stream.of(this.keybind.getLimitKey(), this.keybind.getKey())
                    .filter(key -> !key.equals(InputUtil.UNKNOWN_KEY))
                    .map(InputUtil.Key::getCode)
                    .toList();
        }

        @Override
        protected List<String> getSearchableStrings() {
            ArrayList<String> strings = new ArrayList<>(super.getSearchableStrings());
            strings.addAll(this.macro.getMessages().stream().map(m -> m.string).toList());
            return strings;
        }

        @Override
        public void setBoundKeys(List<InputUtil.Key> keys) {
            InputUtil.Key key = InputUtil.UNKNOWN_KEY;
            InputUtil.Key limitKey = InputUtil.UNKNOWN_KEY;

            if (keys.size() == 1) {
                key = keys.getFirst();
            } else if (keys.size() > 1) {
                limitKey = keys.getFirst();
                key = keys.get(1);
            }

            this.profile.setKey(this.macro, this.keybind, key);
            this.profile.setLimitKey(this.macro, this.keybind, limitKey);
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public void resetToDefault() {
        }
    }
}
