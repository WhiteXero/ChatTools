package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.text.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Timestamp {
    public static Text work(Text message) {
        Instant instant = Instant.now();
        long currentUnixTimestamp = instant.getEpochSecond();
        LocalDateTime currentTime = LocalDateTime.ofEpochSecond(currentUnixTimestamp, 0, ZoneId.systemDefault()
                                                                                               .getRules()
                                                                                               .getOffset(instant));
        // get zone offset
        String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
        Text shortTimeDisplay = TextUtils.of(timeInFormat((String) ChatTools.CONFIG.get("general.Timestamp.Pattern")));
        // yyyy/MM/dd HH:mm:ss UTC±XX:XX
        Text longTimeDisplay = TextUtils.of(String.format("%4d/%d/%d %d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime
                .getMonth()
                .getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString));
        if ((boolean) ChatTools.CONFIG.get("general.Timestamp.CopyToChatBar.Enabled")) {
            String hashcode = TextUtils.putMessageMap(message, currentUnixTimestamp);
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ((MutableText) longTimeDisplay)
                    .append("\n\n").append(TextUtils.trans("texts.copy.launch")));
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chattools get_message " + hashcode);
            MutableText timestampText = ((MutableText) shortTimeDisplay).setStyle(Style.EMPTY.withHoverEvent(hoverEvent)
                                                                                             .withClickEvent(clickEvent));
            return (TextUtils.SPACER.copy().append(timestampText)).append(message);
        } else {
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, longTimeDisplay);
            MutableText timestampText = ((MutableText) shortTimeDisplay).setStyle(Style.EMPTY.withHoverEvent(hoverEvent));
            return (TextUtils.SPACER.copy().append(timestampText)).append(message);
        }
    }

    private static String timeInFormat(String formatter) {
        formatter = TextUtils.encodeColorCodes(formatter);
        LocalDateTime time = LocalDateTime.now();
        formatter = formatter.replace("{hour}", String.format("%d", time.getHour()))
                             .replace("{minute}", String.format("%02d", time.getMinute()))
                             .replace("{second}", String.format("%02d", time.getSecond()));
        return formatter;
    }
}
