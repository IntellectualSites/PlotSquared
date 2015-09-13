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
     *@see MainUtil#getPlot(String, PlotId) for existing plots
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
     * @see MainUtil#getPlot(String, PlotId) for existing plots
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
     * @see MainUtil#getPlot(String, PlotId) for existing plots
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
        settings = new PlotSettings(this);
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
        Flag flag = FlagManager.getPlotFlag(this, "cluster");
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
     * Get the world
     * @return
     */
    public String getWorld() {
        return world;
    }
    
    /**
     * Get or create plot settings
     * @return PlotSettings
     */
    public PlotSettings getSettings() {
        if (settings == null) {
            settings = new PlotSettings(this);
        }
        return settings;
    }
    
    /**
     * Returns true if the plot is not merged, or it is the base plot of multiple merged plots
     * @return
     */
    public boolean isBasePlot() {
        if (settings == null) {
            return true;
        }
        return !settings.getMerged(0) && !settings.getMerged(3);
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
        return settings.getMerged(direction);
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
        if (getDenied().add(uuid)) {
            DBFunc.setDenied(this, uuid);
        }
    }
    
    /**
     * Add someone as a helper (updates database as well)
     *
     * @param uuid
     */
    public void addTrusted(final UUID uuid) {
        if (getTrusted().add(uuid)) {
            DBFunc.setTrusted(this, uuid);
        }
    }
    
    /**
     * Add someone as a trusted user (updates database as well)
     *
     * @param uuid
     */
    public void addMember(final UUID uuid) {
        if (getMembers().add(uuid)) {
            DBFunc.setMember(this, uuid);
        }
    }
    
    /**
     * Set the plot owner (and update the database)
     * @param owner
     */
    public void setOwner(final UUID owner) {
        if (!this.owner.equals(owner)) {
            this.owner = owner;
            DBFunc.setOwner(this, owner);
        }
    }
    
    /**
     * Set the trusted users for this plot
     * @param uuids
     */
    public void setTrusted(final Set<UUID> uuids) {
        if (uuids.size() == 0) {
            return;
        }
        if ((trusted != null) && (trusted.size() > 0)) {
            trusted.removeAll(uuids);
            for (final UUID uuid : trusted) {
                DBFunc.removeTrusted(this, uuid);
            }
            trusted.clear();
        }
        for (final UUID uuid : uuids) {
            addTrusted(uuid);
        }
    }
    
    /**
     * Set the members for this plot
     * @param uuids
     */
    public void setMembers(final Set<UUID> uuids) {
        if (uuids.size() == 0) {
            return;
        }
        if ((members != null) && (members.size() > 0)) {
            members.removeAll(uuids);
            for (final UUID uuid : members) {
                DBFunc.removeMember(this, uuid);
            }
            members.clear();
        }
        for (final UUID uuid : uuids) {
            addMember(uuid);
        }
    }
    
    /**
     * Set the denied users for this plot
     * @param uuids
     */
    public void setDenied(final Set<UUID> uuids) {
        if (uuids.size() == 0) {
            return;
        }
        if ((denied != null) && (denied.size() > 0)) {
            denied.removeAll(uuids);
            for (final UUID uuid : denied) {
                DBFunc.removeDenied(this, uuid);
            }
            denied.clear();
        }
        for (final UUID uuid : uuids) {
            addDenied(uuid);
        }
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
        return FlagManager.getPlotFlag(this, key);
    }
    
    /**
     * Delete a plot (use null for the runnable if you don't need to be notified on completion)
     * @see PS#removePlot(String, PlotId, boolean)
     * @see #clear(Runnable) to simply clear a plot
     */
    public void deletePlot(final Runnable whenDone) {
        MainUtil.removeSign(this);
        MainUtil.clear(this, true, new Runnable() {
            @Override
            public void run() {
                if (PS.get().removePlot(world, id, true)) {
                    DBFunc.delete(Plot.this);
                    TaskManager.runTask(whenDone);
                }
            }
        });
    }
    
    public boolean unclaim() {
        if (PS.get().removePlot(world, id, true)) {
            DBFunc.delete(Plot.this);
            return true;
        }
        return false;
    }
    
    /**
     * Unlink a plot and remove the roads
     * @see MainUtil#unlinkPlot(Plot, boolean removeRoad)
     * @return true if plot was linked
     */
    public boolean unlink() {
        return MainUtil.unlinkPlot(this, true);
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
     * Get the average rating of the plot
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
     * Get the ratings associated with a plot<br>
     *  - The rating object may contain multiple categories
     * @return Map of user who rated to the rating
     */
    public HashMap<UUID, Rating> getRatings() {
        final HashMap<UUID, Rating> map = new HashMap<UUID, Rating>();
        if (getSettings().ratings == null) {
            return map;
        }
        for (final Entry<UUID, Integer> entry : getSettings().ratings.entrySet()) {
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
        if (((pos == null) && (loc == null)) || ((pos != null) && pos.equals(loc))) {
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
        final String name = getSettings().getAlias();
        if (alias == null) {
            alias = "";
        }
        if (name.equals(alias)) {
            return;
        }
        getSettings().setAlias(alias);
        DBFunc.setAlias(this, alias);
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
    public void autoMerge(final boolean removeRoads) {
        MainUtil.autoMerge(this, owner, removeRoads);
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
    public void setComponent(final String component, final PlotBlock... blocks) {
        MainUtil.setComponent(this, component, blocks);
    }
    
    /**
     * Set components such as border, wall, floor
     *  (components are generator specific)
     */
    public void setComponent(final String component, final String blocks) {
        MainUtil.setComponent(this, component, Configuration.BLOCKLIST.parseString(blocks));
    }
    
    /**
     * Get the biome (String)
     */
    public String getBiome() {
        final Location loc = getBottom();
        return BlockManager.manager.getBiome(loc.getWorld(), loc.getX(), loc.getZ());
    }
    
    /**
     * Return the top location for the plot
     * @return
     */
    public Location getTop() {
        return MainUtil.getPlotTopLoc(world, id);
    }
    
    /**
     * Return the bottom location for the plot
     * @return
     */
    public Location getBottom() {
        return MainUtil.getPlotBottomLoc(world, id).add(1, 0, 1);
    }
    
    /**
     * Get the top plot, or this plot if it is not part of a mega plot
     * @return The bottom plot
     */
    public Plot getTopPlot() {
        return MainUtil.getTopPlot(this);
    }
    
    /**
     * Get the bottom plot, or this plot if it is not part of a mega plot
     * @return The bottom plot
     */
    public Plot getBottomPlot() {
        return MainUtil.getBottomPlot(this);
    }
    
    /**
     * Swap the plot contents and settings with another location<br>
     *  - The destination must correspond to a valid plot of equal dimensions
     * @see ChunkManager#swap(String, bot1, top1, bot2, top2) to swap terrain
     * @see MainUtil#getPlotSelectionIds(PlotId, PlotId) to get the plots inside a selection
     * @see MainUtil#swapData(String, PlotId, PlotId, Runnable) to swap plot settings
     * @param other The other plot id to swap with
     * @param whenDone A task to run when finished, or null
     * @see MainUtil#swapData(String, PlotId, PlotId, Runnable)
     * @return boolean if swap was successful
     */
    public boolean swap(final PlotId destination, final Runnable whenDone) {
        return MainUtil.swap(world, id, destination, whenDone);
    }
    
    /**
     * Move the plot to an empty location<br>
     *  - The location must be empty
     * @param destination Where to move the plot
     * @param whenDone A task to run when done, or null
     * @return if the move was successful
     */
    public boolean move(final Plot destination, final Runnable whenDone) {
        return MainUtil.move(this, destination, whenDone);
    }
    
    /**
     * Copy the plot contents and settings to another location<br>
     *  - The destination must correspond to an empty location
     * @param destination The location to copy to
     * @param whenDone The task to run when done
     * @return If the copy was successful
     */
    public boolean copy(final PlotId destination, final Runnable whenDone) {
        return MainUtil.copy(world, id, destination, whenDone);
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
        if (getDenied().remove(uuid)) {
            DBFunc.removeDenied(this, uuid);
            return true;
        }
        return false;
    }
    
    /**
     * Remove a helper (use DBFunc as well)
     *
     * @param uuid
     */
    public boolean removeTrusted(final UUID uuid) {
        if (getTrusted().remove(uuid)) {
            DBFunc.removeTrusted(this, uuid);
            return true;
        }
        return false;
    }
    
    /**
     * Remove a trusted user (use DBFunc as well)
     *
     * @param uuid
     */
    public boolean removeMember(final UUID uuid) {
        if (getMembers().remove(uuid)) {
            DBFunc.removeMember(this, uuid);
            return true;
        }
        return false;
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
}
