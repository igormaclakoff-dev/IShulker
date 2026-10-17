package com.auracode.ishulker.listener;

import com.auracode.ishulker.IShulkerPlugin;
import com.auracode.ishulker.manager.ShulkerService;
import com.auracode.ishulker.util.Items;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Opens a shulker box directly from the player's main hand.
 */
public final class ShulkerOpenListener implements Listener {

    private final IShulkerPlugin plugin;
    private final ShulkerService service;

    public ShulkerOpenListener(IShulkerPlugin plugin, ShulkerService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        boolean airClick = action == Action.RIGHT_CLICK_AIR;
        boolean blockClick = action == Action.RIGHT_CLICK_BLOCK;
        if (!airClick && !(blockClick && !plugin.getSettings().openOnAirOnly())) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.getSettings().requireSneak() && !player.isSneaking()) {
            return;
        }
        if (!service.canUse(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (!Items.isShulkerBox(item)) {
            return;
        }
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
        service.open(player, player.getInventory().getHeldItemSlot(), item);
    }
}
