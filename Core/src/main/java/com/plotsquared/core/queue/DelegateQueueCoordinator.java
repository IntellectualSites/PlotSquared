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
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.function.Consumer;

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
        if (parent != null) {
            return parent.setBlock(x, y, z, pattern);
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        if (parent != null) {
            return parent.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        if (parent != null) {
            return parent.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override public BlockState getBlock(int x, int y, int z) {
        if (parent != null) {
            return parent.getBlock(x, y, z);
        }
        return null;
    }

    @Override public boolean setBiome(int x, int z, BiomeType biome) {
        if (parent != null) {
            return parent.setBiome(x, z, biome);
        }
        return false;
    }

    @Override public boolean setBiome(int x, int y, int z, BiomeType biome) {
        if (parent != null) {
            return parent.setBiome(x, y, z, biome);
        }
        return false;
    }

    @Override public boolean isSettingBiomes() {
        if (parent != null) {
            return parent.isSettingBiomes();
        }
        return false;
    }

    @Override public boolean setEntity(Entity entity) {
        if (parent != null) {
            return parent.setEntity(entity);
        }
        return false;
    }

    @Override public void regenChunk(int x, int z) {
        if (parent != null) {
            parent.regenChunk(x, z);
        }
    }

    @Override public World getWorld() {
        if (parent != null) {
            return parent.getWorld();
        }
        return null;
    }

    @Override public boolean setTile(int x, int y, int z, CompoundTag tag) {
        if (parent != null) {
            return parent.setTile(x, y, z, tag);
        }
        return false;
    }

    @Override public boolean isSettingTiles() {
        if (parent != null) {
            return parent.isSettingTiles();
        }
        return false;
    }

    @Override public boolean enqueue() {
        if (parent != null) {
            return parent.enqueue();
        }
        return false;
    }

    @Override public void start() {
        if (parent != null) {
            parent.start();
        }
    }

    @Override public void cancel() {
        if (parent != null) {
            parent.cancel();
        }
    }

    @Override public void setCompleteTask(Runnable whenDone) {
        if (parent != null) {
            parent.setCompleteTask(whenDone);
        }
    }

    @Override public void setChunkConsumer(Consumer<BlockVector2> consumer) {
        if (parent != null) {
            parent.setChunkConsumer(consumer);
        }
    }

    @Override public CuboidRegion getReadRegion() {
        if (parent != null) {
            return parent.getReadRegion();
        }
        return null;
    }

    @Override public void setReadRegion(CuboidRegion readRegion) {
        if (parent != null) {
            parent.setReadRegion(readRegion);
        }
    }

    @Override public boolean isUnloadAfter() {
        if (parent != null) {
            return parent.isUnloadAfter();
        }
        return false;
    }

    @Override public void setUnloadAfter(boolean setUnloadAfter) {
        if (parent != null) {
            parent.setUnloadAfter(setUnloadAfter);
        }

    }
}