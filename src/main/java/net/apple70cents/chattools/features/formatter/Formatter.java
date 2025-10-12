package net.apple70cents.chattools.features.formatter;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.PlaceholderEngine;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class Formatter {
    public static String work(String msg) {
        for (String s : (List<String>) ConfigUtils.get("formatter.DisableOnMatchList")) {
            if (Pattern.compile(s, Pattern.MULTILINE).matcher(msg).matches()) {
                // return in advance, and don't work with it.
                return msg;
            }
        }
        boolean matched = false;
        String formatter = "{text}";
        for (SpecialUnits.FormatterUnit unit : SpecialUnits.FormatterUnit.fromList((List) ConfigUtils.get("formatter.List"))) {
            if ("*".equals(unit.address) || Pattern.compile(unit.address).matcher(ContextUtils.getSessionIdentifier())
                                                   .matches()) {
                matched = true;
                formatter = unit.formatter;
                // we just need the first match result, break immediately.
                break;
            }
        }
        String modifiedMsg;
        if (matched) {
            LoggerUtils.info("[ChatTools] Chat Formatted.");
            PlaceholderEngine.addNewTempMapping("text", args -> msg);
            modifiedMsg = formatter;
        } else {
            modifiedMsg = msg;
        }
        modifiedMsg = PlaceholderEngine.apply(modifiedMsg);
        modifiedMsg = GradientParser.parse(modifiedMsg);
        PlaceholderEngine.clearTempMappings();

        if (modifiedMsg.length() <= ((Number) ConfigUtils.get("formatter.DisableThreshold")).intValue()) {
            return modifiedMsg;
        } else {
            return msg;
        }
    }
}
