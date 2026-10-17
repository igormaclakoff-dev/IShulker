package com.auracode.ishulker.manager;

import com.auracode.ishulker.IShulkerPlugin;
import com.auracode.ishulker.model.ShulkerView;
import com.auracode.ishulker.util.Items;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core service: keeps track of every opened shulker, builds the virtual
 * inventory and writes the contents back into the source item. Contents are
 * also flushed periodically so a crash cannot duplicate items.
 */
public final class ShulkerService {

    private final IShulkerPlugin plugin;
    private final Map<UUID, ShulkerView> openViews = new ConcurrentHashMap<>();

    private volatile boolean enabled = true;
    private BukkitTask autosaveTask;

    public ShulkerService(IShulkerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the persisted feature state and (re)starts the autosave task.
     */
    public void loadState() {
        this.enabled = plugin.getSettings().enabled();
        restartAutosave();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    /**
     * @param player the player to test
     * @return whether the player may open a shulker right now
     */
    public boolean canUse(Player player) {
        if (!enabled) {
            return false;
        }
        String permission = plugin.getSettings().permission();
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    /**
     * @param ownerId the player id
     * @return the currently opened view, or {@code null}
     */
    public ShulkerView viewOf(UUID ownerId) {
        return openViews.get(ownerId);
    }

    /**
     * Opens the given source item in a virtual 27-slot inventory.
     *
     * @param player the viewer
     * @param slot   the hotbar slot holding the shulker
     * @param source the shulker item stack
     */
    public void open(Player player, int slot, ItemStack source) {
        if (openViews.containsKey(player.getUniqueId())) {
            return;
        }
        ItemMeta meta = source.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockMeta) || !(blockMeta.getBlockState() instanceof ShulkerBox box)) {
            return;
        }
        ShulkerView view = new ShulkerView(player.getUniqueId(), slot);
        Inventory inventory = Bukkit.createInventory(view, 27, resolveTitle(source));
        view.setInventory(inventory);
        inventory.setContents(box.getInventory().getContents());
        openViews.put(player.getUniqueId(), view);
        player.openInventory(inventory);
    }

    /**
     * Saves and forgets the view once its inventory is closed.
     *
     * @param player the viewer
     * @param view   the closed view
     */
    public void handleClose(Player player, ShulkerView view) {
        if (openViews.remove(view.ownerId(), view)) {
            save(player, view);
        }
    }

    /**
     * Saves the open view of a player, if any, without requiring a close event.
     * Used on quit and death.
     *
     * @param player the player
     */
    public void persist(Player player) {
        ShulkerView view = openViews.remove(player.getUniqueId());
        if (view != null) {
            save(player, view);
        }
    }

    /**
     * Flushes every open view and stops the autosave task. Called on disable.
     */
    public void shutdown() {
        stopAutosave();
        for (ShulkerView view : openViews.values()) {
            Player player = Bukkit.getPlayer(view.ownerId());
            if (player != null) {
                save(player, view);
            }
        }
        openViews.clear();
    }

    private void save(Player player, ShulkerView view) {
        Inventory inventory = view.getInventory();
        if (inventory == null) {
            return;
        }
        ItemStack current = player.getInventory().getItem(view.slot());
        if (!Items.isShulkerBox(current)) {
            return;
        }
        ItemMeta meta = current.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockMeta) || !(blockMeta.getBlockState() instanceof ShulkerBox box)) {
            return;
        }
        box.getInventory().setContents(inventory.getContents());
        blockMeta.setBlockState(box);
        current.setItemMeta(meta);
        player.getInventory().setItem(view.slot(), current);
    }

    private void restartAutosave() {
        stopAutosave();
        long interval = plugin.getSettings().autosaveIntervalTicks();
        autosaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::flushOpenViews, interval, interval);
    }

    private void stopAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
    }

    private void flushOpenViews() {
        for (ShulkerView view : openViews.values()) {
            Player player = Bukkit.getPlayer(view.ownerId());
            if (player == null) {
                continue;
            }
            if (needsFlush(player, view)) {
                save(player, view);
            }
        }
    }

    private boolean needsFlush(Player player, ShulkerView view) {
        Inventory inventory = view.getInventory();
        if (inventory == null) {
            return false;
        }
        ItemStack current = player.getInventory().getItem(view.slot());
        if (!Items.isShulkerBox(current)) {
            return false;
        }
        ItemMeta meta = current.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockMeta) || !(blockMeta.getBlockState() instanceof ShulkerBox box)) {
            return false;
        }
        return !Items.sameContents(box.getInventory().getContents(), inventory.getContents());
    }

    private Component resolveTitle(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component name = meta.displayName();
            if (name != null) {
                return name;
            }
        }
        return Component.text(prettify(item.getType()));
    }

    private String prettify(Material material) {
        String base = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (base.isEmpty()) {
            return base;
        }
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
