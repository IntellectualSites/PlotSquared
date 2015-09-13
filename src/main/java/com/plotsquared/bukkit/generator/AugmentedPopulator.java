package com.plotsquared.bukkit.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.BlockWrapper;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.BukkitChunkManager;
import com.plotsquared.bukkit.util.BukkitSetBlockManager;

public class AugmentedPopulator extends BlockPopulator {
    public final PlotWorld plotworld;
    public final PlotManager manager;
    public final BukkitPlotGenerator generator;
    public final PlotCluster cluster;
    public final Random r = new Random();
    public final boolean p;
    public final boolean b;
    public final boolean o;
    private final int bx;
    private final int bz;
    private final int tx;
    private final int tz;
    
    public AugmentedPopulator(final String world, final BukkitPlotGenerator generator, final PlotCluster cluster, final boolean p, final boolean b) {
        MainUtil.initCache();
        PS.log("== NEW AUGMENTED POPULATOR FOR: " + world);
        this.cluster = cluster;
        this.generator = generator;
        plotworld = PS.get().getPlotWorld(world);
        manager = generator.getPlotManager();
        this.p = p;
        this.b = b;
        o = (plotworld.TERRAIN == 1) || (plotworld.TERRAIN == 2);
        final World bukkitWorld = Bukkit.getWorld(world);
        if (cluster != null) {
            final Location bl = manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
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
        if (o) {
            bukkitWorld.getPopulators().add(0, this);
        } else {
            bukkitWorld.getPopulators().add(this);
        }
    }
    
    public static void removePopulator(final String worldname, final PlotCluster cluster) {
        final World world = Bukkit.getWorld(worldname);
        for (final Iterator<BlockPopulator> iterator = world.getPopulators().iterator(); iterator.hasNext();) {
            final BlockPopulator populator = iterator.next();
            if (populator instanceof AugmentedPopulator) {
                if (((AugmentedPopulator) populator).cluster.equals(cluster)) {
                    iterator.remove();
                }
            }
        }
    }
    
    public BlockWrapper get(final int x, final int z, final int i, final int j, final short[][] r, final boolean c) {
        final int y = (i << 4) + (j >> 8);
        final int a = (j - ((y & 0xF) << 8));
        final int z1 = (a >> 4);
        final int x1 = a - (z1 << 4);
        if (r[i] == null) {
            return (c && (((z + z1) < bz) || ((z + z1) > tz) || ((x + x1) < bx) || ((x + x1) > tx))) ? null : new BlockWrapper(x1, y, z1, (short) 0, (byte) 0);
        } else {
            return (c && (((z + z1) < bz) || ((z + z1) > tz) || ((x + x1) < bx) || ((x + x1) > tx))) ? null : new BlockWrapper(x1, y, z1, r[i][j], (byte) 0);
        }
    }
    
    @Override
    public void populate(final World world, final Random rand, final Chunk chunk) {
        final int cx = chunk.getX();
        final int cz = chunk.getZ();
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
            final int X = chunk.getX() << 4;
            final int Z = chunk.getZ() << 4;
            if (ChunkManager.FORCE_PASTE) {
                for (short x = 0; x < 16; x++) {
                    for (short z = 0; z < 16; z++) {
                        final PlotLoc loc = new PlotLoc((short) (X + x), (short) (Z + z));
                        final HashMap<Short, Short> blocks = ChunkManager.GENERATE_BLOCKS.get(loc);
                        final HashMap<Short, Byte> datas = ChunkManager.GENERATE_DATA.get(loc);
                        for (final Entry<Short, Short> entry : blocks.entrySet()) {
                            final int y = entry.getKey();
                            byte data;
                            if (datas != null) {
                                data = datas.get(y);
                            } else {
                                data = 0;
                            }
                            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, blocks.get(y), data);
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
                                BukkitSetBlockManager.setBlockManager.set(world, xx, y, zz, entry2.getValue(), data);
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
            populateBlocks(world, rand, cx, cz, bx, bz, check);
        } else {
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    populateBiome(world, bx, bz);
                }
            }, 20 + rand.nextInt(10));
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    chunk.load(true);
                    populateBlocks(world, rand, cx, cz, bx, bz, check);
                }
            }, 40 + rand.nextInt(40));
        }
    }
    
    private void populateBiome(final World world, final int x, final int z) {
        final Biome biome = Biome.valueOf(plotworld.PLOT_BIOME);
        if (b) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    world.setBiome(x + i, z + j, biome);
                }
            }
        }
    }
    
    private void populateBlocks(final World world, final Random rand, final int X, final int Z, final int x, final int z, final boolean check) {
        final short[][] result = generator.generateExtBlockSections(world, rand, X, Z, null);
        for (int i = 0; i < result.length; i++) {
            if (result[i] != null) {
                for (int j = 0; j < 4096; j++) {
                    final int x1 = MainUtil.x_loc[i][j];
                    final int y = MainUtil.y_loc[i][j];
                    final int z1 = MainUtil.z_loc[i][j];
                    final short id = result[i][j];
                    final int xx = x + x1;
                    final int zz = z + z1;
                    if (check && (((zz) < bz) || ((zz) > tz) || ((xx) < bx) || ((xx) > tx))) {
                        continue;
                    }
                    if (p) {
                        if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                            if (BukkitChunkManager.isIn(ChunkManager.CURRENT_PLOT_CLEAR, xx, zz)) {
                                continue;
                            }
                        } else if (manager.getPlotIdAbs(plotworld, xx, 0, zz) != null) {
                            continue;
                        }
                    }
                    BukkitSetBlockManager.setBlockManager.set(world, xx, y, zz, id, (byte) 0);
                }
            } else {
                final short y_min = MainUtil.y_loc[i][0];
                if (y_min < 128) {
                    for (int x1 = x; x1 < (x + 16); x1++) {
                        for (int z1 = z; z1 < (z + 16); z1++) {
                            if (check && (((z1) < bz) || ((z1) > tz) || ((x1) < bx) || ((x1) > tx))) {
                                continue;
                            }
                            if (p) {
                                if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                                    if (BukkitChunkManager.isIn(ChunkManager.CURRENT_PLOT_CLEAR, x1, z1)) {
                                        continue;
                                    }
                                } else if (manager.getPlotIdAbs(plotworld, x1, 0, z1) != null) {
                                    continue;
                                }
                            }
                            for (int y = y_min; y < (y_min + 16); y++) {
                                BukkitSetBlockManager.setBlockManager.set(world, x1, y, z1, 0, (byte) 0);
                            }
                        }
                    }
                }
            }
        }
        for (final BlockPopulator populator : generator.getPopulators(world.getName())) {
            final Chunk chunk = world.getChunkAt(X, Z);
            populator.populate(world, r, chunk);
        }
    }
}
