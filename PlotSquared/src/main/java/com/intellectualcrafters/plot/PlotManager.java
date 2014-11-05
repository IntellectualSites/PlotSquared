package com.intellectualcrafters.plot;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

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
