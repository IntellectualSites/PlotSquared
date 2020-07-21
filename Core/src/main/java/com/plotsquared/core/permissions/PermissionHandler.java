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
package com.plotsquared.core.permissions;

import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * Permission handler
 */
public interface PermissionHandler {

    /**
     * Initialize the permission handler
     */
    void initialize();

    /**
     * Attempt to construct a permission profile for a plot player
     *
     * @param playerPlotPlayer Plot player
     * @return Permission profile, if one was able to be constructed
     */
    @Nonnull Optional<PermissionProfile> getPermissionProfile(
        @Nonnull PlotPlayer<?> playerPlotPlayer);

    /**
     * Attempt to construct a permission profile for an offline plot player
     *
     * @param offlinePlotPlayer Offline player
     * @return Permission profile, if one was able to be constructed
     */
    @Nonnull Optional<PermissionProfile> getPermissionProfile(
        @Nonnull OfflinePlotPlayer offlinePlotPlayer);

    /**
     * Get all capabilities that the permission handler has
     *
     * @return Immutable set of capabilities
     */
    @Nonnull Set<PermissionHandlerCapability> getCapabilities();

    /**
     * Check whether or not the permission handler has a given capability
     *
     * @param capability Capability
     * @return {@code true} if the handler has the capability, else {@code false}
     */
    default boolean hasCapability(@Nonnull final PermissionHandlerCapability capability) {
        return this.getCapabilities().contains(capability);
    }


    /**
     * Permission handler capabilities
     */
    enum PermissionHandlerCapability {
        /**
         * The ability to check for online (player) permissions
         */
        ONLINE_PERMISSIONS,
        /**
         * The ability to check for offline (player) permissions
         */
        OFFLINE_PERMISSIONS
    }

}
