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
import com.plotsquared.core.plot.PlotArea;

import javax.annotation.Nullable;
import java.util.Comparator;

public class PlotInPrioritizedAreaComparator implements Comparator<Plot> {

    private final PlotArea priorityArea;

    public PlotInPrioritizedAreaComparator(@Nullable final PlotArea area) {
        this.priorityArea = area;
    }

    @Override
    public int compare(final Plot first, final Plot second) {
        if (this.priorityArea == null) {
            return 0; // no defined priority? don't sort
        }
        if (this.priorityArea.equals(first.getArea())) {
            return -1;
        }
        if (this.priorityArea.equals(second.getArea())) {
            return 1;
        }
        return 0; // same area, don't sort
    }

}
