package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.*;
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
    private static Component text;
    private static String playerName;
    private static String nickname;

    private static final CircuitBreakerExecutor executor = CircuitBreakerExecutor.of(() -> {
        try {
            text = TextUtils.replaceComponentText(text.copy(), Pattern.compile(Pattern.quote(playerName)), nickname);
        } catch (Exception e) {
            LoggerUtils.error("[ChatTools] Error occurred on nick-hiding this text: " + text + ", let's show it raw...");
            e.printStackTrace();
        }
    }).setMaxLimitPerSecond(() -> ((Number) ConfigUtils.get("general.CircuitBreaker.NickHiderThreshold")).intValue())
    .setFailsafeFunction(() -> {
        int threshold = ((Number) ConfigUtils.get("general.CircuitBreaker.NickHiderThreshold")).intValue();
        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.CircuitBreaker.exceed.NickHider", threshold));
        MessageUtils.sendToActionbar(TextUtils.trans("texts.CircuitBreaker.exceed.NickHider", threshold));
        LoggerUtils.warn(TextUtils.trans("texts.CircuitBreaker.exceed.NickHider", threshold).getString());
        ConfigUtils.set("general.NickHider.Enabled", false);
    }).setFailsafeJudgement(() -> (Boolean) ConfigUtils.get("general.NickHider.Enabled"));

    public static Component work(Component message) {
        while (cache.size() > ((Number) ConfigUtils.get("general.NickHider.CacheSize")).intValue()) {
            cache.remove(cache.keySet().iterator().next());
        }
        LocalPlayer player = Minecraft.getInstance().player;
        nickname = TextUtils.encodeColorCodes((String) ConfigUtils.get("general.NickHider.Nickname"));
        if (player != null) {
            playerName = player.getName().getString();
            String key = nickname + "|" + playerName + "|" + message.toString();

            if (cache.containsKey(key)) {
                return cache.get(key); // get from cache
            }

            // not cached, let's deal with it!
            if (TextUtils.wash(message.getString()).contains(playerName)) {
                text = message.copy();
                executor.run();
                cache.put(key, text); // put it in cache
                return text;
            }
        }
        return message;
    }
}
