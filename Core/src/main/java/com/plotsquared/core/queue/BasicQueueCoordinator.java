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
package com.plotsquared.core.queue;

import com.plotsquared.core.util.PatternUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BasicQueueCoordinator extends QueueCoordinator {

    private final String world;
    private final ConcurrentHashMap<Long, LocalChunk> blockChunks = new ConcurrentHashMap<>();
    private long modified;
    private LocalChunk lastWrappedChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private boolean setbiome = false;

    private GlobalBlockQueue globalBlockQueue;

    public BasicQueueCoordinator(String world) {
        this.world = world;
        this.modified = System.currentTimeMillis();
    }

    public LocalChunk getLocalChunk(int x, int z) {
        return new LocalChunk(this, x, z) {
            // Allow implementation-specific custom stuff here
        };
    }

    @Override public abstract BlockState getBlock(int x, int y, int z);

    @Override public final String getWorld() {
        return world;
    }

    @Override public final int size() {
        return blockChunks.size();
    }

    @Override public final void setModified(long modified) {
        this.modified = modified;
    }

    @Override public boolean setBlock(int x, int y, int z, @Nonnull Pattern pattern) {
        return setBlock(x, y, z, PatternUtil.apply(pattern, x, y, z));
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setBlock(x & 15, y, z & 15, id);
        return true;
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        // Trying to mix BlockState and BaseBlock leads to all kinds of issues.
        // Since BaseBlock has more features than BlockState, simply convert
        // all BlockStates to BaseBlocks
        return setBlock(x, y, z, id.toBaseBlock());
    }

    @Override public final boolean setBiome(int x, int z, BiomeType biomeType) {
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setBiome(x & 15, z & 15, biomeType);
        setbiome = true;
        return true;
    }

    @Override public final boolean setTile(int x, int y, int z, CompoundTag tag) {
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setTile(x, y, z, tag);
        return true;
    }

    @Override public final boolean settingBiome() {
        return setbiome;
    }

    public final void setChunk(LocalChunk chunk) {
        this.blockChunks.put(chunk.longHash(), chunk);
    }

    private LocalChunk getChunk(final int chunkX, final int ChunkZ) {
        if (chunkX != lastX || ChunkZ != lastZ) {
            lastX = chunkX;
            lastZ = ChunkZ;
            long pair = (long) (chunkX) << 32 | (ChunkZ) & 0xFFFFFFFFL;
            lastWrappedChunk = this.blockChunks.get(pair);
            if (lastWrappedChunk == null) {
                lastWrappedChunk = this.getLocalChunk(chunkX, ChunkZ);
                LocalChunk previous = this.blockChunks.put(pair, lastWrappedChunk);
                if (previous == null) {
                    return lastWrappedChunk;
                }
                lastWrappedChunk = previous;
            }
        }
        return lastWrappedChunk;
    }


}
