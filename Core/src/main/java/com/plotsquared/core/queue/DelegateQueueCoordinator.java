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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

public class DelegateQueueCoordinator extends QueueCoordinator {

    private final QueueCoordinator parent;

    public DelegateQueueCoordinator(QueueCoordinator parent) {
        this.parent = parent;

        if (parent != null) {
            this.setForceSync(parent.isForceSync());
        }
    }

    public QueueCoordinator getParent() {
        return parent;
    }

    @Override public int size() {
        if (parent != null) {
            return parent.size();
        }
        return 0;
    }

    @Override public void setModified(long modified) {
        if (parent != null) {
            parent.setModified(modified);
        }
    }

    @Override public boolean setBlock(int x, int y, int z, Pattern pattern) {
        return parent.setBlock(x, y, z, pattern);
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        return parent.setBlock(x, y, z, id);
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        return parent.setBlock(x, y, z, id);
    }

    @Override public BlockState getBlock(int x, int y, int z) {
        return parent.getBlock(x, y, z);
    }

    @Override public boolean setBiome(int x, int z, BiomeType biome) {
        return parent.setBiome(x, z, biome);
    }

    @Override public boolean settingBiome() {
        return parent.settingBiome();
    }

    @Override public String getWorld() {
        return parent.getWorld();
    }

    @Override public boolean setTile(int x, int y, int z, CompoundTag tag) {
        return parent.setTile(x, y, z, tag);
    }

    @Override public boolean enqueue() {
        if (parent != null) {
            return parent.enqueue();
        }
        return false;
    }
}
