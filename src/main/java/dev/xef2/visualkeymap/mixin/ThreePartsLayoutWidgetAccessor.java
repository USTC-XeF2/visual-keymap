package dev.xef2.visualkeymap.mixin;

import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThreePartsLayoutWidget.class)
public interface ThreePartsLayoutWidgetAccessor {

    @Accessor("body")
    SimplePositioningWidget getBody();
}
