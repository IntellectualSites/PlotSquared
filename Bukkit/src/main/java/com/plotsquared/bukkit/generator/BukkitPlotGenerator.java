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
package com.plotsquared.bukkit.generator;

import com.plotsquared.bukkit.queue.GenChunk;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.BukkitWorld;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.ClassicPlotWorld;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.generator.SingleWorldGenerator;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.location.UncheckedWorldLocation;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.queue.ZeroedDelegateScopedQueueCoordinator;
import com.plotsquared.core.util.ChunkManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.HeightMap;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.function.Predicate.not;

public class BukkitPlotGenerator extends ChunkGenerator implements GeneratorWrapper<ChunkGenerator> {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + BukkitPlotGenerator.class.getSimpleName());

    @SuppressWarnings("unused")
    public final boolean PAPER_ASYNC_SAFE = true;

    private final PlotAreaManager plotAreaManager;
    private final IndependentPlotGenerator plotGenerator;
    private final ChunkGenerator platformGenerator;
    private final boolean full;
    private final String levelName;
    private final boolean useNewGenerationMethods;
    private final BiomeProvider biomeProvider;
    private List<BlockPopulator> populators;
    private boolean loaded = false;

    private PlotArea lastPlotArea;
    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;

    public BukkitPlotGenerator(
            final @NonNull String name,
            final @NonNull IndependentPlotGenerator generator,
            final @NonNull PlotAreaManager plotAreaManager
    ) {
        this.plotAreaManager = plotAreaManager;
        this.levelName = name;
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.populators = new ArrayList<>();
        int minecraftMinorVersion = PlotSquared.platform().serverVersion()[1];
        if (minecraftMinorVersion >= 17) {
            this.populators.add(new BlockStatePopulator(this.plotGenerator));
        } else {
            this.populators.add(new LegacyBlockStatePopulator(this.plotGenerator));
        }
        this.full = true;
        this.useNewGenerationMethods = PlotSquared.platform().serverVersion()[1] >= 19;
        this.biomeProvider = new BukkitPlotBiomeProvider();
    }

    public BukkitPlotGenerator(final String world, final ChunkGenerator cg, final @NonNull PlotAreaManager plotAreaManager) {
        if (cg instanceof BukkitPlotGenerator) {
            throw new IllegalArgumentException("ChunkGenerator: " + cg
                    .getClass()
                    .getName() + " is already a BukkitPlotGenerator!");
        }
        this.plotAreaManager = plotAreaManager;
        this.levelName = world;
        this.full = false;
        this.platformGenerator = cg;
        this.plotGenerator = new DelegatePlotGenerator(cg, world);
        this.useNewGenerationMethods = PlotSquared.platform().serverVersion()[1] >= 19;
        this.biomeProvider = null;
    }

    @Override
    public void augment(PlotArea area) {
        BukkitAugmentedGenerator.get(BukkitUtil.getWorld(area.getWorldName()));
    }

    @Override
    public boolean isFull() {
        return this.full;
    }

    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return this.plotGenerator;
    }

    @Override
    public ChunkGenerator getPlatformGenerator() {
        return this.platformGenerator;
    }

    @Override
    public @NonNull List<BlockPopulator> getDefaultPopulators(@NonNull World world) {
        try {
            checkLoaded(world);
        } catch (Exception e) {
            LOGGER.error("Error attempting to load world into PlotSquared.", e);
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

    // Extracted to synchronized method for thread-safety, preventing multiple internal world load calls
    private synchronized void checkLoaded(@NonNull World world) {
        // Do not attempt to load configurations until WorldEdit has a platform ready.
        if (!PlotSquared.get().isWeInitialised()) {
            return;
        }
        if (!this.loaded) {
            String name = world.getName();
            PlotSquared.get().loadWorld(name, this);
            final Set<PlotArea> areas = this.plotAreaManager.getPlotAreasSet(name);
            if (!areas.isEmpty()) {
                PlotArea area = areas.iterator().next();
                if (!area.isMobSpawning()) {
                    if (!area.isSpawnEggs()) {
                        world.setSpawnFlags(false, false);
                    }
                    setSpawnLimits(world, 0);
                } else {
                    world.setSpawnFlags(true, true);
                    setSpawnLimits(world, -1);
                }
            }
            this.loaded = true;
        }
    }

    @SuppressWarnings("deprecation") // Kept for compatibility with <=1.17.1
    private void setSpawnLimits(@NonNull World world, int limit) {
        world.setAmbientSpawnLimit(limit);
        world.setAnimalSpawnLimit(limit);
        world.setMonsterSpawnLimit(limit);
        world.setWaterAnimalSpawnLimit(limit);
    }

    @Override
    public void generateNoise(
            @NotNull final WorldInfo worldInfo,
            @NotNull final Random random,
            final int chunkX,
            final int chunkZ,
            @NotNull final ChunkData chunkData
    ) {
        if (this.platformGenerator != this) {
            this.platformGenerator.generateNoise(worldInfo, random, chunkX, chunkZ, chunkData);
            return;
        }
        int minY = chunkData.getMinHeight();
        int maxY = chunkData.getMaxHeight();
        GenChunk result = new GenChunk(minY, maxY);
        // Set the chunk location
        result.setChunk(new ChunkWrapper(worldInfo.getName(), chunkX, chunkZ));
        // Set the result data
        result.setChunkData(chunkData);
        result.result = null;

        // Catch any exceptions (as exceptions usually thrown)
        try {
            generate(BlockVector2.at(chunkX, chunkZ), worldInfo.getName(), result, false);
        } catch (Throwable e) {
            LOGGER.error("Error attempting to generate chunk.", e);
        }
    }

    @Override
    public void generateSurface(
            @NotNull final WorldInfo worldInfo,
            @NotNull final Random random,
            final int chunkX,
            final int chunkZ,
            @NotNull final ChunkData chunkData
    ) {
        if (platformGenerator != this) {
            platformGenerator.generateSurface(worldInfo, random, chunkX, chunkZ, chunkData);
        }
    }

    @Override
    public void generateBedrock(
            @NotNull final WorldInfo worldInfo,
            @NotNull final Random random,
            final int chunkX,
            final int chunkZ,
            @NotNull final ChunkData chunkData
    ) {
        if (platformGenerator != this) {
            platformGenerator.generateBedrock(worldInfo, random, chunkX, chunkZ, chunkData);
        }
    }

    @Override
    public void generateCaves(
            @NotNull final WorldInfo worldInfo,
            @NotNull final Random random,
            final int chunkX,
            final int chunkZ,
            @NotNull final ChunkData chunkData
    ) {
        if (platformGenerator != this) {
            platformGenerator.generateCaves(worldInfo, random, chunkX, chunkZ, chunkData);
        }
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull final WorldInfo worldInfo) {
        if (platformGenerator != this) {
            return platformGenerator.getDefaultBiomeProvider(worldInfo);
        }
        return biomeProvider;
    }

    @Override
    public int getBaseHeight(
            @NotNull final WorldInfo worldInfo,
            @NotNull final Random random,
            final int x,
            final int z,
            @NotNull final HeightMap heightMap
    ) {
        PlotArea area = getPlotArea(worldInfo.getName(), x, z);
        if (area instanceof ClassicPlotWorld cpw) {
            // Default to plot height being the heighest point before decoration (i.e. roads, walls etc.)
            return cpw.PLOT_HEIGHT;
        }
        return super.getBaseHeight(worldInfo, random, x, z, heightMap);
    }

    /**
     * The entire method is deprecated, but kept for compatibility with versions lower than or equal to 1.16.2.
     * The method will be removed in future versions, because WorldEdit and FastAsyncWorldEdit only support the latest point
     * release.
     */
    @SuppressWarnings("deprecation") // The entire method is deprecated, but kept for compatibility with <=1.16.2
    @Override
    @Deprecated(since = "7.0.0")
    public @NonNull ChunkData generateChunkData(
            @NonNull World world, @NonNull Random random, int x, int z, @NonNull BiomeGrid biome
    ) {
        if (useNewGenerationMethods) {
            if (this.platformGenerator != this) {
                return this.platformGenerator.generateChunkData(world, random, x, z, biome);
            } else {
                // Throw exception to be caught by the server that indicates the new generation API is being used.
                throw new UnsupportedOperationException("Using new generation methods. This method is unsupported.");
            }
        }

        int minY = BukkitWorld.getMinWorldHeight(world);
        int maxY = BukkitWorld.getMaxWorldHeight(world);
        GenChunk result = new GenChunk(minY, maxY);
        if (this.getPlotGenerator() instanceof SingleWorldGenerator) {
            if (result.getChunkData() != null) {
                for (int chunkX = 0; chunkX < 16; chunkX++) {
                    for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                        for (int y = minY; y < maxY; y++) {
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
                generate(BlockVector2.at(x, z), world.getName(), result, true);
            }
        } catch (Throwable e) {
            LOGGER.error("Error attempting to load world into PlotSquared.", e);
        }
        // Return the result data
        return result.getChunkData();
    }

    private void generate(BlockVector2 loc, String world, ZeroedDelegateScopedQueueCoordinator result, boolean biomes) {
        // Load if improperly loaded
        if (!this.loaded) {
            synchronized (this) {
                PlotSquared.get().loadWorld(world, this);
            }
        }
        // Process the chunk
        if (ChunkManager.preProcessChunk(loc, result)) {
            return;
        }
        PlotArea area = getPlotArea(world, loc.getX(), loc.getZ());
        try {
            this.plotGenerator.generateChunk(result, area, biomes);
        } catch (Throwable e) {
            // Recover from generator error
            LOGGER.error("Error attempting to generate chunk.", e);
        }
        ChunkManager.postProcessChunk(loc, result);
    }

    @Override
    public boolean canSpawn(final @NonNull World world, final int x, final int z) {
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

    @Override
    public String toString() {
        if (this.platformGenerator == this) {
            return this.plotGenerator.getName();
        }
        if (this.platformGenerator == null) {
            return "null";
        } else {
            return this.platformGenerator.getClass().getName();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        return toString().equals(obj.toString()) || toString().equals(obj.getClass().getName());
    }

    public String getLevelName() {
        return this.levelName;
    }

    private synchronized PlotArea getPlotArea(String name, int chunkX, int chunkZ) {
        // Load if improperly loaded
        if (!this.loaded) {
            PlotSquared.get().loadWorld(name, this);
            // Do not set loaded to true as we want to ensure spawn limits are set when "loading" is actually able to be
            // completed properly.
        }
        if (lastPlotArea != null && name.equals(this.levelName) && chunkX == lastChunkX && chunkZ == lastChunkZ) {
            return lastPlotArea;
        }
        BlockVector3 loc = BlockVector3.at(chunkX << 4, 0, chunkZ << 4);
        if (lastPlotArea != null && lastPlotArea.getRegion().contains(loc) && lastPlotArea.getRegion().contains(loc)) {
            return lastPlotArea;
        }
        PlotArea area = UncheckedWorldLocation.at(name, loc).getPlotArea();
        if (area == null) {
            throw new IllegalStateException(String.format(
                    "Cannot generate chunk that does not belong to a plot area. World: %s",
                    name
            ));
        }
        this.lastChunkX = chunkX;
        this.lastChunkZ = chunkZ;
        return this.lastPlotArea = area;
    }

    /**
     * Biome provider should never need to be accessed outside of this class.
     */
    private final class BukkitPlotBiomeProvider extends BiomeProvider {

        private static final List<Biome> BIOMES;

        static {
            Set<Biome> disabledBiomes = new HashSet<>(List.of(Biome.CUSTOM));
            if (PlotSquared.platform().serverVersion()[1] <= 19) {
                final Biome cherryGrove = Registry.BIOME.get(NamespacedKey.minecraft("cherry_grove"));
                if (cherryGrove != null) {
                    disabledBiomes.add(cherryGrove);
                }
            }
            BIOMES = Arrays.stream(Biome.values())
                    .filter(not(disabledBiomes::contains))
                    .toList();
        }

        @Override
        public @NotNull Biome getBiome(@NotNull final WorldInfo worldInfo, final int x, final int y, final int z) {
            PlotArea area = getPlotArea(worldInfo.getName(), x >> 4, z >> 4);
            return BukkitAdapter.adapt(plotGenerator.getBiome(area, x, y, z));
        }

        @Override
        public @NotNull List<Biome> getBiomes(@NotNull final WorldInfo worldInfo) {
            return BIOMES; // Allow all biomes
        }

    }

}
