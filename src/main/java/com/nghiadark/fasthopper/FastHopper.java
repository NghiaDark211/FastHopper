package com.nghiadark.fasthopper;

import com.nghiadark.fasthopper.commands.FastHopperCommand;
import com.nghiadark.fasthopper.listeners.HopperListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class FastHopper extends JavaPlugin {

    private static FastHopper instance;
    private int transferAmount;
    private int transferCooldown;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();
        loadConfigValues();

        // Register listener
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);

        // Register command
        FastHopperCommand command = new FastHopperCommand(this);
        getCommand("fasthopper").setExecutor(command);
        getCommand("fasthopper").setTabCompleter(command);

        getLogger().info("FastHopper has been enabled!");
        getLogger().info("Transfer amount: " + transferAmount + " items per transfer");
        getLogger().info("Transfer cooldown: " + transferCooldown + " ticks");
    }

    @Override
    public void onDisable() {
        getLogger().info("FastHopper has been disabled!");
    }

    /**
     * Load values from config.yml into memory
     */
    public void loadConfigValues() {
        reloadConfig();
        transferAmount = getConfig().getInt("transfer-amount", 1);
        transferCooldown = getConfig().getInt("transfer-cooldown", 8);

        // Clamp values
        transferAmount = Math.max(1, Math.min(64, transferAmount));
        transferCooldown = Math.max(1, Math.min(100, transferCooldown));
    }

    /**
     * Get a formatted message from config with prefix
     */
    public String getMessage(String path) {
        String prefix = getConfig().getString("prefix", "&6&l[FastHopper] &r");
        String message = getConfig().getString("messages." + path, "&cMessage not found: " + path);
        return colorize(prefix + message);
    }

    /**
     * Get a raw message from config (no prefix)
     */
    public String getRawMessage(String path) {
        String message = getConfig().getString("messages." + path, "&cMessage not found: " + path);
        return colorize(message);
    }

    /**
     * Translate color codes using legacy '&' format
     */
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
}
