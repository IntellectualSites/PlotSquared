package com.github.intellectualsites.plotsquared.bukkit.generator;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

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
        World w = BukkitUtil.getWorld(world);
        Location min = result.getMin();
        int cx = min.getX() >> 4;
        int cz = min.getZ() >> 4;
        Random r = new Random(MathMan.pair((short) cx, (short) cz));
        ChunkGenerator.BiomeGrid grid = new ChunkGenerator.BiomeGrid() {
            @Override public void setBiome(int x, int z, Biome biome) {
                result.setBiome(x, z, biome.name());
            }

            @Override @NotNull public Biome getBiome(int x, int z) {
                return Biome.FOREST;
            }
        };
        try {
            chunkGenerator.generateChunkData(w, r, cx, cz, grid);
            return;
        } catch (Throwable ignored) {
        }
        for (BlockPopulator populator : chunkGenerator.getDefaultPopulators(w)) {
            populator.populate(w, r, w.getChunkAt(cx, cz));
        }
    }

}
