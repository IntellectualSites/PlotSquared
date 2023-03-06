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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Player manager providing {@link BukkitPlayer Bukkit players}
 */
@Singleton
public class BukkitPlayerManager extends PlayerManager<BukkitPlayer, Player> {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final PermissionHandler permissionHandler;

    @Inject
    public BukkitPlayerManager(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull PermissionHandler permissionHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.permissionHandler = permissionHandler;
    }

    @NonNull
    @Override
    public BukkitPlayer getPlayer(final @NonNull Player object) {
        if (object.getUniqueId().version() == 2) { // not a real player
            return new BukkitPlayer(this.plotAreaManager, this.eventDispatcher, object, false, this.permissionHandler);
        }
        if (!object.isOnline()) {
            throw new NoSuchPlayerException(object.getUniqueId());
        }
        return getPlayer(object.getUniqueId());
    }

    @Override
    public @NonNull BukkitPlayer createPlayer(final @NonNull UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            throw new NoSuchPlayerException(uuid);
        }
        return new BukkitPlayer(this.plotAreaManager, this.eventDispatcher, player, false, this.permissionHandler);
    }

    @Nullable
    @Override
    public BukkitOfflinePlayer getOfflinePlayer(final @Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(uuid), this.permissionHandler);
    }

    @NonNull
    @Override
    public BukkitOfflinePlayer getOfflinePlayer(final @NonNull String username) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(username), this.permissionHandler);
    }

}
