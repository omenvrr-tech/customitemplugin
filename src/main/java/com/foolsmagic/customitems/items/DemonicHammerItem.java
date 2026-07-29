package com.foolsmagic.customitems.items;

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
import org.bukkit.util.Vector;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import com.foolsmagic.customitems.util.Keys;

/**
 * EXAMPLE custom item with a special ability, wired into the same texture/model you
 * already made (assets/blank/items/demonichammer.json). This is just a template -
 * duplicate this class for each new special item you add. As long as it implements
 * AbilityItem and is registered in CustomItemsPlugin#onEnable, the Fool's Magic card
 * will automatically be able to suppress it.
 *
 * Ability here: right-click knocks back and damages nearby enemies in a small radius.
 * Swap out onAbilityActivate's body for whatever the hammer should actually do.
 */
public class DemonicHammerItem implements AbilityItem {

    public static final String ID = "demonic_hammer";
    private static final double ABILITY_RADIUS = 4.0;
    private static final double ABILITY_DAMAGE = 6.0;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text("Demonic Hammer", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(java.util.List.of(
                Component.text("Right-click: slam the ground, damaging", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("and knocking back nearby enemies.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(Keys.ITEM_ID, PersistentDataType.STRING, ID);
        stack.setItemMeta(meta);

        stack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("blank:demonichammer"));
        // See the FALLBACK note in FoolsMagicItem.create() if this line doesn't compile
        // against your Paper API version.

        return stack;
    }

    @Override
    public void onAbilityActivate(Player player, ItemStack stack) {
        Location center = player.getLocation();
        center.getWorld().spawnParticle(Particle.EXPLOSION, center, 1);
        center.getWorld().playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);

        for (LivingEntity entity : center.getWorld().getLivingEntities()) {
            if (entity.equals(player)) continue;
            if (entity.getLocation().distance(center) > ABILITY_RADIUS) continue;

            entity.damage(ABILITY_DAMAGE, player);
            Vector knockback = entity.getLocation().toVector()
                    .subtract(center.toVector())
                    .normalize()
                    .multiply(1.2)
                    .setY(0.5);
            entity.setVelocity(knockback);
        }
    }
}
