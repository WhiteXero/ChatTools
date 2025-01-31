package net.apple70cents.chattools.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

public class CopyFeatureScreen extends Screen {
    private MultilineText messageSplit;
    private Screen oldScreen;
    private TextUtils.MessageUnit unit;
    private Map<String, ButtonWidget> buttons;
    private final Map<String, ButtonData> buttonDatas;
    private MultilineText previewTextSplit;

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
        this.oldScreen = MinecraftClient.getInstance().currentScreen;
        this.messageSplit = MultilineText.EMPTY;
        this.unit = unit;
        this.buttons = new HashMap<>();
        this.buttonDatas = new HashMap<>();
    }

    @Override
    protected void init() {
        super.init();
        this.messageSplit = MultilineText.create(this.textRenderer, unit.message, this.width - 50);

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
                MinecraftClient.getInstance().keyboard.setClipboard(buttonData.getValue().content);
            })));
        }
        addCenterButton("cancel", this.height - 30, (button) -> {
            if (oldScreen instanceof ChatHistoryNavigatorScreen) {
                if (((ChatHistoryNavigatorScreen) oldScreen).keywordField != null) {
                    ((ChatHistoryNavigatorScreen) oldScreen).chatUnitListWidget.setKeyword(((ChatHistoryNavigatorScreen) oldScreen).keywordField.getText());
                } else {
                    ((ChatHistoryNavigatorScreen) oldScreen).chatUnitListWidget.setKeyword("");
                }
            }
            MinecraftClient.getInstance().setScreen(oldScreen);
        });
    }

    protected ButtonWidget addCenterButton(String translationKey, int y, ButtonWidget.PressAction func) {
        int buttonW = 200;
        int buttonH = 20;
        //#if MC>=11900
        ButtonWidget buttonWidget = ButtonWidget.builder(TextUtils.trans("texts.copy." + translationKey), func)
                                                .position(this.width / 2 - buttonW / 2, y - buttonH / 2)
                                                .size(buttonW, buttonH).build();
        //#else
        //$$ ButtonWidget buttonWidget = new ButtonWidget(this.width / 2 - buttonW / 2, y - buttonH / 2, buttonW, buttonH, TextUtils.trans("texts.copy." + translationKey), func);
        //#endif

        //#if MC>=11700
        this.addDrawableChild(buttonWidget);
        //#else
        //$$ addButton(buttonWidget);
        //#endif

        return buttonWidget;
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
        // context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.getTitleY(), 16777215);
        // this draws the message
        this.messageSplit.drawCenterWithShadow(context, this.width / 2, this.getMessageY());
        // this draws the content preview
        Text previewText = TextUtils.SPACER;
        for (Map.Entry<String, ButtonWidget> button : buttons.entrySet()) {
            if (button.getValue().isMouseOver(mouseX, mouseY)) {
                previewText = TextUtils.of(buttonDatas.get(button.getKey()).content);
            }
        }
        this.previewTextSplit = MultilineText.create(this.textRenderer, previewText, this.width - 50);
        this.previewTextSplit.drawCenterWithShadow(context, this.width / 2, this.height / 2 + 60);
    }

    private int getTitleY() {
        int i = (this.height - this.getMessagesHeight()) / 2;
        return MathHelper.clamp(i - 29, 10, 30);
    }

    private int getMessageY() {
        return this.getTitleY() + 20;
    }

    private int getMessagesHeight() {
        return this.messageSplit.count() * 9;
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
