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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AbstractDB {
    
    /**
     * The UUID that will count as everyone.
     */
    UUID everyone = UUID.fromString("1-1-3-3-7");
    
    /**
     * Set Plot owner.
     *
     * @param plot Plot in which the owner should be set
     * @param uuid The uuid of the new owner
     */
    void setOwner(Plot plot, UUID uuid);
    
    /**
     * Create all settings, and create default helpers, trusted + denied lists.
     *
     * @param plots Plots for which the default table entries should be created
     * @param whenDone
     */
    void createPlotsAndData(ArrayList<Plot> plots, Runnable whenDone);
    
    /**
     * Create a plot
     *
     * @param plot That should be created
     */
    void createPlot(Plot plot);
    
    /**
     * Create tables.
     *
     * @throws Exception If the database manager is unable to create the tables
     */
    void createTables() throws Exception;
    
    /**
     * Delete a plot.
     *
     * @param plot The plot to delete.
     */
    void delete(Plot plot);
    
    void deleteSettings(Plot plot);
    
    void deleteHelpers(Plot plot);
    
    void deleteTrusted(Plot plot);
    
    void deleteDenied(Plot plot);
    
    void deleteComments(Plot plot);
    
    void deleteRatings(Plot plot);

    void delete(PlotCluster cluster);

    void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete);

    void removePersistentMeta(UUID uuid, String key);

    void getPersistentMeta(UUID uuid, RunnableVal<Map<String, byte[]>> result);

    /**
     * Create plot settings.
     *
     * @param id   Plot Entry ID
     * @param plot Plot Object
     */
    void createPlotSettings(int id, Plot plot);
    
    /**
     * Get the table entry ID.
     *
     * @param plot Plot Object
     *
     * @return Integer = Plot Entry Id
     */
    int getId(Plot plot);
    
    /**
     * Get the id of a given plot cluster.
     *
     * @param cluster PlotCluster Object
     *
     * @return Integer = Cluster Entry Id
     */
    int getClusterId(PlotCluster cluster);
    
    /**
     * @return A linked HashMap containing all plots
     */
    HashMap<String, HashMap<PlotId, Plot>> getPlots();

    /**
     *
     * @param toValidate
     */
    void validateAllPlots(Set<Plot> toValidate);
    
    /**
     * @return A HashMap containing all plot clusters
     */
    HashMap<String, Set<PlotCluster>> getClusters();
    
    /**
     * Set the merged status for a plot.
     *
     * @param plot The plot to set the merged status of
     * @param merged boolean[]
     */
    void setMerged(Plot plot, boolean[] merged);
    
    /**
     * Swap the settings, helpers etc. of two plots.
     * @param plot1 Plot1
     * @param plot2 Plot2
     */
    void swapPlots(Plot plot1, Plot plot2);
    
    /**
     * Set plot flags.
     *
     * @param plot  Plot Object
     * @param flags flags to set (flag[])
     */
    void setFlags(Plot plot, Collection<Flag> flags);
    
    /**
     * Set cluster flags.
     *
     * @param cluster PlotCluster Object
     * @param flags flags to set (flag[])
     */
    void setFlags(PlotCluster cluster, Collection<Flag> flags);
    
    /**
     * Rename a cluster.
     */
    void setClusterName(PlotCluster cluster, String name);
    
    /**
     * Set the plot alias.
     *
     * @param plot  Plot for which the alias should be set
     * @param alias Plot Alias
     */
    void setAlias(Plot plot, String alias);
    
    /**
     * Purge a plot.
     *
     * @param uniqueIds list of plot id (db) to be purged
     */
    void purgeIds(Set<Integer> uniqueIds);
    
    /**
     * Purge a whole world.
     *
     * @param area World in which the plots should be purged
     * @param plotIds
     */
    void purge(PlotArea area, Set<PlotId> plotIds);
    
    /**
     * Set Plot Home Position.
     *
     * @param plot     Plot Object
     * @param position Plot Home Position
     */
    void setPosition(Plot plot, String position);
    
    /**
     *
     * @param cluster
     * @param position
     */
    void setPosition(PlotCluster cluster, String position);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void removeTrusted(Plot plot, UUID uuid);
    
    /**
     * @param cluster   PlotCluster Object
     * @param uuid Player that should be removed
     */
    void removeHelper(PlotCluster cluster, UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void removeMember(Plot plot, UUID uuid);

    /**
     *
     * @param cluster
     * @param uuid
     */
    void removeInvited(PlotCluster cluster, UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    void setTrusted(Plot plot, UUID uuid);
    
    /**
     * @param cluster PlotCluster Object
     * @param uuid Player that should be removed
     */
    void setHelper(PlotCluster cluster, UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player that should be added
     */
    void setMember(Plot plot, UUID uuid);
    
    /**
     *
     * @param cluster
     * @param uuid
     */
    void setInvited(PlotCluster cluster, UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid   Player uuid
     */
    void removeDenied(Plot plot, UUID uuid);
    
    /**
     * @param plot   Plot Object
     * @param uuid Player uuid that should be added
     */
    void setDenied(Plot plot, UUID uuid);
    
    /**
     * Get Plots ratings.
     *
     * @param plot Plot Object
     *
     * @return Plot Ratings (pre-calculated)
     */
    HashMap<UUID, Integer> getRatings(Plot plot);
    
    /**
     * Set a rating for a plot.
     * @param plot
     * @param rater
     * @param value
     */
    void setRating(Plot plot, UUID rater, int value);
    
    /**
     * Remove a plot comment.
     *
     * @param plot    Plot Object
     * @param comment Comment to remove
     */
    void removeComment(Plot plot, PlotComment comment);
    
    /**
     * Clear an inbox.
     *
     * @param plot
     * @param inbox
     */
    void clearInbox(Plot plot, String inbox);
    
    /**
     * Set a plot comment.
     *
     * @param plot    Plot Object
     * @param comment Comment to add
     */
    void setComment(Plot plot, PlotComment comment);
    
    /**
     * Get Plot Comments.
     *
     * @param plot The Plot to get comments from
     */
    void getComments(Plot plot, String inbox, RunnableVal<List<PlotComment>> whenDone);

    void createPlotAndSettings(Plot plot, Runnable whenDone);

    void createCluster(PlotCluster cluster);

    void resizeCluster(PlotCluster current, PlotId min, PlotId max);

    void movePlot(Plot originalPlot, Plot newPlot);
    
    /**
     * Replace a old uuid with a new one in the database.
     *
     * <ul>
     *  <li> Useful for replacing a few uuids (not the entire database).</li>
     *  <li>or entire conversion, the uuidconvert command scales better.</li>
     * </ul>
     * @param old
     * @param now
     */
    void replaceUUID(UUID old, UUID now);
    
    /**
     * Don't use this method unless you want to ruin someone's server.
     * @return true if the tables were deleted, false when an error is encountered
     */
    boolean deleteTables();
    
    void close();
    
    void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max);

    void updateTables(int[] oldVersion);
}
