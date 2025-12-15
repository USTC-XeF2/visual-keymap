package dev.xef2.visualkeymap.mixin;

import dev.xef2.visualkeymap.VisualKeymap;
import dev.xef2.visualkeymap.gui.screen.VisualKeymapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ControlsOptionsScreen.class)
public class ControlsOptionsScreenMixin {

    @Redirect(
            method = "addOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/OptionListWidget;addAll([Lnet/minecraft/client/option/SimpleOption;)V"
            )
    )
    private void addCustomOptionAndRedirectAddAll(
            OptionListWidget instance,
            SimpleOption<?>[] originalOptions
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        ButtonWidget viewVisualButton = ButtonWidget.builder(
                VisualKeymap.getTranslationText("gui.open_keymap"),
                (button) -> client.setScreen(new VisualKeymapScreen(
                        (ControlsOptionsScreen) (Object) this,
                        client.options
                ))
        ).build();

        List<ClickableWidget> widgets = new ArrayList<>(List.of(viewVisualButton));

        for (SimpleOption<?> option : originalOptions) {
            widgets.add(option.createWidget(client.options));
        }

        instance.addAll(widgets);
    }
}
