package com.foolsmagic.customitems.items;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Like AbilityItem, but for weapons whose special ability triggers on landing a
 * melee hit rather than on right-click (e.g. a sword's on-hit effects). Automatically
 * respects SuppressionManager the same way AbilityItem does - if the Fool's Magic
 * card has the attacker suppressed, onHit() simply never gets called.
 */
public interface OnHitAbilityItem extends CustomItem {

    /** Called after a successful melee hit with this weapon, when the attacker is NOT suppressed. */
    void onHit(Player attacker, LivingEntity victim, EntityDamageByEntityEvent event, ItemStack stack);
}
