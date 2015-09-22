package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.ChunkDataStream;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.SetBlockQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;

public class SpongeChunkManager extends ChunkManager {
    
    @Override
    public void setChunk(final ChunkWrapper loc, final PlotBlock[][] result) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public int[] countEntities(final Plot plot) {
        final Location pos1 = plot.getBottomAbs();
        final Location pos2 = plot.getTopAbs();
        
        final String worldname = pos1.getWorld();
        final World world = SpongeUtil.getWorld(worldname);
        final int bx = pos1.getX();
        final int bz = pos1.getZ();
        final int tx = pos2.getX();
        final int tz = pos2.getZ();
        final int[] count = new int[5];
        world.getEntities(new Predicate<Entity>() {
            @Override
            public boolean apply(final Entity entity) {
                final org.spongepowered.api.world.Location loc = entity.getLocation();
                final int x = loc.getBlockX();
                if ((x >= bx) && (x <= tx)) {
                    final int z = loc.getBlockZ();
                    if ((z >= bz) && (z <= tz)) {
                        count[0]++;
                        if (entity instanceof Living) {
                            count[3]++;
                            if (entity instanceof Animal) {
                                count[1]++;
                            } else if (entity instanceof Monster) {
                                count[2]++;
                            }
                        } else {
                            count[4]++;
                        }
                    }
                }
                return false;
            }
        });
        
        return count;
    }
    
    @Override
    public boolean loadChunk(final String world, final ChunkLoc loc, final boolean force) {
        final World worldObj = SpongeUtil.getWorld(world);
        return worldObj.loadChunk(loc.x << 4, 0, loc.z << 4, force).isPresent();
    }
    
    @Override
    public boolean unloadChunk(final String world, final ChunkLoc loc, final boolean save, final boolean safe) {
        final World worldObj = SpongeUtil.getWorld(world);
        final Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            return worldObj.unloadChunk(chunk.get());
        }
        return false;
    }
    
    @Override
    public Set<ChunkLoc> getChunkChunks(final String world) {
        final HashSet<ChunkLoc> chunks = new HashSet<ChunkLoc>();
        final World worldObj = SpongeUtil.getWorld(world);
        final ChunkDataStream storage = worldObj.getWorldStorage().getGeneratedChunks();
        while (storage.hasNext()) {
            storage.next();
            
            // TODO get chunk from DataContainer
        }
        return chunks;
    }
    
    @Override
    public void regenerateChunk(final String world, final ChunkLoc loc) {
        final World worldObj = SpongeUtil.getWorld(world);
        final Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        if (chunk.isPresent()) {
            // TODO regenerate chunk
        }
    }
    
    @Override
    public void deleteRegionFile(final String world, final ChunkLoc loc) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void deleteRegionFiles(final String world, final List<ChunkLoc> chunks) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void deleteRegionFiles(String world, List<ChunkLoc> chunks, Runnable whenDone) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public Plot hasPlot(final String world, final ChunkLoc chunk) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone) {
        // TODO Auto-generated method stub
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone) {
        // TODO Auto-generated method stub
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public void swap(final String world, final PlotId id, final PlotId plotid) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void swap(final String worldname, final Location bot1, final Location top1, final Location bot2, final Location top2) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void clearAllEntities(final Location pos1, final Location pos2) {
        final String worldname = pos1.getWorld();
        final World world = SpongeUtil.getWorld(worldname);
        final int bx = pos1.getX();
        final int bz = pos1.getZ();
        final int tx = pos2.getX();
        final int tz = pos2.getZ();
        world.getEntities(new Predicate<Entity>() {
            @Override
            public boolean apply(final Entity entity) {
                final org.spongepowered.api.world.Location loc = entity.getLocation();
                final int x = loc.getBlockX();
                if ((x >= bx) && (x <= tx)) {
                    final int z = loc.getBlockZ();
                    if ((z >= bz) && (z <= tz)) {
                        entity.remove();
                    }
                }
                return false;
            }
        });
    }
    
}
