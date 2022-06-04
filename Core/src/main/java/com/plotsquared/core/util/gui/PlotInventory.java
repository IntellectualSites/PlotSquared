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
package com.plotsquared.core.util.gui;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotItemStack;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * @param <P> The platform player
 * @param <I> The platform item object
 * @since TODO
 */
public abstract class PlotInventory<P, I> {

    private final PlotInventoryClickHandler NOOP_CLICK_HANDLER = (x, y) -> {
    };

    private final PlotPlayer<P> player;
    private final int size;
    private final Caption titleCaption;
    private final TagResolver[] titleResolvers;

    /**
     * Instantiates a new Plot inventory.
     *
     * @param size           The size of the inventory - must be a multiple of 9
     * @param titleCaption   The caption to use for the title
     * @param titleResolvers The tag resolvers to use for the title
     * @since 7.0.0
     */
    protected PlotInventory(PlotPlayer<P> player, int size, Caption titleCaption, TagResolver... titleResolvers) {
        this.player = player;
        this.size = size;
        this.titleCaption = titleCaption;
        this.titleResolvers = titleResolvers;
    }

    /**
     * Set an item in this inventory at a specific slot / index.
     * Overrides existing items and click handler.
     *
     * @param slot    The slot / index where to place the item
     * @param item    The item to add to this inventory
     * @param onClick The handler to call when clicking this item
     * @since TODO
     */
    public abstract void setItem(int slot, PlotItemStack item, PlotInventoryClickHandler onClick);

    /**
     * Set an item in this inventory at a specific slot / index.
     * Overrides existing items and click handler.
     *
     * @param slot The slot / index where to place the item
     * @param item The item to add to this inventory
     * @since TODO
     */
    public void setItem(int slot, PlotItemStack item) {
        setItem(slot, item, NOOP_CLICK_HANDLER);
    }

    /**
     * Add an item to this inventory, at the first slot possible (first empty slot, or first slot with the exact same item)
     *
     * @param item    The item to add to this inventory
     * @param onClick The handler to call when clicking this item
     * @since TODO
     */
    public abstract void addItem(PlotItemStack item, PlotInventoryClickHandler onClick);

    /**
     * Add an item to this inventory, at the first slot possible (first empty slot, or first slot with the exact same item)
     *
     * @param item The item to add to this inventory
     * @since TODO
     */
    public void addItem(PlotItemStack item) {
        addItem(item, NOOP_CLICK_HANDLER);
    }

    /**
     * Opens this inventory
     *
     * @since TODO
     */
    public abstract void open();

    /**
     * Close this inventory
     *
     * @since TODO
     */
    public abstract void close();

    public abstract I toPlatformItem(PlotItemStack item);

    /**
     * @return the size of this inventory (must be a multiple of 9)
     * @since TODO
     */
    public int size() {
        return size;
    }

    /**
     * Get the associated player of this inventory
     * @return {@link PlotPlayer}
     */
    public PlotPlayer<P> player() {
        return player;
    }

    protected Caption titleCaption() {
        return titleCaption;
    }

    protected TagResolver[] titleResolvers() {
        return titleResolvers;
    }

}
