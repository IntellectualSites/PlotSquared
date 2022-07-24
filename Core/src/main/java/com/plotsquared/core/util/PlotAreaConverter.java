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
package com.plotsquared.core.util;

import com.plotsquared.core.plot.PlotArea;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.khelekore.prtree.MBRConverter;

public class PlotAreaConverter implements MBRConverter<PlotArea> {

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMin(final int axis, final PlotArea area) {
        final CuboidRegion region = area.getRegion();
        if (axis == AXIS_X) {
            return region.getMinimumPoint().getX();
        } else if (axis == AXIS_Y) {
            return region.getMinimumPoint().getY();
        } else if (axis == AXIS_Z) {
            return region.getMinimumPoint().getZ();
        } else {
            throw new IllegalArgumentException("Unknown axis: " + axis);
        }
    }

    @Override
    public double getMax(final int axis, final PlotArea area) {
        final CuboidRegion region = area.getRegion();
        if (axis == AXIS_X) {
            return region.getMaximumPoint().getX();
        } else if (axis == AXIS_Y) {
            return region.getMaximumPoint().getY();
        } else if (axis == AXIS_Z) {
            return region.getMaximumPoint().getZ();
        } else {
            throw new IllegalArgumentException("Unknown axis: " + axis);
        }
    }

}
