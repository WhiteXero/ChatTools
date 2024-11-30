package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.general.NickHider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=12102
import net.minecraft.client.render.entity.state.EntityRenderState;
//#endif
/**
 * @author 70CentsApple
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    //#if MC>=12102
    Entity entity;
    float tickDelta;
    @Inject(method = "updateRenderState", at = @At(value = "HEAD"))
    private void updateEntityAndTickDelta(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        this.entity = entity;
        this.tickDelta = tickDelta;
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void render(EntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render", at = @At(value = "HEAD"))
    //$$ private void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
    //#endif
        if (!((boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled"))) {
            return;
        }
        if ((boolean) ChatTools.CONFIG.get("bubble.Enabled")) {
            BubbleRenderer.render(entity, matrices, vertexConsumers, tickDelta);
        }
    }

    @ModifyVariable(method = "renderLabelIfPresent", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
    public Text nickHiderChangeLabel(Text text) {
        if (!((boolean) ChatTools.CONFIG.get("general.ChatTools.Enabled"))) {
            return text;
        } else if (!((boolean) ChatTools.CONFIG.get("general.NickHider.Enabled"))) {
            return text;
        }
        return NickHider.work(text);
    }
}
