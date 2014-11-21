package com.intellectualcrafters.plot.util;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;

public class SendChunk {
    
    private static final RefClass classWorld = getRefClass("{nms}.World");
    private static final RefClass classEntityPlayer = getRefClass("{nms}.EntityPlayer");
    private static final RefClass classChunkCoordIntPair = getRefClass("{nms}.ChunkCoordIntPair");
    private static final RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
    private static final RefClass classChunk = getRefClass("{nms}.Chunk");
    
    private static RefMethod methodGetHandle;
    
    
    private static RefField chunkCoordIntPairQueue;
    private static RefField players;
    private static RefField locX;
    private static RefField locZ;
    private static RefField world;
    
    private static RefConstructor ChunkCoordIntPairCon;
    
    public SendChunk() throws NoSuchMethodException {
        methodGetHandle = classCraftChunk.getMethod("getHandle");
        chunkCoordIntPairQueue = classEntityPlayer.getField("chunkCoordIntPairQueue");
        
        players = classWorld.getField("players");
        locX = classEntityPlayer.getField("locX");
        locZ = classEntityPlayer.getField("locZ");
        
        world = classChunk.getField("world");
        
        ChunkCoordIntPairCon = classChunkCoordIntPair.getConstructor(int.class, int.class);
    }
    
    public static void sendChunk(ArrayList<Chunk> chunks) {
        int diffx, diffz;
        int view = Bukkit.getServer().getViewDistance() << 4;
        for (Chunk chunk : chunks) {
            final Object c = methodGetHandle.of(chunk).call();
            
            final Object w = world.of(c).get();
            final Object p = players.of(w).get();
            
            for (Object ep : (List<Object>) p) {
                int x = ((Double) locX.of(ep).get()).intValue();
                int z = ((Double) locZ.of(ep).get()).intValue();
                diffx = Math.abs(x - (chunk.getX() << 4));
                diffz = Math.abs(z - (chunk.getZ() << 4));
                if (diffx <= view && diffz <= view) {
                    System.out.print("PLAYER ");
                    Object pair = ChunkCoordIntPairCon.create(chunk.getX(), chunk.getZ());
                    Object pq = chunkCoordIntPairQueue.of(ep).get();
                    ((List) pq).add(pair);
                }
                else {
                    System.out.print("NOT P "+diffx+ " | "+diffz+" | "+view);
                    System.out.print("2 "+x+ " | "+z+" | ");
                    System.out.print("3 "+(chunk.getX() << 4)+ " | "+(chunk.getZ() << 4)+" | ");
                }
            }
        }
    }
}
