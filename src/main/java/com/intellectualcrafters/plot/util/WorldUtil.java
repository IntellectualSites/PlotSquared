package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;

public abstract class WorldUtil {
    public static WorldUtil IMP;
    
    public abstract int getBiomeFromString(String value);
    
    public abstract String[] getBiomeList();
    
    public abstract String getMainWorld();

    public abstract boolean isWorld(String worldname);
    
    public abstract String[] getSign(Location loc);
    
    public abstract Location getSpawn(String world);
    
    public abstract String getClosestMatchingName(PlotBlock plotBlock);
    
    public abstract boolean isBlockSolid(PlotBlock block);
    
    public abstract StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name);
    
    public abstract String getBiome(String world, int x, int z);
    
    public abstract PlotBlock getBlock(Location location);
    
    public abstract int getHighestBlock(String world, int x, int z);
    
    public abstract boolean addItems(String world, PlotItem item);
    
    public abstract void setSign(String world, int x, int y, int z, String[] lines);
    
    public abstract void setBiomes(String world, RegionWrapper region, String biome);
}
