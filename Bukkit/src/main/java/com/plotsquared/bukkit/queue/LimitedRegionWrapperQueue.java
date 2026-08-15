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
package com.plotsquared.bukkit.queue;

import com.plotsquared.bukkit.schematic.StateWrapper;
import com.plotsquared.core.queue.DelegateQueueCoordinator;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.LimitedRegion;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Wraps a {@link LimitedRegion} inside a {@link com.plotsquared.core.queue.QueueCoordinator} so it can be written to.
 *
 * @since 6.9.0
 */
public class LimitedRegionWrapperQueue extends DelegateQueueCoordinator {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + LimitedRegionWrapperQueue.class.getSimpleName());

    private final LimitedRegion limitedRegion;
    private boolean useOtherRestoreTagMethod = false;

    /**
     * @since 6.9.0
     */
    public LimitedRegionWrapperQueue(LimitedRegion limitedRegion) {
        super(null);
        this.limitedRegion = limitedRegion;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, @NonNull final Pattern pattern) {
        return setBlock(x, y, z, pattern.applyBlock(BlockVector3.at(x, y, z)));
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, @NonNull final BaseBlock id) {
        boolean result = setBlock(x, y, z, id.toImmutableState());
        if (result && id.hasNbtData()) {
            CompoundTag tag = id.getNbtData();
            StateWrapper sw = new StateWrapper(tag);
            try {
                if (useOtherRestoreTagMethod && getWorld() != null) {
                    sw.restoreTag(getWorld().getName(), x, y, z);
                } else {
                    sw.restoreTag(limitedRegion.getBlockState(x, y, z).getBlock());
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Error attempting to populate tile entity into the world at location {},{},{}", x, y, z, e);
                return false;
            } catch (IllegalStateException e) {
                useOtherRestoreTagMethod = true;
                LOGGER.warn("IllegalStateException attempting to populate tile entity into the world at location {},{},{}. " +
                        "Possibly on <=1.17.1, switching to secondary method.", x, y, z, e);
            }
        }
        return result;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, @NonNull final BlockState id) {
        try {
            limitedRegion.setType(x, y, z, BukkitAdapter.adapt(id.getBlockType()));
            limitedRegion.setBlockData(x, y, z, BukkitAdapter.adapt(id));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error attempting to populate block into the world at location {},{},{}", x, y, z, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean setEntity(@NonNull final Entity entity) {
        EntityType type = BukkitAdapter.adapt(entity.getState().getType());
        double x = entity.getLocation().getX();
        double y = entity.getLocation().getY();
        double z = entity.getLocation().getZ();
        Location location = new Location(limitedRegion.getWorld(), x, y, z);
        try {
            limitedRegion.spawnEntity(location, type);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error attempting to populate entity into the world at location {},{},{}", (int) x, (int) y, (int) z, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean setTile(final int x, final int y, final int z, @NonNull final CompoundTag tag) {
        StateWrapper sw = new StateWrapper(tag);
        try {
            return sw.restoreTag(limitedRegion.getBlockState(x, y, z).getBlock());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error attempting to populate tile entity into the world at location {},{},{}", x, y, z, e);
            return false;
        }
    }

    @Override
    public boolean isSettingTiles() {
        return true;
    }

}
