package dev.xef2.visualkeymap;

import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.api.MinecraftImpl;
import dev.xef2.visualkeymap.api.VisualKeymapApi;
import dev.xef2.visualkeymap.integration.CommandKeysIntegration;
import dev.xef2.visualkeymap.integration.MaLiLibIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class VisualKeymap implements ClientModInitializer {
    private static final String MOD_ID = "visualkeymap";

    private static final List<VisualKeymapApi<?>> apiImpl = new ArrayList<>(List.of(
            new MinecraftImpl()
    ));

    @Override
    public void onInitializeClient() {
        FabricLoader loader = FabricLoader.getInstance();

        loader.getEntrypointContainers(MOD_ID, VisualKeymapApi.class).forEach(entrypoint -> {
            try {
                VisualKeymapApi<?> api = entrypoint.getEntrypoint();
                apiImpl.add(api);
            } catch (Throwable ignored) {
            }
        });

        if (loader.isModLoaded("malilib")) {
            apiImpl.add(new MaLiLibIntegration());
        }

        if (loader.isModLoaded("commandkeys")) {
            apiImpl.add(new CommandKeysIntegration());
        }

        ModConfig.load();
    }

    public static List<? extends KeyBinding> getKeyBindings() {
        return apiImpl.stream().flatMap(api -> api.getKeyBindings().stream()).toList();
    }

    public static void saveKeyBindings() {
        apiImpl.forEach(VisualKeymapApi::save);
    }

    public static String getTranslationKey(String key) {
        return MOD_ID + "." + key;
    }

    public static MutableComponent getTranslatedComponent(String key, Object... args) {
        return Component.translatable(getTranslationKey(key), args);
    }
}
