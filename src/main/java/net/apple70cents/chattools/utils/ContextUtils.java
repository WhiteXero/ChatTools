package net.apple70cents.chattools.utils;

import net.minecraft.client.Minecraft;
//#if MC>=11903
import net.minecraft.core.registries.Registries;
//#endif

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    protected static final Map<String, Supplier<String>> MAPPINGS = new ConcurrentHashMap<>();

    static {
        MAPPINGS.put("x", () -> String.format("%.1f", Minecraft.getInstance().player.getX()));
        MAPPINGS.put("y", () -> String.format("%.1f", Minecraft.getInstance().player.getY()));
        MAPPINGS.put("z", () -> String.format("%.1f", Minecraft.getInstance().player.getZ()));
        MAPPINGS.put("pos", () -> String.format("(%.1f, %.1f, %.1f)", Minecraft.getInstance().player.getX(),
                Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ()));
        MAPPINGS.put("dimension_reg_name", () -> Minecraft.getInstance().level.dimension().location().toString());
        MAPPINGS.put("biome_reg_name",
                () -> String.valueOf(Minecraft.getInstance().level.registryAccess().lookupOrThrow(
                        //#if MC>=11903
                        Registries.BIOME
                        //#else
                        //$$ net.minecraft.core.Registry.BIOME_REGISTRY
                        //#endif
                ).getKey(Minecraft.getInstance().level.getBiome(Minecraft.getInstance().player.blockPosition())
                        //#if MC>=11800
                        .value()
                        //#endif
                )));
        MAPPINGS.put("biome",
                () -> TextUtils.transWithPrefix(MAPPINGS.get("biome_reg_name").get().replace(":", "."), "biome.")
                        .getString());
        MAPPINGS.put("world_time", () -> String.valueOf(Minecraft.getInstance().level.getDayTime() % 24000));
        MAPPINGS.put("game_time", () -> String.valueOf(Minecraft.getInstance().level.getGameTime()));
        MAPPINGS.put("real_time_long", () -> {
            java.time.Instant instant = java.time.Instant.now();
            java.time.ZonedDateTime currentTime = java.time.ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
            return String.format("%4d/%d/%d %02d:%02d:%02d UTC%s", currentTime.getYear(),
                    currentTime.getMonth().getValue(), currentTime.getDayOfMonth(), currentTime.getHour(),
                    currentTime.getMinute(), currentTime.getSecond(), offsetString);
        });
        MAPPINGS.put("real_time_short", () -> {
            java.time.Instant instant = java.time.Instant.now();
            java.time.ZonedDateTime currentTime = java.time.ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            return String.format("%02d:%02d:%02d", currentTime.getHour(), currentTime.getMinute(),
                    currentTime.getSecond());
        });
        MAPPINGS.put("nickname", () -> Minecraft.getInstance().player.getGameProfile()
                //#if MC>=12109
                .name()
                //#else
                //$$ .getName()
                //#endif
        );
        MAPPINGS.put("uuid", () -> Minecraft.getInstance().player.getGameProfile()
                //#if MC>=12109
                .id()
                //#else
                //$$ .getId()
                //#endif
                .toString());
        MAPPINGS.put("session_identifier", ContextUtils::getSessionIdentifier);
        MAPPINGS.put("health", () -> String.format("%.1f", Minecraft.getInstance().player.getHealth()));
        MAPPINGS.put("player_count", () -> String.valueOf(Minecraft.getInstance().level.getServer().getPlayerCount()));
        MAPPINGS.put("random", () -> String.format("%.4f", Math.random()));
        MAPPINGS.put("flip", () -> Math.random() < 0.5 ? "HEAD" : "TAIL");
        MAPPINGS.put("item_in_hand", () -> Minecraft.getInstance().player.getMainHandItem().getHoverName().getString());
        MAPPINGS.put("item_in_hand_reg_name", () -> Minecraft.getInstance().level.registryAccess().lookupOrThrow(
                //#if MC>=11903
                Registries.ITEM
                //#else
                //$$ net.minecraft.core.Registry.ITEM_REGISTRY
                //#endif
        ).getKey(Minecraft.getInstance().player.getMainHandItem().getItem()).toString());
    }

    public static String replacePlaceholders(String text) {
        for (Map.Entry<String, Supplier<String>> entry : MAPPINGS.entrySet()) {
            String key = entry.getKey();
            Pattern pattern = Pattern.compile("\\{" + Pattern.quote(key) + "\\}");
            Matcher matcher = pattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String result = "UNKNOWN";
                try {
                    result = entry.getValue().get();
                } catch (Exception ignored) {
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result));
            }
            matcher.appendTail(sb);
            text = sb.toString();
        }
        return text;
    }
}
