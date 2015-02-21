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
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                MainUtil.runners.remove(plot);
            }
        }, 90);
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
        setWallFilling(dpw, plot.id, new PlotBlock[] { wall_filling });
        final int maxy = BukkitUtil.getMaxHeight(world);
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                setWall(dpw, plot.id, new PlotBlock[] { wall });
                TaskManager.runTaskLater(new Runnable() {
                    @Override
                    public void run() {
                        if ((pos2.getX() - pos1.getX()) < 48) {
                            MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), 0, pos1.getZ()), new Location(world, pos2.getX() + 1, 1, pos2.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT + 1, pos1.getZ()), new Location(world, pos2.getX() + 1, maxy + 1, pos2.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setCuboid(world, new Location(world, pos1.getX(), 1, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT, pos2.getZ() + 1), filling);
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), plotfloor);
                                                }
                                            }, 5);
                                        }
                                    }, 5);
                                }
                            }, 5);
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
                                BukkitUtil.regenerateChunk(world, i / 16, j / 16);
                            }
                        }
                        final Location max = mx;
                        final Location min = mn;
                        if (min == null) {
                            MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), 0, pos1.getZ()), new Location(world, pos2.getX() + 1, 1, pos2.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT + 1, pos1.getZ()), new Location(world, pos2.getX() + 1, maxy + 1, pos2.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setCuboid(world, new Location(world, pos1.getX(), 1, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT, pos2.getZ() + 1), filling);
                                            TaskManager.runTaskLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainUtil.setCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), plotfloor);
                                                }
                                            }, 5);
                                        }
                                    }, 5);
                                }
                            }, 5);
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
                                            MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, min.getX() + 1, maxy + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
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
                                            MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, max.getX() + 1, maxy + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
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
                            }, 25);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), 0, plotMinZ), new Location(world, plotMaxX + 1, 1, min.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, plotMaxX + 1, maxy + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
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
                            }, 29);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, 0, min.getZ()), new Location(world, min.getX() + 1, 1, max.getZ() + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, min.getZ()), new Location(world, min.getX() + 1, maxy + 1, max.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
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
                            }, 33);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, 0, max.getZ()), new Location(world, min.getX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, min.getX() + 1, maxy + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
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
                            }, 37);
                            TaskManager.runTaskLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), 0, max.getZ()), new Location(world, max.getX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
                                    TaskManager.runTaskLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.setSimpleCuboid(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, max.getX() + 1, maxy + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
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
                                            MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, min.getZ()), new Location(world, plotMaxX + 1, maxy + 1, max.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
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
                                            MainUtil.setSimpleCuboid(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, plotMaxX + 1, maxy + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
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
