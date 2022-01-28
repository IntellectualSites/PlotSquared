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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
package com.plotsquared.sponge.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.user.UserManager;

import java.util.UUID;

@Singleton
public class SpongePlayerManager extends PlayerManager<SpongePlayer, ServerPlayer> {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final PermissionHandler permissionHandler;
    private final UserManager userManager;

    @Inject
    public SpongePlayerManager(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull PermissionHandler permissionHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.permissionHandler = permissionHandler;
        userManager = Sponge.game().server().userManager();
    }

    @NonNull
    @Override
    public SpongePlayer getPlayer(final @NonNull ServerPlayer object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull SpongePlayer createPlayer(final @NonNull UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public SpongeOfflinePlayer getOfflinePlayer(final @Nullable UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public SpongeOfflinePlayer getOfflinePlayer(final @NonNull String username) {
        throw new UnsupportedOperationException();
    }

}
