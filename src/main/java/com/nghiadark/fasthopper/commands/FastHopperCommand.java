package com.nghiadark.fasthopper.commands;

import com.nghiadark.fasthopper.FastHopper;
import com.nghiadark.fasthopper.UpdateChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FastHopperCommand implements CommandExecutor, TabCompleter {

    private final FastHopper plugin;

    public FastHopperCommand(FastHopper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("fasthopper.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "set" -> handleSet(sender, args);
            case "info" -> handleInfo(sender);
            case "update" -> handleUpdate(sender);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("fasthopper.reload")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return;
        }

        plugin.loadConfigValues();
        sender.sendMessage(plugin.getMessage("reload-success"));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fasthopper.set")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sendHelp(sender);
            return;
        }

        String type = args[1].toLowerCase();
        String valueStr = args[2];

        int value;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("invalid-number"));
            return;
        }

        switch (type) {
            case "amount" -> {
                if (value < 1 || value > 64) {
                    sender.sendMessage(plugin.getMessage("invalid-range-amount"));
                    return;
                }
                plugin.setTransferAmount(value);
                sender.sendMessage(plugin.getMessage("set-amount-success")
                        .replace("%amount%", String.valueOf(value)));
            }
            case "cooldown" -> {
                if (value < 1 || value > 100) {
                    sender.sendMessage(plugin.getMessage("invalid-range-cooldown"));
                    return;
                }
                plugin.setTransferCooldown(value);
                sender.sendMessage(plugin.getMessage("set-cooldown-success")
                        .replace("%cooldown%", String.valueOf(value)));
            }
            default -> sendHelp(sender);
        }
    }

    private void handleInfo(CommandSender sender) {
        if (!sender.hasPermission("fasthopper.info")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return;
        }

        String info = plugin.getRawMessage("info");
        String[] lines = plugin.colorize(info).split("\n");

        for (String line : lines) {
            sender.sendMessage(line
                    .replace("%amount%", String.valueOf(plugin.getTransferAmount()))
                    .replace("%cooldown%", String.valueOf(plugin.getTransferCooldown())));
        }
    }

    private void handleUpdate(CommandSender sender) {
        UpdateChecker uc = plugin.getUpdateChecker();
        if (uc == null || !uc.isChecked()) {
            sender.sendMessage(plugin.getMessage("update-checking"));
            return;
        }
        sender.sendMessage(uc.getUpdateMessage(true));
    }

    private void sendHelp(CommandSender sender) {
        String help = plugin.getRawMessage("help");
        String[] lines = plugin.colorize(help).split("\n");

        for (String line : lines) {
            sender.sendMessage(line);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("fasthopper.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reload", "info", "update", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("amount", "cooldown"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (args[1].equalsIgnoreCase("amount")) {
                completions.addAll(Arrays.asList("1", "2", "4", "8", "16", "32", "64"));
            } else if (args[1].equalsIgnoreCase("cooldown")) {
                completions.addAll(Arrays.asList("1", "2", "4", "8", "10", "20"));
            }
        }

        // Filter by what the user has typed so far
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .collect(Collectors.toList());
    }
}
