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
package com.plotsquared.core.player;

import com.plotsquared.core.permissions.PermissionHolder;

import java.util.UUID;

public interface OfflinePlotPlayer extends PermissionHolder {

    /**
     * Returns the UUID of the player.
     *
     * @return the UUID of the player
     */
    UUID getUUID();

    /**
     * Gets the time in milliseconds when the player was last seen online.
     *
     * @return the time in milliseconds when last online
     */
    long getLastPlayed();

    /**
     * Returns the name of the player.
     *
     * @return the name of the player
     */
    String getName();

}
