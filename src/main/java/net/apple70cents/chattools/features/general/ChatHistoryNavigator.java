package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.ChatHistoryNavigatorScreen;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

public class ChatHistoryNavigator {
    public static boolean shouldWork() {
        if (!(boolean) ChatTools.CONFIG.get("general.ChatHistoryNavigator.Enabled")) {
            return false;
        }
        if (!(MinecraftClient.getInstance().currentScreen instanceof ChatScreen)) {
            return false;
        }
        return KeyboardUtils.isKeyPressingWithModifier("key.keyboard.f", SpecialUnits.KeyModifiers.CTRL, SpecialUnits.MacroModes.LAZY);
    }

    public static void popupNavigatorScreen() {
        MinecraftClient.getInstance().setScreen(new ChatHistoryNavigatorScreen(TextUtils.trans("texts.ChatHistoryNavigator.title")));
    }
}
