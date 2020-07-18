/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot;

import com.plotsquared.core.command.Template;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.FileBytes;
import com.sk89q.worldedit.function.pattern.Pattern;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class PlotManager {

    private final PlotArea plotArea;

    public PlotManager(PlotArea plotArea) {
        this.plotArea = plotArea;
    }

    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots).
     */
    public abstract PlotId getPlotIdAbs(int x, int y, int z);

    public abstract PlotId getPlotId(int x, int y, int z);

    // If you have a circular plot, just return the corner if it were a square
    public abstract Location getPlotBottomLocAbs(PlotId plotId);

    // the same applies here
    public abstract Location getPlotTopLocAbs(PlotId plotId);

    /*
     * Plot clearing (return false if you do not support some method)
     */
    public boolean clearPlot(Plot plot, Runnable whenDone) {
        return clearPlot(plot, whenDone, null);
    }

    public boolean claimPlot(Plot plot) {
        return claimPlot(plot, null);

    }

    public boolean unClaimPlot(Plot plot, Runnable whenDone) {
        return unClaimPlot(plot, whenDone, null);

    }

    public abstract boolean clearPlot(Plot plot, Runnable whenDone,
        @Nullable QueueCoordinator queue);

    public abstract boolean claimPlot(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean unClaimPlot(Plot plot, Runnable whenDone,
        @Nullable QueueCoordinator queue);

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    public abstract Location getSignLoc(Plot plot);

    /*
     * Plot set functions (return false if you do not support the specific set
     * method).
     */
    public abstract String[] getPlotComponents(PlotId plotId);

    public boolean setComponent(PlotId plotId, String component, Pattern blocks) {
        return setComponent(plotId, component, blocks, null);
    }

    public abstract boolean setComponent(PlotId plotId, String component, Pattern blocks,
        @Nullable QueueCoordinator queue);

    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging).
     */
    public boolean createRoadEast(Plot plot) {
        return createRoadEast(plot, null);
    }

    public boolean createRoadSouth(Plot plot) {
        return createRoadSouth(plot, null);
    }

    public boolean createRoadSouthEast(Plot plot) {
        return createRoadSouthEast(plot, null);
    }

    public boolean removeRoadEast(Plot plot) {
        return removeRoadEast(plot, null);
    }

    public boolean removeRoadSouth(Plot plot) {
        return removeRoadSouth(plot, null);
    }

    public boolean removeRoadSouthEast(Plot plot) {
        return removeRoadSouthEast(plot, null);
    }

    public boolean startPlotMerge(List<PlotId> plotIds) {
        return startPlotMerge(plotIds, null);
    }

    public boolean startPlotUnlink(List<PlotId> plotIds) {
        return startPlotUnlink(plotIds, null);
    }

    public boolean finishPlotMerge(List<PlotId> plotIds) {
        return finishPlotMerge(plotIds, null);
    }

    public boolean finishPlotUnlink(List<PlotId> plotIds) {
        return finishPlotUnlink(plotIds, null);
    }

    public abstract boolean createRoadEast(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean createRoadSouth(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean createRoadSouthEast(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean removeRoadEast(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean removeRoadSouth(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean removeRoadSouthEast(Plot plot, @Nullable QueueCoordinator queue);

    public abstract boolean startPlotMerge(List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    public abstract boolean startPlotUnlink(List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    public abstract boolean finishPlotMerge(List<PlotId> plotIds, @Nullable QueueCoordinator queue);

    public abstract boolean finishPlotUnlink(List<PlotId> plotIds,
        @Nullable QueueCoordinator queue);

    public void exportTemplate() throws IOException {
        HashSet<FileBytes> files = new HashSet<>(Collections.singletonList(
            new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml",
                Template.getBytes(plotArea))));
        Template.zipAll(plotArea.getWorldName(), files);
    }

    public int getWorldHeight() {
        return 255;
    }

    /**
     * Sets all the blocks along all the plot walls to their correct state (claimed or unclaimed).
     *
     * @return true if the wall blocks were successfully set
     */
    public boolean regenerateAllPlotWalls() {
        return regenerateAllPlotWalls(null);
    }

    public boolean regenerateAllPlotWalls(@Nullable QueueCoordinator queue) {
        boolean success = true;
        for (Plot plot : plotArea.getPlots()) {
            if (plot.hasOwner()) {
                success &= claimPlot(plot, queue);
            } else {
                success &= unClaimPlot(plot, null, queue);
            }
        }
        return success;
    }

}
