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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;

/**
 * The plot class
 *
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
    public final String world;
    /**
     * plot owner
     * (Merged plots can have multiple owners)
     * Direct access is Deprecated: use getOwners()
     */
    @Deprecated
    public UUID owner;
    
    /**
     * Plot creation timestamp (rough)
     * Direct access is Deprecated: use getTimestamp()
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
    public Plot(final String world, final PlotId id, final UUID owner) {
        this.world = world;
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
    public Plot(final String world, final PlotId id) {
        this.world = world;
        this.id = id;
    }
    
    /**
     * Return a new/cached plot object at a given world/plot id
     *
     * @see MainUtil#getPlotSelectionOwned(String world, PlotId bottom, PlotId top) return a list of owned plots between (inclusive) two plot ids.
     *
     * @param world
     * @param id
     * @return
     */
    public static Plot getPlot(final String world, final PlotId id) {
        return MainUtil.getPlot(world, id);
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
        return MainUtil.getPlot(loc);
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
    public Plot(final String world, final PlotId id, final UUID owner, final int temp) {
        this.world = world;
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
    final Collection<Flag> flags, final String world, final boolean[] merged, final long timestamp, final int temp) {
        this.id = id;
        this.world = world;
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
     * Get the metadata for a key
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
     * @return
     */
    public PlotCluster getCluster() {
        if (!Settings.ENABLE_CLUSTERS) {
            return null;
        }
        if (owner == null) {
            return ClusterManager.getCluster(this);
        }
        Flag flag = FlagManager.getPlotFlagRaw(this, "cluster");
        if (flag != null) {
            PlotCluster cluster = (PlotCluster) flag.getValue();
            cluster = ClusterManager.getCluster(cluster.world, cluster.getName());
            if (cluster != null) {
                return cluster;
            }
            cluster = ClusterManager.getCluster(this);
            if (cluster == null) {
                FlagManager.removePlotFlag(this, "cluster");
                return null;
            } else {
                flag = new Flag(flag.getAbstractFlag(), cluster);
                FlagManager.addPlotFlag(this, flag);
                return cluster;
            }
        }
        final PlotCluster cluster = ClusterManager.getCluster(this);
        if (cluster != null) {
            flag = new Flag(FlagManager.getFlag("cluster"), cluster);
            FlagManager.addPlotFlag(this, flag);
            return cluster;
        }
        return null;
    }
    
    /**
     * Efficiently get the players currently inside this plot
     * @return
     */
    public List<PlotPlayer> getPlayersInPlot() {
        return MainUtil.getPlayersInPlot(this);
    }
    
    /**
     * Check if the plot has a set owner
     *
     * @return false if there is no owner
     */
    public boolean hasOwner() {
        return owner != null;
    }
    
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
        return (getDenied() != null) && ((denied.contains(DBFunc.everyone) && !isAdded(uuid)) || (!isAdded(uuid) && denied.contains(uuid)));
    }
    
    /**
     * Get the plot ID
     */
    public PlotId getId() {
        return id;
    }
    
    /**
     * Get the plot world object for this plot<br>
     *  - The generic PlotWorld object can be casted to it's respective class for more control
     * @return PlotWorld
     */
    public PlotWorld getWorld() {
        return PS.get().getPlotWorld(world);
    }
    
    /**
     * Get the plot manager object for this plot<br>
     *  - The generic PlotManager object can be casted to it's respective class for more control
     * @return PlotManager
     */
    public PlotManager getManager() {
        return PS.get().getPlotManager(world);
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
        for (Plot plot : MainUtil.getConnectedPlots(this)) {
            if (plot.id.y < min.y || (plot.id.y == min.y && plot.id.x < min.x)) {
                origin = plot;
                min = plot.id;
            }
        }
        for (Plot plot : MainUtil.getConnectedPlots(this)) {
            plot.origin = origin;
        }
        return origin;
    }
    
    /**
     * Check if the plot is merged
     * @return
     */
    public boolean isMerged() {
        if (settings == null) {
            return false;
        }
        return settings.getMerged(0) || settings.getMerged(2) || settings.getMerged(1) || settings.getMerged(3);
    }
    
    /**
     * Get the timestamp in milliseconds of when the plot was created (unreliable)
     * @return
     */
    public long getTimestamp() {
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    /**
     * Get if the plot is merged in a direction
     * @param direction
     * @return
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
                return settings.getMerged(i2) && settings.getMerged(i) && MainUtil.getPlotAbs(world, MainUtil.getPlotIdRelative(id, i)).getMerged(i2) && settings.getMerged(i) && settings.getMerged(i2) && MainUtil.getPlotAbs(world, MainUtil.getPlotIdRelative(id, i2)).getMerged(i);
            case 4:
            case 5:
            case 6:
                i = direction - 4;
                i2 = direction - 3;
                return settings.getMerged(i2) && settings.getMerged(i) && MainUtil.getPlotAbs(world, MainUtil.getPlotIdRelative(id, i)).getMerged(i2) && settings.getMerged(i) && settings.getMerged(i2) && MainUtil.getPlotAbs(world, MainUtil.getPlotIdRelative(id, i2)).getMerged(i);
                
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
     *
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
     * @see MainUtil#clear(Plot, boolean, Runnable)
     * @see MainUtil#clearAsPlayer(Plot, boolean, Runnable)
     * @see #deletePlot() to clear and delete a plot
     * @param whenDone A runnable to execute when clearing finishes, or null
     */
    public void clear(final Runnable whenDone) {
        MainUtil.clear(this, false, whenDone);
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
     * @param flag
     * @param value
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
     * @param flag
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
        boolean result = MainUtil.delete(this, whenDone);
        return result;
    }
    
    /**
     * Returns true if a previous task was running
     * @return
     */
    public int addRunning() {
        int value = getRunning();
        for (Plot plot : getConnectedPlots()) {
            MainUtil.runners.put(plot, value + 1);
        }
        return value;
    }
    
    public int removeRunning() {
        int value = getRunning();
        if (value < 2) {
            for (Plot plot : getConnectedPlots()) {
                MainUtil.runners.remove(plot);
            }
        }
        else {
            for (Plot plot : getConnectedPlots()) {
                MainUtil.runners.put(plot, value - 1);
            }
        }
        return value;
    }
    
    public int getRunning() {
        Integer value = MainUtil.runners.get(this);
        return value == null ? 0 : value;
    }
    
    public boolean unclaim() {
        return PlotHandler.unclaim(this);
    }
    
    /**
     * Unlink a plot and remove the roads
     * @see MainUtil#unlinkPlot(Plot, boolean removeRoad)
     * @return true if plot was linked
     */
    public boolean unlink() {
        return MainUtil.unlinkPlot(this, true, true);
    }
    
    /**
     * Return the home location for the plot
     * @see MainUtil#getPlotHome(Plot)
     * @return Home location
     */
    public Location getHome() {
        return MainUtil.getPlotHome(this);
    }
    
    /**
     * Get the average rating of the plot. This is the value displayed in /plot info
     * @return average rating as double
     */
    public double getAverageRating() {
        double sum = 0;
        final Collection<Rating> ratings = getBasePlot(false).getRatings().values();
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
    public void setHome(final BlockLoc loc) {
        final BlockLoc pos = getSettings().getPosition();
        if (((pos == null || pos.equals(new BlockLoc(0, 0, 0))) && (loc == null)) || ((pos != null) && pos.equals(loc))) {
            return;
        }
        getSettings().setPosition(loc);
        if (getSettings().getPosition() == null) {
            DBFunc.setPosition(this, "");
        } else {
            DBFunc.setPosition(this, getSettings().getPosition().toString());
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
     * @see MainUtil#update(Plot)
     */
    public void refreshChunks() {
        MainUtil.update(this);
    }
    
    /**
     * Remove the plot sign if it is set
     */
    public void removeSign() {
        MainUtil.removeSign(this);
    }
    
    /**
     * Set the plot sign if plot signs are enabled
     */
    public void setSign() {
        MainUtil.setSign(this);
    }
    
    /**
     * Register a plot and create it in the database<br>
     *  - The plot will not be created if the owner is null<br>
     *  - Any setting from before plot creation will not be saved until the server is stopped properly. i.e. Set any values/options after plot creation.
     * @return true if plot was created successfully
     */
    public boolean create() {
        return MainUtil.createPlot(owner, this);
    }
    
    /**
     * Auto merge the plot with any adjacent plots of the same owner
     * @see MainUtil#autoMerge(Plot, UUID) to specify the owner
     * @param removeRoads If to remove roads when merging
     */
    public boolean autoMerge(final boolean removeRoads) {
        return MainUtil.autoMerge(this, -1, Integer.MAX_VALUE, owner, removeRoads);
    }
    
    /**
     * Set the plot biome (this does not set the terrain, @see BiomeGenerator plugin for terrain)
     */
    public void setBiome(final String biome, final Runnable whenDone) {
        MainUtil.setBiome(this, biome, whenDone);
    }
    
    /**
     * Set components such as border, wall, floor
     *  (components are generator specific)
     */
    public boolean setComponent(final String component, final PlotBlock... blocks) {
        return MainUtil.setComponent(this, component, blocks);
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
        return MainUtil.setComponent(this, component, parsed);
    }
    
    /**
     * Get the biome (String)
     */
    public String getBiome() {
        final Location loc = getBottomAbs();
        return BlockManager.manager.getBiome(loc.getWorld(), loc.getX(), loc.getZ());
    }
    
    /**
     * Return the top location for the plot
     * @return
     */
    public Location getTopAbs() {
        return MainUtil.getPlotTopLocAbs(world, id);
    }
    
    /**
     * Return the bottom location for the plot
     * @return
     */
    public Location getBottomAbs() {
        return MainUtil.getPlotBottomLocAbs(world, id);
    }
    
    /**
     * Returns the top and bottom connected plot.<br>
     *  - If the plot is not connected, it will return itself for the top/bottom<br>
     *  - the returned IDs will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape
     * @deprecated as merged plots no longer need to be rectangular
     * @param plot
     * @return new PlotId[] { bottom, top }
     * @see MainUtil#getCornerIds(Plot)
     */
    @Deprecated
    public Location[] getCorners() {
        return MainUtil.getCorners(this);
    }
    
    /**
     * @deprecated in favor of getCorners()[0];
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
     * Get a set of plots connected (and including) this plot<br>
     *  - This result is cached globally
     * @see MainUtil#getConnectedPlots(Plot)
     * @return
     */
    public Set<Plot> getConnectedPlots() {
        return MainUtil.getConnectedPlots(this);
    }
    
    /**
     * This will combine each plot into effective rectangular regions
     *  - This result is cached globally
     * @see MainUtil#getRegions(Plot) 
     * @return
     */
    public Set<RegionWrapper> getRegions() {
        return MainUtil.getRegions(this);
    }
    
    /**
     * Swap the plot contents and settings with another location<br>
     *  - The destination must correspond to a valid plot of equal dimensions
     * @see ChunkManager#swap(String, bot1, top1, bot2, top2) to swap terrain
     * @see MainUtil#getPlotSelectionIds(PlotId, PlotId) to get the plots inside a selection
     * @see MainUtil#swapData(String, PlotId, PlotId, Runnable) to swap plot settings
     * @param other The other plot to swap with
     * @param whenDone A task to run when finished, or null
     * @see MainUtil#swapData(String, PlotId, PlotId, Runnable)
     * @return boolean if swap was successful
     */
    public boolean swap(final Plot destination, final Runnable whenDone) {
        return MainUtil.move(this, destination, whenDone, true);
    }
    
    /**
     * Move the plot to an empty location<br>
     *  - The location must be empty
     * @param destination Where to move the plot
     * @param whenDone A task to run when done, or null
     * @return if the move was successful
     */
    public boolean move(final Plot destination, final Runnable whenDone) {
        return MainUtil.move(this, destination, whenDone, false);
    }
    
    /**
     * Copy the plot contents and settings to another location<br>
     *  - The destination must correspond to an empty location
     * @param destination The location to copy to
     * @param whenDone The task to run when done
     * @return If the copy was successful
     */
    public boolean copy(final Plot destination, final Runnable whenDone) {
        return MainUtil.copy(this, destination, whenDone);
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
        return world + ";" + getId().x + ";" + getId().y;
    }
    
    /**
     * Remove a denied player (use DBFunc as well)
     *
     * @param uuid
     */
    public boolean removeDenied(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (UUID other : new HashSet<>(getDenied())) {
                result = result || PlotHandler.removeDenied(this, other);
            }
            return result;
        }
        return PlotHandler.removeDenied(this, uuid);
    }
    
    /**
     * Remove a helper (use DBFunc as well)
     *
     * @param uuid
     */
    public boolean removeTrusted(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (UUID other : new HashSet<>(getTrusted())) {
                result = result || PlotHandler.removeTrusted(this, other);
            }
            return result;
        }
        return PlotHandler.removeTrusted(this, uuid);
    }
    
    /**
     * Remove a trusted user (use DBFunc as well)
     *
     * @param uuid
     */
    public boolean removeMember(final UUID uuid) {
        if (uuid == DBFunc.everyone) {
            boolean result = false;
            for (UUID other : new HashSet<>(getMembers())) {
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
        SchematicHandler.manager.getCompoundTag(world, id, new RunnableVal<CompoundTag>() {
            @Override
            public void run() {
                if (value == null) {
                    if (whenDone != null) {
                        whenDone.value = false;
                        TaskManager.runTask(whenDone);
                    }
                } else {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            final String name = id + "," + world + "," + MainUtil.getName(owner);
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
     * Upload the plot to the configured web interface
     * @param whenDone value will be null if uploading fails
     */
    public void upload(final RunnableVal<URL> whenDone) {
        SchematicHandler.manager.getCompoundTag(world, id, new RunnableVal<CompoundTag>() {
            @Override
            public void run() {
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
        return ((id.x.equals(other.id.x)) && (id.y.equals(other.id.y)) && (StringMan.isEqual(world, other.world)));
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

    public HashMap<String, Flag> getFlags() {
        if (settings == null) {
            return new HashMap<>(0);
        }
        return settings.flags;
    }

    public String getAlias() {
        if (settings == null) {
            return "";
        }
        return getSettings().getAlias();
    }

    /**
     * Set the raw merge data<br>
     *  - Updates DB
     *  - Does not modify terrain
     * @param merged
     */
    public void setMerged(boolean[] merged) {
        getSettings().merged = merged;
        DBFunc.setMerged(this, merged);
        MainUtil.connected_cache = null;
        MainUtil.regions_cache = null;
        if (origin != null) {
            origin.origin = null;
            origin = null;
        }
    }

    /**
     * Set the raw merge data<br>
     *  - Updates DB
     *  - Does not modify terrain
     * @param merged
     */
    public void setMerged(int direction, boolean value) {
        if (getSettings().setMerged(direction, value)) {
            if (value) {
                Plot other = MainUtil.getPlotRelative(this, direction).getBasePlot(false);
                if (!other.equals(getBasePlot(false))) {
                    Plot base = ((other.id.y < id.y) || ((other.id.y == id.y) && (other.id.x < id.x))) ? other : origin;
                    origin.origin = base;
                    other.origin = base;
                    origin = base;
                    MainUtil.connected_cache = null;
                }
            }
            else {
                if (origin != null) {
                    origin.origin = null;
                    origin = null;
                }
                MainUtil.connected_cache = null;
            }
            DBFunc.setMerged(this, getSettings().getMerged());
            MainUtil.regions_cache = null;
        }
    }

    public boolean[] getMerged() {
        if (settings == null) {
            return new boolean[] {false, false, false, false };
        }
        return settings.getMerged();
    }

    public BlockLoc getPosition() {
        if (settings == null) {
            return new BlockLoc(0, 0, 0);
        }
        return settings.getPosition();
    }
}
