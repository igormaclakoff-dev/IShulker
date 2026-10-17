package com.auracode.ishulker.model;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Marker holder for a shulker box that is currently opened from a player's
 * hand. The owner and the hotbar slot are remembered so the contents can be
 * written back into the very same item when the view is closed.
 */
public final class ShulkerView implements InventoryHolder {

    private final UUID ownerId;
    private final int slot;
    private Inventory inventory;

    public ShulkerView(UUID ownerId, int slot) {
        this.ownerId = ownerId;
        this.slot = slot;
    }

    /**
     * @return the player that opened this shulker
     */
    public UUID ownerId() {
        return ownerId;
    }

    /**
     * @return the hotbar slot that held the shulker when it was opened
     */
    public int slot() {
        return slot;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Links the created inventory to this holder.
     *
     * @param inventory the virtual inventory backing the view
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
