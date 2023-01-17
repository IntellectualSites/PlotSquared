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

import com.plotsquared.core.location.Location;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Iterator;

public class RegionUtil {

    public static @NonNull Location[] getCorners(
            final @NonNull String world,
            final @NonNull CuboidRegion region
    ) {
        final BlockVector3 min = region.getMinimumPoint();
        final BlockVector3 max = region.getMaximumPoint();
        return new Location[]{Location.at(world, min), Location.at(world, max)};
    }

    public static @NonNull Location[] getCorners(String world, Collection<CuboidRegion> regions) {
        CuboidRegion aabb = getAxisAlignedBoundingBox(regions);
        return getCorners(world, aabb);
    }

    /**
     * Create a minimum {@link CuboidRegion} containing all given regions.
     *
     * @param regions The regions the bounding box should contain.
     * @return a CuboidRegion that contains all given regions.
     */
    public static @NonNull CuboidRegion getAxisAlignedBoundingBox(Iterable<CuboidRegion> regions) {
        Iterator<CuboidRegion> iterator = regions.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("No regions given");
        }
        CuboidRegion next = iterator.next();
        BlockVector3 min = next.getMinimumPoint();
        BlockVector3 max = next.getMaximumPoint();

        while (iterator.hasNext()) {
            next = iterator.next();
            // as max >= min, this is enough to check
            min = min.getMinimum(next.getMinimumPoint());
            max = max.getMaximum(next.getMaximumPoint());
        }
        return new CuboidRegion(min, max);
    }

    public static CuboidRegion createRegion(
            int pos1x, int pos2x, int pos1y, int pos2y, int pos1z,
            int pos2z
    ) {
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

    // Because WorldEdit (not FastAsyncWorldEdit) lack this for CuboidRegion
    public static boolean intersects(CuboidRegion region, CuboidRegion other) {
        BlockVector3 regionMin = region.getMinimumPoint();
        BlockVector3 regionMax = region.getMaximumPoint();

        BlockVector3 otherMin = other.getMinimumPoint();
        BlockVector3 otherMax = other.getMaximumPoint();

        return otherMin.getX() <= regionMax.getX() && otherMax.getX() >= regionMin.getX()
                && otherMin.getZ() <= regionMax.getZ() && otherMax.getZ() >= regionMin.getZ();
    }

}
