package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;

/**
 * A plot manager with square plots which tesselate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot)
 */
public class ClassicPlotManager extends SquarePlotManager {
    @Override
    public boolean setComponent(final PlotWorld plotworld, final PlotId plotid, final String component, final PlotBlock[] blocks) {
        switch (component) {
            case "floor": {
                setFloor(plotworld, plotid, blocks);
                return true;
            }
            case "wall": {
                setWallFilling(plotworld, plotid, blocks);
                return true;
            }
            case "all": {
                setAir(plotworld, plotid, blocks);
                return true;
            }
            case "outline": {
                setOutline(plotworld, plotid, blocks);
                return true;
            }
            case "border": {
                setWall(plotworld, plotid, blocks);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean unclaimPlot(final PlotWorld plotworld, final Plot plot, final Runnable whenDone) {
        final ClassicPlotWorld dpw = ((ClassicPlotWorld) plotworld);
        setWallFilling(dpw, plot.id, new PlotBlock[] { dpw.WALL_FILLING });
        if ((dpw.WALL_BLOCK.id != 0) || !dpw.WALL_BLOCK.equals(dpw.CLAIMED_WALL_BLOCK)) {
            setWall(dpw, plot.id, new PlotBlock[] { dpw.WALL_BLOCK });
        }
        SetBlockQueue.addNotify(whenDone);
        return true;
    }
    
    public boolean setFloor(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        Plot plot = MainUtil.getPlotAbs(plotworld.worldname, plotid);
        if (plot.isBasePlot()) {
            final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
            for (RegionWrapper region : MainUtil.getRegions(plot)) {
                Location pos1 = new Location(plot.world, region.minX, dpw.PLOT_HEIGHT, region.minZ);
                Location pos2 = new Location(plot.world, region.maxX, dpw.PLOT_HEIGHT, region.maxZ);
                MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
            }
        }
        return true;
    }
    
    public boolean setAir(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        Plot plot = MainUtil.getPlotAbs(plotworld.worldname, plotid);
        if (!plot.isBasePlot()) {
            return false;
        }
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        for (RegionWrapper region : MainUtil.getRegions(plot)) {
            Location pos1 = new Location(plot.world, region.minX, dpw.PLOT_HEIGHT + 1, region.minZ);
            Location pos2 = new Location(plot.world, region.maxX, 255, region.maxZ);
            MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
        }
        return true;
    }
    
    public boolean setOutline(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = MainUtil.getPlotAbs(plotworld.worldname, plotid);
        final Location bottom = MainUtil.getPlotBottomLocAbs(plotworld.worldname, plotid);
        final Location top = MainUtil.getPlotTopLoc_(plot);
        final PseudoRandom random = new PseudoRandom();
        if (!plot.getMerged(0)) {
            int z = bottom.getZ();
            for (int x = bottom.getX(); x <= (top.getX()); x++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(3)) {
            int x = bottom.getX();
            for (int z = bottom.getZ(); z <= (top.getZ()); z++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        
        if (!plot.getMerged(2)) {
            int z = top.getZ();
            for (int x = bottom.getX(); x <= (top.getX()); x++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(1)) {
            int x = top.getX();
            for (int z = bottom.getZ(); z <= (top.getZ()); z++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (plot.isBasePlot()) {
            for (RegionWrapper region : MainUtil.getRegions(plot)) {
                Location pos1 = new Location(plot.world, region.minX, 255, region.minZ);
                Location pos2 = new Location(plot.world, region.maxX, 255, region.maxZ);
                MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
            }
        }
        return true;
    }
    
    public boolean setWallFilling(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = MainUtil.getPlotAbs(plotworld.worldname, plotid);
        final Location bot = MainUtil.getPlotBottomLoc_(plot).subtract(plot.getMerged(3) ? 0 : 1, 0, plot.getMerged(0) ? 0 : 1);
        final Location top = MainUtil.getPlotTopLoc_(plot).add(1, 0, 1);
        final PseudoRandom random = new PseudoRandom();
        if (!plot.getMerged(0)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < (top.getX()); x++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(3)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < (top.getZ()); z++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(2)) {
            int z = top.getZ();
            for (int x = bot.getX(); x < (top.getX() + (plot.getMerged(1) ? 0 : 1)); x++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(1)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < (top.getZ() + (plot.getMerged(2) ? 0 : 1)); z++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        return true;
    }
    
    public boolean setWall(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = MainUtil.getPlotAbs(plotworld.worldname, plotid);
        final Location bot = MainUtil.getPlotBottomLoc_(plot).subtract(plot.getMerged(3) ? 0 : 1, 0, plot.getMerged(0) ? 0 : 1);
        final Location top = MainUtil.getPlotTopLoc_(plot).add(1, 0, 1);
        final PseudoRandom random = new PseudoRandom();
        final int y = dpw.WALL_HEIGHT + 1;
        if (!plot.getMerged(0)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < (top.getX()); x++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        if (!plot.getMerged(3)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < (top.getZ()); z++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        if (!plot.getMerged(2)) {
            int z = top.getZ();
            for (int x = bot.getX(); x < (top.getX() + (plot.getMerged(1) ? 0 : 1)); x++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        if (!plot.getMerged(1)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < (top.getZ() + (plot.getMerged(2) ? 0 : 1)); z++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        return true;
    }
    
    /**
     * PLOT MERGING
     */
    @Override
    public boolean createRoadEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sx = pos2.getX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getZ() - 2;
        final int ez = pos2.getZ() + 2;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(plotworld.worldname, ex, 255, ez - 1), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 0, sz + 1), new Location(plotworld.worldname, ex, 0, ez - 1), new PlotBlock((short) 7,
        (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, sx, dpw.WALL_HEIGHT, ez - 1), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.WALL_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, sx, dpw.WALL_HEIGHT + 1, ez - 1),
        dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, ex, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT, ez - 1), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, ez - 1),
        dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        return true;
    }
    
    @Override
    public boolean createRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sz = pos2.getZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getX() - 2;
        final int ex = pos2.getX() + 2;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex - 1, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz), new Location(plotworld.worldname, ex - 1, 0, ez), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz), new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT, sz), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, sz), new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT + 1, sz),
        dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, ez), new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT, ez), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez), new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT + 1, ez),
        dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        return true;
    }
    
    @Override
    public boolean createRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sx = pos2.getX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos2.getZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.ROAD_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, ex - 1, 255, ez - 1), new PlotBlock(
        (short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz + 1), new Location(plotworld.worldname, ex - 1, 0, ez - 1), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        return true;
    }
    
    @Override
    public boolean removeRoadEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sx = pos2.getX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getZ() - 1;
        final int ez = pos2.getZ() + 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT - 1, ez - 1), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.PLOT_HEIGHT, sz + 1), new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT, ez - 1), dpw.TOP_BLOCK);
        return true;
    }
    
    @Override
    public boolean removeRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sz = pos2.getZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getX() - 1;
        final int ex = pos2.getX() + 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz), new Location(plotworld.worldname, ex - 1, dpw.PLOT_HEIGHT - 1, ez), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.PLOT_HEIGHT, sz), new Location(plotworld.worldname, ex - 1, dpw.PLOT_HEIGHT, ez), dpw.TOP_BLOCK);
        return true;
    }
    
    @Override
    public boolean removeRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location loc = getPlotTopLocAbs(dpw, plot.id);
        final int sx = loc.getX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = loc.getZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT + 1, sz), new Location(plotworld.worldname, ex, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT - 1, ez), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT, sz), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT, ez), dpw.TOP_BLOCK);
        return true;
    }
    
    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED)
     */
    @Override
    public boolean finishPlotMerge(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final PlotBlock block = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        final PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        if ((block.id != 0) || !block.equals(unclaim)) {
            for (final PlotId id : plotIds) {
                setWall(plotworld, id, new PlotBlock[] { block });
            }
        }
        return true;
    }
    
    @Override
    public boolean finishPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final PlotBlock block = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        final PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        for (final PlotId id : plotIds) {
            if ((block.id != 0) || !block.equals(unclaim)) {
                setWall(plotworld, id, new PlotBlock[] { block });
            }
        }
        return true;
    }
    
    @Override
    public boolean startPlotMerge(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }
    
    @Override
    public boolean startPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }
    
    @Override
    public boolean claimPlot(final PlotWorld plotworld, final Plot plot) {
        final PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        final PlotBlock claim = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        if ((claim.id != 0) || !claim.equals(unclaim)) {
            setWall(plotworld, plot.id, new PlotBlock[] { claim });
        }
        return true;
    }
    
    @Override
    public String[] getPlotComponents(final PlotWorld plotworld, final PlotId plotid) {
        return new String[] { "floor", "wall", "border", "all", "outline" };
    }
    
    /**
     * Remove sign for a plot
     */
    @Override
    public Location getSignLoc(final PlotWorld plotworld, Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        plot = plot.getBasePlot(false);
        final Location bot = plot.getBottomAbs();
        return new com.intellectualcrafters.plot.object.Location(plotworld.worldname, bot.getX() - 1, dpw.ROAD_HEIGHT + 1, bot.getZ() - 2);
    }
}
