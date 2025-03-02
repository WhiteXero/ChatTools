package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
//#if MC<12000
//$$ import net.minecraft.world.item.TooltipFlag;
//#endif

/**
 * @author 70CentsApple
 */
public class ClickEventsPreviewer {
    public static Style work(Style style) {
        if (style == null) {
            return null;
        }
        HoverEvent hoverEvent = style.getHoverEvent();
        Component textToAppend = getComponentToAppend(style);

        // Return if already injected or with no text to append
        if (isModified(style) || textToAppend == null) {
            return style;
        }
        // Add two empty lines before it (Also works as a Style Spacer)
        Component textToAppendWithTwoEmptyLinesInFront = TextUtils.literal("\n\n").copy().append(textToAppend);
        if (hoverEvent == null) {
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, textToAppend));
        } else {
            Component oldHoverComponent = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
            // Has Actions.SHOW_TEXT
            if (oldHoverComponent != null) {
                Component newHoverComponent = (TextUtils.SPACER.copy().append(oldHoverComponent)).append(textToAppendWithTwoEmptyLinesInFront);
                style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newHoverComponent));
            } else {
                HoverEvent.EntityTooltipInfo entityContent = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
                HoverEvent.ItemStackInfo itemContent = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
                if (entityContent != null) {
                    // Has Actions.SHOW_ENTITY
                    oldHoverComponent = TextUtils.textArray2text(entityContent.getTooltipLines());
                } else if (itemContent != null) {
                    // Has Actions.SHOW_ITEM
                    oldHoverComponent = TextUtils.textArray2text(
                            //#if MC>=12000
                            Screen.getTooltipFromItem(Minecraft.getInstance(), itemContent.getItemStack())
                            //#elseif MC>=11900
                            //$$ itemContent.getItemStack().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED)
                            //#else
                            //$$ itemContent.getItemStack().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.ADVANCED)
                            //#endif
                    );
                }
                if (oldHoverComponent != null) {
                    Component newHoverComponent = (TextUtils.SPACER.copy().append(oldHoverComponent)).append(textToAppendWithTwoEmptyLinesInFront);
                    style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newHoverComponent));
                } else {
                    style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, textToAppendWithTwoEmptyLinesInFront));
                }
            }
        }
        return style;
    }

    @Unique
    private static Component getComponentToAppend(Style style) {
        boolean hasInsertion = style.getInsertion() != null && !style.getInsertion().isBlank();
        boolean hasClickEvent = style.getClickEvent() != null;
        if (!hasInsertion && !hasClickEvent) {
            return null;
        }
        List<Component> texts = new ArrayList<>();
        texts.add(TextUtils.trans("texts.PreviewClickEvents.overall"));
        if (hasInsertion) {
            texts.add(TextUtils.trans("texts.PreviewClickEvents.insertion", style.getInsertion()));
        }
        if (hasClickEvent) {
            texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent"));
            ClickEvent clickEvent = style.getClickEvent();
            Component value = TextUtils.of(clickEvent.getValue()).copy().withStyle(ChatFormatting.GREEN);
            //#if MC>=12002
            String action = clickEvent.getAction().getSerializedName();
            //#else
            //$$ String action = clickEvent.getAction().getName();
            //#endif
            switch (action) {
                case "open_url":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.openUrl", value));
                    break;
                case "open_file":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.openFile", value));
                    break;
                case "run_command":
                    if (value.getString().contains("/chattools get_message")) {
                        // skip it
                        texts.remove(texts.size() - 1);
                        // the overall text is the only remained text
                        if (texts.size() <= 1) {
                            return null;
                        }
                    } else {
                        texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.runCommand", value));
                    }
                    break;
                case "suggest_command":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.suggestCommand", value));
                    break;
                case "change_page":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.changePage", value));
                    break;
                case "copy_to_clipboard":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.copyToClipboard", value));
                    break;
                default:
                    LoggerUtils.warn("[ChatTools] Unknown clickEvent action type: " + action);
            }
        }
        return TextUtils.textArray2text(texts);
    }

    @Unique
    private static Boolean isModified(Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) {
            return false;
        }
        Component tooltip = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
        if (tooltip == null) {
            return false;
        }
        return tooltip.getString().contains(TextUtils.trans("texts.PreviewClickEvents.overall").getString());
    }
}
