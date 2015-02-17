////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.AbstractSetBlock;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

@SuppressWarnings("deprecation") public class HybridPlotManager extends ClassicPlotManager {

    private static boolean UPDATE = false;
    private int task;
    
    public static boolean checkModified(Plot plot, int requiredChanges) {
        World world = Bukkit.getWorld(plot.world);
        Location bottom = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        Location top = PlotHelper.getPlotTopLoc(world, plot.id);
        
        int botx = bottom.getBlockX();
        int botz = bottom.getBlockZ();
        
        int topx = top.getBlockX();
        int topz = top.getBlockZ();
        
        HybridPlotWorld hpw = (HybridPlotWorld) PlotMain.getWorldSettings(world);
        
        PlotBlock[] air = new PlotBlock[] {new PlotBlock((short) 0, (byte) 0)};
        
        int changes = checkModified(requiredChanges, world, botx, topx, hpw.PLOT_HEIGHT, hpw.PLOT_HEIGHT, botz, topz, hpw.TOP_BLOCK);
        if (changes == -1) {
            return true;
        }
        requiredChanges -= changes;
        changes = checkModified(requiredChanges, world, botx, topx, hpw.PLOT_HEIGHT + 1, hpw.PLOT_HEIGHT + 1, botz, topz, air);
        if (changes == -1) {
            return true;
        }
        requiredChanges -= changes;
        changes = checkModified(requiredChanges, world, botx, topx, hpw.PLOT_HEIGHT + 2, world.getMaxHeight() - 1, botz, topz, air);
        if (changes == -1) {
            return true;
        }
        requiredChanges -= changes;
        changes = checkModified(requiredChanges, world, botx, topx, 1, hpw.PLOT_HEIGHT - 1, botz, topz, hpw.MAIN_BLOCK);
        return changes == -1;
    }
    
