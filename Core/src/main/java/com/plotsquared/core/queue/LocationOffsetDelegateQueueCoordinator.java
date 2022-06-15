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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Offsets input coordinates and delegates to a parent queue
 */
public class LocationOffsetDelegateQueueCoordinator extends DelegateQueueCoordinator {

    private final boolean[][] canPlace;
    private final int blockX;
    private final int blockZ;

    public LocationOffsetDelegateQueueCoordinator(
            final boolean[][] canPlace,
            final int blockX,
            final int blockZ,
            @Nullable QueueCoordinator parent
    ) {
        super(parent);
        this.canPlace = canPlace;
        this.blockX = blockX;
        this.blockZ = blockZ;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BlockState id) {
        try {
            if (canPlace[x - blockX][z - blockZ]) {
                return super.setBlock(x, y, z, id);
            }
        } catch (final Exception e) {
            throw e;
        }
        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BaseBlock id) {
        try {
            if (canPlace[x - blockX][z - blockZ]) {
                return super.setBlock(x, y, z, id);
            }
        } catch (final Exception e) {
            throw e;
        }
        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull Pattern pattern) {
        final BlockVector3 blockVector3 = BlockVector3.at(x + blockX, y, z + blockZ);
        return this.setBlock(x, y, z, pattern.applyBlock(blockVector3));
    }

    @Override
    public boolean setBiome(int x, int z, @NonNull BiomeType biome) {
        try {
            if (canPlace[x - blockX][z - blockZ]) {
                return super.setBiome(x, z, biome);
            }
        } catch (final Exception e) {
            throw e;
        }
        return false;
    }

    @Override
    public boolean setBiome(int x, int y, int z, @NonNull BiomeType biome) {
        try {
            if (canPlace[x - blockX][z - blockZ]) {
                return super.setBiome(x, y, z, biome);
            }
        } catch (final Exception e) {
            throw e;
        }
        return false;
    }

    @Override
    public boolean setTile(int x, int y, int z, @NonNull CompoundTag tag) {
        try {
            if (canPlace[x - blockX][z - blockZ]) {
                return super.setTile(x, y, z, tag);
            }
        } catch (final Exception e) {
            throw e;
        }
        return false;
    }

}
