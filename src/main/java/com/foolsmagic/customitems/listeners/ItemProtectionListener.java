package com.foolsmagic.customitems.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.foolsmagic.customitems.items.CustomItem;
import com.foolsmagic.customitems.items.Indestructible;
import com.foolsmagic.customitems.items.ItemRegistry;

/**
 * Keeps any CustomItem marked Indestructible (e.g. the Fool's Magic card) alive
 * forever once it exists as a dropped item entity: immune to fire/lava/explosions,
 * and never despawns from the natural 5-minute item-despawn timer.
 *
 * Doesn't stop it from being picked up, moved between inventories, or (obviously)
 * falling into the void - there's no reasonable way to protect against that.
 */
public class ItemProtectionListener implements Listener {

    private final ItemRegistry registry;

    public ItemProtectionListener(ItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.ITEM) return;
        if (!isIndestructible(((Item) event.getEntity()).getItemStack())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (event.getEntityType() != EntityType.ITEM) return;
        if (!isIndestructible(((Item) event.getEntity()).getItemStack())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent event) {
        if (!isIndestructible(event.getEntity().getItemStack())) return;
        event.setCancelled(true);
    }

    private boolean isIndestructible(ItemStack stack) {
        CustomItem item = registry.identify(stack);
        return item instanceof Indestructible;
    }
}
