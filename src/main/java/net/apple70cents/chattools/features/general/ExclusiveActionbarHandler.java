package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//#if MC>=12000
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif

public class ExclusiveActionbarHandler {
    protected static class ExclusiveActionbarMessageUnit {
        Component text;
        long startTime;
        long lifeTimeInMillis;

        ExclusiveActionbarMessageUnit(Component text, long startTime, long lifeTimeInMillis) {
            this.text = text;
            this.startTime = startTime;
            this.lifeTimeInMillis = lifeTimeInMillis;
        }

        ExclusiveActionbarMessageUnit(Component text, long lifeTimeInMillis) {
            this.text = text;
            this.startTime = System.currentTimeMillis();
            this.lifeTimeInMillis = lifeTimeInMillis;
        }
    }

    private static List<ExclusiveActionbarMessageUnit> messageUnitList = new CopyOnWriteArrayList<>();

    public static void addToRenderQueue(Component message, long lifeTimeInMillis) {
        messageUnitList.add(new ExclusiveActionbarMessageUnit(message, lifeTimeInMillis));
    }

    public static void tick() {
        long currentTime = System.currentTimeMillis();
        messageUnitList.removeIf(unit -> (currentTime - unit.startTime) > unit.lifeTimeInMillis);
    }

    //#if MC>=12000
    public static void render(GuiGraphics context) {
        //#else
        //$$ public static void render(PoseStack pose) {
        //#endif
        Font font = Minecraft.getInstance().font;

        float size = ((Number) ConfigUtils.get("general.ExclusiveActionbar.Size")).floatValue();
        int xOffset = ((Number) ConfigUtils.get("general.ExclusiveActionbar.XOffset")).intValue();
        int yOffset = ((Number) ConfigUtils.get("general.ExclusiveActionbar.YOffset")).intValue();
        int index = 0;
        for (ExclusiveActionbarMessageUnit ele : messageUnitList) {
            int opacity = calculateOpacity(ele.startTime, ele.lifeTimeInMillis);
            yOffset += calculateOffset(ele.startTime, ele.lifeTimeInMillis);
            if (opacity > 2) {
                //#if MC>=12000
                context.pose().pushPose();
                context.pose()
                       .translate(context.guiWidth() / 2.0F, context.guiHeight() - 68.0F - 4.0F, 0.0F);
                context.pose().scale(size, size, 1);
                context.drawCenteredString(font, ele.text, xOffset, yOffset + index * 12, opacity << 24 | 16777215);
                context.pose().popPose();
                //#else
                //$$ pose.pushPose();
                //$$ pose.translate(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2.0F, Minecraft
                //$$         .getInstance().getWindow().getGuiScaledHeight() - 68.0F - 4.0F, 0.0F);
                //$$ pose.scale(size, size, 1);
                //$$ int textWidth = font.width(ele.text);
                //$$ font.drawShadow(pose, ele.text, (-textWidth / 2.0F) + xOffset, yOffset + index * 12, opacity << 24 | 16777215);
                //$$ pose.popPose();
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
