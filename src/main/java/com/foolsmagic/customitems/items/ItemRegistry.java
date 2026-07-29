package com.foolsmagic.customitems.items;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.foolsmagic.customitems.util.Keys;

public class ItemRegistry {

    private final Map<String, CustomItem> byId = new LinkedHashMap<>();

    public void register(CustomItem item) {
        byId.put(item.id(), item);
    }

    public CustomItem get(String id) {
        return byId.get(id);
    }

    public Collection<CustomItem> all() {
        return byId.values();
    }

    /** Figures out which registered CustomItem (if any) a given stack is, via its id tag. */
    public CustomItem identify(ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null) return null;
        String id = stack.getItemMeta().getPersistentDataContainer()
                .get(Keys.ITEM_ID, PersistentDataType.STRING);
        if (id == null) return null;
        return byId.get(id);
    }
}
