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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Plot (X,Y) tuples for plot locations
 * within a plot area
 */
public class PlotId {

    private final int x;
    private final int y;
    private final int hash;

    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    private PlotId(int x, int y) {
        this.x = x;
        this.y = y;
        this.hash = (this.getX() << 16) | (this.getY() & 0xFFFF);
    }

    /**
     * Create a new plot ID instance
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    @Nonnull public static PlotId of(final int x, final int y) {
        return PlotId.of(x, y);
    }

    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     * @return the PlotId representation of the arguement
     * @throws IllegalArgumentException if the string does not contain a valid PlotId
     */
    @Nonnull public static PlotId fromString(@Nonnull String string) {
        PlotId plot = fromStringOrNull(string);
        if (plot == null)
            throw new IllegalArgumentException("Cannot create PlotID. String invalid.");
        return plot;
    }

    /**
     * Attempt to parse a plot ID from a string
     *
     * @param string ID string
     * @return Plot ID, or {@code null} if none could be parsed
     */
    @Nullable public static PlotId fromStringOrNull(@Nonnull String string) {
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
        return of(x, y);
    }

    /**
     * Gets the PlotId from the HashCode<br>
     * Note: Only accurate for small x,z values (short)
     *
     * @param hash ID hash
     * @return Plot ID
     */
    @Nonnull public static PlotId unpair(final int hash) {
        return PlotId.of(hash >> 16, hash & 0xFFFF);
    }

    /**
     * Get a copy of the plot ID
     *
     * @return Plot ID copy
     */
    @NotNull public PlotId copy() {
        return of(this.getX(), this.getY());
    }

    /**
     * Get the ID X component
     *
     * @return X component
     */
    public int getX() {
        return this.getX();
    }

    /**
     * Get the ID Y component
     *
     * @return Y component
     */
    public int getY() {
        return this.getY();
    }

    /**
     * Get the next plot ID for claiming purposes
     *
     * @return Next plot ID
     */
    @Nonnull public PlotId getNextId() {
        int absX = Math.abs(x);
        int absY = Math.abs(y);
        if (absX > absY) {
            if (x > 0) {
                return PlotId.of(x, y + 1);
            } else {
                return PlotId.of(x, y - 1);
            }
        } else if (absY > absX) {
            if (y > 0) {
                return PlotId.of(x - 1, y);
            } else {
                return PlotId.of(x + 1, y);
            }
        } else {
            if (x == y && x > 0) {
                return PlotId.of(x, y + 1);
            }
            if (x == absX) {
                return PlotId.of(x, y + 1);
            }
            if (y == absY) {
                return PlotId.of(x, y - 1);
            }
            return PlotId.of(x + 1, y);
        }
    }

    /**
     * Get the PlotId in a relative direction
     *
     * @param direction Direction
     * @return Relative plot ID
     */
    @Nonnull public PlotId getRelative(@Nonnull final Direction direction) {
        switch (direction) {
            case NORTH:
                return PlotId.of(this.getX(), this.getY() - 1);
            case EAST:
                return PlotId.of(this.getX() + 1, this.getY());
            case SOUTH:
                return PlotId.of(this.getX(), this.getY() + 1);
            case WEST:
                return PlotId.of(this.getX() - 1, this.getY());
        }
        return this;
    }

    @Override public boolean equals(final Object obj) {
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
        final PlotId other = (PlotId) obj;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by ";"
     *
     * @return {@code x + ";" + y}
     */
    @Override @Nonnull public String toString() {
        return this.getX() + ";" + this.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by ","
     *
     * @return {@code x + "," + y}
     */
    @Nonnull public String toCommaSeparatedString() {
        return this.getX() + "," + this.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by "-"
     *
     * @return {@code x + "-" + y}
     */
    @Nonnull public String toDashSeparatedString() {
        return this.getX() + "-" + this.getY();
    }

    @Override public int hashCode() {
        return this.hash;
    }


    public static final class PlotRangeIterator implements Iterator<PlotId>, Iterable<PlotId> {

        private final PlotId start;
        private final PlotId end;

        private int x;
        private int y;

        private PlotRangeIterator(@Nonnull final PlotId start, @Nonnull final PlotId end) {
            this.start = start;
            this.end = end;
            this.x = this.start.getX();
            this.y = this.start.getY();
        }

        public static PlotRangeIterator range(@Nonnull final PlotId start, @Nonnull final PlotId end) {
            return new PlotRangeIterator(start, end);
        }

        @Override public boolean hasNext() {
            if (this.x < this.end.getX()) {
                return true;
            } else if (this.x == this.end.getX()) {
                return this.y < this.end.getY();
            } else {
                return false;
            }
        }

        @Override public PlotId next() {
            if (!hasNext()) {
               throw new IndexOutOfBoundsException("The iterator has no more entries");
            }
            if (this.y == this.end.getY()) {
                this.x++;
                this.y = 0;
            } else {
                this.y++;
            }
            return PlotId.of(this.start.getX() + this.x, this.start.getY() + this.y);
        }

        @Nonnull @Override public Iterator<PlotId> iterator() {
            return this;
        }

    }

}
