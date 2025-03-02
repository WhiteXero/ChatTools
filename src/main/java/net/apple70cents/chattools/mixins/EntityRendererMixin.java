package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=12102
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//#endif
/**
 * @author 70CentsApple
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    //#if MC>=12102
    Entity entity;
    float tickDelta;
    @Inject(method = "extractRenderState", at = @At(value = "HEAD"))
    private void updateEntityAndTickDelta(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        this.entity = entity;
        this.tickDelta = tickDelta;
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void render(EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render", at = @At(value = "HEAD"))
    //$$ private void render(Entity entity, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
    //#endif
        if (!((boolean) ConfigUtils.get("general.ChatTools.Enabled"))) {
            return;
        }
        if ((boolean) ConfigUtils.get("bubble.Enabled")) {
            BubbleRenderer.render(entity, poseStack, multiBufferSource, tickDelta);
        }
    }

    @ModifyVariable(method = "renderNameTag", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
    public Component nickHiderChangeLabel(Component text) {
        if (!((boolean) ConfigUtils.get("general.ChatTools.Enabled"))) {
            return text;
        } else if (!((boolean) ConfigUtils.get("general.NickHider.Enabled"))) {
            return text;
        }
        return NickHider.work(text);
    }
}
