package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;
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
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
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
	
    @Override
    public void analyzePlot(Plot plot, RunnableVal<PlotAnalysis> whenDone) {
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
        World world = Bukkit.getWorld(plot.world);
        ChunkGenerator gen = world.getGenerator();
        BiomeGrid base = new BiomeGrid() { @Override public void setBiome(int a, int b, Biome c) {} @Override public Biome getBiome(int a, int b) {return null;}};
        final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        int bx = bot.getX() >> 4;
        int bz = bot.getZ() >> 4;
        int tx = top.getX() >> 4;
        int tz = top.getZ() >> 4;
        Random r = new Random();
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                Chunk chunk = world.getChunkAt(X, Z);
                short[][] result = gen.generateExtBlockSections(world, r, x, z, base);
                for (int x = 0; x < 16; x++) {
                    
                }
            }
        }
        
        short[] [] result = gen.generateExtBlockSections(world, null, x, z, )
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
