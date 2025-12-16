package dev.xef2.visualkeymap.mixin;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ControlsScreen.class)
public class ControlsScreenMixin {

    @Redirect(
            method = "addOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"
            )
    )
    private void addKeymapOptionAndRedirect(
            OptionsList instance,
            OptionInstance<?>[] originalOptions
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        Button viewVisualButton = Button.builder(
                VisualKeymap.getTranslatedComponent("gui.open_keymap"),
                (button) -> minecraft.setScreen(new VisualKeymapScreen(
                        (ControlsScreen) (Object) this,
                        minecraft.options
                ))
        ).build();

        List<AbstractWidget> widgets = new ArrayList<>(List.of(viewVisualButton));

        for (OptionInstance<?> option : originalOptions) {
            widgets.add(option.createButton(minecraft.options));
        }

        instance.addSmall(widgets);
    }
}
