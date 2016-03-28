package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetQueue;

import java.util.ArrayList;

/**
 * A plot manager with square plots which tessellate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot).
 */
public class ClassicPlotManager extends SquarePlotManager {

    @Override
    public boolean setComponent(PlotArea plotworld, PlotId plotid, String component, PlotBlock[] blocks) {
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
                setAll(plotworld, plotid, blocks);
                return true;
            }
            case "air": {
                setAir(plotworld, plotid, blocks);
                return true;
            }
            case "main": {
                setMain(plotworld, plotid, blocks);
                return true;
            }
            case "middle": {
                setMiddle(plotworld, plotid, blocks);
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
    public boolean unclaimPlot(PlotArea plotworld, Plot plot, Runnable whenDone) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        setWallFilling(dpw, plot.getId(), new PlotBlock[]{dpw.WALL_FILLING});
        if (dpw.WALL_BLOCK.id != 0 || !dpw.WALL_BLOCK.equals(dpw.CLAIMED_WALL_BLOCK)) {
            setWall(dpw, plot.getId(), new PlotBlock[]{dpw.WALL_BLOCK});
        }
        SetQueue.IMP.addTask(whenDone);
        return true;
    }

    public boolean setFloor(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        Plot plot = plotworld.getPlotAbs(plotid);
        if (plot.isBasePlot()) {
            ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
            for (RegionWrapper region : plot.getRegions()) {
                Location pos1 = new Location(plotworld.worldname, region.minX, dpw.PLOT_HEIGHT, region.minZ);
                Location pos2 = new Location(plotworld.worldname, region.maxX, dpw.PLOT_HEIGHT, region.maxZ);
                MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
            }
        }
        return true;
    }

