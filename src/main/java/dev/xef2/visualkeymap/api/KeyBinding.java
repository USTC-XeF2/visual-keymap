package dev.xef2.visualkeymap.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

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

    private static InputUtil.Key getKeyFromCode(int code) {
        return code >= 0 && code <= 7
                ? InputUtil.Type.MOUSE.createFromCode(code)
                : InputUtil.Type.KEYSYM.createFromCode(code);
    }

    public Text getBoundKeysLocalizedText() {
        List<Integer> keyCodes = this.getKeyCodes();
        if (keyCodes.isEmpty()) {
            return Text.translatable("key.keyboard.unknown");
        }

        MutableText text = Text.empty();
        for (int i = 0; i < keyCodes.size(); i++) {
            if (i > 0) {
                text.append(Text.of(" + "));
            }
            InputUtil.Key key = getKeyFromCode(keyCodes.get(i));
            text.append(key.getLocalizedText());
        }
        return text;
    }

    abstract public void setBoundKeys(List<InputUtil.Key> keys);

    abstract public boolean isDefault();

    abstract public void resetToDefault();
}
