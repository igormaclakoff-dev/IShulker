package com.auracode.ishulker;

import com.auracode.ishulker.command.IShulkerCommand;
import com.auracode.ishulker.command.IShulkerTabCompleter;
import com.auracode.ishulker.config.Messages;
import com.auracode.ishulker.config.PluginConfig;
import com.auracode.ishulker.listener.ShulkerGuardListener;
import com.auracode.ishulker.listener.ShulkerOpenListener;
import com.auracode.ishulker.manager.ShulkerService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point of IShulker: wires the configuration, service, listeners and
 * the administration command together.
 */
public final class IShulkerPlugin extends JavaPlugin {

    private PluginConfig settings;
    private Messages messages;
    private ShulkerService shulkerService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.settings = new PluginConfig(this);
        this.messages = new Messages(this);
        this.shulkerService = new ShulkerService(this);
        this.shulkerService.loadState();

        getServer().getPluginManager().registerEvents(new ShulkerOpenListener(this, shulkerService), this);
        getServer().getPluginManager().registerEvents(new ShulkerGuardListener(this, shulkerService), this);

        PluginCommand command = getCommand("ishulker");
        if (command != null) {
            command.setExecutor(new IShulkerCommand(this));
            command.setTabCompleter(new IShulkerTabCompleter());
        } else {
            getLogger().warning("Команда /ishulker не объявлена в plugin.yml.");
        }

        getLogger().info("IShulker включён (от AuraCode).");
    }

    @Override
    public void onDisable() {
        if (shulkerService != null) {
            shulkerService.shutdown();
        }
        getLogger().info("IShulker выключен.");
    }

    /**
     * Reloads config.yml and resets the feature state from the configuration.
     */
    public void reloadAll() {
        settings.reload();
        shulkerService.loadState();
    }

    public PluginConfig getSettings() {
        return settings;
    }

    public Messages getMessages() {
        return messages;
    }

    public ShulkerService getShulkerService() {
        return shulkerService;
    }
}
