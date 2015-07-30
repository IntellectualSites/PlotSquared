package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.schematic.PlotItem;

public abstract class BlockManager {
    public static BlockManager manager;
    
    public abstract boolean isBlockSolid(PlotBlock block);
    
    public abstract StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name);
    
    public abstract String getClosestMatchingName(PlotBlock block);

    public abstract String[] getBiomeList();
    
    public abstract boolean addItems(String world, PlotItem items);

    public abstract int getBiomeFromString(String biome);

    public abstract PlotBlock getPlotBlockFromString(String block);

    public abstract int getHeighestBlock(String world, int x, int z);

    public abstract String getBiome(String world, int x, int z);
    
    public abstract PlotBlock getBlock(Location loc);

    public abstract Location getSpawn(String world);

    public abstract String[] getSign(Location loc);

    public abstract boolean isWorld(String world);

    public abstract void functionSetBlocks(String worldname, int[] x, int[] y, int[] z, int[] id, byte[] data);

    public abstract void functionSetSign(String worldname, int x, int y, int z, String[] lines);

    public abstract void functionSetBlock(String worldname, int x, int y, int z, int id, byte data);

    public abstract void functionSetBiomes(final String worldname, final int[] x, final int z[], final String biome);

    public static void setBiomes(final String worldname, final int[] x, final int z[], final String biome) {
        manager.functionSetBiomes(worldname, x, z, biome);
    }

    public static void setBlocks(final String worldname, final int[] x, final int y[], final int z[], final PlotBlock[][] blocks) {
        final int[] id = new int[blocks.length];
        final byte[] data = new byte[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            final PlotBlock[] current = blocks[i];
            final int n = MainUtil.random.random(current.length);
            id[i] = current[n].id;
            data[i] = current[n].data;
        }
        setBlocks(worldname, x, y, z, id, data);
    }

    public static void setBlocks(final String worldname, final int[] x, final int y[], final int z[], final PlotBlock[] blocks) {
        final int[] id = new int[blocks.length];
        final byte[] data = new byte[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            final PlotBlock current = blocks[i];
            id[i] = current.id;
            data[i] = current.data;
        }
        setBlocks(worldname, x, y, z, id, data);
    }

    public static void setSign(final String worldname, final int x, final int y, final int z, final String[] lines) {
        manager.functionSetSign(worldname, x, y, z, lines);
    }

    public static void setBlocks(final String worldname, final int[] x, final int[] y, final int[] z, final int[] id, final byte[] data) {
        manager.functionSetBlocks(worldname, x, y, z, id, data);
    }
}
