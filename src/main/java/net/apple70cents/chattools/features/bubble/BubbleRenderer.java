package net.apple70cents.chattools.features.bubble;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.utils.ContextUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//#if MC>=12100
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.util.math.Vec3d;
//#endif
//#if MC>=11900
import org.joml.Matrix4f;
//#else
//$$ import net.minecraft.util.math.Matrix4f;
//#endif

/**
 * @author 70CentsApple
 */
public class BubbleRenderer {
    static MinecraftClient mc = MinecraftClient.getInstance();
    static TextRenderer textRenderer = mc.textRenderer;

    protected static class BubbleUnit {
        Text text;
        long startTime;

        BubbleUnit(Text text, long startTime) {
            this.text = text;
            this.startTime = startTime;
        }

        BubbleUnit(String str, long startTime) {
            this.text = Text.of(str);
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

        public void render(Entity entity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, float tickDelta) {
            if (mc.player == null) {
                return;
            }
            Text renderText = text;
            if ((boolean) ChatTools.CONFIG.get("general.NickHider.Enabled")) {
                renderText = NickHider.work(text);
            }
            int yOffset = ((Number) ChatTools.CONFIG.get("bubble.YOffset")).intValue();
            EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
            matrixStack.push();
            // getNameLabelHeight() -> getHeight() + 0.5F

            //#if MC>=12100
            Vec3d vec3d = entity.getAttachments()
                                .getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getYaw(tickDelta));
            if (vec3d != null) {
                matrixStack.translate(vec3d.x, vec3d.y + 0.5F + yOffset / 10.0F, vec3d.z);
            }
            //#else
            //$$ matrixStack.translate(0.0F, entity.getHeight() + 0.5F + yOffset / 10.0F, 0.0F);
            //#endif
            matrixStack.multiply(renderDispatcher.getRotation());
            matrixStack.scale(
                    //#if MC>=12100
                    0.025F
                    //#else
                    //$$ -0.025F
                    //#endif
                    , -0.025F, 0.025F);
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            float xOffset = -textRenderer.getWidth(renderText) / 2.0F;
            textRenderer.draw(renderText, xOffset, 0, 553648127, false, matrix4f, vertexConsumers,
                    //#if MC>=11900
                    TextRenderer.TextLayerType.SEE_THROUGH
                    //#else
                    //$$ true
                    //#endif
                    , 1056964608, 15728640);
            textRenderer.draw(renderText, xOffset, 0, -1, false, matrix4f, vertexConsumers,
                    //#if MC>=11900
                    TextRenderer.TextLayerType.NORMAL
                    //#else
                    //$$ false
                    //#endif
                    , 0, 15728640);
            matrixStack.pop();
        }
    }

    private static Map<String, BubbleUnit> bubbleMap = new HashMap<>();

    public static void render(Entity entity, MatrixStack matrices, VertexConsumerProvider vertex, float tickDelta) {
        if (bubbleMap.isEmpty()) {
            return;
        } else if (mc.world == null) {
            return;
        }
        for (AbstractClientPlayerEntity potentialSender : mc.world.getPlayers()) {
            Text senderDisplayName = potentialSender.getDisplayName();
            Text entityDisplayName = entity.getDisplayName();
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
                                .getLifetime() >= ((Number) ChatTools.CONFIG.get("bubble.Lifetime")).intValue() * 1000L) {
                // the bubble's lifetime is over, let's remove it
                bubbleMap.remove(senderName);
                continue;
            }
            double d = mc.getEntityRenderDispatcher().getSquaredDistanceToCamera(potentialSender);
            if (d <= 4096.0) {
                bubbleMap.get(senderName).render(entity, matrices, vertex, tickDelta);
            }
        }
    }

    public static void addChatBubble(Text text) {
        String message = TextUtils.wash(text.getString());
        if (mc.world == null) {
            return;
        }
        String pattern = "";
        boolean serverAddressPass = false;
        boolean fallback = false;
        for (SpecialUnits.BubbleRuleUnit unit : SpecialUnits.BubbleRuleUnit.fromList((List) ChatTools.CONFIG.get("bubble.List"))) {
            if (mc.getCurrentServerEntry() == null) {
                // single player world
                if ("*".equals(unit.address)) {
                    serverAddressPass = true;
                    pattern = unit.pattern;
                    fallback = unit.fallback;
                    break;
                }
            } else if ("*".equals(unit.address) || Pattern.compile(unit.address)
                                                          .matcher(ContextUtils.getSessionIdentifier()).matches()) {
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
