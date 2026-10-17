package com.auracode.ishulker.config;

import com.auracode.ishulker.IShulkerPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed access to {@code config.yml}. Values are read lazily so a reload is
 * picked up immediately.
 */
public final class PluginConfig {

    private final IShulkerPlugin plugin;

    public PluginConfig(IShulkerPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public boolean enabled() {
        return raw().getBoolean("enabled", true);
    }

    public boolean requireSneak() {
        return raw().getBoolean("settings.require-sneak", true);
    }

    public boolean openOnAirOnly() {
        return raw().getBoolean("settings.open-on-air-only", true);
    }

    public String permission() {
        return raw().getString("settings.permission", "ishulker.use");
    }

    public boolean preventNesting() {
        return raw().getBoolean("settings.prevent-nesting", true);
    }

    public long autosaveIntervalTicks() {
        return Math.max(1L, raw().getLong("settings.autosave-interval-ticks", 20L));
    }

    public String message(String path, String fallback) {
        return raw().getString("messages." + path, fallback);
    }

    private FileConfiguration raw() {
        return plugin.getConfig();
    }
}
