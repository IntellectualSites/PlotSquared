package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

/**
 * An utility that can be used to send chunks, rather than using bukkit code to do so (uses heavy NMS)
 *
 * @author Empire92
 */
public class SendChunk {

    // Ref Class
    private static final RefClass classWorld = getRefClass("{nms}.World");
    private static final RefClass classEntityPlayer = getRefClass("{nms}.EntityPlayer");
    private static final RefClass classChunkCoordIntPair = getRefClass("{nms}.ChunkCoordIntPair");
    private static final RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
    private static final RefClass classChunk = getRefClass("{nms}.Chunk");

    // Ref Method
    private static RefMethod methodGetHandle;

    // Ref Field
    private static RefField chunkCoordIntPairQueue;
    private static RefField players;
    private static RefField locX;
    private static RefField locZ;
    private static RefField world;

    // Ref Constructor
    private static RefConstructor ChunkCoordIntPairCon;

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

    public static void sendChunk(final ArrayList<Chunk> chunks) {
        int diffx, diffz;
        final int view = Bukkit.getServer().getViewDistance() << 4;
        for (final Chunk chunk : chunks) {
            final Object c = methodGetHandle.of(chunk).call();

            final Object w = world.of(c).get();
            final Object p = players.of(w).get();

            for (final Object ep : (List<Object>) p) {
                final int x = ((Double) locX.of(ep).get()).intValue();
                final int z = ((Double) locZ.of(ep).get()).intValue();
                diffx = Math.abs(x - (chunk.getX() << 4));
                diffz = Math.abs(z - (chunk.getZ() << 4));
                if ((diffx <= view) && (diffz <= view)) {
                    final Object pair = ChunkCoordIntPairCon.create(chunk.getX(), chunk.getZ());
                    final Object pq = chunkCoordIntPairQueue.of(ep).get();
                    ((List) pq).add(pair);
                }
            }
        }
    }
}
