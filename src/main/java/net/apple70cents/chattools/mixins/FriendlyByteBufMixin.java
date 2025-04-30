package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {
    @ModifyVariable(method = "readUtf(I)Ljava/lang/String;", at = @At(value = "HEAD"), argsOnly = true)
    public int fixChatPacket(int i) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return i;
        }
        if (!(boolean) ConfigUtils.get("general.IncreaseChatFieldMaxLength")) {
            return i;
        }
        return Short.MAX_VALUE;
    }

    @ModifyVariable(method = "writeUtf(Ljava/lang/String;I)Lnet/minecraft/network/FriendlyByteBuf;", at = @At(value = "HEAD"), argsOnly = true)
    public int fixChatPacket2(int i) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return i;
        }
        if (!(boolean) ConfigUtils.get("general.IncreaseChatFieldMaxLength")) {
            return i;
        }
        return Short.MAX_VALUE;
    }
}
