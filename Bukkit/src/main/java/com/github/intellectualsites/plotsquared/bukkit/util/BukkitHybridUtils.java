package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.generator.ChunkGenerator;

import java.util.HashSet;

public class BukkitHybridUtils extends HybridUtils {

    @Override public void analyzeRegion(final String world, final RegionWrapper region,
        final RunnableVal<PlotAnalysis> whenDone) {
        // int diff, int variety, int vertices, int rotation, int height_sd
        /*
         * diff: compare to base by looping through all blocks
         * variety: add to HashSet for each PlotBlock
         * height_sd: loop over all blocks and get top block
         *
         * vertices: store air map and compare with neighbours
         * for each block check the adjacent
         *  - Store all blocks then go through in second loop
         *  - recheck each block
         *
         */
        TaskManager.runTaskAsync(() -> {
            final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
            final World worldObj = Bukkit.getWorld(world);
            final ChunkGenerator chunkGenerator = worldObj.getGenerator();
            if (!(chunkGenerator instanceof BukkitPlotGenerator)) {
                return;
            }

            final Location bot = new Location(world, region.minX, region.minY, region.minZ);
            final Location top = new Location(world, region.maxX, region.maxY, region.maxZ);

            final int bx = bot.getX();
            final int bz = bot.getZ();
            final int tx = top.getX();
            final int tz = top.getZ();
            final int cbx = bx >> 4;
            final int cbz = bz >> 4;
            final int ctx = tx >> 4;
            final int ctz = tz >> 4;
            MainUtil.initCache();
            final int width = tx - bx + 1;
            final int length = tz - bz + 1;

            System.gc();
            System.gc();
            final BlockBucket[][][] oldBlocks = new BlockBucket[256][width][length];
            final PlotBlock[][][] newBlocks = new PlotBlock[256][width][length];
            final BlockBucket airBucket = BlockBucket.withSingle(StringPlotBlock.EVERYTHING);

            PlotArea area = PlotSquared.get().getPlotArea(world, null);

            if (!(area instanceof HybridPlotWorld)) {
                return;
            }

            HybridPlotWorld hpw = (HybridPlotWorld) area;
            final BlockBucket[][] result = hpw.getBlockBucketChunk();

            if (result == null) {
                return;
            }

            if (hpw.PLOT_SCHEMATIC) {
                short[] rx = new short[16];
                short[] rz = new short[16];
                short rbx;
                short rbz;
                if (bx < 0) {
                    rbx = (short) (hpw.SIZE + (bx % hpw.SIZE));
                } else {
                    rbx = (short) (bx % hpw.SIZE);
                }
                if (bz < 0) {
                    rbz = (short) (hpw.SIZE + (bz % hpw.SIZE));
                } else {
                    rbz = (short) (bz % hpw.SIZE);
                }
                for (short i = 0; i < 16; i++) {
                    short v = (short) (rbx + i);
                    if (v >= hpw.SIZE) {
                        v -= hpw.SIZE;
                    }
                    rx[i] = v;
                }
                for (short i = 0; i < 16; i++) {
                    short v = (short) (rbz + i);
                    if (v >= hpw.SIZE) {
                        v -= hpw.SIZE;
                    }
                    rz[i] = v;
                }
                int minY;
                if (Settings.Schematics.PASTE_ON_TOP) {
                    minY = hpw.SCHEM_Y;
                } else {
                    minY = 1;
                }
                for (short x = 0; x < 16; x++) {
                    for (short z = 0; z < 16; z++) {
                        BaseBlock[] blocks = hpw.G_SCH.get(MathMan.pair(rx[x], rz[z]));
                        for (int y = 0; y < blocks.length; y++) {
                            if (blocks[y] != null) {
                                result[(minY + y) >> 4][(((minY + y) & 0xF) << 8) | (z << 4) | x] =
                                    BlockBucket.withSingle(PlotBlock.get(blocks[y]));
                            }
                        }
                    }
                }
            }

            final Runnable run = () -> ChunkManager.chunkTask(bot, top, new RunnableVal<int[]>() {
                @Override public void run(int[] value) {
                    // [chunkx, chunkz, pos1x, pos1z, pos2x, pos2z, isedge]
                    int X = value[0];
                    int Z = value[1];
                    int xb = (X << 4) - bx;
                    int zb = (Z << 4) - bz;
                    for (int i = 0; i < result.length; i++) {
                        if (result[i] == null) {
                            for (int j = 0; j < 4096; j++) {
                                int x = MainUtil.x_loc[i][j] + xb;
                                if (x < 0 || x >= width) {
                                    continue;
                                }
                                int z = MainUtil.z_loc[i][j] + zb;
                                if (z < 0 || z >= length) {
                                    continue;
                                }
                                int y = MainUtil.y_loc[i][j];
                                oldBlocks[y][x][z] = airBucket;
                            }
                            continue;
                        }
                        for (int j = 0; j < result[i].length; j++) {
                            int x = MainUtil.x_loc[i][j] + xb;
                            if (x < 0 || x >= width) {
                                continue;
                            }
                            int z = MainUtil.z_loc[i][j] + zb;
                            if (z < 0 || z >= length) {
                                continue;
                            }
                            int y = MainUtil.y_loc[i][j];
                            oldBlocks[y][x][z] = result[i][j] != null ? result[i][j] : airBucket;
                        }
                    }

                }
            }, () -> TaskManager.runTaskAsync(() -> {
                int size = width * length;
                int[] changes = new int[size];
                int[] faces = new int[size];
                int[] data = new int[size];
                int[] air = new int[size];
                int[] variety = new int[size];
                int i = 0;
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        HashSet<PlotBlock> types = new HashSet<>();
                        for (int y = 0; y < 256; y++) {
                            BlockBucket old = oldBlocks[y][x][z];
                            try {
                                if (old == null) {
                                    old = airBucket;
                                }
                                PlotBlock now = newBlocks[y][x][z];
                                if (!old.getBlocks().contains(now)) {
                                    changes[i]++;
                                }
                                if (now.isAir()) {
                                    air[i]++;
                                } else {
                                    // check vertices
                                    // modifications_adjacent
                                    if (x > 0 && z > 0 && y > 0 && x < width - 1 && z < length - 1
                                        && y < 255) {
                                        if (newBlocks[y - 1][x][z].isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x - 1][z].isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x][z - 1].isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y + 1][x][z].isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x + 1][z].isAir()) {
                                            faces[i]++;
                                        }
                                        if (newBlocks[y][x][z + 1].isAir()) {
                                            faces[i]++;
                                        }
                                    }

                                    Material material = now.to(Material.class);
                                    if (material != null) {
                                        BlockData blockData = material.createBlockData();
                                        if (blockData instanceof Directional) {
                                            data[i] += 8;
                                        } else if (!blockData.getClass().equals(BlockData.class)) {
                                            data[i]++;
                                        }
                                    }
                                    types.add(now);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        variety[i] = types.size();
                        i++;
                    }
                }
                // analyze plot
                // put in analysis obj

                // run whenDone
                PlotAnalysis analysis = new PlotAnalysis();
                analysis.changes = (int) (MathMan.getMean(changes) * 100);
                analysis.faces = (int) (MathMan.getMean(faces) * 100);
                analysis.data = (int) (MathMan.getMean(data) * 100);
                analysis.air = (int) (MathMan.getMean(air) * 100);
                analysis.variety = (int) (MathMan.getMean(variety) * 100);

                analysis.changes_sd = (int) (MathMan.getSD(changes, analysis.changes) * 100);
                analysis.faces_sd = (int) (MathMan.getSD(faces, analysis.faces) * 100);
                analysis.data_sd = (int) (MathMan.getSD(data, analysis.data) * 100);
                analysis.air_sd = (int) (MathMan.getSD(air, analysis.air) * 100);
                analysis.variety_sd = (int) (MathMan.getSD(variety, analysis.variety) * 100);
                System.gc();
                System.gc();
                whenDone.value = analysis;
                whenDone.run();
            }), 5);
            System.gc();
            MainUtil.initCache();
            ChunkManager.chunkTask(bot, top, new RunnableVal<int[]>() {
                @Override public void run(int[] value) {
                    int X = value[0];
                    int Z = value[1];
                    worldObj.loadChunk(X, Z);
                    int minX;
                    if (X == cbx) {
                        minX = bx & 15;
                    } else {
                        minX = 0;
                    }
                    int minZ;
                    if (Z == cbz) {
                        minZ = bz & 15;
                    } else {
                        minZ = 0;
                    }
                    int maxX;
                    if (X == ctx) {
                        maxX = tx & 15;
                    } else {
                        maxX = 16;
                    }
                    int maxZ;
                    if (Z == ctz) {
                        maxZ = tz & 15;
                    } else {
                        maxZ = 16;
                    }

                    int cbx = X << 4;
                    int cbz = Z << 4;

                    int xb = cbx - bx;
                    int zb = cbz - bz;
                    for (int x = minX; x <= maxX; x++) {
                        int xx = cbx + x;
                        for (int z = minZ; z <= maxZ; z++) {
                            int zz = cbz + z;
                            for (int y = 0; y < 256; y++) {
                                PlotBlock block = queue.getBlock(xx, y, zz);
                                int xr = xb + x;
                                int zr = zb + z;
                                newBlocks[y][xr][zr] = block;
                            }
                        }
                    }
                    worldObj.unloadChunkRequest(X, Z);
                }
            }, () -> TaskManager.runTaskAsync(run), 5);
        });
    }
}
