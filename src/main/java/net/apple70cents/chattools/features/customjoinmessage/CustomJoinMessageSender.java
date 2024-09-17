package net.apple70cents.chattools.features.customjoinmessage;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.regex.Pattern;

public class CustomJoinMessageSender {
    public static void work(String currentAddress) {
        LoggerUtils.info("[ChatTools] Trying to send join message as session identifier: " + currentAddress);
        for (SpecialUnits.CustomJoinMessageRuleUnit unit : SpecialUnits.CustomJoinMessageRuleUnit.fromList((List) ChatTools.CONFIG.get("customJoinMessage.List"))) {
            if ("*".equals(unit.address) || Pattern.compile(unit.address).matcher(currentAddress).matches()) {
                makeMessageSchedule(unit.delayInMilliseconds, unit.message, unit.forceDisableFormatter);
            }
        }
    }

    public static void makeMessageSchedule(long delayInMilliseconds, String message, boolean forceDisableFormatter) {
        LoggerUtils.info("[ChatTools] Will send custom join message '" + message + "' within " + delayInMilliseconds + "ms");
        new Thread(() -> {
            try {
                Thread.sleep(delayInMilliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MinecraftClient.getInstance().execute(() -> {
                MessageUtils.setJustSentMessage(true);
                MessageUtils.sendToPublicChat(message, forceDisableFormatter);
            });
        }).start();
    }
}
