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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.queue;

import com.plotsquared.core.util.ChunkUtil;
import com.plotsquared.core.util.MathMan;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;

public class LocalChunk {

    private final QueueCoordinator parent;
    private final int x;
    private final int z;
    private final int minSection;

    private final BaseBlock[][] baseblocks;
    private final BiomeType[][] biomes;
    private final HashMap<BlockVector3, CompoundTag> tiles = new HashMap<>();
    private final HashMap<Location, BaseEntity> entities = new HashMap<>();

    public LocalChunk(@NonNull QueueCoordinator parent, int x, int z) {
        this.parent = parent;
        this.x = x;
        this.z = z;
        this.minSection = parent.getWorld() != null ? (parent.getWorld().getMinY() >> 4) : 0;
        int sections = parent.getWorld() != null ? (parent.getWorld().getMaxY() >> 4) - minSection + 1 : 16;
        baseblocks = new BaseBlock[sections][];
        biomes = new BiomeType[sections][];
    }

    public @NonNull QueueCoordinator getParent() {
        return this.parent;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    /**
     * Get the minimum layer position stored (usually -4 or 0).
     *
     * @since TODO
     */
    public int getMinSection() {
        return this.minSection;
    }

    public @NonNull BaseBlock[][] getBaseblocks() {
        return this.baseblocks;
    }

    public @NonNull BiomeType[][] getBiomes() {
        return this.biomes;
    }

    public @NonNull HashMap<BlockVector3, CompoundTag> getTiles() {
        return this.tiles;
    }

    public void setBiome(final int x, final int y, final int z, final @NonNull BiomeType biomeType) {
        final int i = (y >> 4) - minSection;
        final int j = ChunkUtil.getJ(x, y, z);
        BiomeType[] array = this.biomes[i];
        if (array == null) {
            array = this.biomes[i] = new BiomeType[4096];
        }
        array[j] = biomeType;
    }

    @Override
    public int hashCode() {
        return MathMan.pair((short) x, (short) z);
    }

    public void setBlock(final int x, final int y, final int z, final @NonNull BaseBlock baseBlock) {
        final int i = (y >> 4) - minSection;
        final int j = ChunkUtil.getJ(x, y, z);
        BaseBlock[] array = baseblocks[i];
        if (array == null) {
            array = (baseblocks[i] = new BaseBlock[4096]);
        }
        array[j] = baseBlock;
    }

    public void setTile(final int x, final int y, final int z, final @NonNull CompoundTag tag) {
        tiles.put(BlockVector3.at(x, y, z), tag);
    }

    public void setEntity(@NonNull Location location, @NonNull BaseEntity entity) {
        this.entities.put(location, entity);
    }

    public @NonNull HashMap<Location, BaseEntity> getEntities() {
        return this.entities;
    }

}
