package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Direction;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;

import java.util.List;

/**
 * A plot manager with square plots which tessellate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot).
 */
public class ClassicPlotManager extends SquarePlotManager {

    private final ClassicPlotWorld classicPlotWorld;

    public ClassicPlotManager(ClassicPlotWorld classicPlotWorld) {
        super(classicPlotWorld);
        this.classicPlotWorld = classicPlotWorld;
    }

    @Override public boolean setComponent(PlotId plotId, String component,
                                          BlockBucket blocks) {
        switch (component) {
            case "floor":
                return setFloor(plotId, blocks);
            case "wall":
                return setWallFilling(plotId, blocks);
            case "all":
                return setAll(plotId, blocks);
            case "air":
                return setAir(plotId, blocks);
            case "main":
                return setMain(plotId, blocks);
            case "middle":
                return setMiddle(plotId, blocks);
            case "outline":
                return setOutline(plotId, blocks);
            case "border":
                return setWall(plotId, blocks);
        }
        return false;
    }

    @Override public boolean unClaimPlot(Plot plot, Runnable whenDone) {
        setWallFilling(plot.getId(), classicPlotWorld.WALL_FILLING);
        setWall(plot.getId(), classicPlotWorld.WALL_BLOCK);
        return GlobalBlockQueue.IMP.addEmptyTask(whenDone);
    }

    public boolean setFloor(PlotId plotId, BlockBucket blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        if (plot.isBasePlot()) {
            for (RegionWrapper region : plot.getRegions()) {
                Location pos1 =
                    new Location(classicPlotWorld.worldname, region.minX, classicPlotWorld.PLOT_HEIGHT, region.minZ);
                Location pos2 =
                    new Location(classicPlotWorld.worldname, region.maxX, classicPlotWorld.PLOT_HEIGHT, region.maxZ);
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        return queue.enqueue();
    }

    public boolean setAll(PlotId plotId, BlockBucket blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int maxY = getWorldHeight();
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(classicPlotWorld.worldname, region.minX, 1, region.minZ);
            Location pos2 = new Location(classicPlotWorld.worldname, region.maxX, maxY, region.maxZ);
            queue.setCuboid(pos1, pos2, blocks);
        }
        return queue.enqueue();
    }

    public boolean setAir(PlotId plotId, BlockBucket blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int maxY = getWorldHeight();
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 =
                new Location(classicPlotWorld.worldname, region.minX, classicPlotWorld.PLOT_HEIGHT + 1, region.minZ);
            Location pos2 = new Location(classicPlotWorld.worldname, region.maxX, maxY, region.maxZ);
            queue.setCuboid(pos1, pos2, blocks);
        }
        return queue.enqueue();
    }

    public boolean setMain(PlotId plotId, BlockBucket blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        for (RegionWrapper region : plot.getRegions()) {
            Location pos1 = new Location(classicPlotWorld.worldname, region.minX, 1, region.minZ);
            Location pos2 =
                new Location(classicPlotWorld.worldname, region.maxX, classicPlotWorld.PLOT_HEIGHT - 1, region.maxZ);
            queue.setCuboid(pos1, pos2, blocks);
        }
        return queue.enqueue();
    }

    public boolean setMiddle(PlotId plotId, BlockBucket blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        Location[] corners = plot.getCorners();
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);

