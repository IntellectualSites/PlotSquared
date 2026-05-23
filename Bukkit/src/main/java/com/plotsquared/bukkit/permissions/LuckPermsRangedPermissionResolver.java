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
package com.plotsquared.bukkit.permissions;

import com.plotsquared.core.permissions.RangedPermissionResolver;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MathMan;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.stream.IntStream;

public class LuckPermsRangedPermissionResolver implements RangedPermissionResolver {

    private final LuckPerms luckPerms;

    public LuckPermsRangedPermissionResolver() {
        this.luckPerms = Objects.requireNonNull(
                Bukkit.getServicesManager().getRegistration(LuckPerms.class),
                "LuckPerms is not available"
        ).getProvider();
    }

    @Override
    public @NonNegative int getPermissionRange(
            final @NonNull PlotPlayer<?> player,
            final @NonNull String stub,
            final @Nullable String worldContext,
            final @NonNegative int range
    ) {
        // no need to use LuckPerms for basic checks
        if (this.hasWildcardRange(player, stub, worldContext)) {
            return INFINITE_RANGE_VALUE;
        }
        return this.streamFullPermissionRange(player, stub, worldContext, range)
                .sorted()
                .reduce((first, second) -> second)
                .orElse(0);
    }

    @NonNull
    public IntStream streamFullPermissionRange(
            final @NonNull PlotPlayer<?> player,
            final @NonNull String stub,
            final @Nullable String worldContext,
            @NonNegative final int range
    ) {
        final User user = this.luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) {
            throw new IllegalStateException("Luckperms User is null - is the Player online? (UUID: %s)".formatted(player.getUUID()));
        }
        final QueryOptions queryOptions = worldContext == null ?
                QueryOptions.nonContextual() :
                QueryOptions.contextual(ImmutableContextSet.of("world", worldContext));
        return user.resolveInheritedNodes(queryOptions).stream()
                // only support normal permission nodes (regex permission nodes would be a pita to support)
                .filter(NodeType.PERMISSION::matches)
                .map(node -> ((PermissionNode) node).getPermission())
                // check that the node actually has additional data after the stub
                .filter(permission -> permission.startsWith(stub + "."))
                // extract the raw data after the stub
                .map(permission -> permission.substring(stub.length() + 1))
                // check if data is integer and parse
                .filter(MathMan::isInteger)
                .mapToInt(Integer::parseInt)
                // only use values that are positive
                .filter(value -> value > -1);
    }

}
