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
package com.plotsquared.core.util;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;

/**
 * This class is only used by internal functions, for most cases use the PlotInventory class
 */
public abstract class InventoryUtil {

    public abstract void open(final PlotInventory inv);

    public abstract void close(final PlotInventory inv);

    /**
     * Attempts to set an item into a {@link PlotInventory} while also checking the existence of the material
     *
     * @param plotInventory The inventory where the item should be placed
     * @param index         The index where to place the item
     * @param item          The item to place into the inventory
     * @return {@code true} if the item could be placed, {@code false} otherwise (e.g. item not available in current version)
     * @since 6.5.0
     */
    public abstract boolean setItemChecked(
            final PlotInventory plotInventory, final int index,
            final PlotItemStack item
    );

    /**
     * Attempts to set an item into a {@link PlotInventory}
     *
     * @param plotInventory The inventory where the item should be placed
     * @param index         The index where to place the item
     * @param item          The item to place into the inventory
     * @see #setItemChecked(PlotInventory, int, PlotItemStack)
     */
    public void setItem(
            final PlotInventory plotInventory, final int index,
            final PlotItemStack item
    ) {
        setItemChecked(plotInventory, index, item);
    }

    public abstract PlotItemStack[] getItems(final PlotPlayer<?> player);

    public abstract boolean isOpen(final PlotInventory plotInventory);

}
