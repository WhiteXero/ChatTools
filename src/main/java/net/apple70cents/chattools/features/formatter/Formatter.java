package net.apple70cents.chattools.features.formatter;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class Formatter {
    public static String work(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        for (String s : (List<String>) ConfigUtils.get("formatter.DisableOnMatchList")) {
            if (Pattern.compile(s, Pattern.MULTILINE).matcher(message).matches()) {
                // return in advance, and don't work with it.
                return message;
            }
        }
        boolean matched = false;
        String formatter = "{text}";
        for (SpecialUnits.FormatterUnit unit : SpecialUnits.FormatterUnit.fromList((List) ConfigUtils.get("formatter.List"))) {
            if ("*".equals(unit.address) || Pattern.compile(unit.address).matcher(ContextUtils.getSessionIdentifier()).matches()) {
                matched = true;
                formatter = unit.formatter;
                // we just need the first match result, break immediately.
                break;
            }
        }
        if (matched) {
            LoggerUtils.info("[ChatTools] Chat Formatted.");
            message = formatter.replace("{text}", message);
        }
        if (player != null) {
            message = message.replace("{pos}", String.format("(%d,%d,%d)", (int) player.getX(), (int) player.getY(), (int) player.getZ()));
        }
        return message;
    }
}
