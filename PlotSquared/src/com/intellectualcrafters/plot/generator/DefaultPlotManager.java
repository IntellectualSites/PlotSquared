package com.intellectualcrafters.plot.generator;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.Configuration;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotManager;
import com.intellectualcrafters.plot.PlotWorld;

public class DefaultPlotManager extends PlotManager {

    @Override
    public PlotId getPlotIdAbs(PlotWorld plotworld, Location loc) {
        DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
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

        int rx = (x) % size;
        int rz = (z) % size;

        int end = pathWidthLower + dpw.PLOT_WIDTH;
        boolean northSouth = (rz <= pathWidthLower) || (rz > (pathWidthLower + dpw.PLOT_WIDTH));
        boolean eastWest = (rx <= pathWidthLower) || (rx > end);

        if (northSouth || eastWest) {
            return null;
        }
        return new PlotId(dx + 1, dz + 1);
    }

    @Override
    public boolean isInPlotAbs(PlotWorld plotworld, Location loc, Plot plot) {
        PlotId result = getPlotIdAbs(plotworld, loc);
        if (result==null) {
            return false;
        }
        return result==plot.id;
    }

    @Override
    public Location getPlotBottomLocAbs(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);
        
        int px = plot.id.x;
        int pz = plot.id.y;

        int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plot.world), x, 1, z);
    }

    @Override
    public Location getPlotTopLocAbs(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);
        
        int px = plot.id.x;
        int pz = plot.id.y;

        int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plot.world), x, 256, z);
    }

    @Override
    public boolean clearPlot(Player player, Plot plot, boolean mega) {
        World world = player.getWorld();
        DefaultPlotWorld plotworld = (DefaultPlotWorld) PlotMain.getWorldSettings(world);
        
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);

        // TODO stuff
        
        return true;
    }

    @Override
    public boolean clearSign(Player player, Plot plot, boolean mega) {
        World world = player.getWorld();
        DefaultPlotWorld plotworld = (DefaultPlotWorld) PlotMain.getWorldSettings(world);
        Location pl = new Location(world, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX(), plotworld.ROAD_HEIGHT + 1, getPlotBottomLoc(world, plot.id).getBlockZ());
        Block bs = pl.add(0, 0, -1).getBlock();
        bs.setType(Material.AIR);
        return true;
    }

    @Override
    public boolean clearEntities(Player player, Plot plot, boolean mega) {
        World world = Bukkit.getWorld(plot.world);
        Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
        for (int i = (pos1.getBlockX() / 16) * 16; i < (16 + ((pos2.getBlockX() / 16) * 16)); i += 16) {
            for (int j = (pos1.getBlockZ() / 16) * 16; j < (16 + ((pos2.getBlockZ() / 16) * 16)); j += 16) {
                Chunk chunk = world.getChunkAt(i, j);
                for (Entity entity : chunk.getEntities()) {
                    Location eloc = entity.getLocation();
                    if (eloc.getBlockX() >= pos1.getBlockX() && eloc.getBlockZ() >= pos1.getBlockZ() && eloc.getBlockX() <= pos2.getBlockX() && eloc.getBlockZ() <= pos2.getBlockZ()) {
                        if (entity instanceof Player) {
                            PlotMain.teleportPlayer((Player) entity, entity.getLocation(), plot);
                        } else {
                            entity.remove();
                        }
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public boolean setWall(Player player, Plot plot, Block block) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setBiome(Player player, Plot plot, Biome biome) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createRoadEast(Plot plot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createRoadSouth(Plot plot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createRoadSouthEast(Plot plot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeRoadEast(Plot plot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeRoadSouth(Plot plot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeRoadSouthEast(Plot plot) {
        // TODO Auto-generated method stub
        return false;
    }

}
