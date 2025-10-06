package net.apple70cents.chattools;

import net.apple70cents.chattools.features.chatkeybindings.Macro;
import net.apple70cents.chattools.features.chatkeybindings.Repeat;
import net.apple70cents.chattools.features.chatkeybindings.ReviewLastMessageWithUpArrowOnly;
import net.apple70cents.chattools.utils.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * @author 70CentsApple
 */
public class ChatTools implements ModInitializer {

    @Override
    public void onInitialize() {
        LoggerUtils.init();
        ConfigUtils.init();
        // show welcome message if needed
        ClientTickEvents.START_WORLD_TICK.register(client -> {
            if ((boolean) ConfigUtils.get("general.ShowWelcomeMessageEnabled")) {
                if (Minecraft.getInstance().player != null) {
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.welcomeMessage").copy().setStyle(TextUtils.WEBSITE_URL_STYLE));
                    LoggerUtils.info("[ChatTools] Shown welcome message.");
                    ConfigUtils.set("general.ShowWelcomeMessageEnabled", false);
                    ConfigUtils.save();
                }
            }
        });

        // register features
        ClientTickEvents.START_WORLD_TICK.register(client -> {
            if (!(boolean) ConfigUtils.get("general.ChatTools.Enabled")) {
                return;
            }
            Repeat.tick();
            ReviewLastMessageWithUpArrowOnly.tick();
            if ((boolean) ConfigUtils.get("chatkeybindings.Macro.Enabled")) {
                Macro.tick();
            }
        });

        // register commands
        CommandRegistryUtils.register();

        Runnable runnable = () -> {
            if (DownloadUtils.shouldCheckIfFullyReady()) {
                if (!DownloadUtils.checkIfFullyReady()) {
                    DownloadUtils.startDownload();
                    LoggerUtils.info("[ChatTools] Not yet fully ready, downloading...");
                }
                LoggerUtils.info("[ChatTools] Initial download thread terminated.");
            } else {
                LoggerUtils.info("[ChatTools] No need to check addons readiness.");
            }
        };
        // Start the file download in a new thread
        Thread downloadThread = new Thread(runnable, "ChatTools-Download-Thread");
        downloadThread.start();
    }
}
