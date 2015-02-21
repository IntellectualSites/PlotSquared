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

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.ChunkManager;
import com.intellectualcrafters.plot.util.bukkit.SetBlockManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

@SuppressWarnings("deprecation")
public class HybridPlotManager extends ClassicPlotManager {
    private static boolean UPDATE = false;
    private int task;
    
    public static boolean checkModified(final Plot plot, int requiredChanges) {
        final Location bottom = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        final int botx = bottom.getX();
        final int botz = bottom.getZ();
        final int topx = top.getX();
        final int topz = top.getZ();
        final HybridPlotWorld hpw = (HybridPlotWorld) PlotSquared.getPlotWorld(plot.world);
        final PlotBlock[] air = new PlotBlock[] { new PlotBlock((short) 0, (byte) 0) };
        int changes = checkModified(requiredChanges, plot.world, botx, topx, hpw.PLOT_HEIGHT, hpw.PLOT_HEIGHT, botz, topz, hpw.TOP_BLOCK);
        if (changes == -1) {
            return true;
        }
        requiredChanges -= changes;
        changes = checkModified(requiredChanges, plot.world, botx, topx, hpw.PLOT_HEIGHT + 1, hpw.PLOT_HEIGHT + 1, botz, topz, air);
        if (changes == -1) {
            return true;
        }
        requiredChanges -= changes;
        changes = checkModified(requiredChanges, plot.world, botx, topx, hpw.PLOT_HEIGHT + 2, BukkitUtil.getMaxHeight(plot.world) - 1, botz, topz, air);
        if (changes == -1) {
            return true;
        }
        requiredChanges -= changes;
        changes = checkModified(requiredChanges, plot.world, botx, topx, 1, hpw.PLOT_HEIGHT - 1, botz, topz, hpw.MAIN_BLOCK);
        return changes == -1;
    }
    
