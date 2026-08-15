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
package com.plotsquared.bukkit.player;

import com.plotsquared.core.permissions.NullPermissionProfile;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.PermissionProfile;
import com.plotsquared.core.player.OfflinePlotPlayer;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class BukkitOfflinePlayer implements OfflinePlotPlayer {

    public final OfflinePlayer player;
    private final PermissionProfile permissionProfile;

    /**
     * Please do not use this method. Instead use BukkitUtil.getPlayer(Player),
     * as it caches player objects.
     *
     * @param player            Bukkit OfflinePlayer player to convert
     * @param permissionHandler Permission Profile to be used
     */
    public BukkitOfflinePlayer(
            final @NonNull OfflinePlayer player, final @NonNull
    PermissionHandler permissionHandler
    ) {
        this.player = player;
        this.permissionProfile = permissionHandler.getPermissionProfile(this)
                .orElse(NullPermissionProfile.INSTANCE);
    }

    @NonNull
    @Override
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    @NonNegative
    public long getLastPlayed() {
        return this.player.getLastSeen();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public boolean hasPermission(
            final @Nullable String world,
            final @NonNull String permission
    ) {
        return this.permissionProfile.hasPermission(world, permission);
    }

    @Override
    public boolean hasKeyedPermission(
            final @Nullable String world,
            final @NonNull String stub,
            final @NonNull String key
    ) {
        return this.permissionProfile.hasPermission(world, stub + "." + key) || this.permissionProfile.hasPermission(
                world,
                stub + ".*"
        );
    }

    @Override
    public boolean hasPermission(@NonNull final String permission, final boolean notify) {
        return hasPermission(permission);
    }

}
