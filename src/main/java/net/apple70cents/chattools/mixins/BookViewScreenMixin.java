package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ClickEventsPreviewer;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {
    @Inject(method = "getClickedComponentStyleAt", at = @At(value = "RETURN"), cancellable = true)
    public void modifyHoverEvent(double x, double y, CallbackInfoReturnable<Style> cir) {
        Style style = cir.getReturnValue();
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            cir.setReturnValue(style);
            return;
        }
        if (!(boolean) ConfigUtils.get("general.PreviewClickEvents.Enabled")) {
            cir.setReturnValue(style);
            return;
        }
        cir.setReturnValue(ClickEventsPreviewer.work(style));
    }
}
