package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.general.ClickEventsPreviewer;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookScreen.class)
public abstract class BookScreenMixin {
    @Inject(method = "getTextStyleAt", at = @At(value = "RETURN"), cancellable = true)
    public void modifyHoverEvent(double x, double y, CallbackInfoReturnable<Style> cir) {
        Style style = cir.getReturnValue();
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            cir.setReturnValue(style);
            return;
        }
        if (!(boolean) ChatTools.CONFIG.get("general.PreviewClickEvents.Enabled")) {
            cir.setReturnValue(style);
            return;
        }
        cir.setReturnValue(ClickEventsPreviewer.work(style));
    }
}
