package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;

public class SingleWorldGenerator extends IndependentPlotGenerator {
    private Location bedrock1 = new Location(null, 0, 0, 0);
    private Location bedrock2 = new Location(null, 15, 0, 15);
    private Location dirt1 = new Location(null, 0, 1, 0);
    private Location dirt2 = new Location(null, 15, 2, 15);
    private Location grass1 = new Location(null, 0, 3, 0);
    private Location grass2 = new Location(null, 15, 3, 15);

    @Override public String getName() {
        return "PlotSquared:single";
    }

    @Override public BlockBucket[][] generateBlockBucketChunk(PlotArea settings) {
        BlockBucket[][] blockBuckets = new BlockBucket[16][];
        SinglePlotArea area = (SinglePlotArea) settings;
        if (area.VOID) {
            return blockBuckets;
        }
        for (int x = bedrock1.getX(); x <= bedrock2.getX(); x++) {
            for (int z = bedrock1.getZ(); z <= bedrock2.getZ(); z++) {
                for (int y = bedrock1.getY(); y <= bedrock2.getY(); y++) {
                    int layer = y >> 4;
                    if (blockBuckets[layer] == null) {
                        blockBuckets[layer] = new BlockBucket[4096];
                    }
                    blockBuckets[layer][((y & 0xF) << 8) | (z << 4) | x] =
                        BlockBucket.withSingle(BlockUtil.get("bedrock"));
                }
            }
        }
        for (int x = dirt1.getX(); x <= dirt2.getX(); x++) {
            for (int z = dirt1.getZ(); z <= dirt2.getZ(); z++) {
                for (int y = dirt1.getY(); y <= dirt2.getY(); y++) {
                    int layer = y >> 4;
                    if (blockBuckets[layer] == null) {
                        blockBuckets[layer] = new BlockBucket[4096];
                    }
                    blockBuckets[layer][((y & 0xF) << 8) | (z << 4) | x] =
                        BlockBucket.withSingle(BlockUtil.get("dirt"));
                }
            }
        }
        for (int x = grass1.getX(); x <= grass2.getX(); x++) {
            for (int z = grass1.getZ(); z <= grass2.getZ(); z++) {
                for (int y = grass1.getY(); y <= grass2.getY(); y++) {
                    int layer = y >> 4;
                    if (blockBuckets[layer] == null) {
                        blockBuckets[layer] = new BlockBucket[4096];
                    }
                    blockBuckets[layer][((y & 0xF) << 8) | (z << 4) | x] =
                        BlockBucket.withSingle(BlockUtil.get("grass_block"));
                }
            }
        }
        return blockBuckets;
    }

    @Override public void generateChunk(ScopedLocalBlockQueue result, PlotArea settings) {
        SinglePlotArea area = (SinglePlotArea) settings;
        if (area.VOID) {
            Location min = result.getMin();
            if (min.getX() == 0 && min.getZ() == 0) {
                result.setBlock(0, 0, 0, BlockUtil.get("bedrock"));
            }
        } else {
            result.setCuboid(bedrock1, bedrock2, BlockUtil.get("bedrock"));
            result.setCuboid(dirt1, dirt2, BlockUtil.get("dirt"));
            result.setCuboid(grass1, grass2, BlockUtil.get("grass_block"));
        }
        result.fillBiome("PLAINS");
    }

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return ((SinglePlotAreaManager) PlotSquared.get().getPlotAreaManager()).getArea();
    }

    @Override public void initialize(PlotArea area) {

    }
}
