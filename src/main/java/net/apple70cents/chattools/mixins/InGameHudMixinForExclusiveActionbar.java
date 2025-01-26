package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.general.ExclusiveActionbarHandler;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=12100
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
//#elseif MC>=12000
//$$ import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

/**
 * @author 70CentsApple
 */

@Mixin(InGameHud.class)
public abstract class InGameHudMixinForExclusiveActionbar {
    //#if MC>=12100
    @Inject(method = "renderOverlayMessage", at = @At(value = "HEAD"))
    public void renderExclusiveActionbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
    //#elseif MC>=12000
    //$$ @Inject(method = "render", at = @At(value = "HEAD"))
    //$$ public void renderExclusiveActionbar(DrawContext context, float tickDelta, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render", at = @At(value = "HEAD"))
    //$$ public void renderExclusiveActionbar(MatrixStack context, float tickDelta, CallbackInfo ci) {
    //#endif
        if (MinecraftClient.getInstance().options.hudHidden) {
            return;
        }
        if (!((boolean) ConfigUtils.get("general.ChatTools.Enabled"))) {
            return;
        }
        if (!((boolean) ConfigUtils.get("general.ExclusiveActionbar.Enabled"))) {
            return;
        }
        ExclusiveActionbarHandler.tick();
        ExclusiveActionbarHandler.render(context);
    }
}
