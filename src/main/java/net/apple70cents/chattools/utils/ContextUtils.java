package net.apple70cents.chattools.utils;

import net.minecraft.client.Minecraft;

public class ContextUtils {
    public static String getSessionIdentifier() {
        try {
            return Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName();
        } catch (Exception e1) {
            try {
                return Minecraft.getInstance().getCurrentServer().ip;
            } catch (Exception e2) {
                return "-";
            }
        }
    }
}
