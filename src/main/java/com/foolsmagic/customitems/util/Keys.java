package com.foolsmagic.customitems.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * Central place for every PersistentDataContainer key the plugin uses.
 * Call {@link #init(Plugin)} once from onEnable before anything else touches these.
 */
public final class Keys {

    private Keys() {}

    /** Stores which registered CustomItem an ItemStack is (e.g. "fools_magic", "demonic_hammer"). */
    public static NamespacedKey ITEM_ID;

    /** Stores the Fool's Magic card's current aura radius (0 = off). */
    public static NamespacedKey FOOL_RADIUS;

    /** Stores how many hits the Voidreaver has landed, for the every-4th-hit Void Slash. */
    public static NamespacedKey VOIDREAVER_HITS;

    /** Stores the last-used timestamp (millis) for the Timekeeper's Watch cooldown. */
    public static NamespacedKey TIMEKEEPER_LAST_USED;

    public static void init(Plugin plugin) {
        ITEM_ID = new NamespacedKey(plugin, "item_id");
        FOOL_RADIUS = new NamespacedKey(plugin, "fool_radius");
        VOIDREAVER_HITS = new NamespacedKey(plugin, "voidreaver_hits");
        TIMEKEEPER_LAST_USED = new NamespacedKey(plugin, "timekeeper_last_used");
    }
}
