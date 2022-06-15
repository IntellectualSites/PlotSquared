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

import com.plotsquared.core.plot.PlotArea;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * Queue Coordinator that only sets blocks with the specified PlotArea
 */
public class AreaBoundDelegateQueueCoordinator extends DelegateQueueCoordinator {

    private final PlotArea area;

    public AreaBoundDelegateQueueCoordinator(final @NonNull PlotArea area, final @Nullable QueueCoordinator parent) {
        super(parent);
        this.area = Objects.requireNonNull(area);
    }

    /**
     * Gets the plot area block settings is limited to
     *
     * @return PlotArea
     */
    public PlotArea getArea() {
        return this.area;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BlockState id) {
        if (area.contains(x, z)) {
            return super.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BaseBlock id) {
        if (area.contains(x, z)) {
            return super.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull Pattern pattern) {
        if (area.contains(x, z)) {
            return super.setBlock(x, y, z, pattern);
        }
        return false;
    }

    @Override
    public boolean setBiome(int x, int z, @NonNull BiomeType biome) {
        if (area.contains(x, z)) {
            return super.setBiome(x, z, biome);
        }
        return false;
    }

    @Override
    public boolean setBiome(int x, int y, int z, @NonNull BiomeType biome) {
        if (area.contains(x, z)) {
            return super.setBiome(x, y, z, biome);
        }
        return false;
    }

    @Override
    public boolean setTile(int x, int y, int z, @NonNull CompoundTag tag) {
        if (area.contains(x, z)) {
            return super.setTile(x, y, z, tag);
        }
        return false;
    }

}
