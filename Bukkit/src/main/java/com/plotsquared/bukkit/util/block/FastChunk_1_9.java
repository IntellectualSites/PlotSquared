package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.Arrays;

public class FastChunk_1_9 extends PlotChunk<Chunk> {

    public int[][] ids;
    public short[] count;
    public short[] air;
    public short[] relight;
    public int[][] biomes;
    public Chunk chunk;
    public FastChunk_1_9(final ChunkWrapper chunk) {
        super(chunk);
        ids = new int[16][];
        count = new short[16];
        air = new short[16];
        relight = new short[16];
    }

    @Override
    public Chunk getChunkAbs() {
        ChunkWrapper loc = getChunkWrapper();
        return BukkitUtil.getWorld(loc.world).getChunkAt(loc.x, loc.z);
    }

    @Override
    public Chunk getChunk() {
        if (chunk == null) {
            final ChunkWrapper cl = getChunkWrapper();
            chunk = Bukkit.getWorld(cl.world).getChunkAt(cl.x, cl.z);
        }
        return chunk;
    }

    @Override
    public void setChunkWrapper(final ChunkWrapper loc) {
        super.setChunkWrapper(loc);
        chunk = null;
    }

    /**
     * Get the number of block changes in a specified section
     * @param i
     * @return
     */
    public int getCount(final int i) {
        return count[i];
    }

    public int getAir(final int i) {
        return air[i];
    }

    public void setCount(int i, short value) {
        count[i] = value;
    }

    /**
     * Get the number of block changes in a specified section
     * @param i
     * @return
     */
    public int getRelight(final int i) {
        return relight[i];
    }

    public int getTotalCount() {
        int total = 0;
        for (int i = 0; i < 16; i++) {
            total += count[i];
        }
        return total;
    }

    public int getTotalRelight() {
        if (getTotalCount() == 0) {
            Arrays.fill(count, (short) 1);
            Arrays.fill(relight, Short.MAX_VALUE);
            return Short.MAX_VALUE;
        }
        int total = 0;
        for (int i = 0; i < 16; i++) {
            total += relight[i];
        }
        return total;
    }

    /**
     * Get the raw data for a section
     * @param i
     * @return
     */
    public int[] getIdArray(final int i) {
        return ids[i];
    }

    public int[][] getIdArrays() {
        return ids;
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final int id, byte data) {
        final int i = MainUtil.CACHE_I[y][x][z];
        final int j = MainUtil.CACHE_J[y][x][z];
        int[] vs = ids[i];
        if (vs == null) {
            vs = ids[i] = new int[4096];
            count[i]++;
        } else if (vs[j] == 0) {
            count[i]++;
        }
        switch (id) {
            case 0:
                air[i]++;
                vs[j] = -1;
                return;
            case 10:
            case 11:
            case 39:
            case 40:
            case 51:
            case 74:
            case 89:
            case 122:
            case 124:
            case 138:
            case 169:
                relight[i]++;
            case 2:
            case 4:
            case 13:
            case 14:
            case 15:
            case 20:
            case 21:
            case 22:
            case 30:
            case 32:
            case 37:
            case 41:
            case 42:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 55:
            case 56:
            case 57:
            case 58:
            case 60:
            case 7:
            case 8:
            case 9:
            case 73:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 85:
            case 87:
            case 88:
            case 101:
            case 102:
            case 103:
            case 110:
            case 112:
            case 113:
            case 121:
            case 129:
            case 133:
            case 165:
            case 166:
            case 170:
            case 172:
            case 173:
            case 174:
            case 181:
            case 182:
            case 188:
            case 189:
            case 190:
            case 191:
            case 192:
                vs[j] = (id);
                return;
            case 130:
            case 76:
            case 62:
                relight[i]++;
            case 54:
            case 146:
            case 61:
            case 65:
            case 68:
            case 50:
                if (data < 2) {
                    data = 2;
                }
            default:
                vs[j] = id + (data << 12);
                return;
        }
    }

    @Override
    public PlotChunk clone() {
        FastChunk_1_9 toReturn = new FastChunk_1_9(getChunkWrapper());
        toReturn.air = air.clone();
        toReturn.count = count.clone();
        toReturn.relight = relight.clone();
        toReturn.ids = new int[ids.length][];
        for (int i = 0; i < ids.length; i++) {
            int[] matrix = ids[i];
            if (matrix != null) {
                toReturn.ids[i] = new int[matrix.length];
                System.arraycopy(matrix, 0, toReturn.ids[i], 0, matrix.length);
            }
        }
        return toReturn;
    }

    @Override
    public PlotChunk shallowClone() {
        FastChunk_1_9 toReturn = new FastChunk_1_9(getChunkWrapper());
        toReturn.air = air;
        toReturn.count = count;
        toReturn.relight = relight;
        toReturn.ids = ids;
        return toReturn;
    }

    @Override
    public void setBiome(int x, int z, int biome) {
        if (biomes == null) {
            biomes = new int[16][16];
        }
        biomes[x][z] = biome;
    }
}
