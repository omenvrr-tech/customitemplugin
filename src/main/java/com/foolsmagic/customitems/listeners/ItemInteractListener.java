package com.foolsmagic.customitems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import com.foolsmagic.customitems.CustomItemsPlugin;
import com.foolsmagic.customitems.items.AbilityItem;
import com.foolsmagic.customitems.items.CustomItem;
import com.foolsmagic.customitems.items.FoolsMagicItem;
import com.foolsmagic.customitems.items.ItemRegistry;
import com.foolsmagic.customitems.suppression.SuppressionManager;

public class ItemInteractListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ItemRegistry registry;
    private final SuppressionManager suppressionManager;

    public ItemInteractListener(CustomItemsPlugin plugin, ItemRegistry registry, SuppressionManager suppressionManager) {
        this.plugin = plugin;
        this.registry = registry;
        this.suppressionManager = suppressionManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only handle main-hand right clicks, and only once per interaction (ignore the off-hand echo).
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        CustomItem custom = registry.identify(item);
        if (custom == null) return;

        Player player = event.getPlayer();

        if (custom instanceof FoolsMagicItem fools) {
            event.setCancelled(true);
            fools.cycleRadius(player, item);
            return;
        }

        if (custom instanceof AbilityItem ability) {
            event.setCancelled(true);

            if (suppressionManager.isSuppressed(player)) {
                String message = plugin.getConfig().getString("messages.ability-suppressed", "");
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
                return;
            }

            ability.onAbilityActivate(player, item);
        }
    }
}
