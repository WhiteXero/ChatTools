package net.apple70cents.chattools.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

//#if MC>=12005
import net.minecraft.data.registries.VanillaRegistries;
//#endif

/**
 * @author 70CentsApple
 */
public class TextUtils {
    public static final Style WEBSITE_URL_STYLE = Style.EMPTY.withUnderlined(true)
                                                             .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://70centsapple.top/blogs/#/chat-tools-faq"))
                                                             .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ConfigScreenUtils.getTooltip("general.FAQ", "FAQ", null)));
    public static final String PREFIX = "key.chattools.";
    public static final Component SPACER = literal("").copy().setStyle(Style.EMPTY);

    public static class MessageUnit {
        public Component message;
        public long unixTimestamp;
        public int occurrenceCount;

        public MessageUnit(Component message, long unixTimestamp, int occurrenceCount) {
            this.message = message;
            this.unixTimestamp = unixTimestamp;
            this.occurrenceCount = occurrenceCount;
        }
    }

    // For a newly received message, the key is its hashcode and the value is its MessageUnit
    public static Map<String, MessageUnit> messageMap = new LinkedHashMap<>();
    public static MessageUnit latestMessage = null;

    /**
     * Generates a random string conducted by 0-9,a-z
     *
     * @param length the length
     * @return the random string
     */
    public static String generateRandomString(int length) {
        final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";
        final Random RANDOM = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static MessageUnit getLatestMessage() {
        return latestMessage;
    }

    protected static void setLatestMessage(MessageUnit unit) {
        latestMessage = unit;
    }

    public static String putMessageMap(Component text, long unixTimestamp, int occurrenceCount) {
        int maxSize = ((Number) ConfigUtils.get("general.MaxHistoryLength")).intValue();
        while (messageMap.size() > maxSize) {
            // pops the first element
            messageMap.remove(messageMap.keySet().iterator().next());
        }

        // The `hashcode` is NOT REALLY a hashcode, but actually just a random string
        String hashcode = generateRandomString(6);
        int retries = 0;
        while (messageMap.containsKey(hashcode) && retries < 10) {
            hashcode = generateRandomString(6);
            retries++;
        }
        MessageUnit messageUnit = new MessageUnit(text, unixTimestamp, occurrenceCount);
        messageMap.put(hashcode, messageUnit);
        setLatestMessage(messageUnit);
        return hashcode;
    }

    public static MessageUnit getMessageMap(String hash) {
        try {
            return messageMap.get(hash);
        } catch (Exception e) {
            return null;
        }
    }


    public static Component literal(String str) {
        //#if MC>=11900
        return Component.literal(str);
        //#else
        //$$return new TextComponent(str);
        //#endif
    }

    public static Component transWithPrefix(String str, String prefix) {
        //#if MC>=11900
        return Component.translatable(prefix + str);
        //#else
        //$$return new TranslatableComponent(prefix + str);
        //#endif
    }

    public static Component transWithPrefix(String str, String prefix, Object... args) {
        //#if MC>=11900
        return Component.translatable(prefix + str, args);
        //#else
        //$$return new TranslatableComponent(prefix + str, args);
        //#endif
    }

    public static Component trans(String str, Object... args) {
        return transWithPrefix(str, PREFIX, args);
    }

    public static Component trans(String str) {
        return transWithPrefix(str, PREFIX);
    }

    public static Component of(String str) {
        return Component.nullToEmpty(str);
    }

    public static Component empty() {
        //#if MC>=11900
        return Component.empty();
        //#else
        //$$return of("");
        //#endif
    }

    /**
     * removes color codes in the string
     *
     * @param str the string
     * @return string with no color codes
     */
    public static String wash(String str) {
        return Pattern.compile("§.").matcher(str).replaceAll("");
    }

    /**
     * turn '&' into REAL color codes in the string
     * it will not modify '\&'
     * @param str the string
     * @return string with color codes
     */
    public static String encodeColorCodes(String str) {
        return str.replace('&', '§').replace("\\§", "&");
    }

    /**
     * turn REAL color codes into '&' in the string
     * @param str the string
     * @return a string, in which color codes are turned into '&'
     */
    public static String decodeColorCodes(String str) {
        return str.replace('§', '&');
    }

    public static Component textArray2text(List<Component> texts) {
        MutableComponent result = (MutableComponent) literal("");
        for (int i = 0; i < texts.size(); i++) {
            result.append(texts.get(i));
            if (i != texts.size() - 1) {
                result.append(literal("\n"));
            }
        }
        return result;
    }

    /**
     * replace a {@link MutableComponent}
     *
     * @param text      the text
     * @param oldString old string
     * @param newString new string
     * @return text after replacement
     */
    public static MutableComponent replaceComponent(MutableComponent text, String oldString, String newString) {
        //#if MC>=12005
        JsonElement jsonElement = new Component.SerializerAdapter(VanillaRegistries.createLookup()).serialize(text, null, null);
        //#else
        //$$ JsonElement jsonElement = Component.Serializer.toJsonTree(text);
        //#endif
        replaceFieldValue(jsonElement, oldString, newString);
        //#if MC>=12005
        return new Component.SerializerAdapter(VanillaRegistries.createLookup()).deserialize(jsonElement, null, null);
        //#else
        //$$ return Component.Serializer.fromJson(jsonElement);
        //#endif
    }

    private static void replaceFieldValue(JsonElement jsonElement, String oldValue, String newValue) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> ele : jsonObject.entrySet()) {
                String key = ele.getKey();
                JsonElement value = ele.getValue();

                if (isProtectedField(key)) {
                    continue;
                }

                if (isTextField(key) && value.isJsonPrimitive() && value.getAsString().contains(oldValue)) {
                    jsonObject.addProperty(key, value.getAsString().replace(oldValue, newValue));
                } else {
                    replaceFieldValue(value, oldValue, newValue);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                replaceFieldValue(element, oldValue, newValue);
            }
        }
    }

    private static boolean isProtectedField(String key) {
        return key.contains("enchantments") || key.contains("tag");
    }

    private static boolean isTextField(String key) {
        return key.contains("text") || key.contains("value") || key.contains("translate");
    }
}
