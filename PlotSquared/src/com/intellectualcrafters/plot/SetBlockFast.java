package com.intellectualcrafters.plot;

import org.bukkit.entity.Player;

import static com.intellectualcrafters.plot.ReflectionUtils.*;

public class SetBlockFast {
    
    private static final RefClass classBlock = getRefClass("{nms}.Block");
    private static final RefClass classChunk = getRefClass("{nms}.Chunk");
    private static final RefClass classWorld = getRefClass("{nms}.World");
    private static final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
    
    private static final RefMethod methodGetHandle = classCraftWorld.getMethod("getHandle");
    private static final RefMethod methodGetChunkAt = classWorld.getMethod("getChunkAt",int.class,int.class);
    private static final RefMethod methodA = classChunk.getMethod("a",int.class,int.class,int.class,classBlock,int.class);
    private static final RefMethod methodGetById = classBlock.getMethod("getById", int.class);
    
    public SetBlockFast() {
        
    }

    public static boolean set(org.bukkit.World world, int x, int y, int z, int blockId, byte data) {
        Object w = methodGetHandle.of(world).call();
        Object chunk = methodGetChunkAt.of(w).call(x >> 4, z >> 4);
        Object block = methodGetById.of(null).call(blockId);
        methodA.of(chunk).call(x & 0x0f, y, z & 0x0f,block , data);
        return true;
    }
    
    public static void update(Player player) {
        for (int cx = -8; cx < 8; cx++) {
            for (int cz = -8; cz < 8; cz++) {
                player.getWorld().refreshChunk(player.getLocation().getChunk().getX() + cx, player.getLocation().getChunk().getZ() + cz);
            }
        }
    }
}
