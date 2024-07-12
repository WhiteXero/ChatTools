package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.filter.ChatFilter;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.features.general.Timestamp;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.apple70cents.chattools.features.responder.Responder;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
//#if MC>=11900
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
//#endif

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @ModifyConstant(method =
            //#if MC>=12005
            "addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"
            //#elseif MC>=11900
            //$$ "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"
            //#else
            //$$ "addMessage(Lnet/minecraft/text/Text;IIZ)V"
            //#endif
            , constant = @Constant(intValue = 100), require = 0)
    public int modifyMaxHistorySize(int originalMaxSize) {
        if ((boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return ((Number) ChatTools.CONFIG.get("general.MaxHistoryLength")).intValue();
        } else {
            return 100;
        }
    }

    //#if MC>=12005
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "HEAD"), cancellable = true)
    //#elseif MC>=11900
    //$$ @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "HEAD"), cancellable = true)
    //#else
    //$$ @Inject(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "HEAD"), cancellable = true)
    //#endif
    public void onReceivingMessages(
            //#if MC>=12005
            Text message, MessageSignatureData signatureData, MessageIndicator indicator
            //#elseif MC>=11900
            //$$ Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh
            //#else
            //$$ Text message, int messageId, int timestamp, boolean refresh
            //#endif
            , CallbackInfo ci) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        if (ChatFilter.shouldFilter(message)) {
            LoggerUtils.info("[ChatTools] Filtered message: " + message.getString());
            ChatFilter.sendPlaceholderIfActive();
            ci.cancel();
        }
    }

    //#if MC>=12005
    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine;<init>(ILnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"))
    //#elseif MC>=11900
    //$$ @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"))
    //#else
    //$$ @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;IIZ)V"))
    //#endif
    public void onReceivingMessages(Args args) {
        //#if MC>=12005
        final int MESSAGE_IDX = 1;
        //#else
        //$$ final int MESSAGE_IDX = 0;
        //#endif
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        Text message = args.get(MESSAGE_IDX);
        // ignores this message if Chat Filter is to work
        if (ChatFilter.shouldFilter(message)) {
            return;
        }
        if ((boolean) ChatTools.CONFIG.get("bubble.Enabled")) {
            // it must be done before NickHider began to work
            BubbleRenderer.addChatBubble(message);
        }
        if ((boolean) ChatTools.CONFIG.get("responder.Enabled") && !MessageUtils.hadJustSentMessage()) {
            // obviously, we don't respond to our own messages
            Responder.work(message);
        }
        if ((boolean) ChatTools.CONFIG.get("general.NickHider.Enabled")) {
            message = NickHider.work(message);
        }
        if (BasicNotifier.shouldWork(message)) {
            message = BasicNotifier.work(message);
        }
        if ((boolean) ChatTools.CONFIG.get("general.Timestamp.Enabled")) {
            message = Timestamp.work(message);
        }
        // we need to reset `justSentMessage` status, since it might be that this message received was sent by us
        MessageUtils.setJustSentMessage(false);
        args.set(MESSAGE_IDX, message);
    }
}
