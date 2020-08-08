/*
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
package com.plotsquared.bukkit.queue;

import com.google.common.base.Preconditions;
import com.plotsquared.bukkit.util.BukkitBlockUtil;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.plotsquared.core.util.ChunkUtil;
import com.plotsquared.core.util.PatternUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class GenChunk extends ScopedQueueCoordinator {

    public final Biome[] biomes;
    public BlockState[][] result;
    public BiomeGrid biomeGrid;
    public Chunk chunk;
    public String world;
    public int chunkX;
    public int chunkZ;
    private ChunkData chunkData = null;

    public GenChunk() {
        super(null, Location.at("", 0, 0, 0), Location.at("", 15, 255, 15));
        this.biomes = Biome.values();
    }

    @Nullable public ChunkData getChunkData() {
        return this.chunkData;
    }

    /**
     * Set the internal Bukkit chunk data
     */
    public void setChunkData(@Nonnull ChunkData chunkData) {
        this.chunkData = chunkData;
    }

    @Nonnull public Chunk getChunk() {
        if (chunk == null) {
            World worldObj = BukkitUtil.getWorld(world);
            if (worldObj != null) {
                this.chunk = worldObj.getChunkAt(chunkX, chunkZ);
            }
        }
        return chunk;
    }

    /**
     * Set the chunk being represented
     */
    public void setChunk(@Nonnull Chunk chunk) {
        this.chunk = chunk;
    }


    /**
     * Set the world and XZ of the chunk being represented via {@link ChunkWrapper}
     */
    public void setChunk(@Nonnull ChunkWrapper wrap) {
        chunk = null;
        world = wrap.world;
        chunkX = wrap.x;
        chunkZ = wrap.z;
    }

    @Override public void fillBiome(@Nonnull BiomeType biomeType) {
        if (biomeGrid == null) {
            return;
        }
        Biome biome = BukkitAdapter.adapt(biomeType);
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    this.biomeGrid.setBiome(x, y, z, biome);
                }
            }
        }
    }

    @Override public void setCuboid(@Nonnull Location pos1, @Nonnull Location pos2, @Nonnull BlockState block) {
        if (result != null && pos1.getX() == 0 && pos1.getZ() == 0 && pos2.getX() == 15 && pos2.getZ() == 15) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                int layer = y >> 4;
                BlockState[] data = result[layer];
                if (data == null) {
                    result[layer] = data = new BlockState[4096];
                }
                int start = y << 8;
                int end = start + 256;
                Arrays.fill(data, start, end, block);
            }
        }
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        chunkData.setRegion(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1, BukkitAdapter.adapt(block));
    }

    @Override public boolean setBiome(int x, int z, @Nonnull BiomeType biomeType) {
        return setBiome(x, z, BukkitAdapter.adapt(biomeType));
    }

    /**
     * Set the in the whole column of XZ
     */
    public boolean setBiome(int x, int z, @Nonnull Biome biome) {
        if (this.biomeGrid != null) {
            for (int y = 0; y < 256; y++) {
                this.setBiome(x, y, z, biome);
            }
            return true;
        }
        return false;
    }

    public boolean setBiome(int x, int y, int z, @Nonnull Biome biome) {
        if (this.biomeGrid != null) {
            this.biomeGrid.setBiome(x, y, z, biome);
            return true;
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, @Nonnull Pattern pattern) {
        return setBlock(x, y, z, PatternUtil.apply(Preconditions.checkNotNull(pattern, "Pattern may not be null"), x, y, z));
    }

    @Override public boolean setBlock(int x, int y, int z, @Nonnull BlockState id) {
        if (this.result == null) {
            this.chunkData.setBlock(x, y, z, BukkitAdapter.adapt(id));
            return true;
        }
        this.chunkData.setBlock(x, y, z, BukkitAdapter.adapt(id));
        this.storeCache(x, y, z, id);
        return true;
    }

    private void storeCache(final int x, final int y, final int z, @Nonnull final BlockState id) {
        int i = y >> 4;
        BlockState[] v = this.result[i];
        if (v == null) {
            this.result[i] = v = new BlockState[4096];
        }
        int j = ChunkUtil.getJ(x, y, z);
        v[j] = id;
    }

    @Override public boolean setBlock(int x, int y, int z, @Nonnull BaseBlock id) {
        if (this.result == null) {
            this.chunkData.setBlock(x, y, z, BukkitAdapter.adapt(id));
            return true;
        }
        this.chunkData.setBlock(x, y, z, BukkitAdapter.adapt(id));
        this.storeCache(x, y, z, id.toImmutableState());
        return true;
    }

    @Override @Nullable public BlockState getBlock(int x, int y, int z) {
        int i = y >> 4;
        if (result == null) {
            return BukkitBlockUtil.get(chunkData.getType(x, y, z));
        }
        BlockState[] array = result[i];
        if (array == null) {
            return BlockTypes.AIR.getDefaultState();
        }
        int j = ChunkUtil.getJ(x, y, z);
        return array[j];
    }

    public int getX() {
        return chunk == null ? chunkX : chunk.getX();
    }

    public int getZ() {
        return chunk == null ? chunkZ : chunk.getZ();
    }

    @Override @Nonnull public com.sk89q.worldedit.world.World getWorld() {
        return chunk == null ? BukkitAdapter.adapt(Bukkit.getWorld(world)) : BukkitAdapter.adapt(chunk.getWorld());
    }

    @Override @Nonnull public Location getMax() {
        return Location.at(getWorld().getName(), 15 + (getX() << 4), 255, 15 + (getZ() << 4));
    }

    @Override @Nonnull public Location getMin() {
        return Location.at(getWorld().getName(), getX() << 4, 0, getZ() << 4);
    }

    @Nonnull public GenChunk clone() {
        GenChunk toReturn = new GenChunk();
        if (this.result != null) {
            for (int i = 0; i < this.result.length; i++) {
                BlockState[] matrix = this.result[i];
                if (matrix != null) {
                    toReturn.result[i] = new BlockState[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result[i], 0, matrix.length);
                }
            }
        }
        toReturn.chunkData = this.chunkData;
        return toReturn;
    }
}
