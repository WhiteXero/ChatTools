package net.apple70cents.chattools.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

//#if MC>=12000
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif

public class CopyFeatureScreen extends Screen {
    private MultiLineLabel messageSplit;
    private Screen oldScreen;
    private TextUtils.MessageUnit unit;
    private Map<String, Button> buttons;
    private final Map<String, ButtonData> buttonDatas;
    private MultiLineLabel previewTextSplit;

    protected static class ButtonData {
        public int index;
        public String content;

        public ButtonData(int index, String content) {
            this.index = index;
            this.content = content;
        }
    }

    public CopyFeatureScreen(TextUtils.MessageUnit unit) {
        super(TextUtils.trans("texts.copy.title"));
        this.oldScreen = Minecraft.getInstance().screen;
        this.messageSplit = MultiLineLabel.EMPTY;
        this.unit = unit;
        this.buttons = new HashMap<>();
        this.buttonDatas = new HashMap<>();
    }

    @Override
    protected void init() {
        super.init();
        this.messageSplit = MultiLineLabel.create(this.font, unit.message, this.width - 50);

        this.buttonDatas.put("copyObjectData", new ButtonData(-3, unit.message.toString()));
        this.buttonDatas.put("copyRaw", new ButtonData(-2, unit.message.getString()));
        this.buttonDatas.put("copyWithColorCodeEscaped", new ButtonData(-1, TextUtils.decodeColorCodes(unit.message.getString())));
        this.buttonDatas.put("copyWithNoColorCode", new ButtonData(0, TextUtils.wash(unit.message.getString())));
        this.buttonDatas.put("copyUnixTimestamp", new ButtonData(1, String.valueOf(unit.unixTimestamp)));
        this.buttonDatas.put("copyTimestamp", new ButtonData(2, this.getLongTimeDisplay(unit.unixTimestamp)));

        this.addButtons();
    }

    protected void addButtons() {
        int midH = this.height / 2;
        for (Map.Entry<String, ButtonData> buttonData : buttonDatas.entrySet()) {
            buttons.put(buttonData.getKey(), addCenterButton(buttonData.getKey(), midH + 21 * buttonData.getValue().index, (button -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(buttonData.getValue().content);
            })));
        }
        addCenterButton("cancel", this.height - 30, (button) -> {
            if (oldScreen instanceof ChatHistoryNavigatorScreen) {
                if (((ChatHistoryNavigatorScreen) oldScreen).keywordField != null) {
                    ((ChatHistoryNavigatorScreen) oldScreen).chatUnitListWidget.setKeyword(((ChatHistoryNavigatorScreen) oldScreen).keywordField.getValue());
                } else {
                    ((ChatHistoryNavigatorScreen) oldScreen).chatUnitListWidget.setKeyword("");
                }
            }
            Minecraft.getInstance().setScreen(oldScreen);
        });
    }

    protected Button addCenterButton(String translationKey, int y, Button.OnPress func) {
        int buttonW = 200;
        int buttonH = 20;
        //#if MC>=11900
        Button buttonWidget = Button.builder(TextUtils.trans("texts.copy." + translationKey), func)
                                                .pos(this.width / 2 - buttonW / 2, y - buttonH / 2)
                                                .size(buttonW, buttonH).build();
        //#else
        //$$ Button buttonWidget = new Button(this.width / 2 - buttonW / 2, y - buttonH / 2, buttonW, buttonH, TextUtils.trans("texts.copy." + translationKey), func);
        //#endif

        //#if MC>=11700
        this.addRenderableWidget(buttonWidget);
        //#else
        //$$ addButton(buttonWidget);
        //#endif

        return buttonWidget;
    }

    @Override
    public void render(
            //#if MC>=12000
            GuiGraphics context
            //#else
            //$$ PoseStack context
            //#endif
            , int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // this draws the title
        // context.drawCenteredString(this.font, this.title, this.width / 2, this.getTitleY(), 0xffffff);
        // this draws the message
        this.messageSplit.renderCentered(context, this.width / 2, this.getMessageY());
        // this draws the content preview
        Component previewText = TextUtils.SPACER;
        for (Map.Entry<String, Button> button : buttons.entrySet()) {
            if (button.getValue().isMouseOver(mouseX, mouseY)) {
                previewText = TextUtils.of(buttonDatas.get(button.getKey()).content);
            }
        }
        this.previewTextSplit = MultiLineLabel.create(this.font, previewText, this.width - 50);
        this.previewTextSplit.renderCentered(context, this.width / 2, this.height / 2 + 60);
    }

    private int getTitleY() {
        int i = (this.height - this.getMessagesHeight()) / 2;
        return Mth.clamp(i - 29, 10, 30);
    }

    private int getMessageY() {
        return this.getTitleY() + 20;
    }

    private int getMessagesHeight() {
        return this.messageSplit.getLineCount() * 9;
    }

    private String getLongTimeDisplay(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime currentTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneId.systemDefault().getRules()
                                                                                    .getOffset(instant));
        String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
        // yyyy/MM/dd HH:mm:ss UTC±XX:XX
        String longTimeDisplay = String.format("%4d/%d/%d %02d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime
                .getMonth()
                .getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString);
        return longTimeDisplay;
    }

}
