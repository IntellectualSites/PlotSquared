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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Queue that delegates to a parent queue.
 */
public class DelegateQueueCoordinator extends QueueCoordinator {

    private final QueueCoordinator parent;

    public DelegateQueueCoordinator(QueueCoordinator parent) {
        super(parent == null ? null : parent.getWorld());
        this.parent = parent;

        if (parent != null) {
            this.setForceSync(parent.isForceSync());
            this.setShouldGen(parent.isShouldGen());
        }
    }

    public QueueCoordinator getParent() {
        return parent;
    }

    @Override
    public int size() {
        if (parent != null) {
            return parent.size();
        }
        return 0;
    }

    @Override
    public void setModified(long modified) {
        if (parent != null) {
            parent.setModified(modified);
        }
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull Pattern pattern) {
        if (parent != null) {
            return parent.setBlock(x, y, z, pattern);
        }
        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BaseBlock id) {
        if (parent != null) {
            return parent.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, @NonNull BlockState id) {
        if (parent != null) {
            return parent.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override
    public @Nullable BlockState getBlock(int x, int y, int z) {
        if (parent != null) {
            return parent.getBlock(x, y, z);
        }
        return null;
    }

    @Override
    public boolean setBiome(int x, int z, @NonNull BiomeType biome) {
        if (parent != null) {
            return parent.setBiome(x, z, biome);
        }
        return false;
    }

    @Override
    public boolean setBiome(int x, int y, int z, @NonNull BiomeType biome) {
        if (parent != null) {
            return parent.setBiome(x, y, z, biome);
        }
        return false;
    }

    @Override
    public boolean isSettingBiomes() {
        if (parent != null) {
            return parent.isSettingBiomes();
        }
        return false;
    }

    @Override
    public void setBiomesEnabled(final boolean enabled) {
        if (parent != null) {
            parent.setBiomesEnabled(enabled);
        }
    }

    @Override
    public boolean setEntity(@NonNull Entity entity) {
        if (parent != null) {
            return parent.setEntity(entity);
        }
        return false;
    }

    @Override
    public void regenChunk(int x, int z) {
        if (parent != null) {
            parent.regenChunk(x, z);
        }
    }

    @Override
    public @Nullable World getWorld() {
        if (parent != null) {
            return parent.getWorld();
        }
        return null;
    }

    @Override
    public boolean setTile(int x, int y, int z, @NonNull CompoundTag tag) {
        if (parent != null) {
            return parent.setTile(x, y, z, tag);
        }
        return false;
    }

    @Override
    public boolean isSettingTiles() {
        if (parent != null) {
            return parent.isSettingTiles();
        }
        return false;
    }

    @Override
    public boolean enqueue() {
        if (parent != null) {
            return parent.enqueue();
        }
        return false;
    }

    @Override
    public void start() {
        if (parent != null) {
            parent.start();
        }
    }

    @Override
    public void cancel() {
        if (parent != null) {
            parent.cancel();
        }
    }

    @Override
    public Runnable getCompleteTask() {
        if (parent != null) {
            return parent.getCompleteTask();
        }
        return null;
    }

    @Override
    public void setCompleteTask(Runnable whenDone) {
        if (parent != null) {
            parent.setCompleteTask(whenDone);
        }
    }

    @Override
    public @Nullable Consumer<BlockVector2> getChunkConsumer() {
        if (parent != null) {
            return parent.getChunkConsumer();
        }
        return null;
    }

    @Override
    public void setChunkConsumer(@NonNull Consumer<BlockVector2> consumer) {
        if (parent != null) {
            parent.setChunkConsumer(consumer);
        }
    }

    @Override
    public void addProgressSubscriber(@NonNull ProgressSubscriber progressSubscriber) {
        if (parent != null) {
            parent.addProgressSubscriber(progressSubscriber);
        }
    }

    @Override
    public @NonNull LightingMode getLightingMode() {
        if (parent != null) {
            return parent.getLightingMode();
        }
        return LightingMode.valueOf(Settings.QUEUE.LIGHTING_MODE);
    }

    @Override
    public void setLightingMode(@Nullable LightingMode mode) {
        if (parent != null) {
            parent.setLightingMode(mode);
        }
    }

    @Override
    public SideEffectSet getSideEffectSet() {
        if (parent != null) {
            return parent.getSideEffectSet();
        }
        return null;
    }

    @Override
    public void setSideEffectSet(final SideEffectSet sideEffectSet) {
        if (parent != null) {
            parent.setSideEffectSet(sideEffectSet);
        }
    }

    @Override
    public @NonNull List<BlockVector2> getReadChunks() {
        if (parent != null) {
            return parent.getReadChunks();
        }
        return new ArrayList<>();
    }

    @Override
    public void addReadChunks(@NonNull Set<BlockVector2> readChunks) {
        if (parent != null) {
            parent.addReadChunks(readChunks);
        }
    }

    @Override
    public void addReadChunk(@NonNull BlockVector2 chunk) {
        if (parent != null) {
            parent.addReadChunk(chunk);
        }
    }

    @Override
    public boolean isUnloadAfter() {
        if (parent != null) {
            return parent.isUnloadAfter();
        }
        return false;
    }

    @Override
    public void setUnloadAfter(boolean setUnloadAfter) {
        if (parent != null) {
            parent.setUnloadAfter(setUnloadAfter);
        }
    }

    @Override
    public @Nullable CuboidRegion getRegenRegion() {
        if (parent != null) {
            return parent.getRegenRegion();
        }
        return null;
    }

    @Override
    public void setRegenRegion(@NonNull CuboidRegion regenRegion) {
        if (parent != null) {
            parent.setRegenRegion(regenRegion);
        }
    }

}
