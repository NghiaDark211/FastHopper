package com.nghiadark.fasthopper;

import com.nghiadark.fasthopper.commands.FastHopperCommand;
import com.nghiadark.fasthopper.listeners.HopperListener;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class FastHopper extends JavaPlugin implements Listener {

    private static FastHopper instance;
    private int transferAmount;
    private int transferCooldown;
    private UpdateChecker updateChecker;
    private Map<String, String> messagesCache;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        upgradeConfig();
        loadConfigValues();
        loadLanguage();

        getServer().getPluginManager().registerEvents(new HopperListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        FastHopperCommand command = new FastHopperCommand(this);
        getCommand("fasthopper").setExecutor(command);
        getCommand("fasthopper").setTabCompleter(command);

        updateChecker = new UpdateChecker(this);
        getServer().getGlobalRegionScheduler().runDelayed(this, scheduledTask -> {
            updateChecker.checkAsync();
        }, 60);

        getLogger().info("FastHopper has been enabled!");
        getLogger().info("Transfer amount: " + transferAmount + " items per transfer");
        getLogger().info("Transfer cooldown: " + transferCooldown + " ticks");
    }

    @Override
    public void onDisable() {
        getLogger().info("FastHopper has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("fasthopper.update")) return;

        getServer().getGlobalRegionScheduler().runDelayed(this, scheduledTask -> {
            if (updateChecker.isUpdateAvailable()) {
                player.sendMessage(updateChecker.getUpdateMessage(true));
            }
        }, 20);
    }

    private void upgradeConfig() {
        if (getConfig().contains("messages")) {
            getConfig().set("messages", null);
            if (!getConfig().contains("language")) {
                getConfig().set("language", "en");
            }
            saveConfig();
            getLogger().info("Upgraded config.yml to new format (language files)");
        }
    }

    public void loadConfigValues() {
        reloadConfig();
        transferAmount = getConfig().getInt("transfer-amount", 1);
        transferCooldown = getConfig().getInt("transfer-cooldown", 8);

        transferAmount = Math.max(1, Math.min(64, transferAmount));
        transferCooldown = Math.max(1, Math.min(100, transferCooldown));
    }

    public void loadLanguage() {
        messagesCache = new HashMap<>();
        String lang = getConfig().getString("language", "en");
        String fileName = "language/" + lang + ".yml";

        YamlConfiguration langConfig;
        try (InputStreamReader reader = new InputStreamReader(
                getResource(fileName), StandardCharsets.UTF_8)) {
            langConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            getLogger().warning("Could not load language file: " + fileName + ", falling back to en");
            try (InputStreamReader reader = new InputStreamReader(
                    getResource("language/en.yml"), StandardCharsets.UTF_8)) {
                langConfig = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception e2) {
                getLogger().severe("Could not load default language file!");
                return;
            }
        }

        for (String key : langConfig.getKeys(false)) {
            messagesCache.put(key, langConfig.getString(key, ""));
        }
    }

    public String getMessage(String path) {
        String prefix = getConfig().getString("prefix", "&6&l[FastHopper] &r");
        String message = messagesCache != null ? messagesCache.getOrDefault(path, "&cMessage not found: " + path) : "&cMessage not found: " + path;
        return colorize(prefix + message);
    }

    public String getRawMessage(String path) {
        String message = messagesCache != null ? messagesCache.getOrDefault(path, "&cMessage not found: " + path) : "&cMessage not found: " + path;
        return colorize(message);
    }

    public String colorize(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    public static FastHopper getInstance() {
        return instance;
    }

    public int getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(int amount) {
        this.transferAmount = Math.max(1, Math.min(64, amount));
        getConfig().set("transfer-amount", this.transferAmount);
        saveConfig();
    }

    public int getTransferCooldown() {
        return transferCooldown;
    }

    public void setTransferCooldown(int cooldown) {
        this.transferCooldown = Math.max(1, Math.min(100, cooldown));
        getConfig().set("transfer-cooldown", this.transferCooldown);
        saveConfig();
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
