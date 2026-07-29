package com.foolsmagic.customitems.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.foolsmagic.customitems.items.CustomItem;
import com.foolsmagic.customitems.items.ItemRegistry;

public class GiveItemCommand implements CommandExecutor, TabCompleter {

    private final ItemRegistry registry;

    public GiveItemCommand(ItemRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("itemslist")) {
            sender.sendMessage("Registered items: " +
                    registry.all().stream().map(CustomItem::id).collect(Collectors.joining(", ")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /giveitem <item-id> [player]");
            return true;
        }

        CustomItem item = registry.get(args[0]);
        if (item == null) {
            sender.sendMessage("Unknown item id '" + args[0] + "'. Try /itemslist");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("Player '" + args[1] + "' not found/online.");
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("Console must specify a player: /giveitem <item-id> <player>");
            return true;
        }

        ItemStack stack = item.create();
        target.getInventory().addItem(stack);
        sender.sendMessage("Gave " + target.getName() + " a " + item.id() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("itemslist")) return List.of();
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            registry.all().forEach(item -> options.add(item.id()));
        } else if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
        }
        return options;
    }
}
