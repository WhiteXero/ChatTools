package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.KeyboardUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Translator {
    public static boolean shouldWork() {
        if (!(boolean) ConfigUtils.get("general.Translator.Enabled")) {
            return false;
        }
        if (!(Minecraft.getInstance().screen instanceof ChatScreen)) {
            return false;
        }
        return KeyboardUtils.isKeyPressingWithModifier("key.keyboard.tab", SpecialUnits.KeyModifiers.SHIFT, SpecialUnits.MacroModes.LAZY);
    }

    public static void work(EditBox chatField) {
        String originalText = chatField.getValue();
        String method = (boolean) ConfigUtils.get("general.Translator.PostInstead") ? "POST" : "GET";
        if (((String) ConfigUtils.get("general.Translator.API")).isBlank()) {
            chatField.setValue(TextUtils.trans("texts.translator.requireApi").getString());
            return;
        }
        chatField.setValue(TextUtils.trans("texts.translator.await").getString());
        Runnable runnable = () -> {
            try {
                String api = (String) ConfigUtils.get("general.Translator.API");
                if (api.contains("{text}")) {
                    api = api.replace("{text}", URLEncoder.encode(originalText, StandardCharsets.UTF_8));
                } else {
                    api += URLEncoder.encode(originalText, StandardCharsets.UTF_8);
                }
                URL formattedUrl = new URL(api);
                HttpURLConnection connection = (HttpURLConnection) formattedUrl.openConnection();
                connection.setRequestMethod(method);
                LoggerUtils.info("[ChatTools] Visiting \"" + api + "\" with method: " + method);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Failed with HTTP Error Code " + responseCode);
                } else {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        chatField.setValue(response.toString());
                        LoggerUtils.info("[ChatTools] Response: " + response);
                        return;
                    }
                }
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Error occurred when visiting Translation API");
                chatField.setValue(e.toString());
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable, "ChatTools-Translation-Thread");
        thread.start();
    }
}
