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

/**
 * The plot class
 */
@SuppressWarnings("javadoc")
public class Plot {
    /**
     * plot ID
     * Direct access is Deprecated: use getId()
     */
    @Deprecated
    public final PlotId id;
    /**
     * plot world
     * Direct access is Deprecated: use getWorld()
     */
    @Deprecated
    public PlotArea area;
    /**
     * plot owner
     * (Merged plots can have multiple owners)
     * Direct access is Deprecated: use getOwners()
     */
    @Deprecated
    public UUID owner;
    
    /**
     * Plot creation timestamp (not accurate if the plot was created before this was implemented)<br>
     *  - Milliseconds since the epoch<br>
     * Direct access is Deprecated: use {@link #getTimestamp() getTimestamp}
     */
    @Deprecated
    public long timestamp;
    
    /**
     * List of trusted (with plot permissions)
     * Direct access is Deprecated: use getTrusted()
     */
    @Deprecated
    public HashSet<UUID> trusted;
    /**
     * List of members users (with plot permissions)
     * Direct access is Deprecated: use getMembers()
     */
    @Deprecated
    public HashSet<UUID> members;
    /**
     * List of denied players
     * Direct access is Deprecated: use getDenied()
     */
    @Deprecated
    public HashSet<UUID> denied;
    /**
     * External settings class<br>
     *  - Please favor the methods over direct access to this class<br>
     *  - The methods are more likely to be left unchanged from version changes<br>
     *  Direct access is Deprecated: use getSettings()
     */
    @Deprecated
    public PlotSettings settings;
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
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     *  For persistent metadata use the flag system
     *  @see FlagManager
     */
    private ConcurrentHashMap<String, Object> meta;
    
    /**
     * Constructor for a new plot<br>
     * (Only changes after plot.create() will be properly set in the database)
     *
     * @see Plot#getPlot(String, PlotId) for existing plots
     * @see Plot#getPlot(Location) for existing plots
     *
     * @param world
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
     * @see Plot#getPlot(String, PlotId) for existing plots
     *
     * @param world
     * @param id
     */
    public Plot(final PlotArea area, final PlotId id) {
        this.area = area;
        this.id = id;
    }
    
