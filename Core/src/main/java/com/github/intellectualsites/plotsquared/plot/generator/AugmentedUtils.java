package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotAreaType;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.util.block.DelegateLocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.world.RegionUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AugmentedUtils {

    private static boolean enabled = true;

    public static void bypass(boolean bypass, Runnable run) {
        enabled = bypass;
        run.run();
        enabled = true;
    }

    public static boolean generate(@NotNull final String world, final int chunkX, final int chunkZ,
        LocalBlockQueue queue) {
        if (!enabled) {
            return false;
        }

        final int blockX = chunkX << 4;
        final int blockZ = chunkZ << 4;
        CuboidRegion region = RegionUtil.createRegion(blockX, blockX + 15, blockZ, blockZ + 15);
        Set<PlotArea> areas = PlotSquared.get().getPlotAreas(world, region);
        if (areas.isEmpty()) {
            return false;
        }
        boolean toReturn = false;
        for (final PlotArea area : areas) {
            if (area.getType() == PlotAreaType.NORMAL) {
                return false;
            }
            if (area.getTerrain() == 3) {
                continue;
            }
            IndependentPlotGenerator generator = area.getGenerator();
            // Mask
            if (queue == null) {
                queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
            }
            LocalBlockQueue primaryMask;
            // coordinates
            int bxx;
            int bzz;
            int txx;
            int tzz;
            // gen
            if (area.getType() == PlotAreaType.PARTIAL) {
                bxx = Math.max(0, area.getRegion().getMinimumPoint().getX() - blockX);
                bzz = Math.max(0, area.getRegion().getMinimumPoint().getZ() - blockZ);
                txx = Math.min(15, area.getRegion().getMaximumPoint().getX() - blockX);
                tzz = Math.min(15, area.getRegion().getMaximumPoint().getZ() - blockZ);
                primaryMask = new DelegateLocalBlockQueue(queue) {
                    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
                        if (area.contains(x, z)) {
                            return super.setBlock(x, y, z, id);
                        }
                        return false;
                    }

                    @Override public boolean setBiome(int x, int z, BiomeType biome) {
                        if (area.contains(x, z)) {
                            return super.setBiome(x, z, biome);
                        }
                        return false;
                    }
                };
            } else {
                bxx = bzz = 0;
                txx = tzz = 15;
                primaryMask = queue;
            }
            LocalBlockQueue secondaryMask;
            BlockState air = BlockTypes.AIR.getDefaultState();
            if (area.getTerrain() == 2) {
                PlotManager manager = area.getPlotManager();
                final boolean[][] canPlace = new boolean[16][16];
                boolean has = false;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        int rx = x + blockX;
                        int rz = z + blockZ;
                        boolean can = manager.getPlotId(rx, 0, rz) == null;
                        if (can) {
                            for (int y = 1; y < 128; y++) {
                                queue.setBlock(rx, y, rz, air);
                            }
                            canPlace[x][z] = can;
                            has = true;
                        }
                    }
                }
                if (!has) {
                    continue;
                }
                toReturn = true;
                secondaryMask = new DelegateLocalBlockQueue(primaryMask) {
                    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
                        if (canPlace[x - blockX][z - blockZ]) {
                            return super.setBlock(x, y, z, id);
                        }
                        return false;
                    }

                    @Override public boolean setBiome(int x, int y, BiomeType biome) {
                        return super.setBiome(x, y, biome);
                    }
                };
            } else {
                secondaryMask = primaryMask;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        for (int y = 1; y < 128; y++) {
                            queue.setBlock(blockX + x, y, blockZ + z, air);
                        }
                    }
                }
                toReturn = true;
            }
            ScopedLocalBlockQueue scoped = new ScopedLocalBlockQueue(secondaryMask,
                new Location(area.getWorldName(), blockX, 0, blockZ),
                new Location(area.getWorldName(), blockX + 15, 255, blockZ + 15));
            generator.generateChunk(scoped, area);
            generator.populateChunk(scoped, area);
        }
        if (queue != null) {
            queue.flush();
        }
        return toReturn;
    }
}
