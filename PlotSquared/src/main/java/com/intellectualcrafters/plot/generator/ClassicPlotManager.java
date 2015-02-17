package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.UUIDHandler;


/**
 * A plot manager with square plots which tesselate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot) 
 */
public abstract class ClassicPlotManager extends SquarePlotManager {
    
    @Override
    public boolean setComponent(World world, PlotWorld plotworld, PlotId plotid, String component, PlotBlock[] blocks) {
        switch(component) {
            case "floor": {
                setFloor(world, plotworld, plotid, blocks);
                return true;
            }
            case "wall": {
                setWallFilling(world, plotworld, plotid, blocks);
                return true;
            }
            case "border": {
                setWall(world, plotworld, plotid, blocks);
                return true;
            }
        }
        return false;
    }

    public boolean setFloor(final World world, final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plotid).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plotid);
        PlotHelper.setCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), blocks);
        return true;
    }

    public boolean setWallFilling(final World w, final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        final Location bottom = PlotHelper.getPlotBottomLoc(w, plotid);
        final Location top = PlotHelper.getPlotTopLoc(w, plotid);

        int x, z;
        z = bottom.getBlockZ();
        for (x = bottom.getBlockX(); x < (top.getBlockX() + 1); x++) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                PlotHelper.setBlock(w, x, y, z, blocks);
            }
        }

        x = top.getBlockX() + 1;
        for (z = bottom.getBlockZ(); z < (top.getBlockZ() + 1); z++) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                PlotHelper.setBlock(w, x, y, z, blocks);
            }
        }

        z = top.getBlockZ() + 1;
        for (x = top.getBlockX() + 1; x > (bottom.getBlockX() - 1); x--) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                PlotHelper.setBlock(w, x, y, z, blocks);
            }
        }
        x = bottom.getBlockX();
        for (z = top.getBlockZ() + 1; z > (bottom.getBlockZ() - 1); z--) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                PlotHelper.setBlock(w, x, y, z, blocks);
            }
        }
        return true;
    }

    public boolean setWall(final World w, final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        final Location bottom = PlotHelper.getPlotBottomLoc(w, plotid);
        final Location top = PlotHelper.getPlotTopLoc(w, plotid);

        int x, z;

        z = bottom.getBlockZ();
        for (x = bottom.getBlockX(); x < (top.getBlockX() + 1); x++) {
            PlotHelper.setBlock(w, x, dpw.WALL_HEIGHT + 1, z, blocks);
        }
        x = top.getBlockX() + 1;
        for (z = bottom.getBlockZ(); z < (top.getBlockZ() + 1); z++) {
            PlotHelper.setBlock(w, x, dpw.WALL_HEIGHT + 1, z, blocks);
        }
        z = top.getBlockZ() + 1;
        for (x = top.getBlockX() + 1; x > (bottom.getBlockX() - 1); x--) {
            PlotHelper.setBlock(w, x, dpw.WALL_HEIGHT + 1, z, blocks);
        }
        x = bottom.getBlockX();
        for (z = top.getBlockZ() + 1; z > (bottom.getBlockZ() - 1); z--) {
            PlotHelper.setBlock(w, x, dpw.WALL_HEIGHT + 1, z, blocks);
        }
        return true;
    }

    /**
     * Set a plot biome
     */
    @Override
    public boolean setBiome(final World world, final Plot plot, final Biome biome) {

        final int bottomX = PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX() - 1;
        final int topX = PlotHelper.getPlotTopLoc(world, plot.id).getBlockX() + 1;
        final int bottomZ = PlotHelper.getPlotBottomLoc(world, plot.id).getBlockZ() - 1;
        final int topZ = PlotHelper.getPlotTopLoc(world, plot.id).getBlockZ() + 1;

        final Block block = world.getBlockAt(PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 1, 1));
        final Biome current = block.getBiome();
        if (biome.equals(current)) {
            return false;
        }

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                final Block blk = world.getBlockAt(x, 0, z);
                final Biome c = blk.getBiome();
                if (c.equals(biome)) {
                    x += 15;
                    continue;
                }
                blk.setBiome(biome);
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
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sx = pos2.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getBlockZ() - 1;
        final int ez = pos2.getBlockZ() + 2;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(w, ex + 1, 257 + 1, ez), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz + 1), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez), new PlotBlock((short) 7, (byte) 0));

        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz + 1), new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.WALL_HEIGHT + 1, sz + 1), new Location(w, sx + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);

        PlotHelper.setCuboid(w, new Location(w, ex, 1, sz + 1), new Location(w, ex + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        PlotHelper.setCuboid(w, new Location(w, ex, dpw.WALL_HEIGHT + 1, sz + 1), new Location(w, ex + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);

        PlotHelper.setCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);

        return true;
    }

    @Override
    public boolean createRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sz = pos2.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getBlockX() - 1;
        final int ex = pos2.getBlockX() + 2;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(w, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(w, new Location(w, sx + 1, 0, sz), new Location(w, ex, 1, ez + 1), new PlotBlock((short) 7, (byte) 0));

        PlotHelper.setCuboid(w, new Location(w, sx + 1, 1, sz), new Location(w, ex, dpw.WALL_HEIGHT + 1, sz + 1), dpw.WALL_FILLING);
        PlotHelper.setCuboid(w, new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, sz), new Location(w, ex, dpw.WALL_HEIGHT + 2, sz + 1), dpw.WALL_BLOCK);

        PlotHelper.setCuboid(w, new Location(w, sx + 1, 1, ez), new Location(w, ex, dpw.WALL_HEIGHT + 1, ez + 1), dpw.WALL_FILLING);
        PlotHelper.setCuboid(w, new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, ez), new Location(w, ex, dpw.WALL_HEIGHT + 2, ez + 1), dpw.WALL_BLOCK);

        PlotHelper.setCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);

        return true;
    }

    @Override
    public boolean createRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sx = pos2.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos2.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, dpw.ROAD_HEIGHT + 1, sz + 1), new Location(w, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setCuboid(w, new Location(w, sx + 1, 0, sz + 1), new Location(w, ex, 1, ez), new PlotBlock((short) 7, (byte) 0));
        PlotHelper.setCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);

        return true;
    }

    @Override
    public boolean removeRoadEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sx = pos2.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getBlockZ();
        final int ez = pos2.getBlockZ() + 1;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(w, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.PLOT_HEIGHT, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);

        return true;
    }

    @Override
    public boolean removeRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sz = pos2.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getBlockX();
        final int ex = pos2.getBlockX() + 1;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(w, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.PLOT_HEIGHT, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);

        return true;
    }

    @Override
    public boolean removeRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final World world = Bukkit.getWorld(plot.world);

        final Location loc = getPlotTopLocAbs(dpw, plot.id);

        final int sx = loc.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = loc.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;

        PlotHelper.setSimpleCuboid(world, new Location(world, sx, dpw.ROAD_HEIGHT + 1, sz), new Location(world, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(world, new Location(world, sx + 1, 1, sz + 1), new Location(world, ex, dpw.ROAD_HEIGHT, ez), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(world, new Location(world, sx + 1, dpw.ROAD_HEIGHT, sz + 1), new Location(world, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.TOP_BLOCK);
        return true;
    }

    @Override
    public boolean clearPlot(final World world, final PlotWorld plotworld, final Plot plot, final boolean isDelete, Runnable whendone) {
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
        ChunkManager.regenerateRegion(pos1, pos2, whendone);
        return true;
    }
    
    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED)
     */
    @Override
    public boolean finishPlotMerge(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        final PlotId pos1 = plotIds.get(0);
        PlotBlock block = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        if (block.id != 0) {
            setWall(world, plotworld, pos1, new PlotBlock[] {(( ClassicPlotWorld) plotworld).WALL_BLOCK });
        }
        return true;
    }

    @Override
    public boolean finishPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        PlotBlock block = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        if (block.id != 0) {
            for (PlotId id : plotIds) {
                setWall(world, plotworld, id, new PlotBlock[] {(( ClassicPlotWorld) plotworld).WALL_BLOCK });
                Plot plot = PlotHelper.getPlot(world, id);
                if (plot.hasOwner()) {
                    String name = UUIDHandler.getName(plot.owner);
                    if (name != null) {
                        PlotHelper.setSign(world, name, plot);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startPlotMerge(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean startPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean claimPlot(World world, final PlotWorld plotworld, Plot plot) {
        PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        PlotBlock claim = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        if (claim != unclaim) {
            setWall(world, plotworld, plot.id, new PlotBlock[] {claim});
        }
        return true;
    }

    @Override
    public boolean unclaimPlot(World world, final PlotWorld plotworld, Plot plot) {
        PlotBlock unclaim = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        PlotBlock claim = ((ClassicPlotWorld) plotworld).CLAIMED_WALL_BLOCK;
        if (claim != unclaim) {
            setWall(world, plotworld, plot.id, new PlotBlock[] {unclaim});
        }
        return true;
    }

    @Override
    public String[] getPlotComponents(World world, PlotWorld plotworld, PlotId plotid) {
        return new String[] {
                "floor",
                "wall",
                "border"
        };
    }
    
    @Override
    public Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final ClassicPlotWorld dpw = ((ClassicPlotWorld) plotworld);

        final int px = plotid.x;
        final int pz = plotid.y;

        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plotworld.worldname), x, 256, z);
    }
    
    /**
     * Remove sign for a plot
     */
    @Override
    public Location getSignLoc(final World world, final PlotWorld plotworld, final Plot plot) {
        final ClassicPlotWorld dpw = (ClassicPlotWorld) plotworld;
        return new Location(world, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX(), dpw.ROAD_HEIGHT + 1, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockZ() - 1);
    }
    
    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, final Location loc) {
        final ClassicPlotWorld dpw = ((ClassicPlotWorld) plotworld);

        // get x,z loc
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        // get plot size
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;

        // get size of path on bottom part, and top part of plot
        // (As 0,0 is in the middle of a road, not the very start)
        int pathWidthLower;
        if ((dpw.ROAD_WIDTH % 2) == 0) {
            pathWidthLower = (int) (Math.floor(dpw.ROAD_WIDTH / 2) - 1);
        } else {
            pathWidthLower = (int) Math.floor(dpw.ROAD_WIDTH / 2);
        }

        // calulating how many shifts need to be done
        int dx = x / size;
        int dz = z / size;
        if (x < 0) {
            dx--;
            x += ((-dx) * size);
        }
        if (z < 0) {
            dz--;
            z += ((-dz) * size);
        }

        // reducing to first plot
        final int rx = (x) % size;
        final int rz = (z) % size;

        // checking if road (return null if so)
        final int end = pathWidthLower + dpw.PLOT_WIDTH;
        final boolean northSouth = (rz <= pathWidthLower) || (rz > end);
        final boolean eastWest = (rx <= pathWidthLower) || (rx > end);
        if (northSouth || eastWest) {
            return null;
        }
        // returning the plot id (based on the number of shifts required)
        return new PlotId(dx + 1, dz + 1);
    }
    
    @Override
    public PlotId getPlotId(final PlotWorld plotworld, final Location loc) {
        final ClassicPlotWorld dpw = ((ClassicPlotWorld) plotworld);

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        if (plotworld == null) {
            return null;
        }
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int pathWidthLower;
        if ((dpw.ROAD_WIDTH % 2) == 0) {
            pathWidthLower = (int) (Math.floor(dpw.ROAD_WIDTH / 2) - 1);
        } else {
            pathWidthLower = (int) Math.floor(dpw.ROAD_WIDTH / 2);
        }

        int dx = x / size;
        int dz = z / size;

        if (x < 0) {
            dx--;
            x += ((-dx) * size);
        }
        if (z < 0) {
            dz--;
            z += ((-dz) * size);
        }

        final int rx = (x) % size;
        final int rz = (z) % size;

        final int end = pathWidthLower + dpw.PLOT_WIDTH;

        final boolean northSouth = (rz <= pathWidthLower) || (rz > end);
        final boolean eastWest = (rx <= pathWidthLower) || (rx > end);
        if (northSouth && eastWest) {
            // This means you are in the intersection
            final PlotId id = PlayerFunctions.getPlotAbs(loc.add(dpw.ROAD_WIDTH, 0, dpw.ROAD_WIDTH));
            final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.settings.getMerged(0) && plot.settings.getMerged(3))) {
                return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var
            // poorly)
            final PlotId id = PlayerFunctions.getPlotAbs(loc.add(0, 0, dpw.ROAD_WIDTH));
            final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(0)) {
                return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final PlotId id = PlayerFunctions.getPlotAbs(loc.add(dpw.ROAD_WIDTH, 0, 0));
            final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(3)) {
                return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(dx + 1, dz + 1);
        final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
        if (plot == null) {
            return id;
        }
        return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
    }
    
    /**
     * Get the bottom plot loc (some basic math)
     */
    @Override
    public Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final ClassicPlotWorld dpw = ((ClassicPlotWorld) plotworld);

        final int px = plotid.x;
        final int pz = plotid.y;

        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plotworld.worldname), x, 1, z);
    }
}
