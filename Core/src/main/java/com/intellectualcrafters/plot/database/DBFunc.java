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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.PlotComment;

/**
 * Database Functions
 *  - These functions do not update the local plot objects and only make changes to the DB
 */
public class DBFunc {
    /**
     * The "global" uuid
     */
    public static final UUID everyone = UUID.fromString("1-1-3-3-7");
    /**
     * Abstract Database Manager
     */
    public static AbstractDB dbManager;
    
    public static void movePlot(final Plot originalPlot, final Plot newPlot) {
        if ((originalPlot.temp == -1) || (newPlot.temp == -1)) {
            return;
        }
        dbManager.movePlot(originalPlot, newPlot);
    }
    
    public static void validatePlots(final Set<Plot> plots) {
        dbManager.validateAllPlots(plots);
    }
    
    /**
     * Check if a resultset contains a column
     * @param r
     * @param name
     * @return
     * @throws SQLException
     */
    public static boolean hasColumn(final ResultSet r, final String name) {
        try {
            final ResultSetMetaData meta = r.getMetaData();
            final int count = meta.getColumnCount();
            for (int x = 1; x <= count; x++) {
                if (name.equals(meta.getColumnName(x))) {
                    return true;
                }
            }
            return false;
        } catch (final SQLException e) {
            return false;
        }
    }
    
    /**
     * Set the owner of a plot
     *
     * @param plot Plot Object
     * @param uuid New Owner
     */
    public static void setOwner(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setOwner(plot, uuid);
    }
    
    /**
     * Create all settings + (trusted, denied, members)
     *
     * @param plots List containing all plot objects
     */
    public static void createPlotsAndData(final ArrayList<Plot> plots, final Runnable whenDone) {
        dbManager.createPlotsAndData(plots, whenDone);
    }
    
    /**
     * Create a plot
     *
     * @param plot Plot to create
     */
    public static void createPlot(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.createPlot(plot);
    }
    
