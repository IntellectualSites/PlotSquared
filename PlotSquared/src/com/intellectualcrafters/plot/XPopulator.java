package com.intellectualcrafters.plot;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

/**
 * TODO recode this class Fuck you PlotMe!
 * 
 * @author Citymonstret
 * 
 */
public class XPopulator extends BlockPopulator {
    private int X;
    private int Z;
    private long state;

    public final long nextLong() {
        long a = this.state;
        this.state = xorShift64(a);
        return a;
    }

    public final long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public final int random(int n) {
        long result = ((nextLong() >>> 32) * n) >> 32;
        return (int) result;
    }

    public void setCuboidRegion(int x1, int x2, int y1, int y2, int z1, int z2, short id, byte data, World w) {
        if (data == 0) {
            return;
        }
        for (int x = x1; x < x2; x++) {
            for (int z = z1; z < z2; z++) {
                for (int y = y1; y < y2; y++) {
                    setBlock(w, x, y, z, id, data);
                }
            }
        }
    }

    private void setCuboidRegion(int x1, int x2, int y1, int y2, int z1, int z2, short[] id, short[] v, World w) {
        if (id.length == 1) {
            setCuboidRegion(x1, x2, y1, y2, z1, z2, id[0], (byte) v[0], w);
        } else {
            for (int x = x1; x < x2; x++) {
                for (int z = z1; z < z2; z++) {
                    for (int y = y1; y < y2; y++) {
                        int i = random(id.length);
                        if (v[i] != 0) {
                            setBlock(w, x, y, z, id[i], (byte) v[i]);
                        }
                    }
                }
            }
        }

    }

    public short[] getBlock(String block) {
        if (block.contains(":")) {
            String[] split = block.split(":");
            return new short[] { Short.parseShort(split[0]), Short.parseShort(split[1]) };
        }
        return new short[] { Short.parseShort(block), 0 };
    }

    private int plotsize, pathsize, plotheight, wallheight, roadheight, size;
    private byte w_v, f1_v, wf_v, f2_v;
    private short w_id, f1_id, wf_id, f2_id;
    private short[] p_id, p_v, f_id, f_v;
    private double pathWidthLower;
    private PlotWorld plotworld;

    public XPopulator(PlotWorld plotworld) {
        this.plotworld = plotworld;
        this.plotsize = plotworld.PLOT_WIDTH;
        this.pathsize = plotworld.ROAD_WIDTH;
        this.plotheight = plotworld.PLOT_HEIGHT;
        this.wallheight = plotworld.WALL_HEIGHT;
        this.roadheight = plotworld.ROAD_HEIGHT;
        this.size = this.pathsize + this.plotsize;
        // WALL
        short[] result_w = getBlock(plotworld.WALL_BLOCK);
        this.w_id = result_w[0];
        this.w_v = (byte) result_w[1];

        // WALL FILLING
        short[] result_wf = getBlock(plotworld.WALL_FILLING);
        this.wf_id = result_wf[0];
        this.wf_v = (byte) result_wf[1];

        // ROAD
        short[] result_f1 = getBlock(plotworld.ROAD_BLOCK);
        this.f1_id = result_f1[0];
        this.f1_v = (byte) result_f1[1];
        //

        // Floor 2
        short[] result_f2 = getBlock(plotworld.ROAD_STRIPES);
        this.f2_id = result_f2[0];
        this.f2_v = (byte) result_f2[1];
        //

        this.p_id = new short[plotworld.MAIN_BLOCK.length];
        this.p_v = new short[plotworld.MAIN_BLOCK.length];
        this.f_id = new short[plotworld.TOP_BLOCK.length];
        this.f_v = new short[plotworld.TOP_BLOCK.length];
        for (int i = 0; i < plotworld.MAIN_BLOCK.length; i++) {
            short[] result = getBlock(plotworld.MAIN_BLOCK[i]);
            this.p_id[i] = result[0];
            this.p_v[i] = result[1];
        }
        for (int i = 0; i < plotworld.TOP_BLOCK.length; i++) {
            short[] result = getBlock(plotworld.TOP_BLOCK[i]);
            this.f_id[i] = result[0];
            this.f_v[i] = result[1];
        }
        if ((pathsize % 2) == 0) {
            pathWidthLower = Math.floor(this.pathsize / 2)-1;
        }
        else {
            this.pathWidthLower = Math.floor(this.pathsize / 2);
        }
    }

