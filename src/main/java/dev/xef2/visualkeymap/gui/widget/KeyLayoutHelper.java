package dev.xef2.visualkeymap.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class KeyLayoutHelper {
    public static KeyboardLayout getLayout(boolean isFull) {
        List<KeyLayout> keys = new ArrayList<>();

        keys.add(new KeyLayout("escape", 0.0, 0.0));
        keys.add(new KeyLayout("f1", 0.0, 2.0));
        keys.add(new KeyLayout("f2", 0.0, 3.0));
        keys.add(new KeyLayout("f3", 0.0, 4.0));
        keys.add(new KeyLayout("f4", 0.0, 5.0));
        keys.add(new KeyLayout("f5", 0.0, 6.5));
        keys.add(new KeyLayout("f6", 0.0, 7.5));
        keys.add(new KeyLayout("f7", 0.0, 8.5));
        keys.add(new KeyLayout("f8", 0.0, 9.5));
        keys.add(new KeyLayout("f9", 0.0, 11.0));
        keys.add(new KeyLayout("f10", 0.0, 12.0));
        keys.add(new KeyLayout("f11", 0.0, 13.0));
        keys.add(new KeyLayout("f12", 0.0, 14.0));

        keys.add(new KeyLayout("grave.accent", 1.0, 0.0));
        for (int i = 1; i <= 10; i++) {
            keys.add(new KeyLayout(i == 10 ? "0" : String.valueOf(i), 1.0, i));
        }
        keys.add(new KeyLayout("minus", 1.0, 11.0));
        keys.add(new KeyLayout("equal", 1.0, 12.0));
        keys.add(new KeyLayout("backspace", 1.0, 13.0, 2.0));

        keys.add(new KeyLayout("tab", 2.0, 0.0, 1.5));
        String[] row2Keys = {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "left.bracket", "right.bracket"};
        for (int i = 0; i < row2Keys.length; i++) {
            keys.add(new KeyLayout(row2Keys[i], 2.0, 1.5 + i));
        }
        keys.add(new KeyLayout("backslash", 2.0, 13.5, 1.5));

        keys.add(new KeyLayout("caps.lock", 3.0, 0.0, 1.75));
        String[] row3Keys = {"a", "s", "d", "f", "g", "h", "j", "k", "l", "semicolon", "apostrophe"};
        for (int i = 0; i < row3Keys.length; i++) {
            keys.add(new KeyLayout(row3Keys[i], 3.0, 1.75 + i));
        }
        keys.add(new KeyLayout("enter", 3.0, 12.75, 2.25));

        keys.add(new KeyLayout("left.shift", 4.0, 0.0, 2.25));
        String[] row4Keys = {"z", "x", "c", "v", "b", "n", "m", "comma", "period", "slash"};
        for (int i = 0; i < row4Keys.length; i++) {
            keys.add(new KeyLayout(row4Keys[i], 4.0, 2.25 + i));
        }
        keys.add(new KeyLayout("right.shift", 4.0, 12.25, 2.75));

        keys.add(new KeyLayout("left.control", 5.0, 0.0, 1.25));
        keys.add(new KeyLayout("left.win", 5.0, 1.25, 1.25));
        keys.add(new KeyLayout("left.alt", 5.0, 2.5, 1.25));
        keys.add(new KeyLayout("space", 5.0, 3.75, 6.25));
        keys.add(new KeyLayout("right.alt", 5.0, 10, 1.25));
        keys.add(new KeyLayout("right.win", 5.0, 11.25, 1.25));
        keys.add(new KeyLayout("menu", 5.0, 12.5, 1.25));
        keys.add(new KeyLayout("right.control", 5.0, 13.75, 1.25));

        keys.add(new KeyLayout("print.screen", 0.0, 15.0));
        keys.add(new KeyLayout("scroll.lock", 0.0, 16.0));
        keys.add(new KeyLayout("pause", 0.0, 17.0));

        keys.add(new KeyLayout("insert", 1.0, 15.0));
        keys.add(new KeyLayout("home", 1.0, 16.0));
        keys.add(new KeyLayout("page.up", 1.0, 17.0));
        keys.add(new KeyLayout("delete", 2.0, 15.0));
        keys.add(new KeyLayout("end", 2.0, 16.0));
        keys.add(new KeyLayout("page.down", 2.0, 17.0));

        keys.add(new KeyLayout("mouse.left", 3.0, 15.0));
        keys.add(new KeyLayout("mouse.middle", 3.0, 16.0));
        keys.add(new KeyLayout("mouse.right", 3.0, 17.0));
        keys.add(new KeyLayout("mouse.4", 4.0, 15.0));
        keys.add(new KeyLayout("mouse.5", 4.0, 17.0));

        keys.add(new KeyLayout("up", 4.0, 16.0));
        keys.add(new KeyLayout("left", 5.0, 15.0));
        keys.add(new KeyLayout("down", 5.0, 16.0));
        keys.add(new KeyLayout("right", 5.0, 17.0));

        if (isFull) {
            double numpadColOffset = 18.0;

            keys.add(new KeyLayout("num.lock", 1.0, numpadColOffset + 0.0));
            keys.add(new KeyLayout("keypad.divide", 1.0, numpadColOffset + 1.0));
            keys.add(new KeyLayout("keypad.multiply", 1.0, numpadColOffset + 2.0));
            keys.add(new KeyLayout("keypad.subtract", 1.0, numpadColOffset + 3.0));

            keys.add(new KeyLayout("keypad.7", 2.0, numpadColOffset + 0.0));
            keys.add(new KeyLayout("keypad.8", 2.0, numpadColOffset + 1.0));
            keys.add(new KeyLayout("keypad.9", 2.0, numpadColOffset + 2.0));
            keys.add(new KeyLayout("keypad.add", 2.0, numpadColOffset + 3.0, 1.0, 2.0)); // Height Mult 2.0 (Row 2 & 3)

            keys.add(new KeyLayout("keypad.4", 3.0, numpadColOffset + 0.0));
            keys.add(new KeyLayout("keypad.5", 3.0, numpadColOffset + 1.0));
            keys.add(new KeyLayout("keypad.6", 3.0, numpadColOffset + 2.0));

            keys.add(new KeyLayout("keypad.1", 4.0, numpadColOffset + 0.0));
            keys.add(new KeyLayout("keypad.2", 4.0, numpadColOffset + 1.0));
            keys.add(new KeyLayout("keypad.3", 4.0, numpadColOffset + 2.0));
            keys.add(new KeyLayout("keypad.enter", 4.0, numpadColOffset + 3.0, 1.0, 2.0)); // Height Mult 2.0 (Row 4 & 5)

            keys.add(new KeyLayout("keypad.0", 5.0, numpadColOffset + 0.0, 2.0)); // Width Mult 2.0
            keys.add(new KeyLayout("keypad.decimal", 5.0, numpadColOffset + 2.0));
        }

        return new KeyboardLayout(keys, 6.0, isFull ? 22.0 : 18.0);
    }

    @Environment(EnvType.CLIENT)
    public record KeyboardLayout(List<KeyLayout> keys, double rows, double columns) {
    }

    @Environment(EnvType.CLIENT)
    public record KeyLayout(String translationKey, double row, double col, double widthMult, double heightMult) {
        public KeyLayout(String translationKey, double row, double col) {
            this(translationKey, row, col, 1.0, 1.0);
        }

        public KeyLayout(String translationKey, double row, double col, double widthMult) {
            this(translationKey, row, col, widthMult, 1.0);
        }

        public InputUtil.Key getKey() {
            String translationKey = this.translationKey;
            if (!translationKey.startsWith("mouse.")) {
                translationKey = "keyboard." + translationKey;
            }
            return InputUtil.fromTranslationKey("key." + translationKey);
        }
    }
}
