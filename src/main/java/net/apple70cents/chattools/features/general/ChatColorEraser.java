package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatColorEraser {
    private static final Map<String, Component> cache = new LinkedHashMap<>();

    public static Component work(Component message) {
        String key = message.toString();

        if (cache.containsKey(key)) {
            return cache.get(key); // get from cache
        }

        Component result = message.copy();
        try {
            result = TextUtils.replaceComponentColor(message.copy());
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Error occurred on erasing the color of this text: " + result + ", let's show it raw...");
            e.printStackTrace();
            return message;
        }
        cache.put(key, result);
        return result;
    }
}
