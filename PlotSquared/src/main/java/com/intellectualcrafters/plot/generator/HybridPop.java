package com.intellectualcrafters.plot.generator;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;

/**
 * @author Citymonstret
 */
public class HybridPop extends BlockPopulator {

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
    private final HybridPlotWorld plotworld;
    final short pathWidthLower;
    final short pathWidthUpper;
    Biome biome;
    private int X;
    private int Z;
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
        
        if ((count1 > 0 && doFloor) || (count2 > 0 && doFilling)) {
            doState = true;
        }

        this.wallheight = this.plotworld.WALL_HEIGHT;
        this.roadheight = this.plotworld.ROAD_HEIGHT;
        this.plotheight = this.plotworld.PLOT_HEIGHT;

        if ((this.pathsize % 2) == 0) {
            this.pathWidthLower = (short) (Math.floor(this.pathsize / 2) - 1);
        } else {
            this.pathWidthLower = (short) (Math.floor(this.pathsize / 2));
        }
        
        this.pathWidthUpper = (short) (this.pathWidthLower + this.plotsize + 1);
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
    public void populate(final World w, final Random r, final Chunk c) {
        int cx = c.getX(), cz = c.getZ();
        
        if (doState) {
            final int prime = 13;
            int h = 1;
            h = (prime * h) + cx;
            h = (prime * h) + cz;
            this.state = h;
        }
        
        this.X = cx << 4;
        this.Z = cz << 4;
        
        HybridPlotManager manager = (HybridPlotManager) PlotMain.getPlotManager(w);
        RegionWrapper plot = ChunkManager.CURRENT_PLOT_CLEAR;
        if (plot != null) {
            short sx = (short) ((X) % this.size);
            short sz = (short) ((Z) % this.size);
            
            if (sx < 0) {
                sx += this.size;
            }
            
            if (sz < 0) {
                sz += this.size;
            }
            
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    if (isIn(plot, X + x, Z + z)) {
                        if (doFilling) {
                            for (short y = 1; y < this.plotheight; y++) {
                                setBlock(w, x, y, z, this.filling);
                            }
                        }
                        if (doFloor) {
                            setBlock(w, x,(short) this.plotheight, z, this.plotfloors);
                        }
                    }
                    else {
                        ChunkLoc loc = new ChunkLoc(X + x, Z + z);
                        HashMap<Short, Byte> data = ChunkManager.GENERATE_DATA.get(loc);
                        if (data != null) {
                            for (short y : data.keySet()) {
                                setBlock(w, x, y, z, data.get(y).byteValue());
                            }
                        }
                    }
                }
            }
            return;
        }
        
        short sx = (short) ((X) % this.size);
        short sz = (short) ((Z) % this.size);
        
        if (sx < 0) {
            sx += this.size;
        }
        
        if (sz < 0) {
            sz += this.size;
        }
        
        // Setting biomes
        for (short x = 0; x < 16; x++) {
            for (short z = 0; z < 16; z++) {
                
                short absX = (short) ((sx + x) % this.size);
                short absZ = (short) ((sz + z) % this.size);
                
                boolean gx = absX > pathWidthLower;
                boolean gz = absZ > pathWidthLower;
                
                boolean lx = absX < pathWidthUpper;
                boolean lz = absZ < pathWidthUpper;
                
                // inside plot
                if (gx && gz && lx && lz) {
                    if (doFilling) {
                        for (short y = 1; y < this.plotheight; y++) {
                            setBlock(w, x, y, z, this.filling);
                        }
                    }
                    if (doFloor) {
                        setBlock(w, x, (short) this.plotheight, z, this.plotfloors);
                    }
                } else {
                    // wall
                    if ((absX >= pathWidthLower && absX <= pathWidthUpper && absZ >= pathWidthLower && absZ <= pathWidthUpper))
                    {
                        if (this.wallfilling != 0) {
                            for (short y = 1; y <= this.wallheight; y++) {
                                setBlock(w, x, y, z, this.wallfilling);
                            }
                        }
                        if (this.wall != 0 && !this.plotworld.ROAD_SCHEMATIC_ENABLED) {
                            setBlock(w, x, (short) (this.wallheight + 1), z, this.wall);
                        }
                    }
                    // road
                    else {
                        if (this.roadblock != 0) {
                            for (short y = 1; y <= this.roadheight; y++) {
                                setBlock(w, x, y, z, this.roadblock);
                            }
                        }
                    }
                    if (this.plotworld.ROAD_SCHEMATIC_ENABLED) {
                        ChunkLoc loc = new ChunkLoc(absX, absZ);
                        HashMap<Short, Byte> blocks = this.plotworld.G_SCH_DATA.get(loc);
                        if (blocks != null) {
                            for (short y : blocks.keySet()) {
                                setBlock(w, x, (short) (this.roadheight + y), z, blocks.get(y));        
                            }
                        }
                    }
                }
            }
        }
    }

    private void setBlock(final World w, short x, short y, short z, byte[] blkids) {
        if (blkids.length == 1) {
            setBlock(w, x, y, z, blkids[0]);
        }
        else {
            final int i = random(blkids.length);
            setBlock(w, x, y, z, blkids[i]);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void setBlock(final World w, final short x, final short y, final short z, final byte val) {
        w.getBlockAt(this.X + x, y, this.Z + z).setData(val, false);
    }
    
    public boolean isIn(RegionWrapper plot, int x, int z) {
        return (x >= plot.minX && x <= plot.maxX && z >= plot.minZ && z <= plot.maxZ);
    }

}
