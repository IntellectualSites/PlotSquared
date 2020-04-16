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

import com.plotsquared.core.PlotSquared;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import javax.annotation.Nullable;

public class LocationOffsetDelegateLocalBlockQueue extends DelegateLocalBlockQueue {

    private final boolean[][] canPlace;
    private final int blockX;
    private final int blockZ;

    public LocationOffsetDelegateLocalBlockQueue(final boolean[][] canPlace,
        final int blockX, final int blockZ,
        @Nullable LocalBlockQueue parent) {
        super(parent);
        this.canPlace = canPlace;
        this.blockX = blockX;
        this.blockZ = blockZ;
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        if (canPlace[x - blockX][z - blockZ]) {
            return super.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        try {
            if (canPlace[x - blockX][z - blockZ]) {
                return super.setBlock(x, y, z, id);
            }
        } catch (final Exception e) {
            PlotSquared.debug(String.format("Failed to set block at: %d;%d;%d (to = %s) with offset %d;%d."
                + " Translated to: %d;%d", x, y, z, id, blockX, blockZ, x - blockX, z - blockZ));
            throw e;
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, Pattern pattern) {
        final BlockVector3 blockVector3 = BlockVector3.at(x + blockX, y, z + blockZ);
        return this.setBlock(x, y, z, pattern.apply(blockVector3));
    }

    @Override public boolean setBiome(int x, int y, BiomeType biome) {
        return super.setBiome(x, y, biome);
    }

}
