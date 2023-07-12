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

import com.intellectualsites.annotations.NotPublic;
import com.plotsquared.core.location.Location;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link QueueCoordinator} that caches all blocks set to it in a given array of form BlockState[][][]. An offset can be
 * applied to blocks set to it, and the scope limited. This should have blocks set to it one chunk at a time, based on the
 * result of {@link BlockArrayCacheScopedQueueCoordinator#getMin()} and {@link BlockArrayCacheScopedQueueCoordinator#getMax()}.
 * The min and max points of this queue are offset according to the minimum point given in the constructor, and the offsets set
 * in {@link BlockArrayCacheScopedQueueCoordinator#setOffsetX(int)} and
 * {@link BlockArrayCacheScopedQueueCoordinator#setOffsetZ(int)}
 * <p>
 * Internal use only. Subject to change at any time and created for specific use cases.
 */
@NotPublic
public class BlockArrayCacheScopedQueueCoordinator extends ZeroedDelegateScopedQueueCoordinator {

    private final BlockState[][][] blockStates;
    private final int height;
    private final int width;
    private final int length;
    private final int minY;
    private final int maxY;
    private final int scopeMinX;
    private final int scopeMinZ;
    private final int scopeMaxX;
    private final int scopeMaxZ;
    private int offsetX = 0;
    private int offsetZ = 0;

    /**
     * Construct a new instance
     *
     * @param min Inclusive location of the minimum point to limit the scope to.
     * @param max Inclusive location of the maximum point to limit the scope to.
     * @since 6.8.0
     */
    public BlockArrayCacheScopedQueueCoordinator(Location min, Location max) {
        super(null, min, max);
        this.width = max.getX() - min.getX() + 1;
        this.length = max.getZ() - min.getZ() + 1;
        this.minY = min.getY();
        this.maxY = max.getY();
        this.height = maxY - minY + 1;

        this.scopeMinX = min.getX() & 15;
        this.scopeMinZ = min.getZ() & 15;
        this.scopeMaxX = scopeMinX + width;
        this.scopeMaxZ = scopeMinZ + length;
        this.blockStates = new BlockState[height][width][length];
    }

    public BlockState[][][] getBlockStates() {
        return blockStates;
    }

    @Override
    public boolean setBlock(int x, final int y, int z, final @NonNull BlockState id) {
        x += offsetX;
        z += offsetZ;
        if (x >= scopeMinX && x < scopeMaxX && y >= minY && y <= maxY && z >= scopeMinZ && z < scopeMaxZ) {
            blockStates[y - minY][x - scopeMinX][z - scopeMinZ] = id;
        }
        return false;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, @NonNull final Pattern pattern) {
        int rx = x + offsetX;
        int rz = z + offsetZ;
        if (rx >= scopeMinX && rx < scopeMaxX && y >= minY && y <= maxY && rz >= scopeMinZ && rz < scopeMaxZ) {
            BlockState state = pattern
                    .applyBlock(super.getMin().getBlockVector3().add(BlockVector3.at(x, y, z)))
                    .toImmutableState();
            blockStates[y - minY][rx - scopeMinX][rz - scopeMinZ] = state;
        }
        return false;
    }

    @Override
    public @NonNull Location getMin() {
        return super.getMin().add(offsetX - scopeMinX, 0, offsetZ - scopeMinZ);
    }

    @Override
    public @NonNull Location getMax() {
        return getMin().add(15, 0, 15).withY(maxY);
    }

    @Override
    public boolean setBlock(int x, int y, int z, final @NonNull BaseBlock id) {
        x += offsetX;
        z += offsetZ;
        if (x >= scopeMinX && x < scopeMaxX && y >= minY && y <= maxY && z >= scopeMinZ && z < scopeMaxZ) {
            blockStates[y - minY][x - scopeMinX][z - scopeMinZ] = id.toImmutableState();
        }
        return false;
    }

    @Override
    public @Nullable BlockState getBlock(final int x, final int y, final int z) {
        if (x >= 0 && x < width && y >= minY && y <= maxY && z >= 0 && z < length) {
            return blockStates[y - minY][x][z];
        }
        return null;
    }

    public void setOffsetX(final int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetZ(final int offsetZ) {
        this.offsetZ = offsetZ;
    }

    @Override
    public int size() {
        return height * width * length;
    }

    @Override
    public boolean setBiome(final int x, final int z, @NonNull final BiomeType biome) {
        //do nothing
        return false;
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, @NonNull final BiomeType biome) {
        //do nothing
        return false;
    }

    @Override
    public void fillBiome(final BiomeType biome) {
        //do nothing
    }

    @Override
    public boolean setTile(final int x, final int y, final int z, @NonNull final CompoundTag tag) {
        //do nothing
        return false;
    }

}
