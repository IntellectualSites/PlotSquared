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
package com.plotsquared.core.location;

import com.intellectualsites.annotations.DoNotUse;
import com.sk89q.worldedit.math.BlockVector3;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Used internally for generation to reference locations in worlds that "don't exist yet". There is no guarantee that the world
 * name provided by {@link UncheckedWorldLocation#getWorldName()} exists on the server.
 *
 * @since 6.9.0
 */
@DoNotUse
public final class UncheckedWorldLocation extends Location {

    private final String worldName;

    /**
     * @since 6.9.0
     */
    private UncheckedWorldLocation(
            final @NonNull String worldName, final int x, final int y, final int z
    ) {
        super(World.nullWorld(), BlockVector3.at(x, y, z), 0f, 0f);
        this.worldName = worldName;
    }

    /**
     * Construct a new location with yaw and pitch equal to 0
     *
     * @param world World
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @return New location
     * @since 6.9.0
     */
    @DoNotUse
    public static @NonNull UncheckedWorldLocation at(
            final @NonNull String world, final int x, final int y, final int z
    ) {
        return new UncheckedWorldLocation(world, x, y, z);
    }

    /**
     * Construct a new location with yaw and pitch equal to 0
     *
     * @param world World
     * @param loc   Coordinates
     * @return New location
     * @since 7.0.0
     */
    @DoNotUse
    public static @NonNull UncheckedWorldLocation at(final @NonNull String world, BlockVector3 loc) {
        return new UncheckedWorldLocation(world, loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    @DoNotUse
    public @NonNull String getWorldName() {
        return this.worldName;
    }

}
