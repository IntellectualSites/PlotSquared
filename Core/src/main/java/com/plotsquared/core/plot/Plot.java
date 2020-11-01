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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.Like;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.PlotLoc;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.expiration.PlotAnalysis;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.InternalFlag;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.plot.flag.implementations.KeepFlag;
import com.plotsquared.core.plot.flag.implementations.ServerPlotFlag;
import com.plotsquared.core.plot.flag.types.DoubleFlag;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.TimeUtil;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.plotsquared.core.util.entity.EntityCategories.CAP_ANIMAL;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ENTITY;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MISC;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MOB;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MONSTER;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_VEHICLE;

/**
 * The plot class<br>
 * [IMPORTANT]
 * - Unclaimed plots will not have persistent information.
 * - Any information set/modified in an unclaimed object may not be reflected in other instances
 * - Using the `new` operator will create an unclaimed plot instance
 * - Use the methods from the PlotArea/PS/Location etc to get existing plots
 */
public class Plot {

    public static final int MAX_HEIGHT = 256;

    private static final Logger logger = LoggerFactory.getLogger("P2/" + Plot.class.getSimpleName());
    private static final DecimalFormat FLAG_DECIMAL_FORMAT = new DecimalFormat("0");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    static Set<Plot> connected_cache;
    static Set<CuboidRegion> regions_cache;
    static {
        FLAG_DECIMAL_FORMAT.setMaximumFractionDigits(340);
    }
    /**
     * Plot flag container
     */
    private final FlagContainer flagContainer = new FlagContainer(null);
    /**
     * Utility used to manage plot comments
     */
    private final PlotCommentContainer plotCommentContainer = new PlotCommentContainer(this);
    /**
     * Utility used to modify the plot
     */
    private final PlotModificationManager plotModificationManager = new PlotModificationManager(this);
    /**
     * Represents whatever the database manager needs it to: <br>
     * - A value of -1 usually indicates the plot will not be stored in the DB<br>
     * - A value of 0 usually indicates that the DB manager hasn't set a value<br>
     *
     * @deprecated magical
     */
    @Deprecated public int temp;
    @Nonnull private PlotId id;
    // These will be injected
    @Inject private EventDispatcher eventDispatcher;
    @Inject private PlotListener plotListener;
    @Inject private RegionManager regionManager;
    @Inject private WorldUtil worldUtil;
    @Inject private SchematicHandler schematicHandler;
    /**
     * plot owner
     * (Merged plots can have multiple owners)
     * Direct access is Deprecated: use getOwners()
     *
     * @deprecated
     */
    private UUID owner;
    /**
     * Plot creation timestamp (not accurate if the plot was created before this was implemented)<br>
     * - Milliseconds since the epoch<br>
     */
    private long timestamp;
    /**
     * List of trusted (with plot permissions).
     */
    HashSet<UUID> trusted;
    /**
     * List of members users (with plot permissions).
     */
    HashSet<UUID> members;
    /**
     * List of denied players.
     */
    HashSet<UUID> denied;
    /**
     * External settings class.
     * - Please favor the methods over direct access to this class<br>
     * - The methods are more likely to be left unchanged from version changes<br>
     */
    PlotSettings settings;
    private PlotArea area;
    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     */
    private ConcurrentHashMap<String, Object> meta;
    /**
     * The cached origin plot.
     * - The origin plot is used for plot grouping and relational data
     */
    private Plot origin;

    /**
     * Constructor for a new plot.
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @param area  the PlotArea where the plot is located
     * @param id    the plot id
     * @param owner the plot owner
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(final PlotArea area, @Nonnull final PlotId id, final UUID owner) {
        this(area, id, owner, 0);
    }

    /**
     * Constructor for an unowned plot.
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @param area the PlotArea where the plot is located
     * @param id   the plot id
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(@Nonnull final PlotArea area, @Nonnull final PlotId id) {
        this(area, id, null, 0);
    }

    /**
     * Constructor for a temporary plot (use -1 for temp)<br>
     * The database will ignore any queries regarding temporary plots.
     * Please note that some bulk plot management functions may still affect temporary plots (TODO: fix this)
     *
     * @param area  the PlotArea where the plot is located
     * @param id    the plot id
     * @param owner the owner of the plot
     * @param temp  Represents whatever the database manager needs it to
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(final PlotArea area, @Nonnull final PlotId id, final UUID owner, final int temp) {
        this.area = area;
        this.id = id;
        this.owner = owner;
        this.temp = temp;
        this.flagContainer.setParentContainer(area.getFlagContainer());
        PlotSquared.platform().getInjector().injectMembers(this);
    }

    /**
     * Constructor for a saved plots (Used by the database manager when plots are fetched)
     *
     * @param id      the plot id
     * @param owner   the plot owner
     * @param trusted the plot trusted players
     * @param members the plot added players
     * @param denied  the plot denied players
     * @param alias the plot's alias
     * @param position plot home position
     * @param flags the plot's flags
     * @param area the plot's PlotArea
     * @param merged  an array giving merged plots
     * @param timestamp when the plot was created
     * @param temp value representing whatever DBManager needs to to. Do not touch tbh.
     * @see Plot#getPlot(Location) for existing plots
     */
    public Plot(@Nonnull PlotId id,
                UUID owner,
                HashSet<UUID> trusted,
                HashSet<UUID> members,
                HashSet<UUID> denied,
                String alias,
                BlockLoc position,
                Collection<PlotFlag<?, ?>> flags,
                PlotArea area,
                boolean[] merged,
                long timestamp,
                int temp) {
        this.id = id;
        this.area = area;
        this.owner = owner;
        this.settings = new PlotSettings();
        this.members = members;
        this.trusted = trusted;
        this.denied = denied;
        this.settings.setAlias(alias);
        this.settings.setPosition(position);
        this.settings.setMerged(merged);
        this.timestamp = timestamp;
        this.temp = temp;
        if (area != null) {
            this.flagContainer.setParentContainer(area.getFlagContainer());
            if (flags != null) {
                for (PlotFlag<?, ?> flag : flags) {
                    this.flagContainer.addFlag(flag);
                }
            }
        }
        PlotSquared.platform().getInjector().injectMembers(this);
    }

    /**
     * Get the plot from a string.
     *
     * @param player  Provides a context for what world to search in. Prefixing the term with 'world_name;' will override this context.
     * @param arg     The search term
     * @param message If a message should be sent to the player if a plot cannot be found
     * @return The plot if only 1 result is found, or null
     */
    @Nullable public static Plot getPlotFromString(@Nullable final PlotPlayer<?> player,
                                                   @Nullable final String arg,
                                                   final boolean message) {
        if (arg == null) {
            if (player == null) {
                if (message) {
                    logger.info("No plot area string was supplied");
                }
                return null;
            }
            return player.getCurrentPlot();
        }
        PlotArea area;
        if (player != null) {
            area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(arg);
            if (area == null) {
                area = player.getApplicablePlotArea();
            }
        } else {
            area = ConsolePlayer.getConsole().getApplicablePlotArea();
        }
        String[] split = arg.split("[;,]");
        PlotId id;
        if (split.length == 4) {
            area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(split[0] + ';' + split[1]);
            id = PlotId.fromString(split[2] + ';' + split[3]);
        } else if (split.length == 3) {
            area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(split[0]);
            id = PlotId.fromString(split[1] + ';' + split[2]);
        } else if (split.length == 2) {
            id = PlotId.fromString(arg);
        } else {
            Collection<Plot> plots;
            if (area == null) {
                plots = PlotQuery.newQuery().allPlots().asList();
            } else {
                plots = area.getPlots();
            }
            for (Plot p : plots) {
                String name = p.getAlias();
                if (!name.isEmpty() && name.equalsIgnoreCase(arg)) {
                    return p;
                }
            }
            if (message && player != null) {
                player.sendMessage(TranslatableCaption.of("invalid.not_valid_plot_id"));
            }
            return null;
        }
        if (area == null) {
            if (message && player != null) {
                player.sendMessage(TranslatableCaption.of("errors.invalid_plot_world"));
            }
            return null;
        }
        return area.getPlotAbs(id);
    }

    /**
     * Gets a plot from a string e.g. [area];[id]
     *
     * @param defaultArea if no area is specified
     * @param string      plot id/area + id
     * @return New or existing plot object
     */
    @Nullable public static Plot fromString(@Nullable final PlotArea defaultArea, @Nonnull final String string) {
        final String[] split = string.split("[;,]");
        if (split.length == 2) {
            if (defaultArea != null) {
                PlotId id = PlotId.fromString(split[0] + ';' + split[1]);
                return defaultArea.getPlotAbs(id);
            }
        } else if (split.length == 3) {
            PlotArea pa = PlotSquared.get().getPlotAreaManager().getPlotArea(split[0], null);
            if (pa != null) {
                PlotId id = PlotId.fromString(split[1] + ';' + split[2]);
                return pa.getPlotAbs(id);
            }
        } else if (split.length == 4) {
            PlotArea pa = PlotSquared.get().getPlotAreaManager().getPlotArea(split[0], split[1]);
            if (pa != null) {
                PlotId id = PlotId.fromString(split[1] + ';' + split[2]);
                return pa.getPlotAbs(id);
            }
        }
        return null;
    }

    /**
     * Return a new/cached plot object at a given location.
     *
     * @param location the location of the plot
     * @return plot at location or null
     * @see PlotPlayer#getCurrentPlot() if a player is expected here.
     */
    @Nullable public static Plot getPlot(@Nonnull final Location location) {
        final PlotArea pa = location.getPlotArea();
        if (pa != null) {
            return pa.getPlot(location);
        }
        return null;
    }

    @Nonnull static Location[] getCorners(@Nonnull final String world, @Nonnull final CuboidRegion region) {
        final BlockVector3 min = region.getMinimumPoint();
        final BlockVector3 max = region.getMaximumPoint();
        return new Location[] {Location.at(world, min), Location.at(world, max)};
    }

    /**
     * Get the owner of this exact plot, as it is
     * stored in the database.
     * <p>
     * If the plot is a mega-plot, then the method returns
     * the owner of this particular subplot.
     * <p>
     * Unlike {@link #getOwner()} this method does not
     * consider factors such as {@link com.plotsquared.core.plot.flag.implementations.ServerPlotFlag}
     * that could alter the de facto owner of the plot.
     *
     * @return The plot owner of this particular (sub-)plot
     * as stored in the database, if one exists. Else, null.
     */
    @Nullable public UUID getOwnerAbs() {
        return this.owner;
    }

    /**
     * Set the owner of this exact sub-plot. This does
     * not update the database.
     *
     * @param owner The new owner of this particular sub-plot.
     */
    public void setOwnerAbs(@Nullable final UUID owner) {
        this.owner = owner;
    }

