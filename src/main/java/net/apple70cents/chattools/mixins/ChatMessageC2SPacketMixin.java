package net.apple70cents.chattools.mixins;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.apple70cents.chattools.ChatTools;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(ChatMessageC2SPacket.class)
public abstract class ChatMessageC2SPacketMixin {
    //#if MC>=11900
    @ModifyConstant(method = "write", constant = @Constant(intValue = 256), require = 0)
    private int increaseMaxLength(int endIndex) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return 256;
        }
        if (!(boolean) ChatTools.CONFIG.get("general.IncreaseChatFieldMaxLength")) {
            return 256;
        }
        return Integer.MAX_VALUE;
    }
    //#else
    //$$ @ModifyConstant(method = "<init>(Ljava/lang/String;)V", constant = @Constant(intValue = 256), require = 0)
    //$$ private int increaseMaxLength(int endIndex) {
    //$$     if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
    //$$         return 256;
    //$$     }
    //$$     if (!(boolean) ChatTools.CONFIG.get("general.IncreaseChatFieldMaxLength")) {
    //$$         return 256;
    //$$     }
    //$$     return Integer.MAX_VALUE;
    //$$ }
    //#endif
}
