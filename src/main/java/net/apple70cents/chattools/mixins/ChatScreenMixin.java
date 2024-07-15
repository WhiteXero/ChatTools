package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.general.ChatHistoryNavigator;
import net.apple70cents.chattools.features.general.Translator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("TAIL"))
    private void increaseChatFieldMaxLength(CallbackInfo ci) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ChatTools.CONFIG.get("general.IncreaseChatFieldMaxLength")) {
            return;
        }
        chatField.setMaxLength(Integer.MAX_VALUE);
    }

    //#if MC>=11900
    @Inject(method = "normalize", at = @At("HEAD"), cancellable = true)
    private void doNotNormalize(String text, CallbackInfoReturnable<String> cir) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ChatTools.CONFIG.get("general.IncreaseChatFieldMaxLength")) {
            return;
        }
        cir.setReturnValue(StringUtils.normalizeSpace(text.trim()));
    }
    //#endif

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled")) {
            return;
        }
        if (Translator.shouldWork()) {
            Translator.work(chatField);
        }
        if (ChatHistoryNavigator.shouldWork()) {
            ChatHistoryNavigator.popupNavigatorScreen();
        }
    }

}
