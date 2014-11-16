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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;

@SuppressWarnings("unused")
public abstract class PlotManager {

    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots)
     */

    public abstract PlotId getPlotIdAbs(final PlotWorld plotworld, final Location loc);

    public abstract PlotId getPlotId(final PlotWorld plotworld, final Location loc);

    public abstract boolean isInPlotAbs(final PlotWorld plotworld, final Location loc, final PlotId plotid);

    // If you have a circular plot, just return the corner if it were a square
    public abstract Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid);

    // the same applies here
    public abstract Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid);

    /*
     * Plot clearing (return false if you do not support some method)
     */

    public abstract boolean clearPlot(final World world, final Plot plot);

    public abstract Location getSignLoc(final World world, final PlotWorld plotworld, final Plot plot);

    /*
     * Plot set functions (return false if you do not support the specific set
     * method)
     */

    public abstract boolean setWallFilling(final World world, final PlotWorld plotworld, final PlotId plotid, final PlotBlock block);

    public abstract boolean setWall(final World world, final PlotWorld plotworld, final PlotId plotid, final PlotBlock block);

    public abstract boolean setFloor(final World world, final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] block);

    public abstract boolean setBiome(final World world, final Plot plot, final Biome biome);

    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging)
     */
    public abstract boolean createRoadEast(final PlotWorld plotworld, final Plot plot);

    public abstract boolean createRoadSouth(final PlotWorld plotworld, final Plot plot);

    public abstract boolean createRoadSouthEast(final PlotWorld plotworld, final Plot plot);

    public abstract boolean removeRoadEast(final PlotWorld plotworld, final Plot plot);

    public abstract boolean removeRoadSouth(final PlotWorld plotworld, final Plot plot);

    public abstract boolean removeRoadSouthEast(final PlotWorld plotworld, final Plot plot);

    public abstract boolean startPlotMerge(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds);

    public abstract boolean startPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds);

    public abstract boolean finishPlotMerge(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds);

    public abstract boolean finishPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds);

}
