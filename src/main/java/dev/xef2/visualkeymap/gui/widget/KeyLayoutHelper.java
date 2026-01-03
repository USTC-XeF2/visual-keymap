package dev.xef2.visualkeymap.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class KeyLayoutHelper {

    private static final double SPACING = 0.25;
    private static final double MAIN_ROW_OFF = 1.0 + SPACING;
    private static final double UTIL_COL_OFF = 15.0 + SPACING;
    private static final double NUMPAD_COL_OFF = UTIL_COL_OFF + 3.0 + SPACING;

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

        keys.add(new KeyLayout("grave.accent", MAIN_ROW_OFF, 0.0));
        for (int i = 1; i <= 10; i++) {
            keys.add(new KeyLayout(i == 10 ? "0" : String.valueOf(i), MAIN_ROW_OFF, i));
        }
        keys.add(new KeyLayout("minus", MAIN_ROW_OFF, 11.0));
        keys.add(new KeyLayout("equal", MAIN_ROW_OFF, 12.0));
        keys.add(new KeyLayout("backspace", MAIN_ROW_OFF, 13.0, 2.0));

        keys.add(new KeyLayout("tab", MAIN_ROW_OFF + 1.0, 0.0, 1.5));
        String[] row2Keys = {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "left.bracket", "right.bracket"};
        for (int i = 0; i < row2Keys.length; i++) {
            keys.add(new KeyLayout(row2Keys[i], MAIN_ROW_OFF + 1.0, 1.5 + i));
        }
        keys.add(new KeyLayout("backslash", MAIN_ROW_OFF + 1.0, 13.5, 1.5));

        keys.add(new KeyLayout("caps.lock", MAIN_ROW_OFF + 2.0, 0.0, 1.75));
        String[] row3Keys = {"a", "s", "d", "f", "g", "h", "j", "k", "l", "semicolon", "apostrophe"};
        for (int i = 0; i < row3Keys.length; i++) {
            keys.add(new KeyLayout(row3Keys[i], MAIN_ROW_OFF + 2.0, 1.75 + i));
        }
        keys.add(new KeyLayout("enter", MAIN_ROW_OFF + 2.0, 12.75, 2.25));

        keys.add(new KeyLayout("left.shift", MAIN_ROW_OFF + 3.0, 0.0, 2.25));
        String[] row4Keys = {"z", "x", "c", "v", "b", "n", "m", "comma", "period", "slash"};
        for (int i = 0; i < row4Keys.length; i++) {
            keys.add(new KeyLayout(row4Keys[i], MAIN_ROW_OFF + 3.0, 2.25 + i));
        }
        keys.add(new KeyLayout("right.shift", MAIN_ROW_OFF + 3.0, 12.25, 2.75));

        keys.add(new KeyLayout("left.control", MAIN_ROW_OFF + 4.0, 0.0, 1.25));
        keys.add(new KeyLayout("left.win", MAIN_ROW_OFF + 4.0, 1.25, 1.25));
        keys.add(new KeyLayout("left.alt", MAIN_ROW_OFF + 4.0, 2.5, 1.25));
        keys.add(new KeyLayout("space", MAIN_ROW_OFF + 4.0, 3.75, 6.25));
        keys.add(new KeyLayout("right.alt", MAIN_ROW_OFF + 4.0, 10, 1.25));
        keys.add(new KeyLayout("right.win", MAIN_ROW_OFF + 4.0, 11.25, 1.25));
        keys.add(new KeyLayout("menu", MAIN_ROW_OFF + 4.0, 12.5, 1.25));
        keys.add(new KeyLayout("right.control", MAIN_ROW_OFF + 4.0, 13.75, 1.25));

        keys.add(new KeyLayout("print.screen", 0.0, UTIL_COL_OFF));
        keys.add(new KeyLayout("scroll.lock", 0.0, UTIL_COL_OFF + 1.0));
        keys.add(new KeyLayout("pause", 0.0, UTIL_COL_OFF + 2.0));

        keys.add(new KeyLayout("insert", MAIN_ROW_OFF, UTIL_COL_OFF));
        keys.add(new KeyLayout("home", MAIN_ROW_OFF, UTIL_COL_OFF + 1.0));
        keys.add(new KeyLayout("page.up", MAIN_ROW_OFF, UTIL_COL_OFF + 2.0));
        keys.add(new KeyLayout("delete", MAIN_ROW_OFF + 1.0, UTIL_COL_OFF));
        keys.add(new KeyLayout("end", MAIN_ROW_OFF + 1.0, UTIL_COL_OFF + 1.0));
        keys.add(new KeyLayout("page.down", MAIN_ROW_OFF + 1.0, UTIL_COL_OFF + 2.0));

        keys.add(new KeyLayout("mouse.left", MAIN_ROW_OFF + 2.0, UTIL_COL_OFF));
        keys.add(new KeyLayout("mouse.middle", MAIN_ROW_OFF + 2.0, UTIL_COL_OFF + 1.0));
        keys.add(new KeyLayout("mouse.right", MAIN_ROW_OFF + 2.0, UTIL_COL_OFF + 2.0));
        keys.add(new KeyLayout("mouse.4", MAIN_ROW_OFF + 3.0, UTIL_COL_OFF));
        keys.add(new KeyLayout("mouse.5", MAIN_ROW_OFF + 3.0, UTIL_COL_OFF + 2.0));

        keys.add(new KeyLayout("up", MAIN_ROW_OFF + 3.0, UTIL_COL_OFF + 1.0));
        keys.add(new KeyLayout("left", MAIN_ROW_OFF + 4.0, UTIL_COL_OFF));
        keys.add(new KeyLayout("down", MAIN_ROW_OFF + 4.0, UTIL_COL_OFF + 1.0));
        keys.add(new KeyLayout("right", MAIN_ROW_OFF + 4.0, UTIL_COL_OFF + 2.0));

        double totalCols = UTIL_COL_OFF + 3.0;

        if (isFull) {
            keys.add(new KeyLayout("num.lock", MAIN_ROW_OFF, NUMPAD_COL_OFF));
            keys.add(new KeyLayout("keypad.divide", MAIN_ROW_OFF, NUMPAD_COL_OFF + 1.0));
            keys.add(new KeyLayout("keypad.multiply", MAIN_ROW_OFF, NUMPAD_COL_OFF + 2.0));
            keys.add(new KeyLayout("keypad.subtract", MAIN_ROW_OFF, NUMPAD_COL_OFF + 3.0));

            keys.add(new KeyLayout("keypad.7", MAIN_ROW_OFF + 1.0, NUMPAD_COL_OFF));
            keys.add(new KeyLayout("keypad.8", MAIN_ROW_OFF + 1.0, NUMPAD_COL_OFF + 1.0));
            keys.add(new KeyLayout("keypad.9", MAIN_ROW_OFF + 1.0, NUMPAD_COL_OFF + 2.0));
            keys.add(new KeyLayout("keypad.add", MAIN_ROW_OFF + 1.0, NUMPAD_COL_OFF + 3.0, 1.0, 2.0));

            keys.add(new KeyLayout("keypad.4", MAIN_ROW_OFF + 2.0, NUMPAD_COL_OFF));
            keys.add(new KeyLayout("keypad.5", MAIN_ROW_OFF + 2.0, NUMPAD_COL_OFF + 1.0));
            keys.add(new KeyLayout("keypad.6", MAIN_ROW_OFF + 2.0, NUMPAD_COL_OFF + 2.0));

            keys.add(new KeyLayout("keypad.1", MAIN_ROW_OFF + 3.0, NUMPAD_COL_OFF));
            keys.add(new KeyLayout("keypad.2", MAIN_ROW_OFF + 3.0, NUMPAD_COL_OFF + 1.0));
            keys.add(new KeyLayout("keypad.3", MAIN_ROW_OFF + 3.0, NUMPAD_COL_OFF + 2.0));
            keys.add(new KeyLayout("keypad.enter", MAIN_ROW_OFF + 3.0, NUMPAD_COL_OFF + 3.0, 1.0, 2.0));

            keys.add(new KeyLayout("keypad.0", MAIN_ROW_OFF + 4.0, NUMPAD_COL_OFF, 2.0));
            keys.add(new KeyLayout("keypad.decimal", MAIN_ROW_OFF + 4.0, NUMPAD_COL_OFF + 2.0));

            totalCols = NUMPAD_COL_OFF + 4.0;
        }

        return new KeyboardLayout(keys, MAIN_ROW_OFF + 5.0, totalCols);
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

        public InputConstants.Key getKey() {
            String translationKey = this.translationKey;
            if (!translationKey.startsWith("mouse.")) {
                translationKey = "keyboard." + translationKey;
            }
            return InputConstants.getKey("key." + translationKey);
        }
    }
}
