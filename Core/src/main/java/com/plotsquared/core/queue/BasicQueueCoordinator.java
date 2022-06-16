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
package com.plotsquared.core.queue;

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.plotsquared.core.util.PatternUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Standard block setting queue that allows block setting across numerous chunks, without limits.
 */
public abstract class BasicQueueCoordinator extends QueueCoordinator {

    private final World world;
    private final ConcurrentHashMap<BlockVector2, LocalChunk> blockChunks = new ConcurrentHashMap<>();
    private final List<BlockVector2> readRegion = new ArrayList<>();
    private final List<ProgressSubscriber> progressSubscribers = new ArrayList<>();
    private LocalChunk lastWrappedChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private boolean settingBiomes = false;
    private boolean disableBiomes = false;
    private boolean settingTiles = false;
    private boolean regen = false;
    private int[] regenStart;
    private int[] regenEnd;
    private CuboidRegion regenRegion = null;
    private Consumer<BlockVector2> consumer = null;
    private boolean unloadAfter = true;
    private Runnable whenDone = null;
    private SideEffectSet sideEffectSet = null;
    @Nullable
    private LightingMode lightingMode = LightingMode.valueOf(Settings.QUEUE.LIGHTING_MODE);

    public BasicQueueCoordinator(@NonNull World world) {
        super(world);
        this.world = world;
    }

    @Override
    public abstract BlockState getBlock(int x, int y, int z);

    @Override
    public final @NonNull World getWorld() {
        return world;
    }

    @Override
    public final int size() {
        return blockChunks.size() + readRegion.size();
    }

    @Override
    public final void setModified(long modified) {
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull Pattern pattern) {
        return setBlock(x, y, z, PatternUtil.apply(pattern, x, y, z));
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BaseBlock id) {
        if ((y > world.getMaxY()) || (y < world.getMinY())) {
            return false;
        }
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setBlock(x & 15, y, z & 15, id);
        return true;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BlockState id) {
        // Trying to mix BlockState and BaseBlock leads to all kinds of issues.
        // Since BaseBlock has more features than BlockState, simply convert
        // all BlockStates to BaseBlocks
        return setBlock(x, y, z, id.toBaseBlock());
    }

    @SuppressWarnings("removal")
    @Override
    public boolean setBiome(int x, int z, @NonNull BiomeType biomeType) {
        if (disableBiomes) {
            return false;
        }
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        for (int y = world.getMinY(); y <= world.getMaxY(); y++) {
            chunk.setBiome(x & 15, y, z & 15, biomeType);
        }
        settingBiomes = true;
        return true;
    }

    @Override
    public final boolean setBiome(int x, int y, int z, @NonNull BiomeType biomeType) {
        if (disableBiomes) {
            return false;
        }
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setBiome(x & 15, y, z & 15, biomeType);
        settingBiomes = true;
        return true;
    }

    @Override
    public boolean isSettingBiomes() {
        return this.settingBiomes;
    }

    @Override
    public void setBiomesEnabled(boolean settingBiomes) {
        this.settingBiomes = settingBiomes;
        this.disableBiomes = true;
    }

    @Override
    public boolean setTile(int x, int y, int z, @NonNull CompoundTag tag) {
        LocalChunk chunk = getChunk(x >> 4, z >> 4);
        chunk.setTile(x, y, z, tag);
        settingTiles = true;
        return true;
    }

    @Override
    public boolean isSettingTiles() {
        return this.settingTiles;
    }

    @Override
    public boolean setEntity(@NonNull Entity entity) {
        if (entity.getState() == null || entity.getState().getType() == EntityTypes.PLAYER) {
            return false;
        }
        Location location = entity.getLocation();
        LocalChunk chunk = getChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        chunk.setEntity(location, entity.getState());
        return true;
    }

    @Override
    public @NonNull List<BlockVector2> getReadChunks() {
        return this.readRegion;
    }

    @Override
    public void addReadChunk(@NonNull BlockVector2 chunk) {
        this.readRegion.add(chunk);
    }

    @Override
    public void addReadChunks(@NonNull Set<BlockVector2> readRegion) {
        this.readRegion.addAll(readRegion);
    }

