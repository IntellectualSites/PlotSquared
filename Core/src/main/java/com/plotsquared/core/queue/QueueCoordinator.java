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

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.plotsquared.core.util.PatternUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class QueueCoordinator {

    private final AtomicBoolean enqueued = new AtomicBoolean();
    private boolean forceSync = false;
    private boolean shouldGen = true;
    @Nullable
    private Object chunkObject;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    @Inject
    private GlobalBlockQueue blockQueue;

    /**
     * Default constructor requires world to indicate any extents given to {@link QueueCoordinator} also need this constructor.
     *
     * @param world world as all queues should have this constructor
     */
    public QueueCoordinator(@Nullable World world) {
        PlotSquared.platform().injector().injectMembers(this);
    }

    /**
     * Get a {@link ZeroedDelegateScopedQueueCoordinator} limited to the chunk at the specific chunk Coordinates
     *
     * @param x chunk x coordinate
     * @param z chunk z coordinate
     * @return a new {@link ZeroedDelegateScopedQueueCoordinator}
     * @since 7.0.0
     */
    public ZeroedDelegateScopedQueueCoordinator getForChunk(int x, int z, int minY, int maxY) {
        int bx = x << 4;
        int bz = z << 4;
        return new ZeroedDelegateScopedQueueCoordinator(this, Location.at(getWorld().getName(), bx, minY, bz),
                Location.at(getWorld().getName(), bx + 15, maxY, bz + 15)
        );
    }

    /**
     * Get the size of the queue in chunks
     *
     * @return size
     */
    public abstract int size();

    /**
     * Set when the queue was last modified
     *
     * @param modified long of system millis
     */
    public abstract void setModified(long modified);

    /**
     * Returns true if the queue should be forced to be synchronous when enqueued. This is not necessarily synchronous to the
     * server, and simply effectively makes {@link QueueCoordinator#enqueue()} a blocking operation.
     *
     * @return is force sync
     */
    public boolean isForceSync() {
        return forceSync;
    }

    /**
     * Set whether the queue should be forced to be synchronous. This is not necessarily synchronous to the server, and simply
     * effectively makes {@link QueueCoordinator#enqueue()} a blocking operation.
     *
     * @param forceSync force sync or not
     */
    public void setForceSync(boolean forceSync) {
        this.forceSync = forceSync;
    }


    /**
     * Get whether chunks should be generated as part of this operation. Default is true. Disabling this may not be supported
     * depending on server implementation. (i.e. setting to false may not actually disable generation as part of this operation
     * - this is just a catch-all in case of future differing server implementations; the option will work on Spigot/Paper).
     *
     * @since 7.5.0
     */
    public boolean isShouldGen() {
        return shouldGen;
    }

    /**
     * Set whether chunks should be generated as part of this operation. Default is true. Disabling this may not be supported
     * depending on server implementation. (i.e. setting to false may not actually disable generation as part of this operation
     * - this is just a catch-all in case of future differing server implementations; the option will work on Spigot/Paper).
     *
     * @param shouldGen should generate new chunks or not
     * @since 7.5.0
     */
    public void setShouldGen(boolean shouldGen) {
        this.shouldGen = shouldGen;
    }

    /**
     * Get the Chunk Object set to the queue
     *
     * @return chunk object. Usually the implementation-specific chunk (e.g. bukkit Chunk)
     */
    public @Nullable Object getChunkObject() {
        return chunkObject;
    }

    /**
     * Set a chunk object (e.g. the Bukkit Chunk object) to the queue. This will be used as fallback in case of WNA failure.
     * Should ONLY be used in specific cases (i.e. generation, where a chunk is being populated)
     *
     * @param chunkObject chunk object. Usually the implementation-specific chunk (e.g. bukkit Chunk)
     */
    public void setChunkObject(@NonNull Object chunkObject) {
        this.chunkObject = chunkObject;
    }

    /**
     * Sets the block at the coordinates provided to the given id.
     *
     * @param x  the x coordinate from from 0 to 15 inclusive
     * @param y  the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z  the z coordinate from 0 to 15 inclusive
     * @param id the BlockState to set the block to
     * @return success or not
     */
    public abstract boolean setBlock(final int x, final int y, final int z, final @NonNull BlockState id);

    /**
     * Sets the block at the coordinates provided to the given id.
     *
     * @param x  the x coordinate from from 0 to 15 inclusive
     * @param y  the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z  the z coordinate from 0 to 15 inclusive
     * @param id the BaseBlock to set the block to
     * @return success or not
     */
    public abstract boolean setBlock(final int x, final int y, final int z, final @NonNull BaseBlock id);

    /**
     * Sets the block at the coordinates provided to the given id.
     *
     * @param x       the x coordinate from from 0 to 15 inclusive
     * @param y       the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z       the z coordinate from 0 to 15 inclusive
     * @param pattern the pattern to set the block to
     * @return success or not
     */
    public boolean setBlock(final int x, final int y, final int z, final @NonNull Pattern pattern) {
        return setBlock(x, y, z, PatternUtil.apply(pattern, x, y, z));
    }

    /**
     * Sets a tile entity at the coordinates provided to the given CompoundTag
     *
     * @param x   the x coordinate from from 0 to 15 inclusive
     * @param y   the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z   the z coordinate from 0 to 15 inclusive
     * @param tag the CompoundTag to set the tile to
     * @return success or not
     */
    public abstract boolean setTile(int x, int y, int z, @NonNull CompoundTag tag);

    /**
     * Whether the queue has any tiles being set
     *
     * @return if setting tiles
     */
    public abstract boolean isSettingTiles();

    /**
     * Get a block at the given coordinates.
     *
     * @param x block x
     * @param y block y
     * @param z block z
     * @return WorldEdit BlockState
     */
    public @Nullable
    abstract BlockState getBlock(int x, int y, int z);

    /**
     * Set a biome in XZ. This will likely set to the whole column
     *
     * @param x     x coordinate
     * @param z     z coordinate
     * @param biome biome
     * @return success or not
     * @deprecated Biomes now take XYZ, see {@link #setBiome(int, int, int, BiomeType)}
     *         <br>
     *         Scheduled for removal once we drop the support for versions not supporting 3D biomes, 1.18 and earlier.
     */
    @Deprecated(forRemoval = true, since = "6.0.0")
    public abstract boolean setBiome(int x, int z, @NonNull BiomeType biome);

    /**
     * Set a biome in XYZ
     *
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @param biome biome
     * @return success or not
     */
    public abstract boolean setBiome(int x, int y, int z, @NonNull BiomeType biome);

    /**
     * Whether the queue has any biomes to be set
     *
     * @return if setting biomes
     */
    public abstract boolean isSettingBiomes();

    /**
     * If the queue should accept biome placement
     *
     * @param enabled If biomes should be enabled
     * @since 6.8.0
     */
    public abstract void setBiomesEnabled(boolean enabled);

    /**
     * Add entities to be created
     *
     * @param entities list of entities to add to queue
     */
    public void addEntities(@NonNull List<? extends Entity> entities) {
        for (Entity e : entities) {
            this.setEntity(e);
        }
    }

    /**
     * Add an entity to be created
     *
     * @param entity entity to add to queue
     * @return success or not
     */
    public abstract boolean setEntity(@NonNull Entity entity);

    /**
     * Get the list of chunks that are added manually. This usually indicated the queue is "read only".
     *
     * @return list of BlockVector2 of chunks that are to be "read"
     */
    public @NonNull
    abstract List<BlockVector2> getReadChunks();

    /**
     * Add a set of {@link BlockVector2} Chunk coordinates to the Read Chunks list
     *
     * @param readChunks set of BlockVector2 to add to "read" chunks
     */
    public abstract void addReadChunks(@NonNull Set<BlockVector2> readChunks);

    /**
     * Add a {@link BlockVector2} Chunk coordinate to the Read Chunks list
     *
     * @param chunk BlockVector2 to add to "read" chunks
     */
    public abstract void addReadChunk(@NonNull BlockVector2 chunk);

    /**
     * Whether chunks should be unloaded after being accessed
     *
     * @return if is unloading chunks after accessing them
     */
    public abstract boolean isUnloadAfter();

    /**
     * Set whether chunks should be unloaded after being accessed
     *
     * @param unloadAfter if to unload chunks after being accessed
     */
    public abstract void setUnloadAfter(boolean unloadAfter);

    /**
     * Get the {@link CuboidRegion} designated for direct regeneration
     *
     * @return CuboidRegion to regenerate
     */
    public @Nullable
    abstract CuboidRegion getRegenRegion();

    /**
     * Set the {@link CuboidRegion} designated for direct regeneration
     *
     * @param regenRegion CuboidRegion to regenerate
     */
    public abstract void setRegenRegion(@NonNull CuboidRegion regenRegion);

    /**
     * Set a specific chunk at the chunk coordinates XZ to be regenerated.
     *
     * @param x chunk x
     * @param z chunk z
     */
    public abstract void regenChunk(int x, int z);

    /**
     * Get the world the queue is writing to
     *
     * @return world of the queue
     */
    public @Nullable
    abstract World getWorld();

    /**
     * Set the queue as having been modified now
     */
    public final void setModified() {
        setModified(System.currentTimeMillis());
    }

    /**
     * Enqueue the queue to start it
     *
     * @return success or not
     * @since 6.0.10
     */
    public boolean enqueue() {
        boolean success = false;
        if (enqueued.compareAndSet(false, true)) {
            success = true;
            start();
        }
        return success;
    }

    /**
     * Start the queue
     */
    public abstract void start();

    /**
     * Cancel the queue
     */
    public abstract void cancel();

    /**
     * Get the task to be run when all chunks have been accessed
     *
     * @return task to be run when queue is complete
     */
    public abstract Runnable getCompleteTask();

    /**
     * Set the task to be run when all chunks have been accessed
     *
     * @param whenDone task to be run when queue is complete
     */
    public abstract void setCompleteTask(@Nullable Runnable whenDone);

    /**
     * Return the chunk consumer set to the queue or null if one is not set
     *
     * @return Consumer to be executed on each chunk in queue
     */
    public @Nullable
    abstract Consumer<BlockVector2> getChunkConsumer();

    /**
     * Set the Consumer that will be executed on each chunk in queue
     *
     * @param consumer Consumer to be executed on each chunk in queue
     */
    public abstract void setChunkConsumer(@NonNull Consumer<BlockVector2> consumer);

    /**
     * Add a {@link ProgressSubscriber} to the Queue to subscribe to the relevant Chunk Processor
     */
    public abstract void addProgressSubscriber(@NonNull ProgressSubscriber progressSubscriber);

    /**
     * Get the {@link LightingMode} to be used when setting blocks
     */
    public @NonNull
    abstract LightingMode getLightingMode();

    /**
     * Set the {@link LightingMode} to be used when setting blocks
     *
     * @param mode lighting mode. Null to use default.
     */
    public abstract void setLightingMode(@Nullable LightingMode mode);

    /**
     * Get the overriding {@link SideEffectSet} to be used by the queue if it exists, else null
     *
     * @return Overriding {@link SideEffectSet} or null
     */
    public abstract @Nullable SideEffectSet getSideEffectSet();

    /**
     * Set the overriding {@link SideEffectSet} to be used by the queue. Null to use default side effects.
     *
     * @param sideEffectSet side effects to override with, or null to use default
     */
    public abstract void setSideEffectSet(@Nullable SideEffectSet sideEffectSet);

    /**
     * Fill a cuboid between two positions with a BlockState
     *
     * @param pos1  1st cuboid position
     * @param pos2  2nd cuboid position
     * @param block block to fill
     */
    public void setCuboid(@NonNull Location pos1, @NonNull Location pos2, @NonNull BlockState block) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.max(pos1.getY(), pos2.getY());
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBlock(x, y, z, block);
                }
            }
        }
    }

    /**
     * Fill a cuboid between two positions with a Pattern
     *
     * @param pos1   1st cuboid position
     * @param pos2   2nd cuboid position
     * @param blocks pattern to fill
     */
    public void setCuboid(@NonNull Location pos1, @NonNull Location pos2, @NonNull Pattern blocks) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.max(pos1.getY(), pos2.getY());
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBlock(x, y, z, blocks);
                }
            }
        }
    }

    /**
     * Fill a cuboid between two positions with a BiomeType
     *
     * @param pos1  1st cuboid position
     * @param pos2  2nd cuboid position
     * @param biome biome to fill
     */
    public void setBiomeCuboid(@NonNull Location pos1, @NonNull Location pos2, @NonNull BiomeType biome) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.max(pos1.getY(), pos2.getY());
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBiome(x, y, z, biome);
                }
            }
        }
    }

    /**
     * Get the min Y limit associated with the queue
     */
    protected int getMinY() {
        return getWorld() != null ? getWorld().getMinY() : PlotSquared.platform().versionMinHeight();
    }

    /**
     * Get the max Y limit associated with the queue
     */
    protected int getMaxY() {
        return getWorld() != null ? getWorld().getMinY() : PlotSquared.platform().versionMaxHeight();
    }

    /**
     * Get the min chunk layer associated with the queue. Usually 0 or -4;
     */
    protected int getMinLayer() {
        return (getWorld() != null ? getWorld().getMinY() : PlotSquared.platform().versionMinHeight()) >> 4;
    }

    /**
     * Get the max chunk layer associated with the queue. Usually 15 or 19
     */
    protected int getMaxLayer() {
        return (getWorld() != null ? getWorld().getMaxY() : PlotSquared.platform().versionMaxHeight()) >> 4;
    }

}
