package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import java.util.HashSet;
import java.util.Random;

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
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
                final World worldObj = Bukkit.getWorld(world);
                final ChunkGenerator gen = worldObj.getGenerator();
                if (gen == null) {
                    return;
                }
                final BiomeGrid nullBiomeGrid = new BiomeGrid() {
                    @Override public void setBiome(int a, int b, Biome c) {
                    }

                    @Override public Biome getBiome(int a, int b) {
                        return null;
                    }
                };

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
                final Random r = new Random();
                MainUtil.initCache();
                final int width = tx - bx + 1;
                final int length = tz - bz + 1;

                System.gc();
                System.gc();
                final short[][][] oldBlocks = new short[256][width][length];
                final short[][][] newBlocks = new short[256][width][length];

                final Runnable run = new Runnable() {
                    @Override public void run() {
                        ChunkManager.chunkTask(bot, top, new RunnableVal<int[]>() {
                            @Override public void run(int[] value) {
                                // [chunkx, chunkz, pos1x, pos1z, pos2x, pos2z, isedge]
                                int X = value[0];
                                int Z = value[1];
                                short[][] result =
                                    gen.generateExtBlockSections(worldObj, r, X, Z, nullBiomeGrid);
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
                                            oldBlocks[y][x][z] = 0;
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
                                        oldBlocks[y][x][z] = result[i][j];
                                    }
                                }

                            }
                        }, new Runnable() {
                            @Override public void run() {
                                TaskManager.runTaskAsync(new Runnable() {
                                    @Override public void run() {
                                        int size = width * length;
                                        int[] changes = new int[size];
                                        int[] faces = new int[size];
                                        int[] data = new int[size];
                                        int[] air = new int[size];
                                        int[] variety = new int[size];
                                        int i = 0;
                                        for (int x = 0; x < width; x++) {
                                            for (int z = 0; z < length; z++) {
                                                HashSet<Short> types = new HashSet<>();
                                                for (int y = 0; y < 256; y++) {
                                                    short old = oldBlocks[y][x][z];
                                                    short now = newBlocks[y][x][z];
                                                    if (old != now) {
                                                        changes[i]++;
                                                    }
                                                    if (now == 0) {
                                                        air[i]++;
                                                    } else {
                                                        // check vertices
                                                        // modifications_adjacent
                                                        if (x > 0 && z > 0 && y > 0 && x < width - 1
                                                            && z < length - 1 && y < 255) {
                                                            if (newBlocks[y - 1][x][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newBlocks[y][x - 1][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newBlocks[y][x][z - 1] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newBlocks[y + 1][x][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newBlocks[y][x + 1][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newBlocks[y][x][z + 1] == 0) {
                                                                faces[i]++;
                                                            }
                                                        }

                                                        Material material =
                                                            Material.getMaterial(now);
                                                        if (material != null) {
                                                            Class<? extends MaterialData> md =
                                                                material.getData();
                                                            if (md.equals(Directional.class)) {
                                                                data[i] += 8;
                                                            } else if (!md
                                                                .equals(MaterialData.class)) {
                                                                data[i]++;
                                                            }
                                                        }
                                                        types.add(now);
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

                                        analysis.changes_sd =
                                            (int) MathMan.getSD(changes, analysis.changes);
                                        analysis.faces_sd =
                                            (int) MathMan.getSD(faces, analysis.faces);
                                        analysis.data_sd = (int) MathMan.getSD(data, analysis.data);
                                        analysis.air_sd = (int) MathMan.getSD(air, analysis.air);
                                        analysis.variety_sd =
                                            (int) MathMan.getSD(variety, analysis.variety);
                                        System.gc();
                                        System.gc();
                                        whenDone.value = analysis;
                                        whenDone.run();
                                    }
                                });
                            }
                        }, 5);

                    }
                };
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
                                    newBlocks[y][xr][zr] = block.id;
                                }
                            }
                        }
                        worldObj.unloadChunkRequest(X, Z, true);
                    }
                }, new Runnable() {
                    @Override public void run() {
                        TaskManager.runTaskAsync(run);
                    }
                }, 5);
            }
        });
    }
}
