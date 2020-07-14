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
package com.plotsquared.core.plot;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.InventoryUtil;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotInventory {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + PlotInventory.class.getSimpleName());

    private static final String META_KEY = "inventory";
    public final PlotPlayer<?> player;
    public final int size;
    private final PlotItemStack[] items;
    private String title;
    private boolean open = false;
    private final InventoryUtil inventoryUtil;

    public PlotInventory(@Nonnull final InventoryUtil inventoryUtil,
                         PlotPlayer<?> player, int size, String name) {
        this.size = size;
        this.title = name == null ? "" : name;
        this.player = player;
        this.items = new PlotItemStack[size * 9];
        this.inventoryUtil = inventoryUtil;
    }

    public static boolean hasPlotInventoryOpen(@Nonnull final PlotPlayer<?> plotPlayer) {
        return getOpenPlotInventory(plotPlayer) != null;
    }

    public static PlotInventory getOpenPlotInventory(@Nonnull final PlotPlayer<?> plotPlayer) {
        return plotPlayer.getMeta(META_KEY, null);
    }

    public static void setPlotInventoryOpen(@Nonnull final PlotPlayer<?> plotPlayer,
        @Nonnull final PlotInventory plotInventory) {
        plotPlayer.setMeta(META_KEY, plotInventory);
    }

    public static void removePlotInventoryOpen(@Nonnull final PlotPlayer<?>plotPlayer) {
        plotPlayer.deleteMeta(META_KEY);
    }

    public boolean onClick(int index) {
        return true;
    }

    public void openInventory() {
        if (this.title == null) {
            return;
        }
        if (!hasPlotInventoryOpen(player)) {
            this.open = true;
            setPlotInventoryOpen(player, this);
            this.inventoryUtil.open(this);
        }
    }

    public void close() {
        if (this.title == null) {
            return;
        }
        removePlotInventoryOpen(player);
        this.inventoryUtil.close(this);
        this.open = false;
    }

    public void setItem(int index, PlotItemStack item) {
        this.items[index] = item;
        this.inventoryUtil.setItem(this, index, item);
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

}
