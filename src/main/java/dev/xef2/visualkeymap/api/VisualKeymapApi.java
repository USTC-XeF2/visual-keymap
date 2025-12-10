package dev.xef2.visualkeymap.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface VisualKeymapApi<T extends KeyBinding> {
    List<T> getKeyBindings();

    void save();
}
