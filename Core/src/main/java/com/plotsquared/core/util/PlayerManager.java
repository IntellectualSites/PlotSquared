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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.uuid.UUIDMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Manages player instances
 */
public abstract class PlayerManager<P extends PlotPlayer<? extends T>, T> {

    private final Map<UUID, P> playerMap = new HashMap<>();
    private final Object playerLock = new Object();

    public static void getUUIDsFromString(@Nonnull final String list,
        @Nonnull final BiConsumer<Collection<UUID>, Throwable> consumer) {
        String[] split = list.split(",");

        final Set<UUID> result = new HashSet<>();
        final List<String> request = new LinkedList<>();

        for (final String name : split) {
            if (name.isEmpty()) {
                consumer.accept(Collections.emptySet(), null);
                return;
            } else if ("*".equals(name)) {
                result.add(DBFunc.EVERYONE);
            } else if (name.length() > 16) {
                try {
                    result.add(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                    consumer.accept(Collections.emptySet(), null);
                    return;
                }
            } else {
                request.add(name);
            }
        }

        if (request.isEmpty()) {
            consumer.accept(result, null);
        } else {
            PlotSquared.get().getImpromptuUUIDPipeline()
                .getUUIDs(request, Settings.UUID.NON_BLOCKING_TIMEOUT)
                .whenComplete((uuids, throwable) -> {
                    if (throwable != null) {
                        consumer.accept(null, throwable);
                    } else {
                        for (final UUIDMapping uuid : uuids) {
                            result.add(uuid.getUuid());
                        }
                        consumer.accept(result, null);
                    }
                });
        }
    }

    /**
     * Get a list of names given a list of UUIDs.
     * - Uses the format {@link Captions#PLOT_USER_LIST} for the returned string
     *
     * @param uuids UUIDs
     * @return Name list
     */
    @Nonnull public static String getPlayerList(@Nonnull final Collection<UUID> uuids) {
        if (uuids.size() < 1) {
            return Captions.NONE.getTranslated();
        }

        final List<UUID> players = new LinkedList<>();
        final List<String> users = new LinkedList<>();
        for (final UUID uuid : uuids) {
            if (uuid == null) {
                users.add(Captions.NONE.getTranslated());
            } else if (DBFunc.EVERYONE.equals(uuid)) {
                users.add(Captions.EVERYONE.getTranslated());
            } else if (DBFunc.SERVER.equals(uuid)) {
                users.add(Captions.SERVER.getTranslated());
            } else {
                players.add(uuid);
            }
        }

        try {
            for (final UUIDMapping mapping : PlotSquared.get().getImpromptuUUIDPipeline()
                .getNames(players).get(Settings.UUID.BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS)) {
                users.add(mapping.getUsername());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        String c = Captions.PLOT_USER_LIST.getTranslated();
        StringBuilder list = new StringBuilder();
        for (int x = 0; x < users.size(); x++) {
            if (x + 1 == uuids.size()) {
                list.append(c.replace("%user%", users.get(x)).replace(",", ""));
            } else {
                list.append(c.replace("%user%", users.get(x)));
            }
        }
        return list.toString();
    }

    /**
     * Get the name from a UUID.
     *
     * @param owner Owner UUID
     * @return The player's name, None, Everyone or Unknown
     */
    @Nonnull public static String getName(@Nullable final UUID owner) {
        return getName(owner, true);
    }

    /**
     * Get the name from a UUID.
     *
     * @param owner    Owner UUID
     * @param blocking Whether or not the operation can be blocking
     * @return The player's name, None, Everyone or Unknown
     */
    @Nonnull public static String getName(@Nullable final UUID owner, final boolean blocking) {
        if (owner == null) {
            return Captions.NONE.getTranslated();
        }
        if (owner.equals(DBFunc.EVERYONE)) {
            return Captions.EVERYONE.getTranslated();
        }
        if (owner.equals(DBFunc.SERVER)) {
            return Captions.SERVER.getTranslated();
        }
        final String name;
        if (blocking) {
            name = PlotSquared.get().getImpromptuUUIDPipeline()
                .getSingle(owner, Settings.UUID.BLOCKING_TIMEOUT);
        } else {
            final UUIDMapping uuidMapping =
                PlotSquared.get().getImpromptuUUIDPipeline().getImmediately(owner);
            if (uuidMapping != null) {
                name = uuidMapping.getUsername();
            } else {
                name = null;
            }
        }
        if (name == null) {
            return Captions.UNKNOWN.getTranslated();
        }
        return name;
    }

    /**
     * Remove a player from the player map
     *
     * @param plotPlayer Player to remove
     */
    public void removePlayer(@Nonnull final PlotPlayer<?> plotPlayer) {
        synchronized (playerLock) {
            this.playerMap.remove(plotPlayer.getUUID());
        }
    }

    /**
     * Remove a player from the player map
     *
     * @param uuid Player to remove
     */
    public void removePlayer(@Nonnull final UUID uuid) {
        synchronized (playerLock) {
            this.playerMap.remove(uuid);
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
     * that the caller actually knows that the player exists and is online.
     * <p>
     * The method will throw an exception if there is no such
     * player online.
     *
     * @param object Platform player object
     * @return Player object
     */
    @Nonnull public abstract P getPlayer(@Nonnull final T object);

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
    @Nonnull public P getPlayer(@Nonnull final UUID uuid) {
        synchronized (playerLock) {
            P player = this.playerMap.get(uuid);
            if (player == null) {
                player = createPlayer(uuid);
                this.playerMap.put(uuid, player);
            }
            return player;
        }
    }

    @Nonnull public abstract P createPlayer(@Nonnull final UUID uuid);

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
    @Nullable public abstract OfflinePlotPlayer getOfflinePlayer(@Nonnull final String username);

    /**
     * Get all online players
     *
     * @return Unmodifiable collection of players
     */
    public Collection<P> getPlayers() {
        return Collections.unmodifiableCollection(new ArrayList<>(this.playerMap.values()));
    }


    public static final class NoSuchPlayerException extends IllegalArgumentException {

        public NoSuchPlayerException(@Nonnull final UUID uuid) {
            super(String.format("There is no online player with UUID '%s'", uuid.toString()));
        }

        @Override public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

}
