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

import com.intellectualcrafters.plot.Flag;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotComment;
import com.intellectualcrafters.plot.PlotId;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * @author Citymonstret
 */
public abstract class AbstractDB {

    // TODO MongoDB @Brandon

    /**
     * Set Plot owner
     *
     * @param plot
     * @param uuid
     */
    public abstract void setOwner(final Plot plot, final UUID uuid);

    public abstract void createAllSettingsAndHelpers(final ArrayList<Plot> plots);

    /**
     * Create a plot
     *
     * @param plots
     */
    public abstract void createPlots(final ArrayList<Plot> plots);

    /**
     * Create a plot
     *
     * @param plot
     */
    public abstract void createPlot(final Plot plot);

    /**
     * Create tables
     *
     * @throws SQLException
     */
    public abstract void createTables(final String database, final boolean add_constraint) throws Exception;

    /**
     * Delete a plot
     *
     * @param plot
     */
    public abstract void delete(final String world, final Plot plot);

    /**
     * Create plot settings
     *
     * @param id
     * @param plot
     */
    public abstract void createPlotSettings(final int id, final Plot plot);

    public abstract int getId(final String world, final PlotId id2);

    /**
     * @return
     */
    public abstract LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots();

    public abstract void setMerged(final String world, final Plot plot, final boolean[] merged);

    public abstract void setFlags(final String world, final Plot plot, final Flag[] flags);

    /**
     * @param plot
     * @param alias
     */
    public abstract void setAlias(final String world, final Plot plot, final String alias);

    public abstract void purge(final String world, final PlotId id);

    public abstract void purge(final String world);

    /**
     * @param plot
     * @param position
     */
    public abstract void setPosition(final String world, final Plot plot, final String position);

    /**
     * @param id
     * @return
     */
    public abstract HashMap<String, Object> getSettings(final int id);

    /**
     *
     */
    public UUID everyone = UUID.fromString("1-1-3-3-7");

    /**
     * @param plot
     * @param player
     */
    public abstract void removeHelper(final String world, final Plot plot, final OfflinePlayer player);

    /**
     * @param plot
     * @param player
     */
    public abstract void removeTrusted(final String world, final Plot plot, final OfflinePlayer player);

    /**
     * @param plot
     * @param player
     */
    public abstract void setHelper(final String world, final Plot plot, final OfflinePlayer player);

    /**
     * @param plot
     * @param player
     */
    public abstract void setTrusted(final String world, final Plot plot, final OfflinePlayer player);

    /**
     * @param plot
     * @param player
     */
    public abstract void removeDenied(final String world, final Plot plot, final OfflinePlayer player);

    /**
     * @param plot
     * @param player
     */
    public abstract void setDenied(final String world, final Plot plot, final OfflinePlayer player);

    public abstract double getRatings(final Plot plot);

    public abstract void removeComment(final String world, final Plot plot, final PlotComment comment);

    public abstract void setComment(final String world, final Plot plot, final PlotComment comment);

    public abstract ArrayList<PlotComment> getComments(final String world, final Plot plot, final int tier);
}
