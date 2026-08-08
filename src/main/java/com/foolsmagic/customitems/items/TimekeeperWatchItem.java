package com.foolsmagic.customitems.items;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import io.papermc.paper.datacomponent.DataComponentTypes;

import com.foolsmagic.customitems.effects.FreezeManager;
import com.foolsmagic.customitems.util.Keys;

/**
 * Timekeeper's Watch. Right-click: freeze every other living entity within 20 blocks
 * for 5 seconds while you move normally. 90 second cooldown. Freezing does not make
 * targets invulnerable - your attacks (and anyone/anything else's) still land on them
 * normally while they're frozen, they just can't move or act back.
 */
public class TimekeeperWatchItem implements AbilityItem, Indestructible {

    public static final String ID = "timekeeper_watch";

    private static final double FREEZE_RADIUS = 20.0;
    private static final long FREEZE_DURATION_TICKS = 100L; // 5 seconds
    private static final long COOLDOWN_MILLIS = 90_000L;    // 90 seconds

    private final Plugin plugin;
    private final FreezeManager freezeManager;

    public TimekeeperWatchItem(Plugin plugin, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.CLOCK);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text("Timekeeper's Watch", NamedTextColor.AQUA, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click: freeze everyone else within 20", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("blocks for 5 seconds. You move normally.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("They can still be hit while frozen.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Cooldown: 90s", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(Keys.ITEM_ID, PersistentDataType.STRING, ID);

        stack.setItemMeta(meta);
        stack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("blank:timekeeperwatch"));
        // See FoolsMagicItem.create()'s FALLBACK note if the line above doesn't compile.

        return stack;
    }

    @Override
    public void onAbilityActivate(Player player, ItemStack stack) {
        long remaining = getRemainingCooldownMillis(stack);
        if (remaining > 0) {
            player.sendActionBar(Component.text(
                    "Timekeeper's Watch is recharging (" + (remaining / 1000 + 1) + "s left)",
                    NamedTextColor.GRAY));
            return;
        }

        setLastUsed(stack, System.currentTimeMillis());
        // Visual cooldown swipe on the clock icon (purely cosmetic feedback).
        player.setCooldown(Material.CLOCK, (int) (COOLDOWN_MILLIS / 1000L * 20L));

        Location center = player.getLocation();
        for (LivingEntity entity : player.getWorld().getNearbyLivingEntities(center, FREEZE_RADIUS)) {
            if (entity.equals(player)) continue;
            freezeManager.freeze(entity, FREEZE_DURATION_TICKS);
        }

        center.getWorld().spawnParticle(Particle.END_ROD, center, 80, FREEZE_RADIUS / 3, 1, FREEZE_RADIUS / 3, 0.02);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 0.6f);
        player.sendActionBar(Component.text("Time stops around you...", NamedTextColor.AQUA));
    }

    private long getRemainingCooldownMillis(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0;
        Long lastUsed = meta.getPersistentDataContainer().get(Keys.TIMEKEEPER_LAST_USED, PersistentDataType.LONG);
        if (lastUsed == null) return 0;
        long elapsed = System.currentTimeMillis() - lastUsed;
        return Math.max(0, COOLDOWN_MILLIS - elapsed);
    }

    private void setLastUsed(ItemStack stack, long timestamp) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.TIMEKEEPER_LAST_USED, PersistentDataType.LONG, timestamp);
        stack.setItemMeta(meta);
    }
}