    public boolean setAll(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        Plot plot = plotworld.getPlotAbs(plotid);
        if (!plot.isBasePlot()) {
            return false;
        }
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(plotworld.worldname, region.minX, 1, region.minZ);
            Location pos2 = new Location(plotworld.worldname, region.maxX, 255, region.maxZ);
            MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
        }
        return true;
    }

    public boolean setAir(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        Plot plot = plotworld.getPlotAbs(plotid);
        if (!plot.isBasePlot()) {
            return false;
        }
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(plotworld.worldname, region.minX, dpw.PLOT_HEIGHT + 1, region.minZ);
            Location pos2 = new Location(plotworld.worldname, region.maxX, 255, region.maxZ);
            MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
        }
        return true;
    }

    public boolean setMain(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        Plot plot = plotworld.getPlotAbs(plotid);
        if (!plot.isBasePlot()) {
            return false;
        }
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(plotworld.worldname, region.minX, 1, region.minZ);
            Location pos2 = new Location(plotworld.worldname, region.maxX, dpw.PLOT_HEIGHT - 1, region.maxZ);
            MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
        }
        return true;
    }

    public boolean setMiddle(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        Plot plot = plotworld.getPlotAbs(plotid);
        if (!plot.isBasePlot()) {
            return false;
        }
        Location[] corners = plot.getCorners();
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        SetQueue.IMP.setBlock(plotworld.worldname, (corners[0].getX() + corners[1].getX()) / 2, dpw.PLOT_HEIGHT,
                (corners[0].getZ() + corners[1].getZ()) / 2, blocks[0]);
        return true;
    }

    public boolean setOutline(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = plotworld.getPlotAbs(plotid);
        Location bottom = plot.getBottomAbs();
        Location top = plot.getExtendedTopAbs();
        PseudoRandom random = new PseudoRandom();
        if (!plot.getMerged(0)) {
            int z = bottom.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(3)) {
            int x = bottom.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }

        if (!plot.getMerged(2)) {
            int z = top.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(1)) {
            int x = top.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = dpw.PLOT_HEIGHT; y <= 255; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (plot.isBasePlot()) {
            for (RegionWrapper region : plot.getRegions()) {
                Location pos1 = new Location(plotworld.worldname, region.minX, 255, region.minZ);
                Location pos2 = new Location(plotworld.worldname, region.maxX, 255, region.maxZ);
                MainUtil.setCuboidAsync(plotworld.worldname, pos1, pos2, blocks);
            }
        }
        return true;
    }

    public boolean setWallFilling(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = plotworld.getPlotAbs(plotid);
        Location bot = plot.getExtendedBottomAbs().subtract(plot.getMerged(3) ? 0 : 1, 0, plot.getMerged(0) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        PseudoRandom random = new PseudoRandom();
        if (!plot.getMerged(0)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(3)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(2)) {
            int z = top.getZ();
            for (int x = bot.getX(); x < top.getX() + (plot.getMerged(1) ? 0 : 1); x++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        if (!plot.getMerged(1)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < top.getZ() + (plot.getMerged(2) ? 0 : 1); z++) {
                for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                    SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
                }
            }
        }
        return true;
    }

    public boolean setWall(PlotArea plotworld, PlotId plotid, PlotBlock[] blocks) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = plotworld.getPlotAbs(plotid);
        Location bot = plot.getExtendedBottomAbs().subtract(plot.getMerged(3) ? 0 : 1, 0, plot.getMerged(0) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        PseudoRandom random = new PseudoRandom();
        int y = dpw.WALL_HEIGHT + 1;
        if (!plot.getMerged(0)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        if (!plot.getMerged(3)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        if (!plot.getMerged(2)) {
            int z = top.getZ();
            for (int x = bot.getX(); x < top.getX() + (plot.getMerged(1) ? 0 : 1); x++) {
                SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        if (!plot.getMerged(1)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < top.getZ() + (plot.getMerged(2) ? 0 : 1); z++) {
                SetQueue.IMP.setBlock(plotworld.worldname, x, y, z, blocks[random.random(blocks.length)]);
            }
        }
        return true;
    }

    /**
     * PLOT MERGING.
     */
    @Override
    public boolean createRoadEast(PlotArea plotArea, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotArea;
        Location pos1 = getPlotBottomLocAbs(plotArea, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotArea, plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 2;
        int ez = pos2.getZ() + 2;
        MainUtil.setSimpleCuboidAsync(plotArea.worldname,
                new Location(plotArea.worldname, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1),
                new Location(plotArea.worldname, ex, 255, ez - 1), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotArea.worldname, new Location(plotArea.worldname, sx, 0, sz + 1),
                new Location(plotArea.worldname, ex, 0, ez - 1), new PlotBlock((short) 7,
                        (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotArea.worldname, new Location(plotArea.worldname, sx, 1, sz + 1),
                new Location(plotArea.worldname, sx, dpw.WALL_HEIGHT, ez - 1), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotArea.worldname, new Location(plotArea.worldname, sx, dpw.WALL_HEIGHT + 1, sz + 1),
                new Location(plotArea.worldname, sx, dpw.WALL_HEIGHT + 1, ez - 1),
                dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotArea.worldname, new Location(plotArea.worldname, ex, 1, sz + 1),
                new Location(plotArea.worldname, ex, dpw.WALL_HEIGHT, ez - 1), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotArea.worldname, new Location(plotArea.worldname, ex, dpw.WALL_HEIGHT + 1, sz + 1),
                new Location(plotArea.worldname, ex, dpw.WALL_HEIGHT + 1, ez - 1),
                dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotArea.worldname, new Location(plotArea.worldname, sx + 1, 1, sz + 1),
                new Location(plotArea.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean createRoadSouth(PlotArea plotworld, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        Location pos1 = getPlotBottomLocAbs(plotworld, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotworld, plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 2;
        int ex = pos2.getX() + 2;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname,
                new Location(plotworld.worldname, sx + 1, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz),
                new Location(plotworld.worldname, ex - 1, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz),
                new Location(plotworld.worldname, ex - 1, 0, ez), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz),
                new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT, sz), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, sz),
                new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT + 1, sz),
                dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, ez),
                new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT, ez), dpw.WALL_FILLING);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.WALL_HEIGHT + 1, ez),
                new Location(plotworld.worldname, ex - 1, dpw.WALL_HEIGHT + 1, ez),
                dpw.WALL_BLOCK);
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1),
                new Location(plotworld.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean createRoadSouthEast(PlotArea plotworld, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        Location pos2 = getPlotTopLocAbs(plotworld, plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = pos2.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.ROAD_HEIGHT + 1, sz + 1),
                new Location(plotworld.worldname, ex - 1, 255, ez - 1), new PlotBlock(
                        (short) 0, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 0, sz + 1),
                new Location(plotworld.worldname, ex - 1, 0, ez - 1), new PlotBlock((short) 7, (byte) 0));
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz + 1),
                new Location(plotworld.worldname, ex - 1, dpw.ROAD_HEIGHT, ez - 1), dpw.ROAD_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadEast(PlotArea plotworld, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        Location pos1 = getPlotBottomLocAbs(plotworld, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotworld, plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 1;
        int ez = pos2.getZ() + 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz),
                new Location(plotworld.worldname, ex, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz + 1),
                new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT - 1, ez - 1), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.PLOT_HEIGHT, sz + 1),
                new Location(plotworld.worldname, ex, dpw.PLOT_HEIGHT, ez - 1), dpw.TOP_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadSouth(PlotArea plotworld, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        Location pos1 = getPlotBottomLocAbs(plotworld, plot.getId());
        Location pos2 = getPlotTopLocAbs(plotworld, plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 1;
        int ex = pos2.getX() + 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz),
                new Location(plotworld.worldname, ex, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, 1, sz),
                new Location(plotworld.worldname, ex - 1, dpw.PLOT_HEIGHT - 1, ez), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx + 1, dpw.PLOT_HEIGHT, sz),
                new Location(plotworld.worldname, ex - 1, dpw.PLOT_HEIGHT, ez), dpw.TOP_BLOCK);
        return true;
    }

    @Override
    public boolean removeRoadSouthEast(PlotArea plotworld, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        Location loc = getPlotTopLocAbs(dpw, plot.getId());
        int sx = loc.getX() + 1;
        int ex = sx + dpw.ROAD_WIDTH - 1;
        int sz = loc.getZ() + 1;
        int ez = sz + dpw.ROAD_WIDTH - 1;
        MainUtil.setSimpleCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT + 1, sz),
                new Location(plotworld.worldname, ex, 255, ez), new PlotBlock((short) 0, (byte) 0));
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, 1, sz),
                new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT - 1, ez), dpw.MAIN_BLOCK);
        MainUtil.setCuboidAsync(plotworld.worldname, new Location(plotworld.worldname, sx, dpw.ROAD_HEIGHT, sz),
                new Location(plotworld.worldname, ex, dpw.ROAD_HEIGHT, ez), dpw.TOP_BLOCK);
        return true;
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED).
     */
    @Override
    public boolean finishPlotMerge(PlotArea plotworld, ArrayList<PlotId> plotIds) {
        PlotBlock block = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        if (block.id != 0 || !block.equals(unclaim)) {
            for (PlotId id : plotIds) {
                setWall(plotworld, id, new PlotBlock[]{block});
            }
        }
        return true;
    }

    @Override
    public boolean finishPlotUnlink(PlotArea plotworld, ArrayList<PlotId> plotIds) {
        PlotBlock block = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        for (PlotId id : plotIds) {
            if (block.id != 0 || !block.equals(unclaim)) {
                setWall(plotworld, id, new PlotBlock[]{block});
            }
        }
        return true;
    }

    @Override
    public boolean startPlotMerge(PlotArea plotworld, ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean startPlotUnlink(PlotArea plotworld, ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean claimPlot(PlotArea plotworld, Plot plot) {
        PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        PlotBlock claim = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        if (claim.id != 0 || !claim.equals(unclaim)) {
            setWall(plotworld, plot.getId(), new PlotBlock[]{claim});
        }
        return true;
    }

    @Override
    public String[] getPlotComponents(PlotArea plotworld, PlotId plotid) {
        return new String[]{"main", "floor", "air", "all", "border", "wall", "outline", "middle"};
    }

    /**
     * Remove sign for a plot.
     */
    @Override
    public Location getSignLoc(PlotArea plotworld, Plot plot) {
        ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        plot = plot.getBasePlot(false);
        Location bot = plot.getBottomAbs();
        return new com.intellectualcrafters.plot.object.Location(plotworld.worldname, bot.getX() - 1, dpw.ROAD_HEIGHT + 1, bot.getZ() - 2);
    }
}
