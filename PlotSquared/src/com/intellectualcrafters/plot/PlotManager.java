package com.intellectualcrafters.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class PlotManager {
    public final PlotWorld plotworld;
    
    public PlotManager(PlotWorld plotworld) {
        this.plotworld = plotworld;
    }
    
    /*
     *  Plot locations (methods with Abs in them will not need to consider mega plots)
     */
    
    public abstract PlotId getPlotIdAbs(World world, Location loc);
    
    public abstract boolean isInPlotAbs(Location loc, Plot plot);
    
    public abstract Location getPlotBottomLocAbs(Plot plot); // If you have a circular plot, just return the corner if it were a square
    
    public abstract Location getPlotTopLocAbs(Plot plot); // the same applies here
    
    /*
     *  Plot clearing (return false if you do not support some method)
     */
    
    public abstract boolean clearPlotAbs(Player player, Plot plot);
    
    public abstract boolean clearSignAbs(Player player, Plot plot);
    
    public abstract boolean clearEntitiesAbs(Player player, Plot plot);
    
        // The function below will apply to MEGA PLOTS, return false if you don't support clearing of mega plots
        // the function getPlotBottomLoc(plot) will return the bottom location of the entire mega plot
    public abstract boolean clearMegaPlot(Player player, Plot plot);
    
    /*
     * Plot set functions (return false if you do not support the specific set method)
     */
    
    public abstract boolean setWall(Player player, Plot plot, Block block);
    
    public abstract boolean setBiome(Player player, Plot plot, Biome biome);
    
    /*
     *  PLOT MERGING (return false if your generator does not support plot merging)
     */
    public abstract boolean createRoadEast(Plot plot);
    
    public abstract boolean createRoadSouth(Plot plot);
    
    public abstract boolean createRoadSouthEast(Plot plot);
    
    public abstract boolean removeRoadEast(Plot plot);
    
    public abstract boolean removeRoadSouth(Plot plot);
    
    public abstract boolean removeRoadSouthEast(Plot plot);
    
}
