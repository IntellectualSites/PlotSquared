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

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any object which can hold permissions
 */
public interface PermissionHolder {

    /**
     * Check if the owner of the profile has a given (global) permission
     *
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    default boolean hasPermission(@Nonnull final String permission) {
        return hasPermission(null ,permission);
    }

    /**
     * Check the the highest permission a PlotPlayer has within a specified range.<br>
     * - Excessively high values will lag<br>
     * - The default range that is checked is {@link Settings.Limit#MAX_PLOTS}<br>
     *
     * @param stub   The permission stub to check e.g. for `plots.plot.#` the stub is `plots.plot`
     * @param range  The range to check
     * @return The highest permission they have within that range
     */
    @Nonnegative default int hasPermissionRange(@Nonnull final String stub,
                                                @Nonnegative final int range) {
        if (hasPermission(Captions.PERMISSION_ADMIN.getTranslated())) {
            return Integer.MAX_VALUE;
        }
        String[] nodes = stub.split("\\.");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            builder.append(nodes[i]).append(".");
            if (!stub.equals(builder + Captions.PERMISSION_STAR.getTranslated())) {
                if (hasPermission(builder + Captions.PERMISSION_STAR.getTranslated())) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        if (hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        for (int i = range; i > 0; i--) {
            if (hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Check if the owner of the profile has a given permission
     *
     * @param world      World name
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    boolean hasPermission(@Nullable String world, @Nonnull String permission);

}
