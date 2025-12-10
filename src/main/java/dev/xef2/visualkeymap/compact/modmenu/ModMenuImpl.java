package dev.xef2.visualkeymap.compact.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.minecraft.client.MinecraftClient;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> new VisualKeymapScreen(screen, MinecraftClient.getInstance().options);
    }
}
