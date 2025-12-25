package dev.xef2.visualkeymap.api;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public abstract class KeyBinding {

    private final Component category;
    private final Component name;
    private final int maxBoundKeys;

    public KeyBinding(Component category, Component name, int maxBoundKeys) {
        this.category = category;
        this.name = name;
        this.maxBoundKeys = maxBoundKeys;
    }

    public MutableComponent getDisplayName() {
        return this.category.copy().append(" - ").append(this.name);
    }

    @Nullable
    public Component getComment() {
        return null;
    }

    public final Component getTooltip() {
        Component comment = this.getComment();
        if (comment == null) {
            return this.name;
        }
        return Component.empty().append(this.name.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                .append("\n").append(comment);
    }

    public int getMaxBoundKeys() {
        return this.maxBoundKeys;
    }

    abstract public List<Integer> getKeyCodes();

    public List<Integer> getModifierKeyCodes() {
        return List.of();
    }

    public final List<Integer> getFullKeyCodes() {
        List<Integer> fullKeyCodes = new ArrayList<>(this.getModifierKeyCodes());
        fullKeyCodes.addAll(this.getKeyCodes());
        return fullKeyCodes;
    }

    public int getOrder() {
        return 0;
    }

    public static List<List<KeyBinding>> getConflictBindings(List<? extends KeyBinding> bindings) {
        if (bindings.size() <= 1) {
            return List.of();
        }

        Map<ConflictKey, List<KeyBinding>> conflictMap = new HashMap<>();

        for (KeyBinding binding : bindings) {
            ConflictKey key = new ConflictKey(binding.getFullKeyCodes(), binding.getOrder());
            conflictMap.computeIfAbsent(key, k -> new ArrayList<>()).add(binding);
        }

        return conflictMap.values().stream()
                .filter(list -> list.size() > 1)
                .toList();
    }

    private static Component getDisplayNameFromCode(int code) {
        InputConstants.Type inputType = code >= 0 && code <= 7
                ? InputConstants.Type.MOUSE
                : InputConstants.Type.KEYSYM;
        return inputType.getOrCreate(code).getDisplayName();
    }

    public Component getBoundKeysLocalizedText() {
        List<Integer> keyCodes = this.getKeyCodes();
        if (keyCodes.isEmpty()) {
            return Component.translatable("key.keyboard.unknown");
        }

        MutableComponent text = Component.empty();
        List<Integer> modifierKeyCodes = this.getModifierKeyCodes();
        if (!modifierKeyCodes.isEmpty()) {
            MutableComponent modifierText = Component.literal("[ ");
            for (int modifierKeyCode : modifierKeyCodes) {
                modifierText.append(getDisplayNameFromCode(modifierKeyCode));
                modifierText.append(" + ");
            }
            modifierText.append("] ");
            text.append(modifierText.withStyle(ChatFormatting.ITALIC));
        }
        for (int i = 0; i < keyCodes.size(); i++) {
            if (i > 0) {
                text.append(" + ");
            }
            text.append(getDisplayNameFromCode(keyCodes.get(i)));
        }
        return text;
    }

    protected List<String> getSearchableStrings() {
        return Stream.concat(
                Stream.of(this.category, this.name),
                this.getFullKeyCodes().stream().map(KeyBinding::getDisplayNameFromCode)
        ).map(Component::getString).toList();
    }

    public final boolean containsSearchText(String searchText) {
        List<String> searchTerms = Arrays.stream(searchText.toLowerCase().split(" "))
                .filter(s -> !s.isBlank()).toList();
        if (searchTerms.isEmpty()) {
            return false;
        }
        String targetString = String.join(" ", this.getSearchableStrings()).toLowerCase();
        return searchTerms.stream().allMatch(targetString::contains);
    }

    abstract public void setBoundKeys(List<InputConstants.Key> keys);

    abstract public boolean isDefault();

    abstract public void resetToDefault();

    private record ConflictKey(List<Integer> fullKeyCodes, int order) {
    }
}
