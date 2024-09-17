package net.apple70cents.chattools.utils;

import net.minecraft.client.MinecraftClient;

public class ContextUtils {
    public static String getSessionIdentifier() {
        try {
            return MinecraftClient.getInstance().getServer().getSaveProperties().getLevelName();
        } catch (Exception e1) {
            try {
                return MinecraftClient.getInstance().getCurrentServerEntry().address;
            } catch (Exception e2) {
                return "-";
            }
        }
    }
}
