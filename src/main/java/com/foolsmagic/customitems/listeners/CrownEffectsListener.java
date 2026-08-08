package com.foolsmagic.customitems.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffectType;

import com.foolsmagic.customitems.items.EmperorCrownItem;
import com.foolsmagic.customitems.items.ItemRegistry;

public class CrownEffectsListener implements Listener {

    /** Potion effects "Unshakable Presence" blocks entirely. Extend this list as needed. */
    private static final Set<PotionEffectType> NEGATIVE_EFFECTS = Set.of(
            PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.NAUSEA,
            PotionEffectType.BLINDNESS, PotionEffectType.POISON, PotionEffectType.WITHER,
            PotionEffectType.HUNGER, PotionEffectType.MINING_FATIGUE, PotionEffectType.UNLUCK,
            PotionEffectType.DARKNESS
    );

    private final ItemRegistry registry;
    private final EmperorCrownItem crownItem;

    // Villager UUID -> their trades before we discounted them, so we can restore after close.
    private final Map<UUID, java.util.List<MerchantRecipe>> originalTrades = new HashMap<>();

    public CrownEffectsListener(ItemRegistry registry, EmperorCrownItem crownItem) {
        this.registry = registry;
        this.crownItem = crownItem;
    }

    // --- +5 hearts on equip/unequip ---

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (event.getSlotType() != PlayerArmorChangeEvent.SlotType.HEAD) return;

        boolean wasCrown = registry.identify(event.getOldItem()) instanceof EmperorCrownItem;
        boolean isCrown = registry.identify(event.getNewItem()) instanceof EmperorCrownItem;

        if (isCrown && !wasCrown) {
            crownItem.applyHealthBonus(event.getPlayer());
        } else if (wasCrown && !isCrown) {
            crownItem.removeHealthBonus(event.getPlayer());
        }
    }

    // Reconciles the health bonus on login, in case armor was equipped outside a
    // tracked PlayerArmorChangeEvent (e.g. loaded straight from the save file).
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (EmperorCrownItem.isWearing(player, registry)) {
            crownItem.applyHealthBonus(player);
        } else {
            crownItem.removeHealthBonus(player);
        }
    }

    // --- Divisive Opinions + Unshakable Presence ---

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!EmperorCrownItem.isWearing(player, registry)) return;
        if (event.getNewEffect() == null) return;

        PotionEffectType type = event.getNewEffect().getType();
        if (type.equals(PotionEffectType.INVISIBILITY) || NEGATIVE_EFFECTS.contains(type)) {
            event.setCancelled(true);
        }
    }

    // --- Golden Tongue: hostile mobs ignore the wearer ---

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Monster)) return;
        if (!EmperorCrownItem.isWearing(player, registry)) return;
        event.setCancelled(true);
    }

    // --- Golden Tongue: creepers near the wearer won't explode ---

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        double r = EmperorCrownItem.GOLDEN_TONGUE_RADIUS;
        for (Entity nearby : creeper.getNearbyEntities(r, r, r)) {
            if (nearby instanceof Player player && EmperorCrownItem.isWearing(player, registry)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // --- Silver Tongue: discount villager trades while wearing the crown ---

    @EventHandler
    public void onInteractVillager(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        Player player = event.getPlayer();
        if (!EmperorCrownItem.isWearing(player, registry)) return;

        java.util.List<MerchantRecipe> current = villager.getRecipes();
        java.util.List<MerchantRecipe> discounted = new java.util.ArrayList<>(current.size());

        for (MerchantRecipe recipe : current) {
            MerchantRecipe copy = new MerchantRecipe(
                    recipe.getResult(), recipe.getUses(), recipe.getMaxUses(),
                    recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier());
            for (ItemStack ingredient : recipe.getIngredients()) {
                copy.addIngredient(discount(ingredient));
            }
            discounted.add(copy);
        }

        originalTrades.put(villager.getUniqueId(), current);
        villager.setRecipes(discounted);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInv)) return;
        if (!(merchantInv.getHolder() instanceof Villager villager)) return;

        java.util.List<MerchantRecipe> original = originalTrades.remove(villager.getUniqueId());
        if (original != null && villager.isValid()) {
            villager.setRecipes(original);
        }
    }

    private ItemStack discount(ItemStack ingredient) {
        if (ingredient == null || ingredient.getAmount() <= 1) return ingredient;
        ItemStack copy = ingredient.clone();
        int discounted = (int) Math.max(1, Math.round(copy.getAmount() * (1 - EmperorCrownItem.TRADE_DISCOUNT)));
        copy.setAmount(discounted);
        return copy;
    }
}
