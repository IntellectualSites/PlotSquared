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

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

public class PlotItemStack {

    public final int amount;
    public final String name;
    public final String[] lore;
    private final ItemType type;

    /**
     * @param id     Legacy numerical item ID
     * @param data   Legacy numerical item data
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     * @deprecated Use {@link #PlotItemStack(String, int, String, String...)}
     */
    @Deprecated public PlotItemStack(final int id, final short data, final int amount,
        final String name, final String... lore) {

        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.type = LegacyMapper.getInstance().getItemFromLegacy(id, data);
    }

    /**
     * @param id     String ID
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     */
    public PlotItemStack(final String id, final int amount, final String name,
        final String... lore) {
        this.type = ItemTypes.get(id);
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public BlockState getBlockState() {
        return type.getBlockType().getDefaultState();
    }

    public ItemType getType() {
        return this.type;
    }
}
