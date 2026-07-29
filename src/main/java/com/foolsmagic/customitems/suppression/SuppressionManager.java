package com.foolsmagic.customitems.suppression;

import org.bukkit.entity.Player;

import com.foolsmagic.customitems.items.FoolsMagicItem;
import com.foolsmagic.customitems.items.ItemRegistry;

/**
 * Decides whether a given player is currently "suppressed" - i.e. whether their
 * AbilityItem right-clicks should silently fizzle instead of firing.
 *
 * A player is suppressed if ANY online player has an active (radius > 0) Fool's
 * Magic card and that player is either:
 *   - the target themself (the card holder's own other special items are always
 *     suppressed while the card is active), or
 *   - within that card's radius of the target.
 */
public class SuppressionManager {

    private final ItemRegistry registry;

    public SuppressionManager(ItemRegistry registry) {
        this.registry = registry;
    }

    public boolean isSuppressed(Player target) {
        for (Player holder : target.getWorld().getPlayers()) {
            int radius = FoolsMagicItem.getActiveRadius(holder, registry);
            if (radius <= 0) continue;

            if (holder.equals(target)) {
                return true;
            }
            if (holder.getLocation().distance(target.getLocation()) <= radius) {
                return true;
            }
        }

        // Also check players in other worlds who might not matter (radius is a physical
        // block distance, so cross-world checks are skipped - remove this comment/adjust
        // if you want a cross-world "same server" aura instead).
        return false;
    }
}
