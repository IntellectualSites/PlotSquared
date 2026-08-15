/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util;

import com.google.inject.Singleton;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.InventoryUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Singleton
public class BukkitInventoryUtil extends InventoryUtil {

    @SuppressWarnings("deprecation") // Paper deprecation
    private static @Nullable ItemStack getItem(PlotItemStack item) {
        if (item == null) {
            return null;
        }
        Material material = BukkitAdapter.adapt(item.getType());
        if (material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(material, item.getAmount());
        ItemMeta meta = null;
        if (item.getName() != null) {
            meta = stack.getItemMeta();
            Component nameComponent = BukkitUtil.MINI_MESSAGE.deserialize(item.getName());
            meta.setDisplayName(BukkitUtil.LEGACY_COMPONENT_SERIALIZER.serialize(nameComponent));
        }
        if (item.getLore() != null) {
            if (meta == null) {
                meta = stack.getItemMeta();
            }
            List<String> lore = new ArrayList<>();
            for (String entry : item.getLore()) {
                lore.add(BukkitUtil.LEGACY_COMPONENT_SERIALIZER.serialize(BukkitUtil.MINI_MESSAGE.deserialize(entry)));
            }
            meta.setLore(lore);
        }
        if (meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    @Override
    public void open(PlotInventory inv) {
        BukkitPlayer bp = (BukkitPlayer) inv.getPlayer();
        Inventory inventory = Bukkit.createInventory(null, inv.getLines() * 9,
                ChatColor.translateAlternateColorCodes('&', inv.getTitle())
        );
        PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < inv.getLines() * 9; i++) {
            PlotItemStack item = items[i];
            if (item != null) {
                inventory.setItem(i, getItem(item));
            }
        }
        bp.player.openInventory(inventory);
    }

    @Override
    public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        BukkitPlayer bp = (BukkitPlayer) inv.getPlayer();
        bp.player.closeInventory();
    }

    @Override
    public boolean setItemChecked(PlotInventory inv, int index, PlotItemStack item) {
        BukkitPlayer bp = (BukkitPlayer) inv.getPlayer();
        InventoryView opened = bp.player.getOpenInventory();
        ItemStack stack = getItem(item);
        if (stack == null) {
            return false;
        }
        if (!inv.isOpen()) {
            return true;
        }
        opened.setItem(index, stack);
        bp.player.updateInventory();
        return true;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
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

    @Override
    public PlotItemStack[] getItems(PlotPlayer<?> player) {
        BukkitPlayer bp = (BukkitPlayer) player;
        PlayerInventory inv = bp.player.getInventory();
        return IntStream.range(0, 36).mapToObj(i -> getItem(inv.getItem(i)))
                .toArray(PlotItemStack[]::new);
    }

    @SuppressWarnings("deprecation") // #getTitle is needed for Spigot compatibility
    @Override
    public boolean isOpen(PlotInventory plotInventory) {
        if (!plotInventory.isOpen()) {
            return false;
        }
        BukkitPlayer bp = (BukkitPlayer) plotInventory.getPlayer();
        InventoryView opened = bp.player.getOpenInventory();
        if (plotInventory.isOpen()) {
            if (opened.getType() == InventoryType.CRAFTING) {
                opened.getTitle();
            }
        }
        return false;
    }

}