    @Override
    public CuboidRegion getRegenRegion() {
        return this.regenRegion != null ? this.regenRegion.clone() : null;
    }

    @Override
    public void setRegenRegion(@NonNull CuboidRegion regenRegion) {
        this.regenRegion = regenRegion;
    }

    @Override
    public void regenChunk(int x, int z) {
        regen = true;
        // There will never only be one nullified coordinate pair
        if (regenStart == null) {
            regenStart = new int[]{x, z};
            regenEnd = new int[]{x, z};
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

    @Override
    public boolean isUnloadAfter() {
        return this.unloadAfter;
    }

    @Override
    public void setUnloadAfter(boolean unloadAfter) {
        this.unloadAfter = unloadAfter;
    }

    /**
     * Gets the int[x,z] chunk coordinates where regeneration should start from
     *
     * @return int[x, z] of regen start
     */
    public int[] getRegenStart() {
        return regenStart;
    }

    /**
     * Gets the int[x,z] chunk coordinates where regeneration should finish
     *
     * @return int[x, z] of regen end
     */
    public int[] getRegenEnd() {
        return regenEnd;
    }

    /**
     * Whether the queue has a start/end to chunk regeneration
     *
     * @return if is regenerating queue with int[x,z] start and end
     */
    public boolean isRegen() {
        return regen;
    }

    /**
     * Gets the map of ChunkCoordinates in {@link BlockVector2} form against the {@link LocalChunk} of cached chunks to be written
     *
     * @return ConcurrentHashMap of chunks to be accessed
     */
    public @NonNull ConcurrentHashMap<BlockVector2, LocalChunk> getBlockChunks() {
        return this.blockChunks;
    }

    /**
     * Forces an {@link LocalChunk} into the list of chunks to be written. Overwrites existing chunks in the map
     *
     * @param chunk add a LocalChunk to be written to by the queue
     */
    public final void setChunk(@NonNull LocalChunk chunk) {
        this.blockChunks.put(BlockVector2.at(chunk.getX(), chunk.getZ()), chunk);
    }

    @Override
    public @Nullable
    final Consumer<BlockVector2> getChunkConsumer() {
        return this.consumer;
    }

    @Override
    public final void setChunkConsumer(@NonNull Consumer<BlockVector2> consumer) {
        this.consumer = consumer;
    }

    /**
     * Get the list of progress subscribers currently added to the queue to be added to the Chunk Coordinator
     */
    public final List<ProgressSubscriber> getProgressSubscribers() {
        return this.progressSubscribers;
    }

    @Override
    public final void addProgressSubscriber(@NonNull ProgressSubscriber progressSubscriber) {
        this.progressSubscribers.add(progressSubscriber);
    }

    @Override
    public @NonNull
    final LightingMode getLightingMode() {
        if (lightingMode == null) {
            return LightingMode.valueOf(Settings.QUEUE.LIGHTING_MODE);
        }
        return this.lightingMode;
    }

    @Override
    public final void setLightingMode(@Nullable LightingMode mode) {
        this.lightingMode = mode;
    }

    @Override
    public Runnable getCompleteTask() {
        return this.whenDone;
    }

    @Override
    public void setCompleteTask(Runnable whenDone) {
        this.whenDone = whenDone;
    }

    @Override
    public SideEffectSet getSideEffectSet() {
        return sideEffectSet;
    }

    @Override
    public void setSideEffectSet(SideEffectSet sideEffectSet) {
        this.sideEffectSet = sideEffectSet;
    }

    // Don't ask about the @NonNull placement. That's how it needs to be else it errors.
    @Override
    public void setBiomeCuboid(
            final com.plotsquared.core.location.@NonNull Location pos1,
            final com.plotsquared.core.location.@NonNull Location pos2,
            @NonNull final BiomeType biome
    ) {
        if (disableBiomes) {
            return;
        }
        super.setBiomeCuboid(pos1, pos2, biome);
    }

    /**
     * Get the {@link LocalChunk} from the queue at the given chunk coordinates. Returns a new instance if one doesn't exist
     */
    @NonNull
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
