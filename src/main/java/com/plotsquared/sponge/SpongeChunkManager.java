package com.plotsquared.sponge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.ChunkDataStream;
import org.spongepowered.api.world.storage.WorldStorage;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.SetBlockQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongeChunkManager extends ChunkManager {
    
    @Override
    public void setChunk(ChunkWrapper loc, PlotBlock[][] result) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public int[] countEntities(Plot plot) {
        // TODO Auto-generated method stub
        return new int[5];
    }
    
    @Override
    public boolean loadChunk(String world, ChunkLoc loc, boolean force) {
        World worldObj = SpongeUtil.getWorld(world);
        return worldObj.loadChunk(loc.x << 4, 0, loc.z << 4, force).isPresent();
    }
    
    @Override
    public boolean unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe) {
        World worldObj = SpongeUtil.getWorld(world);
        Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            return worldObj.unloadChunk(chunk.get());
        }
        return false;
    }
    
    @Override
    public List<ChunkLoc> getChunkChunks(String world) {
        ArrayList<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
        World worldObj = SpongeUtil.getWorld(world);
        ChunkDataStream storage = worldObj.getWorldStorage().getGeneratedChunks();
        while (storage.hasNext()) {
            DataContainer data = storage.next();
            
            // TODO get chunk from DataContainer
        }
        return chunks;
    }
    
    @Override
    public void regenerateChunk(String world, ChunkLoc loc) {
        World worldObj = SpongeUtil.getWorld(world);
        Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            // TODO regenerate chunk
        }
    }
    
    @Override
    public void deleteRegionFile(String world, ChunkLoc loc) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void deleteRegionFiles(String world, List<ChunkLoc> chunks) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Plot hasPlot(String world, ChunkLoc chunk) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos, Runnable whenDone) {
        // TODO Auto-generated method stub
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public boolean regenerateRegion(Location pos1, Location pos2, Runnable whenDone) {
        // TODO Auto-generated method stub
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public void clearAllEntities(Plot plot) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void swap(String world, PlotId id, PlotId plotid) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void swap(String worldname, Location bot1, Location top1, Location bot2, Location top2) {
        // TODO Auto-generated method stub
        
    }
    
}
