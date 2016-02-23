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
package com.intellectualcrafters.plot.database;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.PlotComment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**


 */
public interface AbstractDB {
    
    /**
     * The UUID that will count as everyone
     */
    UUID everyone = UUID.fromString("1-1-3-3-7");
    
    /**
     * Set Plot owner
     *
     * @param plot Plot in which the owner should be set
     * @param uuid The uuid of the new owner
     */
    void setOwner(final Plot plot, final UUID uuid);
    
    /**
     * Create all settings, and create default helpers, trusted + denied lists
     *
     * @param plots Plots for which the default table entries should be created
     */
    void createPlotsAndData(final ArrayList<Plot> plots, final Runnable whenDone);
    
    /**
     * Create a plot
     *
     * @param plot That should be created
     */
    void createPlot(final Plot plot);
    
    /**
     * Create tables
     *
     * @throws SQLException If the database manager is unable to create the tables
     */
    void createTables() throws Exception;
    
    /**
     * Delete a plot
     *
     * @param plot Plot that should be deleted
     */
    void delete(final Plot plot);
    
    void deleteSettings(Plot plot);
    
    void deleteHelpers(Plot plot);
    
    void deleteTrusted(Plot plot);
    
    void deleteDenied(Plot plot);
    
    void deleteComments(Plot plot);
    
    void deleteRatings(Plot plot);

    void delete(final PlotCluster cluster);

    void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete);

    void removePersistentMeta(UUID uuid, String key);

    void getPersistentMeta(UUID uuid, RunnableVal<Map<String, byte[]>> result);

    /**
     * Create plot settings
     *
     * @param id   Plot Entry ID
     * @param plot Plot Object
     */
    void createPlotSettings(final int id, final Plot plot);
    
    /**
     * Get the table entry ID
     *
     * @param plot Plot Object
     *
     * @return Integer = Plot Entry Id
     */
    int getId(final Plot plot);
    
    /**
     * Get the id of a given plot cluster
     *
     * @param cluster PlotCluster Object
     *
     * @return Integer = Cluster Entry Id
     */
    int getClusterId(final PlotCluster cluster);
    
    /**
     * @return A linked hashmap containing all plots
     */
    HashMap<String, HashMap<PlotId, Plot>> getPlots();
    
    /**
     *
     */
    void validateAllPlots(final Set<Plot> toValidate);
    
    /**
     * @return A hashmap containing all plot clusters
     */
    HashMap<String, Set<PlotCluster>> getClusters();
    
    /**
     * Set the merged status for a plot
     *
     * @param plot   Plot Object
     * @param merged boolean[]
     */
    void setMerged(final Plot plot, final boolean[] merged);
    
    /**
     * Swap the settings, helpers etc. of two plots
     * @param p1 Plot1
     * @param p2 Plot2
     */
    void swapPlots(final Plot p1, final Plot p2);
    
    /**
     * Set plot flags
     *
     * @param plot  Plot Object
     * @param flags flags to set (flag[])
     */
    void setFlags(final Plot plot, final Collection<Flag> flags);
    
    /**
     * Set cluster flags
     *
     * @param cluster PlotCluster Object
     * @param flags flags to set (flag[])
     */
    void setFlags(final PlotCluster cluster, final Collection<Flag> flags);
    
    /**
     * Rename a cluster
     */
    void setClusterName(final PlotCluster cluster, final String name);
    
    /**
     * Set the plot alias
     *
     * @param plot  Plot for which the alias should be set
     * @param alias Plot Alias
     */
    void setAlias(final Plot plot, final String alias);
    
    /**
     * Purge a plot
     *
     * @param uniqueIds list of plot id (db) to be purged
     */
    void purgeIds(final Set<Integer> uniqueIds);
    
    /**
     * Purge a whole world
     *
     * @param area World in which the plots should be purged
     */
    void purge(final PlotArea area, final Set<PlotId> plotIds);
    
    /**
     * Set Plot Home Position
     *
     * @param plot     Plot Object
     * @param position Plot Home Position
     */
    void setPosition(final Plot plot, final String position);
    
    /**
     *
     * @param cluster
     * @param position
     */
    void setPosition(final PlotCluster cluster, final String position);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void removeTrusted(final Plot plot, final UUID uuid);
    
    /**
     * @param cluster   PlotCluster Object
     * @param uuid Player that should be removed
     */
    void removeHelper(final PlotCluster cluster, final UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void removeMember(final Plot plot, final UUID uuid);
    
    /**
     *
     * @param cluster
     * @param uuid
     */
    void removeInvited(final PlotCluster cluster, final UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void setTrusted(final Plot plot, final UUID uuid);
    
    /**
     * @param cluster PlotCluster Object
     * @param uuid Player that should be removed
     */
    void setHelper(final PlotCluster cluster, final UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be added
     */
    void setMember(final Plot plot, final UUID uuid);
    
    /**
     *
     * @param cluster
     * @param uuid
     */
    void setInvited(final PlotCluster cluster, final UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid   Player uuid
     */
    void removeDenied(final Plot plot, final UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player uuid that should be added
     */
    void setDenied(final Plot plot, final UUID uuid);
    
    /**
     * Get Plots ratings
     *
     * @param plot Plot Object
     *
     * @return Plot Ratings (pre-calculated)
     */
    HashMap<UUID, Integer> getRatings(final Plot plot);
    
    /**
     * Set a rating for a plot
     * @param plot
     * @param rater
     * @param value
     */
    void setRating(final Plot plot, final UUID rater, final int value);
    
    /**
     * Remove a plot comment
     *
     * @param plot    Plot Object
     * @param comment Comment to remove
     */
    void removeComment(final Plot plot, final PlotComment comment);
    
    /**
     * Clear an inbox
     * @param plot
     * @param inbox
     */
    void clearInbox(final Plot plot, final String inbox);
    
    /**
     * Set a plot comment
     *
     * @param plot    Plot Object
     * @param comment Comment to add
     */
    void setComment(final Plot plot, final PlotComment comment);
    
    /**
     * Get Plot Comments
     *
     * @param plot  Plot Object
     * @return Plot Comments within the specified tier
     */
    void getComments(final Plot plot, final String inbox, final RunnableVal<List<PlotComment>> whenDone);
    
    void createPlotAndSettings(final Plot plot, final Runnable whenDone);
    
    void createCluster(final PlotCluster cluster);
    
    void resizeCluster(final PlotCluster current, PlotId min, PlotId max);
    
    void movePlot(final Plot originalPlot, final Plot newPlot);
    
    /**
     * Replace a old uuid with a new one in the database<br>
     * - Useful for replacing a few uuids (not the entire database)<br>
     * - For entire conversion, the uuidconvert command scales better
     * @param old
     * @param now
     */
    void replaceUUID(final UUID old, final UUID now);
    
    /**
     * Don't fuck with this one, unless you enjoy it rough
     */
    boolean deleteTables();
    
    void close();
    
    void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max);
}
