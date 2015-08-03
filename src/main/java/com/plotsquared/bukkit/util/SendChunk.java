package com.plotsquared.bukkit.util;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;

/**
 * An utility that can be used to send chunks, rather than using bukkit code to do so (uses heavy NMS)
 *
 * @author Empire92
 */
public class SendChunk {
    
    // Ref Class
    private final RefClass classWorld = getRefClass("{nms}.World");
    private final RefClass classEntityPlayer = getRefClass("{nms}.EntityPlayer");
    private final RefClass classChunkCoordIntPair = getRefClass("{nms}.ChunkCoordIntPair");
    private final RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
    private final RefClass classChunk = getRefClass("{nms}.Chunk");
    private boolean v1_7_10 = PS.get().checkVersion(PS.get().IMP.getServerVersion(), 1, 7, 10) && !PS.get().checkVersion(PS.get().IMP.getServerVersion(), 1, 8, 0);
    // Ref Method
    private RefMethod methodGetHandle;
    // Ref Field
    private RefField chunkCoordIntPairQueue;
    private RefField players;
    private RefField locX;
    private RefField locZ;
    private RefField world;
    // Ref Constructor
    private RefConstructor ChunkCoordIntPairCon;

    /**
     * Constructor
     *
     * @throws NoSuchMethodException
     */
    public SendChunk() throws NoSuchMethodException {
        methodGetHandle = classCraftChunk.getMethod("getHandle");
        chunkCoordIntPairQueue = classEntityPlayer.getField("chunkCoordIntPairQueue");
        players = classWorld.getField("players");
        locX = classEntityPlayer.getField("locX");
        locZ = classEntityPlayer.getField("locZ");
        world = classChunk.getField("world");
        ChunkCoordIntPairCon = classChunkCoordIntPair.getConstructor(int.class, int.class);
    }

    public void sendChunk(final Collection<Chunk> chunks) {
        int diffx, diffz;
        final int view = Bukkit.getServer().getViewDistance() << 4;
        for (final Chunk chunk : chunks) {
            if (!chunk.isLoaded()) {
                continue;
            }
            boolean unload = true;
            final Object c = methodGetHandle.of(chunk).call();
            final Object w = world.of(c).get();
            final Object p = players.of(w).get();
            for (final Object ep : (List<Object>) p) {
                final int x = ((Double) locX.of(ep).get()).intValue();
                final int z = ((Double) locZ.of(ep).get()).intValue();
                diffx = Math.abs(x - (chunk.getX() << 4));
                diffz = Math.abs(z - (chunk.getZ() << 4));
                if ((diffx <= view) && (diffz <= view)) {
                    unload = false;
                    if (v1_7_10) {
                        chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                        chunk.load(true);
                    }
                    else {
                        final Object pair = ChunkCoordIntPairCon.create(chunk.getX(), chunk.getZ());
                        final Object pq = chunkCoordIntPairQueue.of(ep).get();
                        ((List) pq).add(pair);
                    }
                }
            }
            if (unload) {
                chunk.unload(true, true);
            }
        }
    }

    public void sendChunk(final String worldname, final List<ChunkLoc> locs) {
        final World myworld = Bukkit.getWorld(worldname);
        final ArrayList<Chunk> chunks = new ArrayList<>();
        for (final ChunkLoc loc : locs) {
            chunks.add(myworld.getChunkAt(loc.x, loc.z));
        }
        sendChunk(chunks);
    }
}
