package com.github.intellectualsites.plotsquared.util;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

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
