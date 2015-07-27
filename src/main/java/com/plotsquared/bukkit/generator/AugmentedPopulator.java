package com.plotsquared.bukkit.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.object.BlockWrapper;
import com.plotsquared.bukkit.util.bukkit.BukkitChunkManager;
import com.plotsquared.bukkit.util.bukkit.BukkitSetBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class AugmentedPopulator extends BlockPopulator {
    public static short[][] x_loc;
    public static short[][] y_loc;
    public static short[][] z_loc;
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
        initCache();
        this.cluster = cluster;
        this.generator = generator;
        this.plotworld = PS.get().getPlotWorld(world);
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

    public static void initCache() {
        if (x_loc == null) {
            x_loc = new short[16][4096];
            y_loc = new short[16][4096];
            z_loc = new short[16][4096];
            for (int i = 0; i < 16; i++) {
                int i4 = i << 4;
                for (int j = 0; j < 4096; j++) {
                    final int y = (i4) + (j >> 8);
                    final int a = (j - ((y & 0xF) << 8));
                    final int z1 = (a >> 4);
                    final int x1 = a - (z1 << 4);
                    x_loc[i][j] = (short) x1;
                    y_loc[i][j] = (short) y;
                    z_loc[i][j] = (short) z1;
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
            return (c && (((z + z1) < this.bz) || ((z + z1) > this.tz) || ((x + x1) < this.bx) || ((x + x1) > this.tx))) ? null : new BlockWrapper(x1, y, z1, (short) 0, (byte) 0);
        } else {
            return (c && (((z + z1) < this.bz) || ((z + z1) > this.tz) || ((x + x1) < this.bx) || ((x + x1) > this.tx))) ? null : new BlockWrapper(x1, y, z1, r[i][j], (byte) 0);
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
        if (this.plotworld.TERRAIN == 3) {
            int X = chunk.getX() << 4;
            int Z = chunk.getZ() << 4;
            if (ChunkManager.FORCE_PASTE) {
                for (short x = 0; x < 16; x++) {
                    for (short z = 0; z < 16; z++) {
                        final PlotLoc loc = new PlotLoc((short) (X + x), (short) (Z + z));
                        final HashMap<Short, Short> blocks = ChunkManager.GENERATE_BLOCKS.get(loc);
                        HashMap<Short, Byte> datas = ChunkManager.GENERATE_DATA.get(loc);
                        for (final Entry<Short, Short> entry : blocks.entrySet()) {
                            int y = entry.getKey();
                            byte data;
                            if (datas != null) {
                                data = datas.get(y);
                            }
                            else {
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
        if (this.plotworld.TERRAIN > 1) {
            final PlotId plot1 = this.manager.getPlotIdAbs(this.plotworld, bx, 0, bz);
            final PlotId plot2 = this.manager.getPlotIdAbs(this.plotworld, tx, 0, tz);
            if ((plot1 != null) && (plot2 != null) && plot1.equals(plot2)) {
                return;
            }
        }
        if (this.o) {
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
        final Biome biome = Biome.valueOf(this.plotworld.PLOT_BIOME);
        if (this.b) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    world.setBiome(x + i, z + j, biome);
                }
            }
        }
    }
    
    private void populateBlocks(final World world, final Random rand, final int X, final int Z, final int x, final int z, final boolean check) {
        final short[][] result = this.generator.generateExtBlockSections(world, rand, X, Z, null);
        for (int i = 0; i < result.length; i++) {
            if (result[i] != null) {
                for (int j = 0; j < 4096; j++) {
                    int x1 = x_loc[i][j];
                    int y = y_loc[i][j];
                    int z1 = z_loc[i][j];
                    short id = result[i][j];
                    final int xx = x + x1;
                    final int zz = z + z1;
                    if (check && (((zz) < this.bz) || ((zz) > this.tz) || ((xx) < this.bx) || ((xx) > this.tx))) {
                        continue;
                    }
                    if (this.p) {
                        if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                            if (BukkitChunkManager.isIn(ChunkManager.CURRENT_PLOT_CLEAR, xx, zz)) {
                                continue;
                            }
                        } else if (this.manager.getPlotIdAbs(this.plotworld, xx, 0, zz) != null) {
                            continue;
                        }
                    }
                    BukkitSetBlockManager.setBlockManager.set(world, xx, y, zz, id, (byte) 0);
                }
            }
            else {
                short y_min = y_loc[i][0];
                if (y_min < 128) {
                    for (int x1 = x; x1 < x + 16; x1++) {
                        for (int z1 = z; z1 < z + 16; z1++) {
                            if (check && (((z1) < this.bz) || ((z1) > this.tz) || ((x1) < this.bx) || ((x1) > this.tx))) {
                                continue;
                            }
                            if (this.p) {
                                if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
                                    if (BukkitChunkManager.isIn(ChunkManager.CURRENT_PLOT_CLEAR, x1, z1)) {
                                        continue;
                                    }
                                } else if (this.manager.getPlotIdAbs(this.plotworld, x1, 0, z1) != null) {
                                    continue;
                                }
                            }
                            for (int y = y_min; y < y_min + 16; y++) {
                                BukkitSetBlockManager.setBlockManager.set(world, x1, y, z1, 0, (byte) 0);
                            }
                        }
                    }
                }
            }
        }
        for (final BlockPopulator populator : this.generator.getPopulators(world.getName())) {
            Chunk chunk = world.getChunkAt(X, Z);
            populator.populate(world, this.r, chunk);
        }
    }

    public boolean isIn(final RegionWrapper plot, final int x, final int z) {
        return ((x >= plot.minX) && (x <= plot.maxX) && (z >= plot.minZ) && (z <= plot.maxZ));
    }
}
