package com.intellectualcrafters.plot.util;

import org.bukkit.World;

import com.intellectualcrafters.plot.object.PlotBlock;

public abstract class BlockManager {
    
    private static BlockManager manager = new BukkitUtil();
    
    private static long state = 1;
    
    public static long nextLong() {
        final long a = state;
        state = xorShift64(a);
        return a;
    }

    public static long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public static int random(final int n) {
        if (n == 1) {
            return 0;
        }
        final long r = ((nextLong() >>> 32) * n) >> 32;
        return (int) r;
    }
    
    public abstract void functionSetBlock(String worldname, int[] x, int[] y, int[] z, int[] id, byte[] data);
    
    public abstract void setSign(String worldname, int x, int y, int z);
    
    
    public static void setBlocks(String worldname, int[] x, int y[], int z[], PlotBlock[][] blocks) {
        int[] id = new int[blocks.length];
        byte[] data = new byte[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            PlotBlock[] current = blocks[i];
            int n = random(current.length);
            id[i] = current[n].id;
            data[i] = current[n].data;
        }
        setBlocks(worldname, x, y, z, id, data);
    }
    
    public static void setBlocks(String worldname, int[] x, int y[], int z[], PlotBlock[] blocks) {
        int[] id = new int[blocks.length];
        byte[] data = new byte[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            PlotBlock current = blocks[i];
            id[i] = current.id;
            data[i] = current.data;
        }
        setBlocks(worldname, x, y, z, id, data);
    }
    
    public static void setBlocks(String worldname, int[] x, int[] y, int[] z, int[] id, byte[] data) {
    }
}
