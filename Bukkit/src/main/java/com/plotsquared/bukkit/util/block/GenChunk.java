package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.block.ScopedLocalBlockQueue;
import com.plotsquared.bukkit.util.BukkitUtil;

import java.util.Arrays;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.material.MaterialData;

public class GenChunk extends ScopedLocalBlockQueue {

    private final Biome[] biomes;
    public short[][] result;
    public byte[][] result_data;
    public ChunkData cd;
    public BiomeGrid grid;

    public Chunk chunk;
    public String world;
    private int cx;
    private int cz;

    public GenChunk(Chunk chunk, ChunkWrapper wrap) {
        super(null, new Location(null, 0, 0, 0), new Location(null, 15, 255, 15));
        this.biomes = Biome.values();
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public void setChunk(ChunkWrapper wrap) {
        chunk = null;
        world = wrap.world;
        cx = wrap.x;
        cz = wrap.z;
    }

    public Chunk getChunk() {
        if (chunk == null) {
            World worldObj = BukkitUtil.getWorld(world);
            if (worldObj != null) {
                this.chunk = worldObj.getChunkAt(cx, cz);
            }
        }
        return chunk;
    }

    public ChunkWrapper getChunkWrapper() {
        if (chunk == null) {
            return new ChunkWrapper(world, cx, cz);
        }
        return new ChunkWrapper(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    @Override public void fillBiome(String biomeName) {
        if (grid == null) {
            return;
        }
        Biome biome = Biome.valueOf(biomeName.toUpperCase());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                this.grid.setBiome(x, z, biome);
            }
        }
    }

    @Override public void setCuboid(Location pos1, Location pos2, PlotBlock block) {
        if (block.data == 0 && result != null && pos1.getX() == 0 && pos1.getZ() == 0
            && pos2.getX() == 15 && pos2.getZ() == 15) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                int layer = y >> 4;
                short[] data = result[layer];
                if (data == null) {
                    result[layer] = data = new short[4096];
                }
                int start = y << 8;
                int end = start + 256;
                Arrays.fill(data, start, end, block.id);
            }
        } else {
            super.setCuboid(pos1, pos2, block);
        }
    }

    @Override public boolean setBiome(int x, int z, String biome) {
        return setBiome(x, z, Biome.valueOf(biome.toUpperCase()));
    }

    public boolean setBiome(int x, int z, int biome) {
        if (this.grid != null) {
            this.grid.setBiome(x, z, this.biomes[biome]);
            return true;
        }
        return false;
    }

    public boolean setBiome(int x, int z, Biome biome) {
        if (this.grid != null) {
            this.grid.setBiome(x, z, biome);
            return true;
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, int id, int data) {
        if (this.result == null) {
            Material material = Material.getMaterial(id);
            if (material == null) {
                return false;
            }
            this.cd.setBlock(x, y, z, new MaterialData(material, (byte) data));
            return true;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        short[] v = this.result[i];
        if (v == null) {
            this.result[i] = v = new short[4096];
        }
        int j = MainUtil.CACHE_J[y][x][z];
        v[j] = (short) id;
        if (data != 0) {
            byte[] vd = this.result_data[i];
            if (vd == null) {
                this.result_data[i] = vd = new byte[4096];
            }
            vd[j] = (byte) data;
        }
        return true;
    }

    @Override public PlotBlock getBlock(int x, int y, int z) {
        int i = MainUtil.CACHE_I[y][x][z];
        if (result == null) {
            MaterialData md = cd.getTypeAndData(x, y, z);
            return PlotBlock.get(md.getItemTypeId(), md.getData());
        }
        short[] array = result[i];
        if (array == null) {
            return PlotBlock.get(0, 0);
        }
        int j = MainUtil.CACHE_J[y][x][z];
        short id = array[j];
        if (id == 0) {
            return PlotBlock.get(id, 0);
        }
        byte[] dataArray = result_data[i];
        if (dataArray == null) {
            return PlotBlock.get(id, 0);
        }
        return PlotBlock.get(id, dataArray[j]);
    }

    public int getX() {
        if (chunk == null) {
            return cx;
        } else {
            return chunk.getX();
        }
    }

    public int getZ() {
        if (chunk == null) {
            return cz;
        } else {
            return chunk.getZ();
        }
    }

    @Override public String getWorld() {
        if (chunk == null) {
            return world;
        } else {
            return chunk.getWorld().getName();
        }
    }

    @Override public Location getMax() {
        return new Location(getWorld(), 15 + (getX() << 4), 255, 15 + (getZ() << 4));
    }

    @Override public Location getMin() {
        return new Location(getWorld(), getX() << 4, 0, getZ() << 4);
    }

    public GenChunk clone() {
        GenChunk toReturn =
            new GenChunk(chunk, new ChunkWrapper(getWorld(), chunk.getX(), chunk.getZ()));
        if (this.result != null) {
            for (int i = 0; i < this.result.length; i++) {
                short[] matrix = this.result[i];
                if (matrix != null) {
                    toReturn.result[i] = new short[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result[i], 0, matrix.length);
                }
            }
            for (int i = 0; i < this.result_data.length; i++) {
                byte[] matrix = this.result_data[i];
                if (matrix != null) {
                    toReturn.result_data[i] = new byte[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result_data[i], 0, matrix.length);
                }
            }
        }
        toReturn.cd = this.cd;
        return toReturn;
    }

    public GenChunk shallowClone() {
        GenChunk toReturn = new GenChunk(chunk, new ChunkWrapper(getWorld(), getX(), getZ()));
        toReturn.result = this.result;
        toReturn.result_data = this.result_data;
        toReturn.cd = this.cd;
        return toReturn;
    }
}
