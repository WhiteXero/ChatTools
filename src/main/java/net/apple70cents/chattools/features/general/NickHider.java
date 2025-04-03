package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class NickHider {
    private static final Map<String, Component> cache = new LinkedHashMap<>();

    public static Component work(Component message) {
        while (cache.size() > ((Number) ConfigUtils.get("general.NickHider.CacheSize")).intValue()) {
            cache.remove(cache.keySet().iterator().next());
        }
        LocalPlayer player = Minecraft.getInstance().player;
        String nickname = TextUtils.encodeColorCodes((String) ConfigUtils.get("general.NickHider.Nickname"));
        if (player != null) {
            String playerName = player.getName().getString();
            String key = nickname + "|" + playerName + "|" + message.toString();

            if (cache.containsKey(key)) {
                return cache.get(key); // get from cache
            }

            // not cached, let's deal with it!
            if (TextUtils.wash(message.getString()).contains(playerName)) {
                Component result = message.copy();
                try {
                    result = TextUtils.replaceComponentText(message.copy(), Pattern.compile(Pattern.quote(playerName)), nickname);
                } catch (Exception e) {
                    LoggerUtils.error("[ChatTools] Error occurred on nick-hiding this text: " + result + ", let's show it raw...");
                    e.printStackTrace();
                }
                cache.put(key, result); // put it in cache
                return result;
            }
        }
        return message;
    }
}
