/*
 *
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
package com.plotsquared.bukkit.generator;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.queue.GenChunk;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.generator.SingleWorldGenerator;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import com.sk89q.worldedit.math.BlockVector2;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BukkitPlotGenerator extends ChunkGenerator
    implements GeneratorWrapper<ChunkGenerator> {

    @SuppressWarnings("unused") public final boolean PAPER_ASYNC_SAFE = true;

    private final IndependentPlotGenerator plotGenerator;
    private final ChunkGenerator platformGenerator;
    private final boolean full;
    private List<BlockPopulator> populators;
    private boolean loaded = false;

    @Getter private final String levelName;

    public BukkitPlotGenerator(String name, IndependentPlotGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator may not be null!");
        }
        this.levelName = name;
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.populators = new ArrayList<>();
        this.populators.add(new BlockStatePopulator(this.plotGenerator));
        this.full = true;
        MainUtil.initCache();
    }

    public BukkitPlotGenerator(final String world, final ChunkGenerator cg) {
        if (cg instanceof BukkitPlotGenerator) {
            throw new IllegalArgumentException("ChunkGenerator: " + cg.getClass().getName()
                + " is already a BukkitPlotGenerator!");
        }
        this.levelName = world;
        this.full = false;
        this.platformGenerator = cg;
        this.plotGenerator = new DelegatePlotGenerator(cg, world);
        MainUtil.initCache();
    }

    @Override public void augment(PlotArea area) {
        BukkitAugmentedGenerator.get(BukkitUtil.getWorld(area.getWorldName()));
    }

    @Override public boolean isFull() {
        return this.full;
    }

    @Override public IndependentPlotGenerator getPlotGenerator() {
        return this.plotGenerator;
    }

    @Override public ChunkGenerator getPlatformGenerator() {
        return this.platformGenerator;
    }

    @Override
    @NotNull
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        try {
            if (!this.loaded) {
                String name = world.getName();
                PlotSquared.get().loadWorld(name, this);
                Set<PlotArea> areas = PlotSquared.get().getPlotAreas(name);
                if (!areas.isEmpty()) {
                    PlotArea area = areas.iterator().next();
                    if (!area.isMobSpawning()) {
                        if (!area.isSpawnEggs()) {
                            world.setSpawnFlags(false, false);
                        }
                        world.setAmbientSpawnLimit(0);
                        world.setAnimalSpawnLimit(0);
                        world.setMonsterSpawnLimit(0);
                        world.setWaterAnimalSpawnLimit(0);
                    } else {
                        world.setSpawnFlags(true, true);
                        world.setAmbientSpawnLimit(-1);
                        world.setAnimalSpawnLimit(-1);
                        world.setMonsterSpawnLimit(-1);
                        world.setWaterAnimalSpawnLimit(-1);
                    }
                }
                this.loaded = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<BlockPopulator> toAdd = new ArrayList<>();
        List<BlockPopulator> existing = world.getPopulators();
        if (populators == null && platformGenerator != null) {
            populators = new ArrayList<>(platformGenerator.getDefaultPopulators(world));
        }
        if (populators != null) {
            for (BlockPopulator populator : this.populators) {
                if (!existing.contains(populator)) {
                    toAdd.add(populator);
                }
            }
        }
        return toAdd;
    }

    @Override
    @NotNull
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z,
        @NotNull BiomeGrid biome) {

        GenChunk result = new GenChunk();
        if (this.getPlotGenerator() instanceof SingleWorldGenerator) {
            if (result.getChunkData() != null) {
                for (int chunkX = 0; chunkX < 16; chunkX++) {
                    for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                        for (int y = 0; y < world.getMaxHeight(); y++) {
                            biome.setBiome(chunkX, y, chunkZ, Biome.PLAINS);

                        }
                    }
                }
                return result.getChunkData();
            }
        }
        // Set the chunk location
        result.setChunk(new ChunkWrapper(world.getName(), x, z));
        // Set the result data
        result.setChunkData(createChunkData(world));
        result.biomeGrid = biome;
        result.result = null;

        // Catch any exceptions (as exceptions usually thrown)
        try {
            // Fill the result data if necessary
            if (this.platformGenerator != this) {
                return this.platformGenerator.generateChunkData(world, random, x, z, biome);
            } else {
                generate(BlockVector2.at(x, z), world, result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // Return the result data
        return result.getChunkData();
    }

    private void generate(BlockVector2 loc, World world, ScopedLocalBlockQueue result) {
        // Load if improperly loaded
        if (!this.loaded) {
            String name = world.getName();
            PlotSquared.get().loadWorld(name, this);
            this.loaded = true;
        }
        // Process the chunk
        if (ChunkManager.preProcessChunk(loc, result)) {
            return;
        }
        PlotArea area = PlotSquared.get().getPlotArea(world.getName(), null);
        if (area == null && (area = PlotSquared.get().getPlotArea(this.levelName, null)) == null) {
            throw new IllegalStateException("Cannot regenerate chunk that does not belong to a plot area."
                + " Location: " + loc + ", world: " + world);
        }
        try {
            this.plotGenerator.generateChunk(result, area);
        } catch (Throwable e) {
            // Recover from generator error
            e.printStackTrace();
        }
        ChunkManager.postProcessChunk(loc, result);
    }

    @Override
    public boolean canSpawn(@NotNull final World world, final int x, final int z) {
        return true;
    }

    public boolean shouldGenerateCaves() {
        return false;
    }

    public boolean shouldGenerateDecorations() {
        return false;
    }

    public boolean isParallelCapable() {
        return true;
    }

    public boolean shouldGenerateMobs() {
        return false;
    }

    public boolean shouldGenerateStructures() {
        return true;
    }

    @Override public String toString() {
        if (this.platformGenerator == this) {
            return this.plotGenerator.getName();
        }
        if (this.platformGenerator == null) {
            return "null";
        } else {
            return this.platformGenerator.getClass().getName();
        }
    }

    @Override public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        return toString().equals(obj.toString()) || toString().equals(obj.getClass().getName());
    }

}
