package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ChatHistoryNavigator;
import net.apple70cents.chattools.features.general.Translator;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=12005
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import java.util.List;
//#endif

/**
 * @author 70CentsApple
 */
@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("TAIL"))
    private void increaseChatFieldMaxLength(CallbackInfo ci) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.IncreaseChatFieldMaxLength")) {
            return;
        }
        chatField.setMaxLength(Integer.MAX_VALUE);
    }

    //#if MC>=11900
    @Inject(method = "normalize", at = @At("HEAD"), cancellable = true)
    private void doNotTruncate(String text, CallbackInfoReturnable<String> cir) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (!(boolean) ConfigUtils.get("general.IncreaseChatFieldMaxLength")) {
            return;
        }
        cir.setReturnValue(StringUtils.normalizeSpace(text.trim()));
    }
    //#endif

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
            return;
        }
        if (Translator.shouldWork()) {
            Translator.work(chatField);
        }
        if (ChatHistoryNavigator.shouldWork()) {
            ChatHistoryNavigator.popupNavigatorScreen();
        }
    }


    //#if MC>=12005
    @Unique
    private boolean shouldHideChatHistory() {
        return MinecraftClient.getInstance().options.hudHidden &&
                (boolean) ConfigUtils.get("general.ChatTools.Enabled") &&
                (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) &&
                (boolean) ConfigUtils.get("general.HideChatHistoryInF1Mode");
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V"))
    private boolean hideChatHistoryInF1Mode_1(ChatHud instance, DrawContext context, int i1, int i2, int i3, boolean b) {
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawHoverEvent(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Style;II)V"))
    private boolean hideChatHistoryInF1Mode_2(DrawContext instance, TextRenderer textRenderer, Style style, int x, int y) {
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawOrderedTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V"))
    private boolean hideChatHistoryInF1Mode_3(DrawContext instance, TextRenderer textRenderer, List<? extends OrderedText> text, int x, int y) {
        // if addition conditions are satisfied, don't make it render
        return !shouldHideChatHistory();
    }

    @Inject(method = "getTextStyleAt", at = @At(value = "HEAD"), cancellable = true)
    private void hideChatHistoryInF1Mode_4(double x, double y, CallbackInfoReturnable<Style> cir) {
        // if addition conditions are satisfied, don't consume its click
        if (shouldHideChatHistory()) {
            cir.setReturnValue(null);
        }
    }
    //#endif
}
