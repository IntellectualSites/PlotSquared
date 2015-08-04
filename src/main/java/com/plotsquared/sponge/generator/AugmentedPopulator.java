package com.plotsquared.sponge.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.util.SpongeUtil;

public class AugmentedPopulator implements Populator {

    public final PlotWorld plotworld;
    public final PlotManager manager;
    public final SpongePlotGenerator generator;
    public final GeneratorPopulator populator;
    public final PlotCluster cluster;
    public final Random r = new Random();
    public final boolean p;
    public final boolean b;
    public final boolean o;
    private final int bx;
    private final int bz;
    private final int tx;
    private final int tz;

    public AugmentedPopulator(String worldname, WorldGenerator gen, final SpongePlotGenerator generator, final PlotCluster cluster, final boolean p, final boolean b) {
//        MainUtil.initCache();
        // Initialize any chach that's needed
        this.cluster = cluster;
        this.generator = generator;
        this.populator = generator.getBaseGeneratorPopulator();
        this.plotworld = generator.getNewPlotWorld(worldname);
        this.manager = generator.getPlotManager();
        this.p = p;
        this.b = b;
        this.o = (this.plotworld.TERRAIN == 1) || (this.plotworld.TERRAIN == 2);
        if (cluster != null) {
            final Location bl = this.manager.getPlotBottomLocAbs(this.plotworld, cluster.getP1());
            final Location tl = this.manager.getPlotTopLocAbs(this.plotworld, cluster.getP2()).add(1, 0, 1);
            this.bx = bl.getX();
            this.bz = bl.getZ();
            this.tx = tl.getX();
            this.tz = tl.getZ();
        } else {
            this.bx = Integer.MIN_VALUE;
            this.bz = Integer.MIN_VALUE;
            this.tx = Integer.MAX_VALUE;
            this.tz = Integer.MAX_VALUE;
        }
        // Add the populator
        List<Populator> populators = gen.getPopulators();
        if (this.o) {
            populators.add(0, this);
        } else {
            populators.add(this);
        }
    }
    
    public static void removePopulator(final String worldname, final PlotCluster cluster) {
        final World world = SpongeUtil.getWorld(worldname);
        List<Populator> populators = world.getWorldGenerator().getPopulators();
        for (final Iterator<Populator> iterator = populators.iterator(); iterator.hasNext();) {
            final Populator populator = iterator.next();
            if (populator instanceof AugmentedPopulator) {
                if (((AugmentedPopulator) populator).cluster.equals(cluster)) {
                    iterator.remove();
                }
            }
        }
    }
    
