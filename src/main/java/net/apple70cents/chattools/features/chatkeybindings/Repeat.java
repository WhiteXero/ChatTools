package net.apple70cents.chattools.features.chatkeybindings;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class Repeat {
    private static boolean keyWasPressed;

    public static void tick() {
        String key = (String) ConfigUtils.get("chatkeybindings.RepeatKey");
        SpecialUnits.KeyModifiers modifier = SpecialUnits.KeyModifiers.valueOf((String) ConfigUtils.get("chatkeybindings.RepeatKeyModifier"));
        if (KeyboardUtils.isKeyPressingWithModifier(key, modifier, SpecialUnits.MacroModes.GREEDY) && MinecraftClient.getInstance().currentScreen == null) {
            if (!keyWasPressed) {
                keyWasPressed = true;
                LoggerUtils.info("[ChatTools] Triggered the latest command.");
                MinecraftClient mc = MinecraftClient.getInstance();
                List<String> history = mc.inGameHud.getChatHud().getMessageHistory();
                if (history.isEmpty()) {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.repeat.failure.empty"));
                } else {
                    MessageUtils.sendToPublicChat(history.get(history.size() - 1));
                }
            }
        } else {
            keyWasPressed = false;
        }
    }
}
