package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
//#if MC<12000
//$$ import net.minecraft.client.item.TooltipContext;
//$$ import net.minecraft.client.gui.screen.Screen;
//#endif

public class ClickEventsPreviewer {
    /* FIXME 某些情况会工作不了 例如输入错误指令（如/undefined）后的 empty parent（想办法把parent的style拿到？）类似结构如下：
        或许也可以换个方法或类（？）注入？比如类似Text的get tooltip或者visit（？）
      empty[
        style={color=red},
        siblings=[
            empty[
                style={color=gray, clickEvent=ClickEvent{action=SUGGEST_COMMAND, value='/undefined'}},
                siblings=[
                    literal{plg}[
                        style={color=red, underlined}
                    ],
                    translation{
                        key='command.context.here',
                        args=[]
                    }[
                        style={color=red, italic}
                    ]
                ]
            ]
        ]
    ]
     */
    public static Style work(Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        Text textToAppend = getTextToAppend(style);

        // Return if already injected or with no text to append
        if (isModified(style) || textToAppend == null) {
            return style;
        }
        // Add two empty lines before it (Also works as a Style Spacer)
        textToAppend = ((MutableText) TextUtils.literal("\n\n")).append(textToAppend);
        if (hoverEvent == null) {
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, textToAppend));
        } else {
            Text oldHoverText = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
            // Has Actions.SHOW_TEXT
            if (oldHoverText != null) {
                Text newHoverText = (TextUtils.SPACER.copy().append(oldHoverText)).append(textToAppend);
                style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newHoverText));
            } else {
                HoverEvent.EntityContent entityContent = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
                HoverEvent.ItemStackContent itemContent = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
                if (entityContent != null) {
                    // Has Actions.SHOW_ENTITY
                    oldHoverText = TextUtils.textArray2text(entityContent.asTooltip());
                } else if (itemContent != null) {
                    // Has Actions.SHOW_ITEM
                    oldHoverText = TextUtils.textArray2text(
                            //#if MC>=12000
                            Screen.getTooltipFromItem(MinecraftClient.getInstance(), itemContent.asStack())
                            //#elseif MC>=11900
                            //$$ itemContent.asStack().getTooltip(MinecraftClient.getInstance().player, TooltipContext.ADVANCED)
                            //#else
                            //$$ itemContent.asStack().getTooltip(MinecraftClient.getInstance().player, TooltipContext.Default.ADVANCED)
                            //#endif
                    );
                }
                if (oldHoverText != null) {
                    Text newHoverText = (TextUtils.SPACER.copy().append(oldHoverText)).append(textToAppend);
                    style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newHoverText));
                } else {
                    style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, textToAppend));
                }
            }
        }
        return style;
    }

    @Unique
    private static Text getTextToAppend(Style style) {
        boolean hasInsertion = style.getInsertion() != null && !style.getInsertion().isBlank();
        boolean hasClickEvent = style.getClickEvent() != null;
        if (!hasInsertion && !hasClickEvent) {
            return null;
        }
        List<Text> texts = new ArrayList<>();
        texts.add(TextUtils.trans("texts.PreviewClickEvents.overall"));
        if (hasInsertion) {
            texts.add(TextUtils.trans("texts.PreviewClickEvents.insertion", style.getInsertion()));
        }
        if (hasClickEvent) {
            texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent"));
            ClickEvent clickEvent = style.getClickEvent();
            Text value = TextUtils.of(clickEvent.getValue()).copy().formatted(Formatting.GREEN);
            //#if MC>=12002
            String action = clickEvent.getAction().asString();
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
                        // only the overall text is left
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
        Text tooltip = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
        if (tooltip == null) {
            return false;
        }
        return tooltip.getString().contains(TextUtils.trans("texts.PreviewClickEvents.overall").getString());
    }
}
