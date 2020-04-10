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
package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * (x,y,z) or (x,z) representation for PlotSquared (hence the "Plot" prefix)
 */
@AllArgsConstructor public final class PlotLoc {

    /**
     * Use the getter
     *
     * @see #getX()
     * @deprecated
     */
    @Deprecated public int x;
    /**
     * Use the getter
     *
     * @see #getY()
     * @deprecated
     */
    @Deprecated public int y;
    /**
     * Use the getter
     *
     * @see #getZ()
     * @deprecated
     */
    @Deprecated public int z;

    /**
     * Initialize a new {@link PlotLoc} and set the Y value to {@code -1}
     *
     * @param x X value
     * @param z Y value
     */
    public PlotLoc(final int x, final int z) {
        this(x, -1, z);
    }

    @Nullable public static PlotLoc fromString(final String input) {
        if (input == null || "side".equalsIgnoreCase(input)) {
            return null;
        } else if (StringMan.isEqualIgnoreCaseToAny(input, "center", "middle")) {
            return new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                String[] split = input.split(",");
                if (split.length == 2) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (split.length == 3) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]));
                } else {
                    throw new IllegalArgumentException(
                        String.format("Unable to deserialize: %s", input));
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.x;
        result = (prime * result) + this.y;
        result = (prime * result) + this.z;
        return result;
    }

    @Override public String toString() {
        if (this.y == -1) {
            return String.format("%d,%d", x, z);
        }
        return String.format("%d,%d,%d", x, y, z);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlotLoc other = (PlotLoc) obj;
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }
}