    /**
     * Get the name of the world that the plot is in
     *
     * @return World name
     */
    @Nullable public String getWorldName() {
        return area.getWorldName();
    }

    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     *
     * @param key   metadata key
     * @param value metadata value
     */
    public void setMeta(@Nonnull final String key, @Nonnull final Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<>();
        }
        this.meta.put(key, value);
    }

    /**
     * Gets the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     *
     * @param key metadata key to get value for
     * @return Object value
     */
    @Nullable public Object getMeta(@Nonnull final String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }

    /**
     * Delete the metadata for a key<br>
     * - metadata is session only
     * - deleting other plugin's metadata may cause issues
     *
     * @param key key to delete
     */
    public void deleteMeta(@Nonnull final String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }

    /**
     * Gets the cluster this plot is associated with
     *
     * @return the PlotCluster object, or null
     */
    @Nullable public PlotCluster getCluster() {
        if (this.getArea() == null) {
            return null;
        }
        return this.getArea().getCluster(this.id);
    }

    /**
     * Efficiently get the players currently inside this plot<br>
     * - Will return an empty list if no players are in the plot<br>
     * - Remember, you can cast a PlotPlayer to it's respective implementation (BukkitPlayer, SpongePlayer) to obtain the player object
     *
     * @return list of PlotPlayer(s) or an empty list
     */
    @Nonnull public List<PlotPlayer<?>> getPlayersInPlot() {
        final List<PlotPlayer<?>> players = new ArrayList<>();
        for (final PlotPlayer<?> player : PlotSquared.platform().getPlayerManager().getPlayers()) {
            if (this.equals(player.getCurrentPlot())) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Checks if the plot has an owner.
     *
     * @return {@code true} if there is an owner, else {@code false}
     */
    public boolean hasOwner() {
        return this.getOwnerAbs() != null;
    }

    /**
     * Checks if a UUID is a plot owner (merged plots may have multiple owners)
     *
     * @param uuid Player UUID
     * @return {@code true} if the provided uuid is the owner of the plot, else {@code false}
     */
    public boolean isOwner(@Nonnull final UUID uuid) {
        if (uuid.equals(this.getOwner())) {
            return true;
        }
        if (!isMerged()) {
            return false;
        }
        final Set<Plot> connected = getConnectedPlots();
        return connected.stream().anyMatch(current -> uuid.equals(current.getOwner()));
    }

    /**
     * Checks if the given UUID is the owner of this specific plot
     *
     * @param uuid Player UUID
     * @return {@code true} if the provided uuid is the owner of the plot, else {@code false}
     */
    public boolean isOwnerAbs(@Nullable final UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return uuid.equals(this.getOwner());
    }

    /**
     * Get the plot owner of this particular sub-plot.
     * (Merged plots can have multiple owners)
     * Direct access is discouraged: use getOwners()
     *
     * @see #getOwnerAbs() getOwnerAbs() to get the owner as stored in the database
     *
     * @return Server if ServerPlot flag set, else {@link #getOwnerAbs()}
     */
    @Nullable public UUID getOwner() {
        if (this.getFlag(ServerPlotFlag.class)) {
            return DBFunc.SERVER;
        }
        return this.getOwnerAbs();
    }

    /**
     * Sets the plot owner (and update the database)
     *
     * @param owner uuid to set as owner
     */
    public void setOwner(@Nonnull final UUID owner) {
        if (!hasOwner()) {
            this.setOwnerAbs(owner);
            this.getPlotModificationManager().create();
            return;
        }
        if (!isMerged()) {
            if (!owner.equals(this.getOwnerAbs())) {
                this.setOwnerAbs(owner);
                DBFunc.setOwner(this, owner);
            }
            return;
        }
        for (final Plot current : getConnectedPlots()) {
            if (!owner.equals(current.getOwnerAbs())) {
                current.setOwnerAbs(owner);
                DBFunc.setOwner(current, owner);
            }
        }
    }

    /**
     * Gets a immutable set of owner UUIDs for a plot (supports multi-owner mega-plots).
     * <p>
     * This method cannot be used to add or remove owners from a plot.
     * </p>
     *
     * @return Immutable view of plot owners
     */
    @Nonnull public Set<UUID> getOwners() {
        if (this.getOwner() == null) {
            return ImmutableSet.of();
        }
        if (isMerged()) {
            Set<Plot> plots = getConnectedPlots();
            Plot[] array = plots.toArray(new Plot[0]);
            ImmutableSet.Builder<UUID> owners = ImmutableSet.builder();
            UUID last = this.getOwner();
            owners.add(this.getOwner());
            for (final Plot current : array) {
                if (current.getOwner() == null) {
                    continue;
                }
                if (last == null || current.getOwner().getMostSignificantBits() != last.getMostSignificantBits()) {
                    owners.add(current.getOwner());
                    last = current.getOwner();
                }
            }
            return owners.build();
        }
        return ImmutableSet.of(this.getOwner());
    }

    /**
     * Checks if the player is either the owner or on the trusted/added list.
     *
     * @param uuid uuid to check
     * @return {@code true} if the player is added/trusted or is the owner, else {@code false}
     */
    public boolean isAdded(@Nonnull final UUID uuid) {
        if (!this.hasOwner() || getDenied().contains(uuid)) {
            return false;
        }
        if (isOwner(uuid)) {
            return true;
        }
        if (getMembers().contains(uuid)) {
            return isOnline();
        }
        if (getTrusted().contains(uuid) || getTrusted().contains(DBFunc.EVERYONE)) {
            return true;
        }
        if (getMembers().contains(DBFunc.EVERYONE)) {
            return isOnline();
        }
        return false;
    }

    /**
     * Checks if the player is not permitted on this plot.
     *
     * @param uuid uuid to check
     * @return {@code false} if the player is allowed to enter the plot, else {@code true}
     */
    public boolean isDenied(@Nonnull final UUID uuid) {
        return this.denied != null && (this.denied.contains(DBFunc.EVERYONE) && !this.isAdded(uuid) || !this.isAdded(uuid) && this.denied
            .contains(uuid));
    }

    /**
     * Gets the {@link PlotId} of this plot.
     *
     * @return the PlotId for this plot
     */
    @Nonnull public PlotId getId() {
        return this.id;
    }

    /**
     * Change the plot ID
     *
     * @param id new plot ID
     */
    public void setId(@Nonnull final PlotId id) {
        this.id = id;
    }

    /**
     * Gets the plot world object for this plot<br>
     * - The generic PlotArea object can be casted to its respective class for more control (e.g. HybridPlotWorld)
     *
     * @return PlotArea
     */
    @Nullable public PlotArea getArea() {
        return this.area;
    }

    /**
     * Assigns this plot to a plot area.<br>
     * (Mostly used during startup when worlds are being created)<br>
     * <p>
     * Do not use this unless you absolutely know what you are doing.
     * </p>
     *
     * @param area area to assign to
     */
    public void setArea(@Nonnull final PlotArea area) {
        if (this.getArea() == area) {
            return;
        }
        if (this.getArea() != null) {
            this.area.removePlot(this.id);
        }
        this.area = area;
        area.addPlot(this);
        this.flagContainer.setParentContainer(area.getFlagContainer());
    }

    /**
     * Gets the plot manager object for this plot<br>
     * - The generic PlotManager object can be casted to its respective class for more control (e.g. HybridPlotManager)
     *
     * @return PlotManager
     */
    @Nonnull public PlotManager getManager() {
        return this.area.getPlotManager();
    }

    /**
     * Gets or create plot settings.
     *
     * @return PlotSettings
     */
    @Nonnull public PlotSettings getSettings() {
        if (this.settings == null) {
            this.settings = new PlotSettings();
        }
        return this.settings;
    }

    /**
     * Returns true if the plot is not merged, or it is the base
     * plot of multiple merged plots.
     *
     * @return Boolean
     */
    public boolean isBasePlot() {
        return !this.isMerged() || this.equals(this.getBasePlot(false));
    }

    /**
     * The base plot is an arbitrary but specific connected plot. It is useful for the following:<br>
     * - Merged plots need to be treated as a single plot for most purposes<br>
     * - Some data such as home location needs to be associated with the group rather than each plot<br>
     * - If the plot is not merged it will return itself.<br>
     * - The result is cached locally
     *
     * @param recalculate whether to recalculate the merged plots to find the origin
     *
     * @return base Plot
     */
    public Plot getBasePlot(final boolean recalculate) {
        if (this.origin != null && !recalculate) {
            if (this.equals(this.origin)) {
                return this;
            }
            return this.origin.getBasePlot(false);
        }
        if (!this.isMerged()) {
            this.origin = this;
            return this.origin;
        }
        this.origin = this;
        PlotId min = this.id;
        for (Plot plot : this.getConnectedPlots()) {
            if (plot.id.getY() < min.getY() || plot.id.getY() == min.getY() && plot.id.getX() < min.getX()) {
                this.origin = plot;
                min = plot.id;
            }
        }
        for (Plot plot : this.getConnectedPlots()) {
            plot.origin = this.origin;
        }
        return this.origin;
    }

    /**
     * Checks if this plot is merged in any direction.
     *
     * @return {@code true} if this plot is merged, otherwise {@code false}
     */
    public boolean isMerged() {
        return getSettings().getMerged(0) || getSettings().getMerged(2) || getSettings().getMerged(1) || getSettings().getMerged(3);
    }

    /**
     * Gets the timestamp of when the plot was created (unreliable)<br>
     * - not accurate if the plot was created before this was implemented<br>
     * - Milliseconds since the epoch<br>
     *
     * @return the creation date of the plot
     */
    public long getTimestamp() {
        if (this.timestamp == 0) {
            this.timestamp = System.currentTimeMillis();
        }
        return this.timestamp;
    }

    /**
     * Gets if the plot is merged in a direction<br>
     * ------- Actual -------<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * ----- Artificial -----<br>
     * 4 = north-east<br>
     * 5 = south-east<br>
     * 6 = south-west<br>
     * 7 = north-west<br>
     * ----------<br>
     *
     * Note: A plot that is merged north and east will not be merged northeast if the northeast plot is not part of the same group<br>
     *
     * @param dir direction to check for merged plot
     * @return {@code true} if merged in that direction, else {@code false}
     */
    public boolean isMerged(final int dir) {
        if (this.settings == null) {
            return false;
        }
        switch (dir) {
            case 0:
            case 1:
            case 2:
            case 3:
                return this.getSettings().getMerged(dir);
            case 7:
                int i = dir - 4;
                int i2 = 0;
                if (this.getSettings().getMerged(i2)) {
                    if (this.getSettings().getMerged(i)) {
                        if (Objects.requireNonNull(
                            this.area.getPlotAbs(this.id.getRelative(Direction.getFromIndex(i)))).isMerged(i2)) {
                            return Objects.requireNonNull(this.area
                                .getPlotAbs(this.id.getRelative(Direction.getFromIndex(i2)))).isMerged(i);
                        }
                    }
                }
                return false;
            case 4:
            case 5:
            case 6:
                i = dir - 4;
                i2 = dir - 3;
                return this.getSettings().getMerged(i2) && this.getSettings().getMerged(i) && Objects
                    .requireNonNull(
                        this.area.getPlotAbs(this.id.getRelative(Direction.getFromIndex(i)))).isMerged(i2) && Objects
                    .requireNonNull(
                        this.area.getPlotAbs(this.id.getRelative(Direction.getFromIndex(i2)))).isMerged(i);

        }
        return false;
    }

    /**
     * Gets the denied users.
     *
     * @return a set of denied users
     */
    @Nonnull public HashSet<UUID> getDenied() {
        if (this.denied == null) {
            this.denied = new HashSet<>();
        }
        return this.denied;
    }

    /**
     * Sets the denied users for this plot.
     *
     * @param uuids uuids to deny
     */
    public void setDenied(@Nonnull final Set<UUID> uuids) {
        boolean larger = uuids.size() > getDenied().size();
        HashSet<UUID> intersection;
        if (larger) {
            intersection = new HashSet<>(getDenied());
        } else {
            intersection = new HashSet<>(uuids);
        }
        if (larger) {
            intersection.retainAll(uuids);
        } else {
            intersection.retainAll(getDenied());
        }
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(getDenied());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            removeDenied(uuid);
        }
        for (UUID uuid : uuids) {
            addDenied(uuid);
        }
    }

    /**
     * Gets the trusted users.
     *
     * @return a set of trusted users
     */
    @Nonnull public HashSet<UUID> getTrusted() {
        if (this.trusted == null) {
            this.trusted = new HashSet<>();
        }
        return this.trusted;
    }

    /**
     * Sets the trusted users for this plot.
     *
     * @param uuids uuids to trust
     */
    public void setTrusted(@Nonnull final Set<UUID> uuids) {
        boolean larger = uuids.size() > getTrusted().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? getTrusted() : uuids);
        intersection.retainAll(larger ? uuids : getTrusted());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(getTrusted());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            removeTrusted(uuid);
        }
        for (UUID uuid : uuids) {
            addTrusted(uuid);
        }
    }

    /**
     * Gets the members
     *
     * @return a set of members
     */
    @Nonnull public HashSet<UUID> getMembers() {
        if (this.members == null) {
            this.members = new HashSet<>();
        }
        return this.members;
    }

    /**
     * Sets the members for this plot.
     *
     * @param uuids uuids to set member status for
     */
    public void setMembers(@Nonnull final Set<UUID> uuids) {
        boolean larger = uuids.size() > getMembers().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? getMembers() : uuids);
        intersection.retainAll(larger ? uuids : getMembers());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(getMembers());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            removeMember(uuid);
        }
        for (UUID uuid : uuids) {
            addMember(uuid);
        }
    }

    /**
     * Denies a player from this plot. (updates database as well)
     *
     * @param uuid the uuid of the player to deny.
     */
    public void addDenied(@Nonnull final UUID uuid) {
        for (final Plot current : getConnectedPlots()) {
            if (current.getDenied().add(uuid)) {
                DBFunc.setDenied(current, uuid);
            }
        }
    }

    /**
     * Add someone as a helper (updates database as well)
     *
     * @param uuid the uuid of the player to trust
     */
    public void addTrusted(@Nonnull final UUID uuid) {
        for (final Plot current : getConnectedPlots()) {
            if (current.getTrusted().add(uuid)) {
                DBFunc.setTrusted(current, uuid);
            }
        }
    }

    /**
     * Add someone as a trusted user (updates database as well)
     *
     * @param uuid the uuid of the player to add as a member
     */
    public void addMember(@Nonnull final UUID uuid) {
        for (final Plot current : getConnectedPlots()) {
            if (current.getMembers().add(uuid)) {
                DBFunc.setMember(current, uuid);
            }
        }
    }

    /**
     * Sets the plot owner (and update the database)
     *
     * @param owner     uuid to set as owner
     * @param initiator player initiating set owner
     * @return boolean
     */
    public boolean setOwner(UUID owner, PlotPlayer<?> initiator) {
        if (!hasOwner()) {
            this.setOwnerAbs(owner);
            this.getPlotModificationManager().create();
            return true;
        }
        if (!isMerged()) {
            if (!owner.equals(this.getOwnerAbs())) {
                this.setOwnerAbs(owner);
                DBFunc.setOwner(this, owner);
            }
            return true;
        }
        for (final Plot current : getConnectedPlots()) {
            if (!owner.equals(current.getOwnerAbs())) {
                current.setOwnerAbs(owner);
                DBFunc.setOwner(current, owner);
            }
        }
        return true;
    }

    public boolean isLoaded() {
        return this.worldUtil.isWorld(getWorldName());
    }

    /**
     * This will return null if the plot hasn't been analyzed
     *
     * @param settings The set of settings to obtain the analysis of
     *
     * @return analysis of plot
     */
    public PlotAnalysis getComplexity(Settings.Auto_Clear settings) {
        return PlotAnalysis.getAnalysis(this, settings);
    }

    /**
     * Get an immutable view of all the flags associated with the plot.
     *
     * @return Immutable set containing the flags associated with the plot
     */
    public Set<PlotFlag<?, ?>> getFlags() {
        return ImmutableSet.copyOf(flagContainer.getFlagMap().values());
    }

    /**
     * Sets a flag for the plot and stores it in the database.
     *
     * @param flag Flag to set
     * @param <V> flag value type
     * @return A boolean indicating whether or not the operation succeeded
     */
    public <V> boolean setFlag(@Nonnull final PlotFlag<V, ?> flag) {
        if (flag instanceof KeepFlag && ExpireManager.IMP != null) {
            ExpireManager.IMP.updateExpired(this);
        }
        for (final Plot plot : this.getConnectedPlots()) {
            plot.getFlagContainer().addFlag(flag);
            plot.reEnter();
            DBFunc.setFlag(plot, flag);
        }
        return true;
    }

    /**
     * Parse the flag value into a flag instance based on the provided
     * flag class, and store it in the database.
     *
     * @param flag  Flag type
     * @param value Flag value
     * @return A boolean indicating whether or not the operation succeeded
     */
    public boolean setFlag(@Nonnull final Class<?> flag, @Nonnull final String value) {
        try {
            this.setFlag(GlobalFlagContainer.getInstance().getFlagErased(flag).parse(value));
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Remove a flag from this plot
     *
     * @param flag the flag to remove
     * @return success
     */
    public boolean removeFlag(@Nonnull final Class<? extends PlotFlag<?, ?>> flag) {
        return this.removeFlag(getFlagContainer().queryLocal(flag));
    }

    /**
     * Get flags associated with the plot.
     *
     * @param plotOnly          Whether or not to only consider the plot. If this parameter is set to
     *                          true, the default values of the owning plot area will not be considered
     * @param ignorePluginFlags Whether or not to ignore {@link InternalFlag internal flags}
     * @return Collection containing all the flags that matched the given criteria
     */
    public Collection<PlotFlag<?, ?>> getApplicableFlags(final boolean plotOnly, final boolean ignorePluginFlags) {
        if (!hasOwner()) {
            return Collections.emptyList();
        }
        final Map<Class<?>, PlotFlag<?, ?>> flags = new HashMap<>();
        if (!plotOnly && getArea() != null && !getArea().getFlagContainer().getFlagMap().isEmpty()) {
            final Map<Class<?>, PlotFlag<?, ?>> flagMap = getArea().getFlagContainer().getFlagMap();
            flags.putAll(flagMap);
        }
        final Map<Class<?>, PlotFlag<?, ?>> flagMap = getFlagContainer().getFlagMap();
        if (ignorePluginFlags) {
            for (final PlotFlag<?, ?> flag : flagMap.values()) {
                if (flag instanceof InternalFlag) {
                    continue;
                }
                flags.put(flag.getClass(), flag);
            }
        } else {
            flags.putAll(flagMap);
        }
        return flags.values();
    }

    /**
     * Get flags associated with the plot and the plot area that contains it.
     *
     * @param ignorePluginFlags Whether or not to ignore {@link InternalFlag internal flags}
     * @return Collection containing all the flags that matched the given criteria
     */
    public Collection<PlotFlag<?, ?>> getApplicableFlags(final boolean ignorePluginFlags) {
        return getApplicableFlags(false, ignorePluginFlags);
    }

    /**
     * Remove a flag from this plot
     *
     * @param flag the flag to remove
     * @return success
     */
    public boolean removeFlag(@Nonnull final PlotFlag<?, ?> flag) {
        if (flag == null) {
            return false;
        }
        boolean removed = false;
        for (final Plot plot : origin.getConnectedPlots()) {
            final Object value = plot.getFlagContainer().removeFlag(flag);
            if (value == null) {
                continue;
            }
            plot.reEnter();
            DBFunc.removeFlag(plot, flag);
            removed = true;
        }
        return removed;
    }

    /**
     * Count the entities in a plot
     *
     * @return array of entity counts
     * @see RegionManager#countEntities(Plot)
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     */
    public int[] countEntities() {
        int[] count = new int[6];
        for (Plot current : this.getConnectedPlots()) {
            int[] result = this.regionManager.countEntities(current);
            count[CAP_ENTITY] += result[CAP_ENTITY];
            count[CAP_ANIMAL] += result[CAP_ANIMAL];
            count[CAP_MONSTER] += result[CAP_MONSTER];
            count[CAP_MOB] += result[CAP_MOB];
            count[CAP_VEHICLE] += result[CAP_VEHICLE];
            count[CAP_MISC] += result[CAP_MISC];
        }
        return count;
    }

    /**
     * Returns true if a previous task was running
     *
     * @return true if a previous task is running
     */
    public int addRunning() {
        int value = this.getRunning();
        for (Plot plot : this.getConnectedPlots()) {
            plot.setMeta("running", value + 1);
        }
        return value;
    }

    /**
     * Decrement the number of tracked tasks this plot is running<br>
     * - Used to track/limit the number of things a player can do on the plot at once
     *
     * @return previous number of tasks (int)
     */
    public int removeRunning() {
        int value = this.getRunning();
        if (value < 2) {
            for (Plot plot : this.getConnectedPlots()) {
                plot.deleteMeta("running");
            }
        } else {
            for (Plot plot : this.getConnectedPlots()) {
                plot.setMeta("running", value - 1);
            }
        }
        return value;
    }

    /**
     * Gets the number of tracked running tasks for this plot<br>
     * - Used to track/limit the number of things a player can do on the plot at once
     *
     * @return number of tasks (int)
     */
    public int getRunning() {
        Integer value = (Integer) this.getMeta("running");
        return value == null ? 0 : value;
    }

    /**
     * Unclaim the plot (does not modify terrain). Changes made to this plot will not be reflected in unclaimed plot objects.
     *
     * @return false if the Plot has no owner, otherwise true.
     */
    public boolean unclaim() {
        if (!this.hasOwner()) {
            return false;
        }
        for (Plot current : getConnectedPlots()) {
            List<PlotPlayer<?>> players = current.getPlayersInPlot();
            for (PlotPlayer<?> pp : players) {
                this.plotListener.plotExit(pp, current);
            }

            if (Settings.Backup.DELETE_ON_UNCLAIM) {
                // Destroy all backups when the plot is unclaimed
                Objects.requireNonNull(PlotSquared.platform()).getBackupManager().getProfile(current).destroy();
            }

            getArea().removePlot(getId());
            DBFunc.delete(current);
            current.setOwnerAbs(null);
            current.settings = null;
            for (final PlotPlayer<?> pp : players) {
                this.plotListener.plotEntry(pp, current);
            }
        }
        return true;
    }

    public void getCenter(final Consumer<Location> result) {
        Location[] corners = getCorners();
        Location top = corners[0];
        Location bot = corners[1];
        Location location = Location.at(this.getWorldName(), MathMan.average(bot.getX(), top.getX()), MathMan.average(bot.getY(), top.getY()),
            MathMan.average(bot.getZ(), top.getZ()));
        if (!isLoaded()) {
            result.accept(location);
            return;
        }
        this.worldUtil.getHighestBlock(getWorldName(), location.getX(), location.getZ(), y -> {
            int height = y;
            if (area.allowSigns()) {
                height = Math.max(y, getManager().getSignLoc(this).getY());
            }
            result.accept(location.withY(1 + height));
        });
    }

    /**
     * @deprecated May cause synchronous chunk loads
     *
     * @return Location of center
     */
    @Deprecated public Location getCenterSynchronous() {
        Location[] corners = getCorners();
        Location top = corners[0];
        Location bot = corners[1];
        Location location = Location.at(this.getWorldName(), MathMan.average(bot.getX(), top.getX()), MathMan.average(bot.getY(), top.getY()),
            MathMan.average(bot.getZ(), top.getZ()));
        if (!isLoaded()) {
            return location;
        }
        int y = this.worldUtil.getHighestBlockSynchronous(getWorldName(), location.getX(), location.getZ());
        if (area.allowSigns()) {
            y = Math.max(y, getManager().getSignLoc(this).getY());
        }
        return location.withY(1 + y);
    }

    /**
     * @deprecated May cause synchronous chunk loads
     *
     * @return side where players should teleport to
     */
    @Deprecated public Location getSideSynchronous() {
        CuboidRegion largest = getLargestRegion();
        int x = (largest.getMaximumPoint().getX() >> 1) - (largest.getMinimumPoint().getX() >> 1) + largest.getMinimumPoint().getX();
        int z = largest.getMinimumPoint().getZ() - 1;
        PlotManager manager = getManager();
        int y = isLoaded() ? this.worldUtil.getHighestBlockSynchronous(getWorldName(), x, z) : 62;
        if (area.allowSigns() && (y <= 0 || y >= 255)) {
            y = Math.max(y, manager.getSignLoc(this).getY() - 1);
        }
        return Location.at(getWorldName(), x, y + 1, z);
    }

    public void getSide(Consumer<Location> result) {
        CuboidRegion largest = getLargestRegion();
        int x = (largest.getMaximumPoint().getX() >> 1) - (largest.getMinimumPoint().getX() >> 1) + largest.getMinimumPoint().getX();
        int z = largest.getMinimumPoint().getZ() - 1;
        PlotManager manager = getManager();
        if (isLoaded()) {
            this.worldUtil.getHighestBlock(getWorldName(), x, z, y -> {
                int height = y;
                if (area.allowSigns() && (y <= 0 || y >= 255)) {
                    height = Math.max(y, manager.getSignLoc(this).getY() - 1);
                }
                result.accept(Location.at(getWorldName(), x, height + 1, z));
            });
        } else {
            int y = 62;
            if (area.allowSigns()) {
                y = Math.max(y, manager.getSignLoc(this).getY() - 1);
            }
            result.accept(Location.at(getWorldName(), x, y + 1, z));
        }
    }

    /**
     * @deprecated May cause synchronous chunk loading
     *
     * @return the plot home location
     */
    @Deprecated public Location getHomeSynchronous() {
        BlockLoc home = this.getPosition();
        if (home == null || home.getX() == 0 && home.getZ() == 0) {
            return this.getDefaultHomeSynchronous(true);
        } else {
            Location bottom = this.getBottomAbs();
            Location location = Location
                .at(bottom.getWorldName(), bottom.getX() + home.getX(), bottom.getY() + home.getY(), bottom.getZ() + home.getZ(), home.getYaw(),
                    home.getPitch());
            if (!isLoaded()) {
                return location;
            }
            if (!this.worldUtil.getBlockSynchronous(location).getBlockType().getMaterial().isAir()) {
                location = location.withY(
                    Math.max(1 + this.worldUtil.getHighestBlockSynchronous(this.getWorldName(), location.getX(), location.getZ()), bottom.getY()));
            }
            return location;
        }
    }

    /**
     * Return the home location for the plot
     *
     * @param result consumer to pass location to when found
     */
    public void getHome(final Consumer<Location> result) {
        BlockLoc home = this.getPosition();
        if (home == null || home.getX() == 0 && home.getZ() == 0) {
            this.getDefaultHome(result);
        } else {
            Location bottom = this.getBottomAbs();
            Location location = Location
                .at(bottom.getWorldName(), bottom.getX() + home.getX(), bottom.getY() + home.getY(), bottom.getZ() + home.getZ(), home.getYaw(),
                    home.getPitch());
            if (!isLoaded()) {
                result.accept(location);
                return;
            }
            this.worldUtil.getBlock(location, block -> {
                if (!block.getBlockType().getMaterial().isAir()) {
                    this.worldUtil.getHighestBlock(this.getWorldName(), location.getX(), location.getZ(),
                        y -> result.accept(location.withY(Math.max(1 + y, bottom.getY()))));
                } else {
                    result.accept(location);
                }
            });
        }
    }

    /**
     * Sets the home location
     *
     * @param location location to set as home
     */
    public void setHome(BlockLoc location) {
        Plot plot = this.getBasePlot(false);
        if (location != null && new BlockLoc(0, 0, 0).equals(location)) {
            return;
        }
        plot.getSettings().setPosition(location);
        if (location != null) {
            DBFunc.setPosition(plot, plot.getSettings().getPosition().toString());
            return;
        }
        DBFunc.setPosition(plot, null);
    }

    /**
     * Gets the default home location for a plot<br>
     * - Ignores any home location set for that specific plot
     *
     * @param result consumer to pass location to when found
     */
    public void getDefaultHome(Consumer<Location> result) {
        getDefaultHome(false, result);
    }

    /**
     * @deprecated May cause synchronous chunk loads
     *
     * @param member if to get the home for plot members
     *
     * @return location of home for members or visitors
     */
    @Deprecated public Location getDefaultHomeSynchronous(final boolean member) {
        Plot plot = this.getBasePlot(false);
        PlotLoc loc = member ? area.getDefaultHome() : area.getNonmemberHome();
        if (loc != null) {
            int x;
            int z;
            if (loc.getX() == Integer.MAX_VALUE && loc.getZ() == Integer.MAX_VALUE) {
                // center
                CuboidRegion largest = plot.getLargestRegion();
                x = (largest.getMaximumPoint().getX() >> 1) - (largest.getMinimumPoint().getX() >> 1) + largest.getMinimumPoint().getX();
                z = (largest.getMaximumPoint().getZ() >> 1) - (largest.getMinimumPoint().getZ() >> 1) + largest.getMinimumPoint().getZ();
            } else {
                // specific
                Location bot = plot.getBottomAbs();
                x = bot.getX() + loc.getX();
                z = bot.getZ() + loc.getZ();
            }
            int y = loc.getY() < 1 ? (isLoaded() ? this.worldUtil.getHighestBlockSynchronous(plot.getWorldName(), x, z) + 1 : 63) : loc.getY();
            return Location.at(plot.getWorldName(), x, y, z);
        }
        // Side
        return plot.getSideSynchronous();
    }

    public void getDefaultHome(boolean member, Consumer<Location> result) {
        Plot plot = this.getBasePlot(false);
        PlotLoc loc = member ? area.getDefaultHome() : area.getNonmemberHome();
        if (loc != null) {
            int x;
            int z;
            if (loc.getX() == Integer.MAX_VALUE && loc.getZ() == Integer.MAX_VALUE) {
                // center
                CuboidRegion largest = plot.getLargestRegion();
                x = (largest.getMaximumPoint().getX() >> 1) - (largest.getMinimumPoint().getX() >> 1) + largest.getMinimumPoint().getX();
                z = (largest.getMaximumPoint().getZ() >> 1) - (largest.getMinimumPoint().getZ() >> 1) + largest.getMinimumPoint().getZ();
            } else {
                // specific
                Location bot = plot.getBottomAbs();
                x = bot.getX() + loc.getX();
                z = bot.getZ() + loc.getZ();
            }
            if (loc.getY() < 1) {
                if (isLoaded()) {
                    this.worldUtil.getHighestBlock(plot.getWorldName(), x, z, y -> result.accept(Location.at(plot.getWorldName(), x, y + 1, z)));
                } else {
                    result.accept(Location.at(plot.getWorldName(), x, 63, z));
                }
            } else {
                result.accept(Location.at(plot.getWorldName(), x, loc.getY(), z));
            }
            return;
        }
        // Side
        plot.getSide(result);
    }

    public double getVolume() {
        double count = 0;
        for (CuboidRegion region : getRegions()) {
            count += (region.getMaximumPoint().getX() - (double) region.getMinimumPoint().getX() + 1) * (
                region.getMaximumPoint().getZ() - (double) region.getMinimumPoint().getZ() + 1) * MAX_HEIGHT;
        }
        return count;
    }

    /**
     * Gets the average rating of the plot. This is the value displayed in /plot info
     *
     * @return average rating as double
     */
    public double getAverageRating() {
        Collection<Rating> ratings = this.getRatings().values();
        double sum = ratings.stream().mapToDouble(Rating::getAverageRating).sum();
        return sum / ratings.size();
    }

    /**
     * Sets a rating for a user<br>
     * - If the user has already rated, the following will return false
     *
     * @param uuid   uuid of rater
     * @param rating rating
     * @return success
     */
    public boolean addRating(UUID uuid, Rating rating) {
        Plot base = this.getBasePlot(false);
        PlotSettings baseSettings = base.getSettings();
        if (baseSettings.getRatings().containsKey(uuid)) {
            return false;
        }
        int aggregate = rating.getAggregate();
        baseSettings.getRatings().put(uuid, aggregate);
        DBFunc.setRating(base, uuid, aggregate);
        return true;
    }

    /**
     * Clear the ratings/likes for this plot
     */
    public void clearRatings() {
        Plot base = this.getBasePlot(false);
        PlotSettings baseSettings = base.getSettings();
        if (baseSettings.getRatings() != null && !baseSettings.getRatings().isEmpty()) {
            DBFunc.deleteRatings(base);
            baseSettings.setRatings(null);
        }
    }

    public Map<UUID, Boolean> getLikes() {
        final Map<UUID, Boolean> map = new HashMap<>();
        final Map<UUID, Rating> ratings = this.getRatings();
        ratings.forEach((uuid, rating) -> map.put(uuid, rating.getLike()));
        return map;
    }

    /**
     * Gets the ratings associated with a plot<br>
     * - The rating object may contain multiple categories
     *
     * @return Map of user who rated to the rating
     */
    public HashMap<UUID, Rating> getRatings() {
        Plot base = this.getBasePlot(false);
        HashMap<UUID, Rating> map = new HashMap<>();
        if (!base.hasRatings()) {
            return map;
        }
        for (Entry<UUID, Integer> entry : base.getSettings().getRatings().entrySet()) {
            map.put(entry.getKey(), new Rating(entry.getValue()));
        }
        return map;
    }

    public boolean hasRatings() {
        Plot base = this.getBasePlot(false);
        return base.settings != null && base.settings.getRatings() != null;
    }

    public boolean claim(@Nonnull final PlotPlayer<?> player, boolean teleport, String schematic) {
        if (!canClaim(player)) {
            return false;
        }
        return claim(player, teleport, schematic, true);
    }

    public boolean claim(@Nonnull final PlotPlayer<?> player, boolean teleport, String schematic, boolean updateDB) {

        if (updateDB) {
            if (!this.getPlotModificationManager().create(player.getUUID(), true)) {
                logger.error("Player {} attempted to claim plot {}, but the database failed to update", player.getName(),
                    this.getId().toCommaSeparatedString());
                return false;
            }
        } else {
            area.addPlot(this);
            updateWorldBorder();
        }
        this.getPlotModificationManager().setSign(player.getName());
        player.sendMessage(TranslatableCaption.of("working.claimed"));
        if (teleport && Settings.Teleport.ON_CLAIM) {
            teleportPlayer(player, TeleportCause.COMMAND, result -> {
            });
        }
        PlotArea plotworld = getArea();
        if (plotworld.isSchematicOnClaim()) {
            Schematic sch;
            try {
                if (schematic == null || schematic.isEmpty()) {
                    sch = schematicHandler.getSchematic(plotworld.getSchematicFile());
                } else {
                    sch = schematicHandler.getSchematic(schematic);
                    if (sch == null) {
                        sch = schematicHandler.getSchematic(plotworld.getSchematicFile());
                    }
                }
            } catch (SchematicHandler.UnsupportedFormatException e) {
                e.printStackTrace();
                return true;
            }
            schematicHandler.paste(sch, this, 0, 1, 0, Settings.Schematics.PASTE_ON_TOP, player, new RunnableVal<Boolean>() {
                @Override public void run(Boolean value) {
                    if (value) {
                        player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_success"));
                    } else {
                        player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_failed"));
                    }
                }
            });
        }
        plotworld.getPlotManager().claimPlot(this, null);
        return true;
    }

    /**
     * Retrieve the biome of the plot.
     *
     * @param result consumer to pass biome to when found
     */
    public void getBiome(Consumer<BiomeType> result) {
        this.getCenter(location -> this.worldUtil.getBiome(location.getWorldName(), location.getX(), location.getZ(), result));
    }

    //TODO Better documentation needed.

    /**
     * @deprecated May cause synchronous chunk loads
     *
     * @return biome at center of plot
     */
    @Deprecated public BiomeType getBiomeSynchronous() {
        final Location location = this.getCenterSynchronous();
        return this.worldUtil.getBiomeSynchronous(location.getWorldName(), location.getX(), location.getZ());
    }

    /**
     * Returns the top location for the plot.
     *
     * @return location of Absolute Top
     */
    public Location getTopAbs() {
        return this.getManager().getPlotTopLocAbs(this.id).withWorld(this.getWorldName());
    }

    /**
     * Returns the bottom location for the plot.
     *
     * @return location of absolute bottom of plot
     */
    public Location getBottomAbs() {
        return this.getManager().getPlotBottomLocAbs(this.id).withWorld(this.getWorldName());
    }

    /**
     * Swaps the settings for two plots.
     *
     * @param plot the plot to swap data with
     * @return Future containing the result
     */
    public CompletableFuture<Boolean> swapData(Plot plot) {
        if (!this.hasOwner()) {
            if (plot != null && plot.hasOwner()) {
                plot.moveData(this, null);
                return CompletableFuture.completedFuture(true);
            }
            return CompletableFuture.completedFuture(false);
        }
        if (plot == null || plot.getOwner() == null) {
            this.moveData(plot, null);
            return CompletableFuture.completedFuture(true);
        }
        // Swap cached
        final PlotId temp = PlotId.of(this.getId().getX(), this.getId().getY());
        this.id = plot.getId().copy();
        plot.id = temp.copy();
        this.area.removePlot(this.getId());
        plot.area.removePlot(plot.getId());
        this.area.addPlotAbs(this);
        plot.area.addPlotAbs(plot);
        // Swap database
        return DBFunc.swapPlots(plot, this);
    }

    /**
     * Moves the settings for a plot.
     *
     * @param plot     the plot to move
     * @param whenDone task to run when settings have been moved
     * @return success or not
     */
    public boolean moveData(Plot plot, Runnable whenDone) {
        if (!this.hasOwner()) {
            TaskManager.runTask(whenDone);
            return false;
        }
        if (plot.hasOwner()) {
            TaskManager.runTask(whenDone);
            return false;
        }
        this.area.removePlot(this.id);
        this.id = plot.getId().copy();
        this.area.addPlotAbs(this);
        DBFunc.movePlot(this, plot);
        TaskManager.runTaskLater(whenDone, TaskTime.ticks(1L));
        return true;
    }

    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @return Location top of mega plot
     */
    public Location getExtendedTopAbs() {
        Location top = this.getTopAbs();
        if (!this.isMerged()) {
            return top;
        }
        if (this.isMerged(Direction.SOUTH)) {
            top = top.withZ(this.getRelative(Direction.SOUTH).getBottomAbs().getZ() - 1);
        }
        if (this.isMerged(Direction.EAST)) {
            top = top.withX(this.getRelative(Direction.EAST).getBottomAbs().getX() - 1);
        }
        return top;
    }

    /**
     * Gets the bottom location for a plot.<br>
     * - Does not respect mega plots<br>
     * - Merged plots, only the road will be considered part of the plot<br>
     *
     * @return Location bottom of mega plot
     */
    public Location getExtendedBottomAbs() {
        Location bot = this.getBottomAbs();
        if (!this.isMerged()) {
            return bot;
        }
        if (this.isMerged(Direction.NORTH)) {
            bot = bot.withZ(this.getRelative(Direction.NORTH).getTopAbs().getZ() + 1);
        }
        if (this.isMerged(Direction.WEST)) {
            bot = bot.withX(this.getRelative(Direction.WEST).getTopAbs().getX() + 1);
        }
        return bot;
    }

    /**
     * Returns the top and bottom location.<br>
     * - If the plot is not connected, it will return its own corners<br>
     * - the returned locations will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     *
     * @return new Location[] { bottom, top }
     * @deprecated as merged plots no longer need to be rectangular
     */
    @Deprecated public Location[] getCorners() {
        if (!this.isMerged()) {
            return new Location[] {this.getBottomAbs(), this.getTopAbs()};
        }
        return RegionUtil.getCorners(this.getWorldName(), this.getRegions());
    }

    /**
     * @deprecated in favor of getCorners()[0];<br>
     * @return bottom corner location
     */
    // Won't remove as suggestion also points to deprecated method
    @Deprecated public Location getBottom() {
        return this.getCorners()[0];
    }

    /**
     * @deprecated in favor of getCorners()[1];
     * @return the top corner of the plot
     */
    // Won't remove as suggestion also points to deprecated method
    @Deprecated public Location getTop() {
        return this.getCorners()[1];
    }

    /**
     * Gets plot display name.
     *
     * @return alias if set, else id
     */
    @Override public String toString() {
        if (this.settings != null && this.settings.getAlias().length() > 1) {
            return this.settings.getAlias();
        }
        return this.area + ";" + this.id.toString();
    }

    /**
     * Remove a denied player (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     *
     * @param uuid uuid of player to remove from denied list
     *
     * @return success or not
     */
    public boolean removeDenied(UUID uuid) {
        if (uuid == DBFunc.EVERYONE && !denied.contains(uuid)) {
            boolean result = false;
            for (UUID other : new HashSet<>(getDenied())) {
                result = rmvDenied(other) || result;
            }
            return result;
        }
        return rmvDenied(uuid);
    }

    private boolean rmvDenied(UUID uuid) {
        for (Plot current : this.getConnectedPlots()) {
            if (current.getDenied().remove(uuid)) {
                DBFunc.removeDenied(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove a helper (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     *
     * @param uuid uuid of trusted player to remove
     *
     * @return success or not
     */
    public boolean removeTrusted(UUID uuid) {
        if (uuid == DBFunc.EVERYONE && !trusted.contains(uuid)) {
            boolean result = false;
            for (UUID other : new HashSet<>(getTrusted())) {
                result = rmvTrusted(other) || result;
            }
            return result;
        }
        return rmvTrusted(uuid);
    }

    private boolean rmvTrusted(UUID uuid) {
        for (Plot plot : this.getConnectedPlots()) {
            if (plot.getTrusted().remove(uuid)) {
                DBFunc.removeTrusted(plot, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove a trusted user (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     *
     * @param uuid uuid of player to remove
     *
     * @return success or not
     */
    public boolean removeMember(UUID uuid) {
        if (this.members == null) {
            return false;
        }
        if (uuid == DBFunc.EVERYONE && !members.contains(uuid)) {
            boolean result = false;
            for (UUID other : new HashSet<>(this.members)) {
                result = rmvMember(other) || result;
            }
            return result;
        }
        return rmvMember(uuid);
    }

    private boolean rmvMember(UUID uuid) {
        for (Plot current : this.getConnectedPlots()) {
            if (current.getMembers().remove(uuid)) {
                DBFunc.removeMember(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Plot other = (Plot) obj;
        return this.hashCode() == other.hashCode() && this.id.equals(other.id) && this.area == other.area;
    }

    /**
     * Gets the plot hashcode<br>
     * Note: The hashcode is unique if:<br>
     * - Plots are in the same world<br>
     * - The x,z coordinates are between Short.MIN_VALUE and Short.MAX_VALUE<br>
     *
     * @return integer.
     */
    @Override public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * Gets the plot alias.
     * - Returns an empty string if no alias is set
     *
     * @return The plot alias
     */
    @Nonnull public String getAlias() {
        if (this.settings == null) {
            return "";
        }
        return this.settings.getAlias();
    }

    /**
     * Sets the plot alias.
     *
     * @param alias The alias
     */
    public void setAlias(String alias) {
        for (Plot current : this.getConnectedPlots()) {
            String name = this.getSettings().getAlias();
            if (alias == null) {
                alias = "";
            }
            if (name.equals(alias)) {
                return;
            }
            current.getSettings().setAlias(alias);
            DBFunc.setAlias(current, alias);
        }
    }

    /**
     * Sets the raw merge data<br>
     * - Updates DB<br>
     * - Does not modify terrain<br>
     *
     * @param direction direction to merge the plot in
     * @param value if the plot is merged or not
     */
    public void setMerged(Direction direction, boolean value) {
        if (this.getSettings().setMerged(direction, value)) {
            if (value) {
                Plot other = this.getRelative(direction).getBasePlot(false);
                if (!other.equals(this.getBasePlot(false))) {
                    Plot base = other.id.getY() < this.id.getY() || other.id.getY() == this.id.getY() && other.id.getX() < this.id.getX() ?
                        other :
                        this.origin;
                    this.origin.origin = base;
                    other.origin = base;
                    this.origin = base;
                    connected_cache = null;
                }
            } else {
                if (this.origin != null) {
                    this.origin.origin = null;
                    this.origin = null;
                }
                connected_cache = null;
            }
            DBFunc.setMerged(this, this.getSettings().getMerged());
            regions_cache = null;
        }
    }

    /**
     * Gets the merged array.
     *
     * @return boolean [ north, east, south, west ]
     */
    public boolean[] getMerged() {
        return this.getSettings().getMerged();
    }

    /**
     * Sets the raw merge data<br>
     * - Updates DB<br>
     * - Does not modify terrain<br>
     * Gets if the plot is merged in a direction<br>
     * ----------<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * ----------<br>
     * Note: Diagonal merging (4-7) must be done by merging the corresponding plots.
     *
     * @param merged set the plot's merged plots
     */
    public void setMerged(boolean[] merged) {
        this.getSettings().setMerged(merged);
        DBFunc.setMerged(this, merged);
        clearCache();
    }

    public void clearCache() {
        connected_cache = null;
        regions_cache = null;
        if (this.origin != null) {
            this.origin.origin = null;
            this.origin = null;
        }
    }

    /**
     * Gets the set home location or 0,0,0 if no location is set<br>
     * - Does not take the default home location into account
     *
     * @return home location
     */
    public BlockLoc getPosition() {
        return this.getSettings().getPosition();
    }

    /**
     * Check if a plot can be claimed by the provided player.
     *
     * @param player the claiming player
     *
     * @return if the given player can claim the plot
     */
    public boolean canClaim(@Nonnull PlotPlayer player) {
        PlotCluster cluster = this.getCluster();
        if (cluster != null) {
            if (!cluster.isAdded(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.claim")) {
                return false;
            }
        }
        final UUID owner = this.getOwnerAbs();
        if (owner != null) {
            return false;
        }
        return !isMerged();
    }

    /**
     * Merge the plot settings<br>
     * - Used when a plot is merged<br>
     *
     * @param plot plot to merge the data from
     */
    public void mergeData(Plot plot) {
        final FlagContainer flagContainer1 = this.getFlagContainer();
        final FlagContainer flagContainer2 = plot.getFlagContainer();
        if (!flagContainer1.equals(flagContainer2)) {
            boolean greater = flagContainer1.getFlagMap().size() > flagContainer2.getFlagMap().size();
            if (greater) {
                flagContainer1.addAll(flagContainer2.getFlagMap().values());
            } else {
                flagContainer2.addAll(flagContainer1.getFlagMap().values());
            }
            if (!greater) {
                this.flagContainer.clearLocal();
                this.flagContainer.addAll(flagContainer2.getFlagMap().values());
            }
            plot.flagContainer.clearLocal();
            plot.flagContainer.addAll(this.flagContainer.getFlagMap().values());
        }
        if (!this.getAlias().isEmpty()) {
            plot.setAlias(this.getAlias());
        } else if (!plot.getAlias().isEmpty()) {
            this.setAlias(plot.getAlias());
        }
        for (UUID uuid : this.getTrusted()) {
            plot.addTrusted(uuid);
        }
        for (UUID uuid : plot.getTrusted()) {
            this.addTrusted(uuid);
        }
        for (UUID uuid : this.getMembers()) {
            plot.addMember(uuid);
        }
        for (UUID uuid : plot.getMembers()) {
            this.addMember(uuid);
        }

        for (UUID uuid : this.getDenied()) {
            plot.addDenied(uuid);
        }
        for (UUID uuid : plot.getDenied()) {
            this.addDenied(uuid);
        }
    }

    /**
     * Gets the plot in a relative location<br>
     * Note: May be null if the partial plot area does not include the relative location
     *
     * @param x relative id X
     * @param y relative id Y
     * @return Plot
     */
    public Plot getRelative(int x, int y) {
        return this.area.getPlotAbs(PlotId.of(this.id.getX() + x, this.id.getY() + y));
    }

    public Plot getRelative(PlotArea area, int x, int y) {
        return area.getPlotAbs(PlotId.of(this.id.getX() + x, this.id.getY() + y));
    }

    /**
     * Gets the plot in a relative direction
     * Note: May be null if the partial plot area does not include the relative location
     *
     * @param direction Direction
     * @return the plot relative to this one
     */
    @Nullable public Plot getRelative(@Nonnull Direction direction) {
        return this.area.getPlotAbs(this.id.getRelative(direction));
    }

    /**
     * Gets a set of plots connected (and including) this plot<br>
     * - This result is cached globally
     *
     * @return a Set of Plots connected to this Plot
     */
    public Set<Plot> getConnectedPlots() {
        if (this.settings == null) {
            return Collections.singleton(this);
        }
        if (!this.isMerged()) {
            return Collections.singleton(this);
        }
        if (connected_cache != null && connected_cache.contains(this)) {
            return connected_cache;
        }
        regions_cache = null;

        HashSet<Plot> tmpSet = new HashSet<>();
        tmpSet.add(this);
        Plot tmp;
        HashSet<Object> queuecache = new HashSet<>();
        ArrayDeque<Plot> frontier = new ArrayDeque<>();
        if (this.isMerged(Direction.NORTH)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.NORTH));
            if (!tmp.isMerged(Direction.SOUTH)) {
                // invalid merge
                if (tmp.isOwnerAbs(this.getOwnerAbs())) {
                    tmp.getSettings().setMerged(Direction.SOUTH, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.NORTH, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (this.isMerged(Direction.EAST)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.EAST));
            assert tmp != null;
            if (!tmp.isMerged(Direction.WEST)) {
                // invalid merge
                if (tmp.isOwnerAbs(this.getOwnerAbs())) {
                    tmp.getSettings().setMerged(Direction.WEST, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.EAST, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (this.isMerged(Direction.SOUTH)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.SOUTH));
            assert tmp != null;
            if (!tmp.isMerged(Direction.NORTH)) {
                // invalid merge
                if (tmp.isOwnerAbs(this.getOwnerAbs())) {
                    tmp.getSettings().setMerged(Direction.NORTH, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.SOUTH, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (this.isMerged(Direction.WEST)) {
            tmp = this.area.getPlotAbs(this.id.getRelative(Direction.WEST));
            if (!tmp.isMerged(Direction.EAST)) {
                // invalid merge
                if (tmp.isOwnerAbs(this.getOwnerAbs())) {
                    tmp.getSettings().setMerged(Direction.EAST, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(Direction.WEST, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        Plot current;
        while ((current = frontier.poll()) != null) {
            if (!current.hasOwner() || current.settings == null) {
                continue;
            }
            tmpSet.add(current);
            queuecache.remove(current);
            if (current.isMerged(Direction.NORTH)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.NORTH));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (current.isMerged(Direction.EAST)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.EAST));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (current.isMerged(Direction.SOUTH)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.SOUTH));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (current.isMerged(Direction.WEST)) {
                tmp = current.area.getPlotAbs(current.id.getRelative(Direction.WEST));
                if (tmp != null && !queuecache.contains(tmp) && !tmpSet.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
        }
        connected_cache = tmpSet;
        return tmpSet;
    }

    /**
     * This will combine each plot into effective rectangular regions<br>
     * - This result is cached globally<br>
     * - Useful for handling non rectangular shapes
     *
     * @return all regions within the plot
     */
    @Nonnull public Set<CuboidRegion> getRegions() {
        if (regions_cache != null && connected_cache != null && connected_cache.contains(this)) {
            return regions_cache;
        }
        if (!this.isMerged()) {
            Location pos1 = this.getBottomAbs();
            Location pos2 = this.getTopAbs();
            connected_cache = Sets.newHashSet(this);
            CuboidRegion rg = new CuboidRegion(pos1.getBlockVector3(), pos2.getBlockVector3());
            regions_cache = Collections.singleton(rg);
            return regions_cache;
        }
        Set<Plot> plots = this.getConnectedPlots();
        Set<CuboidRegion> regions = regions_cache = new HashSet<>();
        Set<PlotId> visited = new HashSet<>();
        for (Plot current : plots) {
            if (visited.contains(current.getId())) {
                continue;
            }
            boolean merge = true;
            PlotId bot = PlotId.of(current.getId().getX(), current.getId().getY());
            PlotId top = PlotId.of(current.getId().getX(), current.getId().getY());
            while (merge) {
                merge = false;
                List<PlotId> ids = Lists.newArrayList((Iterable<? extends PlotId>) PlotId.PlotRangeIterator
                    .range(PlotId.of(bot.getX(), bot.getY() - 1), PlotId.of(top.getX(), bot.getY() - 1)));
                boolean tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.isMerged(Direction.SOUTH) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot = PlotId.of(bot.getX(), bot.getY() - 1);
                }
                ids = Lists.newArrayList((Iterable<? extends PlotId>) PlotId.PlotRangeIterator
                    .range(PlotId.of(top.getX() + 1, bot.getY()), PlotId.of(top.getX() + 1, top.getY())));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.isMerged(Direction.WEST) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top = PlotId.of(top.getX() + 1, top.getY());
                }
                ids = Lists.newArrayList((Iterable<? extends PlotId>) PlotId.PlotRangeIterator
                    .range(PlotId.of(bot.getX(), top.getY() + 1), PlotId.of(top.getX(), top.getY() + 1)));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.isMerged(Direction.NORTH) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top = PlotId.of(top.getX(), top.getY() + 1);
                }
                ids = Lists.newArrayList((Iterable<? extends PlotId>) PlotId.PlotRangeIterator
                    .range(PlotId.of(bot.getX() - 1, bot.getY()), PlotId.of(bot.getX() - 1, top.getY())));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = this.area.getPlotAbs(id);
                    if (plot == null || !plot.isMerged(Direction.EAST) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot = PlotId.of(bot.getX() - 1, bot.getX());
                }
            }
            Location gtopabs = this.area.getPlotAbs(top).getTopAbs();
            Location gbotabs = this.area.getPlotAbs(bot).getBottomAbs();
            visited.addAll(Lists.newArrayList((Iterable<? extends PlotId>) PlotId.PlotRangeIterator.range(bot, top)));
            for (int x = bot.getX(); x <= top.getX(); x++) {
                Plot plot = this.area.getPlotAbs(PlotId.of(x, top.getY()));
                if (plot.isMerged(Direction.SOUTH)) {
                    // south wedge
                    Location toploc = plot.getExtendedTopAbs();
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    BlockVector3 pos1 = BlockVector3.at(botabs.getX(), 0, topabs.getZ() + 1);
                    BlockVector3 pos2 = BlockVector3.at(topabs.getX(), Plot.MAX_HEIGHT - 1, toploc.getZ());
                    regions.add(new CuboidRegion(pos1, pos2));
                    if (plot.isMerged(Direction.SOUTHEAST)) {
                        pos1 = BlockVector3.at(topabs.getX() + 1, 0, topabs.getZ() + 1);
                        pos2 = BlockVector3.at(toploc.getX(), Plot.MAX_HEIGHT - 1, toploc.getZ());
                        regions.add(new CuboidRegion(pos1, pos2));
                        // intersection
                    }
                }
            }

            for (int y = bot.getY(); y <= top.getY(); y++) {
                Plot plot = this.area.getPlotAbs(PlotId.of(top.getX(), y));
                if (plot.isMerged(Direction.EAST)) {
                    // east wedge
                    Location toploc = plot.getExtendedTopAbs();
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    BlockVector3 pos1 = BlockVector3.at(topabs.getX() + 1, 0, botabs.getZ());
                    BlockVector3 pos2 = BlockVector3.at(toploc.getX(), Plot.MAX_HEIGHT - 1, topabs.getZ());
                    regions.add(new CuboidRegion(pos1, pos2));
                    if (plot.isMerged(Direction.SOUTHEAST)) {
                        pos1 = BlockVector3.at(topabs.getX() + 1, 0, topabs.getZ() + 1);
                        pos2 = BlockVector3.at(toploc.getX(), Plot.MAX_HEIGHT - 1, toploc.getZ());
                        regions.add(new CuboidRegion(pos1, pos2));
                        // intersection
                    }
                }
            }
            BlockVector3 pos1 = BlockVector3.at(gbotabs.getX(), 0, gbotabs.getZ());
            BlockVector3 pos2 = BlockVector3.at(gtopabs.getX(), Plot.MAX_HEIGHT - 1, gtopabs.getZ());
            regions.add(new CuboidRegion(pos1, pos2));
        }
        return regions;
    }

    /**
     * Attempt to find the largest rectangular region in a plot (as plots can form non rectangular shapes)
     *
     * @return the plot's largest CuboidRegion
     */
    public CuboidRegion getLargestRegion() {
        Set<CuboidRegion> regions = this.getRegions();
        CuboidRegion max = null;
        double area = Double.NEGATIVE_INFINITY;
        for (CuboidRegion region : regions) {
            double current = (region.getMaximumPoint().getX() - (double) region.getMinimumPoint().getX() + 1) * (
                region.getMaximumPoint().getZ() - (double) region.getMinimumPoint().getZ() + 1);
            if (current > area) {
                max = region;
                area = current;
            }
        }
        return max;
    }

    /**
     * Do the plot entry tasks for each player in the plot<br>
     * - Usually called when the plot state changes (unclaimed/claimed/flag change etc)
     */
    public void reEnter() {
        TaskManager.runTaskLater(() -> {
            for (PlotPlayer<?> pp : Plot.this.getPlayersInPlot()) {
                this.plotListener.plotExit(pp, Plot.this);
                this.plotListener.plotEntry(pp, Plot.this);
            }
        }, TaskTime.ticks(1L));
    }

    public void debug(@Nonnull final String message) {
        try {
            final Collection<PlotPlayer<?>> players = PlotPlayer.getDebugModePlayersInPlot(this);
            if (players.isEmpty()) {
                return;
            }
            Caption caption = TranslatableCaption.of("debug.plot_debug");
            Template plotTemplate = Template.of("plot", this.toString());
            Template messageTemplate = Template.of("message", message);
            for (final PlotPlayer<?> player : players) {
                if (isOwner(player.getUUID()) || Permissions.hasPermission(player, Permission.PERMISSION_ADMIN_DEBUG_OTHER)) {
                    player.sendMessage(caption, plotTemplate, messageTemplate);
                }
            }
        } catch (final Exception ignored) {
        }
    }

    /**
     * Teleport a player to a plot and send them the teleport message.
     *
     * @param player the player
     * @param result Called with the result of the teleportation
     */
    public void teleportPlayer(final PlotPlayer<?> player, Consumer<Boolean> result) {
        teleportPlayer(player, TeleportCause.PLUGIN, result);
    }

    /**
     * Teleport a player to a plot and send them the teleport message.
     *
     * @param player         the player
     * @param cause          the cause of the teleport
     * @param resultConsumer Called with the result of the teleportation
     */
    public void teleportPlayer(final PlotPlayer<?> player, TeleportCause cause, Consumer<Boolean> resultConsumer) {
        Plot plot = this.getBasePlot(false);
        Result result = this.eventDispatcher.callTeleport(player, player.getLocation(), plot).getEventResult();
        if (result == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Teleport"));
            resultConsumer.accept(false);
            return;
        }
        final Consumer<Location> locationConsumer = location -> {
            if (Settings.Teleport.DELAY == 0 || Permissions
                .hasPermission(player, "plots.teleport.delay.bypass")) {
                player.sendMessage(TranslatableCaption.of("teleport.teleported_to_plot"));
                player.teleport(location, cause);
                resultConsumer.accept(true);
                return;
            }
            player.sendMessage(
                    TranslatableCaption.of("teleport.teleport_in_seconds"),
                    Template.of("amount", String.valueOf(Settings.Teleport.DELAY))
            );
            final String name = player.getName();
            TaskManager.addToTeleportQueue(name);
            TaskManager.runTaskLater(() -> {
                if (!TaskManager.removeFromTeleportQueue(name)) {
                    return;
                }
                try {
                    player.sendMessage(TranslatableCaption.of("teleport.teleported_to_plot"));
                    player.teleport(location, cause);
                } catch (final Exception ignored) {
                }
            }, TaskTime.seconds(Settings.Teleport.DELAY));
            resultConsumer.accept(true);
        };
        if (this.area.isHomeAllowNonmember() || plot.isAdded(player.getUUID())) {
            this.getHome(locationConsumer);
        } else {
            this.getDefaultHome(false, locationConsumer);
        }
    }

    /**
     * Checks if the owner of this Plot is online.
     *
     * @return true if the owner of the Plot is online
     */
    public boolean isOnline() {
        if (!this.hasOwner()) {
            return false;
        }
        if (!isMerged()) {
            return PlotSquared.platform().getPlayerManager().getPlayerIfExists(Objects.requireNonNull(this.getOwnerAbs())) != null;
        }
        for (final Plot current : getConnectedPlots()) {
            if (current.hasOwner()
                && PlotSquared.platform().getPlayerManager().getPlayerIfExists(Objects.requireNonNull(current.getOwnerAbs())) != null) {
                return true;
            }
        }
        return false;
    }

    public int getDistanceFromOrigin() {
        Location bot = getManager().getPlotBottomLocAbs(id);
        Location top = getManager().getPlotTopLocAbs(id);
        return Math.max(Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ())), Math.max(Math.abs(top.getX()), Math.abs(top.getZ())));
    }

    /**
     * Expands the world border to include this plot if it is beyond the current border.
     */
    public void updateWorldBorder() {
        int border = this.area.getBorder();
        if (border == Integer.MAX_VALUE) {
            return;
        }
        int max = getDistanceFromOrigin();
        if (max > border) {
            this.area.setMeta("worldBorder", max);
        }
    }

    /**
     * Merges two plots. <br>- Assumes plots are directly next to each other <br> - saves to DB
     *
     * @param lesserPlot the plot to merge into this plot instance
     * @param removeRoads if roads should be removed during the merge
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public void mergePlot(Plot lesserPlot, boolean removeRoads, @Nullable QueueCoordinator queue) {
        Plot greaterPlot = this;
        lesserPlot.getPlotModificationManager().removeSign();
        if (lesserPlot.getId().getX() == greaterPlot.getId().getX()) {
            if (lesserPlot.getId().getY() > greaterPlot.getId().getY()) {
                Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.isMerged(Direction.SOUTH)) {
                lesserPlot.clearRatings();
                greaterPlot.clearRatings();
                lesserPlot.setMerged(Direction.SOUTH, true);
                greaterPlot.setMerged(Direction.NORTH, true);
                lesserPlot.mergeData(greaterPlot);
                if (removeRoads) {
                    //lesserPlot.removeSign();
                    lesserPlot.getPlotModificationManager().removeRoadSouth(queue);
                    Plot diagonal = greaterPlot.getRelative(Direction.EAST);
                    if (diagonal.isMerged(Direction.NORTHWEST)) {
                        lesserPlot.plotModificationManager.removeRoadSouthEast(queue);
                    }
                    Plot below = greaterPlot.getRelative(Direction.WEST);
                    if (below.isMerged(Direction.NORTHEAST)) {
                        below.getRelative(Direction.NORTH).plotModificationManager.removeRoadSouthEast(queue);
                    }
                }
            }
        } else {
            if (lesserPlot.getId().getX() > greaterPlot.getId().getX()) {
                Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.isMerged(Direction.EAST)) {
                lesserPlot.clearRatings();
                greaterPlot.clearRatings();
                lesserPlot.setMerged(Direction.EAST, true);
                greaterPlot.setMerged(Direction.WEST, true);
                lesserPlot.mergeData(greaterPlot);
                if (removeRoads) {
                    //lesserPlot.removeSign();
                    Plot diagonal = greaterPlot.getRelative(Direction.SOUTH);
                    if (diagonal.isMerged(Direction.NORTHWEST)) {
                        lesserPlot.plotModificationManager.removeRoadSouthEast(queue);
                    }
                    lesserPlot.plotModificationManager.removeRoadEast(queue);
                }
                Plot below = greaterPlot.getRelative(Direction.NORTH);
                if (below.isMerged(Direction.SOUTHWEST)) {
                    below.getRelative(Direction.WEST).getPlotModificationManager().removeRoadSouthEast(queue);
                }
            }
        }
    }

    /**
     * Check if the plot is merged in a given direction
     *
     * @param direction Direction
     * @return {@code true} if the plot is merged in the given direction
     */
    public boolean isMerged(@Nonnull final Direction direction) {
        return isMerged(direction.getIndex());
    }

    /**
     * Get the value associated with the specified flag. This will first look at plot
     * specific flag values, then at the containing plot area and its default values
     * and at last, it will look at the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flagClass The flag type (Class)
     * @param <T> the flag value type
     * @return The flag value
     */
    @Nonnull public <T> T getFlag(@Nonnull final Class<? extends PlotFlag<T, ?>> flagClass) {
        return this.flagContainer.getFlag(flagClass).getValue();
    }

    /**
     * Get the value associated with the specified flag. This will first look at plot
     * specific flag values, then at the containing plot area and its default values
     * and at last, it will look at the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flag The flag type (Any instance of the flag)
     * @param <V> the flag type (Any instance of the flag)
     * @param <T> the flag's value type
     * @return The flag value
     */
    @Nonnull public <T, V extends PlotFlag<T, ?>> T getFlag(@Nonnull final V flag) {
        final Class<?> flagClass = flag.getClass();
        final PlotFlag<?, ?> flagInstance = this.flagContainer.getFlagErased(flagClass);
        return FlagContainer.<T, V>castUnsafe(flagInstance).getValue();
    }

    public CompletableFuture<Caption> format(final Caption iInfo, PlotPlayer<?> player, final boolean full) {
        final CompletableFuture<Caption> future = new CompletableFuture<>();
        int num = this.getConnectedPlots().size();
        String alias = !this.getAlias().isEmpty() ? this.getAlias() : TranslatableCaption.of("info.none").getComponent(player);
        Location bot = this.getCorners()[0];
        PlotSquared.platform().getWorldUtil().getBiome(Objects.requireNonNull(this.getWorldName()), bot.getX(), bot.getZ(), biome -> {
            Component trusted = PlayerManager.getPlayerList(this.getTrusted());
            Component members = PlayerManager.getPlayerList(this.getMembers());
            Component denied = PlayerManager.getPlayerList(this.getDenied());
            String seen;
            if (Settings.Enabled_Components.PLOT_EXPIRY && ExpireManager.IMP != null) {
                if (this.isOnline()) {
                    seen = TranslatableCaption.of("info.now").getComponent(player);
                } else {
                    int time = (int) (ExpireManager.IMP.getAge(this) / 1000);
                    if (time != 0) {
                        seen = TimeUtil.secToTime(time);
                    } else {
                        seen = TranslatableCaption.of("info.known").getComponent(player);
                    }
                }
            } else {
                seen = TranslatableCaption.of("info.never").getComponent(player);
            }

            String description = this.getFlag(DescriptionFlag.class);
            if (description.isEmpty()) {
                description = TranslatableCaption.of("info.plot_no_description").getComponent(player);
            }

            Component flags = null;
            Collection<PlotFlag<?, ?>> flagCollection = this.getApplicableFlags(true);
            if (flagCollection.isEmpty()) {
                flags = MINI_MESSAGE.parse(TranslatableCaption.of("info.none").getComponent(player));
            } else {
                String prefix = " ";
                for (final PlotFlag<?, ?> flag : flagCollection) {
                    Object value;
                    if (flag instanceof DoubleFlag && !Settings.General.SCIENTIFIC) {
                        value = FLAG_DECIMAL_FORMAT.format(flag.getValue());
                    } else {
                        value = flag.toString();
                    }
                    Component snip = MINI_MESSAGE
                        .parse(prefix + CaptionUtility.format(player, TranslatableCaption.of("info.plot_flag_list").getComponent(player)),
                            Template.of("flag", flag.getName()), Template.of("value", CaptionUtility.formatRaw(player, value.toString())));
                    if (flags != null) {
                        flags.append(snip);
                    } else {
                        flags = snip;
                    }
                    prefix = ", ";
                }
            }
            boolean build = this.isAdded(player.getUUID());
            Component owner = this.getOwners().isEmpty() ? Component.text("unowned") : PlayerManager.getPlayerList(this.getOwners());
            Template headerTemplate = Template.of("header", TranslatableCaption.of("info.plot_info_header").getComponent(player));
            Template footerTemplate = Template.of("footer", TranslatableCaption.of("info.plot_info_footer").getComponent(player));
            Template areaTemplate;
            if (this.getArea() != null) {
                areaTemplate =
                    Template.of("area", this.getArea().getWorldName() + (this.getArea().getId() == null ? "" : "(" + this.getArea().getId() + ")"));
            } else {
                areaTemplate = Template.of("area", TranslatableCaption.of("info.none").getComponent(player));
            }
            Template idTemplate = Template.of("id", this.getId().toString());
            Template aliasTemplate = Template.of("alias", alias);
            Template numTemplate = Template.of("num", String.valueOf(num));
            Template descTemplate = Template.of("desc", description);
            Template biomeTemplate = Template.of("biome", biome.toString().toLowerCase());
            Template ownerTemplate = Template.of("owner", owner);
            Template membersTemplate = Template.of("members", members);
            Template playerTemplate = Template.of("player", player.getName());
            Template trustedTemplate = Template.of("trusted", trusted);
            Template helpersTemplate = Template.of("helpers", members);
            Template deniedTemplate = Template.of("denied", denied);
            Template seenTemplate = Template.of("seen", seen);
            Template flagsTemplate = Template.of("flags", flags);
            Template buildTemplate = Template.of("build", String.valueOf(build));
            if (iInfo.getComponent(player).contains("<rating>")) {
                TaskManager.runTaskAsync(() -> {
                    Template ratingTemplate;
                    if (Settings.Ratings.USE_LIKES) {
                        ratingTemplate = Template.of("rating", String.format("%.0f%%", Like.getLikesPercentage(this) * 100D));
                    } else {
                        int max = 10;
                        if (Settings.Ratings.CATEGORIES != null && !Settings.Ratings.CATEGORIES.isEmpty()) {
                            max = 8;
                        }
                        if (full && Settings.Ratings.CATEGORIES != null && Settings.Ratings.CATEGORIES.size() > 1) {
                            double[] ratings = this.getAverageRatings();
                            StringBuilder rating = new StringBuilder();
                            String prefix = "";
                            for (int i = 0; i < ratings.length; i++) {
                                rating.append(prefix).append(Settings.Ratings.CATEGORIES.get(i)).append('=')
                                    .append(String.format("%.1f", ratings[i]));
                                prefix = ",";
                            }
                            ratingTemplate = Template.of("rating", rating.toString());
                        } else {
                            ratingTemplate = Template.of("rating", String.format("%.1f", this.getAverageRating()) + '/' + max);
                        }
                    }
                    future.complete(StaticCaption.of(MINI_MESSAGE.serialize(MINI_MESSAGE
                        .parse(iInfo.getComponent(player), headerTemplate, areaTemplate, idTemplate, aliasTemplate, numTemplate, descTemplate,
                            biomeTemplate, ownerTemplate, membersTemplate, playerTemplate, trustedTemplate, helpersTemplate, deniedTemplate,
                            seenTemplate, flagsTemplate, buildTemplate, ratingTemplate, footerTemplate))));
                });
                return;
            }
            future.complete(StaticCaption.of(MINI_MESSAGE.serialize(MINI_MESSAGE
                .parse(iInfo.getComponent(player), headerTemplate, areaTemplate, idTemplate, aliasTemplate, numTemplate, descTemplate, biomeTemplate,
                    ownerTemplate, membersTemplate, playerTemplate, trustedTemplate, helpersTemplate, deniedTemplate, seenTemplate, flagsTemplate,
                    buildTemplate, footerTemplate))));
        });
        return future;
    }

    /**
     * If rating categories are enabled, get the average rating by category.<br>
     * - The index corresponds to the index of the category in the config
     *
     * @return Average ratings in each category
     * @see Settings.Ratings#CATEGORIES Rating categories
     */
    @Nonnull public double[] getAverageRatings() {
        Map<UUID, Integer> rating;
        if (this.getSettings().getRatings() != null) {
            rating = this.getSettings().getRatings();
        } else if (Settings.Enabled_Components.RATING_CACHE) {
            rating = new HashMap<>();
        } else {
            rating = DBFunc.getRatings(this);
        }
        int size = 1;
        if (!Settings.Ratings.CATEGORIES.isEmpty()) {
            size = Math.max(1, Settings.Ratings.CATEGORIES.size());
        }
        double[] ratings = new double[size];
        if (rating == null || rating.isEmpty()) {
            return ratings;
        }
        for (Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if (Settings.Ratings.CATEGORIES.isEmpty()) {
                ratings[0] += current;
            } else {
                for (int i = 0; i < Settings.Ratings.CATEGORIES.size(); i++) {
                    ratings[i] += current % 10 - 1;
                    current /= 10;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            ratings[i] /= rating.size();
        }
        return ratings;
    }

    /**
     * Get the plot flag container
     *
     * @return Flag container
     */
    @Nonnull public FlagContainer getFlagContainer() {
        return this.flagContainer;
    }

    /**
     * Get the plot comment container. This can be used to manage
     * and access plot comments
     *
     * @return Plot comment container
     */
    @Nonnull public PlotCommentContainer getPlotCommentContainer() {
        return this.plotCommentContainer;
    }

    /**
     * Get the plot modification manager
     *
     * @return Plot modification manager
     */
    @Nonnull public PlotModificationManager getPlotModificationManager() {
        return this.plotModificationManager;
    }

}
