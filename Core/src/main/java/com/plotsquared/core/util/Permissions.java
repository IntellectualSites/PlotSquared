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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.PermissionHolder;
import com.plotsquared.core.player.PlotPlayer;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

/**
 * The Permissions class handles checking user permissions.<br>
 * - This will respect * nodes and plots.admin and can be used to check permission ranges (e.g. plots.plot.5)<br>
 * - Checking the PlotPlayer class directly will not take the above into account<br>
 */
public class Permissions {

    public static boolean hasPermission(PlotPlayer<?> player, Permission permission, boolean notify) {
        return hasPermission(player, permission.toString(), notify);
    }

    /**
     * Check if the owner of the profile has a given (global) permission
     *
     * @param caller permission holder
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    public static boolean hasPermission(@Nonnull final PermissionHolder caller, @Nonnull final Permission permission) {
        return caller.hasPermission(permission.toString());
    }

    /**
     * Check if the owner of the profile has a given (global) permission
     *
     * @param caller permission holder
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    public static boolean hasPermission(@Nonnull final PermissionHolder caller, @Nonnull final String permission) {
        return caller.hasPermission(permission);
    }

    /**
     * Checks if a PlotPlayer has a permission, and optionally send the no permission message if applicable.
     *
     * @param player permission holder
     * @param permission permission
     * @param notify if to notify the permission holder
     * @return if permission is had
     */
    public static boolean hasPermission(PlotPlayer<?> player, String permission, boolean notify) {
        if (!hasPermission(player, permission)) {
            if (notify) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", permission)
                );
            }
            return false;
        }
        return true;
    }

    public static int hasPermissionRange(PlotPlayer<?> player, Permission Permission, int range) {
        return hasPermissionRange(player, Permission.toString(), range);
    }

    /**
     * Check the the highest permission a PlotPlayer has within a specified range.<br>
     * - Excessively high values will lag<br>
     * - The default range that is checked is {@link Settings.Limit#MAX_PLOTS}<br>
     *
     * @param player Player to check for
     * @param stub   The permission stub to check e.g. for `plots.plot.#` the stub is `plots.plot`
     * @param range  The range to check
     * @return The highest permission they have within that range
     */
    public static int hasPermissionRange(PlotPlayer<?> player, String stub, int range) {
        return player.hasPermissionRange(stub, range);
    }
}
