package net.apple70cents.chattools.features.responder;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.features.filter.ChatFilter;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class Responder {
    private static final Minecraft mc = Minecraft.getInstance();
    public static long lastRequestTimestamp = -1L;

    /**
     * Replace the group name in the format `{GROUP}` in `message` with the corresponding grouping in the raw message.
     *
     * @param rawMessageReceived the raw message
     * @param rawPattern         the raw pattern
     * @param message            the message to send
     * @return the message after replacing groups with the corresponding values.
     */
    static String replaceAllGroupNames(String rawMessageReceived, String rawPattern, String message) {
        // This RegEx takes out the name of the group in `{}`s.
        // That is, it can take out `name` in `{name}`; `\}\{` in `{\}\{}`;
        // but it cannot take out `{name\}` or `\{name}`.
        final String groupNamePattern = "(?<!\\\\)\\{(?<group>.*?)(?<!\\\\)}";
        Matcher matcher = Pattern.compile(groupNamePattern).matcher(message);
        Matcher rawMessageMatcher = Pattern.compile(rawPattern).matcher(rawMessageReceived);

        if (rawMessageMatcher.find()) {
            while (matcher.find()) {
                String groupName = matcher.group("group");
                String context = rawMessageMatcher.group(groupName);
                if (context != null && !context.isBlank()) {
                    // the context caught is not blank
                    message = message.replace("{" + groupName + "}", context);
                }
            }
        }
        return message;
    }

    public static boolean shouldWork(Component message) {
        boolean enabled = (boolean) ConfigUtils.get("general.ChatTools.Enabled") && (boolean) ConfigUtils.get("responder.Enabled");
        // obviously, we should not respond to our own messages
        boolean notJustSent = !MessageUtils.hadJustSentMessage();
        boolean filterPassed = ChatFilter.shouldFilter(message) ? (boolean) ConfigUtils.get("responder.RespondToFilteredMessages") : true;
        boolean awaitTimePassed = System.currentTimeMillis() - lastRequestTimestamp >= ((Number) ConfigUtils.get("responder.MinAwaitTimeInMilliseconds")).longValue();
        return enabled && notJustSent && filterPassed && awaitTimePassed;
    }

    public static void work(Component text) {
        String messageReceived = TextUtils.wash(text.getString());
        boolean shouldRespond = false;
        String pattern = "";
        String message = "";
        long delayInMilliseconds = 0;
        boolean forceDisableFormatter = false;
        for (SpecialUnits.ResponderRuleUnit unit : SpecialUnits.ResponderRuleUnit.fromList((List) ConfigUtils.get("responder.List"))) {
            if ("*".equals(unit.address) || Pattern.compile(unit.address).matcher(ContextUtils.getSessionIdentifier()).matches()) {
                if (Pattern.compile(unit.pattern, Pattern.MULTILINE).matcher(messageReceived).find()) {
                    shouldRespond = true;
                    pattern = unit.pattern;
                    message = unit.message;
                    delayInMilliseconds = unit.delayInMilliseconds;
                    forceDisableFormatter = unit.forceDisableFormatter;
                    break;
                }
            }
        }
        if (shouldRespond) {
            makeMessageSchedule(messageReceived, pattern, message, delayInMilliseconds, forceDisableFormatter);
        }
    }

    public static void makeMessageSchedule(String messageReceived, String pattern, String msg, long delayInMilliseconds, boolean forceDisableFormatter) {
        LoggerUtils.info("[ChatTools] Will respond within " + delayInMilliseconds + "ms");
        lastRequestTimestamp = System.currentTimeMillis();
        new Thread(() -> {
            // delay
            try {
                Thread.sleep(delayInMilliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // work
            String message = msg;
            message = ContextUtils.replacePlaceholders(message);
            try {
                message = replaceAllGroupNames(messageReceived, pattern, message);
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Failed when auto responding: " + e.getMessage());
                MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.respond.failure", e.getMessage()));
                MessageUtils.sendToActionbar(TextUtils.trans("texts.respond.failure", e.getMessage()));
                e.printStackTrace();
                return;
            }
            LoggerUtils.info("[ChatTools] Respond to `" + pattern + "`, with message `" + message + "`");
            message = message.replace("\\{", "{").replace("\\}", "}");
            MessageUtils.sendToPublicChat(message, forceDisableFormatter);
            // setting `justSentMessage` to false immediately can fix the issue of the order of dealing with messages
            // however it might lead to notifying the response text sent by the user, even with the option `IgnoreMyMessage` enabled
            // FIXME but it's actually not a big problem, gonna delay fixing this.
            MessageUtils.setJustSentMessage(false);
        }).start();
    }
}
