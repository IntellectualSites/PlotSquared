package com.intellectualcrafters.plot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.util.TaskManager;

public class ChunkListener implements Listener {
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (processChunk(event.getChunk(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        processChunk(event.getChunk(), false);
    }

    public void cleanChunk(final Chunk chunk) {
        TaskManager.index.increment();
        final Integer currentIndex = TaskManager.index.toInteger();
        final Integer task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (!chunk.isLoaded()) {
                    Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                    TaskManager.tasks.remove(currentIndex);
                    PlotSquared.log("&aSuccessfully processed and unloaded chunk!");
                    chunk.unload(true, true);
                    return;
                }
                BlockState[] tiles = chunk.getTileEntities();
                if (tiles.length == 0) {
                    Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                    TaskManager.tasks.remove(currentIndex);
                    PlotSquared.log("&aSuccessfully processed and unloaded chunk!");
                    chunk.unload(true, true);
                    return;
                }
                long start = System.currentTimeMillis();
                int i = 0;
                while (System.currentTimeMillis() - start < 250) {
                    if (i >= tiles.length) {
                        Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                        TaskManager.tasks.remove(currentIndex);
                        PlotSquared.log("&aSuccessfully processed and unloaded chunk!");
                        chunk.unload(true, true);
                        return;
                    }
                    tiles[i].getBlock().setType(Material.AIR, false);
                    i++;
                }
            }
        }, 5);
        TaskManager.tasks.put(currentIndex, task);
    }
    
    public boolean processChunk(Chunk chunk, boolean unload) {
        if (!PlotSquared.isPlotWorld(chunk.getWorld().getName())) {
            return false;
        }
        Entity[] entities = chunk.getEntities();
        BlockState[] tiles = chunk.getTileEntities();
        if (entities.length > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            for (Entity ent : entities) {
                ent.remove();
            }
            PlotSquared.log("&aPlotSquared detected unsafe chunk and processed: " + (chunk.getX() << 4) + "," + (chunk.getX() << 4));
        }
        if (tiles.length > Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES) {
            if (unload) {
                PlotSquared.log("&cPlotSquared detected unsafe chunk: " + (chunk.getX() << 4) + "," + (chunk.getX() << 4));
                cleanChunk(chunk);
                return true;
            }
            for (BlockState tile : tiles) {
                tile.getBlock().setType(Material.AIR, false);
            }
        }
        return false;
    }
}
