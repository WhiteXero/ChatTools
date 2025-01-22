package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 70CentsApple
 */
public class MessageUtils {

    private static boolean justSentMessage = false;

    public static boolean hadJustSentMessage() {
        return justSentMessage;
    }

    public static void setJustSentMessage(boolean bl) {
        justSentMessage = bl;
    }

    public static void sendToActionbar(Text text) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }
        if (!(boolean) ChatTools.CONFIG.get("general.ExclusiveActionbar.Enabled")) {
            MinecraftClient.getInstance().player.sendMessage(text, true);
        } else {
            ExclusiveActionbarHandler.addToRenderQueue(text, 4000);
        }
    }

    public static void sendToNonPublicChat(Text text) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
    }

    public static void sendToPublicChat(String text, boolean forceDisableFormatter) {
        boolean oldStatus = (boolean) ChatTools.CONFIG.get("formatter.Enabled");
        if (forceDisableFormatter) {
            ChatTools.CONFIG.set("formatter.Enabled", false);
        }
        sendToPublicChat(text);
        ChatTools.CONFIG.set("formatter.Enabled", oldStatus);
    }

    public static void sendToPublicChat(String text) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        setJustSentMessage(true);

        //#if MC>=11900
        String text2 = StringUtils.normalizeSpace(text.trim());
        if (!text2.isEmpty()) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(text);
            if (text2.startsWith("/")) {
                player.networkHandler.sendChatCommand(text2.substring(1));
            } else {
                player.networkHandler.sendChatMessage(text2);
            }
        }
        //#else
        //$$ player.sendChatMessage(text);
        //#endif
    }

    /**
     * finds the most front player nickname (if there is) in the given string
     *
     * @param str the string
     * @return null or the player name
     */
    public static String findTheFirstPlayerName(String str) {
        if (MinecraftClient.getInstance().world == null) {
            return null;
        }
        int minIndex = str.length();
        String firstPlayerName = null;
        for (AbstractClientPlayerEntity player : MinecraftClient.getInstance().world.getPlayers()) {
            if (player.getDisplayName() == null) {
                continue;
            }
            String playerName = player.getDisplayName().getString();

            if (str.contains(playerName) && str.indexOf(playerName) < minIndex) {
                minIndex = str.indexOf(playerName);
                firstPlayerName = playerName;
            }
        }
        return firstPlayerName;
    }
}