    @Override
    public void populate(World w, Random r, Chunk c) {
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
        double absX = ((((cx * 16) + 16) - this.pathWidthLower - 1) + (8 * this.size)), absZ = ((((cz * 16) + 16) - this.pathWidthLower - 1) + (8 * this.size));
        int plotMinX = (int) (((absX) % this.size));
        int plotMinZ = (int) (((absZ) % this.size));
        int roadStartX = (plotMinX + this.pathsize);
        int roadStartZ = (plotMinZ + this.pathsize);
        if (roadStartX >= this.size) {
            roadStartX -= this.size;
        }
        if (roadStartZ >= this.size) {
            roadStartZ -= this.size;
        }

        // ROADS

        if (((plotMinZ + 1) <= 16) || ((roadStartZ <= 16) && (roadStartZ > 0))) {
            int start = Math.max((16 - plotMinZ - this.pathsize) + 1, (16 - roadStartZ) + 1);
            int end = Math.min(16 - plotMinZ - 1, (16 - roadStartZ) + this.pathsize);
            if ((start >= 0) && (start <= 16) && (end < 0)) {
                end = 16;
            }
            setCuboidRegion(0, 16, 1, this.roadheight + 1, Math.max(start, 0), Math.min(16, end), this.f1_id, this.f1_v, w);
        }
        if (((plotMinX + 1) <= 16) || ((roadStartX <= 16) && (roadStartX > 0))) {
            int start = Math.max((16 - plotMinX - this.pathsize) + 1, (16 - roadStartX) + 1);
            int end = Math.min(16 - plotMinX - 1, (16 - roadStartX) + this.pathsize);
            if ((start >= 0) && (start <= 16) && (end < 0)) {
                end = 16;
            }
            setCuboidRegion(Math.max(start, 0), Math.min(16, end), 1, this.roadheight + 1, 0, 16, this.f1_id, this.f1_v, w);
        }

        // STRIPES
        if ((this.pathsize > 4) && this.plotworld.ROAD_STRIPES_ENABLED) {
            if ((plotMinZ + 2) <= 16) {
                int value = (plotMinZ + 2);
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
                setCuboidRegion(0, end, this.roadheight, this.roadheight + 1, 16 - value, (16 - value) + 1, this.f2_id, this.f2_v, w); //
                setCuboidRegion(start, 16, this.roadheight, this.roadheight + 1, 16 - value, (16 - value) + 1, this.f2_id, this.f2_v, w); //
            }
            if ((plotMinX + 2) <= 16) {
                int value = (plotMinX + 2);
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
                setCuboidRegion(16 - value, (16 - value) + 1, this.roadheight, this.roadheight + 1, 0, end, this.f2_id, this.f2_v, w); //
                setCuboidRegion(16 - value, (16 - value) + 1, this.roadheight, this.roadheight + 1, start, 16, this.f2_id, this.f2_v, w); //
            }
            if ((roadStartZ <= 16) && (roadStartZ > 1)) {
                int val = roadStartZ;
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
                setCuboidRegion(0, end, this.roadheight, this.roadheight + 1, (16 - val) + 1, (16 - val) + 2, this.f2_id, this.f2_v, w);
                setCuboidRegion(start, 16, this.roadheight, this.roadheight + 1, (16 - val) + 1, (16 - val) + 2, this.f2_id, this.f2_v, w);
            }
            if ((roadStartX <= 16) && (roadStartX > 1)) {
                int val = roadStartX;
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
                setCuboidRegion((16 - val) + 1, (16 - val) + 2, this.roadheight, this.roadheight + 1, 0, end, this.f2_id, this.f2_v, w); //
                setCuboidRegion((16 - val) + 1, (16 - val) + 2, this.roadheight, this.roadheight + 1, start, 16, this.f2_id, this.f2_v, w); //
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
                setCuboidRegion(0, end, 1, this.wallheight + 1, 16 - plotMinZ - 1, 16 - plotMinZ, this.wf_id, this.wf_v, w);
                setCuboidRegion(0, end, this.wallheight + 1, this.wallheight + 2, 16 - plotMinZ - 1, 16 - plotMinZ, this.w_id, this.w_v, w);
                setCuboidRegion(start, 16, 1, this.wallheight + 1, 16 - plotMinZ - 1, 16 - plotMinZ, this.wf_id, this.wf_v, w);
                setCuboidRegion(start, 16, this.wallheight + 1, this.wallheight + 2, 16 - plotMinZ - 1, 16 - plotMinZ, this.w_id, this.w_v, w);
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
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, 1, this.wallheight + 1, 0, end, this.wf_id, this.wf_v, w);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, this.wallheight + 1, this.wallheight + 2, 0, end, this.w_id, this.w_v, w);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, 1, this.wallheight + 1, start, 16, this.wf_id, this.wf_v, w);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, this.wallheight + 1, this.wallheight + 2, start, 16, this.w_id, this.w_v, w);
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
                setCuboidRegion(0, end, 1, this.wallheight + 1, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wf_id, this.wf_v, w);
                setCuboidRegion(0, end, this.wallheight + 1, this.wallheight + 2, 16 - roadStartZ, (16 - roadStartZ) + 1, this.w_id, this.w_v, w);
                setCuboidRegion(start, 16, 1, this.wallheight + 1, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wf_id, this.wf_v, w);
                setCuboidRegion(start, 16, this.wallheight + 1, this.wallheight + 2, 16 - roadStartZ, (16 - roadStartZ) + 1, this.w_id, this.w_v, w);
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
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, 1, this.wallheight + 1, 0, end, this.wf_id, this.wf_v, w);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, this.wallheight + 1, this.roadheight + 2, 0, end, this.w_id, this.w_v, w);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, 1, this.wallheight + 1, start, 16, this.wf_id, this.wf_v, w);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, this.wallheight + 1, this.wallheight + 2, start, 16, this.w_id, this.w_v, w);
            }
        }

        // PLOT

        if (this.plotsize > 16) {
            if (roadStartX <= 16) {
                if (roadStartZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 0, 16 - roadStartZ, this.p_id, this.p_v, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.f_id, this.f_v, w);
                }
                if (plotMinZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 16 - plotMinZ, 16, this.p_id, this.p_v, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.f_id, this.f_v, w);
                }
            } else {
                if (roadStartZ <= 16) {
                    if (plotMinX > 16) {
                        setCuboidRegion(0, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.p_id, this.p_v, w);
                        setCuboidRegion(0, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.f_id, this.f_v, w);
                    }
                }
            }
            if (plotMinX <= 16) {
                if (plotMinZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 16 - plotMinZ, 16, this.p_id, this.p_v, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.f_id, this.f_v, w);
                } else {
                    int z = 16 - roadStartZ;
                    if (z < 0) {
                        z = 16;
                    }
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, z, this.p_id, this.p_v, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, z, this.f_id, this.f_v, w);
                }
                if (roadStartZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.p_id, this.p_v, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.f_id, this.f_v, w);
                } else {
                    if (roadStartX <= 16) {
                        if (plotMinZ > 16) {
                            int x = 16 - roadStartX;
                            if (x < 0) {
                                x = 16;
                            }
                            setCuboidRegion(0, x, 1, this.plotheight, 0, 16, this.p_id, this.p_v, w);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, 16, this.f_id, this.f_v, w);
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
                        setCuboidRegion(0, x, 1, this.plotheight, 16 - plotMinZ, 16, this.p_id, this.p_v, w);
                        setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.f_id, this.f_v, w);
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
                            setCuboidRegion(0, x, 1, this.plotheight, 0, z, this.p_id, this.p_v, w);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, z, this.f_id, this.f_v, w);
                        } else {
                            setCuboidRegion(0, x, 1, this.plotheight, 0, z, this.p_id, this.p_v, w);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, z, this.f_id, this.f_v, w);
                        }
                    }
                }
            }
        } else {
            if (roadStartX <= 16) {
                if (roadStartZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 0, 16 - roadStartZ, this.p_id, this.p_v, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.f_id, this.f_v, w);
                }
                if (plotMinZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 16 - plotMinZ, 16, this.p_id, this.p_v, w);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.f_id, this.f_v, w);
                }
            }
            if (plotMinX <= 16) {
                if (plotMinZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 16 - plotMinZ, 16, this.p_id, this.p_v, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.f_id, this.f_v, w);
                }
                if (roadStartZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.p_id, this.p_v, w);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.f_id, this.f_v, w);
                }
            }
        }
    }

    private void setBlock(World w, int x, int y, int z, short id, byte val) {
        w.getBlockAt(this.X + x, y, this.Z + z).setData(val, false);
    }

}
