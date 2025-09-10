package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;


public class Translator {
    public static boolean shouldWork() {
        if (!(boolean) ConfigUtils.get("translator.Translator.Enabled")) {
            return false;
        }
        if (!(Minecraft.getInstance().screen instanceof ChatScreen)) {
            return false;
        }
        return KeyboardUtils.isKeyPressingWithModifier("key.keyboard.tab", SpecialUnits.KeyModifiers.SHIFT, SpecialUnits.MacroModes.LAZY);
    }

    public static void work(EditBox chat) {
        if (chat.getValue().isBlank()) {
            return;
        }
        switch ((String) ConfigUtils.get("translator.Translator.Mode")) {
            case "BUILTIN":
                String api = (String) ConfigUtils.get("translator.Translator.Builtin.API");
                boolean usePost = (boolean) ConfigUtils.get("translator.Translator.Builtin.PostInstead");
                new BuiltinTranslator(chat, api, usePost).work();
                break;
            case "BAIDU":
                String baiduAppId = (String) ConfigUtils.get("translator.Translator.Baidu.Appid");
                String baiduKey = (String) ConfigUtils.get("translator.Translator.Baidu.Appkey");
                String baiduFrom = (String) ConfigUtils.get("translator.Translator.Baidu.from");
                String baiduTo = (String) ConfigUtils.get("translator.Translator.Baidu.to");
                new BaiduTranslator(chat, baiduAppId, baiduKey, baiduFrom, baiduTo).work();
                break;
            case "MICROSOFT_FREE":
                String microsoftFreeFrom = (String) ConfigUtils.get("translator.Translator.MicrosoftFree.from");
                String microsoftFreeTo = (String) ConfigUtils.get("translator.Translator.MicrosoftFree.to");
                new MicrosoftFreeTranslator(chat, microsoftFreeFrom, microsoftFreeTo).work();
                break;
            case "GOOGLE_FREE":
                String googleFreeSl = (String) ConfigUtils.get("translator.Translator.GoogleFree.sl");
                String googleFreeTl = (String) ConfigUtils.get("translator.Translator.GoogleFree.tl");
                new GoogleFreeTranslator(chat, googleFreeSl, googleFreeTl).work();
                break;
            default:
                return;
        }
    }
}
