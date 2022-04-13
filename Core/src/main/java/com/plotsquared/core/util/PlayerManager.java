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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.uuid.UUIDMapping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private final Map<UUID, P> playerMap = new HashMap<>();
    private final Object playerLock = new Object();

    public static void getUUIDsFromString(
            final @NonNull String list,
            final @NonNull BiConsumer<Collection<UUID>, Throwable> consumer
    ) {
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
     * - Uses the format {@link TranslatableCaption#of(String)} of "info.plot_user_list" for the returned string
     *
     * @param uuids        UUIDs
     * @param localeHolder the localeHolder to localize the component for
     * @return Component of name list
     */
    public static @NonNull Component getPlayerList(final @NonNull Collection<UUID> uuids, LocaleHolder localeHolder) {
        if (uuids.isEmpty()) {
            return MINI_MESSAGE.parse(TranslatableCaption.of("info.none").getComponent(localeHolder));
        }

        final List<UUID> players = new LinkedList<>();
        final List<String> users = new LinkedList<>();
        for (final UUID uuid : uuids) {
            if (uuid == null) {
                users.add(MINI_MESSAGE.stripTokens(TranslatableCaption.of("info.none").getComponent(localeHolder)));
            } else if (DBFunc.EVERYONE.equals(uuid)) {
                users.add(MINI_MESSAGE.stripTokens(TranslatableCaption.of("info.everyone").getComponent(localeHolder)));
            } else if (DBFunc.SERVER.equals(uuid)) {
                users.add(MINI_MESSAGE.stripTokens(TranslatableCaption.of("info.console").getComponent(localeHolder)));
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

        String c = TranslatableCaption.of("info.plot_user_list").getComponent(ConsolePlayer.getConsole());
        TextComponent.Builder list = Component.text();
        for (int x = 0; x < users.size(); x++) {
            if (x + 1 == uuids.size()) {
                list.append(MINI_MESSAGE.parse(c, Template.of("user", users.get(x))));
            } else {
                list.append(MINI_MESSAGE.parse(c + ", ", Template.of("user", users.get(x))));
            }
        }
        return list.asComponent();
    }

    /**
     * Get the name from a UUID.
     *
     * @param owner Owner UUID
     * @return The player's name, None, Everyone or Unknown
     * @deprecated Use {@link #resolveName(UUID)}
     */
    @Deprecated(forRemoval = true, since = "6.4.0")
    public static @NonNull String getName(final @Nullable UUID owner) {
        return getName(owner, true);
    }

    /**
     * Get the name from a UUID.
     *
     * @param owner    Owner UUID
     * @param blocking Whether or not the operation can be blocking
     * @return The player's name, None, Everyone or Unknown
     * @deprecated Use {@link #resolveName(UUID, boolean)}
     */
    @Deprecated(forRemoval = true, since = "6.4.0")
    public static @NonNull String getName(final @Nullable UUID owner, final boolean blocking) {
        if (owner == null) {
            TranslatableCaption.of("info.none");
        }
        if (owner.equals(DBFunc.EVERYONE)) {
            TranslatableCaption.of("info.everyone");
        }
        if (owner.equals(DBFunc.SERVER)) {
            TranslatableCaption.of("info.server");
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
            TranslatableCaption.of("info.unknown");
        }
        return name;
    }

    /**
     * Attempts to resolve the username by an uuid
     * <p>
     * <b>Note:</b> blocks the thread until the name was resolved or failed
     *
     * @param owner The UUID of the owner
     * @return A caption containing either the name, {@code None}, {@code Everyone} or {@code Unknown}
     * @see #resolveName(UUID, boolean)
     * @since 6.4.0
     */
    public static @NonNull Caption resolveName(final @Nullable UUID owner) {
        return resolveName(owner, true);
    }

    /**
     * Attempts to resolve the username by an uuid
     *
     * @param owner    The UUID of the owner
     * @param blocking If the operation should block the current thread for {@link Settings.UUID#BLOCKING_TIMEOUT} milliseconds
     * @return A caption containing either the name, {@code None}, {@code Everyone} or {@code Unknown}
     * @since 6.4.0
     */
    public static @NonNull Caption resolveName(final @Nullable UUID owner, final boolean blocking) {
        if (owner == null) {
            return TranslatableCaption.of("info.none");
        }
        if (owner.equals(DBFunc.EVERYONE)) {
            return TranslatableCaption.of("info.everyone");
        }
        if (owner.equals(DBFunc.SERVER)) {
            return TranslatableCaption.of("info.server");
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
            return TranslatableCaption.of("info.unknown");
        }
        return StaticCaption.of(name);
    }

    /**
     * Remove a player from the player map
     *
     * @param plotPlayer Player to remove
     */
    public void removePlayer(final @NonNull PlotPlayer<?> plotPlayer) {
        synchronized (playerLock) {
            this.playerMap.remove(plotPlayer.getUUID());
        }
    }

    /**
     * Remove a player from the player map
     *
     * @param uuid Player to remove
     */
    public void removePlayer(final @NonNull UUID uuid) {
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
    public @Nullable P getPlayerIfExists(final @Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return this.playerMap.get(uuid);
    }

    public @Nullable P getPlayerIfExists(final @Nullable String name) {
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
    public @NonNull
    abstract P getPlayer(final @NonNull T object);

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
    public @NonNull P getPlayer(final @NonNull UUID uuid) {
        synchronized (playerLock) {
            P player = this.playerMap.get(uuid);
            if (player == null) {
                player = createPlayer(uuid);
                this.playerMap.put(uuid, player);
            }
            return player;
        }
    }

    public @NonNull
    abstract P createPlayer(final @NonNull UUID uuid);

    /**
     * Get an an offline player object from the player's UUID
     *
     * @param uuid Player UUID
     * @return Offline player object
     */
    public @Nullable
    abstract OfflinePlotPlayer getOfflinePlayer(final @Nullable UUID uuid);

    /**
     * Get an offline player object from the player's username
     *
     * @param username Player name
     * @return Offline player object
     */
    public @Nullable
    abstract OfflinePlotPlayer getOfflinePlayer(final @NonNull String username);

    /**
     * Get all online players
     *
     * @return Unmodifiable collection of players
     */
    public Collection<P> getPlayers() {
        return Collections.unmodifiableCollection(new ArrayList<>(this.playerMap.values()));
    }


    public static final class NoSuchPlayerException extends IllegalArgumentException {

        public NoSuchPlayerException(final @NonNull UUID uuid) {
            super(String.format("There is no online player with UUID '%s'", uuid));
        }

    }

}
