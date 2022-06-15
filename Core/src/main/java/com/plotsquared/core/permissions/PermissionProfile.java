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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A permission profile that can be used to check for permissions
 */
public interface PermissionProfile {

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
     * Check if the owner of the profile has a given permission
     *
     * @param world      World name
     * @param permission Permission
     * @return {@code true} if the owner has the given permission, else {@code false}
     */
    boolean hasPermission(final @Nullable String world, @NonNull String permission);

    /**
     * Check if the owner of the profile has a given (global) keyed permission. Checks both {@code permission.key}
     * and {@code permission.*}
     *
     * @param permission Permission
     * @param key        Permission "key"
     * @return {@code true} if the owner has the given permission, else {@code false}
     * @since 6.0.10
     */
    @SuppressWarnings("unused")
    default boolean hasKeyedPermission(
            final @NonNull String permission,
            final @NonNull String key
    ) {
        return hasKeyedPermission(null, permission, key);
    }

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
    boolean hasKeyedPermission(
            @Nullable String world, final @NonNull String permission,
            final @NonNull String key
    );

}
