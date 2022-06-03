/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util.gui;

import com.google.common.base.Preconditions;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.gui.PlotInventory;
import com.plotsquared.core.util.gui.PlotInventoryClickHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Implementation of the {@link PlotInventory} for the bukkit platform.
 */
public class BukkitPlotInventory extends PlotInventory<Player, ItemStack> {

    private static Listener INVENTORY_LISTENER;
    private final ItemStack[] items;
    private final PlotInventoryClickHandler<Player>[] clickHandlers;
    private final Map<UUID, Inventory> viewers = new WeakHashMap<>();
    private static final Map<UUID, BukkitPlotInventory> INVENTORIES = new WeakHashMap<>();

    /**
     * {@inheritDoc}
     */
    protected BukkitPlotInventory(final int size, final Caption titleCaption, final TagResolver... titleResolvers) {
        super(size, titleCaption, titleResolvers);
        this.items = new ItemStack[size];
        this.clickHandlers = new PlotInventoryClickHandler[size];

        if (INVENTORY_LISTENER != null) {
            return;
        }
        INVENTORY_LISTENER = new Listener() {
            @EventHandler
            public void onInventoryClick(final org.bukkit.event.inventory.InventoryClickEvent event) {
                final PlotPlayer<Player> player = BukkitUtil.adapt((Player) event.getWhoClicked());

                BukkitPlotInventory currentInventory = INVENTORIES.get(player.getUUID());
                if (currentInventory == null) {
                    return;
                }
                if (!Objects.equals(event.getClickedInventory(), currentInventory.viewers.get(player.getUUID()))) {
                    return;
                }

                final int slot = event.getRawSlot();
                if (slot < 0 || slot >= currentInventory.size()) {
                    return;
                }
                final PlotInventoryClickHandler<Player> clickHandler = currentInventory.clickHandlers[slot];
                if (clickHandler == null) {
                    return;
                }
                event.setCancelled(true);
                final ItemStack item = event.getCurrentItem();
                if (item == null) {
                    clickHandler.handle(null, player);
                    return;
                }
                clickHandler.handle(new PlotItemStack(
                        BukkitAdapter.asItemType(item.getType()),
                        item.getAmount(),
                        item.getItemMeta().getDisplayName(),
                        item.getItemMeta().hasLore() ? item.getItemMeta().getLore().toArray(String[]::new) : new String[0]
                ), player);
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                final PlotPlayer<Player> player = BukkitUtil.adapt((Player) event.getPlayer());
                BukkitPlotInventory currentInventory = INVENTORIES.get(player.getUUID());
                if (currentInventory == null) {
                    return;
                }
                currentInventory.viewers.remove(player.getUUID());
                INVENTORIES.remove(player.getUUID());
            }

        };
        BukkitPlatform bukkitPlatform = ((BukkitPlatform) PlotSquared.platform());
        bukkitPlatform.getServer().getPluginManager().registerEvents(INVENTORY_LISTENER, bukkitPlatform);
    }

    @Override
    public void setItem(final int slot, final PlotItemStack item, final PlotInventoryClickHandler<Player> onClick) {
        Preconditions.checkElementIndex(slot, size(), "Slot must be in range (0, " + size() + ")");
        this.items[slot] = toPlatformItem(item);
        this.clickHandlers[slot] = onClick;
        for (final Inventory value : viewers.values()) {
            value.setItem(slot, this.items[slot]);
        }
    }

    @Override
    public void addItem(final PlotItemStack item, final PlotInventoryClickHandler<Player> onClick) {
        // TODO: probably needs more love (who doesn't)
        int slot = -1;
        // try to fill stacks
        for (int i = 0; i < items.length; i++) {
            if (Objects.equals(items[i], toPlatformItem(item))) {
                slot = i;
                break;
            }
        }
        // search for empty slots
        if (slot == -1) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null || items[i].getType() == Material.AIR) {
                    slot = i;
                    break;
                }
            }
        }
        Preconditions.checkElementIndex(slot, size());
        this.items[slot] = toPlatformItem(item);
        this.clickHandlers[slot] = onClick;
        for (final Inventory value : viewers.values()) {
            value.setItem(slot, this.items[slot]);
        }
    }

    @Override
    protected void openPlatformPlayer(final PlotPlayer<Player> player) {
        Component title = MiniMessage.miniMessage().deserialize(
                titleCaption().getComponent(player), titleResolvers()
        );
        Inventory inventory = Bukkit.createInventory(player.getPlatformPlayer(), size(),
                BukkitUtil.LEGACY_COMPONENT_SERIALIZER.serialize(title)
        );
        for (int i = 0; i < items.length; i++) {
            inventory.setItem(i, items[i]);
        }
        INVENTORIES.put(player.getUUID(), this);
        viewers.put(player.getUUID(), inventory);
        player.getPlatformPlayer().openInventory(inventory);
    }

    @Override
    protected void closePlatformPlayer(final PlotPlayer<Player> player) {
        if (player.getPlatformPlayer().getOpenInventory().getTopInventory().equals(INVENTORIES.get(player.getUUID()))) {
            player.getPlatformPlayer().closeInventory();
        }
    }

    @Override
    public ItemStack toPlatformItem(final PlotItemStack item) {
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

}
