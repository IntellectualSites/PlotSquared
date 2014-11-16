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

/**
 * Wrapper class for blocks, using
 * pure data rather than the object.
 * <p/>
 * Useful for NMS
 *
 * @author Empire92
 */
public class BlockWrapper {

    // Public Final //////////////////////////
    public final int x;                     //
    public final int y;                     //
    public final int z;                     //
    public final int id;                    //
    public final byte data;                 //
    //////////////////////////////////////////

    /**
     * Constructor
     *
     * @param x    X Loc Value
     * @param y    Y Loc Value
     * @param z    Z Loc Value
     * @param id   Material ID
     * @param data Data Value
     */
    public BlockWrapper(int x, int y, int z, short id, byte data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }
}
