package net.apple70cents.chattools.mixins;

//#if MC>=12111

import net.apple70cents.chattools.features.general.ClickEventsPreviewer;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    Style hoveredTextStyle;
    @Shadow
    Style clickableTextStyle;

    @Inject(method = "renderDeferredElements", at = @At("HEAD"))
    public void modifyHoverEvent(CallbackInfo ci) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.PreviewClickEvents.Enabled")) {
            return;
        }
        Style style = clickableTextStyle != null ? clickableTextStyle : hoveredTextStyle;
        hoveredTextStyle = ClickEventsPreviewer.work(style);
    }
}
//#else
//$$ // no-op
//$$ @org.spongepowered.asm.mixin.Mixin(net.minecraft.client.Minecraft.class)
//$$ public class GuiGraphicsMixin {}
//#endif
