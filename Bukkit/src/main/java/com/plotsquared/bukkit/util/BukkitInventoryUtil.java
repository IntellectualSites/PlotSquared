/*
 *
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.InventoryUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class BukkitInventoryUtil extends InventoryUtil {

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
        bp.player.openInventory(inventory);
    }

    @Override public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
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

    private static ItemStack getItem(PlotItemStack item) {
        if (item == null) {
            return null;
        }
        ItemStack stack = new ItemStack(BukkitAdapter.adapt(item.getType()), item.amount);
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

    public PlotItemStack getItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        // int id = item.getTypeId();
        Material id = item.getType();
        ItemMeta meta = item.getItemMeta();
        int amount = item.getAmount();
        String name = null;
        String[] lore = null;
        if (item.hasItemMeta()) {
            assert meta != null;
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
            if (meta.hasLore()) {
                List<String> itemLore = meta.getLore();
                assert itemLore != null;
                lore = itemLore.toArray(new String[0]);
            }
        }
        return new PlotItemStack(id.name(), amount, name, lore);
    }

    @Override public PlotItemStack[] getItems(PlotPlayer player) {
        BukkitPlayer bp = (BukkitPlayer) player;
        PlayerInventory inv = bp.player.getInventory();
        return IntStream.range(0, 36).mapToObj(i -> getItem(inv.getItem(i)))
            .toArray(PlotItemStack[]::new);
    }

    @Override public boolean isOpen(PlotInventory plotInventory) {
        if (!plotInventory.isOpen()) {
            return false;
        }
        BukkitPlayer bp = (BukkitPlayer) plotInventory.player;
        InventoryView opened = bp.player.getOpenInventory();
        if (plotInventory.isOpen()) {
            if (opened.getType() == InventoryType.CRAFTING) {
                opened.getTitle();
            }
        }
        return false;
    }
}
