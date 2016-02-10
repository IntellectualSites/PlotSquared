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

public class PlotId {
    /**
     * x value
     */
    public Integer x;
    /**
     * y value
     */
    public Integer y;
    
    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    public PlotId(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     *
     * @return null if the string is invalid
     */
    public static PlotId fromString(final String string) {
        if (string == null) {
            return null;
        }
        int x, y;
        final String[] parts = string.split(";");
        if (parts.length < 2) {
            return null;
        }
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (final Exception e) {
            return null;
        }
        return new PlotId(x, y);
    }
    
    public PlotId getRelative(final int direction) {
        switch (direction) {
            case 0:
                return new PlotId(this.x, this.y - 1);
            case 1:
                return new PlotId(this.x + 1, this.y);
            case 2:
                return new PlotId(this.x, this.y + 1);
            case 3:
                return new PlotId(this.x - 1, this.y);
        }
        return this;
    }
    
    public PlotId getRelative(int x, int y) {
        return new PlotId(this.x + x, this.y + y);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlotId other = (PlotId) obj;
        return ((x.equals(other.x)) && (y.equals(other.y)));
    }
    
    @Override
    public String toString() {
        return x + ";" + y;
    }
    
    public static PlotId unpair(int hash) {
        return new PlotId(hash >> 16, hash & 0xFFFF);
    }
    
    private int hash;
    
    public void recalculateHash() {
        hash = 0;
        hashCode();
    }
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = (x << 16) | (y & 0xFFFF);
        }
        return hash;
    }
}
