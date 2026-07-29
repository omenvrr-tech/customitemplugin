package com.foolsmagic.customitems.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Any CustomItem that also implements this has a right-click "special ability".
 * The listener automatically checks SuppressionManager before calling onAbilityActivate,
 * so you never need to check suppression yourself inside the ability code -
 * if this method runs, the aura has already given the green light.
 *
 * To add a new special-ability item later: implement CustomItem + AbilityItem,
 * register it in CustomItemsPlugin#onEnable, done. The Fool's Magic card will
 * automatically be able to suppress it - no extra wiring needed.
 */
public interface AbilityItem extends CustomItem {

    /** Called when the holder right-clicks with this item and is NOT currently suppressed. */
    void onAbilityActivate(Player player, ItemStack stack);
}
