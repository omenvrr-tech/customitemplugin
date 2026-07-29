package com.foolsmagic.customitems;

import org.bukkit.plugin.java.JavaPlugin;

import com.foolsmagic.customitems.commands.GiveItemCommand;
import com.foolsmagic.customitems.items.DemonicHammerItem;
import com.foolsmagic.customitems.items.FoolsMagicItem;
import com.foolsmagic.customitems.items.ItemRegistry;
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
        // implementing CustomItem (+ AbilityItem if it has a right-click ability) and
        // registering it on the next line - the Fool's Magic card will suppress it
        // automatically, no other code changes needed.
        itemRegistry.register(new DemonicHammerItem());

        suppressionManager = new SuppressionManager(itemRegistry);

        getServer().getPluginManager().registerEvents(
                new ItemInteractListener(this, itemRegistry, suppressionManager), this);
        getServer().getPluginManager().registerEvents(
                new ItemProtectionListener(itemRegistry), this);

        GiveItemCommand giveItemCommand = new GiveItemCommand(itemRegistry);
        getCommand("giveitem").setExecutor(giveItemCommand);
        getCommand("giveitem").setTabCompleter(giveItemCommand);
        getCommand("itemslist").setExecutor(giveItemCommand);

        getLogger().info("CustomItems enabled - " + itemRegistry.all().size() + " item(s) registered.");
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public SuppressionManager getSuppressionManager() {
        return suppressionManager;
    }
}
