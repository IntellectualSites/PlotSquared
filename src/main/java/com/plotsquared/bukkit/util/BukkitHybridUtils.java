package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.TaskManager;

public class BukkitHybridUtils extends HybridUtils {
    
    @Override
    public void analyzeRegion(final String world, final RegionWrapper region, final RunnableVal<PlotAnalysis> whenDone) {
        // int diff, int variety, int verticies, int rotation, int height_sd
        /*
         * diff: compare to base by looping through all blocks
         * variety: add to hashset for each plotblock
         * height_sd: loop over all blocks and get top block
         *
         * verticies: store air map and compare with neighbours
         * for each block check the adjacent
         *  - Store all blocks then go through in second loop
         *  - recheck each block
         *
         */
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final World worldObj = Bukkit.getWorld(world);
                final ChunkGenerator gen = worldObj.getGenerator();
                if (gen == null) {
                    return;
                }
                final BiomeGrid nullBiomeGrid = new BiomeGrid() {
                    @Override
                    public void setBiome(final int a, final int b, final Biome c) {}
                    
                    @Override
                    public Biome getBiome(final int a, final int b) {
                        return null;
                    }
                };
                
                final Location bot = new Location(world, region.minX, region.minY, region.minZ);
                final Location top = new Location(world, region.maxX, region.maxY, region.maxZ);
                
//                final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
//                final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
                
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
                final int width = (tx - bx) + 1;
                final int length = (tz - bz) + 1;
                
                System.gc();
                System.gc();
                final short[][][] oldblocks = new short[256][width][length];
                final short[][][] newblocks = new short[256][width][length];
                
                final Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        ChunkManager.chunkTask(bot, top, new RunnableVal<int[]>() {
                            @Override
                            public void run() {
                                // [chunkx, chunkz, pos1x, pos1z, pos2x, pos2z, isedge]
                                final int X = value[0];
                                final int Z = value[1];
                                final short[][] result = gen.generateExtBlockSections(worldObj, r, X, Z, nullBiomeGrid);
                                final int xb = ((X) << 4) - bx;
                                final int zb = ((Z) << 4) - bz;
                                for (int i = 0; i < result.length; i++) {
                                    if (result[i] == null) {
                                        for (int j = 0; j < 4096; j++) {
                                            final int x = MainUtil.x_loc[i][j] + xb;
                                            if ((x < 0) || (x >= width)) {
                                                continue;
                                            }
                                            final int z = MainUtil.z_loc[i][j] + zb;
                                            if ((z < 0) || (z >= length)) {
                                                continue;
                                            }
                                            final int y = MainUtil.y_loc[i][j];
                                            oldblocks[y][x][z] = 0;
                                        }
                                        continue;
                                    }
                                    for (int j = 0; j < result[i].length; j++) {
                                        final int x = MainUtil.x_loc[i][j] + xb;
                                        if ((x < 0) || (x >= width)) {
                                            continue;
                                        }
                                        final int z = MainUtil.z_loc[i][j] + zb;
                                        if ((z < 0) || (z >= length)) {
                                            continue;
                                        }
                                        final int y = MainUtil.y_loc[i][j];
                                        oldblocks[y][x][z] = result[i][j];
                                    }
                                }
                                
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                TaskManager.runTaskAsync(new Runnable() {
                                    @Override
                                    public void run() {
                                        final int size = width * length;
                                        final int[] changes = new int[size];
                                        final int[] faces = new int[size];
                                        final int[] data = new int[size];
                                        final int[] air = new int[size];
                                        final int[] variety = new int[size];
                                        int i = 0;
                                        for (int x = 0; x < width; x++) {
                                            for (int z = 0; z < length; z++) {
                                                final HashSet<Short> types = new HashSet<>();
                                                for (int y = 0; y < 256; y++) {
                                                    final short old = oldblocks[y][x][z];
                                                    final short now = newblocks[y][x][z];
                                                    if (old != now) {
                                                        changes[i]++;
                                                    }
                                                    if (now == 0) {
                                                        air[i]++;
                                                    } else {
                                                        // check verticies
                                                        // modifications_adjacent
                                                        if ((x > 0) && (z > 0) && (y > 0) && (x < (width - 1)) && (z < (length - 1)) && (y < 255)) {
                                                            if (newblocks[y - 1][x][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newblocks[y][x - 1][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newblocks[y][x][z - 1] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newblocks[y + 1][x][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newblocks[y][x + 1][z] == 0) {
                                                                faces[i]++;
                                                            }
                                                            if (newblocks[y][x][z + 1] == 0) {
                                                                faces[i]++;
                                                            }
                                                        }
                                                        
                                                        final Material material = Material.getMaterial(now);
                                                        final Class<? extends MaterialData> md = material.getData();
                                                        if (md.equals(Directional.class)) {
                                                            data[i] += 8;
                                                        } else if (!md.equals(MaterialData.class)) {
                                                            data[i]++;
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
                                        final PlotAnalysis analysis = new PlotAnalysis();
                                        analysis.changes = (int) (MathMan.getMean(changes) * 100);
                                        analysis.faces = (int) (MathMan.getMean(faces) * 100);
                                        analysis.data = (int) (MathMan.getMean(data) * 100);
                                        analysis.air = (int) (MathMan.getMean(air) * 100);
                                        analysis.variety = (int) (MathMan.getMean(variety) * 100);
                                        
                                        analysis.changes_sd = (int) (MathMan.getSD(changes, analysis.changes));
                                        analysis.faces_sd = (int) (MathMan.getSD(faces, analysis.faces));
                                        analysis.data_sd = (int) (MathMan.getSD(data, analysis.data));
                                        analysis.air_sd = (int) (MathMan.getSD(air, analysis.air));
                                        analysis.variety_sd = (int) (MathMan.getSD(variety, analysis.variety));
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
                    
                    @Override
                    public void run() {
                        final int X = value[0];
                        final int Z = value[1];
                        worldObj.loadChunk(X, Z);
                        int minX;
                        int minZ;
                        int maxX;
                        int maxZ;
                        if (X == cbx) {
                            minX = bx & 15;
                        } else {
                            minX = 0;
                        }
                        if (Z == cbz) {
                            minZ = bz & 15;
                        } else {
                            minZ = 0;
                        }
                        if (X == ctx) {
                            maxX = tx & 15;
                        } else {
                            maxX = 16;
                        }
                        if (Z == ctz) {
                            maxZ = tz & 15;
                        } else {
                            maxZ = 16;
                        }
                        
                        final int cbx = X << 4;
                        final int cbz = Z << 4;
                        
                        final int xb = (cbx) - bx;
                        final int zb = (cbz) - bz;
                        for (int x = minX; x <= maxX; x++) {
                            final int xx = cbx + x;
                            for (int z = minZ; z <= maxZ; z++) {
                                final int zz = cbz + z;
                                for (int y = 0; y < 256; y++) {
                                    final Block block = worldObj.getBlockAt(xx, y, zz);
                                    final int xr = xb + x;
                                    final int zr = zb + z;
                                    newblocks[y][xr][zr] = (short) block.getTypeId();
                                }
                            }
                        }
                        worldObj.unloadChunkRequest(X, Z, true);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.runTaskAsync(run);
                    }
                }, 5);
            }
        });
    }
    
    @Override
    public int checkModified(final String worldname, final int x1, final int x2, final int y1, final int y2, final int z1, final int z2, final PlotBlock[] blocks) {
        final World world = BukkitUtil.getWorld(worldname);
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    final Block block = world.getBlockAt(x, y, z);
                    final int id = block.getTypeId();
                    boolean same = false;
                    for (final PlotBlock p : blocks) {
                        if (id == p.id) {
                            same = true;
                            break;
                        }
                    }
                    if (!same) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    @Override
    public int get_ey(final String worldname, final int sx, final int ex, final int sz, final int ez, final int sy) {
        final World world = BukkitUtil.getWorld(worldname);
        final int maxY = world.getMaxHeight();
        int ey = sy;
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= ez; z++) {
                for (int y = sy; y < maxY; y++) {
                    if (y > ey) {
                        final Block block = world.getBlockAt(x, y, z);
                        if (block.getTypeId() != 0) {
                            ey = y;
                        }
                    }
                }
            }
        }
        return ey;
    }
}
