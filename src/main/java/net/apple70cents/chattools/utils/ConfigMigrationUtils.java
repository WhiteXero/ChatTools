package net.apple70cents.chattools.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.apple70cents.chattools.config.ConfigStorage;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class ConfigMigrationUtils {
    public static final double CURRENT_VERSION = ((Number) ConfigUtils.getDefault("config.version")).doubleValue();
    public static double CONFIG_VERSION = -1.0;

    public static void checkAndMigrate() {
        File configFile = ConfigStorage.FILE;
        if (isConfigOutOfDate(configFile)) {
            LoggerUtils.warn("[ChatTools] The config in version: " + CONFIG_VERSION + " is out of date! Trying to migrate it.");
            migrate(configFile);
        }
    }

    public static void readConfigFileVersion(File configFile) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
            JsonObject json = new Gson().fromJson(content, JsonObject.class);
            if (json.has("config.version")) {
                CONFIG_VERSION = json.get("config.version").getAsDouble();
            } else {
                // if there is no `config.version` key, then we assume it is v1.0.
                CONFIG_VERSION = 1.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isConfigOutOfDate(File configFile) {
        readConfigFileVersion(configFile);
        return CONFIG_VERSION < CURRENT_VERSION;
    }

    public static void migrate(File configFile) {
        if (CONFIG_VERSION < 0) {
            LoggerUtils.error("[ChatTools] Error: Unknown config version: " + CONFIG_VERSION);
            return;
        }
        if (CONFIG_VERSION < 2.0) {
            migrateFromV1ToV2(configFile, MinecraftClient.getInstance().getClass().getClassLoader()
                                                         .getResourceAsStream("assets/chattools/migration_v1_to_v2.json"));
        }
        // Refresh
        readConfigFileVersion(configFile);
        if (CONFIG_VERSION < 2.2) {
            // In v2.2.0, we corrected the misspelling word 'responder' from 'responser'
            migrateFromV2ToV2_2(configFile);
        }
    }

    private static void migrateFromV1ToV2(File file, InputStream migrationMappingsInputStream) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            // reads and analyse mappings between v1 and v2
            Reader reader = new InputStreamReader(migrationMappingsInputStream);
            JsonObject keyMappings = new Gson().fromJson(reader, JsonObject.class);

            for (Map.Entry<String, JsonElement> ele : keyMappings.entrySet()) {
                String oldKey = ele.getKey();
                String newKey = ele.getValue().getAsString();
                content = content.replace("\"" + oldKey + "\"", "\"" + newKey + "\"");
            }

            // this flattens the nested settings
            String regex = "(?<clear1>\\\"(nickHiderSettings|soundSettings|actionbarSettings|highlightSettings|toastNotifySettings)\\\"\\s*:\\s*\\{)(?<keep>[^{}]*?)(?<clear2>\\})";
            Matcher matcher = Pattern.compile(regex).matcher(content);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String keepContent = matcher.group("keep");
                matcher.appendReplacement(result, Matcher.quoteReplacement(keepContent));
            }
            matcher.appendTail(result);

            JsonObject json = new Gson().fromJson(result.toString(), JsonObject.class);
            json.addProperty("config.version", (Number) 2.0);
            Files.write(file.toPath(), json.toString().getBytes());
            LoggerUtils.info("[ChatTools] Migrated it from v1 to v2!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void migrateFromV2ToV2_2(File file) {
        // In v2.2.0, we corrected the misspelling word 'responder' from 'responser'
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            content = content.replace("\"responser.Enabled\":", "\"responder.Enabled\":");
            content = content.replace("\"responser.List\":", "\"responder.List\":");

            JsonObject json = new Gson().fromJson(content, JsonObject.class);
            json.addProperty("config.version", (Number) 2.2);

            Gson GSON = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(json, writer);
                LoggerUtils.info("[ChatTools] Migrated it from v2 to v2.2!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}