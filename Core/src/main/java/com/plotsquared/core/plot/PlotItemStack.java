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

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

public class PlotItemStack {

    private final int amount;
    private final String name;
    private final String[] lore;
    private final ItemType type;

    /**
     * @param id     String ID
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     */
    public PlotItemStack(
            final String id, final int amount, final String name,
            final String... lore
    ) {
        this(ItemTypes.get(id), amount, name, lore);
    }

    /**
     * @param type   The item type
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     * @since 6.5.0
     */
    public PlotItemStack(
            final ItemType type, final int amount, final String name,
            final String... lore
    ) {
        this.type = type;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public BlockState getBlockState() {
        return getType().getBlockType().getDefaultState();
    }

    public ItemType getType() {
        return this.type;
    }

    /**
     * Returns the number of items in this stack.
     * Valid values range from 1-255.
     *
     * @return the amount of items in this stack
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns the given name of this stack of items. The name is displayed when
     * hovering over the item.
     *
     * @return the given name of this stack of items
     */
    public String getName() {
        return name;
    }
    public String[] getLore() {
        return lore;
    }

}
