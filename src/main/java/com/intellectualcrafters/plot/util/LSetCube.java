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
package com.intellectualcrafters.plot.util;

import org.bukkit.Location;

/**
 * Cube utilities
 *
 * @author Citymonstret
 */
@SuppressWarnings({ "javadoc", "unused" })
public class LSetCube {
    /**
     * Base locations
     */
    private Location l1, l2;

    /**
     * Constructor
     *
     * @param l1
     * @param l2
     */
    public LSetCube(final Location l1, final Location l2) {
        this.l1 = l1;
        this.l1 = l2;
    }

    /**
     * Secondary constructor
     *
     * @param l1
     * @param size
     */
    public LSetCube(final Location l1, final int size) {
        this.l1 = l1;
        this.l2 = l1.clone().add(size, size, size);
    }

    /**
     * Returns the absolute min. of the cube
     *
     * @return abs. min
     */
    public Location minLoc() {
        final int x = Math.min(this.l1.getBlockX(), this.l2.getBlockX());
        final int y = Math.min(this.l1.getBlockY(), this.l2.getBlockY());
        final int z = Math.min(this.l1.getBlockZ(), this.l2.getBlockZ());
        return new Location(this.l1.getWorld(), x, y, z);
    }

    /**
     * Returns the absolute max. of the cube
     *
     * @return abs. max
     */
    public Location maxLoc() {
        final int x = Math.max(this.l1.getBlockX(), this.l2.getBlockX());
        final int y = Math.max(this.l1.getBlockY(), this.l2.getBlockY());
        final int z = Math.max(this.l1.getBlockZ(), this.l2.getBlockZ());
        return new Location(this.l1.getWorld(), x, y, z);
    }

    /**
     * Creates a LCycler for the cube.
     *
     * @return new lcycler
     */
    public LCycler getCycler() {
        return new LCycler(this);
    }

    /**
     * @author Citymonstret
     */
    protected class LCycler {
        /**
         *
         */
        private final Location min;
        /**
         *
         */
        private final Location max;
        /**
         *
         */
        private Location current;

        /**
         * @param cube
         */
        public LCycler(final LSetCube cube) {
            this.min = cube.minLoc();
            this.max = cube.maxLoc();
            this.current = this.min;
        }

        /**
         * @return
         */
        public boolean hasNext() {
            return ((this.current.getBlockX() + 1) <= this.max.getBlockX()) && ((this.current.getBlockY() + 1) <= this.max.getBlockY()) && ((this.current.getBlockZ() + 1) <= this.max.getBlockZ());
        }

        /**
         * @return
         */
        public Location getNext() {
            if (!hasNext()) {
                return null;
            }
            this.current = this.current.add(1, 1, 1);
            return this.current;
        }
    }
}
