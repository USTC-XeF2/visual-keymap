package dev.xef2.visualkeymap.api;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xef2.visualkeymap.mixin.KeyMappingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MinecraftImpl implements VisualKeymapApi<MinecraftImpl.MinecraftKeyBinding> {

    @Override
    public List<MinecraftKeyBinding> getKeyBindings() {
        return Arrays.stream(Minecraft.getInstance().options.keyMappings).map(MinecraftKeyBinding::new).toList();
    }

    @Override
    public void save() {
        // Minecraft handles keybinding saving automatically.
    }

    @Override
    public String getProviderName() {
        return "minecraft";
    }

    public static class MinecraftKeyBinding extends KeyBinding {

        private final KeyMapping keyMapping;

        public MinecraftKeyBinding(KeyMapping keyMapping) {
            super(keyMapping.getCategory().label(), Component.translatable(keyMapping.getName()), 1);
            this.keyMapping = keyMapping;
        }

        @Override
        public String getId() {
            return this.keyMapping.getName();
        }

        @Override
        public List<Integer> getKeyCodes() {
            InputConstants.Key boundKey = ((KeyMappingAccessor) this.keyMapping).getKey();
            return boundKey.equals(InputConstants.UNKNOWN) ? List.of() : List.of(boundKey.getValue());
        }

        @Override
        public void setBoundKeys(List<InputConstants.Key> keys) {
            this.keyMapping.setKey(keys.isEmpty() ? InputConstants.UNKNOWN : keys.getFirst());
            KeyMapping.resetMapping();
        }

        @Override
        public boolean isDefault() {
            return this.keyMapping.isDefault();
        }

        @Override
        public void resetToDefault() {
            this.setBoundKeys(List.of(this.keyMapping.getDefaultKey()));
        }
    }
}
