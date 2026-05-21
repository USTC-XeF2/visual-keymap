package dev.xef2.visualkeymap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import dev.xef2.visualkeymap.api.KeyBinding;
import dev.xef2.visualkeymap.api.VisualKeymapApi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record KeymapSnapshot(int version, List<KeymapSnapshot.BindingEntry> bindings) {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static KeymapSnapshot fromBindings(List<VisualKeymapApi<?>> apis) {
        List<BindingEntry> entries = new ArrayList<>();
        for (VisualKeymapApi<?> api : apis) {
            String provider = api.getProviderName();
            if (provider.isEmpty()) continue;
            for (KeyBinding binding : api.getKeyBindings()) {
                if (binding.isDefault()) continue;
                entries.add(new BindingEntry(
                        provider,
                        binding.getId(),
                        new ArrayList<>(binding.getKeyCodes()),
                        new ArrayList<>(binding.getModifierKeyCodes())
                ));
            }
        }
        return new KeymapSnapshot(1, entries);
    }

    public void writeToFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        }
    }

    public static KeymapSnapshot readFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, KeymapSnapshot.class);
        }
    }

    public List<ImportMatch> matchBindings(List<VisualKeymapApi<?>> apis) {
        List<ImportMatch> matches = new ArrayList<>();
        for (BindingEntry entry : this.bindings) {
            for (VisualKeymapApi<?> api : apis) {
                if (!api.getProviderName().equals(entry.provider())) continue;
                for (KeyBinding binding : api.getKeyBindings()) {
                    if (binding.getId().equals(entry.id())) {
                        matches.add(new ImportMatch(binding, entry));
                    }
                }
            }
        }
        return matches;
    }

    public record BindingEntry(String provider, String id, List<Integer> keys, List<Integer> modifiers) {
        public BindingEntry {
            provider = provider != null ? provider : "";
            id = id != null ? id : "";
            keys = keys != null ? keys : List.of();
            modifiers = modifiers != null ? modifiers : List.of();
        }
    }

    public static class ImportMatch {
        public final KeyBinding binding;
        public final BindingEntry entry;
        public boolean selected = true;

        public ImportMatch(KeyBinding binding, BindingEntry entry) {
            this.binding = binding;
            this.entry = entry;
        }

        public List<InputConstants.Key> getKeysFromEntry() {
            return entry.keys().stream().map(code -> {
                InputConstants.Type type = code >= 0 && code <= 7 ? InputConstants.Type.MOUSE : InputConstants.Type.KEYSYM;
                return type.getOrCreate(code);
            }).toList();
        }
    }
}
