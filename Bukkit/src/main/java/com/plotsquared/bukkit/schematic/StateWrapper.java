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
package com.plotsquared.bukkit.schematic;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.sk89q.jnbt.CompoundTag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public sealed interface StateWrapper permits StateWrapperSpigot {

    StateWrapper INSTANCE = Factory.createStateWrapper();

    boolean restore(final @NonNull Block block, final @NonNull CompoundTag data);

    default boolean restore(final String worldName, final int x, final int y, final int z, final CompoundTag data) {
        final World world = BukkitUtil.getWorld(worldName);
        if (world == null) {
            return false;
        }
        return this.restore(world.getBlockAt(x, y, z), data);
    }

    @ApiStatus.Internal
    final class Factory {

        private static StateWrapper createStateWrapper() {
            return new StateWrapperSpigot();
        }

    }

}
