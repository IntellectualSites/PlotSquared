package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitInventoryUtil extends InventoryUtil {

    public static ItemStack getItem(PlotItemStack item) {
        if (item == null) {
            return null;
        }
        ItemStack stack = new ItemStack(item.id, item.amount, item.data);
        ItemMeta meta = null;
        if (item.name != null) {
            meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', item.name));
        }
        if (item.lore != null) {
            if (meta == null) {
                meta = stack.getItemMeta();
            }
            List<String> lore = new ArrayList<>();
            for (String entry : item.lore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', entry));
            }
            meta.setLore(lore);
        }
        if (meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @Override public void open(PlotInventory inv) {
        BukkitPlayer bp = (BukkitPlayer) inv.player;
        Inventory inventory = Bukkit.createInventory(null, inv.size * 9, inv.getTitle());
        PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < inv.size * 9; i++) {
            PlotItemStack item = items[i];
            if (item != null) {
                inventory.setItem(i, getItem(item));
            }
        }
        inv.player.setMeta("inventory", inv);
        bp.player.openInventory(inventory);
    }

    @Override public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        inv.player.deleteMeta("inventory");
        BukkitPlayer bp = (BukkitPlayer) inv.player;
        bp.player.closeInventory();
    }

    @Override public void setItem(PlotInventory inv, int index, PlotItemStack item) {
        BukkitPlayer bp = (BukkitPlayer) inv.player;
        InventoryView opened = bp.player.getOpenInventory();
        if (!inv.isOpen()) {
            return;
        }
        opened.setItem(index, getItem(item));
        bp.player.updateInventory();
    }

    public PlotItemStack getItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        int id = item.getTypeId();
        short data = item.getDurability();
        int amount = item.getAmount();
        String name = null;
        String[] lore = null;
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
            if (meta.hasLore()) {
                List<String> itemLore = meta.getLore();
                lore = itemLore.toArray(new String[itemLore.size()]);
            }
        }
        return new PlotItemStack(id, data, amount, name, lore);
    }

    @Override public PlotItemStack[] getItems(PlotPlayer player) {
        BukkitPlayer bp = (BukkitPlayer) player;
        PlayerInventory inv = bp.player.getInventory();
        PlotItemStack[] items = new PlotItemStack[36];
        for (int i = 0; i < 36; i++) {
            items[i] = getItem(inv.getItem(i));
        }
        return items;
    }

    @Override public boolean isOpen(PlotInventory inv) {
        if (!inv.isOpen()) {
            return false;
        }
        BukkitPlayer bp = (BukkitPlayer) inv.player;
        InventoryView opened = bp.player.getOpenInventory();
        return inv.isOpen() && opened.getType() == InventoryType.CRAFTING
            && opened.getTitle() == null;
    }
}