    @Override
    public void populate(final Chunk chunk, Random r_unused) {
        Vector3i min = chunk.getBlockMin();
        final World worldObj = chunk.getWorld();
        final String world = worldObj.getName();
        final int cx = min.getX() >> 4;
        final int cz = min.getZ() >> 4;
        final int bx = cx << 4;
        final int bz = cz << 4;
        final int tx = bx + 15;
        final int tz = bz + 15;
        final boolean inX1 = ((bx >= this.bx) && (bx <= this.tx));
        final boolean inX2 = ((tx >= this.bx) && (tx <= this.tx));
        final boolean inZ1 = ((bz >= this.bz) && (bz <= this.tz));
        final boolean inZ2 = ((tz >= this.bz) && (tz <= this.tz));
        final boolean inX = inX1 || inX2;
        final boolean inZ = inZ1 || inZ2;
        if (!inX || !inZ) {
            return;
        }
        if (this.plotworld.TERRAIN == 3) {
            int X = min.getX();
            int Z = min.getZ();
            if (ChunkManager.FORCE_PASTE) {
                for (short x = 0; x < 16; x++) {
                    for (short z = 0; z < 16; z++) {
                        final PlotLoc loc = new PlotLoc((short) (X + x), (short) (Z + z));
                        final HashMap<Short, Short> blocks = ChunkManager.GENERATE_BLOCKS.get(loc);
                        HashMap<Short, Byte> datas = ChunkManager.GENERATE_DATA.get(loc);
                        for (final Entry<Short, Short> entry : blocks.entrySet()) {
                            int y = entry.getKey();
                            if (datas != null) {
                                SetBlockQueue.setBlock(world, x, y, z, new PlotBlock(blocks.get(y), datas.get(y)));
                            }
                            else {
                                SetBlockQueue.setBlock(world, x, y, z, blocks.get(y));
                            }
                        }
                    }
                }
                return;
            }
            if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                PlotLoc loc;
                for (Entry<PlotLoc, HashMap<Short, Byte>> entry : ChunkManager.GENERATE_DATA.entrySet()) {
                    HashMap<Short, Byte> datas = ChunkManager.GENERATE_DATA.get(entry.getKey());
                    for (Entry<Short, Byte> entry2 : entry.getValue().entrySet()) {
                        Short y = entry2.getKey();
                        byte data;
                        if (datas != null) {
                            data = datas.get(y);
                        }
                        else {
                            data = 0;
                        }
                        loc = entry.getKey();
                        int xx = loc.x - X;
                        int zz = loc.z - Z;
                        if (xx >= 0 && xx < 16) {
                            if (zz >= 0 && zz < 16) {
                                SetBlockQueue.setBlock(world, xx, y, zz, new PlotBlock(entry2.getValue(), data));
                            }
                        }
                    }
                }
            }
            return;
        }
        final boolean check;
        check = !inX1 || !inX2 || !inZ1 || !inZ2;
        if (this.plotworld.TERRAIN > 1) {
            final PlotId plot1 = this.manager.getPlotIdAbs(this.plotworld, bx, 0, bz);
            final PlotId plot2 = this.manager.getPlotIdAbs(this.plotworld, tx, 0, tz);
            if ((plot1 != null) && (plot2 != null) && plot1.equals(plot2)) {
                return;
            }
        }
        if (this.o) {
            populateBlocks(worldObj, chunk, cx, cz, bx, bz, check);
        } else {
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    // TODO populate biome
                }
            }, 20 + r.nextInt(10));
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    populateBlocks(worldObj, chunk, cx, cz, bx, bz, check);
                }
            }, 40 + r.nextInt(40));
        }
    }
    
    private void populateBlocks(final World world, final Chunk chunk, final int X, final int Z, final int x, final int z, final boolean check) {
        final String worldname = world.getName();
        MutableBlockVolume blocks = new MutableBlockVolume() {
            @Override
            public void setBlock(int x, int y, int z, BlockState t) {
                if (check && (((z) < bz) || ((z) > tz) || ((x) < bx) || ((x) > tx))) {
                    return;
                }
                if (p) {
                    if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                        if (ChunkManager.CURRENT_PLOT_CLEAR.isIn(x, z)) {
                            return;
                        }
                    } else if (manager.getPlotIdAbs(plotworld, x, 0, z) != null) {
                        return;
                    }
                }
                PlotBlock block = SpongeMain.THIS.getPlotBlock(t);
                if (block != null) {
                    SetBlockQueue.setBlock(worldname, x, y, z, block);
                }
            }
            
            @Override
            public void setBlock(Vector3i v, BlockState t) {
                setBlock(v.getX(), v.getY(), v.getZ(), t);
            }
            
            @Override
            public BlockType getBlockType(int x, int y, int z) {
                return world.getBlockType(x, y, z);
            }
            
            @Override
            public BlockType getBlockType(Vector3i v) {
                return getBlockType(v.getX(), v.getY(), v.getZ());
            }
            
            @Override
            public Vector3i getBlockSize() {
                return chunk.getBlockSize();
            }
            
            @Override
            public Vector3i getBlockMin() {
                return chunk.getBlockMin();
            }
            
            @Override
            public Vector3i getBlockMax() {
                return chunk.getBlockMax();
            }
            
            @Override
            public BlockState getBlock(int x, int y, int z) {
                return world.getBlock(x, y, z);
            }
            
            @Override
            public BlockState getBlock(Vector3i v) {
                return getBlock(v.getX(), v.getY(), v.getZ());
            }
            
            @Override
            public boolean containsBlock(int x, int y, int z) {
                return ((x) >= bz) && ((z) <= tz) && ((x) >= bx) && ((z) <= tx);
            }
            
            @Override
            public boolean containsBlock(Vector3i v) {
                return containsBlock(v.getX(), v.getY(), v.getZ());
            }
            
            @Override
            public void setBlockType(int x, int y, int z, BlockType t) {
                if (check && (((z) < bz) || ((z) > tz) || ((x) < bx) || ((x) > tx))) {
                    return;
                }
                if (p) {
                    if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                        if (ChunkManager.CURRENT_PLOT_CLEAR.isIn(x, z)) {
                            return;
                        }
                    } else if (manager.getPlotIdAbs(plotworld, x, 0, z) != null) {
                        return;
                    }
                }
                PlotBlock block = SpongeMain.THIS.getPlotBlock(t.getDefaultState());
                if (block != null) {
                    SetBlockQueue.setBlock(worldname, x, y, z, block);
                }
            }
            
            @Override
            public void setBlockType(Vector3i v, BlockType t) {
                setBlockType(v.getX(), v.getY(), v.getZ(), t);
            }
        };
        this.populator.populate(world, blocks , null);
    }
    
}
