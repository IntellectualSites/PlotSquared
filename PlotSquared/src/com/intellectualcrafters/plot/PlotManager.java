package com.intellectualcrafters.plot;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public abstract class PlotManager {

	/*
	 * Plot locations (methods with Abs in them will not need to consider mega
	 * plots)
	 */

	public abstract PlotId getPlotIdAbs(PlotWorld plotworld, Location loc);

	public abstract PlotId getPlotId(PlotWorld plotworld, Location loc);

	public abstract boolean isInPlotAbs(PlotWorld plotworld, Location loc, PlotId plotid);

	// If you have a circular plot, just return the corner if it were a square
	public abstract Location getPlotBottomLocAbs(PlotWorld plotworld, PlotId plotid);

	// the same applies here
	public abstract Location getPlotTopLocAbs(PlotWorld plotworld, PlotId plotid);

	/*
	 * Plot clearing (return false if you do not support some method)
	 */

	public abstract boolean clearPlot(Player player, Plot plot);

	public abstract Location getSignLoc(Player player, PlotWorld plotworld, Plot plot);

	/*
	 * Plot set functions (return false if you do not support the specific set
	 * method)
	 */

	public abstract boolean setWall(Player player, PlotWorld plotworld, PlotId plotid, PlotBlock block);

	public abstract boolean setFloor(Player player, PlotWorld plotworld, PlotId plotid, PlotBlock[] block);

	public abstract boolean setBiome(Player player, Plot plot, Biome biome);

	/*
	 * PLOT MERGING (return false if your generator does not support plot
	 * merging)
	 */
	public abstract boolean createRoadEast(PlotWorld plotworld, Plot plot);

	public abstract boolean createRoadSouth(PlotWorld plotworld, Plot plot);

	public abstract boolean createRoadSouthEast(PlotWorld plotworld, Plot plot);

	public abstract boolean removeRoadEast(PlotWorld plotworld, Plot plot);

	public abstract boolean removeRoadSouth(PlotWorld plotworld, Plot plot);

	public abstract boolean removeRoadSouthEast(PlotWorld plotworld, Plot plot);
	
    public abstract boolean startPlotMerge(World world, PlotWorld plotworld, ArrayList<PlotId> plotIds);
    
    public abstract boolean startPlotUnlink(World world, PlotWorld plotworld, ArrayList<PlotId> plotIds);

	public abstract boolean finishPlotMerge(World world, PlotWorld plotworld, ArrayList<PlotId> plotIds);
	
	public abstract boolean finishPlotUnlink(World world, PlotWorld plotworld, ArrayList<PlotId> plotIds);

}
