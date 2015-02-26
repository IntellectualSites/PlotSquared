////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.object;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Wrapper class for blocks, using pure data rather than the object.
 * <p/>
 * Useful for NMS
 *
 * @author Empire92
 * @author Citymonstret
 */
public class BlockWrapper {
    /**
     * X Coordinate
     */
    public final int x;
    /**
     * Y Coordinate
     */
    public final int y;
    /**
     * Z Coordinate
     */
    public final int z;
    /**
     * Block ID
     */
    public final int id;
    /**
     * Block Data Value
     */
    public final byte data;

    /**
     * Constructor
     *
     * @param x    X Loc Value
     * @param y    Y Loc Value
     * @param z    Z Loc Value
     * @param id   Material ID
     * @param data Data Value
     */
    public BlockWrapper(final int x, final int y, final int z, final short id, final byte data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }

    /**
     * Alternative Constructor Uses block data, rather than typed data
     *
     * @param block Block from which we get the data
     */
    @SuppressWarnings({ "deprecation", "unused" })
    public BlockWrapper(final Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.id = block.getTypeId();
        this.data = block.getData();
    }

    /**
     * Get a block based on the block wrapper
     *
     * @param world World in which the block is/will be, located
     *
     * @return block created/fetched from settings
     */
    @SuppressWarnings({ "unused", "deprecation" })
    public Block toBlock(final World world) {
        final Block block = world.getBlockAt(this.x, this.y, this.z);
        block.setTypeIdAndData(this.id, this.data, true);
        return block;
    }
}
