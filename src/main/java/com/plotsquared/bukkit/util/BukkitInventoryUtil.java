package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class BukkitInventoryUtil extends InventoryUtil {
    
    @Override
    public void open(final PlotInventory inv) {
        final BukkitPlayer bp = ((BukkitPlayer) inv.player);
        final Inventory inventory = Bukkit.createInventory(null, inv.size * 9, inv.getTitle());
        final PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < (inv.size * 9); i++) {
            final PlotItemStack item = items[i];
            if (item != null) {
                inventory.setItem(i, getItem(item));
            }
        }
        inv.player.setMeta("inventory", inv);
        bp.player.openInventory(inventory);
    }
    
    @Override
    public void close(final PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        inv.player.deleteMeta("inventory");
        final BukkitPlayer bp = ((BukkitPlayer) inv.player);
        bp.player.closeInventory();
    }
    
    @Override
    public void setItem(final PlotInventory inv, final int index, final PlotItemStack item) {
        final BukkitPlayer bp = ((BukkitPlayer) inv.player);
        final InventoryView opened = bp.player.getOpenInventory();
        if (!inv.isOpen()) {
            return;
        }
        opened.setItem(index, getItem(item));
        bp.player.updateInventory();
    }
    
    public PlotItemStack getItem(final ItemStack item) {
        if (item == null) {
            return null;
        }
        final int id = item.getTypeId();
        final short data = item.getDurability();
        final int amount = item.getAmount();
        String name = null;
        String[] lore = null;
        if (item.hasItemMeta()) {
            final ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
            if (meta.hasLore()) {
                final List<String> itemLore = meta.getLore();
                lore = itemLore.toArray(new String[itemLore.size()]);
            }
        }
        return new PlotItemStack(id, data, amount, name, lore);
    }
    
    public static ItemStack getItem(final PlotItemStack item) {
        if (item == null) {
            return null;
        }
        final ItemStack stack = new ItemStack(item.id, item.amount, item.data);
        ItemMeta meta = null;
        if (item.name != null) {
            meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', item.name));
        }
        if (item.lore != null) {
            if (meta == null) {
                meta = stack.getItemMeta();
            }
            final List<String> lore = new ArrayList<>();
            for (final String entry : item.lore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', entry));
            }
            meta.setLore(lore);
        }
        if (meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }
    
    @Override
    public PlotItemStack[] getItems(final PlotPlayer player) {
        final BukkitPlayer bp = ((BukkitPlayer) player);
        final PlayerInventory inv = bp.player.getInventory();
        final PlotItemStack[] items = new PlotItemStack[36];
        for (int i = 0; i < 36; i++) {
            items[i] = getItem(inv.getItem(i));
        }
        return items;
    }
    
    @Override
    public boolean isOpen(final PlotInventory inv) {
        if (!inv.isOpen()) {
            return false;
        }
        final BukkitPlayer bp = ((BukkitPlayer) inv.player);
        final InventoryView opened = bp.player.getOpenInventory();
        return (inv.isOpen() && (opened.getType() == InventoryType.CRAFTING) && (opened.getTitle() == null));
    }
}
