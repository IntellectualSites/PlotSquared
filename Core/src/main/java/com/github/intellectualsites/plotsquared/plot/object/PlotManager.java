package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.commands.Template;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.sk89q.worldedit.function.pattern.Pattern;

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
    public abstract boolean clearPlot(Plot plot, Runnable whenDone);

    public abstract boolean claimPlot(Plot plot);

    public abstract boolean unClaimPlot(Plot plot, Runnable whenDone);

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

    public abstract boolean setComponent(PlotId plotId, String component, Pattern blocks);

    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging).
     */
    public abstract boolean createRoadEast(Plot plot);

    public abstract boolean createRoadSouth(Plot plot);

    public abstract boolean createRoadSouthEast(Plot plot);

    public abstract boolean removeRoadEast(Plot plot);

    public abstract boolean removeRoadSouth(Plot plot);

    public abstract boolean removeRoadSouthEast(Plot plot);

    public abstract boolean startPlotMerge(List<PlotId> plotIds);

    public abstract boolean startPlotUnlink(List<PlotId> plotIds);

    public abstract boolean finishPlotMerge(List<PlotId> plotIds);

    public abstract boolean finishPlotUnlink(List<PlotId> plotIds);

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
        boolean success = true;
        for (Plot plot : plotArea.getPlots()) {
            if (plot.hasOwner()) {
                success &= claimPlot(plot);
            } else {
                success &= unClaimPlot(plot, null);
            }
        }
        return success;
    }

}
