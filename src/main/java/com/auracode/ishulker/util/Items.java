package com.auracode.ishulker.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

/**
 * Item related helpers.
 */
public final class Items {

    private Items() {
    }

    /**
     * Checks whether the given stack is any kind of shulker box.
     *
     * @param item the stack to test, may be {@code null}
     * @return {@code true} when the stack is a shulker box
     */
    public static boolean isShulkerBox(ItemStack item) {
        return item != null && item.getType() != Material.AIR && Tag.SHULKER_BOXES.isTagged(item.getType());
    }

    /**
     * Compares two stacks by type, amount and metadata.
     *
     * @param first  first stack, may be {@code null}
     * @param second second stack, may be {@code null}
     * @return {@code true} when both stacks represent the same item
     */
    public static boolean sameItem(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.getAmount() == second.getAmount() && first.isSimilar(second);
    }

    /**
     * Compares two inventory content arrays slot by slot.
     *
     * @param first  first contents, never {@code null}
     * @param second second contents, never {@code null}
     * @return {@code true} when every slot holds an equal stack
     */
    public static boolean sameContents(ItemStack[] first, ItemStack[] second) {
        int shared = Math.min(first.length, second.length);
        for (int index = 0; index < shared; index++) {
            if (!sameItem(first[index], second[index])) {
                return false;
            }
        }
        for (int index = shared; index < first.length; index++) {
            if (first[index] != null) {
                return false;
            }
        }
        for (int index = shared; index < second.length; index++) {
            if (second[index] != null) {
                return false;
            }
        }
        return true;
    }
}
