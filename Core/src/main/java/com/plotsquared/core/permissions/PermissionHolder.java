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
package com.plotsquared.core.permissions;

import com.plotsquared.core.configuration.Settings;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    default boolean hasPermission(final @NonNull String permission) {
        return hasPermission(null, permission);
    }

    /**
     * Check if the owner of the profile has a given (global) permission
     *
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    default boolean hasPermission(final @NonNull Permission permission) {
        return hasPermission(permission.toString());
    }

    /**
     * Check if the owner of the profile has a given (global) keyed permission. Checks both {@code permission.key}
     * and {@code permission.*}
     *
     * @param permission Permission
     * @param key        Permission "key"
     * @return {@code true} if the owner has the given permission, else {@code false}
     * @since 6.0.10
     */
    default boolean hasKeyedPermission(
            final @NonNull String permission,
            final @NonNull String key
    ) {
        return hasKeyedPermission(null, permission, key);
    }

    /**
     * Check the highest permission a PlotPlayer has within a specified range.<br>
     * - Excessively high values will lag<br>
     * - The default range that is checked is {@link Settings.Limit#MAX_PLOTS}<br>
     *
     * @param stub  The permission stub to check e.g. for `plots.plot.#` the stub is `plots.plot`
     * @param range The range to check
     * @return The highest permission they have within that range
     */
    @NonNegative
    default int hasPermissionRange(
            final @NonNull Permission stub,
            @NonNegative final int range
    ) {
        return hasPermissionRange(stub.toString(), range);
    }

    /**
     * Check the highest permission a PlotPlayer has within a specified range.<br>
     * - Excessively high values will lag<br>
     * - The default range that is checked is {@link Settings.Limit#MAX_PLOTS}<br>
     *
     * @param stub  The permission stub to check e.g. for `plots.plot.#` the stub is `plots.plot`
     * @param range The range to check
     * @return The highest permission they have within that range
     */
    @NonNegative
    default int hasPermissionRange(
            final @NonNull String stub,
            @NonNegative final int range
    ) {
        if (hasPermission(Permission.PERMISSION_ADMIN.toString())) {
            return Integer.MAX_VALUE;
        }
        String[] nodes = stub.split("\\.");
        StringBuilder builder = new StringBuilder();
        // Wildcard check from less specific permission to more specific permission
        for (int i = 0; i < (nodes.length - 1); i++) {
            builder.append(nodes[i]).append(".");
            if (!stub.equals(builder + Permission.PERMISSION_STAR.toString())) {
                if (hasPermission(builder + Permission.PERMISSION_STAR.toString())) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        // Wildcard check for the full permission
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
     * Checks if the owner of the profile has a permission, and optionally send the no permission message if applicable.
     *
     * @param permission Permission
     * @param notify     If to notify the permission holder
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    default boolean hasPermission(@NonNull Permission permission, boolean notify) {
        return hasPermission(permission.toString(), notify);
    }

    /**
     * Checks if the owner of the profile has a permission, and optionally send the no permission message if applicable.
     *
     * @param permission Permission
     * @param notify     If to notify the permission holder
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    boolean hasPermission(@NonNull String permission, boolean notify);

    /**
     * Check if the owner of the profile has a given permission
     *
     * @param world      World name
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    boolean hasPermission(@Nullable String world, @NonNull String permission);

    /**
     * Check if the owner of the profile has a given keyed permission. Checks both {@code permission.key}
     * and {@code permission.*}
     *
     * @param world      World name
     * @param permission Permission
     * @param key        Permission "key"
     * @return {@code true} if the owner has the given permission, else {@code false}
     * @since 6.0.10
     */
    boolean hasKeyedPermission(@Nullable String world, @NonNull String permission, @NonNull String key);

}
