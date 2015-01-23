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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotComment;
import com.intellectualcrafters.plot.object.PlotId;

/**
 * DB Functions
 *
 * @author Empire92
 * @author Citymonstret
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

    /**
     * Set the owner of a plot
     *
     * @param plot Plot Object
     * @param uuid New Owner
     */
    public static void setOwner(final Plot plot, final UUID uuid) {
        dbManager.setOwner(plot, uuid);
    }

    /**
     * Create all settings + (helpers, denied, trusted)
     *
     * @param plots List containing all plot objects
     */
    public static void createAllSettingsAndHelpers(final ArrayList<Plot> plots) {
        dbManager.createAllSettingsAndHelpers(plots);
    }

    /**
     * Create all plots
     *
     * @param plots A list containing plot objects
     */
    public static void createPlots(final ArrayList<Plot> plots) {
        dbManager.createPlots(plots);
    }

    /**
     * Create a plot
     *
     * @param plot Plot to create
     */
    public static void createPlot(final Plot plot) {
        dbManager.createPlot(plot);
    }
    
    /**
     * Create a plot
     *
     * @param plot Plot to create
     */
    public static void createPlotAndSettings(final Plot plot) {
        dbManager.createPlotAndSettings(plot);
    }

    /**
     * Create tables
     *
     * @throws Exception
     */
    public static void createTables(final String database, final boolean add_constraint) throws Exception {
        dbManager.createTables(database, add_constraint);
    }

    /**
     * Delete a plot
     *
     * @param plot Plot to delete
     */
    public static void delete(final String world, final Plot plot) {
        dbManager.delete(world, plot);
    }

    /**
     * Create plot settings
     *
     * @param id   Plot ID
     * @param plot Plot Object
     */
    public static void createPlotSettings(final int id, final Plot plot) {
        dbManager.createPlotSettings(id, plot);
    }

    /**
     * Get a plot id
     *
     * @param world World
     * @param id2   Plot ID
     *
     * @return ID
     */
    /*
     * public static int getId(String world, PlotId id2) { Statement stmt =
     * null; try { stmt = connection.createStatement(); ResultSet r =
     * stmt.executeQuery("SELECT `id` FROM `plot` WHERE `plot_id_x` = '" + id2.x
     * + "' AND `plot_id_z` = '" + id2.y + "' AND `world` = '" + world +
     * "' ORDER BY `timestamp` ASC"); int id = Integer.MAX_VALUE;
     * while(r.next()) { id = r.getInt("id"); } stmt.close(); return id; }
     * catch(SQLException e) { e.printStackTrace(); } return Integer.MAX_VALUE;
     * }
     */
    public static int getId(final String world, final PlotId id2) {
        return dbManager.getId(world, id2);
    }

    /**
     * @return Plots
     */
    public static LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots() {
        return dbManager.getPlots();
    }

    public static void setMerged(final String world, final Plot plot, final boolean[] merged) {
        dbManager.setMerged(world, plot, merged);
    }

    public static void setFlags(final String world, final Plot plot, final Set<Flag> flags) {
        dbManager.setFlags(world, plot, flags);
    }

    /**
     * @param plot
     * @param alias
     */
    public static void setAlias(final String world, final Plot plot, final String alias) {
        dbManager.setAlias(world, plot, alias);
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
    public static void setPosition(final String world, final Plot plot, final String position) {
        dbManager.setPosition(world, plot, position);
    }

    /**
     * @param id
     *
     * @return
     */
    public static HashMap<String, Object> getSettings(final int id) {
        return dbManager.getSettings(id);
    }

    /**
     * @param plot
     * @param comment
     */
    public static void removeComment(final String world, final Plot plot, final PlotComment comment) {
        dbManager.removeComment(world, plot, comment);
    }

    /**
     * @param plot
     * @param comment
     */
    public static void setComment(final String world, final Plot plot, final PlotComment comment) {
        dbManager.setComment(world, plot, comment);
    }

    /**
     * @param plot
     */
    public static ArrayList<PlotComment> getComments(final String world, final Plot plot, final int tier, final boolean below) {
        return dbManager.getComments(world, plot, tier, below);
    }

    /**
     * @param plot
     * @param player
     */
    public static void removeHelper(final String world, final Plot plot, final UUID uuid) {
        dbManager.removeHelper(world, plot, uuid);
    }

    /**
     * @param plot
     * @param player
     */
    public static void removeTrusted(final String world, final Plot plot, final UUID uuid) {
        dbManager.removeTrusted(world, plot, uuid);
    }

    /**
     * @param plot
     * @param player
     */
    public static void setHelper(final String world, final Plot plot, final UUID uuid) {
        dbManager.setHelper(world, plot, uuid);
    }

    /**
     * @param plot
     * @param player
     */
    public static void setTrusted(final String world, final Plot plot, final UUID uuid) {
        dbManager.setTrusted(world, plot, uuid);
    }

    /**
     * @param plot
     * @param player
     */
    public static void removeDenied(final String world, final Plot plot, final UUID uuid) {
        dbManager.removeDenied(world, plot, uuid);
    }

    /**
     * @param plot
     * @param player
     */
    public static void setDenied(final String world, final Plot plot, final UUID uuid) {
        dbManager.setDenied(world, plot, uuid);
    }

    public static double getRatings(final Plot plot) {
        return dbManager.getRatings(plot);
    }
}