        int x = MathMan.average(corners[0].getX(), corners[1].getX());
        int z = MathMan.average(corners[0].getZ(), corners[1].getZ());
        queue.setBlock(x, classicPlotWorld.PLOT_HEIGHT, z, blocks.getBlock());
        return queue.enqueue();
    }

    public boolean setOutline(PlotId plotId, BlockBucket blocks) {
        if (classicPlotWorld.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        Location bottom = plot.getBottomAbs();
        Location top = plot.getExtendedTopAbs();
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int maxY = classicPlotWorld.getPlotManager().getWorldHeight();
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bottom.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bottom.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }

        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (plot.isBasePlot()) {
            for (RegionWrapper region : plot.getRegions()) {
                Location pos1 = new Location(classicPlotWorld.worldname, region.minX, maxY, region.minZ);
                Location pos2 = new Location(classicPlotWorld.worldname, region.maxX, maxY, region.maxZ);
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        return queue.enqueue();
    }

    public boolean setWallFilling(PlotId plotId, BlockBucket blocks) {
        if (classicPlotWorld.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        Location bot = plot.getExtendedBottomAbs()
            .subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0,
                plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX();
                 x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ();
                 z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
        return queue.enqueue();
    }

    public boolean setWall(PlotId plotId, BlockBucket blocks) {
        if (classicPlotWorld.ROAD_WIDTH == 0) {
            return false;
        }
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        Location bot = plot.getExtendedBottomAbs()
            .subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0,
                plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int y = classicPlotWorld.WALL_HEIGHT + 1;
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX();
                 x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ();
                 z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                queue.setBlock(x, y, z, blocks.getBlock());
            }
        }
        return queue.enqueue();
    }

    /**
     * PLOT MERGING.
     */
    @Override public boolean createRoadEast(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 2;
        int ez = pos2.getZ() + 2;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int maxY = getWorldHeight();
        queue.setCuboid(
            new Location(classicPlotWorld.worldname, sx, Math.min(classicPlotWorld.WALL_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1,
                sz + 1), new Location(classicPlotWorld.worldname, ex, maxY, ez - 1),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, 0, sz + 1),
            new Location(classicPlotWorld.worldname, ex, 0, ez - 1), PlotBlock.get((short) 7, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, 1, sz + 1),
            new Location(classicPlotWorld.worldname, sx, classicPlotWorld.WALL_HEIGHT, ez - 1), classicPlotWorld.WALL_FILLING);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, classicPlotWorld.WALL_HEIGHT + 1, sz + 1),
            new Location(classicPlotWorld.worldname, sx, classicPlotWorld.WALL_HEIGHT + 1, ez - 1), classicPlotWorld.WALL_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, ex, 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.WALL_HEIGHT, ez - 1), classicPlotWorld.WALL_FILLING);
        queue.setCuboid(new Location(classicPlotWorld.worldname, ex, classicPlotWorld.WALL_HEIGHT + 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.WALL_HEIGHT + 1, ez - 1), classicPlotWorld.WALL_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1), classicPlotWorld.ROAD_BLOCK);
        return queue.enqueue();
    }

    @Override public boolean createRoadSouth(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 2;
        int ex = pos2.getX() + 2;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(
            new Location(classicPlotWorld.worldname, sx + 1, Math.min(classicPlotWorld.WALL_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1,
                sz),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.getPlotManager().getWorldHeight(),
                ez), PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 0, sz),
            new Location(classicPlotWorld.worldname, ex - 1, 0, ez), PlotBlock.get((short) 7, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 1, sz),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.WALL_HEIGHT, sz), classicPlotWorld.WALL_FILLING);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, classicPlotWorld.WALL_HEIGHT + 1, sz),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.WALL_HEIGHT + 1, sz), classicPlotWorld.WALL_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 1, ez),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.WALL_HEIGHT, ez), classicPlotWorld.WALL_FILLING);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, classicPlotWorld.WALL_HEIGHT + 1, ez),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.WALL_HEIGHT + 1, ez), classicPlotWorld.WALL_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1), classicPlotWorld.ROAD_BLOCK);
        return queue.enqueue();
    }

    @Override public boolean createRoadSouthEast(Plot plot) {
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, classicPlotWorld.ROAD_HEIGHT + 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.getPlotManager().getWorldHeight(), ez - 1),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 0, sz + 1),
            new Location(classicPlotWorld.worldname, ex - 1, 0, ez - 1),
            PlotBlock.get((short) 7, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1), classicPlotWorld.ROAD_BLOCK);
        return queue.enqueue();
    }

    @Override public boolean removeRoadEast(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 1;
        int ez = pos2.getZ() + 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(
            new Location(classicPlotWorld.worldname, sx, Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1,
                sz),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, 1, sz + 1),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.PLOT_HEIGHT - 1, ez - 1), classicPlotWorld.MAIN_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, classicPlotWorld.PLOT_HEIGHT, sz + 1),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.PLOT_HEIGHT, ez - 1), classicPlotWorld.TOP_BLOCK);
        return queue.enqueue();
    }

    @Override public boolean removeRoadSouth(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 1;
        int ex = pos2.getX() + 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(
            new Location(classicPlotWorld.worldname, sx, Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1,
                sz),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, 1, sz),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.PLOT_HEIGHT - 1, ez), classicPlotWorld.MAIN_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx + 1, classicPlotWorld.PLOT_HEIGHT, sz),
            new Location(classicPlotWorld.worldname, ex - 1, classicPlotWorld.PLOT_HEIGHT, ez), classicPlotWorld.TOP_BLOCK);
        return queue.enqueue();
    }

    @Override public boolean removeRoadSouthEast(Plot plot) {
        Location location = getPlotTopLocAbs(plot.getId());
        int sx = location.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = location.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, classicPlotWorld.ROAD_HEIGHT + 1, sz),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            PlotBlock.get((short) 0, (byte) 0));
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, 1, sz),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.ROAD_HEIGHT - 1, ez), classicPlotWorld.MAIN_BLOCK);
        queue.setCuboid(new Location(classicPlotWorld.worldname, sx, classicPlotWorld.ROAD_HEIGHT, sz),
            new Location(classicPlotWorld.worldname, ex, classicPlotWorld.ROAD_HEIGHT, ez), classicPlotWorld.TOP_BLOCK);
        return queue.enqueue();
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED).
     *
     * @return false if part of the merge failed, otherwise true if successful.
     */
    @Override public boolean finishPlotMerge(List<PlotId> plotIds) {
        final BlockBucket block = classicPlotWorld.CLAIMED_WALL_BLOCK;
        boolean success = true;
        for (PlotId plotId : plotIds) {
            success &= setWall(plotId, block);
        }
        if (Settings.General.MERGE_REPLACE_WALL) {
            final BlockBucket wallBlock = classicPlotWorld.WALL_FILLING;
            for (PlotId id : plotIds) {
                success &= setWallFilling(id, wallBlock);
            }
        }
        return success;
    }

    @Override public boolean finishPlotUnlink(List<PlotId> plotIds) {
        final BlockBucket block = classicPlotWorld.CLAIMED_WALL_BLOCK;
        boolean success = true;
        for (PlotId id : plotIds) {
            success &= setWall(id, block);
        }
        return success;
    }

    /**
     * Sets all the blocks along all the plot walls to their correct state (claimed or unclaimed).
     *
     * @return true if the wall blocks were successfully set
     */
    @Override public boolean regenerateAllPlotWalls() {
        boolean success = true;
        for (Plot plot : classicPlotWorld.getPlots()) {
            if (plot.hasOwner()) {
                success &= setWall(plot.getId(), classicPlotWorld.CLAIMED_WALL_BLOCK);
            } else {
                success &= setWall(plot.getId(), classicPlotWorld.WALL_BLOCK);
            }
        }
        return success;
    }

    @Override public boolean startPlotMerge(List<PlotId> plotIds) {
        return true;
    }

    @Override public boolean startPlotUnlink(List<PlotId> plotIds) {
        return true;
    }

    @Override public boolean claimPlot(Plot plot) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        return setWall(plot.getId(), claim);
    }

    @Override public String[] getPlotComponents(PlotId plotId) {
        return new String[] {"main", "floor", "air", "all", "border", "wall", "outline", "middle"};
    }

    /**
     * Retrieves the location of where a sign should be for a plot.
     * @param plot The plot
     * @return The location where a sign should be
     */
    @Override public Location getSignLoc(Plot plot) {
        plot = plot.getBasePlot(false);
        Location bot = plot.getBottomAbs();
        return new Location(classicPlotWorld.worldname, bot.getX() - 1, classicPlotWorld.ROAD_HEIGHT + 1,
            bot.getZ() - 2);
    }
}
