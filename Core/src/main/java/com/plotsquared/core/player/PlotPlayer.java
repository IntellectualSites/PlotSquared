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
package com.plotsquared.core.player;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.collection.ByteArrayUtilities;
import com.plotsquared.core.command.CommandCaller;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.NullPermissionProfile;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.PermissionProfile;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.synchronization.LockRepository;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.item.ItemType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The abstract class supporting {@code BukkitPlayer} and {@code SpongePlayer}.
 */
public abstract class PlotPlayer<P> implements CommandCaller, OfflinePlotPlayer, LocaleHolder {

    private static final String NON_EXISTENT_CAPTION = "<red>PlotSquared does not recognize the caption: ";

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PlotPlayer.class.getSimpleName());

    // Used to track debug mode
    private static final Set<PlotPlayer<?>> debugModeEnabled =
            Collections.synchronizedSet(new HashSet<>());

    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, PlotPlayerConverter> converters = new HashMap<>();
    private final LockRepository lockRepository = new LockRepository();
    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final PermissionHandler permissionHandler;
    private Map<String, byte[]> metaMap = new HashMap<>();
    /**
     * The metadata map.
     */
    private ConcurrentHashMap<String, Object> meta;
    private int hash;
    private Locale locale;
    // Delayed initialisation
    private PermissionProfile permissionProfile;

    public PlotPlayer(
            final @NonNull PlotAreaManager plotAreaManager, final @NonNull EventDispatcher eventDispatcher,
            final @NonNull PermissionHandler permissionHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.permissionHandler = permissionHandler;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> PlotPlayer<T> from(final @NonNull T object) {
        // fast path
        if (converters.containsKey(object.getClass())) {
            return converters.get(object.getClass()).convert(object);
        }
        // slow path, meant to only run once per object#getClass instance
        Queue<Class<?>> toVisit = new ArrayDeque<>();
        toVisit.add(object.getClass());
        Class<?> current;
        while ((current = toVisit.poll()) != null) {
            PlotPlayerConverter converter = converters.get(current);
            if (converter != null) {
                if (current != object.getClass()) {
                    // register shortcut for this sub type to avoid further loops
                    converters.put(object.getClass(), converter);
                    LOGGER.info("Registered {} as with converter for {}", object.getClass(), current);
                }
                return converter.convert(object);
            }
            // no converter found yet
            if (current.getSuperclass() != null) {
                toVisit.add(current.getSuperclass()); // add super class if available
            }
            toVisit.addAll(Arrays.asList(current.getInterfaces())); // add interfaces
        }
        throw new IllegalArgumentException(String
                .format(
                        "There is no registered PlotPlayer converter for type %s",
                        object.getClass().getSimpleName()
                ));
    }

    public static <T> void registerConverter(
            final @NonNull Class<T> clazz,
            final PlotPlayerConverter<T> converter
    ) {
        converters.put(clazz, converter);
    }

    public static Collection<PlotPlayer<?>> getDebugModePlayers() {
        return Collections.unmodifiableCollection(debugModeEnabled);
    }

    public static Collection<PlotPlayer<?>> getDebugModePlayersInPlot(final @NonNull Plot plot) {
        if (debugModeEnabled.isEmpty()) {
            return Collections.emptyList();
        }
        final Collection<PlotPlayer<?>> players = new LinkedList<>();
        for (final PlotPlayer<?> player : debugModeEnabled) {
            if (player.getCurrentPlot().equals(plot)) {
                players.add(player);
            }
        }
        return players;
    }

    protected void setupPermissionProfile() {
        this.permissionProfile = permissionHandler.getPermissionProfile(this).orElse(
                NullPermissionProfile.INSTANCE);
    }

    @Override
    public final boolean hasPermission(
            final @Nullable String world,
            final @NonNull String permission
    ) {
        return this.permissionProfile.hasPermission(world, permission);
    }

    @Override
    public final boolean hasKeyedPermission(
            final @Nullable String world,
            final @NonNull String permission,
            final @NonNull String key
    ) {
        return this.permissionProfile.hasKeyedPermission(world, permission, key);
    }

    @Override
    public final boolean hasPermission(@NonNull String permission, boolean notify) {
        if (!hasPermission(permission)) {
            if (notify) {
                sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        TagResolver.resolver("node", Tag.inserting(Component.text(permission)))
                );
            }
            return false;
        }
        return true;
    }

    public abstract Actor toActor();

    public abstract P getPlatformPlayer();

    /**
     * Set some session only metadata for this player.
     *
     * @param key
     * @param value
     */
    void setMeta(String key, Object value) {
        if (value == null) {
            deleteMeta(key);
        } else {
            if (this.meta == null) {
                this.meta = new ConcurrentHashMap<>();
            }
            this.meta.put(key, value);
        }
    }

    /**
     * Get the session metadata for a key.
     *
     * @param key the name of the metadata key
     * @param <T> the object type to return
     * @return the value assigned to the key or null if it does not exist
     */
    @SuppressWarnings("unchecked")
    <T> T getMeta(String key) {
        if (this.meta != null) {
            return (T) this.meta.get(key);
        }
        return null;
    }

    <T> T getMeta(String key, T defaultValue) {
        T meta = getMeta(key);
        if (meta == null) {
            return defaultValue;
        }
        return meta;
    }

    public ConcurrentHashMap<String, Object> getMeta() {
        return meta;
    }

    /**
     * Delete the metadata for a key.
     * - metadata is session only
     * - deleting other plugin's metadata may cause issues
     *
     * @param key
     */
    Object deleteMeta(String key) {
        return this.meta == null ? null : this.meta.remove(key);
    }


    /**
     * Returns the name of the player.
     *
     * @return the name of the player
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Get this player's current plot.
     *
     * @return the plot the player is standing on or null if standing on a road or not in a {@link PlotArea}
     */
    public @Nullable Plot getCurrentPlot() {
        try (final MetaDataAccess<Plot> lastPlotAccess =
                     this.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
            if (lastPlotAccess.get().orElse(null) == null && !Settings.Enabled_Components.EVENTS) {
                return this.getLocation().getPlot();
            }
            return lastPlotAccess.get().orElse(null);
        }
    }

    /**
     * Get the total number of allowed plots
     *
     * @return number of allowed plots within the scope (globally, or in the player's current world as defined in the settings.yml)
     */
    public int getAllowedPlots() {
        final int calculatedLimit = hasPermissionRange("plots.plot", Settings.Limit.MAX_PLOTS);
        return this.eventDispatcher.callPlayerPlotLimit(this, calculatedLimit).limit();
    }

    /**
     * Get the number of plots this player owns.
     *
     * @return number of plots within the scope (globally, or in the player's current world as defined in the settings.yml)
     * @see #getPlotCount(String)
     * @see #getPlots()
     */
    public int getPlotCount() {
        if (!Settings.Limit.GLOBAL) {
            return getPlotCount(getContextualWorldName());
        }
        final AtomicInteger count = new AtomicInteger(0);
        final UUID uuid = getUUID();
        this.plotAreaManager.forEachPlotArea(value -> {
            if (!Settings.Done.COUNTS_TOWARDS_LIMIT) {
                for (Plot plot : value.getPlotsAbs(uuid)) {
                    if (!DoneFlag.isDone(plot)) {
                        count.incrementAndGet();
                    }
                }
            } else {
                count.addAndGet(value.getPlotsAbs(uuid).size());
            }
        });
        return count.get();
    }

    public int getClusterCount() {
        if (!Settings.Limit.GLOBAL) {
            return getClusterCount(getContextualWorldName());
        }
        final AtomicInteger count = new AtomicInteger(0);
        this.plotAreaManager.forEachPlotArea(value -> {
            for (PlotCluster cluster : value.getClusters()) {
                if (cluster.isOwner(getUUID())) {
                    count.incrementAndGet();
                }
            }
        });
        return count.get();
    }

    /**
     * {@return the world name at the player's contextual position}
     * The contextual position can be affected when using a command with
     * an explicit plot override, e.g., `/plot &ltid&gt info`.
     */
    private @NonNull String getContextualWorldName() {
        Plot current = getCurrentPlot();
        if (current != null) {
            return current.getWorldName();
        }
        return getLocation().getWorldName();
    }

    /**
     * {@return the plot area at the player's contextual position}
     * The contextual position can be affected when using a command with
     * an explicit plot override, e.g., `/plot &ltid&gt info`.
     *
     * @since 7.5.9
     */
    public @Nullable PlotArea getContextualPlotArea() {
        Plot current = getCurrentPlot();
        if (current != null) {
            return current.getArea();
        }
        return getLocation().getPlotArea();
    }

    /**
     * Get the number of plots this player owns in the world.
     *
     * @param world the name of the plotworld to check.
     * @return plot count
     */
    public int getPlotCount(String world) {
        UUID uuid = getUUID();
        int count = 0;
        for (PlotArea area : this.plotAreaManager.getPlotAreasSet(world)) {
            if (!Settings.Done.COUNTS_TOWARDS_LIMIT) {
                count +=
                        area.getPlotsAbs(uuid).stream().filter(plot -> !DoneFlag.isDone(plot)).count();
            } else {
                count += area.getPlotsAbs(uuid).size();
            }
        }
        return count;
    }

    public int getClusterCount(String world) {
        int count = 0;
        for (PlotArea area : this.plotAreaManager.getPlotAreasSet(world)) {
            for (PlotCluster cluster : area.getClusters()) {
                if (cluster.isOwner(getUUID())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get a {@link Set} of plots owned by this player.
     *
     * <p>
     * Take a look at {@link PlotSquared} for more searching functions.
     * See {@link #getPlotCount()} for the number of plots.
     * </p>
     *
     * @return a {@link Set} of plots owned by the player
     */
    public Set<Plot> getPlots() {
        return PlotQuery.newQuery().ownedBy(this).asSet();
    }

    /**
     * Return the PlotArea this player is currently in, or null.
     *
     * @return Plot area the player is currently in, or {@code null}
     */
    public @Nullable PlotArea getPlotAreaAbs() {
        return this.plotAreaManager.getPlotArea(getLocation());
    }

    public PlotArea getApplicablePlotArea() {
        Plot plot = getCurrentPlot();
        if (plot == null) {
            return this.plotAreaManager.getApplicablePlotArea(getLocation());
        }
        return plot.getArea();
    }

    @Override
    public @NonNull RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
    }

    /**
     * Get this player's last recorded location or null if they don't any plot relevant location.
     *
     * @return The location
     */
    public @NonNull Location getLocation() {
        Location location = getMeta("location");
        if (location != null) {
            return location;
        }
        return getLocationFull();
    }

    /////////////// PLAYER META ///////////////

    ////////////// PARTIALLY IMPLEMENTED ///////////

    /**
     * Get this player's full location (including yaw/pitch)
     *
     * @return location
     */
    public abstract Location getLocationFull();

    ////////////////////////////////////////////////

    /**
     * Get this player's UUID.
     * <p>=== !IMPORTANT ===</p>
     * The UUID is dependent on the mode chosen in the settings.yml and may not be the same as Bukkit has
     * (especially if using an old version of Bukkit that does not support UUIDs)
     *
     * @return UUID
     */
    @Override
    public @NonNull
    abstract UUID getUUID();

    public boolean canTeleport(final @NonNull Location location) {
        Preconditions.checkNotNull(location, "Specified location cannot be null");
        final Location current = getLocationFull();
        teleport(location);
        boolean result = getLocation().equals(location);
        teleport(current);
        return result;
    }

    /**
     * Teleport this player to a location.
     *
     * @param location the target location
     */
    public void teleport(Location location) {
        teleport(location, TeleportCause.PLUGIN);
    }

    /**
     * Teleport this player to a location.
     *
     * @param location the target location
     * @param cause    the cause of the teleport
     */
    public abstract void teleport(Location location, TeleportCause cause);

    /**
     * Kick this player to a location
     *
     * @param location the target location
     */
    public void plotkick(Location location) {
        setMeta("kick", true);
        teleport(location, TeleportCause.KICK);
        deleteMeta("kick");
    }

    /**
     * Set this compass target.
     *
     * @param location the target location
     */
    public abstract void setCompassTarget(Location location);

    /**
     * Set player data that will persist restarts.
     * - Please note that this is not intended to store large values
     * - For session only data use meta
     *
     * @param key metadata key
     */
    public void setAttribute(String key) {
        setPersistentMeta("attrib_" + key, new byte[]{(byte) 1});
    }

    /**
     * Retrieves the attribute of this player.
     *
     * @param key metadata key
     * @return the attribute will be either {@code true} or {@code false}
     */
    public boolean getAttribute(String key) {
        if (!hasPersistentMeta("attrib_" + key)) {
            return false;
        }
        return getPersistentMeta("attrib_" + key)[0] == 1;
    }

    /**
     * Remove an attribute from a player.
     *
     * @param key metadata key
     */
    public void removeAttribute(String key) {
        removePersistentMeta("attrib_" + key);
    }

    /**
     * Sets the local weather for this Player.
     *
     * @param weather the weather visible to the player
     */
    public abstract void setWeather(@NonNull PlotWeather weather);

    /**
     * Get this player's gamemode.
     *
     * @return the gamemode of the player.
     */
    public abstract @NonNull GameMode getGameMode();

    /**
     * Set this player's gameMode.
     *
     * @param gameMode the gamemode to set
     */
    public abstract void setGameMode(@NonNull GameMode gameMode);

    /**
     * Set this player's local time (ticks).
     *
     * @param time the time visible to the player
     */
    public abstract void setTime(long time);

    /**
     * Determines whether or not the player can fly.
     *
     * @return {@code true} if the player is allowed to fly
     */
    public abstract boolean getFlight();

    /**
     * Sets whether or not this player can fly.
     *
     * @param fly {@code true} if the player can fly, otherwise {@code false}
     */
    public abstract void setFlight(boolean fly);

    /**
     * Play music at a location for this player.
     *
     * @param location where to play the music
     * @param id       the record item id
     */
    public abstract void playMusic(@NonNull Location location, @NonNull ItemType id);

    /**
     * Check if this player is banned.
     *
     * @return {@code true} if the player is banned, {@code false} otherwise.
     */
    public abstract boolean isBanned();

    /**
     * Kick this player from the game.
     *
     * @param message the reason for the kick
     */
    public abstract void kick(String message);

    public void refreshDebug() {
        final boolean debug = this.getAttribute("debug");
        if (debug && !debugModeEnabled.contains(this)) {
            debugModeEnabled.add(this);
        } else if (!debug) {
            debugModeEnabled.remove(this);
        }
    }

    /**
     * Called when this player quits.
     */
    public void unregister() {
        Plot plot = getCurrentPlot();
        if (plot != null && Settings.Enabled_Components.PERSISTENT_META && plot
                .getArea() instanceof SinglePlotArea) {
            PlotId id = plot.getId();
            int x = id.getX();
            int z = id.getY();
            ByteBuffer buffer = ByteBuffer.allocate(14);
            buffer.putShort((short) x);
            buffer.putShort((short) z);
            Location location = getLocation();
            buffer.putInt(location.getX());
            buffer.putShort((short) location.getY());
            buffer.putInt(location.getZ());
            setPersistentMeta("quitLocV2", buffer.array());
        } else if (hasPersistentMeta("quitLocV2")) {
            removePersistentMeta("quitLocV2");
        }
        if (plot != null) {
            this.eventDispatcher.callLeave(this, plot);
        }
        if (Settings.Enabled_Components.BAN_DELETER && isBanned()) {
            for (Plot owned : getPlots()) {
                owned.getPlotModificationManager().deletePlot(null, null);
                LOGGER.info("Plot {} was deleted + cleared due to {} getting banned", owned.getId(), getName());
            }
        }
        if (PlotSquared.platform().expireManager() != null) {
            PlotSquared.platform().expireManager().storeDate(getUUID(), System.currentTimeMillis());
        }
        PlotSquared.platform().playerManager().removePlayer(this);
        PlotSquared.platform().unregister(this);

        debugModeEnabled.remove(this);
    }

    /**
     * Get the amount of clusters this player owns in the specific world.
     *
     * @param world world
     * @return number of clusters owned
     */
    public int getPlayerClusterCount(String world) {
        return PlotSquared.get().getClusters(world).stream()
                .filter(cluster -> getUUID().equals(cluster.owner)).mapToInt(PlotCluster::getArea)
                .sum();
    }

    /**
     * Get the amount of clusters this player owns.
     *
     * @return the number of clusters this player owns
     */
    public int getPlayerClusterCount() {
        final AtomicInteger count = new AtomicInteger();
        this.plotAreaManager.forEachPlotArea(value -> count.addAndGet(value.getClusters().size()));
        return count.get();
    }

    /**
     * Return a {@code Set} of all plots this player owns in a certain world.
     *
     * @param world the world to retrieve plots from
     * @return a {@code Set} of plots this player owns in the provided world
     */
    public Set<Plot> getPlots(String world) {
        return PlotQuery.newQuery().inWorld(world).ownedBy(getUUID()).asSet();
    }

    public void populatePersistentMetaMap() {
        if (Settings.Enabled_Components.PERSISTENT_META) {
            DBFunc.getPersistentMeta(
                    getUUID(), new RunnableVal<>() {
                        @Override
                        public void run(Map<String, byte[]> value) {
                            try {
                                PlotPlayer.this.metaMap = value;
                                if (value.isEmpty()) {
                                    return;
                                }

                                if (PlotPlayer.this.getAttribute("debug")) {
                                    debugModeEnabled.add(PlotPlayer.this);
                                }

                                if (!Settings.Teleport.ON_LOGIN) {
                                    return;
                                }
                                PlotAreaManager manager = PlotPlayer.this.plotAreaManager;

                                if (!(manager instanceof SinglePlotAreaManager)) {
                                    return;
                                }
                                PlotArea area = ((SinglePlotAreaManager) manager).getArea();
                                boolean V2 = false;
                                byte[] arr = PlotPlayer.this.getPersistentMeta("quitLoc");
                                if (arr == null) {
                                    arr = PlotPlayer.this.getPersistentMeta("quitLocV2");
                                    if (arr == null) {
                                        return;
                                    }
                                    V2 = true;
                                    removePersistentMeta("quitLocV2");
                                } else {
                                    removePersistentMeta("quitLoc");
                                }

                                if (!getMeta("teleportOnLogin", true)) {
                                    return;
                                }
                                ByteBuffer quitWorld = ByteBuffer.wrap(arr);
                                final int plotX = quitWorld.getShort();
                                final int plotZ = quitWorld.getShort();
                                PlotId id = PlotId.of(plotX, plotZ);
                                int x = quitWorld.getInt();
                                int y = V2 ? quitWorld.getShort() : (quitWorld.get() & 0xFF);
                                int z = quitWorld.getInt();
                                Plot plot = area.getOwnedPlot(id);

                                if (plot == null) {
                                    return;
                                }

                                final Location location = Location.at(plot.getWorldName(), x, y, z);
                                if (plot.isLoaded()) {
                                    TaskManager.runTask(() -> {
                                        if (getMeta("teleportOnLogin", true)) {
                                            teleport(location, TeleportCause.LOGIN);
                                            sendMessage(
                                                    TranslatableCaption.of("teleport.teleported_to_plot"));
                                        }
                                    });
                                } else if (!PlotSquared.get().isMainThread(Thread.currentThread())) {
                                    if (getMeta("teleportOnLogin", true)) {
                                        plot.teleportPlayer(
                                                PlotPlayer.this,
                                                result -> TaskManager.runTask(() -> {
                                                    if (getMeta("teleportOnLogin", true)) {
                                                        if (plot.isLoaded()) {
                                                            teleport(location, TeleportCause.LOGIN);
                                                            sendMessage(TranslatableCaption
                                                                    .of("teleport.teleported_to_plot"));
                                                        }
                                                    }
                                                })
                                        );
                                    }
                                }
                            } catch (Throwable e) {
                                LOGGER.error("Error populating persistent meta for player {}", PlotPlayer.this.getName(), e);
                            }
                        }
                    }
            );
        }
    }

    byte[] getPersistentMeta(String key) {
        return this.metaMap.get(key);
    }

    Object removePersistentMeta(String key) {
        final Object old = this.metaMap.remove(key);
        if (Settings.Enabled_Components.PERSISTENT_META) {
            DBFunc.removePersistentMeta(getUUID(), key);
        }
        return old;
    }

    /**
     * Access keyed persistent meta data for this player. This returns a meta data
     * access instance, that MUST be closed. It is meant to be used with try-with-resources,
     * like such:
     * <pre>{@code
     * try (final MetaDataAccess<Integer> access = player.accessPersistentMetaData(PlayerMetaKeys.GRANTS)) {
     *     int grants = access.get();
     *     access.set(grants + 1);
     * }
     * }</pre>
     *
     * @param key Meta data key
     * @param <T> Meta data type
     * @return Meta data access. MUST be closed after being used
     */
    public @NonNull <T> MetaDataAccess<T> accessPersistentMetaData(final @NonNull MetaDataKey<T> key) {
        return new PersistentMetaDataAccess<>(this, key, this.lockRepository.lock(key.getLockKey()));
    }

    /**
     * Access keyed temporary meta data for this player. This returns a meta data
     * access instance, that MUST be closed. It is meant to be used with try-with-resources,
     * like such:
     * <pre>{@code
     * try (final MetaDataAccess<Integer> access = player.accessTemporaryMetaData(PlayerMetaKeys.GRANTS)) {
     *     int grants = access.get();
     *     access.set(grants + 1);
     * }
     * }</pre>
     *
     * @param key Meta data key
     * @param <T> Meta data type
     * @return Meta data access. MUST be closed after being used
     */
    public @NonNull <T> MetaDataAccess<T> accessTemporaryMetaData(final @NonNull MetaDataKey<T> key) {
        return new TemporaryMetaDataAccess<>(this, key, this.lockRepository.lock(key.getLockKey()));
    }

    <T> void setPersistentMeta(
            final @NonNull MetaDataKey<T> key,
            final @NonNull T value
    ) {
        if (key.getType().getRawType().equals(Integer.class)) {
            this.setPersistentMeta(key.toString(), Ints.toByteArray((int) (Object) value));
        } else if (key.getType().getRawType().equals(Boolean.class)) {
            this.setPersistentMeta(key.toString(), ByteArrayUtilities.booleanToBytes((boolean) (Object) value));
        } else {
            throw new IllegalArgumentException(String.format("Unknown meta data type '%s'", key.getType()));
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    <T> T getPersistentMeta(final @NonNull MetaDataKey<T> key) {
        final byte[] value = this.getPersistentMeta(key.toString());
        if (value == null) {
            return null;
        }
        final Object returnValue;
        if (key.getType().getRawType().equals(Integer.class)) {
            returnValue = Ints.fromByteArray(value);
        } else if (key.getType().getRawType().equals(Boolean.class)) {
            returnValue = ByteArrayUtilities.bytesToBoolean(value);
        } else {
            throw new IllegalArgumentException(String.format("Unknown meta data type '%s'", key.getType()));
        }
        return (T) returnValue;
    }

    void setPersistentMeta(String key, byte[] value) {
        boolean delete = hasPersistentMeta(key);
        this.metaMap.put(key, value);
        if (Settings.Enabled_Components.PERSISTENT_META) {
            DBFunc.addPersistentMeta(getUUID(), key, value, delete);
        }
    }

    /**
     * Send a title to the player that fades in, in 10 ticks, stays for 50 ticks and fades
     * out in 20 ticks
     *
     * @param title        Title text
     * @param subtitle     Subtitle text
     * @param replacements Variable replacements
     */
    public void sendTitle(
            final @NonNull Caption title, final @NonNull Caption subtitle,
            final @NonNull TagResolver... replacements
    ) {
        sendTitle(
                title,
                subtitle,
                Settings.Titles.TITLES_FADE_IN,
                Settings.Titles.TITLES_STAY,
                Settings.Titles.TITLES_FADE_OUT,
                replacements
        );
    }

    /**
     * Send a title to the player
     *
     * @param title        Title
     * @param subtitle     Subtitle
     * @param fadeIn       Fade in time (in ticks)
     * @param stay         The title stays for (in ticks)
     * @param fadeOut      Fade out time (in ticks)
     * @param replacements Variable replacements
     */
    public void sendTitle(
            final @NonNull Caption title, final @NonNull Caption subtitle,
            final int fadeIn, final int stay, final int fadeOut,
            final @NonNull TagResolver... replacements
    ) {
        final Component titleComponent = MiniMessage.miniMessage().deserialize(title.getComponent(this), replacements);
        final Component subtitleComponent =
                MiniMessage.miniMessage().deserialize(subtitle.getComponent(this), replacements);
        final Title.Times times = Title.Times.times(
                Duration.of(Settings.Titles.TITLES_FADE_IN * 50L, ChronoUnit.MILLIS),
                Duration.of(Settings.Titles.TITLES_STAY * 50L, ChronoUnit.MILLIS),
                Duration.of(Settings.Titles.TITLES_FADE_OUT * 50L, ChronoUnit.MILLIS)
        );
        getAudience().showTitle(Title
                .title(titleComponent, subtitleComponent, times));
    }

    /**
     * Method designed to send an ActionBar to a player.
     *
     * @param caption      Caption
     * @param replacements Variable replacements
     */
    public void sendActionBar(
            final @NonNull Caption caption,
            final @NonNull TagResolver... replacements
    ) {
        String message;
        try {
            message = caption.getComponent(this);
        } catch (final CaptionMap.NoSuchCaptionException exception) {
            // This sends feedback to the player
            message = NON_EXISTENT_CAPTION + ((TranslatableCaption) caption).getKey();
            // And this also prints it to the console
            exception.printStackTrace();
        }
        if (message.isEmpty()) {
            return;
        }
        // Replace placeholders, etc
        message = CaptionUtility.format(this, message)
                .replace('\u2010', '%').replace('\u2020', '&').replace('\u2030', '&')
                .replace("<prefix>", TranslatableCaption.of("core.prefix").getComponent(this));


        final Component component = MiniMessage.miniMessage().deserialize(message, replacements);
        getAudience().sendActionBar(component);
    }

    @Override
    public void sendMessage(
            final @NonNull Caption caption,
            final @NonNull TagResolver... replacements
    ) {
        String message;
        try {
            message = caption.getComponent(this);
        } catch (final CaptionMap.NoSuchCaptionException exception) {
            // This sends feedback to the player
            message = NON_EXISTENT_CAPTION + ((TranslatableCaption) caption).getKey();
            // And this also prints it to the console
            exception.printStackTrace();
        }
        if (message.isEmpty()) {
            return;
        }
        // Replace placeholders, etc
        message = CaptionUtility.format(this, message)
                .replace('\u2010', '%').replace('\u2020', '&').replace('\u2030', '&')
                .replace("<prefix>", TranslatableCaption.of("core.prefix").getComponent(this));
        // Parse the message
        final Component component = MiniMessage.miniMessage().deserialize(message, replacements);
        if (!Objects.equal(component, this.getMeta("lastMessage"))
                || System.currentTimeMillis() - this.<Long>getMeta("lastMessageTime") > 5000) {
            setMeta("lastMessage", component);
            setMeta("lastMessageTime", System.currentTimeMillis());
            getAudience().sendMessage(component);
        }
    }

    /**
     * Sends a message to the command caller, when the future is resolved
     *
     * @param caption          Caption to send
     * @param asyncReplacement Async variable replacement
     * @return A Future to be resolved, after the message was sent
     * @since 7.1.0
     */
    public final CompletableFuture<Void> sendMessage(
            @NonNull Caption caption,
            CompletableFuture<@NonNull TagResolver> asyncReplacement
    ) {
        return sendMessage(caption, new CompletableFuture[]{asyncReplacement});
    }

    /**
     * Sends a message to the command caller, when all futures are resolved
     *
     * @param caption           Caption to send
     * @param asyncReplacements Async variable replacements
     * @param replacements      Sync variable replacements
     * @return A Future to be resolved, after the message was sent
     * @since 7.1.0
     */
    public final CompletableFuture<Void> sendMessage(
            @NonNull Caption caption,
            CompletableFuture<@NonNull TagResolver>[] asyncReplacements,
            @NonNull TagResolver... replacements
    ) {
        return CompletableFuture.allOf(asyncReplacements).whenComplete((unused, throwable) -> {
            Set<TagResolver> resolvers = new HashSet<>(Arrays.asList(replacements));
            if (throwable != null) {
                sendMessage(
                        TranslatableCaption.of("errors.error"),
                        TagResolver.resolver(
                                "value", Tag.inserting(
                                        Component.text("Failed to resolve asynchronous caption replacements")
                                )
                        )
                );
                LOGGER.error("Failed to resolve asynchronous tagresolver(s) for " + caption, throwable);
            } else {
                for (final CompletableFuture<TagResolver> asyncReplacement : asyncReplacements) {
                    resolvers.add(asyncReplacement.join());
                }
            }
            sendMessage(caption, resolvers.toArray(TagResolver[]::new));
        });
    }

    // Redefine from PermissionHolder as it's required from CommandCaller
    @Override
    public boolean hasPermission(@NonNull String permission) {
        return hasPermission(null, permission);
    }

    boolean hasPersistentMeta(String key) {
        return this.metaMap.containsKey(key);
    }

    /**
     * Check if the player is able to see the other player.
     * This does not mean that the other player is in line of sight of the player,
     * but rather that the player is permitted to see the other player.
     *
     * @param other Other player
     * @return {@code true} if the player is able to see the other player, {@code false} if not
     */
    public abstract boolean canSee(PlotPlayer<?> other);

    public abstract void stopSpectating();

    public boolean hasDebugMode() {
        return this.getAttribute("debug");
    }

    @NonNull
    @Override
    public Locale getLocale() {
        if (this.locale == null) {
            this.locale = Locale.forLanguageTag(Settings.Enabled_Components.DEFAULT_LOCALE);
        }
        return this.locale;
    }

    @Override
    public void setLocale(final @NonNull Locale locale) {
        if (!PlotSquared.get().getCaptionMap(TranslatableCaption.DEFAULT_NAMESPACE).supportsLocale(locale)) {
            this.locale = Locale.forLanguageTag(Settings.Enabled_Components.DEFAULT_LOCALE);
        } else {
            this.locale = locale;
        }
    }

    @Override
    public int hashCode() {
        if (this.hash == 0 || this.hash == 485) {
            this.hash = 485 + this.getUUID().hashCode();
        }
        return this.hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof final PlotPlayer<?> other)) {
            return false;
        }
        return this.getUUID().equals(other.getUUID());
    }

    /**
     * Get the {@link Audience} that represents this plot player
     *
     * @return Player audience
     */
    public @NonNull
    abstract Audience getAudience();

    /**
     * Get this player's {@link LockRepository}
     *
     * @return Lock repository instance
     */
    public @NonNull LockRepository getLockRepository() {
        return this.lockRepository;
    }

    /**
     * Removes any effects present of the given type.
     *
     * @param name the name of the type to remove
     * @since 6.10.0
     */
    public abstract void removeEffect(@NonNull String name);

    @FunctionalInterface
    public interface PlotPlayerConverter<BaseObject> {

        PlotPlayer<?> convert(BaseObject object);

    }

}
