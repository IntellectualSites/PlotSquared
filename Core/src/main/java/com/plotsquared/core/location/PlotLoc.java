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
package com.plotsquared.core.location;

import com.plotsquared.core.util.StringMan;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * (x,y,z) or (x,z) representation for PlotSquared (hence the "Plot" prefix)
 */
public final class PlotLoc {

    private final int x;
    private final int y;
    private final int z;

    /**
     * Initialize a new {@link PlotLoc} and set the Y value to {@code -1}
     *
     * @param x X value
     * @param z Z value
     */
    public PlotLoc(final int x, final int z) {
        this(x, -1, z);
    }

    public PlotLoc(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static @Nullable PlotLoc fromString(final String input) {
        if (input == null || "side".equalsIgnoreCase(input)) {
            return null;
        } else if (StringMan.isEqualIgnoreCaseToAny(input, "center", "middle", "centre")) {
            return new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                String[] split = input.split(",");
                if (split.length == 2) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (split.length == 3) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                            Integer.parseInt(split[2])
                    );
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.getX();
        result = (prime * result) + this.getY();
        result = (prime * result) + this.getZ();
        return result;
    }

    @Override
    public String toString() {
        if (this.getY() == -1) {
            return String.format("%d,%d", x, z);
        }
        return String.format("%d,%d,%d", x, y, z);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlotLoc other = (PlotLoc) obj;
        return (this.getX() == other.getX()) && (this.getY() ==
                other.getY()) && (this.getZ() == other.getZ());
    }

}
