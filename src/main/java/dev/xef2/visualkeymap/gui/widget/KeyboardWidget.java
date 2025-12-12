package dev.xef2.visualkeymap.gui.widget;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.WrapperWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class KeyboardWidget extends WrapperWidget {

    private static final int KEY_SPACING = 1;

    private int maxHeight;
    private int keySize;
    private final KeyLayoutHelper.KeyboardLayout keyboardLayout;
    private final Map<KeyLayoutHelper.KeyLayout, KeyWidget> keyWidgetMap = new HashMap<>();

    public KeyboardWidget(
            int x, int y, int width, int maxHeight,
            boolean isFull, VisualKeymapScreen.SharedData sharedData,
            Function<InputUtil.Key, List<? extends KeyBinding>> bindingGetter,
            Consumer<InputUtil.Key> keySelector
    ) {
        super(x, y, width, 0);
        this.maxHeight = maxHeight;

        this.keyboardLayout = KeyLayoutHelper.getLayout(isFull);

        for (KeyLayoutHelper.KeyLayout keyLayout : this.keyboardLayout.keys()) {
            String translationKey = VisualKeymap.getTranslationKey("key." + keyLayout.translationKey());
            InputUtil.Key key = keyLayout.getKey();
            Text text = I18n.hasTranslation(translationKey) ?
                    Text.translatable(translationKey) :
                    key.getLocalizedText();
            this.keyWidgetMap.put(keyLayout, new KeyWidget(
                    key, text, sharedData, () -> bindingGetter.apply(key), () -> keySelector.accept(key)
            ));
        }

        this.updateSizeAndHeight();
    }

    public void updateKeyBindings() {
        this.keyWidgetMap.values().forEach(KeyWidget::updateBindings);
    }

    public void setSize(int width, int maxHeight) {
        this.width = width;
        this.maxHeight = maxHeight;
        this.updateSizeAndHeight();
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
        this.keyWidgetMap.values().forEach(consumer);
    }

    private int getSizeWithSpacing(double mult) {
        return (int) (mult * this.keySize + (mult - 1) * KEY_SPACING);
    }

    private void updateSizeAndHeight() {
        double totalCols = this.keyboardLayout.columns();
        double totalRows = this.keyboardLayout.rows();

        this.keySize = MathHelper.floor(Math.min(
                (this.width - (totalCols - 1) * KEY_SPACING) / totalCols,
                (this.maxHeight - (totalRows - 1) * KEY_SPACING) / totalRows
        ));
        this.height = getSizeWithSpacing(totalRows);
    }

    @Override
    public void refreshPositions() {
        int totalWidth = getSizeWithSpacing(this.keyboardLayout.columns());

        int startX = this.getX() + (this.width - totalWidth) / 2;
        int startY = this.getY();

        for (KeyLayoutHelper.KeyLayout keyLayout : this.keyboardLayout.keys()) {
            int width = getSizeWithSpacing(keyLayout.widthMult());
            int height = getSizeWithSpacing(keyLayout.heightMult());

            int x = startX + (int) (keyLayout.col() * (this.keySize + KEY_SPACING));
            int y = startY + (int) (keyLayout.row() * (this.keySize + KEY_SPACING));

            this.keyWidgetMap.get(keyLayout).setDimensionsAndPosition(width, height, x, y);
        }
    }
}
