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
package com.plotsquared.core.util.comparator;

import com.plotsquared.core.plot.Plot;

import java.util.Comparator;

/**
 * Sort plots by {@link Plot#temp} (being the auto increment id in database) in natural order for {@code temp > 0}.
 * For {@code temp < 1} sort by {@link Plot#hashCode()}
 */
public class PlotByCreationDateComparator implements Comparator<Plot> {

    public static final Comparator<Plot> INSTANCE = new PlotByCreationDateComparator();

    private PlotByCreationDateComparator() {
    }

    @Override
    @SuppressWarnings("deprecation") // Plot#temp
    public int compare(final Plot first, final Plot second) {
        if (first.temp > 0 && second.temp > 0) {
            return Integer.compare(first.temp, second.temp);
        }
        // second is implicitly `< 1` (due to previous condition)
        if (first.temp > 0) {
            return 1;
        }
        // sort dangling plots (temp < 1) by their hashcode
        return Integer.compare(first.hashCode(), second.hashCode());
    }

}
