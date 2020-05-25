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
package com.plotsquared.core.util;

import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;

public class RegionUtil {
    public static CuboidRegion createRegion(int pos1x, int pos2x, int pos1z, int pos2z) {
        return createRegion(pos1x, pos2x, 0, Plot.MAX_HEIGHT - 1, pos1z, pos2z);
    }

    public static CuboidRegion createRegion(int pos1x, int pos2x, int pos1y, int pos2y, int pos1z,
        int pos2z) {
        BlockVector3 pos1 = BlockVector3.at(pos1x, pos1y, pos1z);
        BlockVector3 pos2 = BlockVector3.at(pos2x, pos2y, pos2z);
        return new CuboidRegion(pos1, pos2);
    }

    public static boolean contains(CuboidRegion region, int x, int z) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        return x >= min.getX() && x <= max.getX() && z >= min.getZ() && z <= max.getZ();
    }

    public static boolean contains(CuboidRegion region, int x, int y, int z) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        return x >= min.getX() && x <= max.getX() && z >= min.getZ() && z <= max.getZ() && y >= min
            .getY() && y <= max.getY();
    }

    @NotNull public static Rectangle2D toRectangle(@NotNull final CuboidRegion region) {
        final BlockVector2 min = region.getMinimumPoint().toBlockVector2();
        final BlockVector2 max = region.getMaximumPoint().toBlockVector2();
        return new Rectangle2D.Double(min.getX(), min.getZ(), max.getX(), max.getZ());
    }

    // Because WE (not fawe) lack this for CuboidRegion
    public static boolean intersects(CuboidRegion region, CuboidRegion other) {
        BlockVector3 regionMin = region.getMinimumPoint();
        BlockVector3 regionMax = region.getMaximumPoint();

        BlockVector3 otherMin = other.getMinimumPoint();
        BlockVector3 otherMax = other.getMaximumPoint();

        return otherMin.getX() <= regionMax.getX() && otherMax.getX() >= regionMin.getX()
            && otherMin.getZ() <= regionMax.getZ() && otherMax.getZ() >= regionMin.getZ();
    }
}
