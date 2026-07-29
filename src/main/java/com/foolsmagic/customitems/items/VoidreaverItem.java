package com.foolsmagic.customitems.items;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import io.papermc.paper.datacomponent.DataComponentTypes;

import com.foolsmagic.customitems.util.Keys;

/**
 * Legendary sword. Base damage/speed is whatever the underlying material (Netherite
 * Sword) normally has - per spec, this does NOT add the extra damage/strength/crit
 * stat block from the reference sheet, only the three named on-hit abilities:
 *
 * - Void Slash: every 4th hit sends out a wave dealing 150% of that hit's damage to
 *   everything else near the victim.
 * - Life Steal V: heals the attacker for 5% of the damage dealt, every hit.
 * - Ender's Grasp: 20% chance per hit to reduce the victim's armor by 25% for 5s.
 */
public class VoidreaverItem implements OnHitAbilityItem, Indestructible {

    public static final String ID = "voidreaver";

    private static final double VOID_SLASH_RADIUS = 4.0;
    private static final double VOID_SLASH_MULTIPLIER = 1.5;
    private static final double LIFESTEAL_PERCENT = 0.05;
    private static final double ENDERS_GRASP_CHANCE = 0.20;
    private static final double ENDERS_GRASP_ARMOR_REDUCTION = -0.25; // multiply_scalar_1
    private static final long ENDERS_GRASP_DURATION_TICKS = 100L; // 5 seconds

    private final Plugin plugin;
    private final java.util.Random random = new java.util.Random();

    public VoidreaverItem(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ItemStack create() {
        // Netherite Sword as the base (closest vanilla equivalent to the
        // "Wither Skeleton Sword" ingredient/base referenced in the concept art).
        ItemStack stack = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text("Voidreaver", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Legendary Sword", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Void Slash", NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Every 4th hit unleashes a wave of void", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("energy, dealing 150% damage to nearby foes.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Life Steal V", NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Heals for 5% of damage dealt.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Ender's Grasp", NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("20% chance to weaken enemies, reducing", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("their armor by 25% for 5 seconds.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("\"There is no escape. Only oblivion.\"", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(Keys.ITEM_ID, PersistentDataType.STRING, ID);
        meta.getPersistentDataContainer().set(Keys.VOIDREAVER_HITS, PersistentDataType.INTEGER, 0);

        stack.setItemMeta(meta);
        stack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("blank:voidreaver"));
        // See FoolsMagicItem.create()'s FALLBACK note if the line above doesn't compile
        // against your Paper API version.

        return stack;
    }

    @Override
    public void onHit(Player attacker, LivingEntity victim, EntityDamageByEntityEvent event, ItemStack stack) {
        double damageDealt = event.getFinalDamage();

        // --- Life Steal V: heal 5% of the damage dealt, every hit ---
        double healAmount = damageDealt * LIFESTEAL_PERCENT;
        double newHealth = Math.min(attacker.getHealth() + healAmount,
                attacker.getAttribute(Attribute.MAX_HEALTH).getValue());
        attacker.setHealth(newHealth);

        // --- Ender's Grasp: 20% chance to reduce the victim's armor for 5s ---
        if (random.nextDouble() < ENDERS_GRASP_CHANCE) {
            applyEndersGrasp(victim);
        }

        // --- Void Slash: every 4th hit, AoE wave around the victim ---
        int hits = getHitCount(stack) + 1;
        setHitCount(stack, hits);

        if (hits % 4 == 0) {
            triggerVoidSlash(attacker, victim, damageDealt);
        }
    }

    private void applyEndersGrasp(LivingEntity victim) {
        AttributeInstance armor = victim.getAttribute(Attribute.ARMOR);
        if (armor == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "enders_grasp_" + victim.getUniqueId());
        // Remove any existing modifier first so repeated procs don't stack.
        AttributeModifier existing = armor.getModifier(key);
        if (existing != null) armor.removeModifier(existing);

        AttributeModifier modifier = new AttributeModifier(
                key, ENDERS_GRASP_ARMOR_REDUCTION, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        armor.addModifier(modifier);

        victim.getWorld().spawnParticle(Particle.WITCH, victim.getLocation().add(0, 1, 0), 12, 0.3, 0.5, 0.3);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!victim.isValid()) return;
            AttributeInstance a = victim.getAttribute(Attribute.ARMOR);
            if (a == null) return;
            AttributeModifier current = a.getModifier(key);
            if (current != null) a.removeModifier(current);
        }, ENDERS_GRASP_DURATION_TICKS);
    }

    private void triggerVoidSlash(Player attacker, LivingEntity primaryVictim, double baseDamage) {
        double waveDamage = baseDamage * VOID_SLASH_MULTIPLIER;
        Location center = primaryVictim.getLocation();

        center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0, 1, 0), 40, 0.6, 0.6, 0.6, 0.1);
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.4f);

        for (org.bukkit.entity.Entity nearby : center.getWorld().getNearbyEntities(center, VOID_SLASH_RADIUS, VOID_SLASH_RADIUS, VOID_SLASH_RADIUS)) {
            if (!(nearby instanceof LivingEntity target)) continue;
            if (target.equals(attacker) || target.equals(primaryVictim)) continue;
            target.damage(waveDamage, attacker);
        }
    }

    private int getHitCount(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0;
        Integer count = meta.getPersistentDataContainer().get(Keys.VOIDREAVER_HITS, PersistentDataType.INTEGER);
        return count == null ? 0 : count;
    }

    private void setHitCount(ItemStack stack, int count) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.VOIDREAVER_HITS, PersistentDataType.INTEGER, count);
        stack.setItemMeta(meta);
    }
}
