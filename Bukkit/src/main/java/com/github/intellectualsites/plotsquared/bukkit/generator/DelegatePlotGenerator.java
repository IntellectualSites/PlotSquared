package com.github.intellectualsites.plotsquared.bukkit.generator;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Random;

@RequiredArgsConstructor final class DelegatePlotGenerator extends IndependentPlotGenerator {

    private final ChunkGenerator chunkGenerator;
    private final String world;

    @Override public void initialize(PlotArea area) {
    }

    @Override public PlotManager getNewPlotManager() {
        return PlotSquared.get().IMP.getDefaultGenerator().getNewPlotManager();
    }

    @Override public String getName() {
        return this.chunkGenerator.getClass().getName();
    }

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return PlotSquared.get().IMP.getDefaultGenerator().getNewPlotArea(world, id, min, max);
    }

    @Override public BlockBucket[][] generateBlockBucketChunk(PlotArea settings) {
        BlockBucket[][] blockBuckets = new BlockBucket[16][];
        HybridPlotWorld hpw = (HybridPlotWorld) settings;
        // Bedrock
        if (hpw.PLOT_BEDROCK) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    blockBuckets[0][(z << 4) | x] =
                        BlockBucket.withSingle(PlotBlock.get("bedrock"));
                }
            }
        }
        for (short x = 0; x < 16; x++) {
            for (short z = 0; z < 16; z++) {
                for (int y = 1; y < hpw.PLOT_HEIGHT; y++) {
                    blockBuckets[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = hpw.MAIN_BLOCK;
                }
                blockBuckets[hpw.PLOT_HEIGHT >> 4][((hpw.PLOT_HEIGHT & 0xF) << 8) | (z << 4) | x] =
                    hpw.MAIN_BLOCK;
            }
        }
        return blockBuckets;
    }

    @Override public void generateChunk(final ScopedLocalBlockQueue result, PlotArea settings) {
        World world = BukkitUtil.getWorld(this.world);
        Location min = result.getMin();
        int chunkX = min.getX() >> 4;
        int chunkZ = min.getZ() >> 4;
        Random random = new Random(MathMan.pair((short) chunkX, (short) chunkZ));
        try {
            ChunkGenerator.BiomeGrid grid = new ChunkGenerator.BiomeGrid() {
                @Override public void setBiome(@Range(from = 0, to = 15) int x,
                    @Range(from = 0, to = 15) int z, Biome biome) {
                    result.setBiome(x, z, biome.name());
                }

                @Override @NotNull public Biome getBiome(int x, int z) {
                    return Biome.FOREST;
                }
            };
            chunkGenerator.generateChunkData(world, random, chunkX, chunkZ, grid);
            return;
        } catch (Throwable ignored) {
        }
        for (BlockPopulator populator : chunkGenerator.getDefaultPopulators(world)) {
            populator.populate(world, random, world.getChunkAt(chunkX, chunkZ));
        }
    }

}
