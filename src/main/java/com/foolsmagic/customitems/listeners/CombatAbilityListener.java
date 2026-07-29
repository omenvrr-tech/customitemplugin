package com.foolsmagic.customitems.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.foolsmagic.customitems.items.CustomItem;
import com.foolsmagic.customitems.items.ItemRegistry;
import com.foolsmagic.customitems.items.OnHitAbilityItem;
import com.foolsmagic.customitems.suppression.SuppressionManager;

public class CombatAbilityListener implements Listener {

    private final ItemRegistry registry;
    private final SuppressionManager suppressionManager;

    public CombatAbilityListener(ItemRegistry registry, SuppressionManager suppressionManager) {
        this.registry = registry;
        this.suppressionManager = suppressionManager;
    }

    // MONITOR so this runs after damage is finalized (and only if the hit wasn't cancelled).
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        CustomItem custom = registry.identify(weapon);
        if (!(custom instanceof OnHitAbilityItem ability)) return;

        if (suppressionManager.isSuppressed(attacker)) return;

        ability.onHit(attacker, victim, event, weapon);
    }
}
