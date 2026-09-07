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
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.MathMan;

import java.util.Comparator;

/**
 * Sort plots by their {@link DoneFlag} in reverse numeric natural order. (more recent "finished plots" first)
 * <br>
 * Non-finished plots last, unsorted.
 */
public class PlotByDoneComparator implements Comparator<Plot> {

    public static final PlotByDoneComparator INSTANCE = new PlotByDoneComparator();

    private PlotByDoneComparator() {
    }

    @Override
    public int compare(final Plot first, final Plot second) {
        String firstDone = first.getFlag(DoneFlag.class);
        String lastDone = second.getFlag(DoneFlag.class);
        if (MathMan.isInteger(firstDone)) {
            if (MathMan.isInteger(lastDone)) {
                return Integer.parseInt(lastDone) - Integer.parseInt(firstDone);
            }
            return -1; // only "first" is finished, so sort "second" after "first"
        }
        return 0; // neither is finished
    }

}
