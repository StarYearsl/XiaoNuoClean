package com.xiaonuoclean.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.xiaonuoclean.cleanup.CleanupScheduler;
import com.xiaonuoclean.config.CleanConfig;
import com.xiaonuoclean.config.CleanConfigManager;
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

public class XiaoNuoCleanCommand {
    private final CleanConfigManager configManager;
    private final CleanupScheduler scheduler;

    public XiaoNuoCleanCommand(CleanConfigManager configManager, CleanupScheduler scheduler) {
        this.configManager = configManager;
        this.scheduler = scheduler;
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
        source.sendSuccess(() -> Component.literal(
                "[小诺清理] 清理间隔: " + config.intervalSeconds() + " 秒，距离下次清理: "
                        + scheduler.remainingSeconds() + " 秒，提示秒数: " + config.warningSeconds()
                        + "，白名单数量: " + config.whitelist().size()
        ), false);
        return 1;
    }

    private int reload(CommandSourceStack source) {
        configManager.load();
        scheduler.refresh();
        source.sendSuccess(() -> Component.literal("[小诺清理] 配置已重载。"), false);
        return 1;
    }

    private int clean(CommandSourceStack source) {
        int cleanedCount = scheduler.cleanNow(source.getServer());
        source.sendSuccess(() -> Component.literal("[小诺清理] 已手动清理 " + cleanedCount + " 个掉落物。"), false);
        return 1;
    }

    private int setInterval(CommandSourceStack source, int seconds) {
        CleanConfig current = configManager.config();
        CleanConfig updated = new CleanConfig(seconds, current.warningSeconds(), current.whitelist()).normalize();
        configManager.save(updated);
        scheduler.refresh();
        source.sendSuccess(() -> Component.literal("[小诺清理] 清理间隔已设置为 " + updated.intervalSeconds() + " 秒。"), true);
        return 1;
    }

    private int setWarnings(CommandSourceStack source, String secondsText) {
        List<Integer> warnings = parseWarningSeconds(secondsText);
        if (warnings.isEmpty()) {
            source.sendFailure(Component.literal("[小诺清理] 至少需要提供一个有效的正整数提示秒数。"));
            return 0;
        }

        CleanConfig current = configManager.config();
        CleanConfig updated = new CleanConfig(current.intervalSeconds(), warnings, current.whitelist()).normalize();
        configManager.save(updated);
        scheduler.refresh();
        source.sendSuccess(() -> Component.literal("[小诺清理] 提示秒数已设置为 " + updated.warningSeconds() + "。"), true);
        return 1;
    }

    private int setWhitelistFromMainHand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("[小诺清理] 该指令只能由游戏内管理员玩家执行。"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("[小诺清理] 主手没有物品。"));
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

        CleanConfig updated = new CleanConfig(current.intervalSeconds(), current.warningSeconds(), whitelist).normalize();
        configManager.save(updated);
        scheduler.refresh();
        source.sendSuccess(() -> Component.literal("[小诺清理] 已添加白名单物品: " + itemId), true);
        return 1;
    }

    private int removeWhitelist(CommandSourceStack source, String itemId) {
        CleanConfig current = configManager.config();
        List<String> whitelist = current.mutableWhitelist();
        boolean removed = whitelist.remove(itemId);

        CleanConfig updated = new CleanConfig(current.intervalSeconds(), current.warningSeconds(), whitelist).normalize();
        configManager.save(updated);
        scheduler.refresh();

        if (removed) {
            source.sendSuccess(() -> Component.literal("[小诺清理] 已移除白名单物品: " + itemId), true);
            return 1;
        }

        source.sendFailure(Component.literal("[小诺清理] 白名单中没有物品: " + itemId));
        return 0;
    }

    private int listWhitelist(CommandSourceStack source) {
        List<String> whitelist = configManager.config().whitelist();
        String message = whitelist.isEmpty() ? "[小诺清理] 白名单为空。" : "[小诺清理] 白名单: " + String.join(", ", whitelist);
        source.sendSuccess(() -> Component.literal(message), false);
        return 1;
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
