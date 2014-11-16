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

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotWorld;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * @author Citymonstret
 */
public class XPopulator extends BlockPopulator {

    /*
     * Sorry, this isn't well documented at the moment.
     * We advise you to take a look at a world generation tutorial for
     * information about how a BlockPopulator works.
     */

    private final DefaultPlotWorld plotworld;
    int plotsize;
    int pathsize;
    PlotBlock wall;
    PlotBlock wallfilling;
    PlotBlock floor1;
    PlotBlock floor2;
    int size;
    Biome biome;
    int roadheight;
    int wallheight;
    int plotheight;
    PlotBlock[] plotfloors;
    PlotBlock[] filling;
    private int X;
    private int Z;
    private long state;
    private double pathWidthLower;

    public XPopulator(final PlotWorld pw) {
        this.plotworld = (DefaultPlotWorld) pw;

        // save configuration

        this.plotsize = this.plotworld.PLOT_WIDTH;
        this.pathsize = this.plotworld.ROAD_WIDTH;

        this.floor1 = this.plotworld.ROAD_BLOCK;
        this.floor2 = this.plotworld.ROAD_STRIPES;

        this.wallfilling = this.plotworld.WALL_FILLING;
        this.size = this.pathsize + this.plotsize;
        this.wall = this.plotworld.WALL_BLOCK;

        this.plotfloors = this.plotworld.TOP_BLOCK;
        this.filling = this.plotworld.MAIN_BLOCK;

        this.wallheight = this.plotworld.WALL_HEIGHT;
        this.roadheight = this.plotworld.ROAD_HEIGHT;
        this.plotheight = this.plotworld.PLOT_HEIGHT;

        if ((this.pathsize % 2) == 0) {
            this.pathWidthLower = Math.floor(this.pathsize / 2) - 1;
        } else {
            this.pathWidthLower = Math.floor(this.pathsize / 2);
        }
    }

    public final long nextLong() {
        final long a = this.state;
        this.state = xorShift64(a);
        return a;
    }

    public final long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public final int random(final int n) {
        final long result = ((nextLong() >>> 32) * n) >> 32;
        return (int) result;
    }

    public void setCuboidRegion(final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock block, final World w) {
        if (block.data == 0) {
            return;
        }
        for (int x = x1; x < x2; x++) {
            for (int z = z1; z < z2; z++) {
                for (int y = y1; y < y2; y++) {
                    setBlock(w, x, y, z, block.id, block.data);
                }
            }
        }
    }

    private void setCuboidRegion(final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock[] blocks, final World w) {
        if (blocks.length == 1) {
            setCuboidRegion(x1, x2, y1, y2, z1, z2, blocks[0], w);
        } else {
            for (int x = x1; x < x2; x++) {
                for (int z = z1; z < z2; z++) {
                    for (int y = y1; y < y2; y++) {
                        final int i = random(blocks.length);
                        if (blocks[i].data != 0) {
                            setBlock(w, x, y, z, blocks[i].id, blocks[i].data);
                        }
                    }
                }
            }
        }

    }

    public short[] getBlock(final String block) {
        if (block.contains(":")) {
            final String[] split = block.split(":");
            return new short[]{Short.parseShort(split[0]), Short.parseShort(split[1])};
        }
        return new short[]{Short.parseShort(block), 0};
    }