    public static int checkModified(final int threshhold, final String world, final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock[] blocks) {
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    final Block block = world.getBlockAt(x, y, z);
                    final int id = block.getTypeId();
                    boolean same = false;
                    for (final PlotBlock p : blocks) {
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
    
    public boolean setupRoadSchematic(final Plot plot) {
        final String world = plot.world;
        final Location bot = MainUtil.getPlotBottomLoc(world, plot.id);
        final Location top = MainUtil.getPlotTopLoc(world, plot.id);
        final HybridPlotWorld plotworld = (HybridPlotWorld) PlotSquared.getPlotWorld(world);
        final int sx = (bot.getX() - plotworld.ROAD_WIDTH) + 1;
        final int sz = bot.getZ() + 1;
        final int sy = plotworld.ROAD_HEIGHT;
        final int ex = bot.getX();
        final int ez = top.getZ();
        final int ey = get_ey(world, sx, ex, sz, ez, sy);
        final Location pos1 = new Location(world, sx, sy, sz);
        final Location pos2 = new Location(world, ex, ey, ez);
        final int bx = sx;
        final int bz = sz - plotworld.ROAD_WIDTH;
        final int by = sy;
        final int tx = ex;
        final int tz = sz - 1;
        final int ty = get_ey(world, bx, tx, bz, tz, by);
        final Location pos3 = new Location(world, bx, by, bz);
        final Location pos4 = new Location(world, tx, ty, tz);
        final CompoundTag sideroad = SchematicHandler.getCompoundTag(world, pos1, pos2);
        final CompoundTag intersection = SchematicHandler.getCompoundTag(world, pos3, pos4);
        final String dir = PlotSquared.IMP.getDirectory() + File.separator + "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plot.world + File.separator;
        SchematicHandler.save(sideroad, dir + "sideroad.schematic");
        SchematicHandler.save(intersection, dir + "intersection.schematic");
        plotworld.ROAD_SCHEMATIC_ENABLED = true;
        plotworld.setupSchematics();
        return true;
    }
    
    public int get_ey(final String world, final int sx, final int ex, final int sz, final int ez, final int sy) {
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y < maxY; y++) {
                    if (y > ey) {
                        final Block block = world.getBlockAt(x, y, z);
                        if (block.getTypeId() != 0) {
                            ey = y;
                        }
                    }
                }
            }
        }
        return ey;
    }
    
    public void regenerateChunkChunk(final String world, final ChunkLoc loc) {
        final int sx = loc.x << 5;
        final int sz = loc.z << 5;
        for (int x = sx; x < (sx + 32); x++) {
            for (int z = sz; z < (sz + 32); z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunk.load(false);
            }
        }
        final ArrayList<Chunk> chunks2 = new ArrayList<>();
        for (int x = sx; x < (sx + 32); x++) {
            for (int z = sz; z < (sz + 32); z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunks2.add(chunk);
                regenerateRoad(chunk);
            }
        }
        SetBlockManager.setBlockManager.update(chunks2);
    }
    
    public boolean scheduleRoadUpdate(final String world) {
        if (HybridPlotManager.UPDATE) {
            return false;
        }
        final ArrayList<ChunkLoc> chunks = ChunkManager.getChunkChunks(world);
        final Plugin plugin = (Plugin) PlotSquared.getMain();
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (chunks.size() == 0) {
                    HybridPlotManager.UPDATE = false;
                    PlotSquared.log(C.PREFIX.s() + "Finished road conversion");
                    Bukkit.getScheduler().cancelTask(HybridPlotManager.this.task);
                    return;
                } else {
                    try {
                        final ChunkLoc loc = chunks.get(0);
                        PlotSquared.log("Updating .mcr: " + loc.x + ", " + loc.z + " (aprrox 256 chunks)");
                        PlotSquared.log("Remaining regions: " + chunks.size());
                        regenerateChunkChunk(world, loc);
                        chunks.remove(0);
                    } catch (final Exception e) {
                        final ChunkLoc loc = chunks.get(0);
                        PlotSquared.log("&c[ERROR]&7 Could not update '" + world.getName() + "/region/r." + loc.x + "." + loc.z + ".mca' (Corrupt chunk?)");
                        PlotSquared.log("&d - Potentially skipping 256 chunks");
                        PlotSquared.log("&d - TODO: recommend chunkster if corrupt");
                    }
                }
            }
        }, 20, 20);
        return true;
    }
    
    @Override
    public boolean finishPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        if (hpw.ROAD_SCHEMATIC_ENABLED) {
            for (final PlotId id : plotIds) {
                final Location bottom = getPlotBottomLocAbs(plotworld, id);
                final int sx = bottom.getX() - hpw.PATH_WIDTH_LOWER;
                final int sz = bottom.getZ() - hpw.PATH_WIDTH_LOWER;
                final int sy = hpw.ROAD_HEIGHT;
                for (final ChunkLoc loc : hpw.G_SCH.keySet()) {
                    final HashMap<Short, Short> blocks = hpw.G_SCH.get(loc);
                    final HashMap<Short, Byte> datas = hpw.G_SCH_DATA.get(loc);
                    if (datas == null) {
                        for (final Short y : blocks.keySet()) {
                            MainUtil.setBlock(world, sx + loc.x, sy + y, sz + loc.z, blocks.get(y), (byte) 0);
                        }
                    } else {
                        for (final Short y : blocks.keySet()) {
                            Byte data = datas.get(y);
                            if (data == null) {
                                data = 0;
                            }
                            MainUtil.setBlock(world, sx + loc.x, sy + y, sz + loc.z, blocks.get(y), data);
                        }
                    }
                }
            }
        }
        final PlotBlock block = ((ClassicPlotWorld) plotworld).WALL_BLOCK;
        if (block.id != 0) {
            for (final PlotId id : plotIds) {
                setWall(world, plotworld, id, new PlotBlock[] { ((ClassicPlotWorld) plotworld).WALL_BLOCK });
                final Plot plot = MainUtil.getPlot(world, id);
                if (plot.hasOwner()) {
                    final String name = UUIDHandler.getName(plot.owner);
                    if (name != null) {
                        MainUtil.setSign(world, name, plot);
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
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final String world = plotworld.worldname;
        MainUtil.runners.put(plot, 1);
        final Plugin plugin = PlotSquared.getMain();
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                MainUtil.runners.remove(plot);
            }
        }, 90L);
        final HybridPlotWorld dpw = ((HybridPlotWorld) plotworld);
        final Location pos1 = MainUtil.getPlotBottomLocAbs(world, plot.id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLocAbs(world, plot.id);
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
        final Block block = world.getBlockAt(new Location(world, pos1.getX() - 1, 1, pos1.getZ()));
        if ((block.getTypeId() != wall_filling.id) || (block.getData() != wall_filling.data)) {
            setWallFilling(world, dpw, plot.id, new PlotBlock[] { wall_filling });
        }
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                final Block block = world.getBlockAt(new Location(world, pos1.getX() - 1, dpw.WALL_HEIGHT + 1, pos1.getZ()));
                if ((block.getTypeId() != wall.id) || (block.getData() != wall.data)) {
                    setWall(world, dpw, plot.id, new PlotBlock[] { wall });
                }
                TaskManager.runTaskLater(new Runnable() {
                    @Override
                    public void run() {
                        if ((pos2.getX() - pos1.getX()) < 48) {
                            MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), 0, pos1.getZ()), new Location(world, pos2.getX() + 1, 1, pos2.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT + 1, pos1.getZ()), new Location(world, pos2.getX() + 1, world.getMaxHeight() + 1, pos2.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setCuboid(world, new Location(world, pos1.getX(), 1, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT, pos2.getZ() + 1), filling);
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), plotfloor);
                                                }
                                            }, 5L);
                                        }
                                    }, 5L);
                                }
                            }, 5L);
                            return;
                        }
                        final int startX = (pos1.getX() / 16) * 16;
                        final int startZ = (pos1.getZ() / 16) * 16;
                        final int chunkX = 16 + pos2.getX();
                        final int chunkZ = 16 + pos2.getZ();
                        final Location l1 = MainUtil.getPlotBottomLoc(world, plot.id);
                        final Location l2 = MainUtil.getPlotTopLoc(world, plot.id);
                        final int plotMinX = l1.getX() + 1;
                        final int plotMinZ = l1.getZ() + 1;
                        final int plotMaxX = l2.getX();
                        final int plotMaxZ = l2.getZ();
                        Location mn = null;
                        Location mx = null;
                        for (int i = startX; i < chunkX; i += 16) {
                            for (int j = startZ; j < chunkZ; j += 16) {
                                final Plot plot1 = MainUtil.getPlot(new Location(world, i, 0, j));
                                if ((plot1 != null) && (!plot1.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot2 = MainUtil.getPlot(new Location(world, i + 15, 0, j));
                                if ((plot2 != null) && (!plot2.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot3 = MainUtil.getPlot(new Location(world, i + 15, 0, j + 15));
                                if ((plot3 != null) && (!plot3.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot4 = MainUtil.getPlot(new Location(world, i, 0, j + 15));
                                if ((plot4 != null) && (!plot4.getId().equals(plot.getId()))) {
                                    break;
                                }
                                final Plot plot5 = MainUtil.getPlot(new Location(world, i + 15, 0, j + 15));
                                if ((plot5 != null) && (!plot5.getId().equals(plot.getId()))) {
                                    break;
                                }
                                if (mn == null) {
                                    mn = new Location(world, Math.max(i - 1, plotMinX), 0, Math.max(j - 1, plotMinZ));
                                    mx = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                                } else if ((mx.getZ() < (j + 15)) || (mx.getX() < (i + 15))) {
                                    mx = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                                }
                                world.regenerateChunk(i / 16, j / 16);
                            }
                        }
                        final Location max = mx;
                        final Location min = mn;
                        if (min == null) {
                            MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), 0, pos1.getZ()), new Location(world, pos2.getX() + 1, 1, pos2.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT + 1, pos1.getZ()), new Location(world, pos2.getX() + 1, world.getMaxHeight() + 1, pos2.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setCuboid(world, new Location(world, pos1.getX(), 1, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT, pos2.getZ() + 1), filling);
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), plotfloor);
                                                }
                                            }, 5L);
                                        }
                                    }, 5L);
                                }
                            }, 5L);
                            return;
                        } else {
                            if (min.getX() < plotMinX) {
                                min.setX(plotMinX);
                            }
                            if (min.getZ() < plotMinZ) {
                                min.setZ(plotMinZ);
                            }
                            if (max.getX() > plotMaxX) {
                                max.setX(plotMaxX);
                            }
                            if (max.getZ() > plotMaxZ) {
                                max.setZ(plotMaxZ);
                            }
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, 0, plotMinZ), new Location(world, min.getX() + 1, 1, min.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, min.getX() + 1, world.getMaxHeight() + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, plotMinX, 1, plotMinZ), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, plotMinZ), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 21);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), 0, plotMinZ), new Location(world, max.getX() + 1, 1, min.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, max.getX() + 1, world.getMaxHeight() + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, min.getX(), 1, plotMinZ), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT, min.getZ() + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 25L);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), 0, plotMinZ), new Location(world, plotMaxX + 1, 1, min.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, max.getX(), 1, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, min.getZ() + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 29L);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, 0, min.getZ()), new Location(world, min.getX() + 1, 1, max.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, min.getZ()), new Location(world, min.getX() + 1, world.getMaxHeight() + 1, max.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, plotMinX, 1, min.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT, max.getZ() + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, min.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, max.getZ() + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 33L);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, 0, max.getZ()), new Location(world, min.getX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, min.getX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, plotMinX, 1, max.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, max.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 37L);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), 0, max.getZ()), new Location(world, max.getX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, max.getX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, min.getX(), 1, max.getZ()), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT, max.getZ()), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 41);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), 0, min.getZ()), new Location(world, plotMaxX + 1, 1, max.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, min.getZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, max.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, max.getX(), 1, min.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, max.getZ() + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT, min.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, max.getZ() + 1), plotfloor);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 45);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), 0, max.getZ()), new Location(world, plotMaxX + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, max.getX(), 1, max.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                                                    TaskManager.runTaskLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            MainUtil.setCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT, max.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                                                            TaskManager.runTask(whenDone);
                                                        }
                                                    }, 1);
                                                }
                                            }, 1);
                                        }
                                    }, 1);
                                }
                            }, 49);
                        }
                    }
                }, 20);
            }
        }, 20);
        return true;
    }
}
