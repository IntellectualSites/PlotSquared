package com.intellectualcrafters.plot.generator;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.BlockWrapper;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.AChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.ChunkManager;
import com.intellectualcrafters.plot.util.bukkit.SetBlockManager;

public class AugmentedPopulator extends BlockPopulator {
    public final PlotWorld plotworld;
    public final PlotManager manager;
    public final PlotGenerator generator;
    public final PlotCluster cluster;
    public final Random r = new Random();
    public final boolean p;
    public final boolean b;
    public final boolean o;
    private final int bx;
    private final int bz;
    private final int tx;
    private final int tz;
    
    public BlockWrapper get(final int X, final int Z, final int i, final int j, final short[][] r, final boolean c) {
        final int y = (i << 4) + (j >> 8);
        final int a = (j - ((y & 0xF) << 8));
        final int z = (a >> 4);
        final int x = a - (z << 4);
        if (r[i] == null) {
            return (c && (((Z + z) < this.bz) || ((Z + z) > this.tz) || ((X + x) < this.bx) || ((X + x) > this.tx))) ? null : new BlockWrapper(x, y, z, (short) 0, (byte) 0);
        } else {
            return (c && (((Z + z) < this.bz) || ((Z + z) > this.tz) || ((X + x) < this.bx) || ((X + x) > this.tx))) ? null : new BlockWrapper(x, y, z, r[i][j], (byte) 0);
        }
    }
    
    public AugmentedPopulator(final String world, final PlotGenerator generator, final PlotCluster cluster, final boolean p, final boolean b) {
        this.cluster = cluster;
        this.generator = generator;
        this.plotworld = PlotSquared.getPlotWorld(world);
        this.manager = generator.getPlotManager();
        this.p = p;
        this.b = b;
        this.o = (this.plotworld.TERRAIN == 1) || (this.plotworld.TERRAIN == 2);
        final World bukkitWorld = Bukkit.getWorld(world);
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
        if (this.o) {
            bukkitWorld.getPopulators().add(0, this);
        } else {
            bukkitWorld.getPopulators().add(this);
        }
    }
    
    @Override
    public void populate(final World world, final Random rand, final Chunk chunk) {
        final int X = chunk.getX();
        final int Z = chunk.getZ();
        final int x = X << 4;
        final int z = Z << 4;
        final int x2 = x + 15;
        final int z2 = z + 15;
        final boolean inX1 = ((x >= this.bx) && (x <= this.tx));
        final boolean inX2 = ((x2 >= this.bx) && (x2 <= this.tx));
        final boolean inZ1 = ((z >= this.bz) && (z <= this.tz));
        final boolean inZ2 = ((z2 >= this.bz) && (z2 <= this.tz));
        final boolean inX = inX1 || inX2;
        final boolean inZ = inZ1 || inZ2;
        if (!inX || !inZ) {
            return;
        }
        final boolean check;
        if (!inX1 || !inX2 || !inZ1 || !inZ2) {
            check = true;
        } else {
            check = false;
        }
        if (this.plotworld.TERRAIN == 2) {
            final PlotId plot1 = this.manager.getPlotIdAbs(this.plotworld, x, 0, z);
            final PlotId plot2 = this.manager.getPlotIdAbs(this.plotworld, x2, 0, z2);
            if ((plot1 != null) && (plot2 != null) && plot1.equals(plot2)) {
                return;
            }
        }
        if (this.o) {
            chunk.load(true);
            populateBlocks(world, rand, X, Z, x, z, check);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    populateBiome(world, x, z);
                    chunk.unload(true, true);
                    SetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
                }
            }, 20);
        } else {
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    populateBiome(world, x, z);
                }
            }, 20 + rand.nextInt(10));
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    chunk.load(true);
                    populateBlocks(world, rand, X, Z, x, z, check);
                    chunk.unload(true, true);
                    SetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
                }
            }, 40 + rand.nextInt(40));
        }
    }
    
    private void populateBiome(final World world, final int x, final int z) {
        if (this.b) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    world.setBiome(x + i, z + j, this.plotworld.PLOT_BIOME);
                }
            }
        }
    }
    
    private void populateBlocks(final World world, final Random rand, final int X, final int Z, final int x, final int z, final boolean check) {
        final short[][] result = this.generator.generateExtBlockSections(world, rand, X, Z, null);
        final int length = result[0].length;
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < length; j++) {
                final BlockWrapper blockInfo = get(x, z, i, j, result, check);
                if (blockInfo == null) {
                    continue;
                }
                final int xx = x + blockInfo.x;
                final int zz = z + blockInfo.z;
                if (this.p) {
                    if (AChunkManager.CURRENT_PLOT_CLEAR != null) {
                        if (ChunkManager.isIn(AChunkManager.CURRENT_PLOT_CLEAR, xx, zz)) {
                            continue;
                        }
                    } else if (this.manager.getPlotIdAbs(this.plotworld, xx, 0, zz) != null) {
                        continue;
                    }
                }
                SetBlockManager.setBlockManager.set(world, xx, blockInfo.y, zz, blockInfo.id, (byte) 0);
            }
        }
        for (final BlockPopulator populator : this.generator.getDefaultPopulators(world)) {
            populator.populate(world, this.r, world.getChunkAt(X, Z));
        }
    }
    
    public boolean isIn(final RegionWrapper plot, final int x, final int z) {
        return ((x >= plot.minX) && (x <= plot.maxX) && (z >= plot.minZ) && (z <= plot.maxZ));
    }
}
