package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin {
    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    public void disableTextObfuscation(CallbackInfoReturnable<Boolean> cir) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.DisableTextObfuscation.Enabled")) {
            return;
        }
        cir.setReturnValue(false);
    }
}
