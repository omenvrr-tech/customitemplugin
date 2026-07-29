package com.foolsmagic.customitems.items;

import org.bukkit.inventory.ItemStack;

import com.foolsmagic.customitems.util.Keys;

/**
 * Every custom item (the Fool's Magic card, the Demonic Hammer, and anything you add later)
 * implements this. Register your item with ItemRegistry in CustomItemsPlugin#onEnable.
 */
public interface CustomItem {

    /** Unique id, e.g. "fools_magic". Used as the value stored in Keys.ITEM_ID. */
    String id();

    /** Builds a fresh copy of this item (used by /giveitem and anywhere else you spawn one in). */
    ItemStack create();

    /** True if this stack is (a copy of) this custom item. Default checks the id tag - you shouldn't need to override this. */
    default boolean matches(ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null) return false;
        String storedId = stack.getItemMeta().getPersistentDataContainer()
                .get(Keys.ITEM_ID, org.bukkit.persistence.PersistentDataType.STRING);
        return id().equals(storedId);
    }
}
