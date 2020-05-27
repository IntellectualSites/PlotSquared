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
package com.plotsquared.core.util;

import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player instances
 */
public abstract class PlayerManager<P extends PlotPlayer<? extends T>, T> {

    private final Map<UUID, P> playerMap = new HashMap<>();
    private final Object playerLock = new Object();

    /**
     * Remove a player from the player map
     *
     * @param plotPlayer Player to remove
     */
    public void removePlayer(@NotNull final PlotPlayer plotPlayer) {
        synchronized (playerLock) {
            this.playerMap.remove(plotPlayer.getUUID());
        }
    }

    /**
     * Get the player from its UUID if it is stored in the player map.
     *
     * @param uuid Player UUID
     * @return Player, or null
     */
    @Nullable public P getPlayerIfExists(@Nullable final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return this.playerMap.get(uuid);
    }

    @Nullable public P getPlayerIfExists(@Nullable final String name) {
        for (final P plotPlayer : this.playerMap.values()) {
            if (plotPlayer.getName().equalsIgnoreCase(name)) {
                return plotPlayer;
            }
        }
        return null;
    }

    /**
     * Get a plot player from a platform player object. This method requires
     * that the caller actually knows that the player exists.
     * <p>
     * The method will throw an exception if there is no such
     * player online.
     *
     * @param object Platform player object
     * @return Player object
     */
    @NotNull public abstract P getPlayer(@NotNull final T object);

    /**
     * Get a plot player from a UUID. This method requires
     * that the caller actually knows that the player exists.
     * <p>
     * The method will throw an exception if there is no such
     * player online.
     *
     * @param uuid Player UUID
     * @return Player object
     */
    @NotNull public P getPlayer(@NotNull final UUID uuid) {
        synchronized (playerLock) {
            P player = this.playerMap.get(uuid);
            if (player == null) {
                player = createPlayer(uuid);
                this.playerMap.put(uuid, player);
            }
            return player;
        }
    }

    @NotNull protected abstract P createPlayer(@NotNull final UUID uuid);

    /**
     * Get an an offline player object from the player's UUID
     *
     * @param uuid Player UUID
     * @return Offline player object
     */
    @Nullable public abstract OfflinePlotPlayer getOfflinePlayer(@Nullable final UUID uuid);

    /**
     * Get an offline player object from the player's username
     *
     * @param username Player name
     * @return Offline player object
     */
    @Nullable public abstract OfflinePlotPlayer getOfflinePlayer(@NotNull final String username);

    /**
     * Get all online players
     *
     * @return Unmodifiable collection of players
     */
    public Collection<P> getPlayers() {
        return Collections.unmodifiableCollection(this.playerMap.values());
    }


    public static final class NoSuchPlayerException extends IllegalArgumentException {

        public NoSuchPlayerException(@NotNull final UUID uuid) {
            super(String.format("There is no online player with UUID '%s'", uuid.toString()));
        }

        @Override public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

}
