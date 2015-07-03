package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.BlockManager;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Citymonstret
 */
public class HybridPop extends PlotPopulator {
    /*
     * Sorry, this isn't well documented at the moment.
     * We advise you to take a look at a world generation tutorial for
     * information about how a BlockPopulator works.
     */
    final short plotsize;
    final short pathsize;
    final byte wall;
    final byte wallfilling;
    final byte roadblock;
    final int size;
    final int roadheight;
    final int wallheight;
    final int plotheight;
    final byte[] plotfloors;
    final byte[] filling;
    final short pathWidthLower;
    final short pathWidthUpper;
    private final HybridPlotWorld plotworld;
    Biome biome;
    private long state;
    private boolean doFilling = false;
    private boolean doFloor = false;
    private boolean doState = false;

    public HybridPop(final PlotWorld pw) {
        this.plotworld = (HybridPlotWorld) pw;
        // save configuration
        this.plotsize = (short) this.plotworld.PLOT_WIDTH;
        this.pathsize = (short) this.plotworld.ROAD_WIDTH;
        this.roadblock = this.plotworld.ROAD_BLOCK.data;
        this.wallfilling = this.plotworld.WALL_FILLING.data;
        this.size = this.pathsize + this.plotsize;
        this.wall = this.plotworld.WALL_BLOCK.data;
        int count1 = 0;
        int count2 = 0;
        this.plotfloors = new byte[this.plotworld.TOP_BLOCK.length];
        for (int i = 0; i < this.plotworld.TOP_BLOCK.length; i++) {
            count1++;
            this.plotfloors[i] = this.plotworld.TOP_BLOCK[i].data;
            if (this.plotworld.TOP_BLOCK[i].data != 0) {
                this.doFloor = true;
            }
        }
        this.filling = new byte[this.plotworld.MAIN_BLOCK.length];
        for (int i = 0; i < this.plotworld.MAIN_BLOCK.length; i++) {
            count2++;
            this.filling[i] = this.plotworld.MAIN_BLOCK[i].data;
            if (this.plotworld.MAIN_BLOCK[i].data != 0) {
                this.doFilling = true;
            }
        }
        if (((count1 > 0) && this.doFloor) || ((count2 > 0) && this.doFilling)) {
            this.doState = true;
        }
        this.wallheight = this.plotworld.WALL_HEIGHT;
        this.roadheight = this.plotworld.ROAD_HEIGHT;
        this.plotheight = this.plotworld.PLOT_HEIGHT;
        if (this.pathsize == 0) {
            this.pathWidthLower = (short) -1;
            this.pathWidthUpper = (short) (this.plotsize + 1);
        }
        else {
            if ((this.pathsize % 2) == 0) {
                this.pathWidthLower = (short) (Math.floor(this.pathsize / 2) - 1);
            } else {
                this.pathWidthLower = (short) (Math.floor(this.pathsize / 2));
            }
            this.pathWidthUpper = (short) (this.pathWidthLower + this.plotsize + 1);
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

    @Override
    public void populate(World world, RegionWrapper requiredRegion, PseudoRandom random, int cx, int cz) {
        PS.get().getPlotManager(world.getName());

        int sx = (short) ((this.X) % this.size);
        int sz = (short) ((this.Z) % this.size);
        if (sx < 0) {
            sx += this.size;
        }
        if (sz < 0) {
            sz += this.size;
        }

        if (requiredRegion != null) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    if (contains(requiredRegion, x, z)) {
                        if (this.doFilling) {
                            for (short y = 1; y < this.plotheight; y++) {
                                setBlock(x, y, z, this.filling);
                            }
                        }
                        if (this.doFloor) {
                            setBlock(x, (short) this.plotheight, z, this.plotfloors);
                        }
                        if (this.plotworld.PLOT_SCHEMATIC) {
                            final int absX = ((sx + x) % this.size);
                            final int absZ = ((sz + z) % this.size);
                            final PlotLoc loc = new PlotLoc(absX, absZ);
                            final HashMap<Short, Byte> blocks = this.plotworld.G_SCH_DATA.get(loc);
                            if (blocks != null) {
                                for (final short y : blocks.keySet()) {
                                    setBlockAbs(x, (short) (this.plotheight + y), z, blocks.get(y));
                                }
                            }
                            if (this.plotworld.G_SCH_STATE != null) {
                                HashSet<PlotItem> states = this.plotworld.G_SCH_STATE.get(loc);
                                if (states != null) {
                                    for (PlotItem items : states) {
                                        BlockManager.manager.addItems(this.plotworld.worldname, items);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return;
        }
        
        for (short x = 0; x < 16; x++) {
            for (short z = 0; z < 16; z++) {
                final int absX = ((sx + x) % this.size);
                final int absZ = ((sz + z) % this.size);
                final boolean gx = absX > this.pathWidthLower;
                final boolean gz = absZ > this.pathWidthLower;
                final boolean lx = absX < this.pathWidthUpper;
                final boolean lz = absZ < this.pathWidthUpper;
                // inside plot
                if (gx && gz && lx && lz) {
                    for (short y = 1; y < this.plotheight; y++) {
                        setBlock(x, y, z, this.filling);
                    }
                    setBlock(x, (short) this.plotheight, z, this.plotfloors);
                    if (this.plotworld.PLOT_SCHEMATIC) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Byte> blocks = this.plotworld.G_SCH_DATA.get(loc);
                        if (blocks != null) {
                            for (final short y : blocks.keySet()) {
                                setBlockAbs(x, (short) (this.plotheight + y), z, blocks.get(y));
                            }
                        }
                        if (this.plotworld.G_SCH_STATE != null) {
                            HashSet<PlotItem> states = this.plotworld.G_SCH_STATE.get(loc);
                            if (states != null) {
                                for (PlotItem items : states) {
                                    items.x = this.X + x;
                                    items.z = this.Z + z;
                                    BlockManager.manager.addItems(this.plotworld.worldname, items);
                                }
                            }
                        }
                    }
                } else if (pathsize != 0) {
                    // wall
                    if (((absX >= this.pathWidthLower) && (absX <= this.pathWidthUpper) && (absZ >= this.pathWidthLower) && (absZ <= this.pathWidthUpper))) {
                        for (short y = 1; y <= this.wallheight; y++) {
                            setBlock(x, y, z, this.wallfilling);
                        }
                        if (!this.plotworld.ROAD_SCHEMATIC_ENABLED) {
                            setBlock(x, this.wallheight + 1, z, this.wall);
                        }
                    }
                    // road
                    else {
                        for (short y = 1; y <= this.roadheight; y++) {
                            setBlock(x, y, z, this.roadblock);
                        }
                    }
                    if (this.plotworld.ROAD_SCHEMATIC_ENABLED) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Byte> blocks = this.plotworld.G_SCH_DATA.get(loc);
                        if (blocks != null) {
                            for (final short y : blocks.keySet()) {
                                setBlockAbs(x, (short) (this.roadheight + y), z, blocks.get(y));
                            }
                        }
                    }
                }
            }
        }
    }

    private void setBlock(final short x, final short y, final short z, final byte[] blkids) {
        if (blkids.length == 1) {
            setBlock(x, y, z, blkids[0]);
        } else {
            final int i = random(blkids.length);
            setBlock(x, y, z, blkids[i]);
        }
    }
}
