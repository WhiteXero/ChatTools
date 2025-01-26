package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 70CentsApple
 */
public class NickHider {
    private static final Map<String, Text> cache = new LinkedHashMap<>();

    public static Text work(Text message) {
        while (cache.size() > ((Number) ConfigUtils.get("general.NickHider.CacheSize")).intValue()) {
            cache.remove(cache.keySet().iterator().next());
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        String nickname = TextUtils.encodeColorCodes((String) ConfigUtils.get("general.NickHider.Nickname"));
        if (player != null) {
            String playerName = player.getName().getString();
            String key = nickname + "|" + playerName + "|" + message.toString();

            if (cache.containsKey(key)) {
                return cache.get(key); // get from cache
            }

            // not cached, let's deal with it!
            if (TextUtils.wash(message.getString()).contains(playerName)) {
                Text result = TextUtils.replaceText(message.copy(), playerName, nickname);
                cache.put(key, result); // put it in cache
                return result;
            }
        }
        return message;
    }
}
