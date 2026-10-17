package com.auracode.ishulker.listener;

import com.auracode.ishulker.IShulkerPlugin;
import com.auracode.ishulker.manager.ShulkerService;
import com.auracode.ishulker.model.ShulkerView;
import com.auracode.ishulker.util.Items;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * Blocks every known duplicate vector while a shulker is opened from the hand:
 * moving the source item, nesting shulkers, hotbar/offhand swaps, drops and
 * abrupt disconnects.
 */
public final class ShulkerGuardListener implements Listener {

    private final IShulkerPlugin plugin;
    private final ShulkerService service;

    public ShulkerGuardListener(IShulkerPlugin plugin, ShulkerService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryView view = event.getView();
        if (!(view.getTopInventory().getHolder() instanceof ShulkerView shulker)) {
            return;
        }
        Inventory top = view.getTopInventory();
        Inventory clicked = event.getClickedInventory();
        int topSize = top.getSize();
        int heldSlot = shulker.slot();

        if (clicked != null && clicked.equals(view.getBottomInventory()) && event.getRawSlot() - topSize == heldSlot) {
            event.setCancelled(true);
            return;
        }

        switch (event.getClick()) {
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                if (Items.isShulkerBox(event.getCurrentItem())) {
                    event.setCancelled(true);
                    return;
                }
            }
            case NUMBER_KEY -> {
                int button = event.getHotbarButton();
                if (button >= 0 && (button == heldSlot || Items.isShulkerBox(player.getInventory().getItem(button)))) {
                    event.setCancelled(true);
                    return;
                }
            }
            case SWAP_OFFHAND -> {
                if (clicked != null && clicked.equals(view.getTopInventory())
                        && Items.isShulkerBox(player.getInventory().getItemInOffHand())) {
                    event.setCancelled(true);
                    return;
                }
            }
            default -> {
            }
        }

        if (plugin.getSettings().preventNesting() && clicked != null && clicked.equals(view.getTopInventory())
                && Items.isShulkerBox(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        InventoryView view = event.getView();
        ShulkerView shulker = openView(view);
        if (shulker == null) {
            return;
        }
        int topSize = view.getTopInventory().getSize();
        boolean touchesTop = false;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                touchesTop = true;
            } else if (rawSlot - topSize == shulker.slot()) {
                event.setCancelled(true);
                return;
            }
        }
        if (plugin.getSettings().preventNesting() && touchesTop) {
            for (var item : event.getNewItems().values()) {
                if (Items.isShulkerBox(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (service.viewOf(event.getPlayer().getUniqueId()) == null) {
            return;
        }
        if (Items.isShulkerBox(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (service.viewOf(event.getPlayer().getUniqueId()) == null) {
            return;
        }
        if (Items.isShulkerBox(event.getMainHandItem()) || Items.isShulkerBox(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.persist(event.getPlayer());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (event.getInventory().getHolder() instanceof ShulkerView shulker) {
            service.handleClose(player, shulker);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        service.persist(event.getEntity());
    }

    private ShulkerView openView(InventoryView view) {
        Inventory top = view.getTopInventory();
        if (top.getHolder() instanceof ShulkerView shulker) {
            return shulker;
        }
        return null;
    }
}
