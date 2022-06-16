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
package com.plotsquared.core.util.query;

/**
 * Strategy used when sorting plot results
 */
public enum SortingStrategy {
    /**
     * Plots won't be sorted at all
     */
    NO_SORTING,
    /**
     * Sort by the temporary (magic) plot ID
     */
    SORT_BY_TEMP,
    /**
     * Sort by the value in the plot's {@link com.plotsquared.core.plot.flag.implementations.DoneFlag}
     */
    SORT_BY_DONE,
    /**
     * Sort by the plot rating
     */
    SORT_BY_RATING,
    /**
     * Sort by creation date
     */
    SORT_BY_CREATION,
    /**
     * Sort using a comparator
     */
    COMPARATOR
}
