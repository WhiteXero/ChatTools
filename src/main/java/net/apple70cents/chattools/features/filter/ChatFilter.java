package net.apple70cents.chattools.features.filter;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter {

    public static boolean shouldFilter(Text text) {
        if (!(boolean) ConfigUtils.get("filter.Enabled")) {
            return false;
        }
        // only respond once
        if (TextUtils.wash(text.getString())
                     .contains(TextUtils.wash(TextUtils.trans("texts.filterPlaceholder").getString()))) {
            return false;
        }
        List<String> filterList = (List<String>) ConfigUtils.get("filter.List");
        String washed = TextUtils.wash(text.getString());
        for (String pattern : filterList) {
            if (Pattern.compile(pattern, Pattern.MULTILINE).matcher(washed).find()) {
                return true;
            }
        }
        return false;
    }

    public static void sendPlaceholderIfActive() {
        if (!(boolean) ConfigUtils.get("filter.FilteredPlaceholderEnabled")) {
            return;
        }
        Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextUtils.trans("texts.filterPlaceholder.@Tooltip")));
        Text placeholder = ((MutableText) TextUtils.trans("texts.filterPlaceholder")).setStyle(style);
        MessageUtils.sendToNonPublicChat(placeholder);
    }
}
