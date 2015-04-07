package com.intellectualcrafters.plot.listeners;

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

public class ChunkListener implements Listener {
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        processChunk(event.getChunk());
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        processChunk(event.getChunk());
    }
    
    public void processChunk(Chunk chunk) {
        if (!PlotSquared.isPlotWorld(chunk.getWorld().getName())) {
            return;
        }
        Entity[] entities = chunk.getEntities();
        BlockState[] tiles = chunk.getTileEntities();
        if (tiles.length > Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES) {
            for (BlockState tile : tiles) {
                tile.getBlock().setType(Material.AIR, false);
            }
        }
        if (entities.length > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            for (Entity ent : entities) {
                ent.remove();
            }
        }
    }
}
