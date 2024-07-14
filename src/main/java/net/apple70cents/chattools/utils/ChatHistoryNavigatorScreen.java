package net.apple70cents.chattools.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//#if MC>=11900
import net.minecraft.client.gui.tooltip.Tooltip;
//#endif
//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
//#elseif MC>=11700
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.client.gui.Element;
//$$ import net.minecraft.client.gui.Selectable;
//$$ import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
//$$ import net.minecraft.client.gui.screen.narration.NarrationPart;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.client.gui.Element;
//#endif

public class ChatHistoryNavigatorScreen extends Screen {
    @Nullable
    private TextFieldWidget keywordField;
    ChatUnitListWidget chatUnitListWidget;
    ButtonWidget modeSelectorWidget;

    public ChatHistoryNavigatorScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        this.keywordField = new TextFieldWidget(this.textRenderer, 30, 35, this.width - 155, 20, this.keywordField, TextUtils.trans("texts.ChatHistoryNavigator.placeholder"));
        this.keywordField.setChangedListener(keyword -> {
            this.chatUnitListWidget.setKeyword(keyword);
        });

        //#if MC>=11700
        this.addDrawableChild(this.keywordField);
        //#else
        //$$ this.addButton(this.keywordField);
        //#endif
        this.setInitialFocus(this.keywordField);

        this.chatUnitListWidget = new ChatUnitListWidget(MinecraftClient.getInstance(), this.width - 60, this.height - 120, 65, textRenderer.fontHeight + 3, this.keywordField.getText(), this.chatUnitListWidget);
        //#if MC>=12002
        this.chatUnitListWidget.setX(30);
        //#else
        //$$ this.chatUnitListWidget.setLeftPos(30);
        //#endif

        //#if MC>=12005
        //#elseif MC>=11700
        //$$ this.chatUnitListWidget.setRenderBackground(false);
        //$$ this.chatUnitListWidget.setRenderHorizontalShadows(false);
        //#else
        //$$ this.chatUnitListWidget.method_31322(false);
        //$$ this.chatUnitListWidget.method_31323(false);
        //#endif
        this.addSelectableChild(chatUnitListWidget);

