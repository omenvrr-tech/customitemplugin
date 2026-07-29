package com.foolsmagic.customitems.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import com.foolsmagic.customitems.CustomItemsPlugin;
import com.foolsmagic.customitems.util.Keys;

/**
 * The Fool's Magic card.
 *
 * - Has NO ability of its own (does not implement AbilityItem), so it is never
 *   suppressed by itself or by another Fool's Magic card.
 * - Right-clicking it cycles its aura radius through the values in config.yml
 *   (default: off -> 5 -> 10 -> 25 -> off). You must be holding it to right-click it,
 *   but once set, the aura is active from ANYWHERE in your inventory - hotbar, main
 *   inventory, or offhand - it doesn't need to stay in your hand.
 * - The radius is stored ON THE ITEM STACK ITSELF, so different cards can have
 *   different settings and it survives across sessions/trades.
 * - While the radius is > 0 AND the holder is alive, SuppressionManager treats the
 *   holder AND every player within that many blocks as suppressed for every
 *   AbilityItem. The aura automatically pauses the instant the holder dies, and
 *   resumes on respawn (see getActiveRadius's isDead() check).
 * - Implements Indestructible: as a dropped item entity it can't be destroyed by
 *   fire/lava/explosions and never despawns (see ItemProtectionListener). It's also
 *   flagged Unbreakable so it can't lose durability.
 */
public class FoolsMagicItem implements CustomItem, Indestructible {

    public static final String ID = "fools_magic";

    private final CustomItemsPlugin plugin;

    public FoolsMagicItem(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text("The Fool", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        meta.setUnbreakable(true);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.ITEM_ID, PersistentDataType.STRING, ID);
        pdc.set(Keys.FOOL_RADIUS, PersistentDataType.INTEGER, 0);

        stack.setItemMeta(meta);
        applyLore(stack, 0);

        // Points this item at assets/blank/items/foolsmagic.json in your resource pack,
        // which in turn points at assets/blank/models/item/foolsmagic.json.
        // Requires a 1.21.4+ server/resource pack (the new item-model component system).
        stack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("blank:foolsmagic"));

        // FALLBACK: if your Paper version/API doesn't have DataComponentTypes.ITEM_MODEL
        // (older than 1.21.4), delete the line above and use the classic approach instead:
        //   meta.setCustomModelData(1);
        // and switch your resource pack's model to the old overrides system
        // (a base vanilla item model with "overrides": [{"predicate": {"custom_model_data": 1}, "model": "blank:item/foolsmagic"}])
        // instead of the assets/<ns>/items/*.json files.

        return stack;
    }

    /** Reads the radius stored on a given Fool's Magic stack. Returns 0 if unset/not a Fool card. */
    public static int getRadius(ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null) return 0;
        Integer radius = stack.getItemMeta().getPersistentDataContainer()
                .get(Keys.FOOL_RADIUS, PersistentDataType.INTEGER);
        return radius == null ? 0 : radius;
    }

    /**
     * Highest active radius across every Fool's Magic card a player is carrying
     * (hotbar, main inventory, offhand, armor). 0 if they have none active.
     */
    public static int getActiveRadius(Player player, ItemRegistry registry) {
        // Aura pauses entirely while the holder is dead (between death and respawn).
        if (player.isDead()) return 0;

        int highest = 0;
        PlayerInventory inv = player.getInventory();
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) continue;
            CustomItem item = registry.identify(stack);
            if (!(item instanceof FoolsMagicItem)) continue;
            highest = Math.max(highest, getRadius(stack));
        }
        ItemStack offhand = inv.getItemInOffHand();
        if (registry.identify(offhand) instanceof FoolsMagicItem) {
            highest = Math.max(highest, getRadius(offhand));
        }
        return highest;
    }

    /** Advances this specific card's radius to the next value in the config cycle and notifies the player. */
    public void cycleRadius(Player player, ItemStack stack) {
        List<Integer> cycle = plugin.getConfig().getIntegerList("radius-cycle");
        if (cycle.isEmpty()) cycle = List.of(0, 5, 10, 25);

        int current = getRadius(stack);
        int currentIndex = cycle.indexOf(current);
        int nextIndex = (currentIndex + 1) % cycle.size();
        int next = cycle.get(nextIndex);

        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.FOOL_RADIUS, PersistentDataType.INTEGER, next);
        stack.setItemMeta(meta);
        applyLore(stack, next);

        String key = next == 0 ? "radius-off" : "radius-changed";
        String message = plugin.getConfig().getString("messages." + key, "")
                .replace("%radius%", next == 0 ? "off" : next + " blocks");
        player.sendActionBar(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacyAmpersand().deserialize(message));
    }

    private void applyLore(ItemStack stack, int radius) {
        ItemMeta meta = stack.getItemMeta();
        String radiusText = radius == 0 ? "Off" : radius + " blocks";
        meta.lore(List.of(
                Component.text("Suppresses every special item ability", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("within range of the holder (not this card).", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Aura radius: ", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(radiusText, NamedTextColor.LIGHT_PURPLE)),
                Component.text("Right-click to change", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        stack.setItemMeta(meta);
    }
}
