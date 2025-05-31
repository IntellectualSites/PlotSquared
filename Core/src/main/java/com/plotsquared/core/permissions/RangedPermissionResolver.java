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

import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.stream.IntStream;

/**
 * Represents a resolver for ranged permissions. Return values depend on the actual implementation (see Bukkit module).
 * <br>
 * Even though this interface is not linked to platform implementations by design, implementation-specific details are added to
 * the Javadocs.
 *
 * @since TODO
 */
public interface RangedPermissionResolver {

    int INFINITE_RANGE_VALUE = Integer.MAX_VALUE;

    /**
     * Gets the applicable range value of a player for a specific permission stub
     * ({@code plots.plot} would check for {@code plots.plot.<numeric>}).
     * <br>
     * The standard bukkit implementation would return the lowest numeric value, while the LuckPerms specific resolver would
     * try returning the highest possible value.
     *
     * @param player       the permission holder
     * @param stub         the permission stub to check against
     * @param worldContext the optional world context of the action requiring the range
     * @param range        the maximum permission range to check against (for the default bukkit resolver)
     * @return the applicable range value of the player for the given permission stub
     * @since TODO
     */
    @NonNegative
    int getPermissionRange(
            final @NonNull PlotPlayer<?> player,
            final @NonNull String stub,
            final @Nullable String worldContext,
            final @NonNegative int range
    );

    /**
     * Gets a stream of all applicable permission range values for the given stub. The stream is unordered by default. If a
     * specific order is needed, use the stateful {@link IntStream#sorted()} operation.
     * <br>
     * The standard bukkit implementation will only return a stream containing a single value equal to
     * {@link #getPermissionRange(PlotPlayer, String, String, int)}. For LuckPerms, all applicable node values will be in the
     * stream.
     *
     * @param player       the permission holder
     * @param stub         the permission stub to check against
     * @param worldContext the optional world context of the action requiring the range
     * @param range        the maximum permission range to check against (for the default bukkit resolver)
     * @return a stream of all applicable permission range values for the given stub
     * @since TODO
     */
    @NonNull
    IntStream streamFullPermissionRange(
            final @NonNull PlotPlayer<?> player,
            final @NonNull String stub,
            final @Nullable String worldContext,
            final @NonNegative int range
    );

    /**
     * Checks if the given player has a wildcard range for the given permission stub.
     * <br>
     * For example, if checking for the stub {@code plots.plot}, this method would check for:
     * <ul>
     *     <li>{@code *}</li>
     *     <li>{@code plots.admin}</li>
     *     <li>{@code plots.plot.*}</li>
     *     <li>{@code plots.*}</li>
     * </ul>
     *
     * @param player       the permission holder
     * @param stub         the permission stub to check against
     * @param worldContext the optional world context of the action requiring the range
     * @return {@code true} if the player has a wildcard range for the given permission stub, else {@code false}
     * @since TODO
     */
    default boolean hasWildcardRange(
            final @NonNull PlotPlayer<?> player,
            final @NonNull String stub,
            final @Nullable String worldContext
    ) {
        if (player.hasPermission(Permission.PERMISSION_STAR) ||
                player.hasPermission(Permission.PERMISSION_ADMIN) ||
                player.hasPermission(worldContext, stub + ".*")) {
            return true;
        }
        String node = stub;
        while (true) {
            int lastIndex = node.lastIndexOf('.');
            if (lastIndex == -1) {
                break;
            }
            node = node.substring(0, lastIndex);
            if (player.hasPermission(worldContext, node + ".*")) {
                return true;
            }
        }
        return false;
    }

}
