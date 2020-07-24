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
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityTypes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class BasicQueueCoordinator extends QueueCoordinator {

    private final World world;
    private final ConcurrentHashMap<BlockVector2, LocalChunk> blockChunks =
        new ConcurrentHashMap<>();
    private final List<BlockVector2> readRegion = new ArrayList<>();
    private long modified;
    private LocalChunk lastWrappedChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private boolean settingBiomes = false;
    private boolean settingTiles = false;
    private boolean regen = false;
    private int[] regenStart;
    private int[] regenEnd;
    private Consumer<BlockVector2> consumer = null;
    private boolean unloadAfter = true;
    private GlobalBlockQueue globalBlockQueue;

    public BasicQueueCoordinator(World world) {
        this.world = world;
        this.modified = System.currentTimeMillis();
    }

    @Override public abstract BlockState getBlock(int x, int y, int z);

    @Override public final World getWorld() {
        return world;
    }

    @Override public final int size() {
        return blockChunks.size() + readRegion.size();
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

    @Override public boolean setBiome(int x, int z, BiomeType biomeType) {
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        for (int y = 0; y < 256; y++) {
            chunk.setBiome(x & 15, y, z & 15, biomeType);
        }
        settingBiomes = true;
        return true;
    }

    @Override public final boolean setBiome(int x, int y, int z, BiomeType biomeType) {
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setBiome(x & 15, y, z & 15, biomeType);
        settingBiomes = true;
        return true;
    }

    @Override public boolean isSettingBiomes() {
        return this.settingBiomes;
    }

    @Override public boolean setTile(int x, int y, int z, CompoundTag tag) {
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setTile(x, y, z, tag);
        settingTiles = true;
        return true;
    }

    @Override public boolean isSettingTiles() {
        return this.settingTiles;
    }

    @Override public boolean setEntity(Entity entity) {
        if (entity.getState() == null || entity.getState().getType() == EntityTypes.PLAYER) {
            return false;
        }
        Location location = entity.getLocation();
        LocalChunk chunk = getChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        chunk.setEntity(location, entity.getState());
        return true;
    }

    @Override public List<BlockVector2> getReadChunks() {
        return this.readRegion;
    }

    @Override public void addReadChunk(BlockVector2 chunk) {
        this.readRegion.add(chunk);
    }

    @Override public void addReadChunks(Set<BlockVector2> readRegion) {
        this.readRegion.addAll(readRegion);
    }

    @Override public void regenChunk(int x, int z) {
        regen = true;
        // There will never only be one nullified coordinate pair
        if (regenStart == null) {
            regenStart = new int[] {x, z};
            regenEnd = new int[] {x, z};
            return;
        }
        if (x < regenStart[0]) {
            regenStart[0] = x;
        }
        if (z < regenStart[1]) {
            regenStart[1] = z;
        }
        if (x > regenEnd[0]) {
            regenEnd[0] = x;
        }
        if (z > regenEnd[1]) {
            regenEnd[1] = z;
        }
    }

    @Override public boolean isUnloadAfter() {
        return this.unloadAfter;
    }

    @Override public void setUnloadAfter(boolean unloadAfter) {
        this.unloadAfter = unloadAfter;
    }

    public int[] getRegenStart() {
        return regenStart;
    }

    public int[] getRegenEnd() {
        return regenEnd;
    }

    public boolean isRegen() {
        return regen;
    }

    public ConcurrentHashMap<BlockVector2, LocalChunk> getBlockChunks() {
        return this.blockChunks;
    }

    public final void setChunk(LocalChunk chunk) {
        this.blockChunks.put(BlockVector2.at(chunk.getX(), chunk.getZ()), chunk);
    }

    public final Consumer<BlockVector2> getChunkConsumer() {
        return this.consumer;
    }

    public final void setChunkConsumer(Consumer<BlockVector2> consumer) {
        this.consumer = consumer;
    }

    private LocalChunk getChunk(final int chunkX, final int chunkZ) {
        if (chunkX != lastX || chunkZ != lastZ) {
            lastX = chunkX;
            lastZ = chunkZ;
            BlockVector2 pair = BlockVector2.at(chunkX, chunkZ);
            lastWrappedChunk = this.blockChunks.get(pair);
            if (lastWrappedChunk == null) {
                lastWrappedChunk = new LocalChunk(this, chunkX, chunkZ);
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
