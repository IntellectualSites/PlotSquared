////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.object;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.listener.PlotListener;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The plot class<br>
 * [IMPORTANT]
 *  - Unclaimed plots will not have persistent information.
 *  - Any information set/modified in an unclaimed object may not be reflected in other instances
 *  - Using the `new` operator will create an unclaimed plot instance
 *  - Use the methods from the PlotArea/PS/Location etc to get existing plots
 */
@SuppressWarnings("javadoc")
public class Plot {
    /**
     * @deprecated raw access is deprecated
     */
    @Deprecated
    private static HashSet<Plot> connected_cache;
    private static HashSet<RegionWrapper> regions_cache;
    /**
     * The {@link PlotId}
     */
    private final PlotId id;
    /**
     * plot owner
     * (Merged plots can have multiple owners)
     * Direct access is Deprecated: use getOwners()
     */
    @Deprecated
    public UUID owner;
    /**
     * Has the plot changed since the last save cycle?
     */
    public boolean countsTowardsMax = true;
    /**
     * Represents whatever the database manager needs it to: <br>
     *  - A value of -1 usually indicates the plot will not be stored in the DB<br>
     *  - A value of 0 usually indicates that the DB manager hasn't set a value<br>
     * @deprecated magical
     */
    @Deprecated
    public int temp;
    /**
     * Plot creation timestamp (not accurate if the plot was created before this was implemented)<br>
     *  - Milliseconds since the epoch<br>
     */
    private long timestamp;
    /**
     * List of trusted (with plot permissions)
     */
    private HashSet<UUID> trusted;
    /**
     * List of members users (with plot permissions)
     */
    private HashSet<UUID> members;
    /**
     * List of denied players
     */
    private HashSet<UUID> denied;
    /**
     * External settings class<br>
     *  - Please favor the methods over direct access to this class<br>
     *  - The methods are more likely to be left unchanged from version changes<br>
     */
    private PlotSettings settings;
    /**
     * The {@link PlotArea}
     */
    private PlotArea area;
    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     *  For persistent metadata use the flag system
     *  @see FlagManager
     */
    private ConcurrentHashMap<String, Object> meta;
    /**
     * The cached origin plot<br>
     *  - The origin plot is used for plot grouping and relational data
     */
    private Plot origin;
    
    /**
     * Constructor for a new plot<br>
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @see Plot#getPlot(Location) for existing plots
     *
     * @param area
     * @param id
     * @param owner
     */
    public Plot(final PlotArea area, final PlotId id, final UUID owner) {
        this.area = area;
        this.id = id;
        this.owner = owner;
    }
    
    /**
     * Constructor for an unowned plot<br>
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @see Plot#getPlot(Location) for existing plots
     *
     * @param area
     * @param id
     */
    public Plot(final PlotArea area, final PlotId id) {
        this.area = area;
        this.id = id;
    }
    
    /**
     * Constructor for a temporary plot (use -1 for temp)<br>
     * The database will ignore any queries regarding temporary plots.
     * Please note that some bulk plot management functions may still affect temporary plots (TODO: fix this)
     *
     * @see Plot#getPlot(Location) for existing plots
     *
     * @param area
     * @param id
     * @param owner
     * @param temp
     */
    public Plot(final PlotArea area, final PlotId id, final UUID owner, final int temp) {
        this.area = area;
        this.id = id;
        this.owner = owner;
        this.temp = temp;
    }
    
    /**
     * Constructor for a saved plots (Used by the database manager when plots are fetched)
     *
     * @see Plot#getPlot(Location) for existing plots
     *
     * @param id
     * @param owner
     * @param trusted
     * @param denied
     * @param merged
     */
    public Plot(final PlotId id, final UUID owner, final HashSet<UUID> trusted, final HashSet<UUID> members, final HashSet<UUID> denied, final String alias, final BlockLoc position,
    final Collection<Flag> flags, final PlotArea area, final boolean[] merged, final long timestamp, final int temp) {
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
        if (flags != null) {
            for (final Flag flag : flags) {
                this.settings.flags.put(flag.getKey(), flag);
            }
        }
        this.timestamp = timestamp;
        this.temp = temp;
    }

    /**
     * Get a plot from a string e.g. [area];[id]
     * @param defaultArea If no area is specified
     * @param string plot id/area + id
     * @return New or existing plot object
     */
    public static Plot fromString(final PlotArea defaultArea, final String string) {
        final String[] split = string.split(";|,");
        if (split.length == 2) {
            if (defaultArea != null) {
                final PlotId id = PlotId.fromString(split[0] + ";" + split[1]);
                return id != null ? defaultArea.getPlotAbs(id) : null;
            }
        } else if (split.length == 3) {
            final PlotArea pa = PS.get().getPlotArea(split[0], null);
            if (pa != null) {
                final PlotId id = PlotId.fromString(split[1] + ";" + split[2]);
                return pa.getPlotAbs(id);
            }
        } else if (split.length == 4) {
            final PlotArea pa = PS.get().getPlotArea(split[0], split[1]);
            if (pa != null) {
                final PlotId id = PlotId.fromString(split[1] + ";" + split[2]);
                return pa.getPlotAbs(id);
            }
        }
        return null;
    }
    
