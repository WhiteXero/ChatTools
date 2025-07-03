package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.ConfigStorage;

public class ConfigUtils {
    public final static ConfigStorage DEFAULT_CONFIG = new ConfigStorage(true);
    public static ConfigStorage CONFIG;

    public static void init(){
        if (!ConfigStorage.configFileExists()) {
            // if the config file doesn't exist, create a new one with the default settings.
            DEFAULT_CONFIG.save();
        }

        CONFIG = new ConfigStorage(false).withDefault(DEFAULT_CONFIG.getHashmap());
    }

    public static void save() {
        CONFIG.save();
    }

    public static Object get(String var){
        return CONFIG.get(var);
    }

    public static Object getDefault(String var){
        return DEFAULT_CONFIG.get(var);
    }

    public static void set(String key, Object value) {
        CONFIG.set(key, value);
    }
}
