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

import com.plotsquared.core.location.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlotId {

    @Deprecated public int x;
    @Deprecated public int y;
    private int hash;

    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    public PlotId(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     * @return the PlotId representation of the arguement
     * @throws IllegalArgumentException if the string does not contain a valid PlotId
     */
    @NotNull public static PlotId fromString(@NotNull String string) {
        PlotId plot = fromStringOrNull(string);
        if (plot == null)
            throw new IllegalArgumentException("Cannot create PlotID. String invalid.");
        return plot;
    }

    @Nullable public static PlotId fromStringOrNull(@NotNull String string) {
        String[] parts = string.split("[;,.]");
        if (parts.length < 2) {
            return null;
        }
        int x;
        int y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ignored) {
            return null;
        }
        return new PlotId(x, y);
    }

    public static PlotId of(@Nullable Plot plot) {
        return plot != null ? plot.getId() : null;
    }

    /**
     * Gets the PlotId from the HashCode<br>
     * Note: Only accurate for small x,z values (short)
     *
     * @param hash
     * @return
     */
    public static PlotId unpair(int hash) {
        return new PlotId(hash >> 16, hash & 0xFFFF);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public PlotId getNextId(int step) {
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        if (absX > absY) {
            if (x > 0) {
                return new PlotId(x, y + 1);
            } else {
                return new PlotId(x, y - 1);
            }
        } else if (absY > absX) {
            if (y > 0) {
                return new PlotId(x - 1, y);
            } else {
                return new PlotId(x + 1, y);
            }
        } else {
            if (x == y && x > 0) {
                return new PlotId(x, y + step);
            }
            if (x == absX) {
                return new PlotId(x, y + 1);
            }
            if (y == absY) {
                return new PlotId(x, y - 1);
            }
            return new PlotId(x + 1, y);
        }
    }

    public PlotId getRelative(Direction direction) {
        return getRelative(direction.getIndex());
    }

    /**
     * Get the PlotId in a relative direction
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     *
     * @param direction
     * @return PlotId
     */
    public PlotId getRelative(int direction) {
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

    /**
     * Get the PlotId in a relative location
     *
     * @param x
     * @param y
     * @return PlotId
     */
    public PlotId getRelative(int x, int y) {
        return new PlotId(this.x + x, this.y + y);
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlotId other = (PlotId) obj;
        return this.x == other.x && this.y == other.y;
    }

    /**
     * e.g.
     * 5;-6
     *
     * @return
     */
    @Override public String toString() {
        return this.x + ";" + this.y;
    }

    public String toCommaSeparatedString() {
        return this.x + "," + this.y;
    }



    /**
     * The PlotId object caches the hashcode for faster mapping/fetching/sorting<br>
     * - Recalculation is required if the x/y values change
     * TODO maybe make x/y values private and add this to the mutators
     */
    public void recalculateHash() {
        this.hash = 0;
        hashCode();
    }

    @Override public int hashCode() {
        if (this.hash == 0) {
            this.hash = (this.x << 16) | (this.y & 0xFFFF);
        }
        return this.hash;
    }
}