    /**
     * Return a new/cached plot object at a given location
     *
     * @see PlotPlayer#getCurrentPlot() if a player is expected here.
     *
     * @param loc
     * @return
     */
    public static Plot getPlot(final Location loc) {
        final PlotArea pa = PS.get().getPlotAreaAbs(loc);
        if (pa != null) {
            return pa.getPlot(loc);
        }
        return null;
    }
    
    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     * @see FlagManager
     * @param key
     * @param value
     */
    public void setMeta(final String key, final Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<>();
        }
        this.meta.put(key, value);
    }
    
    /**
     * Get the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     * @param key
     * @return
     */
    public Object getMeta(final String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }
    
    /**
     * Delete the metadata for a key<br>
     *  - metadata is session only
     *  - deleting other plugin's metadata may cause issues
     * @param key
     */
    public void deleteMeta(final String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }
    
    /**
     * Get the cluster this plot is associated with
     * @return the PlotCluster object, or null
     */
    public PlotCluster getCluster() {
        return this.getArea().getCluster(this.id);
    }
    
    /**
     * Efficiently get the players currently inside this plot<br>
     *  - Will return an empty list if no players are in the plot<br>
     *  - Remember, you can cast a PlotPlayer to it's respective implementation (BukkitPlayer, SpongePlayer) to obtain the player object
     * @return list of PlotPlayer(s) or an empty list
     */
    public List<PlotPlayer> getPlayersInPlot() {
        final ArrayList<PlotPlayer> players = new ArrayList<>();
        for (final Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            final PlotPlayer pp = entry.getValue();
            if (this.equals(pp.getCurrentPlot())) {
                players.add(pp);
            }
        }
        return players;
    }
    
    /**
     * Check if the plot has a set owner
     *
     * @return false if there is no owner
     */
    public boolean hasOwner() {
        return this.owner != null;
    }
    
    /**
     * Check if a UUID is a plot owner (merged plots may have multiple owners)
     * @param uuid
     * @return
     */
    public boolean isOwner(final UUID uuid) {
        if (uuid.equals(owner)) {
            return true;
        }
        if (!isMerged()) {
            return false;
        }
        HashSet<Plot> connected = getConnectedPlots();
        for (Plot current : connected) {
            if (uuid.equals(current.owner)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isOwnerAbs(final UUID uuid) {
        return uuid.equals(owner);
    }

    /**
     * Get a list of owner UUIDs for a plot (supports multi-owner mega-plots)
     * @return
     */
    public HashSet<UUID> getOwners() {
        if (owner == null) {
            return new HashSet<>();
        }
        if (isMerged()) {
            HashSet<Plot> plots = getConnectedPlots();
            Plot[] array = plots.toArray(new Plot[plots.size()]);
            final HashSet<UUID> owners = new HashSet<UUID>(1);
            UUID last = owner;
            owners.add(owner);
            for (Plot current : array) {
                if (last == null || current.owner.getMostSignificantBits() != last.getMostSignificantBits()) {
                    owners.add(current.owner);
                    last = current.owner;
                }
            }
            return owners;
        }
        return new HashSet<>(Collections.singletonList(owner));
    }
    
    /**
     * Check if the player is either the owner or on the trusted/added list
     *
     * @param uuid
     *
     * @return true if the player is added/trusted or is the owner
     */
    public boolean isAdded(final UUID uuid) {
        if (owner == null) {
            return false;
        }
        if (getDenied().contains(uuid)) {
            return false;
        }
        if (getTrusted().contains(uuid) || getTrusted().contains(DBFunc.everyone)) {
            return true;
        }
        if (isOwner(uuid)) {
            return true;
        }
        if (getMembers().contains(uuid) || getMembers().contains(DBFunc.everyone)) {
            if (isOnline()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Should the player be denied from entering?
     *
     * @param uuid
     *
     * @return boolean false if the player is allowed to enter
     */
    public boolean isDenied(final UUID uuid) {
        return (this.denied != null) && ((this.denied.contains(DBFunc.everyone) && !this.isAdded(uuid)) || (!this.isAdded(uuid) && this.denied.contains(uuid)));
    }
    
    /**
     * Get the plot ID
     */
    public PlotId getId() {
        return this.id;
    }
    
    /**
     * Get the plot world object for this plot<br>
     *  - The generic PlotArea object can be casted to its respective class for more control (e.g. HybridPlotWorld)
     * @return PlotArea
     */
    public PlotArea getArea() {
        return this.area;
    }

    /**
     * Assign this plot to a plot area.<br>
     * (Mostly used during startup when worlds are being created)<br>
     * Note: Using this when it doesn't make sense will result in strange behavior
     * @param area
     */
    public void setArea(final PlotArea area) {
        if (this.getArea() == area) {
            return;
        }
        if (this.getArea() != null) {
            this.area.removePlot(this.id);
        }
        this.area = area;
        area.addPlot(this);
    }
    
    /**
     * Get the plot manager object for this plot<br>
     *  - The generic PlotManager object can be casted to its respective class for more control (e.g. HybridPlotManager)
     * @return PlotManager
     */
    public PlotManager getManager() {
        return this.area.getPlotManager();
    }
    
    /**
     * Get or create plot settings
     * @return PlotSettings
     * @deprecated use equivalent plot method;
     */
    @Deprecated
    public PlotSettings getSettings() {
        if (this.settings == null) {
            this.settings = new PlotSettings();
        }
        return this.settings;
    }
    
    /**
     * Returns true if the plot is not merged, or it is the base plot of multiple merged plots
     * @return
     */
    public boolean isBasePlot() {
        return !this.isMerged() || this.equals(this.getBasePlot(false));
    }
    
    /**
     * The base plot is an arbitrary but specific connected plot. It is useful for the following:<br>
     *  - Merged plots need to be treated as a single plot for most purposes<br>
     *  - Some data such as home location needs to be associated with the group rather than each plot<br>
     *  - If the plot is not merged it will return itself.<br>
     *  - The result is cached locally
     * @return base Plot
     */
    public Plot getBasePlot(final boolean recalculate) {
        if ((this.origin != null) && !recalculate) {
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
        for (final Plot plot : this.getConnectedPlots()) {
            if ((plot.id.y < min.y) || (plot.id.y == min.y && plot.id.x < min.x)) {
                this.origin = plot;
                min = plot.id;
            }
        }
        for (final Plot plot : this.getConnectedPlots()) {
            plot.origin = this.origin;
        }
        return this.origin;
    }
    
    /**
     * Check if the plot is merged in any direction
     * @return
     */
    public boolean isMerged() {
        return getSettings().getMerged(0) || getSettings().getMerged(2) || getSettings().getMerged(1) || getSettings().getMerged(3);
    }
    
    /**
     * Get the timestamp of when the plot was created (unreliable)<br>
     * - not accurate if the plot was created before this was implemented<br>
     *  - Milliseconds since the epoch<br>
     * @return
     */
    public long getTimestamp() {
        if (this.timestamp == 0) {
            this.timestamp = System.currentTimeMillis();
        }
        return this.timestamp;
    }
    
    /**
     * Get if the plot is merged in a direction<br>
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
     * Note: A plot that is merged north and east will not be merged northeast if the northeast plot is not part of the same group<br>
     * @param direction
     * @return true if merged in that direction
     */
    public boolean getMerged(final int direction) {
        if (settings == null) {
            return false;
        }
        switch (direction) {
            case 0:
            case 1:
            case 2:
            case 3:
                return this.getSettings().getMerged(direction);
            case 7:
                int i = direction - 4;
                int i2 = 0;
                if (this.getSettings().getMerged(i2)) {
                    if (this.getSettings().getMerged(i)) {
                        if (this.area.getPlotAbs(this.id.getRelative(i)).getMerged(i2)) {
                            if (this.area.getPlotAbs(this.id.getRelative(i2)).getMerged(i)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            case 4:
            case 5:
            case 6:
                i = direction - 4;
                i2 = direction - 3;
                return this.getSettings().getMerged(i2)
                        && this.getSettings().getMerged(i)
                        && this.area.getPlotAbs(this.id.getRelative(i)).getMerged(i2)
                        && this.area.getPlotAbs(this.id.getRelative(i2)).getMerged(i);

        }
        return false;
    }

    /**
     * Get the denied users
     * @return
     */
    public HashSet<UUID> getDenied() {
        if (this.denied == null) {
            this.denied = new HashSet<>();
        }
        return this.denied;
    }
    
    /**
     * Set the denied users for this plot
     * @param uuids
     */
    public void setDenied(final Set<UUID> uuids) {
        boolean larger = uuids.size() > getDenied().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? getDenied() : uuids);
        intersection.retainAll(larger ? uuids : getDenied());
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
     * Get the trusted users
     * @return
     */
    public HashSet<UUID> getTrusted() {
        if (this.trusted == null) {
            this.trusted = new HashSet<>();
        }
        return this.trusted;
    }
    
    /**
     * Set the trusted users for this plot
     * @param uuids
     */
    public void setTrusted(final Set<UUID> uuids) {
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
     * Get the members
     * @return
     */
    public HashSet<UUID> getMembers() {
        if (this.members == null) {
            this.members = new HashSet<>();
        }
        return this.members;
    }
    
    /**
     * Set the members for this plot
     * @param uuids
     */
    public void setMembers(final Set<UUID> uuids) {
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
     * Deny someone (updates database as well)
     * @param uuid
     */
    public void addDenied(final UUID uuid) {
        for (Plot current : getConnectedPlots()) {
            if (current.getDenied().add(uuid)) {
                DBFunc.setDenied(current, uuid);
            }
        }
    }
    
    /**
     * Add someone as a helper (updates database as well)
     *
     * @param uuid
     */
    public void addTrusted(final UUID uuid) {
        for (Plot current : getConnectedPlots()) {
            if (current.getTrusted().add(uuid)) {
                DBFunc.setTrusted(current, uuid);
            }
        }
    }
    
    /**
     * Add someone as a trusted user (updates database as well)
     *
     * @param uuid
     */
    public void addMember(final UUID uuid) {
        for (Plot current : getConnectedPlots()) {
            if (current.getMembers().add(uuid)) {
                DBFunc.setMember(current, uuid);
            }
        }
    }
    
    /**
     * Set the plot owner (and update the database)
     * @param owner
     */
    public void setOwner(final UUID owner) {
        if (!hasOwner()) {
            this.owner = owner;
            create();
            return;
        }
        if (!isMerged()) {
            if (!this.owner.equals(owner)) {
                this.owner = owner;
                DBFunc.setOwner(this, owner);
            }
            return;
        }
        for (Plot current : getConnectedPlots()) {
            if (!owner.equals(current.owner)) {
                current.owner = owner;
                DBFunc.setOwner(current, owner);
            }
        }
    }
    
    /**
     * Clear a plot
     * @see this#clear(Runnable)
     * @see this#clear(boolean, boolean, Runnable)
     * @see #deletePlot(Runnable) to clear and delete a plot
     * @param whenDone A runnable to execute when clearing finishes, or null
     */
    public void clear(final Runnable whenDone) {
        this.clear(false, false, whenDone);
    }
    
    public boolean clear(final boolean checkRunning, final boolean isDelete, final Runnable whenDone) {
        if (checkRunning && (this.getRunning() != 0) || !EventUtil.manager.callClear(this)) {
            return false;
        }
        final HashSet<RegionWrapper> regions = this.getRegions();
        final HashSet<Plot> plots = this.getConnectedPlots();
        final ArrayDeque<Plot> queue = new ArrayDeque<>(plots);
        if (isDelete) {
            this.removeSign();
        }
        this.unlinkPlot(true, !isDelete);
        final PlotManager manager = this.area.getPlotManager();
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (queue.isEmpty()) {
                    final AtomicInteger finished = new AtomicInteger(0);
                    final Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            for (final RegionWrapper region : regions) {
                                final Location[] corners = region.getCorners(Plot.this.area.worldname);
                                ChunkManager.manager.clearAllEntities(corners[0], corners[1]);
                            }
                            TaskManager.runTask(whenDone);
                        }
                    };
                    if (isDelete) {
                        for (final Plot current : plots) {
                            manager.unclaimPlot(Plot.this.area, current, new Runnable() {
                                @Override
                                public void run() {
                                    if (finished.incrementAndGet() >= plots.size()) {
                                        run.run();
                                    }
                                }
                            });
                        }
                    } else {
                        for (final Plot current : plots) {
                            manager.claimPlot(Plot.this.area, current);
                        }
                        SetQueue.IMP.addTask(run);
                    }
                    return;
                }
                final Plot current = queue.poll();
                if ((Plot.this.area.TERRAIN != 0) || Settings.FAST_CLEAR) {
                    ChunkManager.manager.regenerateRegion(current.getBottomAbs(), current.getTopAbs(), false, this);
                    return;
                }
                manager.clearPlot(Plot.this.area, current, this);
            }
        };
        run.run();
        return true;
    }
    
    /**
     * Set the biome for a plot asynchronously
     * @param biome The biome e.g. "forest"
     * @param whenDone The task to run when finished, or null
     */
    public void setBiome(final String biome, final Runnable whenDone) {
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(this.getRegions());
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.isEmpty()) {
                    Plot.this.refreshChunks();
                    TaskManager.runTask(whenDone);
                    return;
                }
                final RegionWrapper region = regions.poll();
                final Location pos1 = new Location(Plot.this.area.worldname, region.minX, region.minY, region.minZ);
                final Location pos2 = new Location(Plot.this.area.worldname, region.maxX, region.maxY, region.maxZ);
                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override
                    public void run(final int[] value) {
                        final ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                        ChunkManager.manager.loadChunk(Plot.this.area.worldname, loc, false);
                        MainUtil.setBiome(Plot.this.area.worldname, value[2], value[3], value[4], value[5], biome);
                        ChunkManager.manager.unloadChunk(Plot.this.area.worldname, loc, true, true);
                    }
                }, this, 5);
                
            }
        };
        run.run();
    }
    
    /**
     * Unlink the plot and all connected plots
     * @param createSign
     * @param createRoad
     * @return
     */
    public boolean unlinkPlot(final boolean createRoad, final boolean createSign) {
        if (!this.isMerged()) {
            return false;
        }
        final HashSet<Plot> plots = this.getConnectedPlots();
        final ArrayList<PlotId> ids = new ArrayList<>(plots.size());
        for (final Plot current : plots) {
            current.setHome(null);
            ids.add(current.getId());
        }
        final boolean result = EventUtil.manager.callUnlink(this.area, ids);
        if (!result) {
            return false;
        }
        this.clearRatings();
        if (createSign) {
            this.removeSign();
        }
        final PlotManager manager = this.area.getPlotManager();
        if (createRoad) {
            manager.startPlotUnlink(this.area, ids);
        }
        if ((this.area.TERRAIN != 3) && createRoad) {
            for (final Plot current : plots) {
                if (current.getMerged(1)) {
                    manager.createRoadEast(current.area, current);
                    if (current.getMerged(2)) {
                        manager.createRoadSouth(current.area, current);
                        if (current.getMerged(5)) {
                            manager.createRoadSouthEast(current.area, current);
                        }
                    }
                } else if (current.getMerged(2)) {
                    manager.createRoadSouth(current.area, current);
                }
            }
        }
        for (final Plot current : plots) {
            final boolean[] merged = new boolean[] { false, false, false, false };
            current.setMerged(merged);
            if (createSign) {
                current.setSign(MainUtil.getName(current.owner));
            }
        }
        if (createRoad) {
            manager.finishPlotUnlink(this.area, ids);
        }
        return true;
    }
    
    /**
     * Set the sign for a plot to a specific name
     * @param name
     */
    public void setSign(final String name) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    Plot.this.setSign(name);
                }
            });
            return;
        }
        final PlotManager manager = this.area.getPlotManager();
        if (this.area.ALLOW_SIGNS) {
            final Location loc = manager.getSignLoc(this.area, this);
            final String id = this.id.x + ";" + this.id.y;
            final String[] lines = new String[] {
            C.OWNER_SIGN_LINE_1.formatted().replaceAll("%id%", id),
            C.OWNER_SIGN_LINE_2.formatted().replaceAll("%id%", id).replaceAll("%plr%", name),
            C.OWNER_SIGN_LINE_3.formatted().replaceAll("%id%", id).replaceAll("%plr%", name),
            C.OWNER_SIGN_LINE_4.formatted().replaceAll("%id%", id).replaceAll("%plr%", name) };
            WorldUtil.IMP.setSign(this.area.worldname, loc.getX(), loc.getY(), loc.getZ(), lines);
        }
    }
    
    /**
     * This will return null if the plot hasn't been analyzed
     * @return analysis of plot
     */
    public PlotAnalysis getComplexity() {
        return PlotAnalysis.getAnalysis(this);
    }
    
    public void analyze(final RunnableVal<PlotAnalysis> whenDone) {
        PlotAnalysis.analyzePlot(this, whenDone);
    }
    
    /**
     * Set a flag for this plot
     * @param flag
     * @param value
     */
    public void setFlag(final String flag, final Object value) {
        FlagManager.addPlotFlag(this, new Flag(FlagManager.getFlag(flag), value));
    }
    
    /**
     * Remove a flag from this plot
     * @param flag
     */
    public void removeFlag(final String flag) {
        FlagManager.removePlotFlag(this, flag);
    }
    
    /**
     * Get the flag for a given key
     * @param key
     */
    public Flag getFlag(final String key) {
        return FlagManager.getPlotFlagRaw(this, key);
    }
    
    /**
     * Delete a plot (use null for the runnable if you don't need to be notified on completion)
     * @see PS#removePlot(Plot, boolean)
     * @see #clear(Runnable) to simply clear a plot
     */
    public boolean deletePlot(final Runnable whenDone) {
        if (!this.hasOwner()) {
            return false;
        }
        final HashSet<Plot> plots = this.getConnectedPlots();
        this.clear(false, true, new Runnable() {
            @Override
            public void run() {
                for (final Plot current : plots) {
                    current.unclaim();
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }
    
    /**
     * Count the entities in a plot
     * @see ChunkManager#countEntities(Plot)
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     * @return
     */
    public int[] countEntities() {
        final int[] count = new int[6];
        for (final Plot current : this.getConnectedPlots()) {
            final int[] result = ChunkManager.manager.countEntities(current);
            count[0] += result[0];
            count[1] += result[1];
            count[2] += result[2];
            count[3] += result[3];
            count[4] += result[4];
            count[5] += result[5];
        }
        return count;
    }
    
    /**
     * Returns true if a previous task was running
     * @return
     */
    public int addRunning() {
        final int value = this.getRunning();
        for (final Plot plot : this.getConnectedPlots()) {
            plot.setMeta("running", value + 1);
        }
        return value;
    }

    /**
     * Decrement the number of tracked tasks this plot is running<br>
     *     - Used to track/limit the number of things a player can do on the plot at once
     * @return previous number of tasks (int)
     */
    public int removeRunning() {
        final int value = this.getRunning();
        if (value < 2) {
            for (final Plot plot : this.getConnectedPlots()) {
                plot.deleteMeta("running");
            }
        } else {
            for (final Plot plot : this.getConnectedPlots()) {
                plot.setMeta("running", value - 1);
            }
        }
        return value;
    }

    /**
     * Get the number of tracked running tasks for this plot<br>
     *     - Used to track/limit the number of things a player can do on the plot at once
     * @return number of tasks (int)
     */
    public int getRunning() {
        final Integer value = (Integer) this.getMeta("running");
        return value == null ? 0 : value;
    }

    /**
     * Unclaim the plot (does not modify terrain)<br>
     *      - Changes made to this plot will not be reflected in unclaimed plot objects<br>
     * @return
     */
    public boolean unclaim() {
        if (owner == null) {
            return false;
        }
        for (Plot current : getConnectedPlots()) {
            getArea().removePlot(getId());
            DBFunc.delete(current);
            current.owner = null;
            current.settings = null;
            for (PlotPlayer pp : current.getPlayersInPlot()) {
                PlotListener.plotEntry(pp, current);
            }
        }
        return true;
    }
    
    /**
     * Unlink a plot and remove the roads
     * @see this#unlinkPlot(boolean, boolean)
     * @return true if plot was linked
     */
    public boolean unlink() {
        return this.unlinkPlot(true, true);
    }
    
    public Location getCenter() {
        Location[] corners = getCorners();
        final Location top = corners[0];
        final Location bot = corners[1];
        Location loc = new Location(this.area.worldname, (top.getX() + bot.getX()) / 2, (top.getY() + bot.getY()) / 2, (top.getZ() + bot.getZ()) / 2);
        loc.setY(1 + Math.max(WorldUtil.IMP.getHighestBlock(area.worldname, loc.getX(), loc.getZ()), getManager().getSignLoc(area, this).getY()));
        return loc;
    }
    
    /**
     * Return the home location for the plot
     * @return Home location
     */
    public Location getHome() {
        final BlockLoc home = this.getPosition();
        if ((home == null) || ((home.x == 0) && (home.z == 0))) {
            return this.getDefaultHome();
        } else {
            final Location bot = this.getBottomAbs();
            final Location loc = new Location(bot.getWorld(), bot.getX() + home.x, bot.getY() + home.y, bot.getZ() + home.z, home.yaw, home.pitch);
            if (WorldUtil.IMP.getBlock(loc).id != 0) {
                loc.setY(Math.max(WorldUtil.IMP.getHighestBlock(this.area.worldname, loc.getX(), loc.getZ()), bot.getY()));
            }
            return loc;
        }
    }
    
    /**
     * Set the home location
     * @param loc
     */
    public void setHome(final BlockLoc loc) {
        final Plot plot = this.getBasePlot(false);
        final BlockLoc pos = plot.getSettings().getPosition();
        if (new BlockLoc(0, 0, 0).equals(loc)) {
            return;
        }
        plot.getSettings().setPosition(loc);
        DBFunc.setPosition(plot, plot.getSettings().getPosition().toString());
    }
    
    /**
     * Get the default home location for a plot<br>
     *  - Ignores any home location set for that specific plot
     * @return
     */
    public Location getDefaultHome() {
        final Plot plot = this.getBasePlot(false);
        if (this.area.DEFAULT_HOME != null) {
            final int x;
            final int z;
            if ((this.area.DEFAULT_HOME.x == Integer.MAX_VALUE) && (this.area.DEFAULT_HOME.z == Integer.MAX_VALUE)) {
                // center
                final RegionWrapper largest = plot.getLargestRegion();
                x = ((largest.maxX - largest.minX) / 2) + largest.minX;
                z = ((largest.maxZ - largest.minZ) / 2) + largest.minZ;
            } else {
                // specific
                final Location bot = plot.getBottomAbs();
                x = bot.getX() + this.area.DEFAULT_HOME.x;
                z = bot.getZ() + this.area.DEFAULT_HOME.z;
            }
            final int y = WorldUtil.IMP.getHighestBlock(plot.area.worldname, x, z);
            return new Location(plot.area.worldname, x, y + 1, z);
        }
        // Side
        final RegionWrapper largest = plot.getLargestRegion();
        final int x = ((largest.maxX - largest.minX) / 2) + largest.minX;
        final int z = largest.minZ - 1;
        final PlotManager manager = plot.getManager();
        final int y = Math.max(WorldUtil.IMP.getHighestBlock(plot.area.worldname, x, z), manager.getSignLoc(plot.area, plot).getY());
        return new Location(plot.area.worldname, x, y + 1, z);
    }
    
    /**
     * Get the average rating of the plot. This is the value displayed in /plot info
     * @return average rating as double
     */
    public double getAverageRating() {
        double sum = 0;
        final Collection<Rating> ratings = this.getRatings().values();
        for (final Rating rating : ratings) {
            sum += rating.getAverageRating();
        }
        return sum / ratings.size();
    }
    
    /**
     * Set a rating for a user<br>
     *  - If the user has already rated, the following will return false
     * @param uuid
     * @param rating
     * @return
     */
    public boolean addRating(final UUID uuid, final Rating rating) {
        final Plot base = this.getBasePlot(false);
        final PlotSettings baseSettings = base.getSettings();
        if (baseSettings.getRatings().containsKey(uuid)) {
            return false;
        }
        int aggregate = rating.getAggregate();
        baseSettings.getRatings().put(uuid, aggregate);
        DBFunc.setRating(base, uuid, aggregate);
        return true;
    }
    
    /**
     * Clear the ratings for this plot
     */
    public void clearRatings() {
        final Plot base = this.getBasePlot(false);
        final PlotSettings baseSettings = base.getSettings();
        if ((baseSettings.ratings != null) && !baseSettings.getRatings().isEmpty()) {
            DBFunc.deleteRatings(base);
            baseSettings.ratings = null;
        }
    }
    
    /**
     * Get the ratings associated with a plot<br>
     *  - The rating object may contain multiple categories
     * @return Map of user who rated to the rating
     */
    public HashMap<UUID, Rating> getRatings() {
        final Plot base = this.getBasePlot(false);
        final HashMap<UUID, Rating> map = new HashMap<>();
        if (!base.hasRatings()) {
            return map;
        }
        for (final Entry<UUID, Integer> entry : base.getSettings().getRatings().entrySet()) {
            map.put(entry.getKey(), new Rating(entry.getValue()));
        }
        return map;
    }
    
    public boolean hasRatings() {
        Plot base = this.getBasePlot(false);
        return base.settings != null && base.settings.ratings != null;
    }

    /**
     * Resend all chunks inside the plot to nearby players<br>
     * This should not need to be called
     */
    public void refreshChunks() {
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                final HashSet<ChunkLoc> chunks = new HashSet<>();
                for (final RegionWrapper region : Plot.this.getRegions()) {
                    for (int x = region.minX >> 4; x <= (region.maxX >> 4); x++) {
                        for (int z = region.minZ >> 4; z <= (region.maxZ >> 4); z++) {
                            chunks.add(new ChunkLoc(x, z));
                        }
                    }
                }
                SetQueue.IMP.queue.sendChunk(Plot.this.area.worldname, chunks);
            }
        });
    }
    
    /**
     * Remove the plot sign if it is set
     */
    public void removeSign() {
        final PlotManager manager = this.area.getPlotManager();
        if (!this.area.ALLOW_SIGNS) {
            return;
        }
        final Location loc = manager.getSignLoc(this.area, this);
        SetQueue.IMP.setBlock(this.area.worldname, loc.getX(), loc.getY(), loc.getZ(), 0);
    }
    
    /**
     * Set the plot sign if plot signs are enabled
     */
    public void setSign() {
        if (this.owner == null) {
            this.setSign("unknown");
            return;
        }
        this.setSign(UUIDHandler.getName(this.owner));
    }
    
    /**
     * Register a plot and create it in the database<br>
     *  - The plot will not be created if the owner is null<br>
     *  - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot creation.
     * @return true if plot was created successfully
     */
    public boolean create() {
        return this.create(this.owner, true);
    }

    public boolean claim(final PlotPlayer pp, boolean teleport, String schematic) {
        if (!canClaim(pp)) {
            return false;
        }
        final boolean result = EventUtil.manager.callClaim(pp, this, false);
        if (!result || !create(pp.getUUID(), true)) {
            return false;
        }
        setSign(pp.getName());
        MainUtil.sendMessage(pp, C.CLAIMED);
        if (teleport) {
            teleportPlayer(pp);
        }
        final PlotArea plotworld = getArea();
        if (plotworld.SCHEMATIC_ON_CLAIM) {
            SchematicHandler.Schematic sch;
            if (schematic.isEmpty()) {
                sch = SchematicHandler.manager.getSchematic(plotworld.SCHEMATIC_FILE);
            } else {
                sch = SchematicHandler.manager.getSchematic(schematic);
                if (sch == null) {
                    sch = SchematicHandler.manager.getSchematic(plotworld.SCHEMATIC_FILE);
                }
            }
            SchematicHandler.manager.paste(sch, this, 0, 0, 0, true, new RunnableVal<Boolean>() {
                @Override
                public void run(Boolean value) {
                    if (value) {
                        MainUtil.sendMessage(pp, C.SCHEMATIC_PASTE_SUCCESS);
                    } else {
                        MainUtil.sendMessage(pp, C.SCHEMATIC_PASTE_FAILED);
                    }
                }
            });
        }
        plotworld.getPlotManager().claimPlot(plotworld, this);
        return true;
    }
    
    /**
     * Register a plot and create it in the database<br>
     *  - The plot will not be created if the owner is null<br>
     *  - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot creation.
     * @return true if plot was created successfully
     */
    public boolean create(final UUID uuid, final boolean notify) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        this.owner = uuid;
        final Plot existing = this.area.getOwnedPlotAbs(this.id);
        if (existing != null) {
            throw new IllegalStateException("Plot already exists!");
        }
        if (notify) {
            final Integer meta = (Integer) this.area.getMeta("worldBorder");
            if (meta != null) {
                this.updateWorldBorder();
            }
        }
        connected_cache = null;
        regions_cache = null;
        this.getTrusted().clear();
        this.getMembers().clear();
        this.getDenied().clear();
        this.settings = new PlotSettings();
        if (this.area.addPlot(this)) {
            DBFunc.createPlotAndSettings(this, new Runnable() {
                @Override
                public void run() {
                    final PlotArea plotworld = Plot.this.area;
                    if (notify && plotworld.AUTO_MERGE) {
                        Plot.this.autoMerge(-1, Integer.MAX_VALUE, uuid, true);
                    }
                }
            });
            return true;
        }
        return false;
    }
    
    /**
     * Set components such as border, wall, floor
     *  (components are generator specific)
     */
    public boolean setComponent(final String component, final String blocks) {
        final PlotBlock[] parsed = Configuration.BLOCKLIST.parseString(blocks);
        if ((parsed == null) || (parsed.length == 0)) {
            return false;
        }
        return this.setComponent(component, parsed);
    }
    
    /**
     * Get the biome (String)
     */
    public String getBiome() {
        final Location loc = this.getBottomAbs();
        return WorldUtil.IMP.getBiome(loc.getWorld(), loc.getX(), loc.getZ());
    }
    
    /**
     * Return the top location for the plot
     * @return
     */
    public Location getTopAbs() {
        return this.area.getPlotManager().getPlotTopLocAbs(this.area, this.id);
    }
    
    /**
     * Return the bottom location for the plot
     * @return
     */
    public Location getBottomAbs() {
        return this.area.getPlotManager().getPlotBottomLocAbs(this.area, this.id);
    }
    
    /**
     * Swap the settings for two plots
     * @param p2
     * @param whenDone
     * @return
     */
    public boolean swapData(final Plot p2, final Runnable whenDone) {
        if (this.owner == null) {
            if ((p2 != null) && p2.hasOwner()) {
                p2.moveData(this, whenDone);
                return true;
            }
            return false;
        }
        if ((p2 == null) || (p2.owner == null)) {
            this.moveData(p2, whenDone);
            return true;
        }
        // Swap cached
        final PlotId temp = new PlotId(this.getId().x, this.getId().y);
        this.getId().x = p2.getId().x;
        this.getId().y = p2.getId().y;
        p2.getId().x = temp.x;
        p2.getId().y = temp.y;
        this.area.removePlot(this.getId());
        p2.area.removePlot(p2.getId());
        this.getId().recalculateHash();
        p2.getId().recalculateHash();
        this.area.addPlotAbs(this);
        p2.area.addPlotAbs(p2);
        // Swap database
        DBFunc.dbManager.swapPlots(p2, this);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }
    
    /**
     * Move the settings for a plot
     * @param pos2
     * @param whenDone
     * @return
     */
    public boolean moveData(final Plot pos2, final Runnable whenDone) {
        if (this.owner == null) {
            PS.debug(pos2 + " is unowned (single)");
            TaskManager.runTask(whenDone);
            return false;
        }
        if (pos2.hasOwner()) {
            PS.debug(pos2 + " is unowned (multi)");
            TaskManager.runTask(whenDone);
            return false;
        }
        this.area.removePlot(this.id);
        this.getId().x = pos2.getId().x;
        this.getId().y = pos2.getId().y;
        this.getId().recalculateHash();
        this.area.addPlotAbs(this);
        DBFunc.movePlot(this, pos2);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }
    
    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @return Location top of mega plot
     */
    public Location getExtendedTopAbs() {
        final Location top = this.getTopAbs();
        if (!this.isMerged()) {
            return top;
        }
        if (this.getMerged(2)) {
            top.setZ(this.getRelative(2).getBottomAbs().getZ() - 1);
        }
        if (this.getMerged(1)) {
            top.setX(this.getRelative(1).getBottomAbs().getX() - 1);
        }
        return top;
    }
    
    /**
     * Gets the bottom location for a plot.<br>
     *  - Does not respect mega plots<br>
     *  - Merged plots, only the road will be considered part of the plot<br>
     *
     * @return Location bottom of mega plot
     */
    public Location getExtendedBottomAbs() {
        final Location bot = this.getBottomAbs();
        if (!this.isMerged()) {
            return bot;
        }
        if (this.getMerged(0)) {
            bot.setZ(this.getRelative(0).getTopAbs().getZ() + 1);
        }
        if (this.getMerged(3)) {
            bot.setX(this.getRelative(3).getTopAbs().getX() + 1);
        }
        return bot;
    }
    
    /**
     * Returns the top and bottom location.<br>
     *  - If the plot is not connected, it will return its own corners<br>
     *  - the returned locations will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     * @deprecated as merged plots no longer need to be rectangular
     * @return new Location[] { bottom, top }
     */
    @Deprecated
    public Location[] getCorners() {
        if (!this.isMerged()) {
            return new Location[] { this.getBottomAbs(), this.getTopAbs() };
        }
        return MainUtil.getCorners(this.area.worldname, this.getRegions());
    }
    
    /**
     * Remove the east road section of a plot<br>
     *  - Used when a plot is merged<br>
     */
    public void removeRoadEast() {
        if ((this.area.TYPE != 0) && (this.area.TERRAIN > 1)) {
            if (this.area.TERRAIN == 3) {
                return;
            }
            final Plot other = this.getRelative(1);
            final Location bot = other.getBottomAbs();
            final Location top = this.getTopAbs();
            final Location pos1 = new Location(this.area.worldname, top.getX(), 0, bot.getZ());
            final Location pos2 = new Location(this.area.worldname, bot.getX(), 256, top.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            this.area.getPlotManager().removeRoadEast(this.area, this);
        }
    }
    
    /**
     * Returns the top and bottom plot id.<br>
     *  - If the plot is not connected, it will return itself for the top/bottom<br>
     *  - the returned ids will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     * @deprecated as merged plots no longer need to be rectangular
     * @return new Plot[] { bottom, top }
     */
    @Deprecated
    public PlotId[] getCornerIds() {
        if (!this.isMerged()) {
            return new PlotId[] { this.getId(), this.getId() };
        }
        final PlotId min = new PlotId(this.getId().x, this.getId().y);
        final PlotId max = new PlotId(this.getId().x, this.getId().y);
        for (final Plot current : this.getConnectedPlots()) {
            if (current.getId().x < min.x) {
                min.x = current.getId().x;
            } else if (current.getId().x > max.x) {
                max.x = current.getId().x;
            }
            if (current.getId().y < min.y) {
                min.y = current.getId().y;
            } else if (current.getId().y > max.y) {
                max.y = current.getId().y;
            }
        }
        return new PlotId[] { min, max };
    }
    
    /**
     * @deprecated in favor of getCorners()[0];<br>
     * @return
     */
    @Deprecated
    public Location getBottom() {
        return this.getCorners()[0];
    }
    
    /**
     * @deprecated in favor of getCorners()[1];
     * @return
     */
    @Deprecated
    public Location getTop() {
        return this.getCorners()[1];
    }
    
    /**
     * Swap the plot contents and settings with another location<br>
     *  - The destination must correspond to a valid plot of equal dimensions
     * @see ChunkManager#swap(Location, Location, Location, Location, Runnable) to swap terrain
     * @see this#swapData(Plot, Runnable) to swap plot settings
     * @param destination The other plot to swap with
     * @param whenDone A task to run when finished, or null
     * @see this#swapData(Plot, Runnable)
     * @return boolean if swap was successful
     */
    public boolean swap(final Plot destination, final Runnable whenDone) {
        return this.move(destination, whenDone, true);
    }
    
    /**
     * Move the plot to an empty location<br>
     *  - The location must be empty
     * @param destination Where to move the plot
     * @param whenDone A task to run when done, or null
     * @return if the move was successful
     */
    public boolean move(final Plot destination, final Runnable whenDone) {
        return this.move(destination, whenDone, false);
    }
    
    /**
     * Get plot display name
     *
     * @return alias if set, else id
     */
    @Override
    public String toString() {
        if ((this.settings != null) && (this.settings.getAlias().length() > 1)) {
            return this.settings.getAlias();
        }
        return this.area + ";" + this.id.x + ";" + this.id.y;
    }
    
    /**
     * Remove a denied player (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     * @param uuid
     */
    public boolean removeDenied(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (final UUID other : getDenied()) {
                result = result || rmvDenied(other);
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
     * @param uuid
     */
    public boolean removeTrusted(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (final UUID other : getTrusted()) {
                result = result || rmvTrusted(other);
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
     * @param uuid
     */
    public boolean removeMember(final UUID uuid) {
        if (this.members == null) {
            return false;
        }
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (final UUID other : new HashSet<>(this.members)) {
                result = result || rmvMember(other);
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

    /**
     * Export the plot as a schematic to the configured output directory
     * @return
     */
    public void export(final RunnableVal<Boolean> whenDone) {
        SchematicHandler.manager.getCompoundTag(this, new RunnableVal<CompoundTag>() {
            @Override
            public void run(final CompoundTag value) {
                if (value == null) {
                    if (whenDone != null) {
                        whenDone.value = false;
                        TaskManager.runTask(whenDone);
                    }
                } else {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            final String name = Plot.this.id + "," + Plot.this.area + "," + MainUtil.getName(Plot.this.owner);
                            final boolean result = SchematicHandler.manager.save(value, Settings.SCHEMATIC_SAVE_PATH + File.separator + name + ".schematic");
                            if (whenDone != null) {
                                whenDone.value = result;
                                TaskManager.runTask(whenDone);
                            }
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Export the plot as a BO3 object<br>
     *  - bedrock, floor and main block are ignored in their respective sections
     *  - air is ignored
     *  - The center is considered to be on top of the plot in the center
     * @param whenDone value will be false if exporting fails
     */
    public void exportBO3(final RunnableVal<Boolean> whenDone) {
        final boolean result = BO3Handler.saveBO3(this);
        if (whenDone != null) {
            whenDone.value = result;
        }
        TaskManager.runTask(whenDone);
    }
    
    /**
     * Upload the plot as a schematic to the configured web interface<br>
     * @param whenDone value will be null if uploading fails
     */
    public void upload(final RunnableVal<URL> whenDone) {
        SchematicHandler.manager.getCompoundTag(this, new RunnableVal<CompoundTag>() {
            @Override
            public void run(final CompoundTag value) {
                SchematicHandler.manager.upload(value, null, null, whenDone);
            }
        });
    }

    /**
     * Upload this plot as a world file<br>
     *  - The mca files are each 512x512, so depending on the plot size it may also download adjacent plots<br>
     *  - Works best when (plot width + road width) % 512 == 0<br>
     *  @see com.intellectualcrafters.plot.util.WorldUtil
     * @param whenDone
     */
    public void uploadWorld(RunnableVal<URL> whenDone) {
        WorldUtil.IMP.upload(this, null, null, whenDone);
    }

    /**
     * Upload this plot as a BO3<br>
     *     - May not work on non default generator<br>
     *     - BO3 includes flags/ignores plot main/floor block<br>
     * @see com.intellectualcrafters.plot.util.BO3Handler
     * @param whenDone
     */
    public void uploadBO3(RunnableVal<URL> whenDone) {
        BO3Handler.upload(this, null, null, whenDone);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Plot other = (Plot) obj;
        return (this.hashCode() == other.hashCode()) && this.id.equals(other.id) && (this.area == other.area);
    }
    
    /**
     * Get the plot hashcode<br>
     * Note: The hashcode is unique if:<br>
     *     - Plots are in the same world<br>
     *     - The x,z coordinates are between Short.MIN_VALUE and Short.MAX_VALUE<br>
     * @return integer.
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    /**
     * Get the flags specific to this plot<br>
     *  - Does not take default flags into account<br>
     * @return
     */
    public HashMap<String, Flag> getFlags() {
        if (this.settings == null) {
            return new HashMap<>(0);
        }
        return this.settings.flags;
    }
    
    /**
     * Set a flag for this plot
     * @param flags
     */
    public void setFlags(final Set<Flag> flags) {
        FlagManager.setPlotFlags(this, flags);
    }
    
    /**
     * Get the plot Alias<br>
     *  - Returns an empty string if no alias is set
     * @return
     */
    public String getAlias() {
        if (this.settings == null) {
            return "";
        }
        return this.settings.getAlias();
    }
    
    /**
     * Set the plot alias
     * @param alias
     */
    public void setAlias(String alias) {
        for (final Plot current : this.getConnectedPlots()) {
            final String name = this.getSettings().getAlias();
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
     * Set the raw merge data<br>
     *  - Updates DB<br>
     *  - Does not modify terrain<br>
     * ----------<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * ----------<br>
     * @param direction
     * @param value
     */
    public void setMerged(final int direction, final boolean value) {
        if (this.getSettings().setMerged(direction, value)) {
            if (value) {
                final Plot other = this.getRelative(direction).getBasePlot(false);
                if (!other.equals(this.getBasePlot(false))) {
                    final Plot base = (other.id.y < this.id.y) || (other.id.y == this.id.y && (other.id.x < this.id.x)) ? other : this.origin;
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
     * Get the merged array
     * @return boolean [ north, east, south, west ]
     */
    public boolean[] getMerged() {
        return this.getSettings().getMerged();
    }
    
    /**
     * Set the raw merge data<br>
     *  - Updates DB<br>
     *  - Does not modify terrain<br>
     * Get if the plot is merged in a direction<br>
     * ----------<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * ----------<br>
     * Note: Diagonal merging (4-7) must be done by merging the corresponding plots.
     * @param merged
     */
    public void setMerged(final boolean[] merged) {
        this.getSettings().setMerged(merged);
        DBFunc.setMerged(this, merged);
        connected_cache = null;
        regions_cache = null;
        if (this.origin != null) {
            this.origin.origin = null;
            this.origin = null;
        }
    }
    
    /**
     * Get the set home location or 0,0,0 if no location is set<br>
     *  - Does not take the default home location into account
     * @see #getHome()
     * @return
     */
    public BlockLoc getPosition() {
        return this.getSettings().getPosition();
    }

    /**
     * Check if a plot can be claimed
     * @param player
     * @return
     */
    public boolean canClaim(final PlotPlayer player) {
        if (Settings.ENABLE_CLUSTERS) {
            final PlotCluster cluster = this.getCluster();
            if (cluster != null) {
                if (!cluster.isAdded(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.claim")) {
                    return false;
                }
            }
        }
        return this.guessOwner() == null && !isMerged();
    }

    /**
     * Guess the owner of a plot either by the value in memory, or the sign data<br>
     * Note: Recovering from sign information is useful if e.g. PlotMe conversion wasn't successful
     * @return UUID
     */
    public UUID guessOwner() {
        if (this.hasOwner()) {
            return this.owner;
        }
        if (!this.area.ALLOW_SIGNS) {
            return null;
        }
        try {
            final Location loc = this.getManager().getSignLoc(this.area, this);
            ChunkManager.manager.loadChunk(loc.getWorld(), loc.getChunkLoc(), false);
            final String[] lines = WorldUtil.IMP.getSign(loc);
            if (lines == null) {
                return null;
            }
            loop: for (int i = 4; i > 0; i--) {
                final String caption = C.valueOf("OWNER_SIGN_LINE_" + i).s();
                final int index = caption.indexOf("%plr%");
                if (index == -1) {
                    continue;
                } else if (index < -1) {
                    PS.debug("This should NEVER happen. Seriously, it's impossible.");
                }
                String line = lines[i-1];
                if (line.length() <= index) {
                    return null;
                }
                final String name = line.substring(index);
                if (name.isEmpty()) {
                    return null;
                }
                final UUID owner = UUIDHandler.getUUID(name, null);
                if (owner != null) {
                    this.owner = owner;
                    break;
                }
                if (lines[i - 1].length() == 15) {
                    final BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                    for (final Entry<StringWrapper, UUID> entry : map.entrySet()) {
                        final String key = entry.getKey().value;
                        if ((key.length() > name.length()) && key.startsWith(name)) {
                            this.owner = entry.getValue();
                            break loop;
                        }
                    }
                }
                this.owner = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                break;
            }
            if (this.hasOwner()) {
                this.create();
            }
            return this.owner;
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Remove the south road section of a plot<br>
     *  - Used when a plot is merged<br>
     */
    public void removeRoadSouth() {
        if ((this.area.TYPE != 0) && (this.area.TERRAIN > 1)) {
            if (this.area.TERRAIN == 3) {
                return;
            }
            final Plot other = this.getRelative(2);
            final Location bot = other.getBottomAbs();
            final Location top = this.getTopAbs();
            final Location pos1 = new Location(this.area.worldname, bot.getX(), 0, top.getZ());
            final Location pos2 = new Location(this.area.worldname, top.getX(), 256, bot.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            this.getManager().removeRoadSouth(this.area, this);
        }
    }
    
    /**
     * Auto merge a plot in a specific direction<br>
     * @param dir The direction to merge<br>
     * -1 = All directions<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * @param max The max number of merges to do
     * @param uuid The UUID it is allowed to merge with
     * @param removeRoads Whether to remove roads
     * @return true if a merge takes place
     */
    public boolean autoMerge(final int dir, int max, final UUID uuid, final boolean removeRoads) {
        if (this.owner == null) {
            return false;
        }
        final HashSet<Plot> visited = new HashSet<>();
        final HashSet<PlotId> merged = new HashSet<>();
        final HashSet<Plot> connected = this.getConnectedPlots();
        for (final Plot current : connected) {
            merged.add(current.getId());
        }
        final ArrayDeque<Plot> frontier = new ArrayDeque<>(connected);
        Plot current;
        boolean toReturn = false;
        while (((current = frontier.poll()) != null) && (max >= 0)) {
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            Set<Plot> plots;
            if ((dir == -1 || (dir == 0)) && !current.getMerged(0)) {
                final Plot other = current.getRelative(0);
                if ((other != null)
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || (((plots = other.getConnectedPlots()).size() <= max) && frontier.addAll(plots) && ((max -= plots.size()) != -1)))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
            if ((max >= 0) && ((dir == -1) || (dir == 1)) && !current.getMerged(1)) {
                final Plot other = current.getRelative(1);
                if ((other != null)
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || (((plots = other.getConnectedPlots()).size() <= max) && frontier.addAll(plots) && ((max -= plots.size()) != -1)))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
            if ((max >= 0) && ((dir == -1) || (dir == 2)) && !current.getMerged(2)) {
                final Plot other = current.getRelative(2);
                if ((other != null)
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || (((plots = other.getConnectedPlots()).size() <= max) && frontier.addAll(plots) && ((max -= plots.size()) != -1)))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
            if ((max >= 0) && ((dir == -1) || (dir == 3)) && !current.getMerged(3)) {
                final Plot other = current.getRelative(3);
                if ((other != null)
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || (((plots = other.getConnectedPlots()).size() <= max) && frontier.addAll(plots) && ((max -= plots.size()) != -1)))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
        }
        if (removeRoads && toReturn) {
            final ArrayList<PlotId> ids = new ArrayList<>(merged);
            this.getManager().finishPlotMerge(this.area, ids);
        }
        return toReturn;
    }
    
    /**
     * Merge the plot settings<br>
     *  - Used when a plot is merged<br>
     * @param b
     */
    public void mergeData(final Plot b) {
        final HashMap<String, Flag> flags1 = this.getFlags();
        final HashMap<String, Flag> flags2 = b.getFlags();
        if ((!flags1.isEmpty() || !flags2.isEmpty()) && !flags1.equals(flags2)) {
            final boolean greater = flags1.size() > flags2.size();
            if (greater) {
                flags1.putAll(flags2);
            } else {
                flags2.putAll(flags1);
            }
            final HashSet<Flag> net = new HashSet<>((greater ? flags1 : flags2).values());
            this.setFlags(net);
            b.setFlags(net);
        }
        if (!this.getAlias().isEmpty()) {
            b.setAlias(this.getAlias());
        } else if (!b.getAlias().isEmpty()) {
            this.setAlias(b.getAlias());
        }
        for (final UUID uuid : this.getTrusted()) {
            b.addTrusted(uuid);
        }
        for (final UUID uuid : b.getTrusted()) {
            this.addTrusted(uuid);
        }
        for (final UUID uuid : this.getMembers()) {
            b.addMember(uuid);
        }
        for (final UUID uuid : b.getMembers()) {
            this.addMember(uuid);
        }
        
        for (final UUID uuid : this.getDenied()) {
            b.addDenied(uuid);
        }
        for (final UUID uuid : b.getDenied()) {
            this.addDenied(uuid);
        }
    }

    /**
     * Remove the SE road (only effects terrain)
     */
    public void removeRoadSouthEast() {
        if ((this.area.TYPE != 0) && (this.area.TERRAIN > 1)) {
            if (this.area.TERRAIN == 3) {
                return;
            }
            final Plot other = this.getRelative(1, 1);
            final Location pos1 = this.getTopAbs().add(1, 0, 1);
            final Location pos2 = other.getBottomAbs().subtract(1, 0, 1);
            pos1.setY(0);
            pos2.setY(256);
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            this.area.getPlotManager().removeRoadSouthEast(this.area, this);
        }
    }

    /**
     * Get the plot in a relative location<br>
     * Note: May be null if the partial plot area does not include the relative location
     * @param x
     * @param y
     * @return Plot
     */
    public Plot getRelative(final int x, final int y) {
        return this.area.getPlotAbs(this.id.getRelative(x, y));
    }

    /**
     * Get the plot in a relative direction<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * Note: May be null if the partial plot area does not include the relative location
     * @param direction
     * @return
     */
    public Plot getRelative(final int direction) {
        return this.area.getPlotAbs(this.id.getRelative(direction));
    }
    
    /**
     * Get a set of plots connected (and including) this plot<br>
     *  - This result is cached globally
     * @return
     */
    public HashSet<Plot> getConnectedPlots() {
        if (this.settings == null) {
            return new HashSet<>(Collections.singletonList(this));
        }
        boolean[] merged = this.getMerged();
        final int hash = MainUtil.hash(merged);
        if (hash == 0) {
            return new HashSet<>(Collections.singletonList(this));
        }
        if ((connected_cache != null) && connected_cache.contains(this)) {
            return connected_cache;
        }
        regions_cache = null;
        connected_cache = new HashSet<>();
        final ArrayDeque<Plot> frontier = new ArrayDeque<>();
        final HashSet<Object> queuecache = new HashSet<>();
        connected_cache.add(this);
        Plot tmp;
        if (merged[0]) {
            tmp = this.area.getPlotAbs(this.id.getRelative(0));
            if (!tmp.getMerged(2)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(owner)) {
                    tmp.getSettings().setMerged(2, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(0, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[1]) {
            tmp = this.area.getPlotAbs(this.id.getRelative(1));
            if (!tmp.getMerged(3)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(owner)) {
                    tmp.getSettings().setMerged(3, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(1, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[2]) {
            tmp = this.area.getPlotAbs(this.id.getRelative(2));
            if (!tmp.getMerged(0)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(owner)) {
                    tmp.getSettings().setMerged(0, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(2, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[3]) {
            tmp = this.area.getPlotAbs(this.id.getRelative(3));
            if (!tmp.getMerged(1)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.isOwnerAbs(owner)) {
                    tmp.getSettings().setMerged(1, true);
                    DBFunc.setMerged(tmp, tmp.getSettings().getMerged());
                } else {
                    this.getSettings().setMerged(3, false);
                    DBFunc.setMerged(this, this.getSettings().getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        Plot current;
        while ((current = frontier.poll()) != null) {
            if ((current.owner == null) || (current.settings == null)) {
                // Invalid plot
                // merged onto unclaimed plot
                PS.debug("Ignoring invalid merged plot: " + current + " | " + current.owner);
                continue;
            }
            connected_cache.add(current);
            queuecache.remove(current);
            merged = current.getMerged();
            if (merged[0]) {
                tmp = current.area.getPlotAbs(current.id.getRelative(0));
                if ((tmp != null) && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[1]) {
                tmp = current.area.getPlotAbs(current.id.getRelative(1));
                if ((tmp != null) && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[2]) {
                tmp = current.area.getPlotAbs(current.id.getRelative(2));
                if ((tmp != null) && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[3]) {
                tmp = current.area.getPlotAbs(current.id.getRelative(3));
                if ((tmp != null) && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
        }
        return connected_cache;
    }
    
    /**
     * This will combine each plot into effective rectangular regions<br>
     *  - This result is cached globally<br>
     *  - Useful for handling non rectangular shapes
     * @return
     */
    public HashSet<RegionWrapper> getRegions() {
        if ((regions_cache != null) && (connected_cache != null) && connected_cache.contains(this)) {
            return regions_cache;
        }
        if (!this.isMerged()) {
            final Location pos1 = this.getBottomAbs();
            final Location pos2 = this.getTopAbs();
            connected_cache = new HashSet<>(Collections.singletonList(this));
            regions_cache = new HashSet<>(1);
            regions_cache.add(new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getY(), pos2.getY(), pos1.getZ(), pos2.getZ()));
            return regions_cache;
        }
        final HashSet<Plot> plots = this.getConnectedPlots();
        regions_cache = new HashSet<>();
        final HashSet<PlotId> visited = new HashSet<>();
        for (final Plot current : plots) {
            if (visited.contains(current.getId())) {
                continue;
            }
            boolean merge = true;
            final PlotId bot = new PlotId(current.getId().x, current.getId().y);
            final PlotId top = new PlotId(current.getId().x, current.getId().y);
            while (merge) {
                merge = false;
                ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x, bot.y - 1), new PlotId(top.x, bot.y - 1));
                boolean tmp = true;
                for (final PlotId id : ids) {
                    final Plot plot = this.area.getPlotAbs(id);
                    if ((plot == null) || !plot.getMerged(2) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.y--;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(top.x + 1, bot.y), new PlotId(top.x + 1, top.y));
                tmp = true;
                for (final PlotId id : ids) {
                    final Plot plot = this.area.getPlotAbs(id);
                    if ((plot == null) || !plot.getMerged(3) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.x++;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x, top.y + 1), new PlotId(top.x, top.y + 1));
                tmp = true;
                for (final PlotId id : ids) {
                    final Plot plot = this.area.getPlotAbs(id);
                    if ((plot == null) || !plot.getMerged(0) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.y++;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x - 1, bot.y), new PlotId(bot.x - 1, top.y));
                tmp = true;
                for (final PlotId id : ids) {
                    final Plot plot = this.area.getPlotAbs(id);
                    if ((plot == null) || !plot.getMerged(1) || visited.contains(plot.getId())) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.x--;
                }
            }
            final Location gtopabs = this.area.getPlotAbs(top).getTopAbs();
            final Location gbotabs = this.area.getPlotAbs(bot).getBottomAbs();
            for (final PlotId id : MainUtil.getPlotSelectionIds(bot, top)) {
                visited.add(id);
            }
            for (int x = bot.x; x <= top.x; x++) {
                final Plot plot = this.area.getPlotAbs(new PlotId(x, top.y));
                if (plot.getMerged(2)) {
                    // south wedge
                    final Location toploc = plot.getExtendedTopAbs();
                    final Location botabs = plot.getBottomAbs();
                    final Location topabs = plot.getTopAbs();
                    regions_cache.add(new RegionWrapper(botabs.getX(), topabs.getX(), topabs.getZ() + 1, toploc.getZ()));
                    if (plot.getMerged(5)) {
                        regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), topabs.getZ() + 1, toploc.getZ()));
                        // intersection
                    }
                }
            }
            
            for (int y = bot.y; y <= top.y; y++) {
                final Plot plot = this.area.getPlotAbs(new PlotId(top.x, y));
                if (plot.getMerged(1)) {
                    // east wedge
                    final Location toploc = plot.getExtendedTopAbs();
                    final Location botabs = plot.getBottomAbs();
                    final Location topabs = plot.getTopAbs();
                    regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), botabs.getZ(), topabs.getZ()));
                    if (plot.getMerged(5)) {
                        regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), topabs.getZ() + 1, toploc.getZ()));
                        // intersection
                    }
                }
            }
            regions_cache.add(new RegionWrapper(gbotabs.getX(), gtopabs.getX(), gbotabs.getZ(), gtopabs.getZ()));
        }
        return regions_cache;
    }
    
    /**
     * Attempt to find the largest rectangular region in a plot (as plots can form non rectangular shapes)
     * @return
     */
    public RegionWrapper getLargestRegion() {
        final HashSet<RegionWrapper> regions = this.getRegions();
        RegionWrapper max = null;
        int area = 0;
        for (final RegionWrapper region : regions) {
            final int current = ((region.maxX - region.minX) + 1) * ((region.maxZ - region.minZ) + 1);
            if (current > area) {
                max = region;
                area = current;
            }
        }
        return max;
    }

    /**
     * Do the plot entry tasks for each player in the plot<br>
     *  - Usually called when the plot state changes (unclaimed/claimed/flag change etc)
     */
    public void reEnter() {
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                for (final PlotPlayer pp : Plot.this.getPlayersInPlot()) {
                    PlotListener.plotExit(pp, Plot.this);
                    PlotListener.plotEntry(pp, Plot.this);
                }
            }
        }, 1);
    }
    
    /**
     * Get all the corners of the plot (supports non-rectangular shapes)<br>
     * @return
     */
    public List<Location> getAllCorners() {
        final Area area = new Area();
        for (final RegionWrapper region : this.getRegions()) {
            final Area rectArea = new Area(new Rectangle(region.minX, region.minZ, region.maxX - region.minX + 1, region.maxZ - region.minZ + 1));
            area.add(rectArea);
        }
        final List<Location> locs = new ArrayList<>();
        double[] coords = new double[6];
        for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
            int type = pi.currentSegment(coords);
            final int x = (int) coords[0];
            final int z = (int) coords[1];
            if (type != 4) {
                locs.add(new Location(this.area.worldname, x, 0, z));
            }
        }
        return locs;
    }
    
    /**
     * Teleport a player to a plot and send them the teleport message.
     * @param player
     * @return If the teleportation is allowed.
     */
    public boolean teleportPlayer(final PlotPlayer player) {
        final Plot plot = this.getBasePlot(false);
        final boolean result = EventUtil.manager.callTeleport(player, player.getLocation(), plot);
        if (result) {
            final Location location;
            if (this.area.HOME_ALLOW_NONMEMBER || plot.isAdded(player.getUUID())) {
                location = this.getHome();
            } else {
                location = this.getDefaultHome();
            }
            if ((Settings.TELEPORT_DELAY == 0) || Permissions.hasPermission(player, "plots.teleport.delay.bypass")) {
                MainUtil.sendMessage(player, C.TELEPORTED_TO_PLOT);
                player.teleport(location);
                return true;
            }
            MainUtil.sendMessage(player, C.TELEPORT_IN_SECONDS, Settings.TELEPORT_DELAY + "");
            final String name = player.getName();
            TaskManager.TELEPORT_QUEUE.add(name);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if (!TaskManager.TELEPORT_QUEUE.contains(name)) {
                        MainUtil.sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    TaskManager.TELEPORT_QUEUE.remove(name);
                    if (!player.isOnline()) {
                        return;
                    }
                    MainUtil.sendMessage(player, C.TELEPORTED_TO_PLOT);
                    player.teleport(location);
                }
            }, Settings.TELEPORT_DELAY * 20);
            return true;
        }
        return result;
    }
    
    public boolean isOnline() {
        if (owner == null) {
            return false;
        }
        if (!isMerged()) {
            return UUIDHandler.getPlayer(owner) != null;
        }
        for (Plot current : getConnectedPlots()) {
            if (current.hasOwner() && UUIDHandler.getPlayer(current.owner) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set a component for a plot to the provided blocks<br>
     *  - E.g. floor, wall, border etc.<br>
     *  - The available components depend on the generator being used<br>
     * @param component
     * @param blocks
     * @return
     */
    public boolean setComponent(final String component, final PlotBlock[] blocks) {
        return this.getManager().setComponent(this.area, this.getId(), component, blocks);
    }
    
    /**
     * Expand the world border to include the provided plot (if applicable)
     */
    public void updateWorldBorder() {
        if (this.owner == null) {
            return;
        }
        final int border = this.area.getBorder();
        if (border == Integer.MAX_VALUE) {
            return;
        }
        final PlotId id = new PlotId(Math.abs(this.getId().x) + 1, Math.abs(this.getId().x) + 1);
        final PlotManager manager = this.getManager();
        final Location bot = manager.getPlotBottomLocAbs(this.area, id);
        final Location top = manager.getPlotTopLocAbs(this.area, id);
        final int botmax = Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ()));
        final int topmax = Math.max(Math.abs(top.getX()), Math.abs(top.getZ()));
        final int max = Math.max(botmax, topmax);
        if (max > border) {
            this.area.setMeta("worldBorder", max);
        }
    }
    
    /**
     * Merges 2 plots Removes the road inbetween <br>- Assumes plots are directly next to each other <br> - saves to DB
     *
     * @param lesserPlot
     * @param removeRoads
     */
    public void mergePlot(Plot lesserPlot, final boolean removeRoads) {
        Plot greaterPlot = this;
        if (lesserPlot.getId().x == greaterPlot.getId().x) {
            if (lesserPlot.getId().y > greaterPlot.getId().y) {
                final Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.getMerged(2)) {
                lesserPlot.clearRatings();
                greaterPlot.clearRatings();
                lesserPlot.setMerged(2, true);
                greaterPlot.setMerged(0, true);
                lesserPlot.mergeData(greaterPlot);
                if (removeRoads) {
                    if (lesserPlot.getMerged(5)) {
                        lesserPlot.removeRoadSouthEast();
                    }
                    lesserPlot.removeRoadSouth();
                    final Plot other = this.getRelative(3);
                    if (other.getMerged(2) && other.getMerged(1)) {
                        other.removeRoadEast();
                        greaterPlot.mergePlot(other, removeRoads);
                    }
                }
            }
        } else {
            if (lesserPlot.getId().x > greaterPlot.getId().x) {
                final Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.getMerged(1)) {
                lesserPlot.clearRatings();
                greaterPlot.clearRatings();
                lesserPlot.setMerged(1, true);
                greaterPlot.setMerged(3, true);
                lesserPlot.mergeData(greaterPlot);
                if (removeRoads) {
                    lesserPlot.removeRoadEast();
                    if (lesserPlot.getMerged(5)) {
                        lesserPlot.removeRoadSouthEast();
                    }
                    final Plot other = lesserPlot.getRelative(0);
                    if (other.getMerged(2) && other.getMerged(1)) {
                        other.removeRoadSouthEast();
                        greaterPlot.mergePlot(other, removeRoads);
                    }
                }
            }
        }
    }
    
    /**
     * Move a plot physically, as well as the corresponding settings.
     * @param destination
     * @param whenDone
     * @param allowSwap
     * @return
     */
    public boolean move(final Plot destination, final Runnable whenDone, final boolean allowSwap) {
        final PlotId offset = new PlotId(destination.getId().x - this.getId().x, destination.getId().y - this.getId().y);
        final Location db = destination.getBottomAbs();
        final Location ob = this.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (this.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        boolean occupied = false;
        final HashSet<Plot> plots = this.getConnectedPlots();
        for (final Plot plot : plots) {
            final Plot other = plot.getRelative(offset.x, offset.y);
            if (other.hasOwner()) {
                if (!allowSwap) {
                    TaskManager.runTaskLater(whenDone, 1);
                    return false;
                }
                occupied = true;
            }
        }
        // world border
        destination.updateWorldBorder();
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(this.getRegions());
        // move / swap data
        for (final Plot plot : plots) {
            final Plot other = plot.getRelative(offset.x, offset.y);
            plot.swapData(other, null);
        }
        // copy terrain
        final Runnable move = new Runnable() {
            @Override
            public void run() {
                if (regions.isEmpty()) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                final Runnable task = this;
                final RegionWrapper region = regions.poll();
                final Location[] corners = region.getCorners(Plot.this.area.worldname);
                final Location pos1 = corners[0];
                final Location pos2 = corners[1];
                final Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.area.worldname);
                ChunkManager.manager.regenerateRegion(pos1, pos2, false, task);
            }
        };
        final Runnable swap = new Runnable() {
            @Override
            public void run() {
                if (regions.isEmpty()) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                final RegionWrapper region = regions.poll();
                final Location[] corners = region.getCorners(Plot.this.area.worldname);
                final Location pos1 = corners[0];
                final Location pos2 = corners[1];
                final Location pos3 = pos1.clone().add(offsetX, 0, offsetZ);
                final Location pos4 = pos2.clone().add(offsetX, 0, offsetZ);
                pos3.setWorld(destination.area.worldname);
                pos4.setWorld(destination.area.worldname);
                ChunkManager.manager.swap(pos1, pos2, pos3, pos4, this);
            }
        };
        if (occupied) {
            swap.run();
        } else {
            move.run();
        }
        return true;
    }
    
    /**
     * Copy a plot to a location, both physically and the settings
     * @param destination
     * @param whenDone
     * @return
     */
    public boolean copy(final Plot destination, final Runnable whenDone) {
        final PlotId offset = new PlotId(destination.getId().x - this.getId().x, destination.getId().y - this.getId().y);
        final Location db = destination.getBottomAbs();
        final Location ob = this.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (this.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        final HashSet<Plot> plots = this.getConnectedPlots();
        for (final Plot plot : plots) {
            final Plot other = plot.getRelative(offset.x, offset.y);
            if (other.hasOwner()) {
                TaskManager.runTaskLater(whenDone, 1);
                return false;
            }
        }
        // world border
        destination.updateWorldBorder();
        // copy data
        for (final Plot plot : plots) {
            final Plot other = plot.getRelative(offset.x, offset.y);
            other.create(plot.owner, false);
            if (!plot.getFlags().isEmpty()) {
                other.getSettings().flags = plot.getFlags();
                DBFunc.setFlags(other, plot.getFlags().values());
            }
            if (plot.isMerged()) {
                other.setMerged(plot.getMerged());
            }
            if ((plot.members != null) && !plot.members.isEmpty()) {
                other.members = plot.members;
                for (final UUID member : plot.members) {
                    DBFunc.setMember(other, member);
                }
            }
            if ((plot.trusted != null) && !plot.trusted.isEmpty()) {
                other.trusted = plot.trusted;
                for (final UUID trusted : plot.trusted) {
                    DBFunc.setTrusted(other, trusted);
                }
            }
            if ((plot.denied != null) && !plot.denied.isEmpty()) {
                other.denied = plot.denied;
                for (final UUID denied : plot.denied) {
                    DBFunc.setDenied(other, denied);
                }
            }
        }
        // copy terrain
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(this.getRegions());
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.isEmpty()) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                final RegionWrapper region = regions.poll();
                final Location[] corners = region.getCorners(Plot.this.area.worldname);
                final Location pos1 = corners[0];
                final Location pos2 = corners[1];
                final Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.area.worldname);
                ChunkManager.manager.copyRegion(pos1, pos2, newPos, this);
            }
        };
        run.run();
        return true;
    }
}
