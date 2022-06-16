/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.database;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.task.RunnableVal;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Database Functions
 * - These functions do not update the local plot objects and only make changes to the DB
 */
public class DBFunc {

    /**
     * The "global" uuid.
     */
    // TODO: Use this instead. public static final UUID EVERYONE = UUID.fromString("4aa2aaa4-c06b-485c-bc58-186aa1780d9b");
    public static final UUID EVERYONE = UUID.fromString("1-1-3-3-7");
    public static final UUID SERVER = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Abstract Database Manager
     */
    public static AbstractDB dbManager;

    public static void updateTables(int[] oldVersion) {
        if (dbManager != null) {
            dbManager.updateTables(oldVersion);
        }
    }

    public static void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete) {
        if (dbManager != null) {
            dbManager.addPersistentMeta(uuid, key, meta, delete);
        }
    }

    public static void getPersistentMeta(UUID uuid, RunnableVal<Map<String, byte[]>> result) {
        if (dbManager != null) {
            dbManager.getPersistentMeta(uuid, result);
        }
    }

    public static void removePersistentMeta(UUID uuid, String key) {
        if (dbManager != null) {
            dbManager.removePersistentMeta(uuid, key);
        }
    }

    public static CompletableFuture<Boolean> swapPlots(Plot plot1, Plot plot2) {
        if (dbManager != null) {
            return dbManager.swapPlots(plot1, plot2);
        }
        return CompletableFuture.completedFuture(false);
    }

    public static boolean deleteTables() {
        return dbManager != null && dbManager.deleteTables();
    }

    public static void movePlot(Plot originalPlot, Plot newPlot) {
        if (originalPlot.temp == -1 || newPlot.temp == -1) {
            return;
        }
        DBFunc.dbManager.movePlot(originalPlot, newPlot);
    }

    public static void validatePlots(Set<Plot> plots) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.validateAllPlots(plots);
    }


    //TODO Consider Removal

    /**
     * Check if a {@link ResultSet} contains a column.
     *
     * @param resultSet
     * @param name
     * @return
     */
    @Deprecated
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
        } catch (SQLException ignored) {
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
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setOwner(plot, uuid);
    }

    /**
     * Create all settings + (trusted, denied, members)
     *
     * @param plots List containing all plot objects
     */
    public static void createPlotsAndData(List<Plot> plots, Runnable whenDone) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.createPlotsAndData(plots, whenDone);
    }

    public static void createPlotSafe(
            final Plot plot, final Runnable success,
            final Runnable failure
    ) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.createPlotSafe(plot, success, failure);
    }

    /**
     * Create a plot.
     *
     * @param plot Plot to create
     */
    public static void createPlotAndSettings(Plot plot, Runnable whenDone) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.createPlotAndSettings(plot, whenDone);
    }

    /**
     * Create tables.
     *
     * @throws Exception
     */
    public static void createTables() throws Exception {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.createTables();
    }

    /**
     * Delete a plot.
     *
     * @param plot Plot to delete
     */
    public static void delete(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.delete(plot);
        plot.temp = -1;
    }

    /**
     * Delete the ratings for a plot.
     *
     * @param plot
     */
    public static void deleteRatings(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.deleteRatings(plot);
    }

    /**
     * Delete the trusted list for a plot.
     *
     * @param plot
     */
    public static void deleteTrusted(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.deleteHelpers(plot);
    }

    /**
     * Delete the members list for a plot.
     *
     * @param plot
     */
    public static void deleteMembers(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.deleteTrusted(plot);
    }

    /**
     * Delete the denied list for a plot.
     *
     * @param plot
     */
    public static void deleteDenied(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.deleteDenied(plot);
    }

    /**
     * Delete the comments in a plot.
     *
     * @param plot
     */
    public static void deleteComments(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.deleteComments(plot);
    }

    /**
     * Deleting settings will
     * 1) Delete any settings (flags and such) associated with the plot
     * 2) Prevent any local changes to the plot from saving properly to the db
     * <p>
     * This shouldn't ever be needed
     *
     * @param plot
     */
    public static void deleteSettings(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.deleteSettings(plot);
    }

    public static void delete(PlotCluster toDelete) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.delete(toDelete);
    }

    /**
     * Create plot settings.
     *
     * @param id   Plot ID
     * @param plot Plot Object
     */
    public static void createPlotSettings(int id, Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.createPlotSettings(id, plot);
    }

    /**
     * Get a plot id.
     *
     * @param plot Plot Object
     * @return ID
     */
    public static int getId(Plot plot) {
        if (dbManager == null) {
            return 0;
        }
        return DBFunc.dbManager.getId(plot);
    }

    /**
     * @return Plots
     */
    public static HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        if (dbManager == null) {
            return new HashMap<>();
        }
        return DBFunc.dbManager.getPlots();
    }

    public static void setMerged(Plot plot, boolean[] merged) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setMerged(plot, merged);
    }

    public static void setFlag(Plot plot, PlotFlag<?, ?> flag) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setFlag(plot, flag);
    }

    public static void removeFlag(Plot plot, PlotFlag<?, ?> flag) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeFlag(plot, flag);
    }

    /**
     * @param plot
     * @param alias
     */
    public static void setAlias(Plot plot, String alias) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setAlias(plot, alias);
    }

    public static void purgeIds(Set<Integer> uniqueIds) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.purgeIds(uniqueIds);
    }

    public static void purge(PlotArea area, Set<PlotId> plotIds) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.purge(area, plotIds);
    }

    /**
     * @param plot
     * @param position
     */
    public static void setPosition(Plot plot, String position) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setPosition(plot, position);
    }

    /**
     * @param plot
     * @param comment
     */
    public static void removeComment(Plot plot, PlotComment comment) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeComment(plot, comment);
    }

    public static void clearInbox(Plot plot, String inbox) {
        if (plot != null && plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.clearInbox(plot, inbox);
    }

    /**
     * @param plot
     * @param comment
     */
    public static void setComment(Plot plot, PlotComment comment) {
        if (plot != null && plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setComment(plot, comment);
    }

    /**
     * @param plot
     */
    public static void getComments(
            Plot plot, String inbox,
            RunnableVal<List<PlotComment>> whenDone
    ) {
        if (plot != null && plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.getComments(plot, inbox, whenDone);
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeTrusted(Plot plot, UUID uuid) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeTrusted(plot, uuid);
    }

    /**
     * @param cluster
     * @param uuid
     */
    public static void removeHelper(PlotCluster cluster, UUID uuid) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeHelper(cluster, uuid);
    }

    /**
     * @param cluster
     */
    public static void createCluster(PlotCluster cluster) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.createCluster(cluster);
    }

    /**
     * @param current
     * @param min
     * @param max
     */
    public static void resizeCluster(PlotCluster current, PlotId min, PlotId max) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.resizeCluster(current, min, max);
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeMember(Plot plot, UUID uuid) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeMember(plot, uuid);
    }

    /**
     * @param cluster
     * @param uuid
     */
    public static void removeInvited(PlotCluster cluster, UUID uuid) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeInvited(cluster, uuid);
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void setTrusted(Plot plot, UUID uuid) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setTrusted(plot, uuid);
    }

    public static void setHelper(PlotCluster cluster, UUID uuid) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.setHelper(cluster, uuid);
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void setMember(Plot plot, UUID uuid) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setMember(plot, uuid);
    }

    public static void setInvited(PlotCluster cluster, UUID uuid) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.setInvited(cluster, uuid);
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeDenied(Plot plot, UUID uuid) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.removeDenied(plot, uuid);
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void setDenied(Plot plot, UUID uuid) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setDenied(plot, uuid);
    }

    public static HashMap<UUID, Integer> getRatings(Plot plot) {
        if (plot.temp == -1 || dbManager == null) {
            return new HashMap<>(0);
        }
        return DBFunc.dbManager.getRatings(plot);
    }

    public static void setRating(Plot plot, UUID rater, int value) {
        if (plot.temp == -1 || dbManager == null) {
            return;
        }
        DBFunc.dbManager.setRating(plot, rater, value);
    }

    public static HashMap<String, Set<PlotCluster>> getClusters() {
        if (dbManager == null) {
            return new HashMap<>();
        }
        return DBFunc.dbManager.getClusters();
    }

    public static void setPosition(PlotCluster cluster, String position) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.setPosition(cluster, position);
    }

    public static void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.replaceWorld(oldWorld, newWorld, min, max);
    }

    /**
     * Replace all occurrences of a uuid in the database with another one
     *
     * @param old
     * @param now
     */
    public static void replaceUUID(UUID old, UUID now) {
        if (dbManager == null) {
            return;
        }
        DBFunc.dbManager.replaceUUID(old, now);
    }

    public static void close() {
        if (dbManager != null) {
            DBFunc.dbManager.close();
        }
    }

}
