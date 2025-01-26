package net.apple70cents.chattools.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.filter.ChatFilter;
import net.apple70cents.chattools.features.general.ChatCompactor;
import net.apple70cents.chattools.features.general.ClickEventsPreviewer;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.features.general.Timestamp;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.apple70cents.chattools.features.responder.Responder;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.time.Instant;
import java.util.List;

//#if MC>=11900
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
//#endif

/**
 * @author 70CentsApple
 */
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow
    public abstract void reset();


    @Shadow @Final private List<ChatHudLine> messages;

    @ModifyExpressionValue(method =
            //#if MC>=12005
            {"addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", "addMessage*", "addToMessageHistory"}
            //#elseif MC>=11900
            //$$ "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"
            //#else
            //$$ "addMessage(Lnet/minecraft/text/Text;IIZ)V"
            //#endif
            , at = @At(value = "CONSTANT", args = "intValue=100"))
    public int modifyMaxHistorySize(int originalMaxSize) {
        if ((boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return ((Number) ConfigUtils.get("general.MaxHistoryLength")).intValue();
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
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (ChatFilter.shouldFilter(message)) {
            LoggerUtils.info("[ChatTools] Filtered message: " + message.getString());
            if ((boolean) ConfigUtils.get("responder.RespondToFilteredMessages")) {
                Responder.work(message);
            }
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
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        Text message = args.get(MESSAGE_IDX);
        // ignores this message if Chat Filter is to work
        if (ChatFilter.shouldFilter(message)) {
            return;
        }
        if ((boolean) ConfigUtils.get("bubble.Enabled")) {
            // it must be done before NickHider began to work
            BubbleRenderer.addChatBubble(message);
        }
        // This is not the only attempt that we try to activate the responder.
        // When filtering a message with `responder.respondToFilteredMessages` option on, responder will also try to work.
        if ((boolean) ConfigUtils.get("responder.Enabled") && !MessageUtils.hadJustSentMessage()) {
            // obviously, we don't respond to our own messages
            Responder.work(message);
        }
        if ((boolean) ConfigUtils.get("general.NickHider.Enabled")) {
            message = NickHider.work(message);
        }
        int occurrenceCount = 1;
        if ((boolean) ConfigUtils.get("general.ChatCompactor.Enabled")) {
            occurrenceCount = ChatCompactor.calculateOccurrenceCount(message);
            if (occurrenceCount > 1 && !this.messages.isEmpty()) {
                this.messages.remove(0);
                this.reset();
            }
        }
        String hashcode = TextUtils.putMessageMap(message, Instant.now().getEpochSecond(), occurrenceCount);

        if ((boolean) ConfigUtils.get("notifier.Highlight.InsertBeforeTimestamps")) {
            if ((boolean) ConfigUtils.get("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
            if (BasicNotifier.shouldWork(message)) {
                message = BasicNotifier.work(message);
            }
        } else {
            if (BasicNotifier.shouldWork(message)) {
                message = BasicNotifier.work(message);
            }
            if ((boolean) ConfigUtils.get("general.Timestamp.Enabled")) {
                message = Timestamp.work(message, hashcode);
            }
        }

        if ((boolean) ConfigUtils.get("general.ChatCompactor.Enabled")) {
            message = ChatCompactor.appendTrailing(message, occurrenceCount);
        }
        // we need to reset `justSentMessage` status, since it might be that this message received was sent by us
        MessageUtils.setJustSentMessage(false);
        args.set(MESSAGE_IDX, message);
    }

    @Inject(method = "getTextStyleAt", at = @At(value = "RETURN"), cancellable = true)
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