    /**
     * Create a plot
     *
     * @param plot Plot to create
     */
    public static void createPlotAndSettings(final Plot plot, final Runnable whenDone) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.createPlotAndSettings(plot, whenDone);
    }
    
    /**
     * Create tables
     *
     * @throws Exception
     */
    public static void createTables(final String database) throws Exception {
        dbManager.createTables();
    }
    
    /**
     * Delete a plot
     *
     * @param plot Plot to delete
     */
    public static void delete(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.delete(plot);
        plot.temp = -1;
    }
    
    /**
     * Delete the ratings for a plot
     * @param plot
     */
    public static void deleteRatings(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteRatings(plot);
    }
    
    /**
     * Delete the trusted list for a plot
     * @param plot
     */
    public static void deleteTrusted(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteHelpers(plot);
    }
    
    /**
     * Delete the members list for a plot
     * @param plot
     */
    public static void deleteMembers(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteTrusted(plot);
    }
    
    /**
     * Delete the denied list for a plot
     * @param plot
     */
    public static void deleteDenied(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteDenied(plot);
    }
    
    /**
     * Delete the comments in a plot
     * @param plot
     */
    public static void deleteComments(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteComments(plot);
    }
    
    /**
     * Deleting settings will 
     * 1) Delete any settings (flags and such) associated with the plot
     * 2) Prevent any local changes to the plot from saving properly to the db
     * 
     * This shouldn't ever be needed
     * @param plot
     */
    public static void deleteSettings(final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteSettings(plot);
    }

    public static void delete(final PlotCluster toDelete) {
        dbManager.delete(toDelete);
    }
    
    /**
     * Create plot settings
     *
     * @param id   Plot ID
     * @param plot Plot Object
     */
    public static void createPlotSettings(final int id, final Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.createPlotSettings(id, plot);
    }
    
    /**
     * Get a plot id
     *
     * @param plot Plot Object
     *
     * @return ID
     */
    /*
     * public static int getId(String plotId id2) { Statement stmt =
     * null; try { stmt = connection.createStatement(); ResultSet r =
     * stmt.executeQuery("SELECT `id` FROM `plot` WHERE `plot_id_x` = '" + id2.x
     * + "' AND `plot_id_z` = '" + id2.y + "' AND `world` = '" + world +
     * "' ORDER BY `timestamp` ASC"); int id = Integer.MAX_VALUE;
     * while(r.next()) { id = r.getInt("id"); } stmt.close(); return id; }
     * catch(SQLException e) { e.printStackTrace(); } return Integer.MAX_VALUE;
     * }
     */
    public static int getId(final Plot plot) {
        return dbManager.getId(plot);
    }
    
    /**
     * @return Plots
     */
    public static ConcurrentHashMap<String, ConcurrentHashMap<PlotId, Plot>> getPlots() {
        return dbManager.getPlots();
    }
    
    public static void setMerged(final Plot plot, final boolean[] merged) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setMerged(plot, merged);
    }
    
    public static void setFlags(final Plot plot, final Collection<Flag> flags) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setFlags(plot, flags);
    }
    
    public static void setFlags(final PlotCluster cluster, final Collection<Flag> flags) {
        dbManager.setFlags(cluster, flags);
    }
    
    /**
     * @param plot
     * @param alias
     */
    public static void setAlias(final Plot plot, final String alias) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setAlias(plot, alias);
    }
    
    public static void purgeIds(final String world, final Set<Integer> uniqueIds) {
        dbManager.purgeIds(world, uniqueIds);
    }
    
    public static void purge(final String world, final Set<PlotId> plotIds) {
        dbManager.purge(world, plotIds);
    }
    
    /**
     * @param plot
     * @param position
     */
    public static void setPosition(final Plot plot, final String position) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setPosition(plot, position);
    }
    
    /**
     * @param plot
     * @param comment
     */
    public static void removeComment(final Plot plot, final PlotComment comment) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.removeComment(plot, comment);
    }
    
    public static void clearInbox(final Plot plot, final String inbox) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.clearInbox(plot, inbox);
    }
    
    /**
     * @param plot
     * @param comment
     */
    public static void setComment(final Plot plot, final PlotComment comment) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.setComment(plot, comment);
    }
    
    /**
     * @param plot
     */
    public static void getComments(final Plot plot, final String inbox, final RunnableVal whenDone) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.getComments(plot, inbox, whenDone);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void removeTrusted(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.removeTrusted(plot, uuid);
    }
    
    /**
     * @param cluster
     * @param uuid
     */
    public static void removeHelper(final PlotCluster cluster, final UUID uuid) {
        dbManager.removeHelper(cluster, uuid);
    }
    
    /**
     * @param world
     * @param cluster
     */
    public static void createCluster(final String world, final PlotCluster cluster) {
        dbManager.createCluster(cluster);
    }
    
    /**
     * @param current
     * @param resize
     */
    public static void resizeCluster(final PlotCluster current, final PlotClusterId resize) {
        dbManager.resizeCluster(current, resize);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void removeMember(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.removeMember(plot, uuid);
    }
    
    /**
     *
     * @param cluster
     * @param uuid
     */
    public static void removeInvited(final PlotCluster cluster, final UUID uuid) {
        dbManager.removeInvited(cluster, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void setTrusted(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setTrusted(plot, uuid);
    }
    
    public static void setHelper(final PlotCluster cluster, final UUID uuid) {
        dbManager.setHelper(cluster, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void setMember(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setMember(plot, uuid);
    }
    
    public static void setInvited(final String world, final PlotCluster cluster, final UUID uuid) {
        dbManager.setInvited(cluster, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void removeDenied(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.removeDenied(plot, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void setDenied(final Plot plot, final UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setDenied(plot, uuid);
    }
    
    public static HashMap<UUID, Integer> getRatings(final Plot plot) {
        if (plot.temp == -1) {
            return new HashMap<>(0);
        }
        return dbManager.getRatings(plot);
    }
    
    public static void setRating(final Plot plot, final UUID rater, final int value) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setRating(plot, rater, value);
    }
    
    public static HashMap<String, HashSet<PlotCluster>> getClusters() {
        return dbManager.getClusters();
    }
    
    public static void setPosition(final PlotCluster cluster, final String position) {
        dbManager.setPosition(cluster, position);
    }
    
    /**
     * Replace all occurances of a uuid in the database with another one
     * @param old
     * @param now
     */
    public static void replaceUUID(final UUID old, final UUID now) {
        dbManager.replaceUUID(old, now);
    }
    
    public static void close() {
        dbManager.close();
    }
}
