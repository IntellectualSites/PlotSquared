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
package com.plotsquared.bukkit.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Player manager providing {@link BukkitPlayer Bukkit players}
 */
@Singleton public class BukkitPlayerManager extends PlayerManager<BukkitPlayer, Player> {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject public BukkitPlayerManager(@Nonnull final PlotAreaManager plotAreaManager,
                                       @Nonnull final EventDispatcher eventDispatcher,
                                       @Nullable final EconHandler econHandler) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    @Nonnull @Override public BukkitPlayer getPlayer(@Nonnull final Player object) {
        try {
            return getPlayer(object.getUniqueId());
        } catch (final NoSuchPlayerException exception) {
            return new BukkitPlayer(this.plotAreaManager, this.eventDispatcher, object,
                object.isOnline(), false, this.econHandler);
        }
    }

    @Override @Nonnull public BukkitPlayer createPlayer(@Nonnull final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            throw new NoSuchPlayerException(uuid);
        }
        return new BukkitPlayer(this.plotAreaManager, this.eventDispatcher, player, this.econHandler);
    }

    @Nullable @Override public BukkitOfflinePlayer getOfflinePlayer(@Nullable final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
    }

    @Nonnull @Override public BukkitOfflinePlayer getOfflinePlayer(@Nonnull final String username) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(username));
    }

}