    public static Plot fromString(PlotArea defaultArea, String string) {
        final String[] split = string.split(";|,");
        if (split.length == 2) {
            if (defaultArea != null) {
                PlotId id = PlotId.fromString(split[0] + ";" + split[1]);
                return id != null ? defaultArea.getPlotAbs(id) : null;
            }
        } else if (split.length == 3) {
            PlotArea pa = PS.get().getPlotArea(split[0], null);
            if (pa != null) {
                PlotId id = PlotId.fromString(split[1] + ";" + split[2]);
                return pa.getPlotAbs(id);
            }
        } else if (split.length == 4) {
            PlotArea pa = PS.get().getPlotArea(split[0], split[1]);
            if (pa != null) {
                PlotId id = PlotId.fromString(split[1] + ";" + split[2]);
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
        PlotArea pa = PS.get().getPlotAreaAbs(loc);
        if (pa != null) {
            return pa.getPlot(loc);
        }
        return null;
    }
    
    /**
     * Constructor for a temporary plot (use -1 for temp)<br>
     * The database will ignore any queries regarding temporary plots.
     * Please note that some bulk plot management functions may still affect temporary plots (TODO: fix this)
     *
     * @see Plot#getPlot(String, PlotId) for existing plots
     *
     * @param world
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
     * @see Plot#getPlot(String, PlotId) for existing plots
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
        settings = new PlotSettings();
        this.members = members;
        this.trusted = trusted;
        this.denied = denied;
        settings.setAlias(alias);
        settings.setPosition(position);
        settings.setMerged(merged);
        if (flags != null) {
            for (final Flag flag : flags) {
                settings.flags.put(flag.getKey(), flag);
            }
        }
        this.timestamp = timestamp;
        this.temp = temp;
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
        if (meta == null) {
            meta = new ConcurrentHashMap<String, Object>();
        }
        meta.put(key, value);
    }
    
    /**
     * Get the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     * @param key
     * @return
     */
    public Object getMeta(final String key) {
        if (meta != null) {
            return meta.get(key);
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
        if (meta != null) {
            meta.remove(key);
        }
    }
    
    /**
     * Get the cluster this plot is associated with
     * @return the PlotCluster object, or null
     */
    public PlotCluster getCluster() {
        return area.getCluster(id);
    }
    
    /**
     * Efficiently get the players currently inside this plot<br>
     *  - Will return an empty list if no players are in the plot<br>
     *  - Remember, you can cast a PlotPlayer to it's respective implementation (BukkitPlayer, SpongePlayer) to obtain the player object
     * @return list of PlotPlayer(s) or an empty list
     */
    public List<PlotPlayer> getPlayersInPlot() {
        final ArrayList<PlotPlayer> players = new ArrayList<>();
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
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
        return owner != null;
    }
    
    /**
     * Check if a UUID is a plot owner (merged plots may have multiple owners)
     * @param uuid
     * @return
     */
    public boolean isOwner(final UUID uuid) {
        return PlotHandler.isOwner(this, uuid);
    }
    
    /**
     * Get a list of owner UUIDs for a plot (supports multi-owner mega-plots)
     * @return
     */
    public HashSet<UUID> getOwners() {
        return PlotHandler.getOwners(this);
    }
    
    /**
     * Check if the player is either the owner or on the trusted/added list
     *
     * @param uuid
     *
     * @return true if the player is added/trusted or is the owner
     */
    public boolean isAdded(final UUID uuid) {
        return PlotHandler.isAdded(this, uuid);
    }
    
    /**
     * Should the player be denied from entering?
     *
     * @param uuid
     *
     * @return boolean false if the player is allowed to enter
     */
    public boolean isDenied(final UUID uuid) {
        return (denied != null) && ((denied.contains(DBFunc.everyone) && !isAdded(uuid)) || (!isAdded(uuid) && denied.contains(uuid)));
    }
    
    /**
     * Get the plot ID
     */
    public PlotId getId() {
        return id;
    }
    
    /**
     * Get the plot world object for this plot<br>
     *  - The generic PlotArea object can be casted to its respective class for more control (e.g. HybridPlotWorld)
     * @return PlotArea
     */
    public PlotArea getArea() {
        return area;
    }
    
    public void setArea(PlotArea area) {
        if (this.area != null) {
            this.area.removePlot(id);
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
        return area.getPlotManager();
    }
    
    /**
     * Get or create plot settings
     * @return PlotSettings
     * @deprecated use equivalent plot method;
     */
    @Deprecated
    public PlotSettings getSettings() {
        if (settings == null) {
            settings = new PlotSettings();
        }
        return settings;
    }
    
    /**
     * Returns true if the plot is not merged, or it is the base plot of multiple merged plots
     * @return
     */
    public boolean isBasePlot() {
        if (settings == null || !isMerged()) {
            return true;
        }
        return equals(getBasePlot(false));
    }
    
    /**
     * The cached origin plot<br>
     *  - The origin plot is used for plot grouping and relational data
     */
    private Plot origin;
    
    /**
     * The base plot is an arbitrary but specific connected plot. It is useful for the following:<br>
     *  - Merged plots need to be treated as a single plot for most purposes<br>
     *  - Some data such as home location needs to be associated with the group rather than each plot<br>
     *  - If the plot is not merged it will return itself.<br>
     *  - The result is cached locally
     * @return base Plot
     */
    public Plot getBasePlot(boolean recalculate) {
        if ((origin != null && !recalculate)) {
            if (this.equals(origin)) {
                return this;
            }
            return origin.getBasePlot(false);
        }
        if (!isMerged()) {
            origin = this;
            return origin;
        }
        origin = this;
        PlotId min = id;
        for (Plot plot : getConnectedPlots()) {
            if (plot.id.y < min.y || (plot.id.y.equals(min.y) && plot.id.x < min.x)) {
                origin = plot;
                min = plot.id;
            }
        }
        for (Plot plot : getConnectedPlots()) {
            plot.origin = origin;
        }
        return origin;
    }
    
    /**
     * Check if the plot is merged in any direction
     * @return
     */
    public boolean isMerged() {
        if (settings == null) {
            return false;
        }
        return settings.getMerged(0) || settings.getMerged(2) || settings.getMerged(1) || settings.getMerged(3);
    }
    
    /**
     * Get the timestamp of when the plot was created (unreliable)<br>
     * - not accurate if the plot was created before this was implemented<br>
     *  - Milliseconds since the epoch<br>
     * @return
     */
    public long getTimestamp() {
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
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
                return settings.getMerged(direction);
            case 7:
                int i = direction - 4;
                int i2 = 0;
                return settings.getMerged(i2)
                && settings.getMerged(i)
                && area.getPlotAbs(id.getRelative(i)).getMerged(i2)
                && settings.getMerged(i)
                && settings.getMerged(i2)
                && area.getPlotAbs(id.getRelative(i2)).getMerged(i);
            case 4:
            case 5:
            case 6:
                i = direction - 4;
                i2 = direction - 3;
                return settings.getMerged(i2)
                && settings.getMerged(i)
                && area.getPlotAbs(id.getRelative(i)).getMerged(i2)
                && settings.getMerged(i)
                && settings.getMerged(i2)
                && area.getPlotAbs(id.getRelative(i2)).getMerged(i);
                
        }
        return false;
    }
    
    /**
     * Get the denied users
     * @return
     */
    public HashSet<UUID> getDenied() {
        if (denied == null) {
            denied = new HashSet<>();
        }
        return denied;
    }
    
    /**
     * Get the trusted users
     * @return
     */
    public HashSet<UUID> getTrusted() {
        if (trusted == null) {
            trusted = new HashSet<>();
        }
        return trusted;
    }
    
    /**
     * Get the members
     * @return
     */
    public HashSet<UUID> getMembers() {
        if (members == null) {
            members = new HashSet<>();
        }
        return members;
    }
    
    /**
     * Deny someone (updates database as well)
     * @param uuid
     */
    public void addDenied(final UUID uuid) {
        PlotHandler.addDenied(this, uuid);
    }
    
    /**
     * Add someone as a helper (updates database as well)
     *
     * @param uuid
     */
    public void addTrusted(final UUID uuid) {
        PlotHandler.addTrusted(this, uuid);
    }
    
    /**
     * Add someone as a trusted user (updates database as well)
     *
     * @param uuid
     */
    public void addMember(final UUID uuid) {
        PlotHandler.addMember(this, uuid);
    }
    
    /**
     * Set the plot owner (and update the database)
     * @param owner
     */
    public void setOwner(final UUID owner) {
        PlotHandler.setOwner(this, owner);
    }
    
    /**
     * Set the trusted users for this plot
     * @param uuids
     */
    public void setTrusted(final Set<UUID> uuids) {
        PlotHandler.setTrusted(this, uuids);
    }
    
    /**
     * Set the members for this plot
     * @param uuids
     */
    public void setMembers(final Set<UUID> uuids) {
        PlotHandler.setMembers(this, uuids);
    }
    
    /**
     * Set the denied users for this plot
     * @param uuids
     */
    public void setDenied(final Set<UUID> uuids) {
        PlotHandler.setDenied(this, uuids);
    }
    
    /**
     * Clear a plot
     * @see this#clear(Plot, boolean, Runnable)
     * @see this#clearAsPlayer(Plot, boolean, Runnable)
     * @see #deletePlot(Runnable) to clear and delete a plot
     * @param whenDone A runnable to execute when clearing finishes, or null
     */
    public void clear(final Runnable whenDone) {
        clear(false, false, whenDone);
    }
    
    public boolean clear(final boolean checkRunning, final boolean isDelete, final Runnable whenDone) {
        if (checkRunning && getRunning() != 0) {
            return false;
        }
        if (!EventUtil.manager.callClear(this)) {
            return false;
        }
        final HashSet<RegionWrapper> regions = getRegions();
        final HashSet<Plot> plots = getConnectedPlots();
        final ArrayDeque<Plot> queue = new ArrayDeque<>(plots);
        if (isDelete) {
            removeSign();
        }
        unlinkPlot(true, !isDelete);
        final PlotManager manager = area.getPlotManager();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (queue.size() == 0) {
                    final AtomicInteger finished = new AtomicInteger(0);
                    final Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            if (finished.incrementAndGet() >= plots.size()) {
                                for (RegionWrapper region : regions) {
                                    Location[] corners = region.getCorners(area.worldname);
                                    ChunkManager.manager.clearAllEntities(corners[0], corners[1]);
                                }
                                TaskManager.runTask(whenDone);
                            }
                        }
                    };
                    if (isDelete) {
                        for (Plot current : plots) {
                            manager.unclaimPlot(area, current, run);
                        }
                    } else {
                        for (Plot current : plots) {
                            manager.claimPlot(area, current);
                            SetQueue.IMP.addTask(run);
                        }
                    }
                    return;
                }
                final Plot current = queue.poll();
                if ((area.TERRAIN != 0) || Settings.FAST_CLEAR) {
                    ChunkManager.manager.regenerateRegion(current.getBottomAbs(), current.getTopAbs(), false, this);
                    return;
                }
                manager.clearPlot(area, current, this);
            }
        };
        run.run();
        return true;
    }
    
    /**
     * Set the biome for a plot asynchronously
     * @param plot
     * @param biome The biome e.g. "forest"
     * @param whenDone The task to run when finished, or null
     */
    public void setBiome(final String biome, final Runnable whenDone) {
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(getRegions());
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    Plot.this.refreshChunks();
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = regions.poll();
                Location pos1 = new Location(Plot.this.area.worldname, region.minX, region.minY, region.minZ);
                Location pos2 = new Location(Plot.this.area.worldname, region.maxX, region.maxY, region.maxZ);
                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override
                    public void run(int[] value) {
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
     * @param plot
     * @param createRoad
     * @return
     */
    public boolean unlinkPlot(final boolean createRoad, boolean createSign) {
        if (!isMerged()) {
            return false;
        }
        HashSet<Plot> plots = getConnectedPlots();
        ArrayList<PlotId> ids = new ArrayList<>(plots.size());
        for (Plot current : plots) {
            current.setHome(null);
            ids.add(current.getId());
        }
        final boolean result = EventUtil.manager.callUnlink(area, ids);
        if (!result) {
            return false;
        }
        clearRatings();
        if (createSign) {
            removeSign();
        }
        final PlotManager manager = area.getPlotManager();
        if (createRoad) {
            manager.startPlotUnlink(area, ids);
        }
        if ((area.TERRAIN != 3) && createRoad) {
            for (Plot current : plots) {
                if (current.getMerged(1)) {
                    manager.createRoadEast(area, current);
                    if (current.getMerged(2)) {
                        manager.createRoadSouth(area, current);
                        if (current.getMerged(5)) {
                            manager.createRoadSouthEast(area, current);
                        }
                    }
                } else if (current.getMerged(2)) {
                    manager.createRoadSouth(area, current);
                }
            }
        }
        for (Plot current : plots) {
            boolean[] merged = new boolean[] { false, false, false, false };
            current.setMerged(merged);
            if (createSign) {
                setSign(MainUtil.getName(current.owner));
            }
        }
        if (createRoad) {
            manager.finishPlotUnlink(area, ids);
        }
        return true;
    }
    
    /**
     * Set the sign for a plot to a specific name
     * @param name
     * @param p
     */
    public void setSign(final String name) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    setSign(name);
                }
            });
            return;
        }
        final String rename = name == null ? "unknown" : name;
        final PlotManager manager = area.getPlotManager();
        if (area.ALLOW_SIGNS) {
            final Location loc = manager.getSignLoc(area, this);
            final String id = this.id.x + ";" + this.id.y;
            final String[] lines = new String[] {
            C.OWNER_SIGN_LINE_1.formatted().replaceAll("%id%", id),
            C.OWNER_SIGN_LINE_2.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename),
            C.OWNER_SIGN_LINE_3.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename),
            C.OWNER_SIGN_LINE_4.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename) };
            WorldUtil.IMP.setSign(area.worldname, loc.getX(), loc.getY(), loc.getZ(), lines);
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
     * Set a flag for this plot
     * @param flags
     */
    public void setFlags(Set<Flag> flags) {
        FlagManager.setPlotFlags(this, flags);
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
     * @see PS#removePlot(String, PlotId, boolean)
     * @see #clear(Runnable) to simply clear a plot
     */
    public boolean deletePlot(final Runnable whenDone) {
        if (!hasOwner()) {
            return false;
        }
        final HashSet<Plot> plots = getConnectedPlots();
        clear(false, true, new Runnable() {
            @Override
            public void run() {
                for (Plot current : plots) {
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
     * @param plot
     * @return
     */
    public int[] countEntities() {
        int[] count = new int[6];
        for (Plot current : getConnectedPlots()) {
            int[] result = ChunkManager.manager.countEntities(current);
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
        int value = getRunning();
        for (Plot plot : getConnectedPlots()) {
            plot.setMeta("running", value + 1);
        }
        return value;
    }
    
    public int removeRunning() {
        int value = getRunning();
        if (value < 2) {
            for (Plot plot : getConnectedPlots()) {
                plot.deleteMeta("running");
            }
        }
        else {
            for (Plot plot : getConnectedPlots()) {
                plot.setMeta("running", value - 1);
            }
        }
        return value;
    }
    
    public int getRunning() {
        Integer value = (Integer) getMeta("running");
        return value == null ? 0 : value;
    }
    
    public boolean unclaim() {
        return PlotHandler.unclaim(this);
    }
    
    /**
     * Unlink a plot and remove the roads
     * @see this#unlinkPlot(Plot, boolean, boolean)
     * @return true if plot was linked
     */
    public boolean unlink() {
        return unlinkPlot(true, true);
    }
    
    public Location getCenter() {
        Location top = getTop();
        Location bot = getBottom();
        return new Location(area.worldname, (top.getX() + bot.getX()) / 2, (top.getY() + bot.getY()) / 2, (top.getZ() + bot.getZ()) / 2);
    }

    /**
     * Return the home location for the plot
     * @see this#getPlotHome(Plot)
     * @return Home location
     */
    public Location getHome() {
        final BlockLoc home = getPosition();
        if ((home == null) || ((home.x == 0) && (home.z == 0))) {
            return getDefaultHome();
        } else {
            Location bot = getBottomAbs();
            final Location loc = new Location(bot.getWorld(), bot.getX() + home.x, bot.getY() + home.y, bot.getZ() + home.z, home.yaw, home.pitch);
            if (WorldUtil.IMP.getBlock(loc).id != 0) {
                loc.setY(Math.max(WorldUtil.IMP.getHeighestBlock(area.worldname, loc.getX(), loc.getZ()), bot.getY()));
            }
            return loc;
        }
    }
    
    /**
     * Get the default home location for a plot<br>
     *  - Ignores any home location set for that specific plot
     * @return
     */
    public Location getDefaultHome() {
        Plot plot = getBasePlot(false);
        if (area.DEFAULT_HOME != null) {
            final int x;
            final int z;
            if ((area.DEFAULT_HOME.x == Integer.MAX_VALUE) && (area.DEFAULT_HOME.z == Integer.MAX_VALUE)) {
                // center
                RegionWrapper largest = plot.getLargestRegion();
                x = ((largest.maxX - largest.minX) / 2) + largest.minX;
                z = ((largest.maxZ - largest.minZ) / 2) + largest.minZ;
            } else {
                // specific
                Location bot = plot.getBottomAbs();
                x = bot.getX() + area.DEFAULT_HOME.x;
                z = bot.getZ() + area.DEFAULT_HOME.z;
            }
            final int y = WorldUtil.IMP.getHeighestBlock(plot.area.worldname, x, z);
            return new Location(plot.area.worldname, x, y + 1, z);
        }
        // Side
        RegionWrapper largest = plot.getLargestRegion();
        final int x = ((largest.maxX - largest.minX) / 2) + largest.minX;
        final int z = largest.minZ - 1;
        final PlotManager manager = plot.getManager();
        final int y = Math.max(WorldUtil.IMP.getHeighestBlock(plot.area.worldname, x, z), manager.getSignLoc(plot.area, plot).getY());
        return new Location(plot.area.worldname, x, y + 1, z);
    }
    
    /**
     * Get the average rating of the plot. This is the value displayed in /plot info
     * @return average rating as double
     */
    public double getAverageRating() {
        double sum = 0;
        final Collection<Rating> ratings = getRatings().values();
        for (final Rating rating : ratings) {
            sum += rating.getAverageRating();
        }
        return (sum / ratings.size());
    }
    
    /**
     * Set a rating for a user<br>
     *  - If the user has already rated, the following will return false
     * @param uuid
     * @param rating
     * @return
     */
    public boolean addRating(UUID uuid, Rating rating) {
        Plot base = getBasePlot(false);
        PlotSettings baseSettings = base.getSettings();
        if (baseSettings.getRatings().containsKey(uuid)) {
            return false;
        }
        baseSettings.getRatings().put(uuid, rating.getAggregate());
        DBFunc.setRating(base, uuid, temp);
        return true;
    }
    
    /**
     * Clear the ratings for this plot
     */
    public void clearRatings() {
        Plot base = getBasePlot(false);
        PlotSettings baseSettings = base.getSettings();
        if (baseSettings.ratings != null && baseSettings.ratings.size() > 0) {
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
        Plot base = getBasePlot(false);
        final HashMap<UUID, Rating> map = new HashMap<UUID, Rating>();
        if (base.getSettings().ratings == null) {
            return map;
        }
        for (final Entry<UUID, Integer> entry : base.getSettings().ratings.entrySet()) {
            map.put(entry.getKey(), new Rating(entry.getValue()));
        }
        return map;
    }
    
    /**
     * Set the home location
     * @param loc
     */
    public void setHome(BlockLoc loc) {
        final BlockLoc pos = getSettings().getPosition();
        if (((pos == null || pos.equals(new BlockLoc(0, 0, 0))) && (loc == null)) || ((pos != null) && pos.equals(loc))) {
            return;
        }
        Plot plot = getBasePlot(false);
        plot.getSettings().setPosition(loc);
        if (plot.getSettings().getPosition() == null) {
            DBFunc.setPosition(plot, "");
        } else {
            DBFunc.setPosition(plot, getSettings().getPosition().toString());
        }
    }
    
    /**
     * Set the plot alias
     * @param alias
     */
    public void setAlias(String alias) {
        for (Plot current : getConnectedPlots()) {
            final String name = getSettings().getAlias();
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
     * Resend all chunks inside the plot to nearby players<br>
     * This should not need to be called
     * @see this#update(Plot)
     */
    public void refreshChunks() {
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                final HashSet<ChunkLoc> chunks = new HashSet<>();
                for (RegionWrapper region : getRegions()) {
                    for (int x = region.minX >> 4; x <= region.maxX >> 4; x++) {
                        for (int z = region.minZ >> 4; z <= region.maxZ >> 4; z++) {
                            chunks.add(new ChunkLoc(x, z));
                        }
                    }
                }
                SetQueue.IMP.queue.sendChunk(area.worldname, chunks);
            }
        });
    }
    
    /**
     * Remove the plot sign if it is set
     */
    public void removeSign() {
        final PlotManager manager = area.getPlotManager();
        if (!area.ALLOW_SIGNS) {
            return;
        }
        final Location loc = manager.getSignLoc(area, this);
        SetQueue.IMP.setBlock(area.worldname, loc.getX(), loc.getY(), loc.getZ(), 0);
    }
    
    /**
     * Set the plot sign if plot signs are enabled
     */
    public void setSign() {
        if (owner == null) {
            setSign(null);
            return;
        }
        setSign(UUIDHandler.getName(owner));
    }
    
    /**
     * Register a plot and create it in the database<br>
     *  - The plot will not be created if the owner is null<br>
     *  - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot creation.
     * @return true if plot was created successfully
     */
    public boolean create() {
        return create(owner, true);
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
        owner = uuid;
        Plot existing = area.getOwnedPlotAbs(id);
        if (existing != null) {
            throw new IllegalStateException("Plot already exists!");
        }
        if (notify) {
            Integer meta = (Integer) area.getMeta("worldBorder");
            if (meta != null) {
                updateWorldBorder();
            }
        }
        getTrusted().clear();
        getMembers().clear();
        getDenied().clear();
        settings = new PlotSettings();
        if (area.addPlot(this)) {
            DBFunc.createPlotAndSettings(Plot.this, new Runnable() {
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
        PlotBlock[] parsed = Configuration.BLOCKLIST.parseString(blocks);
        if (parsed == null || parsed.length == 0) {
            return false;
        }
        return setComponent(component, parsed);
    }
    
    /**
     * Get the biome (String)
     */
    public String getBiome() {
        final Location loc = getBottomAbs();
        return WorldUtil.IMP.getBiome(loc.getWorld(), loc.getX(), loc.getZ());
    }
    
    /**
     * Return the top location for the plot
     * @return
     */
    public Location getTopAbs() {
        return area.getPlotManager().getPlotTopLocAbs(area, id);
    }
    
    /**
     * Return the bottom location for the plot
     * @return
     */
    public Location getBottomAbs() {
        return area.getPlotManager().getPlotBottomLocAbs(area, id);
    }
    
    /**
     * Swap the settings for two plots
     * @param p1
     * @param p2
     * @param whenDone
     * @return
     */
    public boolean swapData(Plot p2, final Runnable whenDone) {
        if (owner == null) {
            if ((p2 != null) && (p2.owner != null)) {
                p2.moveData(this, whenDone);
                return true;
            }
            return false;
        }
        if ((p2 == null) || (p2.owner == null)) {
            if (owner != null) {
                moveData(p2, whenDone);
                return true;
            }
            return false;
        }
        // Swap cached
        final PlotId temp = new PlotId(this.getId().x, this.getId().y);
        getId().x = p2.getId().x;
        getId().y = p2.getId().y;
        p2.getId().x = temp.x;
        p2.getId().y = temp.y;
        area.removePlot(getId());
        p2.area.removePlot(p2.getId());
        this.getId().recalculateHash();
        p2.getId().recalculateHash();
        area.addPlotAbs(this);
        p2.area.addPlotAbs(p2);
        // Swap database
        DBFunc.dbManager.swapPlots(p2, this);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }
    
    /**
     * Move the settings for a plot
     * @param pos1
     * @param pos2
     * @param whenDone
     * @return
     */
    public boolean moveData(final Plot pos2, final Runnable whenDone) {
        if (owner == null) {
            PS.debug(pos2 + " is unowned (single)");
            TaskManager.runTask(whenDone);
            return false;
        }
        if (pos2.hasOwner()) {
            PS.debug(pos2 + " is unowned (multi)");
            TaskManager.runTask(whenDone);
            return false;
        }
        area.removePlot(id);
        getId().x = pos2.getId().x;
        getId().y = pos2.getId().y;
        getId().recalculateHash();
        area.addPlotAbs(this);
        DBFunc.movePlot(this, pos2);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }
    
    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @param plot
     * @return Location top of mega plot
     */
    public Location getExtendedTopAbs() {
        Location top = getTopAbs();
        if (!isMerged()) {
            return top;
        }
        if (getMerged(2)) {
            top.setZ(getRelative(2).getBottomAbs().getZ() - 1);
        }
        if (getMerged(1)) {
            top.setX(getRelative(1).getBottomAbs().getX() - 1);
        }
        return top;
    }
    
    /**
     * Gets the bottom location for a plot.<br>
     *  - Does not respect mega plots<br>
     *  - Merged plots, only the road will be considered part of the plot<br>
     *
     * @param plot
     *
     * @return Location bottom of mega plot
     */
    public Location getExtendedBottomAbs() {
        Location bot = getBottomAbs();
        if (!isMerged()) {
            return bot;
        }
        if (getMerged(0)) {
            bot.setZ(getRelative(0).getTopAbs().getZ() + 1);
        }
        if (getMerged(3)) {
            bot.setX(getRelative(3).getTopAbs().getX() + 1);
        }
        return bot;
    }
    
    /**
     * Returns the top and bottom location.<br>
     *  - If the plot is not connected, it will return its own corners<br>
     *  - the returned locations will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     * @deprecated as merged plots no longer need to be rectangular
     * @return new Location[] { bottom, top }
     * @see this#getCorners(Plot)
     */
    @Deprecated
    public Location[] getCorners() {
        if (!isMerged()) {
            return new Location[] { getBottomAbs(), getTopAbs() };
        }
        return MainUtil.getCorners(area.worldname, getRegions());
    }
    
    /**
     * Remove the east road section of a plot<br>
     *  - Used when a plot is merged<br>
     * @param plotworld
     * @param plot
     */
    public void removeRoadEast() {
        if ((area.TYPE != 0) && (area.TERRAIN > 1)) {
            if (area.TERRAIN == 3) {
                return;
            }
            Plot other = getRelative(1);
            Location bot = other.getBottomAbs();
            Location top = getTopAbs();
            final Location pos1 = new Location(area.worldname, top.getX(), 0, bot.getZ());
            final Location pos2 = new Location(area.worldname, bot.getX(), 256, top.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            area.getPlotManager().removeRoadEast(area, this);
        }
    }
    
    /**
     * Returns the top and bottom plot id.<br>
     *  - If the plot is not connected, it will return itself for the top/bottom<br>
     *  - the returned ids will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     * @deprecated as merged plots no longer need to be rectangular
     * @return new Plot[] { bottom, top }
     * @see this#getCornerIds(Plot)
     */
    @Deprecated
    public PlotId[] getCornerIds() {
        if (!isMerged()) {
            return new PlotId[] { getId(), getId() };
        }
        PlotId min = new PlotId(getId().x, getId().y);
        PlotId max = new PlotId(getId().x, getId().y);
        for (Plot current : getConnectedPlots()) {
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
        return getCorners()[0];
    }
    
    /**
     * @deprecated in favor of getCorners()[1];
     * @return
     */
    @Deprecated
    public Location getTop() {
        return getCorners()[0];
    }
    
    /**
     * Swap the plot contents and settings with another location<br>
     *  - The destination must correspond to a valid plot of equal dimensions
     * @see ChunkManager#swap(Location, Location, Location, Location, Runnable) to swap terrain
     * @see this#getPlotSelectionIds(PlotId, PlotId) to get the plots inside a selection
     * @see this#swapData(Plot, Plot, Runnable) to swap plot settings
     * @param destination The other plot to swap with
     * @param whenDone A task to run when finished, or null
     * @see this#swapData(Plot, Plot, Runnable)
     * @return boolean if swap was successful
     */
    public boolean swap(final Plot destination, final Runnable whenDone) {
        return move(destination, whenDone, true);
    }
    
    /**
     * Move the plot to an empty location<br>
     *  - The location must be empty
     * @param destination Where to move the plot
     * @param whenDone A task to run when done, or null
     * @return if the move was successful
     */
    public boolean move(final Plot destination, final Runnable whenDone) {
        return move(destination, whenDone, false);
    }
    
    /**
     * Get plot display name
     *
     * @return alias if set, else id
     */
    @Override
    public String toString() {
        if ((settings != null) && (settings.getAlias().length() > 1)) {
            return settings.getAlias();
        }
        return area + ";" + id.x + ";" + id.y;
    }
    
    /**
     * Remove a denied player (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     * @param uuid
     */
    public boolean removeDenied(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (UUID other : new HashSet<>(denied)) {
                result = result || PlotHandler.removeDenied(this, other);
            }
            return result;
        }
        return PlotHandler.removeDenied(this, uuid);
    }
    
    /**
     * Remove a helper (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     * @param uuid
     */
    public boolean removeTrusted(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (UUID other : new HashSet<>(trusted)) {
                result = result || PlotHandler.removeTrusted(this, other);
            }
            return result;
        }
        return PlotHandler.removeTrusted(this, uuid);
    }
    
    /**
     * Remove a trusted user (use DBFunc as well)<br>
     * Using the * uuid will remove all users
     * @param uuid
     */
    public boolean removeMember(final UUID uuid) {
        if (members == null) {
            return false;
        }
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (UUID other : new HashSet<>(members)) {
                result = result || PlotHandler.removeMember(this, other);
            }
            return result;
        }
        return PlotHandler.removeMember(this, uuid);
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
                            final String name = id + "," + area + "," + MainUtil.getName(owner);
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
     * Upload the plot as a schematic to the configured web interface
     * @param whenDone value will be null if uploading fails
     */
    public void upload(final RunnableVal<URL> whenDone) {
        SchematicHandler.manager.getCompoundTag(this, new RunnableVal<CompoundTag>() {
            @Override
            public void run(final CompoundTag value) {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        final URL url = SchematicHandler.manager.upload(value, null, null);
                        if (whenDone != null) {
                            whenDone.value = url;
                        }
                        TaskManager.runTask(whenDone);
                    }
                });
            }
        });
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Plot other = (Plot) obj;
        if (hashCode() != other.hashCode()) {
            return false;
        }
        return ((id.x.equals(other.id.x)) && (id.y.equals(other.id.y)) && area == other.area);
    }
    
    /**
     * Get the plot hashcode
     *
     * @return integer.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Get the flags specific to this plot<br>
     *  - Does not take default flags into account<br>
     * @return
     */
    public HashMap<String, Flag> getFlags() {
        if (settings == null) {
            return new HashMap<>(0);
        }
        return settings.flags;
    }

    /**
     * Get the plot Alias<br>
     *  - Returns an empty string if no alias is set
     * @return
     */
    public String getAlias() {
        if (settings == null) {
            return "";
        }
        return settings.getAlias();
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
    public void setMerged(boolean[] merged) {
        getSettings().setMerged(merged);
        DBFunc.setMerged(this, merged);
        connected_cache = null;
        regions_cache = null;
        if (origin != null) {
            origin.origin = null;
            origin = null;
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
    public void setMerged(int direction, boolean value) {
        if (getSettings().setMerged(direction, value)) {
            if (value) {
                Plot other = getRelative(direction).getBasePlot(false);
                if (!other.equals(getBasePlot(false))) {
                    Plot base = ((other.id.y < id.y) || ((other.id.y.equals(id.y)) && (other.id.x < id.x))) ? other : origin;
                    origin.origin = base;
                    other.origin = base;
                    origin = base;
                    connected_cache = null;
                }
            }
            else {
                if (origin != null) {
                    origin.origin = null;
                    origin = null;
                }
                connected_cache = null;
            }
            DBFunc.setMerged(this, getSettings().getMerged());
            regions_cache = null;
        }
    }

    /**
     * Get the merged array
     * @return boolean [ north, east, south, west ]
     */
    public boolean[] getMerged() {
        if (settings == null) {
            return new boolean[] {false, false, false, false };
        }
        return settings.getMerged();
    }

    /**
     * Get the set home location or 0,0,0 if no location is set<br>
     *  - Does not take the default home location into account
     * @see this#getPlotHome(Plot)
     * @see #getHome()
     * @return
     */
    public BlockLoc getPosition() {
        if (settings == null) {
            return new BlockLoc(0, 0, 0);
        }
        return settings.getPosition();
    }
    
    public boolean canClaim(final PlotPlayer player) {
        if (Settings.ENABLE_CLUSTERS) {
            final PlotCluster cluster = getCluster();
            if (cluster != null) {
                if (!cluster.isAdded(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.claim")) {
                    return false;
                }
            }
        }
        return guessOwner() == null;
    }
    
    public UUID guessOwner() {
        if (this.owner != null) {
            return this.owner;
        }
        if (!area.ALLOW_SIGNS) {
            return null;
        }
        try {
            Location loc = this.getManager().getSignLoc(area, this);
            ChunkManager.manager.loadChunk(loc.getWorld(), loc.getChunkLoc(), false);
            String[] lines = WorldUtil.IMP.getSign(loc);
            if (lines == null) {
                return null;
            }
            loop: for (int i = 4; i > 0; i--) {
                String caption = C.valueOf("OWNER_SIGN_LINE_" + i).s();
                int index = caption.indexOf("%plr%");
                if (index == -1) {
                    continue;
                }
                String name = lines[i - 1].substring(index);
                if (name.length() == 0) {
                    return null;
                }
                UUID owner = UUIDHandler.getUUID(name, null);
                if (owner != null) {
                    this.owner = owner;
                    break;
                }
                if (lines[i - 1].length() == 15) {
                    BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                    for (Entry<StringWrapper, UUID> entry : map.entrySet()) {
                        String key = entry.getKey().value;
                        if (key.length() > name.length() && key.startsWith(name)) {
                            this.owner = entry.getValue();
                            break loop;
                        }
                    }
                }
                this.owner = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                break;
            }
            if (this.owner != null) {
                this.create();
            }
            return this.owner;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Remove the south road section of a plot<br>
     *  - Used when a plot is merged<br>
     * @param plotworld
     * @param plot
     */
    public void removeRoadSouth() {
        if ((area.TYPE != 0) && (area.TERRAIN > 1)) {
            if (area.TERRAIN == 3) {
                return;
            }
            Plot other = getRelative(2);
            Location bot = other.getBottomAbs();
            Location top = getTopAbs();
            final Location pos1 = new Location(area.worldname, bot.getX(), 0, top.getZ());
            final Location pos2 = new Location(area.worldname, top.getX(), 256, bot.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            getManager().removeRoadSouth(area, this);
        }
    }
    
    /**
     * Auto merge a plot in a specific direction<br>
     * @param plot The plot to merge
     * @param dir The direction to merge<br>
     * -1 = All directions<br>
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * @param max The max number of merges to do
     * @param uuid The UUID it is allowed to merge with
     * @param removeRoads Wether to remove roads
     * @return true if a merge takes place
     */
    public boolean autoMerge(int dir, int max, final UUID uuid, final boolean removeRoads) {
        if (owner == null) {
            return false;
        }
        HashSet<Plot> visited = new HashSet<>();
        HashSet<PlotId> merged = new HashSet<>();
        HashSet<Plot> connected = getConnectedPlots();
        for (Plot current : connected) {
            merged.add(current.getId());
        }
        ArrayDeque<Plot> frontier = new ArrayDeque<>(connected);
        Plot current;
        boolean toReturn = false;
        Set<Plot> plots;
        while ((current = frontier.poll()) != null && max >= 0) {
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            if (max >= 0 && (dir == -1 || dir == 0) && !current.getMerged(0)) {
                Plot other = current.getRelative(0);
                if (other != null
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || ((plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
            if (max >= 0 && (dir == -1 || dir == 1) && !current.getMerged(1)) {
                Plot other = current.getRelative(1);
                if (other != null
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || ((plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
            if (max >= 0 && (dir == -1 || dir == 2) && !current.getMerged(2)) {
                Plot other = current.getRelative(2);
                if (other != null
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || ((plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
            if (max >= 0 && (dir == -1 || dir == 3) && !current.getMerged(3)) {
                Plot other = current.getRelative(3);
                if (other != null
                && other.isOwner(uuid)
                && (other.getBasePlot(false).equals(current.getBasePlot(false)) || ((plots = other.getConnectedPlots()).size() <= max && frontier.addAll(plots) && (max -= plots.size()) != -1))) {
                    current.mergePlot(other, removeRoads);
                    merged.add(current.getId());
                    merged.add(other.getId());
                    toReturn = true;
                }
            }
        }
        if (removeRoads && toReturn) {
            ArrayList<PlotId> ids = new ArrayList<>(merged);
            getManager().finishPlotMerge(area, ids);
        }
        return toReturn;
    }
    
    /**
     * Merge the plot settings<br>
     *  - Used when a plot is merged<br>
     * @param a
     * @param b
     */
    public void mergeData(Plot b) {
        HashMap<String, Flag> flags1 = getFlags();
        HashMap<String, Flag> flags2 = b.getFlags();
        if ((flags1.size() != 0 || flags2.size() != 0) && !flags1.equals(flags2)) {
            boolean greater = flags1.size() > flags2.size();
            if (greater) {
                flags1.putAll(flags2);
            } else {
                flags2.putAll(flags1);
            }
            HashSet<Flag> net = new HashSet<>((greater ? flags1 : flags2).values());
            setFlags(net);
            b.setFlags(net);
        }
        if (getAlias().length() > 0) {
            b.setAlias(getAlias());
        } else if (b.getAlias().length() > 0) {
            setAlias(b.getAlias());
        }
        for (UUID uuid : getTrusted()) {
            b.addTrusted(uuid);
        }
        for (UUID uuid : b.getTrusted()) {
            addTrusted(uuid);
        }
        for (UUID uuid : getMembers()) {
            b.addMember(uuid);
        }
        for (UUID uuid : b.getMembers()) {
            addMember(uuid);
        }
        
        for (UUID uuid : getDenied()) {
            b.addDenied(uuid);
        }
        for (UUID uuid : b.getDenied()) {
            addDenied(uuid);
        }
    }

    public void removeRoadSouthEast() {
        if ((area.TYPE != 0) && (area.TERRAIN > 1)) {
            if (area.TERRAIN == 3) {
                return;
            }
            Plot other = getRelative(1, 1);
            final Location pos1 = getTopAbs().add(1, 0, 1);
            final Location pos2 = other.getBottomAbs().subtract(1, 0, 1);
            pos1.setY(0);
            pos2.setY(256);
            ChunkManager.manager.regenerateRegion(pos1, pos2, true, null);
        } else {
            area.getPlotManager().removeRoadSouthEast(area, this);
        }
    }
    
    public Plot getRelative(int x, int y) {
        return area.getPlotAbs(id.getRelative(x, y));
    }
    
    public Plot getRelative(int direction) {
        return area.getPlotAbs(id.getRelative(direction));
    }
    
    /**
     * @deprecated raw access is deprecated
     */
    @Deprecated
    private static HashSet<Plot> connected_cache;
    private static HashSet<RegionWrapper> regions_cache;
    
    /**
     * Get a set of plots connected (and including) this plot<br>
     *  - This result is cached globally
     * @see this#getConnectedPlots(Plot)
     * @return
     */
    public HashSet<Plot> getConnectedPlots() {
        if (settings == null) {
            return new HashSet<>(Collections.singletonList(this));
        }
        boolean[] merged = getMerged();
        int hash = MainUtil.hash(merged);
        if (hash == 0) {
            return new HashSet<>(Collections.singletonList(this));
        }
        if (connected_cache != null && connected_cache.contains(this)) {
            return connected_cache;
        }
        regions_cache = null;
        connected_cache = new HashSet<Plot>();
        ArrayDeque<Plot> frontier = new ArrayDeque<>();
        HashSet<Object> queuecache = new HashSet<>();
        connected_cache.add(this);
        Plot tmp;
        if (merged[0]) {
            tmp = area.getPlotAbs(id.getRelative(0));
            if (!tmp.getMerged(2)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.hasOwner()) {
                    tmp.getSettings().setMerged(2, true);
                    DBFunc.setMerged(tmp, tmp.settings.getMerged());
                } else {
                    getSettings().setMerged(0, false);
                    DBFunc.setMerged(this, settings.getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[1]) {
            tmp = area.getPlotAbs(id.getRelative(1));
            if (!tmp.getMerged(3)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.hasOwner()) {
                    tmp.getSettings().setMerged(3, true);
                    DBFunc.setMerged(tmp, tmp.settings.getMerged());
                } else {
                    this.getSettings().setMerged(1, false);
                    DBFunc.setMerged(this, this.settings.getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[2]) {
            tmp = area.getPlotAbs(id.getRelative(2));
            if (!tmp.getMerged(0)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.hasOwner()) {
                    tmp.getSettings().setMerged(0, true);
                    DBFunc.setMerged(tmp, tmp.settings.getMerged());
                } else {
                    this.getSettings().setMerged(2, false);
                    DBFunc.setMerged(this, this.settings.getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[3]) {
            tmp = area.getPlotAbs(id.getRelative(3));
            if (!tmp.getMerged(1)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + this);
                if (tmp.hasOwner()) {
                    tmp.getSettings().setMerged(1, true);
                    DBFunc.setMerged(tmp, tmp.settings.getMerged());
                } else {
                    this.getSettings().setMerged(3, false);
                    DBFunc.setMerged(this, this.settings.getMerged());
                }
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        Plot current;
        while ((current = frontier.poll()) != null) {
            if (current.owner == null || current.settings == null) {
                // Invalid plot
                // merged onto unclaimed plot
                PS.debug("Ignoring invalid merged plot: " + current + " | " + current.owner);
                continue;
            }
            connected_cache.add(current);
            queuecache.remove(current);
            merged = current.getMerged();
            if (merged[0]) {
                tmp = area.getPlotAbs(id.getRelative(0));
                if (tmp != null && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[1]) {
                tmp = area.getPlotAbs(id.getRelative(1));
                if (tmp != null && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[2]) {
                tmp = area.getPlotAbs(id.getRelative(2));
                if (tmp != null && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[3]) {
                tmp = area.getPlotAbs(id.getRelative(3));
                if (tmp != null && !queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
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
     * @see this#getRegions(Plot) 
     * @return
     */
    public HashSet<RegionWrapper> getRegions() {
        if (regions_cache != null && connected_cache != null && connected_cache.contains(this)) {
            return regions_cache;
        }
        if (!this.isMerged()) {
            final Location pos1 = getBottomAbs();
            final Location pos2 = getTopAbs();
            connected_cache = new HashSet<>(Collections.singletonList(this));
            regions_cache = new HashSet<>(1);
            regions_cache.add(new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getY(), pos2.getY(), pos1.getZ(), pos2.getZ()));
            return regions_cache;
        }
        HashSet<Plot> plots = getConnectedPlots();
        regions_cache = new HashSet<>();
        HashSet<PlotId> visited = new HashSet<>();
        ArrayList<PlotId> ids;
        for (Plot current : plots) {
            if (visited.contains(current.getId())) {
                continue;
            }
            boolean merge = true;
            boolean tmp = true;
            PlotId bot = new PlotId(current.getId().x, current.getId().y);
            PlotId top = new PlotId(current.getId().x, current.getId().y);
            while (merge) {
                merge = false;
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x, bot.y - 1), new PlotId(top.x, bot.y - 1));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(2) || (visited.contains(plot.getId()))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.y--;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(top.x + 1, bot.y), new PlotId(top.x + 1, top.y));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(3) || (visited.contains(plot.getId()))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.x++;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x, top.y + 1), new PlotId(top.x, top.y + 1));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(0) || (visited.contains(plot.getId()))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.y++;
                }
                ids = MainUtil.getPlotSelectionIds(new PlotId(bot.x - 1, bot.y), new PlotId(bot.x - 1, top.y));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = area.getPlotAbs(id);
                    if (plot == null || !plot.getMerged(1) || (visited.contains(plot.getId()))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.x--;
                }
            }
            Location gtopabs = area.getPlotAbs(top).getTopAbs();
            Location gbotabs = area.getPlotAbs(bot).getBottomAbs();
            for (PlotId id : MainUtil.getPlotSelectionIds(bot, top)) {
                visited.add(id);
            }
            for (int x = bot.x; x <= top.x; x++) {
                Plot plot = area.getPlotAbs(new PlotId(x, top.y));
                if (plot.getMerged(2)) {
                    // south wedge
                    Location toploc = plot.getExtendedTopAbs();
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    regions_cache.add(new RegionWrapper(botabs.getX(), topabs.getX(), topabs.getZ() + 1, toploc.getZ()));
                    if (plot.getMerged(5)) {
                        regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), topabs.getZ() + 1, toploc.getZ()));
                        // intersection
                    }
                }
            }
            
            for (int y = bot.y; y <= top.y; y++) {
                Plot plot = area.getPlotAbs(new PlotId(top.x, y));;
                if (plot.getMerged(1)) {
                    // east wedge
                    Location toploc = plot.getExtendedTopAbs();
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
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
     * @param plot
     * @return
     */
    public RegionWrapper getLargestRegion() {
        HashSet<RegionWrapper> regions = getRegions();
        RegionWrapper max = null;
        int area = 0;
        for (RegionWrapper region : regions) {
            int current = (region.maxX - region.minX + 1) * (region.maxZ - region.minZ + 1);
            if (current > area) {
                max = region;
                area = current;
            }
        }
        return max;
    }
    
    public void reEnter() {
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                for (final PlotPlayer pp : getPlayersInPlot()) {
                    PlotListener.plotExit(pp, Plot.this);
                    PlotListener.plotEntry(pp, Plot.this);
                }
            }
        }, 1);
    }
    
    /**
     * Teleport a player to a plot and send them the teleport message.
     * @param player
     * @param from
     * @param plot
     * @return If the teleportation is allowed.
     */
    public boolean teleportPlayer(final PlotPlayer player) {
        Plot plot = getBasePlot(false);
        final boolean result = EventUtil.manager.callTeleport(player, player.getLocation(), plot);
        if (result) {
            final Location location;
            if (area.HOME_ALLOW_NONMEMBER || plot.isAdded(player.getUUID())) {
                location = getHome();
            } else {
                location = getDefaultHome();
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
    
    /**
     * Set a component for a plot to the provided blocks<br>
     *  - E.g. floor, wall, border etc.<br>
     *  - The available components depend on the generator being used<br>
     * @param plot
     * @param component
     * @param blocks
     * @return
     */
    public boolean setComponent(final String component, final PlotBlock[] blocks) {
        return getManager().setComponent(area, getId(), component, blocks);
    }
    
    /**
     * Expand the world border to include the provided plot (if applicable)
     * @param plot
     */
    public void updateWorldBorder() {
        if (owner == null) {
            return;
        }
        int border = area.getBorder();
        if (border == Integer.MAX_VALUE) {
            return;
        }
        final PlotId id = new PlotId(Math.abs(getId().x) + 1, Math.abs(getId().x) + 1);
        PlotManager manager = getManager();
        final Location bot = manager.getPlotBottomLocAbs(area, id);
        final Location top = manager.getPlotTopLocAbs(area, id);
        final int botmax = Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ()));
        final int topmax = Math.max(Math.abs(top.getX()), Math.abs(top.getZ()));
        final int max = Math.max(botmax, topmax);
        if (max > border) {
            area.setMeta("worldBorder", max);
        }
    }
    
    /**
     * Merges 2 plots Removes the road inbetween <br>- Assumes plots are directly next to each other <br> - saves to DB
     *
     * @param world
     * @param lesserPlot
     * @param greaterPlot
     */
    public void mergePlot(Plot lesserPlot, final boolean removeRoads) {
        Plot greaterPlot = this;
        if (lesserPlot.getId().x.equals(greaterPlot.getId().x)) {
            if (lesserPlot.getId().y > greaterPlot.getId().y) {
                Plot tmp = lesserPlot;
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
                    Plot other = getRelative(3);
                    if (other.getMerged(2) && other.getMerged(1)) {
                        other.removeRoadEast();
                        greaterPlot.mergePlot(other, removeRoads);
                    }
                }
            }
        } else {
            if (lesserPlot.getId().x > greaterPlot.getId().x) {
                Plot tmp = lesserPlot;
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
                    Plot other = lesserPlot.getRelative(0);
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
     * @param origin
     * @param destination
     * @param whenDone
     * @param allowSwap
     * @return
     */
    public boolean move(final Plot destination, final Runnable whenDone, boolean allowSwap) {
        PlotId offset = new PlotId(destination.getId().x - this.getId().x, destination.getId().y - this.getId().y);
        Location db = destination.getBottomAbs();
        Location ob = this.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (this.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        boolean occupied = false;
        HashSet<Plot> plots = this.getConnectedPlots();
        for (Plot plot : plots) {
            Plot other = plot.getRelative(offset.x, offset.y);
            if (other.owner != null) {
                if (!allowSwap) {
                    TaskManager.runTaskLater(whenDone, 1);
                    return false;
                }
                occupied = true;
            }
        }
        // world border
        destination.updateWorldBorder();
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(getRegions());
        // move / swap data
        for (Plot plot : plots) {
            Plot other = plot.getRelative(offset.x, offset.y);
            plot.swapData(other, null);
        }
        // copy terrain
        Runnable move = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                final Runnable task = this;
                RegionWrapper region = regions.poll();
                Location[] corners = region.getCorners(area.worldname);
                final Location pos1 = corners[0];
                final Location pos2 = corners[1];
                Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.area.worldname);
                ChunkManager.manager.regenerateRegion(pos1, pos2, false, task);
            }
        };
        Runnable swap = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = regions.poll();
                Location[] corners = region.getCorners(area.worldname);
                Location pos1 = corners[0];
                Location pos2 = corners[1];
                Location pos3 = pos1.clone().add(offsetX, 0, offsetZ);
                Location pos4 = pos2.clone().add(offsetX, 0, offsetZ);
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
     * @param origin
     * @param destination
     * @param whenDone
     * @return
     */
    public boolean copy(final Plot destination, final Runnable whenDone) {
        PlotId offset = new PlotId(destination.getId().x - this.getId().x, destination.getId().y - this.getId().y);
        Location db = destination.getBottomAbs();
        Location ob = this.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (this.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        HashSet<Plot> plots = this.getConnectedPlots();
        for (Plot plot : plots) {
            Plot other = plot.getRelative(offset.x, offset.y);
            if (other.owner != null) {
                TaskManager.runTaskLater(whenDone, 1);
                return false;
            }
        }
        // world border
        destination.updateWorldBorder();
        // copy data
        for (Plot plot : plots) {
            Plot other = plot.getRelative(offset.x, offset.y);
            other.create(other.owner, false);
            if ((plot.getFlags() != null) && (plot.getFlags().size() > 0)) {
                other.getSettings().flags = plot.getFlags();
                DBFunc.setFlags(other, plot.getFlags().values());
            }
            if (plot.isMerged()) {
                other.setMerged(plot.getMerged());
            }
            if ((plot.members != null) && (plot.members.size() > 0)) {
                other.members = plot.members;
                for (final UUID member : other.members) {
                    DBFunc.setMember(other, member);
                }
            }
            if ((plot.trusted != null) && (plot.trusted.size() > 0)) {
                other.trusted = plot.trusted;
                for (final UUID trusted : other.trusted) {
                    DBFunc.setTrusted(other, trusted);
                }
            }
            if ((plot.denied != null) && (plot.denied.size() > 0)) {
                other.denied = plot.denied;
                for (final UUID denied : other.denied) {
                    DBFunc.setDenied(other, denied);
                }
            }
            PS.get().updatePlot(other);
        }
        // copy terrain
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(this.getRegions());
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = regions.poll();
                Location[] corners = region.getCorners(Plot.this.area.worldname);
                Location pos1 = corners[0];
                Location pos2 = corners[1];
                Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.area.worldname);
                ChunkManager.manager.copyRegion(pos1, pos2, newPos, this);
            }
        };
        run.run();
        return true;
    }
}
