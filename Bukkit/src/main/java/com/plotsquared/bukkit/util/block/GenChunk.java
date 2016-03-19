package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class GenChunk extends PlotChunk<Chunk> {

    public final Biome[] biomes;
    public Chunk chunk;
    public short[][] result;
    public byte[][] result_data;
    public ChunkData cd;
    public BiomeGrid grid;

    public GenChunk(Chunk chunk, ChunkWrapper wrap) {
        super(wrap);
        if ((this.chunk = chunk) == null && wrap != null) {
            World world = BukkitUtil.getWorld(wrap.world);
            if (world != null) {
                chunk = world.getChunkAt(wrap.x, wrap.z);
            }
        }
        this.biomes = Biome.values();
    }

    @Override
    public Chunk getChunkAbs() {
        ChunkWrapper wrap = getChunkWrapper();
        if (chunk == null || wrap.x != chunk.getX() || wrap.z != chunk.getZ()) {
            chunk = BukkitUtil.getWorld(wrap.world).getChunkAt(wrap.x, wrap.z);
        }
        return chunk;
    }

    @Override
    public void setBiome(int x, int z, int biome) {
        grid.setBiome(x, z, biomes[biome]);
    }

    public void setBiome(int x, int z, Biome biome) {
        if (grid != null) {
            grid.setBiome(x, z, biome);
        }
    }

    @Override
    public void setBlock(int x, int y, int z, int id, byte data) {
        if (result == null) {
            cd.setBlock(x, y, z, id, data);
            return;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        short[] v = result[i];
        if (v == null) {
            result[i] = v = new short[4096];
        }
        int j = MainUtil.CACHE_J[y][x][z];
        v[j] = (short) id;
        if (data != 0) {
            byte[] vd = result_data[i];
            if (vd == null) {
                result_data[i] = vd = new byte[4096];
            }
            vd[j] = data;
        }
    }

    @Override
    public PlotChunk clone() {
        GenChunk toReturn = new GenChunk(getChunkAbs(), getChunkWrapper());
        if (result != null) {
            for (int i = 0; i < result.length; i++) {
                short[] matrix = result[i];
                if (matrix != null) {
                    toReturn.result[i] = new short[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result[i], 0, matrix.length);
                }
            }
            for (int i = 0; i < result_data.length; i++) {
                byte[] matrix = result_data[i];
                if (matrix != null) {
                    toReturn.result_data[i] = new byte[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result_data[i], 0, matrix.length);
                }
            }
        }
        toReturn.cd = cd;
        return toReturn;
    }

    @Override
    public PlotChunk shallowClone() {
        GenChunk toReturn = new GenChunk(getChunkAbs(), getChunkWrapper());
        toReturn.result = result;
        toReturn.result_data = result_data;
        toReturn.cd = cd;
        return toReturn;
    }
}
