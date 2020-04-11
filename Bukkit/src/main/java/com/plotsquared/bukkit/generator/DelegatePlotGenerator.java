package com.plotsquared.bukkit.generator;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.PlotSquared;
import com.plotsquared.generator.IndependentPlotGenerator;
import com.plotsquared.location.Location;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotId;
import com.plotsquared.util.MathMan;
import com.plotsquared.queue.ScopedLocalBlockQueue;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import java.util.Random;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

final class DelegatePlotGenerator extends IndependentPlotGenerator {

    private final ChunkGenerator chunkGenerator;
    private final String world;

    public DelegatePlotGenerator(ChunkGenerator chunkGenerator, String world) {
        this.chunkGenerator = chunkGenerator;
        this.world = world;
    }

    @Override public void initialize(PlotArea area) {
    }

    @Override public String getName() {
        return this.chunkGenerator.getClass().getName();
    }

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return PlotSquared.get().IMP.getDefaultGenerator().getNewPlotArea(world, id, min, max);
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
                    @Range(from = 0, to = 15) int z, @NotNull Biome biome) {
                    result.setBiome(x, z, BukkitAdapter.adapt(biome));
                }

                //do not annotate with Override until we discontinue support for 1.4.4
                public void setBiome(int x, int y, int z, @NotNull Biome biome) {
                    result.setBiome(x, z, BukkitAdapter.adapt(biome));

                }

                @Override @NotNull public Biome getBiome(int x, int z) {
                    return Biome.FOREST;
                }

                @Override
                public @NotNull Biome getBiome(int x, int y, int z) {
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
