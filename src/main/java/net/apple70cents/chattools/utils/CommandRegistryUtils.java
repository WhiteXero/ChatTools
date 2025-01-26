package net.apple70cents.chattools.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ConfigScreenGenerator;
import net.apple70cents.chattools.config.ConfigStorage;
import net.apple70cents.chattools.config.SpecialUnits;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

//#if MC>=11900
import net.minecraft.command.CommandRegistryAccess;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//#else
// fabric v2 begin to work since 1.19
//$$ import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
//$$ import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
//#endif


public class CommandRegistryUtils {
    public static void register() {
        //#if MC>=11900
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register((LiteralArgumentBuilder<FabricClientCommandSource>) CommandRegistryUtils.getBuilder(registryAccess));
        });
        //#else
        //$$ ClientCommandManager.DISPATCHER.register((LiteralArgumentBuilder<FabricClientCommandSource>) CommandRegistryUtils.getBuilder());
        //#endif
    }

    public static LiteralArgumentBuilder<?> getBuilder(
            //#if MC>=11900
            CommandRegistryAccess registryAccess
            //#endif
    ) {
        // @formatter:off
        return literal("chattools")
            // chattools send_to_client
            .then(literal("send_to_client")
                // chattools send_to_client text
                .then(literal("text").then(argument("message", TextArgumentType.text(
                        //#if MC>=12006
                        registryAccess
                        //#endif
                    )).executes(t -> {
                    Text text = Texts.parse(null, TextArgumentType.getTextArgument(t, "message"), MinecraftClient.getInstance().player, 0);
                    MessageUtils.sendToNonPublicChat(text);
                    return Command.SINGLE_SUCCESS;
                })))
                // chattools send_to_client actionbar
                .then(literal("actionbar").then(argument("message", TextArgumentType.text(
                        //#if MC>=12006
                        registryAccess
                        //#endif
                    )).executes(t -> {
                    Text text = Texts.parse(null, TextArgumentType.getTextArgument(t, "message"), MinecraftClient.getInstance().player, 0);
                    MessageUtils.sendToActionbar(text);
                    return Command.SINGLE_SUCCESS;
                })
            )))
            // chattools download
            .then(literal("download").executes(t -> {
                LoggerUtils.info("[ChatTools] Command Executed: Trying to download Addon Toast dependencies");
                DownloadUtils.startDownloadWithCallback((file, progress, nowKB, totalKB) -> {
                    MessageUtils.sendToActionbar(TextUtils.trans("texts.download.process", file, progress, nowKB, totalKB));
                });
                if (DownloadUtils.checkIfFullyReady()){
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.download.success"));
                }
                return Command.SINGLE_SUCCESS;
            }))
            // chattools opengui
            .then(literal("opengui").executes(t -> {
                MessageUtils.sendToActionbar(TextUtils.trans("gui.title"));
                MinecraftClient.getInstance()
                               .setOverlay(new ScreenOverlayHelper(MinecraftClient.getInstance(), ConfigScreenGenerator
                                       .getConfigBuilder().setParentScreen(null).build()) {
                               });
                LoggerUtils.info("[ChatTools] Command Executed: GUI opened");
                return Command.SINGLE_SUCCESS;
            }))
            // chattools on
            .then(literal("on").executes(t -> {
                ConfigUtils.set("general.ChatTools.Enabled", true);
                LoggerUtils.info("[ChatTools] Command Executed: Enabled ChatTools");
                MessageUtils.sendToActionbar(TextUtils.trans("texts.on"));
                return Command.SINGLE_SUCCESS;
            }))
            // chattools off
            .then(literal("off").executes(t -> {
                ConfigUtils.set("general.ChatTools.Enabled", false);
                LoggerUtils.info("[ChatTools] Command Executed: Disabled ChatTools");
                MessageUtils.sendToActionbar(TextUtils.trans("texts.off"));
                return Command.SINGLE_SUCCESS;
            }))
            // chattools get_message
            .then(literal("get_message").then(argument("hash",StringArgumentType.string()).executes(t -> {
                String hash = StringArgumentType.getString(t,"hash");
                LoggerUtils.info("[ChatTools] Getting message by hash: " + hash);
                TextUtils.MessageUnit messageUnit = TextUtils.getMessageMap(hash);
                if (messageUnit != null) {
                    LoggerUtils.info(String.format("Time:%d Text:%s", messageUnit.unixTimestamp, messageUnit.message));
                    MinecraftClient.getInstance().setScreen(new CopyFeatureScreen(messageUnit));
                } else {
                    Text errorText = ((MutableText) TextUtils.literal("[ChatTools] Failed to get message by hash: " + hash)).formatted(Formatting.RED);
                    LoggerUtils.error(errorText.getString());
                    MessageUtils.sendToNonPublicChat(errorText);
                }
                return Command.SINGLE_SUCCESS;
            })))
            // chattools config
            .then(literal("config")
                // chattools config openfile
                .then(literal("openfile").executes(t -> {
                    Util.getOperatingSystem().open(ConfigStorage.FILE);
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.requireRestart"));
                    return Command.SINGLE_SUCCESS;
                }))
                // chattools config get
                .then(literal("get")
                    // one arg
                    .then(argument("key", StringArgumentType.string()).executes(t -> {
                        String key = StringArgumentType.getString(t, "key");
                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.get", key, ConfigUtils.get(key)));
                        return Command.SINGLE_SUCCESS;
                    }))
                )
                // chattools config set
                .then(literal("set")
                    .then(argument("key", StringArgumentType.string())
                        .then(argument("value", StringArgumentType.string()).executes(t -> {
                            String key = StringArgumentType.getString(t, "key");
                            String value = StringArgumentType.getString(t, "value");
                            updateConfig(key, value);
                            return Command.SINGLE_SUCCESS;
                        }).then(argument("save", BoolArgumentType.bool()).executes(t -> {
                                String key = StringArgumentType.getString(t, "key");
                                String value = StringArgumentType.getString(t, "value");
                                updateConfig(key, value);
                                if (BoolArgumentType.getBool(t, "save")) {
                                    ConfigUtils.save();
                                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.saved"));
                                }
                                return Command.SINGLE_SUCCESS;
                        })))
                    )
                )
                // chattools config toggle
                .then(literal("toggle")
                        .then(argument("key", StringArgumentType.string())
                                .executes(t -> {
                                    String key = StringArgumentType.getString(t, "key");
                                    toggleBooleanConfig(key);
                                    return Command.SINGLE_SUCCESS;
                                }).then(argument("save", BoolArgumentType.bool()).executes(t -> {
                                    String key = StringArgumentType.getString(t, "key");
                                    toggleBooleanConfig(key);
                                    if (BoolArgumentType.getBool(t, "save")) {
                                        ConfigUtils.save();
                                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.saved"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        )
                )
            )
            // chattools regex_checker
            .then(literal("regex_checker")
                // one arg
                .then(argument("regex", StringArgumentType.string()).executes(t -> {
                        Pair<Boolean, String> result = checkRegex(StringArgumentType.getString(t, "regex"));
                        MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(result.getRight())).formatted(result.getLeft() ? Formatting.GREEN : Formatting.RED));
                        return Command.SINGLE_SUCCESS;
                    })
                    // two args
                    .then(argument("test_context", StringArgumentType.string()).executes(t -> {
                        String regex = StringArgumentType.getString(t, "regex");
                        String testContext = StringArgumentType.getString(t, "test_context");
                        Pair<Boolean, String> result = checkRegex(regex);
                        if (!result.getLeft()) {
                            MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(result.getRight())).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (Pattern.compile(regex).matcher(testContext).find()) {
                            MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(String.format("[ChatTools] Context [%s] could pass the RegEx test!", testContext))).formatted(Formatting.GREEN));
                        } else {
                            MessageUtils.sendToNonPublicChat(((MutableText) TextUtils.literal(String.format("[ChatTools] Context [%s] could NOT pass the RegEx test!", testContext))).formatted(Formatting.RED));
                        }
                        return Command.SINGLE_SUCCESS;
                    }))));
        // @formatter:on
    }

    public static Pair<Boolean, String> checkRegex(String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return new Pair<>(false, e.getMessage().replace("\r", ""));
        }
        return new Pair<>(true, "There's nothing wrong with the RegEx pattern.");
    }

    public static void toggleBooleanConfig(String key) {
        if (!ConfigScreenGenerator.configGuiMapInitialized || ConfigScreenGenerator.getKey2TypeMappings().isEmpty()) {
            ConfigScreenGenerator.getConfigBuilder(); // let Key2TypeMappings initialize
        }
        if (ConfigUtils.get(key) == null) {
            MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.toggle.error", key));
            return;
        }
        boolean now = (boolean) ConfigUtils.get(key);
        updateConfig(key, String.valueOf(!now));
    }

    public static void updateConfig(String key, String value) {
        if (!ConfigScreenGenerator.configGuiMapInitialized || ConfigScreenGenerator.getKey2TypeMappings().isEmpty()) {
            ConfigScreenGenerator.getConfigBuilder(); // let Key2TypeMappings initialize
        }
        try {
            if (!ConfigScreenGenerator.getKey2TypeMappings().containsKey(key)) {
                // if we don't have that key, we consider it as a string
                ConfigUtils.set(key, value);
                MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.warning", key));
            } else {
                switch (String.valueOf(ConfigScreenGenerator.getKey2TypeMappings().get(key))) {
                    case "boolean":
                        ConfigUtils.set(key, Boolean.parseBoolean(value));
                        break;
                    case "String":
                        ConfigUtils.set(key, String.valueOf(value));
                        break;
                    case "intSlider":
                    case "intField":
                        ConfigUtils.set(key, Integer.parseInt(value));
                        break;
                    case "doubleField":
                        ConfigUtils.set(key, Double.parseDouble(value));
                        break;
                    case "EnumToastModes":
                        ConfigUtils.set(key, SpecialUnits.ToastModes.valueOf(value));
                        break;
                    case "EnumKeyModifiers":
                        ConfigUtils.set(key, SpecialUnits.KeyModifiers.valueOf(value));
                        break;
                    case "FAQ":
                    case "sub":
                    case "keycode":
                    case "StringList":
                    case "FormatterList":
                    case "MacroList":
                    case "BubbleList":
                    case "ResponderList":
                    case "CustomJoinMessageList":
                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.unsupported", key));
                        // ignore it, so return in advance
                        return;
                    case "":
                    default:
                        ConfigUtils.set(key, String.valueOf(value));
                        MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set.warning", key));
                }
            }
            MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.config.set", key, ConfigUtils.get(key)));
        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.sendToNonPublicChat(TextUtils.literal(e.toString()).copy()
                                                      .setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
        }
    }
}
