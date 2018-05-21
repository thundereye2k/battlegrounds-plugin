package com.matsg.battlegrounds.gui;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.item.Loadout;
import com.matsg.battlegrounds.api.item.Weapon;
import com.matsg.battlegrounds.config.BattlePlayerYaml;
import com.matsg.battlegrounds.util.EnumMessage;
import com.matsg.battlegrounds.util.ItemStackBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoadoutManagerView implements View {

    private Inventory inventory;
    private Map<ItemStack, Loadout> loadouts;

    public LoadoutManagerView(Battlegrounds plugin, Player player) {
        this.inventory = plugin.getServer().createInventory(this, 27, EnumMessage.CLASS_MANAGER.getMessage());
        this.loadouts = new HashMap<>();

        try {
            int i = 0;
            for (Loadout loadout : new BattlePlayerYaml(plugin, player.getUniqueId()).getLoadouts()) {
                ItemStack itemStack = new ItemStackBuilder(getLoadoutItemStack(loadout))
                        .addItemFlags(ItemFlag.values())
                        .setAmount(++ i)
                        .setDisplayName("§f" + loadout.getName())
                        .setLore(EnumMessage.EDIT_CLASS.getMessage())
                        .setUnbreakable(true)
                        .build();

                inventory.setItem(i + 10, itemStack);
                loadouts.put(itemStack, loadout);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    private ItemStack getLoadoutItemStack(Loadout loadout) {
        for (Weapon weapon : loadout.getWeapons()) {
            if (weapon.getItemStack() != null) {
                return weapon.getItemStack();
            }
        }
        return null;
    }

    public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
        System.out.print(loadouts.get(itemStack).getName());
    }

    public boolean onClose() {
        return true;
    }
}