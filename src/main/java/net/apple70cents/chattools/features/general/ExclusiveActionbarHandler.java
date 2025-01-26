package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

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

    //#if MC>=12000
    public static void render(DrawContext context) {
        //#else
        //$$ public static void render(MatrixStack matrices) {
        //#endif
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        float size = ((Number) ConfigUtils.get("general.ExclusiveActionbar.Size")).floatValue();
        int xOffset = ((Number) ConfigUtils.get("general.ExclusiveActionbar.XOffset")).intValue();
        int yOffset = ((Number) ConfigUtils.get("general.ExclusiveActionbar.YOffset")).intValue();
        int index = 0;
        for (ExclusiveActionbarMessageUnit ele : messageUnitList) {
            int opacity = calculateOpacity(ele.startTime, ele.lifeTimeInMillis);
            yOffset += calculateOffset(ele.startTime, ele.lifeTimeInMillis);
            if (opacity > 2) {
                //#if MC>=12000
                context.getMatrices().push();
                context.getMatrices()
                       .translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() - 68.0F - 4.0F, 0.0F);
                context.getMatrices().scale(size, size, 1);
                int textWidth = textRenderer.getWidth(ele.text);
                context.drawCenteredTextWithShadow(textRenderer, ele.text, xOffset, yOffset + index * 12, opacity << 24 | 16777215);
                context.getMatrices().pop();
                //#else
                //$$ matrices.push();
                //$$ matrices.translate(MinecraftClient.getInstance().getWindow().getScaledWidth() / 2.0F, MinecraftClient
                //$$         .getInstance().getWindow().getScaledHeight() - 68.0F - 4.0F, 0.0F);
                //$$ matrices.scale(size, size, 1);
                //$$ int textWidth = textRenderer.getWidth(ele.text);
                //$$ textRenderer.drawWithShadow(matrices, ele.text, (-textWidth / 2.0F) + xOffset, yOffset + index * 12, opacity << 24 | 16777215);
                //$$ matrices.pop();
                //#endif
                index++;
            }
        }
    }

    /**
     * Calculate opacity for fade in & fade outs.
     *
     * @return opacity between 0 and 255
     */
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

    /**
     * Calculate offset for fade in.
     *
     * @return opacity between 0 and 10
     */
    private static int calculateOffset(long startTime, long lifeTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        if (elapsedTime <= 0 || elapsedTime >= lifeTime) {
            return 10;
        }

        long fadeDuration = 200;
        if (elapsedTime <= fadeDuration) {
            double progress = Math.sin(((double) elapsedTime / fadeDuration * Math.PI / 2));
            return (int) (10 - progress * 10);
        }
        return 0;
    }

}
