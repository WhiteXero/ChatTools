package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.customjoinmessage.CustomJoinMessageSender;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author 70CentsApple
 */
@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {
    //#if MC>=11900
    @Shadow
    @Final
    private ServerInfo serverInfo;
    //#endif

    //#if MC>=11800
    @Inject(method = "onSuccess", at = @At(value = "TAIL"))
    //#else
    //$$ @Inject(method = "onLoginSuccess", at = @At(value = "TAIL"))
    //#endif
    public void onServerLoginSuccess(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ChatTools.CONFIG.get("customJoinMessage.Enabled")) {
            return;
        }
        //#if MC>=11900
        if (this.serverInfo != null) {
            CustomJoinMessageSender.work(this.serverInfo.address);
        }
        //#else
        //$$ if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {CustomJoinMessageSender.work(MinecraftClient.getInstance().getCurrentServerEntry().address);}
        //#endif
    }

}
