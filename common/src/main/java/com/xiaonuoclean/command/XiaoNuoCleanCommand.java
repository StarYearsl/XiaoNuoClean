package com.xiaonuoclean.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.xiaonuoclean.cleanup.CleanupScheduler;
import com.xiaonuoclean.config.CleanConfig;
import com.xiaonuoclean.config.CleanConfigManager;
import com.xiaonuoclean.i18n.LanguageManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XiaoNuoCleanCommand {
    private final CleanConfigManager configManager;
    private final CleanupScheduler scheduler;
    private final LanguageManager languageManager;

    public XiaoNuoCleanCommand(CleanConfigManager configManager, CleanupScheduler scheduler, LanguageManager languageManager) {
        this.configManager = configManager;
        this.scheduler = scheduler;
        this.languageManager = languageManager;
    }

    public static List<String> rootCommandNames() {
        return List.of("xiaonuoclean", "xnc");
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        for (String rootCommandName : rootCommandNames()) {
            dispatcher.register(commandRoot(rootCommandName, context));
        }
    }

    private LiteralArgumentBuilder<CommandSourceStack> commandRoot(String name, CommandBuildContext context) {
        return Commands.literal(name)
                .requires(XiaoNuoCleanCommand::canUse)
                .then(Commands.literal("status")
                        .executes(command -> status(command.getSource())))
                .then(Commands.literal("reload")
                        .executes(command -> reload(command.getSource())))
                .then(Commands.literal("clean")
                        .executes(command -> clean(command.getSource())))
                .then(Commands.literal("interval")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                                .executes(command -> setInterval(
                                        command.getSource(),
                                        IntegerArgumentType.getInteger(command, "seconds")
                                ))))
                .then(Commands.literal("warnings")
                        .then(Commands.literal("set")
                                .then(Commands.argument("seconds", StringArgumentType.greedyString())
                                        .executes(command -> setWarnings(
                                                command.getSource(),
                                                StringArgumentType.getString(command, "seconds")
                                        )))))
                .then(Commands.literal("whitelist")
                        .then(Commands.literal("set")
                                .executes(command -> setWhitelistFromMainHand(command.getSource())))
                        .then(Commands.literal("add")
                                .then(Commands.argument("item", ItemArgument.item(context))
                                        .executes(command -> addWhitelist(
                                                command.getSource(),
                                                itemId(command, "item")
                                        ))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("item", ItemArgument.item(context))
                                        .executes(command -> removeWhitelist(
                                                command.getSource(),
                                                itemId(command, "item")
                                        ))))
                        .then(Commands.literal("list")
                                .executes(command -> listWhitelist(command.getSource()))));
    }

    private static boolean canUse(CommandSourceStack source) {
        if (source.getPlayer() == null || source.permissions() == PermissionSet.ALL_PERMISSIONS) {
            return true;
        }

        return source.permissions() instanceof LevelBasedPermissionSet permissionSet
                && permissionSet.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);
    }

    private int status(CommandSourceStack source) {
        CleanConfig config = configManager.config();
        source.sendSuccess(() -> translated("command.status", Map.of(
                "intervalSeconds", config.intervalSeconds(),
                "remainingSeconds", scheduler.remainingSeconds(),
                "warningSeconds", config.warningSeconds(),
                "whitelistCount", config.whitelist().size()
        )), false);
        return 1;
    }

    private int reload(CommandSourceStack source) {
        configManager.load();
        languageManager.reload();
        scheduler.refresh();
        source.sendSuccess(() -> translated("command.reload"), false);
        return 1;
    }

    private int clean(CommandSourceStack source) {
        int cleanedCount = scheduler.cleanNow(source.getServer());
        source.sendSuccess(() -> translated("command.clean", Map.of("count", cleanedCount)), false);
        return 1;
    }

    private int setInterval(CommandSourceStack source, int seconds) {
        CleanConfig current = configManager.config();
        CleanConfig updated = new CleanConfig(current.language(), seconds, current.warningSeconds(), current.whitelist()).normalize();
        configManager.save(updated);
        scheduler.refresh();
        source.sendSuccess(() -> translated("command.interval.set", Map.of("seconds", updated.intervalSeconds())), true);
        return 1;
    }

    private int setWarnings(CommandSourceStack source, String secondsText) {
        List<Integer> warnings = parseWarningSeconds(secondsText);
        if (warnings.isEmpty()) {
            source.sendFailure(translated("command.warnings.invalid"));
            return 0;
        }

        CleanConfig current = configManager.config();
        CleanConfig updated = new CleanConfig(current.language(), current.intervalSeconds(), warnings, current.whitelist()).normalize();
        configManager.save(updated);
        scheduler.refresh();
        source.sendSuccess(() -> translated("command.warnings.set", Map.of("warningSeconds", updated.warningSeconds())), true);
        return 1;
    }

    private int setWhitelistFromMainHand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(translated("command.whitelist.player_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(translated("command.whitelist.empty_hand"));
            return 0;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return addWhitelist(source, itemId);
    }

    private int addWhitelist(CommandSourceStack source, String itemId) {
        CleanConfig current = configManager.config();
        List<String> whitelist = current.mutableWhitelist();

        if (!whitelist.contains(itemId)) {
            whitelist.add(itemId);
        }

        CleanConfig updated = new CleanConfig(current.language(), current.intervalSeconds(), current.warningSeconds(), whitelist).normalize();
        configManager.save(updated);
        scheduler.refresh();
        source.sendSuccess(() -> translated("command.whitelist.added", Map.of("item", itemId)), true);
        return 1;
    }

    private int removeWhitelist(CommandSourceStack source, String itemId) {
        CleanConfig current = configManager.config();
        List<String> whitelist = current.mutableWhitelist();
        boolean removed = whitelist.remove(itemId);

        CleanConfig updated = new CleanConfig(current.language(), current.intervalSeconds(), current.warningSeconds(), whitelist).normalize();
        configManager.save(updated);
        scheduler.refresh();

        if (removed) {
            source.sendSuccess(() -> translated("command.whitelist.removed", Map.of("item", itemId)), true);
            return 1;
        }

        source.sendFailure(translated("command.whitelist.missing", Map.of("item", itemId)));
        return 0;
    }

    private int listWhitelist(CommandSourceStack source) {
        List<String> whitelist = configManager.config().whitelist();
        source.sendSuccess(() -> whitelist.isEmpty()
                ? translated("command.whitelist.empty")
                : translated("command.whitelist.list", Map.of("items", String.join(", ", whitelist))), false);
        return 1;
    }

    private Component translated(String key) {
        return translated(key, Map.of());
    }

    private Component translated(String key, Map<String, ?> placeholders) {
        return Component.literal(languageManager.translate(configManager.config().language(), key, placeholders));
    }

    static List<Integer> parseWarningSeconds(String secondsText) {
        List<Integer> warnings = new ArrayList<>();

        for (String part : secondsText.trim().split("\\s+")) {
            try {
                int seconds = Integer.parseInt(part);
                if (seconds > 0) {
                    warnings.add(seconds);
                }
            } catch (NumberFormatException ignored) {
                return List.of();
            }
        }

        return warnings;
    }

    private static String itemId(com.mojang.brigadier.context.CommandContext<CommandSourceStack> command, String name) {
        return BuiltInRegistries.ITEM.getKey(ItemArgument.getItem(command, name).item().value()).toString();
    }
}