    public static int checkModified(int threshhold, World world, int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock[] blocks) {
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x,  y, z);
                    int id = block.getTypeId();
                    boolean same = false;
                    for (PlotBlock p : blocks) {
                        if (id == p.id) {
                            same = true;
                            break;
                        }
                    }
                    if (!same) {
                        count++;
                        if (count > threshhold) {
                            return -1;
                        }
                    }
                }
            }
        }
        return count;
    }
    
    public boolean setupRoadSchematic(Plot plot) {
        World world = Bukkit.getWorld(plot.world);
        
        Location bot = PlotHelper.getPlotBottomLoc(world, plot.id);
        Location top = PlotHelper.getPlotTopLoc(world, plot.id);

        HybridPlotWorld plotworld = (HybridPlotWorld) PlotMain.getWorldSettings(world);
        
        int sx = bot.getBlockX() - plotworld.ROAD_WIDTH + 1;
        int sz = bot.getBlockZ() + 1;
        int sy = plotworld.ROAD_HEIGHT;
        
        int ex = bot.getBlockX();
        int ez = top.getBlockZ();
        int ey = get_ey(world, sx, ex, sz, ez, sy);
        
        Location pos1 = new Location(world, sx, sy, sz);
        Location pos2 = new Location(world, ex, ey, ez);
        
        int bx = sx;
        int bz = sz - plotworld.ROAD_WIDTH;
        int by = sy;
        
        int tx = ex;
        int tz = sz - 1;
        int ty = get_ey(world, bx, tx, bz, tz, by);
        
        Location pos3 = new Location(world, bx, by, bz);
        Location pos4 = new Location(world, tx, ty, tz);
        
        CompoundTag sideroad = SchematicHandler.getCompoundTag(world, pos1, pos2);
        CompoundTag intersection = SchematicHandler.getCompoundTag(world, pos3, pos4);
        
        String dir = PlotMain.getMain().getDataFolder() + File.separator + "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot.world + File.separator;
        
        SchematicHandler.save(sideroad, dir + "sideroad.schematic");
        SchematicHandler.save(intersection, dir + "intersection.schematic");
        
        plotworld.ROAD_SCHEMATIC_ENABLED = true;
        plotworld.setupSchematics();
        
        return true;
    }
    
    public int get_ey(World world, int sx, int ex, int sz, int ez, int sy) {
        int maxY = world.getMaxHeight();
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) { 
                for (int y = sy; y < maxY; y++) {
                    if (y > ey) {
                        Block block = world.getBlockAt(new Location(world, x, y, z));
                        if (block.getTypeId() !=0) {
                            ey = y;
                        }
                    }
                }
            }
        }
        return ey;
    }
    
    public void regenerateChunkChunk(World world, ChunkLoc loc) {
        
        int sx = loc.x << 5;
        int sz = loc.z << 5;
        
        for (int x = sx; x < sx + 32; x++) {
            for (int z = sz; z < sz + 32; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunk.load(false);
            }
        }
        
        ArrayList<Chunk> chunks2 = new ArrayList<>();
        for (int x = sx; x < sx + 32; x++) {
            for (int z = sz; z < sz + 32; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunks2.add(chunk);
                regenerateRoad(chunk);
            }
        }
        AbstractSetBlock.setBlockManager.update(chunks2);
    }
    
    public boolean scheduleRoadUpdate(final World world) {
        if (HybridPlotManager.UPDATE) {
            return false;
        }
        final ArrayList<ChunkLoc> chunks = ChunkManager.getChunkChunks(world);
        
        final Plugin plugin = (Plugin) PlotMain.getMain();
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (chunks.size() == 0) {
                    HybridPlotManager.UPDATE = false;
                    PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + "Finished road conversion");
                    Bukkit.getScheduler().cancelTask(task);
                    return;
                }
                else {
                    try {
                        ChunkLoc loc = chunks.get(0);
                        PlotMain.sendConsoleSenderMessage("Updating .mcr: " + loc.x + ", "+loc.z + " (aprrox 256 chunks)");
                        PlotMain.sendConsoleSenderMessage("Remaining regions: "+chunks.size());
                        regenerateChunkChunk(world, loc);
                        chunks.remove(0);
                    }
                    catch (Exception e) {
                        ChunkLoc loc = chunks.get(0);
                        PlotMain.sendConsoleSenderMessage("&c[ERROR]&7 Could not update '"+world.getName() + "/region/r." + loc.x + "." + loc.z + ".mca' (Corrupt chunk?)");
                        PlotMain.sendConsoleSenderMessage("&d - Potentially skipping 256 chunks");
                        PlotMain.sendConsoleSenderMessage("&d - TODO: recommend chunkster if corrupt");
                    }
                }
            }
        }, 20, 20);
        return true;
    }
    
    
    public boolean regenerateRoad(Chunk chunk) {
        World world = chunk.getWorld();
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        int ex = x + 15;
        int ez = z + 15;
        
        Location bot = new Location(world, x, 0, z);
        Location top = new Location(world, ex, 0, ez);
        
        HybridPlotWorld plotworld = (HybridPlotWorld) PlotMain.getWorldSettings(world);
        if (!plotworld.ROAD_SCHEMATIC_ENABLED) {
            return false;
        }
        
        PlotId id1 = getPlotId(plotworld, bot);
        PlotId id2 = getPlotId(plotworld, top);

        boolean toCheck = false;
        
        
        if (id1 == null || id2 == null || id1 != id2) {
            boolean result = chunk.load(false);
            if (result) {
                
                while (!chunk.isLoaded()) {
                    chunk.load(false);
                }
                
                if (id1 != null) {
                    Plot p1 = PlotHelper.getPlot(world, id1);
                    if (p1 != null && p1.hasOwner() && p1.settings.isMerged()) {
                        toCheck = true;
                    }
                }
                if (id2 != null && !toCheck) {
                    Plot p2 = PlotHelper.getPlot(world, id2);
                    if (p2 != null && p2.hasOwner() && p2.settings.isMerged()) {
                        toCheck = true;
                    }
                }
                int size = plotworld.SIZE;
                for (int X = 0; X < 16; X++) {
                    for (int Z = 0; Z < 16; Z++) {
                        
                        short absX = (short) ((x + X) % size);
                        short absZ = (short) ((z + Z) % size);
                        
                        if (absX < 0) {
                            absX += size;
                        }
                        if (absZ < 0) {
                            absZ += size;
                        }
                        
                        boolean gx = absX > plotworld.PATH_WIDTH_LOWER;
                        boolean gz = absZ > plotworld.PATH_WIDTH_LOWER;
                        boolean lx = absX < plotworld.PATH_WIDTH_UPPER;
                        boolean lz = absZ < plotworld.PATH_WIDTH_UPPER;
                        boolean condition;
                        
                        if (toCheck) {
                            Location l = new Location(world, x + X, 1, z + Z);
                            condition = getPlotId(plotworld, l) == null;
                        }
                        else { condition = (!gx || !gz || !lx || !lz); }
                        
                        if (condition) {
                            int sy = plotworld.ROAD_HEIGHT + plotworld.OFFSET;
                            ChunkLoc loc = new ChunkLoc(absX, absZ);
                            HashMap<Short, Short> blocks = plotworld.G_SCH.get(loc);
                            for (short y = (short) (plotworld.ROAD_HEIGHT + 1); y <= plotworld.ROAD_HEIGHT + plotworld.SCHEMATIC_HEIGHT; y++) {
                                PlotHelper.setBlock(world, x + X, sy + y, z + Z, 0, (byte) 0);
                            }
                            if (blocks != null) {
                                HashMap<Short, Byte> datas = plotworld.G_SCH_DATA.get(loc);
                                if (datas == null) {
                                    for (Short y : blocks.keySet()) {
                                        PlotHelper.setBlock(world, x + X, sy + y, z + Z, blocks.get(y), (byte) 0);
                                    }
                                }
                                else {
                                    for (Short y : blocks.keySet()) {
                                        Byte data = datas.get(y);
                                        if (data == null) {
                                            data = 0;
                                        }
                                        PlotHelper.setBlock(world, x + X, sy + y, z + Z, blocks.get(y), data);
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean finishPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        if (hpw.ROAD_SCHEMATIC_ENABLED) {
            for (PlotId id : plotIds) {
                Location bottom = getPlotBottomLocAbs(plotworld, id);
                int sx = bottom.getBlockX() - hpw.PATH_WIDTH_LOWER;
                int sz = bottom.getBlockZ() - hpw.PATH_WIDTH_LOWER;
                int sy = hpw.ROAD_HEIGHT + hpw.OFFSET;
                for (ChunkLoc loc : hpw.G_SCH.keySet()) {
                    HashMap<Short, Short> blocks = hpw.G_SCH.get(loc);
                    HashMap<Short, Byte> datas = hpw.G_SCH_DATA.get(loc);
                    if (datas == null) {
                        for (Short y : blocks.keySet()) {
                            PlotHelper.setBlock(world, sx + loc.x, sy + y, sz + loc.z, blocks.get(y), (byte) 0);
                        }
                    }
                    else {
                        for (Short y : blocks.keySet()) {
                            Byte data = datas.get(y);
                            if (data == null) {
                                data = 0;
                            }
                            PlotHelper.setBlock(world, sx + loc.x, sy + y, sz + loc.z, blocks.get(y), data);
                        }
                    }
                }
                
            }
        }
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
    

    /**
     * Clearing the plot needs to only consider removing the blocks - This implementation has used the SetCuboid
     * function, as it is fast, and uses NMS code - It also makes use of the fact that deleting chunks is a lot faster
     * than block updates This code is very messy, but you don't need to do something quite as complex unless you happen
     * to have 512x512 sized plots
     */
    @Override
    public boolean clearPlot(final World world, final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        PlotHelper.runners.put(plot, 1);
        final Plugin plugin = PlotMain.getMain();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                PlotHelper.runners.remove(plot);
            }
        }, 90L);

        final HybridPlotWorld dpw = ((HybridPlotWorld) plotworld);

        final Location pos1 = PlotHelper.getPlotBottomLocAbs(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLocAbs(world, plot.id);

        final PlotBlock[] plotfloor = dpw.TOP_BLOCK;
        final PlotBlock[] filling = dpw.MAIN_BLOCK;

        // PlotBlock wall = dpw.WALL_BLOCK;
        final PlotBlock wall;

        if (isDelete) {
            wall = dpw.WALL_BLOCK;
        } else {
            wall = dpw.CLAIMED_WALL_BLOCK;
        }

        final PlotBlock wall_filling = dpw.WALL_FILLING;

        final Block block = world.getBlockAt(new Location(world, pos1.getBlockX() - 1, 1, pos1.getBlockZ()));
        if ((block.getTypeId() != wall_filling.id) || (block.getData() != wall_filling.data)) {
            setWallFilling(world, dpw, plot.id, new PlotBlock[] {wall_filling});
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {

                final Block block = world.getBlockAt(new Location(world, pos1.getBlockX() - 1, dpw.WALL_HEIGHT + 1, pos1.getBlockZ()));
                if ((block.getTypeId() != wall.id) || (block.getData() != wall.data)) {
                    setWall(world, dpw, plot.id, new PlotBlock[] {wall});
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if ((pos2.getBlockX() - pos1.getBlockX()) < 48) {
                            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), 0, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, 1, pos2.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT + 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, world.getMaxHeight() + 1, pos2.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT, pos2.getBlockZ() + 1), filling);
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getBlockZ() + 1), plotfloor);
                                                }
                                            }, 5L);
                                        }
                                    }, 5L);
                                }
                            }, 5L);
                            return;
                        }

                        final int startX = (pos1.getBlockX() / 16) * 16;
                        final int startZ = (pos1.getBlockZ() / 16) * 16;
                        final int chunkX = 16 + pos2.getBlockX();
                        final int chunkZ = 16 + pos2.getBlockZ();
                        final Location l1 = PlotHelper.getPlotBottomLoc(world, plot.id);
                        final Location l2 = PlotHelper.getPlotTopLoc(world, plot.id);
                        final int plotMinX = l1.getBlockX() + 1;
                        final int plotMinZ = l1.getBlockZ() + 1;
                        final int plotMaxX = l2.getBlockX();
                        final int plotMaxZ = l2.getBlockZ();
                        Location mn = null;
                        Location mx = null;
                        for (int i = startX; i < chunkX; i += 16) {
                            for (int j = startZ; j < chunkZ; j += 16) {
                                final Plot plot1 = PlotHelper.getCurrentPlot(new Location(world, i, 0, j));
                                if ((plot1 != null) && (!plot1.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot2 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j));
                                if ((plot2 != null) && (!plot2.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot3 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j + 15));
                                if ((plot3 != null) && (!plot3.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot4 = PlotHelper.getCurrentPlot(new Location(world, i, 0, j + 15));
                                if ((plot4 != null) && (!plot4.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot5 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j + 15));
                                if ((plot5 != null) && (!plot5.getId().equals(plot.getId()))) {
                                    break;
                                }
                                if (mn == null) {
                                    mn = new Location(world, Math.max(i - 1, plotMinX), 0, Math.max(j - 1, plotMinZ));
                                    mx = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                                } else if ((mx.getBlockZ() < (j + 15)) || (mx.getBlockX() < (i + 15))) {
                                    mx = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                                }
                                world.regenerateChunk(i / 16, j / 16);
                            }
                        }

                        final Location max = mx;
                        final Location min = mn;

                        if (min == null) {
                            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), 0, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, 1, pos2.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT + 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, world.getMaxHeight() + 1, pos2.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT, pos2.getBlockZ() + 1), filling);
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getBlockZ() + 1), plotfloor);
                                                }
                                            }, 5L);
                                        }
                                    }, 5L);
                                }
                            }, 5L);
                            return;
                        } else {

                            if (min.getBlockX() < plotMinX) {
                                min.setX(plotMinX);
                            }
                            if (min.getBlockZ() < plotMinZ) {
                                min.setZ(plotMinZ);
                            }
                            if (max.getBlockX() > plotMaxX) {
                                max.setX(plotMaxX);
                            }
                            if (max.getBlockZ() > plotMaxZ) {
                                max.setZ(plotMaxZ);
                            }

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, plotMinZ), new Location(world, min.getBlockX() + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, plotMinZ), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, plotMinZ), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 21L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), 0, plotMinZ), new Location(world, max.getBlockX() + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, max.getBlockX() + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), 1, plotMinZ), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT, min.getBlockZ() + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 25L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, plotMinZ), new Location(world, plotMaxX + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, min.getBlockZ() + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 29L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, min.getBlockZ()), new Location(world, min.getBlockX() + 1, 1, max.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, min.getBlockZ()), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, max.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, min.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT, max.getBlockZ() + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, min.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, max.getBlockZ() + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 33L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, max.getBlockZ()), new Location(world, min.getBlockX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, max.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 37L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), 0, max.getBlockZ()), new Location(world, max.getBlockX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, max.getBlockX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), 1, max.getBlockZ()), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 41L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, min.getBlockZ()), new Location(world, plotMaxX + 1, 1, max.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, min.getBlockZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, max.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, min.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, max.getBlockZ() + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, min.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, max.getBlockZ() + 1), plotfloor);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 45L);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, max.getBlockZ()), new Location(world, plotMaxX + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, max.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                                                            TaskManager.runTask(whenDone);
                                                        }
                                                    }, 1L);
                                                }
                                            }, 1L);
                                        }
                                    }, 1L);
                                }
                            }, 49L);
                        }
                    }
                }, 20L);
            }
        }, 20L);
        return true;
    }
}
