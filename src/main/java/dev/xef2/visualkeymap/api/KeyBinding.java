package dev.xef2.visualkeymap.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public abstract class KeyBinding {

    private final Text category;
    private final Text name;
    private final int maxBoundKeys;

    public KeyBinding(Text category, Text name, int maxBoundKeys) {
        this.category = category;
        this.name = name;
        this.maxBoundKeys = maxBoundKeys;
    }

    public MutableText getDisplayName() {
        return this.category.copy().append(Text.of(" - ")).append(this.name);
    }

    public int getMaxBoundKeys() {
        return this.maxBoundKeys;
    }

    abstract public List<Integer> getKeyCodes();

    public List<Integer> getModifierKeyCodes() {
        return List.of();
    }

    public final List<Integer> getFullKeyCodes() {
        List<Integer> fullKeyCodes = new ArrayList<>(this.getModifierKeyCodes());
        fullKeyCodes.addAll(this.getKeyCodes());
        return fullKeyCodes;
    }

    public int getOrder() {
        return 0;
    }

    public static List<List<KeyBinding>> getConflictBindings(List<? extends KeyBinding> bindings) {
        if (bindings.size() <= 1) {
            return List.of();
        }

        Map<ConflictKey, List<KeyBinding>> conflictMap = new HashMap<>();

        for (KeyBinding binding : bindings) {
            ConflictKey key = new ConflictKey(binding.getFullKeyCodes(), binding.getOrder());
            conflictMap.computeIfAbsent(key, k -> new ArrayList<>()).add(binding);
        }

        return conflictMap.values().stream()
                .filter(list -> list.size() > 1)
                .toList();
    }

    private static Text getLocalizedTextFromCode(int code) {
        InputUtil.Type inputType = code >= 0 && code <= 7
                ? InputUtil.Type.MOUSE
                : InputUtil.Type.KEYSYM;
        return inputType.createFromCode(code).getLocalizedText();
    }

    public Text getBoundKeysLocalizedText() {
        List<Integer> keyCodes = this.getKeyCodes();
        if (keyCodes.isEmpty()) {
            return Text.translatable("key.keyboard.unknown");
        }

        MutableText text = Text.empty();
        List<Integer> modifierKeyCodes = this.getModifierKeyCodes();
        if (!modifierKeyCodes.isEmpty()) {
            MutableText modifierText = Text.of("[ ").copy().formatted(Formatting.ITALIC);
            for (int modifierKeyCode : modifierKeyCodes) {
                modifierText.append(getLocalizedTextFromCode(modifierKeyCode));
                modifierText.append(" + ");
            }
            modifierText.append("] ");
            text.append(modifierText);
        }
        for (int i = 0; i < keyCodes.size(); i++) {
            if (i > 0) {
                text.append(Text.of(" + "));
            }
            text.append(getLocalizedTextFromCode(keyCodes.get(i)));
        }
        return text;
    }

    abstract public void setBoundKeys(List<InputUtil.Key> keys);

    abstract public boolean isDefault();

    abstract public void resetToDefault();

    private record ConflictKey(List<Integer> fullKeyCodes, int order) {
    }
}
