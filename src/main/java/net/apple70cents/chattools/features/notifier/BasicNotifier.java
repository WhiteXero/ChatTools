package net.apple70cents.chattools.features.notifier;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class BasicNotifier {
    public static boolean shouldWork(Text text) {
        boolean shouldMatch = false;
        List<String> allowList = (List<String>) ConfigUtils.get("notifier.AllowList");
        List<String> banList = (List<String>) ConfigUtils.get("notifier.BanList");
        String washedMessage = TextUtils.wash(text.getString());
        for (String allowPattern : allowList) {
            if (Pattern.compile(allowPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                shouldMatch = true;
                break;
            }
        }
        // if MatchMyNameEnabled and it does have my name
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (((boolean) ConfigUtils.get("notifier.MatchMyNameEnabled")) && player != null && Pattern
                .compile(player.getName().getString(), Pattern.MULTILINE).matcher(washedMessage).find()) {
            shouldMatch = true;
        }
        // if any of the ban pattern is matched, we should NOT match it
        for (String banPattern : banList) {
            if (Pattern.compile(banPattern, Pattern.MULTILINE).matcher(washedMessage).find()) {
                shouldMatch = false;
                break;
            }
        }
        return shouldMatch;
    }

    public static Text work(Text text) {
        if ((boolean) ConfigUtils.get("notifier.IgnoreMyMessageEnabled") && MessageUtils.hadJustSentMessage()) {
            // my message SHOULD BE and ALREADY BEEN ignored
            MessageUtils.setJustSentMessage(false);
            return text;
        }

        LoggerUtils.info("[ChatTools] Found the latest chat message matches customized RegEx");
        MessageUtils.setJustSentMessage(false);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        World world = MinecraftClient.getInstance().world;

        // Toast
        if ((boolean) ConfigUtils.get("notifier.Toast.Enabled") && !MinecraftClient.getInstance().isWindowFocused()) {
            Toast.work(TextUtils.wash(text.getString()));
        }

        // Sound
        if ((boolean) ConfigUtils.get("notifier.Sound.Enabled") && player != null && world != null) {
            MinecraftClient.getInstance().execute(() -> {
                String identifier = (String) ConfigUtils.get("notifier.Sound.Type");
                int volume = ((Number) ConfigUtils.get("notifier.Sound.Volume")).intValue();
                int pitch = ((Number) ConfigUtils.get("notifier.Sound.Pitch")).intValue();

                boolean sendFromCameraPos = (boolean) ConfigUtils.get("notifier.Sound.PlaySoundFromCameraPositionEnabled");
                Entity camera = MinecraftClient.getInstance().cameraEntity;
                double x = (sendFromCameraPos && camera != null) ? camera.getPos().x : player.getX();
                double y = (sendFromCameraPos && camera != null) ? camera.getPos().y : player.getY();
                double z = (sendFromCameraPos && camera != null) ? camera.getPos().z : player.getZ();
                world.playSound(x, y, z,
                        //#if MC>=12100
                        SoundEvent.of(Identifier.of(identifier))
                        //#elseif MC>=11900
                        //$$ SoundEvent.of(new Identifier(identifier))
                        //#else
                        //$$ new SoundEvent(new Identifier(identifier))
                        //#endif
                        , SoundCategory.PLAYERS, volume * 0.01F, pitch * 0.1F, true);
            });
        }

        // Actionbar notifications
        if ((boolean) ConfigUtils.get("notifier.Actionbar.Enabled")) {
            MinecraftClient.getInstance().execute(() -> {
                MessageUtils.sendToActionbar(TextUtils.trans("texts.actionbar.title"));
            });
        }

        // Highlight
        if ((boolean) ConfigUtils.get("notifier.Highlight.Enabled")) {
            String prefix = TextUtils.encodeColorCodes((String) ConfigUtils.get("notifier.Highlight.Prefix"));
            if ((boolean) ConfigUtils.get("notifier.Highlight.OverwriteEnabled")) {
                return TextUtils.of(prefix + text.getString());
            } else {
                return (TextUtils.SPACER.copy().append(TextUtils.of(prefix))).append(text);
            }
        }
        return text;
    }
}
