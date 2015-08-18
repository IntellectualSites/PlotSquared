package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
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
            case "border": {
                setWall(plotworld, plotid, blocks);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean unclaimPlot(PlotWorld plotworld, Plot plot, Runnable whenDone) {
        final HybridPlotWorld dpw = ((HybridPlotWorld) plotworld);
        setWallFilling(dpw, plot.id, new PlotBlock[] { dpw.WALL_FILLING });
        if (dpw.WALL_BLOCK.id != 0 || !dpw.WALL_BLOCK.equals(dpw.CLAIMED_WALL_BLOCK)) {
            setWall(dpw, plot.id, new PlotBlock[] { dpw.WALL_BLOCK });
        }
        SetBlockQueue.addNotify(whenDone);
        return true;
    }
    
    public boolean setFloor(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = MainUtil.getPlotBottomLoc(plotworld.worldname, plotid).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(plotworld.worldname, plotid).add(1, 0, 1);
        pos1.setY(dpw.PLOT_HEIGHT);
        pos2.setY(dpw.PLOT_HEIGHT + 1);
        MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
        return true;
    }

    public boolean setWallFilling(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        final Location bottom = MainUtil.getPlotBottomLoc(plotworld.worldname, plotid);
        final Location top = MainUtil.getPlotTopLoc(plotworld.worldname, plotid).add(1, 0, 1);
        int x, z;
        z = bottom.getZ();
        PseudoRandom random = new PseudoRandom();
        for (x = bottom.getX(); x <= (top.getX() - 1); x++) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        x = top.getX();
        for (z = bottom.getZ(); z <= (top.getZ() - 1); z++) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        z = top.getZ();
        for (x = top.getX(); x >= (bottom.getX() + 1); x--) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        x = bottom.getX();
        for (z = top.getZ(); z >= (bottom.getZ() + 1); z--) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        return true;
    }

    public boolean setWall(final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        final Location bottom = MainUtil.getPlotBottomLoc(plotworld.worldname, plotid);
        final Location top = MainUtil.getPlotTopLoc(plotworld.worldname, plotid).add(1, 0, 1);
        int x, z;
        z = bottom.getZ();
        PseudoRandom random = new PseudoRandom();
        final int y = dpw.WALL_HEIGHT + 1;
        for (x = bottom.getX(); x <= (top.getX() - 1); x++) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
        }
        x = top.getX();
        for (z = bottom.getZ(); z <= (top.getZ() - 1); z++) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
        }
        z = top.getZ();
        for (x = top.getX(); x >= (bottom.getX() + 1); x--) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
        }
        x = bottom.getX();
        for (z = top.getZ(); z >= (bottom.getZ() + 1); z--) {
            SetBlockQueue.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
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
        final int sz = pos1.getZ() - 1;
        final int ez = pos2.getZ() + 2;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(plotworld.worldname, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.PLOT_HEIGHT, ez), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.WALL_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, ex, 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean createRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sz = pos2.getZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getX() - 1;
        final int ex = pos2.getX() + 2;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz), new Location(plotworld.worldname, ex, 1, ez + 1), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, sz), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 2, sz + 1), dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, ez), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 1, ez + 1), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez), new Location(plotworld.worldname, ex, dpw.WALL_HEIGHT + 2, ez + 1), dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
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
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.ROAD_HEIGHT + 1, sz + 1), new Location(plotworld.worldname, ex, 257, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz + 1), new Location(plotworld.worldname, ex, 1, ez), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1), new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sx = pos2.getX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getZ();
        final int ez = pos2.getZ() + 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.PLOT_HEIGHT, ez), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.PLOT_HEIGHT, sz + 1), new Location(plotworld.worldname, ex + 1, dpw.PLOT_HEIGHT + 1, ez), dpw.TOP_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);
        final int sz = pos2.getZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getX();
        final int ex = pos2.getX() + 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(plotworld.worldname, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz), new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.PLOT_HEIGHT, sz), new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);
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
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT + 1, sz), new Location(plotworld.worldname, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz), new Location(plotworld.worldname, ex + 1, dpw.ROAD_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT, sz), new Location(plotworld.worldname, ex + 1, dpw.ROAD_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);
        return true;
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED)
     */
    @Override
    public boolean finishPlotMerge(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final PlotId pos1 = plotIds.get(0);
        final PlotBlock block = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        final PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        if (block.id != 0 || !block.equals(unclaim)) {
            setWall(plotworld, pos1, new PlotBlock[] { block });
        }
        return true;
    }

    @Override
    public boolean finishPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final PlotBlock block = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        final PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        for (final PlotId id : plotIds) {
            if (block.id != 0 || !block.equals(unclaim)) { 
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
        if (claim.id != 0 || !claim.equals(unclaim)) {
            setWall(plotworld, plot.id, new PlotBlock[] { claim });
        }
        return true;
    }

    @Override
    public String[] getPlotComponents(final PlotWorld plotworld, final PlotId plotid) {
        return new String[] { "floor", "wall", "border" };
    }

    /**
     * Remove sign for a plot
     */
    @Override
    public Location getSignLoc(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location bot = MainUtil.getPlotBottomLoc(plotworld.worldname, plot.id);
        return new com.intellectualcrafters.plot.object.Location(plotworld.worldname, bot.getX(), dpw.ROAD_HEIGHT + 1, bot.getZ() - 1);
    }
}
