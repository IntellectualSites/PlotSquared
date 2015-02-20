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
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;

/**
 * Random utilities
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public class RUtils {
    /**
     * Get the total allocated ram
     *
     * @return total ram
     */
    public static long getTotalRam() {
        return (Runtime.getRuntime().totalMemory() / 1024) / 1024;
    }
    
    /**
     * Get the total free ram
     *
     * @return free ram
     */
    public static long getFreeRam() {
        return (Runtime.getRuntime().freeMemory() / 1024) / 1024;
    }
    
    /**
     * Percentage of used ram
     *
     * @return percentage
     */
    public static long getRamPercentage() {
        return (getFreeRam() / getTotalRam()) * 100;
    }
    
    /**
     * Get formatted time
     *
     * @param sec seconds
     *
     * @return formatted time
     */
    public static String formatTime(final double sec) {
        final double h = sec / 3600;
        final double m = (sec % 3600) / 60;
        final double s = sec % 60;
        final String string = C.TIME_FORMAT.s();
        final String s_h = (int) h + " " + ((int) h != 1 ? "hours" : "hour");
        final String s_m = (int) m + " " + ((int) m != 1 ? "minutes" : "minute");
        final String s_s = (int) s + " " + ((int) s != 1 ? "seconds" : "second");
        return string.replaceAll("%sec%", s_s).replaceAll("%min%", s_m).replaceAll("%hours%", s_h);
    }
    
    /**
     * Force textures on the client
     *
     * @param p texture to force
     */
    public void forceTexture(final Player p) {
        p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
    }
    
    public Direction getDirection(final Location l) {
        final double d = ((l.getYaw() * 4.0F) / 360.0F) + 0.5D;
        final int i = (int) d;
        final int x = d < i ? i - 1 : i;
        switch (x) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.WEST;
            default:
                return null;
        }
    }
    
    public boolean compareDirections(final Location l1, final Location l2) {
        return getDirection(l1) == getDirection(l2);
    }
    
    enum Direction {
        SOUTH(0),
        EAST(1),
        NORTH(2),
        WEST(3);
        private final int i;
        
        Direction(final int i) {
            this.i = i;
        }
        
        public int getInt() {
            return this.i;
        }
    }
}
