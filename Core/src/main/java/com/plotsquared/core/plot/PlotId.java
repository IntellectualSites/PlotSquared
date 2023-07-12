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
package com.plotsquared.core.plot;

import com.plotsquared.core.location.Direction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Plot (X,Y) tuples for plot locations
 * within a plot area
 */
public final class PlotId {

    private final int x;
    private final int y;
    private final int hash;

    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    private PlotId(final int x, final int y) {
        this.x = x;
        this.y = y;
        this.hash = (this.getX() << 16) | (this.getY() & 0xFFFF);
    }

    /**
     * Create a new plot ID instance
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     * @return a new PlotId at x,y
     */
    public static @NonNull PlotId of(final int x, final int y) {
        return new PlotId(x, y);
    }

    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     * @return the PlotId representation of the argument
     * @throws IllegalArgumentException if the string does not contain a valid PlotId
     */
    public static @NonNull PlotId fromString(final @NonNull String string) {
        final PlotId plot = fromStringOrNull(string);
        if (plot == null) {
            throw new IllegalArgumentException("Cannot create PlotID. String invalid.");
        }
        return plot;
    }

    /**
     * Attempt to parse a plot ID from a string
     *
     * @param string ID string
     * @return Plot ID, or {@code null} if none could be parsed
     */
    public static @Nullable PlotId fromStringOrNull(final @NonNull String string) {
        final String[] parts = string.split("[;_,.]");
        if (parts.length < 2) {
            return null;
        }
        int x;
        int y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (final NumberFormatException ignored) {
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
    public static @NonNull PlotId unpair(final int hash) {
        return PlotId.of(hash >> 16, hash & 0xFFFF);
    }

    /**
     * Get the ID X component
     *
     * @return X component
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the ID Y component
     *
     * @return Y component
     */
    public int getY() {
        return this.y;
    }

    /**
     * Get the next plot ID for claiming purposes
     *
     * @return Next plot ID
     */
    public @NonNull PlotId getNextId() {
        final int absX = Math.abs(x);
        final int absY = Math.abs(y);
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
    public @NonNull PlotId getRelative(final @NonNull Direction direction) {
        return switch (direction) {
            case NORTH -> PlotId.of(this.getX(), this.getY() - 1);
            case EAST -> PlotId.of(this.getX() + 1, this.getY());
            case SOUTH -> PlotId.of(this.getX(), this.getY() + 1);
            case WEST -> PlotId.of(this.getX() - 1, this.getY());
            default -> this;
        };
    }

    @Override
    public boolean equals(final Object obj) {
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
    @Override
    public @NonNull String toString() {
        return this.getX() + ";" + this.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by a specified string
     *
     * @param separator Separator
     * @return {@code x + separator + y}
     */
    public @NonNull String toSeparatedString(String separator) {
        return this.getX() + separator + this.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by ","
     *
     * @return {@code x + "," + y}
     */
    public @NonNull String toCommaSeparatedString() {
        return this.getX() + "," + this.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by "_"
     *
     * @return {@code x + "_" + y}
     */
    public @NonNull String toUnderscoreSeparatedString() {
        return this.getX() + "_" + this.getY();
    }

    /**
     * Get a String representation of the plot ID where the
     * components are separated by "-"
     *
     * @return {@code x + "-" + y}
     */
    public @NonNull String toDashSeparatedString() {
        return this.getX() + "-" + this.getY();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }


    public static final class PlotRangeIterator implements Iterator<PlotId>, Iterable<PlotId> {

        private final PlotId start;
        private final PlotId end;

        private int x;
        private int y;

        private PlotRangeIterator(final @NonNull PlotId start, final @NonNull PlotId end) {
            this.start = start;
            this.end = end;
            this.x = this.start.getX();
            this.y = this.start.getY();
        }

        public static PlotRangeIterator range(final @NonNull PlotId start, final @NonNull PlotId end) {
            return new PlotRangeIterator(start, end);
        }

        @Override
        public boolean hasNext() {
            // end is fully included
            return this.x <= this.end.getX() && this.y <= this.end.getY();
        }

        @Override
        public PlotId next() {
            if (!hasNext()) {
                throw new NoSuchElementException("The iterator has no more entries");
            }
            // increment *after* getting the result to include the minimum
            // the id to return
            PlotId result = PlotId.of(this.x, this.y);
            // first increase y, then x
            if (this.y == this.end.getY()) {
                this.x++;
                this.y = this.start.getY();
            } else {
                this.y++;
            }
            return result;
        }

        @NonNull
        @Override
        public Iterator<PlotId> iterator() {
            return this;
        }

    }

}