        // Mode Selector Button
        Text modeSelectorButtonText = TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode());
        ButtonWidget.PressAction pressAction = (button) -> {
            this.chatUnitListWidget.switchToNextSearchMode();
            this.modeSelectorWidget.setMessage(TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode()));
            //#if MC>=11900
            this.modeSelectorWidget.setTooltip(Tooltip.of(TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip")));
            //#endif

            this.chatUnitListWidget.setKeyword(this.keywordField.getText());
            this.chatUnitListWidget.refreshUnitEntries();
        };
        //#if MC>=11900
        this.modeSelectorWidget = ButtonWidget.builder(modeSelectorButtonText, pressAction)
                                              .dimensions(this.width - 120, 35, 90, 20).build();
        this.addDrawableChild(modeSelectorWidget);
        //#elseif MC>=11700
        //$$ this.modeSelectorWidget = new ButtonWidget(this.width - 120, 35, 90, 20, modeSelectorButtonText, pressAction, (button, matrices, mouseX, mouseY) -> renderTooltip(matrices, TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip"), mouseX, mouseY));
        //$$ addDrawableChild(modeSelectorWidget);
        //#else
        //$$ this.modeSelectorWidget = new ButtonWidget(this.width - 120, 35, 90, 20, modeSelectorButtonText, pressAction, (button, matrices, mouseX, mouseY) -> TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip"));
        //$$ addButton(modeSelectorWidget);
        //#endif

        // Done button
        //#if MC>=11900
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).dimensions(this.width / 2 - 80, this.height - 28, 160, 20).build());
        //#elseif MC>=11700
        //$$ addDrawableChild(new ButtonWidget(this.width / 2 - 80, this.height - 28, 160, 20, ScreenTexts.DONE, (button) -> {this.close();}));
        //#else
        //$$ addButton(new ButtonWidget(this.width / 2 - 80, this.height - 28, 160, 20, ScreenTexts.DONE, (button) -> {MinecraftClient.getInstance().openScreen(null);}));
        //#endif
    }


    @Override
    public void render(
            //#if MC>=12000
            DrawContext context
            //#else
            //$$ MatrixStack context
            //#endif
            , int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // this draws the title
        //#if MC>=12000
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);
        //#else
        //$$ drawCenteredTextWithShadow(context,this.textRenderer, this.title, this.width / 2, 15, 16777215);
        //#endif

        if (this.chatUnitListWidget.searchMode == SearchModes.REGEX) {
            try {
                if (this.keywordField != null) {
                    Pattern.compile(this.keywordField.getText());
                }
            } catch (PatternSyntaxException e) {
                Text errorText = TextUtils.literal(e.getDescription()).copy()
                                          .setStyle(Style.EMPTY.withFormatting(Formatting.RED));
                //#if MC>=12000
                context.drawTooltip(textRenderer, errorText, mouseX, mouseY);
                //#else
                //$$ renderTooltip(context, errorText, mouseX, mouseY);
                //#endif
            }
        }

        if (!chatUnitListWidget.hashcodeResultList.isEmpty()) {
            chatUnitListWidget.render(context, mouseX, mouseY, delta);
        }
    }

    protected class ChatUnitEntry extends ElementListWidget.Entry<ChatUnitEntry> {
        TextUtils.MessageUnit messageUnit;

        public ChatUnitEntry(String hashcode) {
            this.messageUnit = TextUtils.getMessageMap(hashcode);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // left click
            if (button == 0) {
                // FIXME Recover hashcodeResultList when the Copy Screen closes
                MinecraftClient.getInstance().setScreen(new CopyFeatureScreen(messageUnit));
                return true;
            }
            return false;
        }

        public Text getText() {
            return this.messageUnit.message;
        }

        public Text getTooltip() {
            LocalDateTime time = LocalDateTime.ofEpochSecond(this.messageUnit.unixTimestamp, 0, ZoneId.systemDefault()
                                                                                                      .getRules()
                                                                                                      .getOffset(Instant.now()));
            String offsetString = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getId();
            // yyyy/MM/dd HH:mm:ss UTC±XX:XX
            Text longTimeDisplay = TextUtils.of(String.format("%4d/%d/%d %d:%02d:%02d\nUTC%s", time.getYear(), time
                    .getMonth()
                    .getValue(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond(), offsetString));
            return longTimeDisplay;
        }

        //#if MC>=11700
        public List<? extends Selectable> selectableChildren() {
            return Collections.emptyList();
        }
        //#endif

        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(
                //#if MC>=12000
                DrawContext context
                //#else
                //$$ MatrixStack context
                //#endif
                , int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //#if MC>=12000
            context.drawTextWithShadow(textRenderer, this.getText(), x, y, 16777215);
            //#else
            //$$ drawTextWithShadow(context, textRenderer, this.getText(), x, y, 16777215);
            //#endif
            if (hovered) {
                List<Text> timestamps = Arrays.stream(this.getTooltip().getString().split("\n")).map(TextUtils::of)
                                              .toList();
                //#if MC>=12000
                context.drawTooltip(textRenderer, timestamps, mouseX, mouseY);
                //#else
                //$$ renderTooltip(context, timestamps, mouseX, mouseY);
                //#endif
            }
        }
    }

    public enum SearchModes {
        CASE_INSENSITIVE, CASE_SENSITIVE, REGEX;
    }

    protected class ChatUnitListWidget extends EntryListWidget<ChatUnitEntry> {
        private List<String> hashcodeResultList;
        private SearchModes searchMode;

        protected void updateResultList(String keyword) {
            if (hashcodeResultList == null) {
                hashcodeResultList = new ArrayList<>();
            }
            hashcodeResultList.clear();
            if (keyword == null || keyword.isBlank()) {
                return;
            }

            if (searchMode == SearchModes.REGEX) {
                try {
                    Pattern.compile(keyword);
                } catch (PatternSyntaxException e) {
                    return;
                }
            }
            Predicate<Map.Entry<String, TextUtils.MessageUnit>> filter = null;
            switch (searchMode) {
                case CASE_INSENSITIVE:
                    filter = entry -> TextUtils.wash(entry.getValue().message.getString().toUpperCase())
                                               .contains(keyword.toUpperCase());
                    break;
                case CASE_SENSITIVE:
                    filter = entry -> TextUtils.wash(entry.getValue().message.getString()).contains(keyword);
                    break;
                case REGEX:
                    try {
                        filter = entry -> Pattern.compile(keyword, Pattern.MULTILINE)
                                                 .matcher(TextUtils.wash(entry.getValue().message.getString())).find();
                    } catch (PatternSyntaxException e) {
                        return;
                    }
                    break;
            }
            Stream<Map.Entry<String, TextUtils.MessageUnit>> stream = TextUtils.messageMap.entrySet().stream();
            hashcodeResultList = stream.filter(filter).map(Map.Entry::getKey).collect(Collectors.toList());
        }

        protected void refreshUnitEntries() {
            if (chatUnitListWidget == null) {
                return;
            }
            this.clearEntries();
            for (String hashcode : hashcodeResultList) {
                this.addEntry(new ChatUnitEntry(hashcode));
            }
        }

        public void setKeyword(String keyword) {
            this.updateResultList(keyword);
            this.refreshUnitEntries();
        }

        public void switchToNextSearchMode() {
            if (this.searchMode == SearchModes.CASE_INSENSITIVE) {
                this.searchMode = SearchModes.CASE_SENSITIVE;
            } else if (this.searchMode == SearchModes.CASE_SENSITIVE) {
                this.searchMode = SearchModes.REGEX;
            } else if (this.searchMode == SearchModes.REGEX) {
                this.searchMode = SearchModes.CASE_INSENSITIVE;
            }
        }

        public void setSearchMode(SearchModes searchMode) {
            this.searchMode = searchMode;
        }

        public SearchModes getSearchMode() {
            return this.searchMode;
        }

        public ChatUnitListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, String keyword, @Nullable ChatUnitListWidget copyFrom) {
            //#if MC>=12002
            super(client, width, height, y, itemHeight);
            //#else
            //$$ super(client, width, height, y, y + height, itemHeight);
            //#endif
            this.searchMode = SearchModes.CASE_INSENSITIVE;
            if (copyFrom != null) {
                this.hashcodeResultList = copyFrom.hashcodeResultList;
                this.searchMode = copyFrom.getSearchMode();
            } else {
                this.hashcodeResultList = new ArrayList<>();
                this.updateResultList(keyword);
            }
            this.refreshUnitEntries();
        }

        @Override
        public int getRowWidth() {
            return this.width - 15;
        }

        @Override
        protected int getScrollbarX() {
            //#if MC>=12002
            int x = this.getX();
            //#else
            //$$ int x = this.left;
            //#endif
            return this.width - 7 + x;
        }

        //#if MC>=12005
        @Override
        protected void drawMenuListBackground(DrawContext context) {
        }
        //#endif

        //#if MC>=11900
        @Nullable
        public Tooltip getTooltip() {
            if (getHoveredEntry() == null) {
                return null;
            }
            return Tooltip.of(getHoveredEntry().getTooltip());
        }
        //#endif

        //#if MC>=12002
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            if (getHoveredEntry() != null) {
                builder.put(NarrationPart.TITLE, getHoveredEntry().getText());
            }
        }
        //#elseif MC>=11700
        //$$ public void appendNarrations(NarrationMessageBuilder builder) {
        //$$     if (getHoveredEntry() != null) {builder.put(NarrationPart.TITLE, getHoveredEntry().getText());}
        //$$ }
        //#else
        //#endif
    }
}
