package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.ChatTools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class ExclusiveActionbarHandler {
    protected static class ExclusiveActionbarMessageUnit {
        Text text;
        long startTime;
        long lifeTimeInMillis;

        ExclusiveActionbarMessageUnit(Text text, long startTime, long lifeTimeInMillis) {
            this.text = text;
            this.startTime = startTime;
            this.lifeTimeInMillis = lifeTimeInMillis;
        }

        ExclusiveActionbarMessageUnit(Text text, long lifeTimeInMillis) {
            this.text = text;
            this.startTime = System.currentTimeMillis();
            this.lifeTimeInMillis = lifeTimeInMillis;
        }
    }

    private static List<ExclusiveActionbarMessageUnit> messageUnitList = new ArrayList<>();

    public static void addToRenderQueue(Text message, long lifeTimeInMillis) {
        messageUnitList.add(new ExclusiveActionbarMessageUnit(message, lifeTimeInMillis));
    }

    public static void tick() {
        long currentTime = System.currentTimeMillis();
        messageUnitList.removeIf(unit -> (currentTime - unit.startTime) > unit.lifeTimeInMillis);
    }

    public static void render(DrawContext context) {
        long currentTime = System.currentTimeMillis();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        float size = ((Number) ChatTools.CONFIG.get("general.ExclusiveActionbar.Size")).floatValue();
        int xOffset = ((Number) ChatTools.CONFIG.get("general.ExclusiveActionbar.XOffset")).intValue();
        int yOffset = ((Number) ChatTools.CONFIG.get("general.ExclusiveActionbar.YOffset")).intValue();
        int index = 0;
        for (ExclusiveActionbarMessageUnit ele : messageUnitList) {
            int opacity = calculateOpacity(ele.startTime, ele.lifeTimeInMillis);
            if (opacity > 2) {
                context.getMatrices().push();
                context.getMatrices()
                       .translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() - 68.0F - 4.0F, 0.0F);
                context.getMatrices().scale(size, size, 1);
                int textWidth = textRenderer.getWidth(ele.text);
                context.drawTextWithBackground(textRenderer, ele.text, -textWidth / 2 + xOffset, yOffset + index * 12, textWidth, ColorHelper.withAlpha(opacity, -1));
                context.getMatrices().pop();
                index++;
            }
        }
    }

    private static int calculateOpacity(long startTime, long lifeTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        if (elapsedTime <= 0 || elapsedTime >= lifeTime) {
            // message outdated
            return 0;
        }

        long fadeDuration = 500;
        if (elapsedTime <= fadeDuration) {
            return (int) (elapsedTime * 255.0 / fadeDuration);
        } else if (elapsedTime <= lifeTime - fadeDuration) {
            return 255;
        } else {
            return (int) ((lifeTime - elapsedTime) * 255.0 / fadeDuration);
        }
    }

}
