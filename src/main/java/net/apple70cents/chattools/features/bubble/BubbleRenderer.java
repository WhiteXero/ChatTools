package net.apple70cents.chattools.features.bubble;

import com.mojang.blaze3d.vertex.PoseStack;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//#if MC>=12100
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
//#endif
//#if MC>=11900
import org.joml.Matrix4f;
//#else
//$$ import com.mojang.math.Matrix4f;
//#endif

/**
 * @author 70CentsApple
 */
public class BubbleRenderer {
    static Minecraft mc = Minecraft.getInstance();
    static Font font = mc.font;

    protected static class BubbleUnit {
        Component text;
        long startTime;

        BubbleUnit(Component text, long startTime) {
            this.text = text;
            this.startTime = startTime;
        }

        BubbleUnit(String str, long startTime) {
            this.text = TextUtils.of(str);
            this.startTime = startTime;
        }

        /**
         * get lifetime in milliseconds
         *
         * @return lifetime
         */
        public long getLifetime() {
            return System.currentTimeMillis() - startTime;
        }

        public String toString() {
            return "BubbleUnit{" + "text=" + text + ", startTime=" + startTime + '}';
        }

        public void render(Entity entity, PoseStack poseStack, MultiBufferSource multiBufferSource, float tickDelta) {
            if (mc.player == null) {
                return;
            }
            Component renderComponent = text;
            if ((boolean) ConfigUtils.get("general.NickHider.Enabled")) {
                renderComponent = NickHider.work(text);
            }
            int yOffset = ((Number) ConfigUtils.get("bubble.YOffset")).intValue();
            EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
            poseStack.pushPose();
            // getNameLabelHeight() -> getHeight() + 0.5F

            //#if MC>=12100
            Vec3 vec3d = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot());
            if (vec3d != null) {
                poseStack.translate(vec3d.x, vec3d.y + 0.5F + yOffset / 10.0F, vec3d.z);
            }
            //#else
            //$$ poseStack.translate(0.0F, entity.getBbHeight() + 0.5F + yOffset / 10.0F, 0.0F);
            //#endif
            poseStack.mulPose(renderDispatcher.cameraOrientation());
            poseStack.scale(
                    //#if MC>=12100
                    0.025F
                    //#else
                    //$$ -0.025F
                    //#endif
                    , -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();
            int maxLineWidth = ((Number) ConfigUtils.get("bubble.MaxLineWidth")).intValue();
            List<FormattedCharSequence> lines = font.split(renderComponent, maxLineWidth);
            int lines_amount = lines.size();
            for (int i = 0; i < lines_amount; i++) {
                int y = 9 * (i - lines_amount);
                FormattedCharSequence line = lines.get(i);
                float xOffset = -font.width(line) / 2.0F;
                font.drawInBatch(line, xOffset, y, 553648127, false, matrix4f, multiBufferSource,
                        //#if MC>=11900
                        Font.DisplayMode.SEE_THROUGH
                        //#else
                        //$$ true
                        //#endif
                        , 1056964608, 15728640);
                font.drawInBatch(line, xOffset, y, -1, false, matrix4f, multiBufferSource,
                        //#if MC>=11900
                        Font.DisplayMode.NORMAL
                        //#else
                        //$$ false
                        //#endif
                        , 0, 15728640);
            }
            poseStack.popPose();
        }
    }

    private static Map<String, BubbleUnit> bubbleMap = new HashMap<>();

    public static void render(Entity entity, PoseStack poseStack, MultiBufferSource multiBufferSource, float tickDelta) {
        if (bubbleMap.isEmpty()) {
            return;
        } else if (mc.level == null) {
            return;
        }
        for (AbstractClientPlayer potentialSender : mc.level.players()) {
            Component senderDisplayName = potentialSender.getDisplayName();
            Component entityDisplayName = entity.getDisplayName();
            if (senderDisplayName == null || entityDisplayName == null) {
                return;
            }
            String senderName = senderDisplayName.getString();
            if (!bubbleMap.containsKey(senderName)) {
                continue;
            } else if (!TextUtils.wash(entityDisplayName.getString()).equals(senderName)) {
                // not the entity being selected
                continue;
            } else if (bubbleMap.get(senderName)
                                .getLifetime() >= ((Number) ConfigUtils.get("bubble.Lifetime")).intValue() * 1000L) {
                // the bubble's lifetime is over, let's remove it
                bubbleMap.remove(senderName);
                continue;
            }
            double d = mc.getEntityRenderDispatcher().distanceToSqr(potentialSender);
            if (d <= 4096.0) {
                bubbleMap.get(senderName).render(entity, poseStack, multiBufferSource, tickDelta);
            }
        }
    }

    public static void addChatBubble(Component text) {
        String message = TextUtils.wash(text.getString());
        if (mc.level == null) {
            return;
        }
        String pattern = "";
        boolean serverAddressPass = false;
        boolean fallback = false;
        for (SpecialUnits.BubbleRuleUnit unit : SpecialUnits.BubbleRuleUnit.fromList((List) ConfigUtils.get("bubble.List"))) {
            if ("*".equals(unit.address) || Pattern.compile(unit.address).matcher(ContextUtils.getSessionIdentifier())
                                                   .matches()) {
                serverAddressPass = true;
                pattern = unit.pattern;
                fallback = unit.fallback;
                break;
            }
        }
        if (serverAddressPass && !pattern.isEmpty()) {
            Matcher matcher = Pattern.compile(pattern).matcher(message);
            if (matcher.find()) {
                String name = matcher.group("name");
                String messageContext = matcher.group("message");
                bubbleMap.put(name, new BubbleUnit(messageContext, System.currentTimeMillis()));
            } else if (fallback) {
                String sender = MessageUtils.findTheFirstPlayerName(message);
                if (sender == null) {
                    return;
                }
                bubbleMap.put(sender, new BubbleUnit(text, System.currentTimeMillis()));
            }
        }
    }
}
