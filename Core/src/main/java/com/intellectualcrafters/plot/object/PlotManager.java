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

import com.intellectualcrafters.plot.commands.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public abstract class PlotManager {

    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots).
     */
    public abstract PlotId getPlotIdAbs(PlotArea plotworld, int x, int y, int z);

    public abstract PlotId getPlotId(PlotArea plotworld, int x, int y, int z);

    // If you have a circular plot, just return the corner if it were a square
    public abstract Location getPlotBottomLocAbs(PlotArea plotworld, PlotId plotid);

    // the same applies here
    public abstract Location getPlotTopLocAbs(PlotArea plotworld, PlotId plotid);

    /*
     * Plot clearing (return false if you do not support some method)
     */
    public abstract boolean clearPlot(PlotArea plotworld, Plot plot, Runnable whenDone);

    public abstract boolean claimPlot(PlotArea plotworld, Plot plot);

    public abstract boolean unclaimPlot(PlotArea plotworld, Plot plot, Runnable whenDone);

    public abstract Location getSignLoc(PlotArea plotworld, Plot plot);

    /*
     * Plot set functions (return false if you do not support the specific set
     * method).
     */
    public abstract String[] getPlotComponents(PlotArea plotworld, PlotId plotid);

    public abstract boolean setComponent(PlotArea plotworld, PlotId plotid, String component, PlotBlock[] blocks);

    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging).
     */
    public abstract boolean createRoadEast(PlotArea plotworld, Plot plot);

    public abstract boolean createRoadSouth(PlotArea plotworld, Plot plot);

    public abstract boolean createRoadSouthEast(PlotArea plotworld, Plot plot);

    public abstract boolean removeRoadEast(PlotArea plotworld, Plot plot);

    public abstract boolean removeRoadSouth(PlotArea plotworld, Plot plot);

    public abstract boolean removeRoadSouthEast(PlotArea plotworld, Plot plot);

    public abstract boolean startPlotMerge(PlotArea plotworld, ArrayList<PlotId> plotIds);

    public abstract boolean startPlotUnlink(PlotArea plotworld, ArrayList<PlotId> plotIds);

    public abstract boolean finishPlotMerge(PlotArea plotworld, ArrayList<PlotId> plotIds);

    public abstract boolean finishPlotUnlink(PlotArea plotworld, ArrayList<PlotId> plotIds);

    public void exportTemplate(PlotArea plotworld) throws IOException {
        HashSet<FileBytes> files = new HashSet<>(
                Collections.singletonList(new FileBytes("templates/" + "tmp-data.yml", Template.getBytes(plotworld))));
        Template.zipAll(plotworld.worldname, files);
    }

}
