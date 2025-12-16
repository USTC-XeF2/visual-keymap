package dev.xef2.visualkeymap.mixin;

import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeaderAndFooterLayout.class)
public interface HeaderAndFooterLayoutAccessor {

    @Accessor("contentsFrame")
    FrameLayout getContentsFrame();
}
