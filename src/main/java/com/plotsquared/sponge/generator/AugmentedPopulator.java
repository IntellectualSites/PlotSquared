package com.plotsquared.sponge.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
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
    
    public AugmentedPopulator(final String worldname, final WorldGenerator gen, final SpongePlotGenerator generator, final PlotCluster cluster, final boolean p, final boolean b) {
        //        MainUtil.initCache();
        // Initialize any chach that's needed
        this.cluster = cluster;
        this.generator = generator;
        populator = generator.getBaseGeneratorPopulator();
        plotworld = generator.getNewPlotWorld(worldname);
        manager = generator.getPlotManager();
        this.p = p;
        this.b = b;
        o = (plotworld.TERRAIN == 1) || (plotworld.TERRAIN == 2);
        if (cluster != null) {
            final Location bl = manager.getPlotBottomLocAbs(plotworld, cluster.getP1()).subtract(1, 0, 1);
            final Location tl = manager.getPlotTopLocAbs(plotworld, cluster.getP2()).add(1, 0, 1);
            bx = bl.getX();
            bz = bl.getZ();
            tx = tl.getX();
            tz = tl.getZ();
        } else {
            bx = Integer.MIN_VALUE;
            bz = Integer.MIN_VALUE;
            tx = Integer.MAX_VALUE;
            tz = Integer.MAX_VALUE;
        }
        // Add the populator
        final List<Populator> populators = gen.getPopulators();
        if (o) {
            populators.add(0, this);
        } else {
            populators.add(this);
        }
    }
    
    public static void removePopulator(final String worldname, final PlotCluster cluster) {
        final World world = SpongeUtil.getWorld(worldname);
        final List<Populator> populators = world.getWorldGenerator().getPopulators();
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
    public void populate(final Chunk chunk, final Random r_unused) {
        final Vector3i min = chunk.getBlockMin();
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
        if (plotworld.TERRAIN == 3) {
            final int X = min.getX();
            final int Z = min.getZ();
            if (ChunkManager.FORCE_PASTE) {
                for (short x = 0; x < 16; x++) {
                    for (short z = 0; z < 16; z++) {
                        final PlotLoc loc = new PlotLoc((short) (X + x), (short) (Z + z));
                        final HashMap<Short, Short> blocks = ChunkManager.GENERATE_BLOCKS.get(loc);
                        final HashMap<Short, Byte> datas = ChunkManager.GENERATE_DATA.get(loc);
                        for (final Entry<Short, Short> entry : blocks.entrySet()) {
                            final int y = entry.getKey();
                            if (datas != null) {
                                SetBlockQueue.setBlock(world, x, y, z, new PlotBlock(blocks.get(y), datas.get(y)));
                            } else {
                                SetBlockQueue.setBlock(world, x, y, z, blocks.get(y));
                            }
                        }
                    }
                }
                return;
            }
            if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                PlotLoc loc;
                for (final Entry<PlotLoc, HashMap<Short, Byte>> entry : ChunkManager.GENERATE_DATA.entrySet()) {
                    final HashMap<Short, Byte> datas = ChunkManager.GENERATE_DATA.get(entry.getKey());
                    for (final Entry<Short, Byte> entry2 : entry.getValue().entrySet()) {
                        final Short y = entry2.getKey();
                        byte data;
                        if (datas != null) {
                            data = datas.get(y);
                        } else {
                            data = 0;
                        }
                        loc = entry.getKey();
                        final int xx = loc.x - X;
                        final int zz = loc.z - Z;
                        if ((xx >= 0) && (xx < 16)) {
                            if ((zz >= 0) && (zz < 16)) {
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
        if (plotworld.TERRAIN > 1) {
            final PlotId plot1 = manager.getPlotIdAbs(plotworld, bx, 0, bz);
            final PlotId plot2 = manager.getPlotIdAbs(plotworld, tx, 0, tz);
            if ((plot1 != null) && (plot2 != null) && plot1.equals(plot2)) {
                return;
            }
        }
        if (o) {
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
        final MutableBlockVolume blocks = new MutableBlockVolume() {
            @Override
            public void setBlock(final int x, final int y, final int z, final BlockState t) {
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
                final PlotBlock block = SpongeMain.THIS.getPlotBlock(t);
                if (block != null) {
                    SetBlockQueue.setBlock(worldname, x, y, z, block);
                }
            }
            
            @Override
            public void setBlock(final Vector3i v, final BlockState t) {
                setBlock(v.getX(), v.getY(), v.getZ(), t);
            }
            
            @Override
            public BlockType getBlockType(final int x, final int y, final int z) {
                return world.getBlockType(x, y, z);
            }
            
            @Override
            public BlockType getBlockType(final Vector3i v) {
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
            public BlockState getBlock(final int x, final int y, final int z) {
                return world.getBlock(x, y, z);
            }
            
            @Override
            public BlockState getBlock(final Vector3i v) {
                return getBlock(v.getX(), v.getY(), v.getZ());
            }
            
            @Override
            public boolean containsBlock(final int x, final int y, final int z) {
                return ((x) >= bz) && ((z) <= tz) && ((x) >= bx) && ((z) <= tx);
            }
            
            @Override
            public boolean containsBlock(final Vector3i v) {
                return containsBlock(v.getX(), v.getY(), v.getZ());
            }
            
            @Override
            public void setBlockType(final int x, final int y, final int z, final BlockType t) {
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
                final PlotBlock block = SpongeMain.THIS.getPlotBlock(t.getDefaultState());
                if (block != null) {
                    SetBlockQueue.setBlock(worldname, x, y, z, block);
                }
            }
            
            @Override
            public void setBlockType(final Vector3i v, final BlockType t) {
                setBlockType(v.getX(), v.getY(), v.getZ(), t);
            }
            
            @Override
            public MutableBlockVolume getBlockCopy() {
                // TODO Auto-generated method stub
                return this;
            }
            
            @Override
            public MutableBlockVolume getBlockCopy(final StorageType arg0) {
                // TODO Auto-generated method stub
                return this;
            }
            
            @Override
            public ImmutableBlockVolume getImmutableBlockCopy() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public UnmodifiableBlockVolume getUnmodifiableBlockView() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public MutableBlockVolume getBlockView(final DiscreteTransform3 arg0) {
                // TODO Auto-generated method stub
                return this;
            }
            
            @Override
            public MutableBlockVolume getBlockView(final Vector3i arg0, final Vector3i arg1) {
                // TODO Auto-generated method stub
                return this;
            }
            
            @Override
            public MutableBlockVolume getRelativeBlockView() {
                // TODO Auto-generated method stub
                return this;
            }
        };
        populator.populate(world, blocks, null);
    }
    
}
