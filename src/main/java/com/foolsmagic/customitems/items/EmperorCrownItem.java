package com.foolsmagic.customitems.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
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
 * Emperor's Crown. Purely passive - all effects come from wearing it in the helmet
 * slot; there's no right-click ability, so it's not suppressible by the Fool's Magic
 * card (that only suppresses right-click/on-hit AbilityItems).
 *
 * Effects (enforced by CrownEffectsListener while worn):
 * - Silver Tongue: discounted villager trades.
 * - Divisive Opinions: can't drink invisibility potions.
 * - Unshakable Presence: immune to negative potion effects (slowness, weakness,
 *   nausea, blindness, poison, wither, hunger, mining fatigue, unluck, darkness).
 * - Golden Tongue: hostile mobs within 20 blocks ignore you; creepers near you won't explode.
 * - +5 hearts (max health) while worn.
 */
public class EmperorCrownItem implements CustomItem, Indestructible {

    public static final String ID = "emperor_crown";
    public static final double BONUS_HEALTH = 10.0; // 5 hearts
    public static final double GOLDEN_TONGUE_RADIUS = 20.0;
    public static final double TRADE_DISCOUNT = 0.20; // 20% off villager prices

    private final NamespacedKey healthModifierKey;

    public EmperorCrownItem(Plugin plugin) {
        this.healthModifierKey = new NamespacedKey(plugin, "emperor_crown_health");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ItemStack create() {
        ItemStack stack = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text("Emperor's Crown", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Silver Tongue", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Villagers offer discounted trades.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Divisive Opinions", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Cannot use invisibility potions.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Unshakable Presence", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Immune to negative potion effects.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Golden Tongue", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Hostile mobs within 20 blocks ignore you.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Nearby creepers won't explode.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("+5 hearts while worn", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(Keys.ITEM_ID, PersistentDataType.STRING, ID);

        stack.setItemMeta(meta);
        stack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("blank:emperorcrown"));
        // See FoolsMagicItem.create()'s FALLBACK note if the line above doesn't compile.
        // GOLDEN_HELMET already carries an equippable(HEAD) component by default, so no
        // extra equippable component setup is needed - only the visual model is overridden.

        return stack;
    }

    public static boolean isWearing(Player player, ItemRegistry registry) {
        ItemStack helmet = player.getInventory().getHelmet();
        return registry.identify(helmet) instanceof EmperorCrownItem;
    }

    /** Adds the +5 hearts modifier if not already present. Safe to call repeatedly. */
    public void applyHealthBonus(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null || maxHealth.getModifier(healthModifierKey) != null) return;

        AttributeModifier modifier = new AttributeModifier(
                healthModifierKey, BONUS_HEALTH, AttributeModifier.Operation.ADD_NUMBER);
        maxHealth.addModifier(modifier);
    }

    /** Removes the +5 hearts modifier if present. Safe to call repeatedly. */
    public void removeHealthBonus(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        AttributeModifier modifier = maxHealth.getModifier(healthModifierKey);
        if (modifier != null) maxHealth.removeModifier(modifier);
    }
}
