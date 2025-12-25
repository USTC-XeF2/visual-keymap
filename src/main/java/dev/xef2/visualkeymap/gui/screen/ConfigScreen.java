package dev.xef2.visualkeymap.gui.screen;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.ModConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

public class ConfigScreen extends OptionsSubScreen {
    public ConfigScreen(Screen screen, Options options) {
        super(screen, options, VisualKeymap.getTranslatedComponent("gui.config_title"));
    }

    private static OptionInstance<?>[] getOptions() {
        ModConfig modConfig = ModConfig.getInstance();
        return new OptionInstance[]{
                OptionInstance.createBoolean(
                        VisualKeymap.getTranslationKey("gui.option.show_numpad"),
                        modConfig.showNumpad,
                        (value) -> modConfig.showNumpad = value
                ),
                OptionInstance.createBoolean(
                        VisualKeymap.getTranslationKey("gui.option.prioritize_conflicting_keybinds"),
                        modConfig.prioritizeConflictingKeybinds,
                        (value) -> modConfig.prioritizeConflictingKeybinds = value
                )
        };
    }

    @Override
    protected void addOptions() {
        if (this.list != null) {
            this.list.addSmall(getOptions());
        }
    }

    @Override
    public void removed() {
        ModConfig.getInstance().save();
    }
}
