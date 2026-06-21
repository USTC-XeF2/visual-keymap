package dev.xef2.visualkeymap.gui.screen;

import dev.xef2.visualkeymap.ModConfig;
import dev.xef2.visualkeymap.VisualKeymap;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;

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
                ),
                new OptionInstance<>(
                        VisualKeymap.getTranslationKey("gui.option.sort_mode"),
                        OptionInstance.noTooltip(),
                        (_, value) -> Component.translatable(VisualKeymap.getTranslationKey(value.getTranslationKey())),
                        new OptionInstance.Enum<>(
                                Arrays.asList(ModConfig.SortMode.values()),
                                StringRepresentable.fromEnum(ModConfig.SortMode::values)
                        ),
                        modConfig.sortMode,
                        (value) -> modConfig.sortMode = value
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
