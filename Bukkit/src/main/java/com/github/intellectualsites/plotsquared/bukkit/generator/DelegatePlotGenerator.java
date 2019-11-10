package com.github.intellectualsites.plotsquared.bukkit.generator;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
                    @Range(from = 0, to = 15) int z, Biome biome) {
                    result.setBiome(x, z, BukkitAdapter.adapt(biome));
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
