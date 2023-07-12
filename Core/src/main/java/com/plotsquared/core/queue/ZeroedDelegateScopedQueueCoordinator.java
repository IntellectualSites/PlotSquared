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

import com.plotsquared.core.location.Location;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Queue that only sets blocks with a designated X-Z area, will accept any Y values. Requires all blocks be set normalized to
 * zero in the x and z directions, i.e. starting from 0,0. An offset of the minimum point of the region will then be applied to
 * x and z.
 *
 * @since 7.0.0
 */
public class ZeroedDelegateScopedQueueCoordinator extends DelegateQueueCoordinator {

    private final Location min;
    private final Location max;
    private final int minX;
    private final int minZ;

    private final int maxX;
    private final int maxZ;

    private final int dx;
    private final int dz;

    /**
     * Create a new ScopedQueueCoordinator instance that delegates to a given QueueCoordinator. Locations are inclusive.
     *
     * @since 7.0.0
     */
    public ZeroedDelegateScopedQueueCoordinator(@Nullable QueueCoordinator parent, @NonNull Location min, @NonNull Location max) {
        super(parent);
        this.min = min;
        this.max = max;
        this.minX = min.getX();
        this.minZ = min.getZ();

        this.maxX = max.getX();
        this.maxZ = max.getZ();

        this.dx = maxX - minX;
        this.dz = maxZ - minZ;
    }

    @Override
    public boolean setBiome(int x, int z, @NonNull BiomeType biome) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBiome(x + minX, z + minZ, biome);
    }

    @Override
    public boolean setBiome(int x, int y, int z, @NonNull BiomeType biome) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBiome(x + minX, y, z + minZ, biome);
    }

    public void fillBiome(BiomeType biome) {
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = 0; x <= dx; x++) {
                for (int z = 0; z < dz; z++) {
                    setBiome(x, y, z, biome);
                }
            }
        }
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BaseBlock id) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBlock(x + minX, y, z + minZ, id);
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BlockState id) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBlock(x + minX, y, z + minZ, id);
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull Pattern pattern) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBlock(x + minX, y, z + minZ, pattern);
    }

    @Override
    public boolean setTile(int x, int y, int z, @NonNull CompoundTag tag) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setTile(x + minX, y, z + minZ, tag);
    }

    public @NonNull Location getMin() {
        return min;
    }

    public @NonNull Location getMax() {
        return max;
    }

}
