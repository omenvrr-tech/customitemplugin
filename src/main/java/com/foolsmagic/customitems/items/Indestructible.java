package com.foolsmagic.customitems.items;

/**
 * Mark a CustomItem with this if it should be indestructible as a physical item:
 * - Never takes damage from fire/lava/explosions while on the ground.
 * - Never despawns while on the ground.
 * - Is flagged Unbreakable (no durability loss) when created.
 *
 * Doesn't stop a player from dying and dropping it (that's normal death behavior /
 * server config like keepInventory) - it just means the item entity itself can't be
 * destroyed once it exists, and will sit there forever until someone picks it up.
 */
public interface Indestructible {
}
