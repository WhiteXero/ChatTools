package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatCompactor {
    public static Text appendTrailing(Text msg, int occurrencesCount) {
        if (occurrencesCount <= 1) {
            return msg;
        }
        int maxOccurrence = ((Number) ChatTools.CONFIG.get("general.ChatCompactor.MaxOccurrence")).intValue();
        String tail = "";
        if (occurrencesCount > maxOccurrence) {
            tail = " (" + maxOccurrence + "+)";
        } else {
            tail = " (" + occurrencesCount + ")";
        }
        return msg.copy().append(TextUtils.literal(tail).copy().formatted(Formatting.GRAY));
    }

    public static int calculateOccurrenceCount(Text message) {
        int previousOccurrenceCount = 0;
        if ((boolean) ChatTools.CONFIG.get("general.ChatCompactor.Enabled") && message != null) {
            TextUtils.MessageUnit latestMessageUnit = TextUtils.getLatestMessage();
            if (latestMessageUnit == null) {
                return 1;
            }
            if ((boolean) ChatTools.CONFIG.get("general.ChatCompactor.UseStrict")) {
                if (message.equals(latestMessageUnit.message)) {
                    previousOccurrenceCount = latestMessageUnit.occurrenceCount;
                }
            } else {
                if (TextUtils.wash(message.getString())
                             .equals(TextUtils.wash(latestMessageUnit.message.getString()))) {
                    previousOccurrenceCount = latestMessageUnit.occurrenceCount;
                }
            }
        }
        return previousOccurrenceCount + 1;
    }
}
