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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.intellectualcrafters.plot.commands.Template;

public abstract class PlotManager {
    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots)
     */
    public abstract PlotId getPlotIdAbs(final PlotArea plotworld, final int x, final int y, final int z);
    
    public abstract PlotId getPlotId(final PlotArea plotworld, final int x, final int y, final int z);
    
    // If you have a circular plot, just return the corner if it were a square
    public abstract Location getPlotBottomLocAbs(final PlotArea plotworld, final PlotId plotid);
    
    // the same applies here
    public abstract Location getPlotTopLocAbs(final PlotArea plotworld, final PlotId plotid);
    
    /*
     * Plot clearing (return false if you do not support some method)
     */
    public abstract boolean clearPlot(final PlotArea plotworld, final Plot plot, final Runnable whenDone);
    
    public abstract boolean claimPlot(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean unclaimPlot(final PlotArea plotworld, final Plot plot, final Runnable whenDone);
    
    public abstract Location getSignLoc(final PlotArea plotworld, final Plot plot);
    
    /*
     * Plot set functions (return false if you do not support the specific set
     * method)
     */
    public abstract String[] getPlotComponents(final PlotArea plotworld, final PlotId plotid);
    
    public abstract boolean setComponent(final PlotArea plotworld, final PlotId plotid, final String component, final PlotBlock[] blocks);
    
    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging)
     */
    public abstract boolean createRoadEast(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean createRoadSouth(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean createRoadSouthEast(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean removeRoadEast(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean removeRoadSouth(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean removeRoadSouthEast(final PlotArea plotworld, final Plot plot);
    
    public abstract boolean startPlotMerge(final PlotArea plotworld, final ArrayList<PlotId> plotIds);
    
    public abstract boolean startPlotUnlink(final PlotArea plotworld, final ArrayList<PlotId> plotIds);
    
    public abstract boolean finishPlotMerge(final PlotArea plotworld, final ArrayList<PlotId> plotIds);
    
    public abstract boolean finishPlotUnlink(final PlotArea plotworld, final ArrayList<PlotId> plotIds);
    
    public void exportTemplate(final PlotArea plotworld) throws IOException {
        final HashSet<FileBytes> files = new HashSet<>(
                Collections.singletonList(new FileBytes("templates/" + "tmp-data.yml", Template.getBytes(plotworld))));
        Template.zipAll(plotworld.worldname, files);
    }
    
}
