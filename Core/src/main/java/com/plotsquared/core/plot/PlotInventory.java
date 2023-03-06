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
package com.plotsquared.core.plot;

import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.InventoryUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlotInventory {

    private final PlotPlayer<?> player;
    private final int lines;
    private final PlotItemStack[] items;
    private final InventoryUtil inventoryUtil;
    private String title;
    private boolean open = false;

    public PlotInventory(
            final @NonNull InventoryUtil inventoryUtil,
            PlotPlayer<?> player, int lines, String name
    ) {
        this.lines = lines;
        this.title = name == null ? "" : name;
        this.player = player;
        this.items = new PlotItemStack[lines * 9];
        this.inventoryUtil = inventoryUtil;
    }

    public static boolean hasPlotInventoryOpen(final @NonNull PlotPlayer<?> plotPlayer) {
        return getOpenPlotInventory(plotPlayer) != null;
    }

    public static PlotInventory getOpenPlotInventory(final @NonNull PlotPlayer<?> plotPlayer) {
        try (final MetaDataAccess<PlotInventory> inventoryAccess = plotPlayer.accessTemporaryMetaData(
                PlayerMetaDataKeys.TEMPORARY_INVENTORY)) {
            return inventoryAccess.get().orElse(null);
        }
    }

    public static void setPlotInventoryOpen(
            final @NonNull PlotPlayer<?> plotPlayer,
            final @NonNull PlotInventory plotInventory
    ) {
        try (final MetaDataAccess<PlotInventory> inventoryAccess = plotPlayer.accessTemporaryMetaData(
                PlayerMetaDataKeys.TEMPORARY_INVENTORY)) {
            inventoryAccess.set(plotInventory);
        }
    }

    public static void removePlotInventoryOpen(final @NonNull PlotPlayer<?> plotPlayer) {
        try (final MetaDataAccess<PlotInventory> inventoryAccess = plotPlayer.accessTemporaryMetaData(
                PlayerMetaDataKeys.TEMPORARY_INVENTORY)) {
            inventoryAccess.remove();
        }
    }

    public boolean onClick(int index) {
        return true;
    }

    public void openInventory() {
        if (this.title == null) {
            return;
        }
        if (!hasPlotInventoryOpen(getPlayer())) {
            this.open = true;
            setPlotInventoryOpen(getPlayer(), this);
            this.inventoryUtil.open(this);
        }
    }

    public void close() {
        if (this.title == null) {
            return;
        }
        removePlotInventoryOpen(getPlayer());
        this.inventoryUtil.close(this);
        this.open = false;
    }

    /**
     * Put an item into this inventory
     *
     * @param index the index (= slot) where to place the item
     * @param item  the item to place
     * @see #setItemChecked(int, PlotItemStack)
     */
    public void setItem(int index, PlotItemStack item) {
        setItemChecked(index, item);
    }

    /**
     * Put an item into this inventory, while also checking the existence of the material in the current version
     *
     * @param index the index (= slot) where to place the item
     * @param item  the item to place
     * @return {@code true} if the item could be placed, otherwise {@code false}
     * @see InventoryUtil#setItemChecked(PlotInventory, int, PlotItemStack)
     * @since 6.5.0
     */
    public boolean setItemChecked(int index, PlotItemStack item) {
        if (!this.inventoryUtil.setItemChecked(this, index, item)) {
            return false;
        }
        this.items[index] = item;
        return true;
    }

    public PlotItemStack getItem(int index) {
        if ((index < 0) || (index >= this.items.length)) {
            return null;
        }
        return this.items[index];
    }

    public PlotItemStack[] getItems() {
        return this.items;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        boolean tmp = this.open;
        close();
        this.title = title;
        if (tmp) {
            openInventory();
        }
    }

    public boolean isOpen() {
        return this.open;
    }

    public PlotPlayer<?> getPlayer() {
        return player;
    }

    public int getLines() {
        return lines;
    }

}
