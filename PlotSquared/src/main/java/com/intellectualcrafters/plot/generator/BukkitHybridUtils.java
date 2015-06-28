package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class BukkitHybridUtils extends HybridUtils {
	
    public double getMean(int[] array) {
        double count = 0;
        for (int i : array) {
            count += i;
        }
        return count / array.length; 
    }
    
    public double getMean(double[] array) {
        double count = 0;
        for (double i : array) {
            count += i;
        }
        return count / array.length; 
    }
    
    public double getSD(double[] array) {
        double av = getMean(array);
        double sd = 0;
        for (int i=0; i<array.length;i++)
        {
            sd += Math.pow(Math.abs(array[i] - av), 2);
        }
        return Math.sqrt(sd/array.length);
    }
    
    public double getSD(int[] array) {
        double av = getMean(array);
        double sd = 0;
        for (int i=0; i<array.length;i++)
        {
            sd += Math.pow(Math.abs(array[i] - av), 2);
        }
        return Math.sqrt(sd/array.length);
    }
    
    public int mod(int x) {
        if (x < 0) {
            return (x % 16) + 16;
        }
        return x % 16;
    }
    
    @Override
    public void analyzePlot(Plot plot, final RunnableVal<PlotAnalysis> whenDone) {
        // TODO Auto-generated method stub
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
        final World world = Bukkit.getWorld(plot.world);
        final ChunkGenerator gen = world.getGenerator();
        if (gen == null) {
            return;
        }
        final BiomeGrid base = new BiomeGrid() { @Override public void setBiome(int a, int b, Biome c) {} @Override public Biome getBiome(int a, int b) {return null;}};
        ClassicPlotWorld cpw = (ClassicPlotWorld) PlotSquared.getPlotWorld(plot.world);
        final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id).add(1, 0, 1);
        final int bx = bot.getX() >> 4;
        final int bz = bot.getZ() >> 4;
        final int tx = top.getX() >> 4;
        final int tz = top.getZ() >> 4;
        final Random r = new Random();
        AugmentedPopulator.initCache();
        
        final PlotAnalysis analysis = new PlotAnalysis();
        
        int num_chunks = (tx - bx + 1) * (tz - bz + 1);
        
        short[][][] oldblocks = new short[256][top.getX() - bot.getX() + 1][top.getZ() - bot.getZ() + 1];
        short[][][] newblocks = new short[256][top.getX() - bot.getX() + 1][top.getZ() - bot.getZ() + 1];
        
        final List<Chunk> chunks = new ArrayList<>();
        
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                Chunk chunk = world.getChunkAt(X, Z);
                chunks.add(chunk);
            }
        }
        
        final double[] changes_sd = new double[num_chunks];
        final double[] rotations_sd = new double[num_chunks];
        final double[] data_sd = new double[num_chunks];
        final double[] variety_sd = new double[num_chunks];
        final double[] uniformity_sd = new double[num_chunks];
        final double[] verticies_sd = new double[num_chunks];
        final double[] height_sd = new double[num_chunks];
        
        final HashSet<Short> variety = new HashSet<>();
        
        TaskManager.index.increment();
        final Integer currentIndex = TaskManager.index.toInteger();
        final Integer task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                int index = chunks.size() - 1;
                if (index == -1) {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            PlotSquared.TASK.cancelTask(TaskManager.tasks.get(currentIndex));
                            analysis.variety_total = variety.size();
                            analysis.changes_sd = getSD(changes_sd);
                            analysis.rotations_sd = getSD(rotations_sd);
                            analysis.data_sd = getSD(data_sd);
                            analysis.uniformity_sd = getSD(uniformity_sd);
                            analysis.variety_sd = getSD(variety_sd);
                            analysis.verticies_sd = getSD(verticies_sd);
                            analysis.height_sd = getSD(height_sd);
                            whenDone.value = analysis;
                            whenDone.run();
                        }
                    });
                    return;
                }
                Chunk chunk = chunks.remove(0);
                int X = chunk.getX();
                int Z = chunk.getZ();
                short[][] result = gen.generateExtBlockSections(world, r, chunk.getX(), chunk.getZ(), base);
                short[][] current = new short[16][];
                int minX;
                int minZ;
                int maxX;
                int maxZ;
                if (X == bx) minX = mod(bot.getX());
                else minX = 0;
                if (Z == bz) minZ = mod(bot.getZ());
                else minZ = 0;
                if (X == tx) maxX = mod(top.getX());
                else maxX = 16;
                if (Z == tz) maxZ = mod(top.getZ());
                else maxZ = 16;
                long changes = 0;
                long rotations = 0;
                long materialdata = 0;
                int[] height = new int[256];
                
                int collumn_index = 0;
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        int max = 0;
                        for (int y = 0; y < 256; y++) {
                            Block block = chunk.getBlock(x, y, z);
                            int i = y >> 4;
                            int j = ((y & 0xF) << 8) | (z << 4) | x;
                            short id = (short) block.getTypeId();
                            if (id != 0) {
                                if (y > max) {
                                    max = y;
                                }
                                BlockState state = block.getState();
                                MaterialData data = state.getData();
                                if (data instanceof Directional) {
                                    rotations++;
                                }
                                else if (!data.getClass().equals(MaterialData.class)) {
                                    materialdata++;
                                }
                                else {
                                    variety.add(id);
                                }
                            }
                            if (current[i] == null) {
                                current[i] = new short[4096];
                            }
                            current[i][j] = id;
                            if (result[i] == null && id != 0 || result[i] != null && result[i][j] != id) {
                                changes++;
                            }
                        }
                        height[collumn_index] = max;
                        collumn_index++;
                    }
                }
                long verticies = 0;
                int[] uniformity = new int[254];
                for (int y = 1; y < 255; y++) {
                    HashSet<Integer> blocks = new HashSet<>();
                    for (int x = minX + 1; x < maxX - 1; x++) {
                        for (int z = minZ + 1; z < maxZ - 1; z++) {
                            short id = current[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
                            if (id == 0) continue;
                            blocks.add((int) id);
                            if (current[(y + 1) >> 4][(((y + 1) & 0xF) << 8) | (z << 4) | x] != 0) verticies--;
                            if (current[(y - 1) >> 4][(((y - 1) & 0xF) << 8) | (z << 4) | x] != 0) verticies--;
                            if (current[y >> 4][((y & 0xF) << 8) | ((z + 1) << 4) | x] != 0) verticies--;
                            if (current[y >> 4][((y & 0xF) << 8) | ((z - 1) << 4) | x] != 0) verticies--;
                            if (current[y >> 4][((y & 0xF) << 8) | (z << 4) | (x + 1)] != 0) verticies--;
                            if (current[y >> 4][((y & 0xF) << 8) | (z << 4) | (x - 1)] != 0) verticies--;
                            verticies += 6;
                        }   
                    }
                    uniformity[y-1] = blocks.size();
                }
                analysis.changes_total += changes;
                analysis.rotations_total += rotations;
                analysis.data_total += materialdata;
                analysis.verticies_total += verticies;
                double tmp = getSD(height);
                analysis.height_total += tmp;
                
                changes_sd[index] = changes;
                rotations_sd[index] = rotations;
                data_sd[index] = materialdata;
                uniformity_sd[index] = getSD(uniformity);
                variety_sd[index] = variety.size();
                verticies_sd[index] = verticies;
                height_sd[index] = tmp;
            };
        }, 1);
        TaskManager.tasks.put(currentIndex, task);
    }
    
	public void checkModified(final Plot plot, final RunnableVal<Integer> whenDone) {
		TaskManager.index.increment();
		final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
		final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        int bx = bot.getX() >> 4;
        int bz = bot.getZ() >> 4;
        int tx = top.getX() >> 4;
        int tz = top.getZ() >> 4;
        World world = BukkitUtil.getWorld(plot.world);
        final HashSet<Chunk> chunks = new HashSet<>();
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                chunks.add(world.getChunkAt(X,Z));
            }
        }
        PlotWorld plotworld = PlotSquared.getPlotWorld(plot.world);
        if (!(plotworld instanceof ClassicPlotWorld)) {
            whenDone.value = -1;
        	TaskManager.runTaskLater(whenDone, 1);
        	return;
        }
        
        final ClassicPlotWorld cpw = (ClassicPlotWorld) plotworld;
        
        final MutableInt count = new MutableInt(0);
        
        final Integer currentIndex = TaskManager.index.toInteger();
        final Integer task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (chunks.size() == 0) {
                    whenDone.value = count.intValue();
                    TaskManager.runTaskLater(whenDone, 1);
                    Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                    TaskManager.tasks.remove(currentIndex);
                    return;
                }
                Iterator<Chunk> iter = chunks.iterator();
                final Chunk chunk = iter.next();
                iter.remove();
                int bx = Math.max(chunk.getX() << 4, bot.getX());
                int bz = Math.max(chunk.getZ() << 4, bot.getZ());
                int ex = Math.min((chunk.getX() << 4) + 15, top.getX());
                int ez = Math.min((chunk.getZ() << 4) + 15, top.getZ());
                // count changes
                count.add(checkModified(plot.world, bx, ex, 1, cpw.PLOT_HEIGHT - 1, bz, ez, cpw.MAIN_BLOCK));
                count.add(checkModified(plot.world, bx, ex, cpw.PLOT_HEIGHT, cpw.PLOT_HEIGHT, bz, ez, cpw.TOP_BLOCK));
                count.add(checkModified(plot.world, bx, ex, cpw.PLOT_HEIGHT + 1, 255, bz, ez, new PlotBlock[] { new PlotBlock((short) 0, (byte) 0) }));
            }
        }, 1);
        TaskManager.tasks.put(currentIndex, task);

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
    
    public void regenerateChunkChunk(final String worldname, final ChunkLoc loc) {
        final World world = BukkitUtil.getWorld(worldname);
        final int sx = loc.x << 5;
        final int sz = loc.z << 5;
        for (int x = sx; x < (sx + 32); x++) {
            for (int z = sz; z < (sz + 32); z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunk.load(false);
            }
        }
        final ArrayList<Chunk> chunks2 = new ArrayList<>();
        for (int x = sx; x < (sx + 32); x++) {
            for (int z = sz; z < (sz + 32); z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunks2.add(chunk);
                regenerateRoad(worldname, new ChunkLoc(x, z), 0);
                MainUtil.update(world.getName(), new ChunkLoc(chunk.getX(), chunk.getZ()));
            }
        }
    }
    
    public final ArrayList<ChunkLoc> getChunks(ChunkLoc region) {
    	ArrayList<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
    	final int sx = region.x << 5;
        final int sz = region.z << 5;
        for (int x = sx; x < (sx + 32); x++) {
            for (int z = sz; z < (sz + 32); z++) {
            	chunks.add(new ChunkLoc(x, z));
            }
        }
        return chunks;
    }
    
    private static boolean UPDATE = false;
    public int task;
    private long last;

    @Override
    public boolean scheduleRoadUpdate(final String world, int extend) {
        if (BukkitHybridUtils.UPDATE) {
            return false;
        }
        BukkitHybridUtils.UPDATE = true;
        final List<ChunkLoc> regions = ChunkManager.manager.getChunkChunks(world);
        return scheduleRoadUpdate(world, regions, extend);
    }
    
    public static List<ChunkLoc> regions;
    public static List<ChunkLoc> chunks = new ArrayList<>();
    public static String world;
    
    public boolean scheduleRoadUpdate(final String world, final List<ChunkLoc> rgs, final int extend) {
        BukkitHybridUtils.regions = rgs;
        BukkitHybridUtils.world = world;
        chunks = new ArrayList<ChunkLoc>();
        final Plugin plugin = BukkitMain.THIS;
        final MutableInt count = new MutableInt(0);
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                count.increment();
                if (count.intValue() % 20 == 0) {
                    PlotSquared.log("PROGRESS: " + ((100 * (2048 - chunks.size())) / 2048) + "%");
                }
                if (regions.size() == 0 && chunks.size() == 0) {
                    BukkitHybridUtils.UPDATE = false;
                    PlotSquared.log(C.PREFIX.s() + "Finished road conversion");
                    Bukkit.getScheduler().cancelTask(BukkitHybridUtils.this.task);
                    return;
                } else {
                    try {
                    	if (chunks.size() < 1024) {
                    	    if (regions.size() > 0) {
                        		final ChunkLoc loc = regions.get(0);
                                PlotSquared.log("&3Updating .mcr: " + loc.x + ", " + loc.z + " (aprrox 1024 chunks)");
                                PlotSquared.log(" - Remaining: " + regions.size());
                        		chunks.addAll(getChunks(loc));
                        		regions.remove(0);
                        		System.gc();
                    	    }
                    	}
                    	if (chunks.size() > 0) {
                    		long diff = System.currentTimeMillis() + 25;
                    		if (System.currentTimeMillis() - last > 1200 && last != 0) {
                    		    last = 0;
                    		    PlotSquared.log(C.PREFIX.s() + "Detected low TPS. Rescheduling in 30s");
                    		    while (chunks.size() > 0) {
                                    ChunkLoc chunk = chunks.get(0);
                                    chunks.remove(0);
                                    regenerateRoad(world, chunk, extend);
                                    ChunkManager.manager.unloadChunk(world, chunk);
                                }
                                Bukkit.getScheduler().cancelTask(BukkitHybridUtils.this.task);
                                TaskManager.runTaskLater(new Runnable() {
                                    @Override
                                    public void run() {
                                       scheduleRoadUpdate(world, regions, extend); 
                                    }
                                }, 2400);
                                return;
                    		}
                    		if (System.currentTimeMillis() - last < 1000) {
                        		while (System.currentTimeMillis() < diff && chunks.size() > 0) {
                        			ChunkLoc chunk = chunks.get(0);
                        			chunks.remove(0);
                        			regenerateRoad(world, chunk, extend);
                        			ChunkManager.manager.unloadChunk(world, chunk);
                        		}
                    		}
                    		last = System.currentTimeMillis();
                    	}
                    } catch (final Exception e) {
                        e.printStackTrace();
                        final ChunkLoc loc = regions.get(0);
                        PlotSquared.log("&c[ERROR]&7 Could not update '" + world + "/region/r." + loc.x + "." + loc.z + ".mca' (Corrupt chunk?)");
                        final int sx = loc.x << 5;
                        final int sz = loc.z << 5;
                        for (int x = sx; x < (sx + 32); x++) {
                            for (int z = sz; z < (sz + 32); z++) {
                                ChunkManager.manager.unloadChunk(world, new ChunkLoc(x, z));
                            }
                        }
                        PlotSquared.log("&d - Potentially skipping 1024 chunks");
                        PlotSquared.log("&d - TODO: recommend chunkster if corrupt");
                    }
                }
            }
        }, 20, 20);
        return true;
    }
}
