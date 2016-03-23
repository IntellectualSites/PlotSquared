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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public static void movePlot(Plot originalPlot, Plot newPlot) {
        if ((originalPlot.temp == -1) || (newPlot.temp == -1)) {
            return;
        }
        dbManager.movePlot(originalPlot, newPlot);
    }

    public static void validatePlots(Set<Plot> plots) {
        dbManager.validateAllPlots(plots);
    }
    
    /**
     * Check if a {@link ResultSet} contains a column.
     * @param resultSet
     * @param name
     * @return
     */
    public static boolean hasColumn(ResultSet resultSet, String name) {
        try {
            ResultSetMetaData meta = resultSet.getMetaData();
            int count = meta.getColumnCount();
            for (int x = 1; x <= count; x++) {
                if (name.equals(meta.getColumnName(x))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Set the owner of a plot
     *
     * @param plot Plot Object
     * @param uuid New Owner
     */
    public static void setOwner(Plot plot, UUID uuid) {
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
    public static void createPlotsAndData(ArrayList<Plot> plots, Runnable whenDone) {
        dbManager.createPlotsAndData(plots, whenDone);
    }
    
    /**
     * Create a plot
     *
     * @param plot Plot to create
     */
    public static void createPlot(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.createPlot(plot);
    }
    
    /**
     * Create a plot.
     *
     * @param plot Plot to create
     */
    public static void createPlotAndSettings(Plot plot, Runnable whenDone) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.createPlotAndSettings(plot, whenDone);
    }
    
    /**
     * Create tables.
     *
     * @throws Exception
     */
    public static void createTables(String database) throws Exception {
        dbManager.createTables();
    }
    
    /**
     * Delete a plot.
     *
     * @param plot Plot to delete
     */
    public static void delete(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.delete(plot);
        plot.temp = -1;
    }
    
    /**
     * Delete the ratings for a plot.
     * @param plot
     */
    public static void deleteRatings(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteRatings(plot);
    }
    
    /**
     * Delete the trusted list for a plot.
     * @param plot
     */
    public static void deleteTrusted(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteHelpers(plot);
    }
    
    /**
     * Delete the members list for a plot.
     * @param plot
     */
    public static void deleteMembers(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteTrusted(plot);
    }
    
    /**
     * Delete the denied list for a plot.
     * @param plot
     */
    public static void deleteDenied(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteDenied(plot);
    }
    
    /**
     * Delete the comments in a plot.
     * @param plot
     */
    public static void deleteComments(Plot plot) {
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
    public static void deleteSettings(Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.deleteSettings(plot);
    }

    public static void delete(PlotCluster toDelete) {
        dbManager.delete(toDelete);
    }
    
    /**
     * Create plot settings.
     *
     * @param id   Plot ID
     * @param plot Plot Object
     */
    public static void createPlotSettings(int id, Plot plot) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.createPlotSettings(id, plot);
    }
    
    /**
     * Get a plot id.
     *
     * @param plot Plot Object
     *
     * @return ID
     */
    public static int getId(Plot plot) {
        return dbManager.getId(plot);
    }
    
    /**
     * @return Plots
     */
    public static HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        return dbManager.getPlots();
    }

    public static void setMerged(Plot plot, boolean[] merged) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setMerged(plot, merged);
    }

    public static void setFlags(Plot plot, Collection<Flag> flags) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setFlags(plot, flags);
    }

    public static void setFlags(PlotCluster cluster, Collection<Flag> flags) {
        dbManager.setFlags(cluster, flags);
    }
    
    /**
     * @param plot
     * @param alias
     */
    public static void setAlias(Plot plot, String alias) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setAlias(plot, alias);
    }

    public static void purgeIds(Set<Integer> uniqueIds) {
        dbManager.purgeIds(uniqueIds);
    }

    public static void purge(PlotArea area, Set<PlotId> plotIds) {
        dbManager.purge(area, plotIds);
    }
    
    /**
     * @param plot
     * @param position
     */
    public static void setPosition(Plot plot, String position) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setPosition(plot, position);
    }
    
    /**
     * @param plot
     * @param comment
     */
    public static void removeComment(Plot plot, PlotComment comment) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.removeComment(plot, comment);
    }

    public static void clearInbox(Plot plot, String inbox) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.clearInbox(plot, inbox);
    }
    
    /**
     * @param plot
     * @param comment
     */
    public static void setComment(Plot plot, PlotComment comment) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.setComment(plot, comment);
    }
    
    /**
     * @param plot
     */
    public static void getComments(Plot plot, String inbox, RunnableVal<List<PlotComment>> whenDone) {
        if ((plot != null) && (plot.temp == -1)) {
            return;
        }
        dbManager.getComments(plot, inbox, whenDone);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void removeTrusted(Plot plot, UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.removeTrusted(plot, uuid);
    }
    
    /**
     * @param cluster
     * @param uuid
     */
    public static void removeHelper(PlotCluster cluster, UUID uuid) {
        dbManager.removeHelper(cluster, uuid);
    }
    
    /**
     * @param cluster
     */
    public static void createCluster(PlotCluster cluster) {
        dbManager.createCluster(cluster);
    }
    
    /**
     * @param current
     * @param min
     * @param max
     */
    public static void resizeCluster(PlotCluster current, PlotId min, PlotId max) {
        dbManager.resizeCluster(current, min, max);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void removeMember(Plot plot, UUID uuid) {
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
    public static void removeInvited(PlotCluster cluster, UUID uuid) {
        dbManager.removeInvited(cluster, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void setTrusted(Plot plot, UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setTrusted(plot, uuid);
    }

    public static void setHelper(PlotCluster cluster, UUID uuid) {
        dbManager.setHelper(cluster, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void setMember(Plot plot, UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setMember(plot, uuid);
    }

    public static void setInvited(PlotCluster cluster, UUID uuid) {
        dbManager.setInvited(cluster, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void removeDenied(Plot plot, UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.removeDenied(plot, uuid);
    }
    
    /**
     * @param plot
     * @param uuid
     */
    public static void setDenied(Plot plot, UUID uuid) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setDenied(plot, uuid);
    }

    public static HashMap<UUID, Integer> getRatings(Plot plot) {
        if (plot.temp == -1) {
            return new HashMap<>(0);
        }
        return dbManager.getRatings(plot);
    }

    public static void setRating(Plot plot, UUID rater, int value) {
        if (plot.temp == -1) {
            return;
        }
        dbManager.setRating(plot, rater, value);
    }
    
    public static HashMap<String, Set<PlotCluster>> getClusters() {
        return dbManager.getClusters();
    }

    public static void setPosition(PlotCluster cluster, String position) {
        dbManager.setPosition(cluster, position);
    }
    
    public static void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max) {
        dbManager.replaceWorld(oldWorld, newWorld, min, max);
    }
    
    /**
     * Replace all occurrences of a uuid in the database with another one
     * @param old
     * @param now
     */
    public static void replaceUUID(UUID old, UUID now) {
        dbManager.replaceUUID(old, now);
    }
    
    public static void close() {
        dbManager.close();
    }
}
