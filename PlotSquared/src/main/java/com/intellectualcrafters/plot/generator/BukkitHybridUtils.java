package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.AChunkManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.ChunkManager;
import com.intellectualcrafters.plot.util.bukkit.SetBlockManager;

public class BukkitHybridUtils extends HybridUtils {

    @Override
    public int checkModified(int threshhold, String worldname, int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock[] blocks) {
        World world = BukkitUtil.getWorld(worldname);
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
                        if (count > threshhold) {
                            return -1;
                        }
                    }
                }
            }
        }
        return count;
    }

    @Override
    public int get_ey(String worldname, int sx, int ex, int sz, int ez, int sy) {
        World world = BukkitUtil.getWorld(worldname);
        int maxY = world.getMaxHeight();
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

    @Override
    public void regenerateChunkChunk(String worldname, ChunkLoc loc) {
        World world = BukkitUtil.getWorld(worldname);
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
                regenerateRoad(worldname, new ChunkLoc(x,z));
            }
        }
        SetBlockManager.setBlockManager.update(chunks2);
    }

    private static boolean UPDATE = false;
    private int task;
    
    @Override
    public boolean scheduleRoadUpdate(final String world) {
        if (BukkitHybridUtils.UPDATE) {
            return false;
        }
        final List<ChunkLoc> chunks = AChunkManager.manager.getChunkChunks(world);
        final Plugin plugin = (Plugin) BukkitMain.THIS;
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (chunks.size() == 0) {
                    BukkitHybridUtils.UPDATE = false;
                    PlotSquared.log(C.PREFIX.s() + "Finished road conversion");
                    Bukkit.getScheduler().cancelTask(BukkitHybridUtils.this.task);
                    return;
                } else {
                    try {
                        final ChunkLoc loc = chunks.get(0);
                        PlotSquared.log("Updating .mcr: " + loc.x + ", " + loc.z + " (aprrox 256 chunks)");
                        PlotSquared.log("Remaining regions: " + chunks.size());
                        regenerateChunkChunk(world, loc);
                        chunks.remove(0);
                    } catch (final Exception e) {
                        final ChunkLoc loc = chunks.get(0);
                        PlotSquared.log("&c[ERROR]&7 Could not update '" + world + "/region/r." + loc.x + "." + loc.z + ".mca' (Corrupt chunk?)");
                        PlotSquared.log("&d - Potentially skipping 256 chunks");
                        PlotSquared.log("&d - TODO: recommend chunkster if corrupt");
                    }
                }
            }
        }, 20, 20);
        return true;
    }
    
}
