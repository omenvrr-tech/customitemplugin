package com.foolsmagic.customitems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.foolsmagic.customitems.commands.GiveItemCommand;
import com.foolsmagic.customitems.effects.FreezeManager;
import com.foolsmagic.customitems.items.DemonicHammerItem;
import com.foolsmagic.customitems.items.EmperorCrownItem;
import com.foolsmagic.customitems.items.FoolsMagicItem;
import com.foolsmagic.customitems.items.ItemRegistry;
import com.foolsmagic.customitems.items.TimekeeperWatchItem;
import com.foolsmagic.customitems.items.VoidreaverItem;
import com.foolsmagic.customitems.listeners.CombatAbilityListener;
import com.foolsmagic.customitems.listeners.CrownEffectsListener;
import com.foolsmagic.customitems.listeners.ItemInteractListener;
import com.foolsmagic.customitems.listeners.ItemProtectionListener;
import com.foolsmagic.customitems.suppression.SuppressionManager;
import com.foolsmagic.customitems.util.Keys;

public class CustomItemsPlugin extends JavaPlugin {

    private ItemRegistry itemRegistry;
    private SuppressionManager suppressionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Keys.init(this);

        itemRegistry = new ItemRegistry();
        // Register the Fool's Magic card itself (no ability, cannot be suppressed).
        itemRegistry.register(new FoolsMagicItem(this));

        // Register every ability item here. Add new custom items by writing a class
        // implementing CustomItem (+ AbilityItem for right-click abilities, or
        // OnHitAbilityItem for melee-hit abilities) and registering it on the next
        // line - the Fool's Magic card will suppress it automatically, no other code
        // changes needed.
        itemRegistry.register(new DemonicHammerItem());

        VoidreaverItem voidreaver = new VoidreaverItem(this);
        itemRegistry.register(voidreaver);

        FreezeManager freezeManager = new FreezeManager(this);
        itemRegistry.register(new TimekeeperWatchItem(this, freezeManager));

        EmperorCrownItem emperorCrown = new EmperorCrownItem(this);
        itemRegistry.register(emperorCrown);

        suppressionManager = new SuppressionManager(itemRegistry);

        getServer().getPluginManager().registerEvents(
                new ItemInteractListener(this, itemRegistry, suppressionManager), this);
        getServer().getPluginManager().registerEvents(
                new ItemProtectionListener(itemRegistry), this);
        getServer().getPluginManager().registerEvents(
                new CombatAbilityListener(itemRegistry, suppressionManager), this);
        getServer().getPluginManager().registerEvents(
                new CrownEffectsListener(itemRegistry, emperorCrown), this);

        registerVoidreaverRecipe(voidreaver);

        GiveItemCommand giveItemCommand = new GiveItemCommand(itemRegistry);
        getCommand("giveitem").setExecutor(giveItemCommand);
        getCommand("giveitem").setTabCompleter(giveItemCommand);
        getCommand("itemslist").setExecutor(giveItemCommand);

        getLogger().info("CustomItems enabled - " + itemRegistry.all().size() + " item(s) registered.");
    }

    /**
     * 3x3 shape matching the reference art:
     *   Ender Pearl | Nether Star   | Ender Pearl
     *   Netherite Ingot | Netherite Sword | Netherite Ingot
     *   Ender Pearl | Nether Star   | Ender Pearl
     *
     * (Netherite Sword substitutes for the non-existent "Wither Skeleton Sword" -
     * change the 'W' ingredient below if you add a real custom item for that later.)
     */
    private void registerVoidreaverRecipe(VoidreaverItem voidreaver) {
        NamespacedKey key = new NamespacedKey(this, "voidreaver");
        ItemStack result = voidreaver.create();

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("ESE", "IWI", "ESE");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('I', Material.NETHERITE_INGOT);
        recipe.setIngredient('W', Material.NETHERITE_SWORD);

        Bukkit.addRecipe(recipe);
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public SuppressionManager getSuppressionManager() {
        return suppressionManager;
    }
}

