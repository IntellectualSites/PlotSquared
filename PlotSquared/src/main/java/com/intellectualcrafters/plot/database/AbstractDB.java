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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotComment;
import com.intellectualcrafters.plot.object.PlotId;

/**
 * @author Citymonstret
 * @author Empire92
 */
public interface AbstractDB {
    // TODO MongoDB @Brandon
    /**
     * The UUID that will count as everyone
     */
    public UUID everyone = UUID.fromString("1-1-3-3-7");

    /**
     * Set Plot owner
     *
     * @param plot Plot in which the owner should be set
     * @param uuid The uuid of the new owner
     */
    public void setOwner(final Plot plot, final UUID uuid);

    /**
     * Create all settings, and create default helpers, trusted + denied lists
     *
     * @param plots Plots for which the default table entries should be created
     */
    public void createAllSettingsAndHelpers(final ArrayList<Plot> plots);

    /**
     * Create a plot
     *
     * @param plots Plots that should be created
     */
    public void createPlots(final ArrayList<Plot> plots);

    /**
     * Create a plot
     *
     * @param plot That should be created
     */
    public void createPlot(final Plot plot);

    /**
     * Create tables
     *
     * @param database Database in which the tables will be created
     *
     * @throws SQLException If the database manager is unable to create the tables
     */
    public void createTables(final String database, final boolean add_constraint) throws Exception;

    /**
     * Delete a plot
     *
     * @param plot Plot that should be deleted
     */
    public void delete(final String world, final Plot plot);

    public void delete(final PlotCluster cluster);

    /**
     * Create plot settings
     *
     * @param id   Plot Entry ID
     * @param plot Plot Object
     */
    public void createPlotSettings(final int id, final Plot plot);

    /**
     * Get the table entry ID
     *
     * @param world Which the plot is located in
     * @param id2   Plot ID
     *
     * @return Integer = Plot Entry Id
     */
    public int getId(final String world, final PlotId id2);

    /**
     * Get the id of a given plot cluster
     *
     * @param world Which the plot is located in
     * @param pos1   bottom Plot ID
     * @param pos2	 top Plot ID
     *
     * @return Integer = Cluster Entry Id
     */
    public int getClusterId(final String world, final PlotClusterId id);

    /**
     * @return A linked hashmap containing all plots
     */
    public LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots();

    /**
     * @return A hashmap containing all plot clusters
     */
    public HashMap<String, HashSet<PlotCluster>> getClusters();

    /**
     * Set the merged status for a plot
     *
     * @param world  World in which the plot is located
     * @param plot   Plot Object
     * @param merged boolean[]
     */
    public void setMerged(final String world, final Plot plot, final boolean[] merged);

    /**
     * Swap the settings, helpers etc. of two plots
     * @param p1 Plot1
     * @param p2 Plot2
     */
    public void swapPlots(final Plot p1, final Plot p2);

    /**
     * Set plot flags
     *
     * @param world World in which the plot is located
     * @param plot  Plot Object
     * @param flags flags to set (flag[])
     */
    public void setFlags(final String world, final Plot plot, final Set<Flag> flags);

    /**
     * Set cluster flags
     *
     * @param world World in which the plot is located
     * @param cluster PlotCluster Object
     * @param flags flags to set (flag[])
     */
    public void setFlags(final PlotCluster cluster, final Set<Flag> flags);

    /**
     * Rename a cluster
     */
    public void setClusterName(final PlotCluster cluster, final String name);

    /**
     * Set the plot alias
     *
     * @param plot  Plot for which the alias should be set
     * @param alias Plot Alias
     */
    public void setAlias(final String world, final Plot plot, final String alias);

    /**
     * Purgle a plot
     *
     * @param world World in which the plot is located
     * @param id    Plot ID
     */
    public void purgeIds(final String world, final Set<Integer> uniqueIds);

    /**
     * Purge a whole world
     *
     * @param world World in which the plots should be purged
     */
    public void purge(final String world, final Set<PlotId> plotIds);

    /**
     * Set Plot Home Position
     *
     * @param plot     Plot Object
     * @param position Plot Home Position
     */
    public void setPosition(final String world, final Plot plot, final String position);

    /**
     *
     * @param cluster
     * @param position
     */
    public void setPosition(final PlotCluster cluster, final String position);

    /**
     * @param id Plot Entry ID
     *
     * @return Plot Settings
     */
    public HashMap<String, Object> getSettings(final int id);

    /**
     *
     * @param id
     * @return
     */
    public HashMap<String, Object> getClusterSettings(final int id);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    public void removeHelper(final String world, final Plot plot, final UUID uuid);

    /**
     * @param cluster   PlotCluster Object
     * @param uuid Player that should be removed
     */
    public void removeHelper(final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    public void removeTrusted(final String world, final Plot plot, final UUID uuid);

    /**
     *
     * @param cluster
     * @param uuid
     */
    public void removeInvited(final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be removed
     */
    public void setHelper(final String world, final Plot plot, final UUID uuid);

    /**
     * @param cluster PlotCluster Object
     * @param uuid Player that should be removed
     */
    public void setHelper(final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param uuid Player that should be added
     */
    public void setTrusted(final String world, final Plot plot, final UUID uuid);

    /**
     *
     * @param world
     * @param cluster
     * @param uuid
     */
    public void setInvited(final String world, final PlotCluster cluster, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param player Player that should be added
     */
    public void removeDenied(final String world, final Plot plot, final UUID uuid);

    /**
     * @param plot   Plot Object
     * @param player Player that should be added
     */
    public void setDenied(final String world, final Plot plot, final UUID uuid);

    /**
     * Get Plots ratings
     *
     * @param plot Plot Object
     *
     * @return Plot Ratings (pre-calculated)
     */
    public double getRatings(final Plot plot);

    /**
     * Remove a plot comment
     *
     * @param world   World in which the plot is located
     * @param plot    Plot Object
     * @param comment Comment to remove
     */
    public void removeComment(final String world, final Plot plot, final PlotComment comment);

    /**
     * Set a plot comment
     *
     * @param world   World in which the plot is located
     * @param plot    Plot Object
     * @param comment Comment to add
     */
    public void setComment(final String world, final Plot plot, final PlotComment comment);

    /**
     * Get Plot Comments
     *
     * @param world World in which the plot is located
     * @param plot  Plot Object
     * @param tier  Comment Tier
     *
     * @return Plot Comments within the specified tier
     */
    public ArrayList<PlotComment> getComments(final String world, final Plot plot, final int tier, boolean below);

    public void createPlotAndSettings(Plot plot);

    public void createCluster(PlotCluster cluster);

    public void resizeCluster(PlotCluster current, PlotClusterId resize);

    public void movePlot(String world, PlotId originalPlot, PlotId newPlot);
}
