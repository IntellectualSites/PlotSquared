package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.TaskManager;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;

public class SpongeChunkManager extends ChunkManager {
    
    @Override
    public int[] countEntities(Plot plot) {
        Location pos1 = plot.getBottomAbs();
        Location pos2 = plot.getTopAbs();
        World world = SpongeUtil.getWorld(pos1.getWorld());
        int bx = pos1.getX();
        int bz = pos1.getZ();
        int tx = pos2.getX();
        int tz = pos2.getZ();
        int[] count = new int[6];
        world.getEntities(entity -> {
            org.spongepowered.api.world.Location loc = entity.getLocation();
            int x = loc.getBlockX();
            if ((x >= bx) && (x <= tx)) {
                int z = loc.getBlockZ();
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
        });
        
        return count;
    }
    
    @Override
    public boolean loadChunk(String world, ChunkLoc loc, boolean force) {
        World worldObj = SpongeUtil.getWorld(world);
        return worldObj.loadChunk(loc.x << 4, 0, loc.z << 4, force).isPresent();
    }
    
    @Override
    public Set<ChunkLoc> getChunkChunks(String world) {
        // TODO save world;
        return super.getChunkChunks(world);
    }

    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos, Runnable whenDone) {
        // TODO copy a region
        TaskManager.runTask(whenDone);
        return false;
    }
    
    @Override
    public void clearAllEntities(Location pos1, Location pos2) {
        String worldName = pos1.getWorld();
        World world = SpongeUtil.getWorld(worldName);
        int bx = pos1.getX();
        int bz = pos1.getZ();
        int tx = pos2.getX();
        int tz = pos2.getZ();
        world.getEntities(entity -> {
            org.spongepowered.api.world.Location loc = entity.getLocation();
            int x = loc.getBlockX();
            if ((x >= bx) && (x <= tx)) {
                int z = loc.getBlockZ();
                if ((z >= bz) && (z <= tz)) {
                    if (!(entity instanceof Player)) {
                        entity.remove();
                    }
                }
            }
            return false;
        });
    }
    
    @Override
    public void swap(Location bot1, Location top1, Location bot2, Location top2, Runnable whenDone) {
        // TODO swap region
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public void unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe) {
        World worldObj = SpongeUtil.getWorld(world);
        Optional<Chunk> chunk = worldObj.getChunk(loc.x << 4, 0, loc.z << 4);
        chunk.ifPresent(worldObj::unloadChunk);
    }
    
    @Override
    public boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment, Runnable whenDone) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
}