    @Override
    public void populate(final World w, final Random r, final Chunk c) {
        int cx = c.getX(), cz = c.getZ();

        final int prime = 31;
        int h = 1;
        h = (prime * h) + cx;
        h = (prime * h) + cz;
        this.state = h;

        this.X = cx << 4;
        this.Z = cz << 4;
        cx = (cx % this.size) + (8 * this.size);
        cz = (cz % this.size) + (8 * this.size);
        final double absX = ((((cx * 16) + 16) - this.pathWidthLower - 1) + (8 * this.size)), absZ = ((((cz * 16) + 16) - this.pathWidthLower - 1) + (8 * this.size));
        final int plotMinX = (int) (((absX) % this.size));
        final int plotMinZ = (int) (((absZ) % this.size));
        int roadStartX = (plotMinX + this.pathsize);
        int roadStartZ = (plotMinZ + this.pathsize);
        if (roadStartX >= this.size) {
            roadStartX -= this.size;
        }
        if (roadStartZ >= this.size) {
            roadStartZ -= this.size;
        }

        // ROADS

        if ((this.pathsize > 16) && ((plotMinX > roadStartX) || (plotMinZ > roadStartZ)) && !((roadStartX < 16) && (roadStartZ < 16)) && (((roadStartX > 16) && (roadStartZ > 16)) || ((plotMinX > roadStartX) && (plotMinZ > roadStartZ)))) {
            setCuboidRegion(0, 16, 1, this.roadheight + 1, 0, 16, this.floor1, w);
            return;
        }

        if (((plotMinZ + 1) <= 16) || ((roadStartZ <= 16) && (roadStartZ > 0))) {
            final int start = Math.max((16 - plotMinZ - this.pathsize) + 1, (16 - roadStartZ) + 1);
            int end = Math.min(16 - plotMinZ - 1, (16 - roadStartZ) + this.pathsize);
            if ((start >= 0) && (start <= 16) && (end < 0)) {
                end = 16;
            }
            setCuboidRegion(0, 16, 1, this.roadheight + 1, Math.max(start, 0), Math.min(16, end), this.floor1, w);
        }
        if (((plotMinX + 1) <= 16) || ((roadStartX <= 16) && (roadStartX > 0))) {
            final int start = Math.max((16 - plotMinX - this.pathsize) + 1, (16 - roadStartX) + 1);
            int end = Math.min(16 - plotMinX - 1, (16 - roadStartX) + this.pathsize);
            if ((start >= 0) && (start <= 16) && (end < 0)) {
                end = 16;
            }
            setCuboidRegion(Math.max(start, 0), Math.min(16, end), 1, this.roadheight + 1, 0, 16, this.floor1, w);
        }

        // STRIPES
        if ((this.pathsize > 4) && this.plotworld.ROAD_STRIPES_ENABLED) {
            if ((plotMinZ + 2) <= 16) {
                final int value = (plotMinZ + 2);
                int start, end;
                if ((plotMinX + 2) <= 16) {
                    start = 16 - plotMinX - 1;
                } else {
                    start = 16;
                }
                if ((roadStartX - 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 2) <= 16) || ((roadStartX - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, this.roadheight, this.roadheight + 1, 16 - value, (16 - value) + 1, this.floor2, w); //
                setCuboidRegion(start, 16, this.roadheight, this.roadheight + 1, 16 - value, (16 - value) + 1, this.floor2, w); //
            }
            if ((plotMinX + 2) <= 16) {
                final int value = (plotMinX + 2);
                int start, end;
                if ((plotMinZ + 2) <= 16) {
                    start = 16 - plotMinZ - 1;
                } else {
                    start = 16;
                }
                if ((roadStartZ - 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 2) <= 16) || ((roadStartZ - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(16 - value, (16 - value) + 1, this.roadheight, this.roadheight + 1, 0, end, this.floor2, w); //
                setCuboidRegion(16 - value, (16 - value) + 1, this.roadheight, this.roadheight + 1, start, 16, this.floor2, w); //
            }
            if ((roadStartZ <= 16) && (roadStartZ > 1)) {
                final int val = roadStartZ;
                int start, end;
                if ((plotMinX + 2) <= 16) {
                    start = 16 - plotMinX - 1;
                } else {
                    start = 16;
                }
                if ((roadStartX - 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 2) <= 16) || ((roadStartX - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, this.roadheight, this.roadheight + 1, (16 - val) + 1, (16 - val) + 2, this.floor2, w);
                setCuboidRegion(start, 16, this.roadheight, this.roadheight + 1, (16 - val) + 1, (16 - val) + 2, this.floor2, w);
            }
            if ((roadStartX <= 16) && (roadStartX > 1)) {
                final int val = roadStartX;
                int start, end;
                if ((plotMinZ + 2) <= 16) {
                    start = 16 - plotMinZ - 1;
                } else {
                    start = 16;
                }
                if ((roadStartZ - 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 2) <= 16) || ((roadStartZ - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion((16 - val) + 1, (16 - val) + 2, this.roadheight, this.roadheight + 1, 0, end, this.floor2, w); //
                setCuboidRegion((16 - val) + 1, (16 - val) + 2, this.roadheight, this.roadheight + 1, start, 16, this.floor2, w); //
            }
        }
        // WALLS
        if (this.pathsize > 0) {
            if ((plotMinZ + 1) <= 16) {
                int start, end;
                if ((plotMinX + 2) <= 16) {
                    start = 16 - plotMinX - 1;
                } else {
                    start = 16;
                }
                if ((roadStartX - 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 2) <= 16) || ((roadStartX - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, this.wallheight + 1, 16 - plotMinZ - 1, 16 - plotMinZ, this.wallfilling, w);
                setCuboidRegion(0, end, this.wallheight + 1, this.wallheight + 2, 16 - plotMinZ - 1, 16 - plotMinZ, this.wall, w);
                setCuboidRegion(start, 16, 1, this.wallheight + 1, 16 - plotMinZ - 1, 16 - plotMinZ, this.wallfilling, w);
                setCuboidRegion(start, 16, this.wallheight + 1, this.wallheight + 2, 16 - plotMinZ - 1, 16 - plotMinZ, this.wall, w);
            }
            if ((plotMinX + 1) <= 16) {
                int start, end;
                if ((plotMinZ + 2) <= 16) {
                    start = 16 - plotMinZ - 1;
                } else {
                    start = 16;
                }
                if ((roadStartZ - 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 2) <= 16) || ((roadStartZ - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, 1, this.wallheight + 1, 0, end, this.wallfilling, w);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, this.wallheight + 1, this.wallheight + 2, 0, end, this.wall, w);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, 1, this.wallheight + 1, start, 16, this.wallfilling, w);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, this.wallheight + 1, this.wallheight + 2, start, 16, this.wall, w);
            }
            if ((roadStartZ <= 16) && (roadStartZ > 0)) {
                int start, end;
                if ((plotMinX + 1) <= 16) {
                    start = 16 - plotMinX;
                } else {
                    start = 16;
                }
                if ((roadStartX + 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 1) <= 16) || (roadStartX <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, this.wallheight + 1, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wallfilling, w);
                setCuboidRegion(0, end, this.wallheight + 1, this.wallheight + 2, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wall, w);
                setCuboidRegion(start, 16, 1, this.wallheight + 1, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wallfilling, w);
                setCuboidRegion(start, 16, this.wallheight + 1, this.wallheight + 2, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wall, w);
            }
            if ((roadStartX <= 16) && (roadStartX > 0)) {
                int start, end;
                if ((plotMinZ + 1) <= 16) {
                    start = 16 - plotMinZ;
                } else {
                    start = 16;
                }
                if ((roadStartZ + 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 1) <= 16) || ((roadStartZ + 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, 1, this.wallheight + 1, 0, end, this.wallfilling, w);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, this.wallheight + 1, this.roadheight + 2, 0, end, this.wall, w);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, 1, this.wallheight + 1, start, 16, this.wallfilling, w);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, this.wallheight + 1, this.wallheight + 2, start, 16, this.wall, w);
            }
        }

        // PLOT

        if (this.plotsize > 16) {
            if (roadStartX <= 16) {
                if (roadStartZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 0, 16 - roadStartZ, this.filling, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors, w);
                }
                if (plotMinZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 16 - plotMinZ, 16, this.filling, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors, w);
                }
            } else {
                if (roadStartZ <= 16) {
                    if (plotMinX > 16) {
                        setCuboidRegion(0, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.filling, w);
                        setCuboidRegion(0, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors, w);
                    }
                }
            }
            if (plotMinX <= 16) {
                if (plotMinZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 16 - plotMinZ, 16, this.filling, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors, w);
                } else {
                    int z = 16 - roadStartZ;
                    if (z < 0) {
                        z = 16;
                    }
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, z, this.filling, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, z, this.plotfloors, w);
                }
                if (roadStartZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.filling, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors, w);
                } else {
                    if (roadStartX <= 16) {
                        if (plotMinZ > 16) {
                            int x = 16 - roadStartX;
                            if (x < 0) {
                                x = 16;
                            }
                            setCuboidRegion(0, x, 1, this.plotheight, 0, 16, this.filling, w);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, 16, this.plotfloors, w);
                        }
                    }
                }
            } else {
                if (plotMinZ <= 16) {
                    if (roadStartX > 16) {
                        int x = 16 - roadStartX;
                        if (x < 0) {
                            x = 16;
                        }
                        setCuboidRegion(0, x, 1, this.plotheight, 16 - plotMinZ, 16, this.filling, w);
                        setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors, w);
                    }
                } else {
                    if (roadStartZ > 16) {
                        int x = 16 - roadStartX;
                        if (x < 0) {
                            x = 16;
                        }
                        int z = 16 - roadStartZ;
                        if (z < 0) {
                            z = 16;
                        }
                        if (roadStartX > 16) {
                            setCuboidRegion(0, x, 1, this.plotheight, 0, z, this.filling, w);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, z, this.plotfloors, w);
                        } else {
                            setCuboidRegion(0, x, 1, this.plotheight, 0, z, this.filling, w);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, z, this.plotfloors, w);
                        }
                    }
                }
            }
        } else {
            if (roadStartX <= 16) {
                if (roadStartZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 0, 16 - roadStartZ, this.filling, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors, w);
                }
                if (plotMinZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 16 - plotMinZ, 16, this.filling, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors, w);
                }
            }
            if (plotMinX <= 16) {
                if (plotMinZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 16 - plotMinZ, 16, this.filling, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors, w);
                }
                if (roadStartZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.filling, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors, w);
                }
            }
        }
    }

    private void setBlock(final World w, final int x, final int y, final int z, final short id, final byte val) {
        w.getBlockAt(this.X + x, y, this.Z + z).setData(val, false);
    }

}
