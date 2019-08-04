package com.github.intellectualsites.plotsquared.bukkit.util.block;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.object.ChunkWrapper;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import java.util.Arrays;

public class GenChunk extends ScopedLocalBlockQueue {

    public final Biome[] biomes;
    public PlotBlock[][] result;
    public BiomeGrid biomeGrid;
    public Chunk chunk;
    public String world;
    public int chunkX;
    public int chunkZ;
    @Getter @Setter private ChunkData chunkData = null;

    public GenChunk() {
        super(null, new Location(null, 0, 0, 0), new Location(null, 15, 255, 15));
        this.biomes = Biome.values();
    }

    public Chunk getChunk() {
        if (chunk == null) {
            World worldObj = BukkitUtil.getWorld(world);
            if (worldObj != null) {
                this.chunk = worldObj.getChunkAt(chunkX, chunkZ);
            }
        }
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public void setChunk(ChunkWrapper wrap) {
        chunk = null;
        world = wrap.world;
        chunkX = wrap.x;
        chunkZ = wrap.z;
    }

    @Override public void fillBiome(String biomeName) {
        if (biomeGrid == null) {
            return;
        }
        Biome biome = Biome.valueOf(biomeName.toUpperCase());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                this.biomeGrid.setBiome(x, z, biome);
            }
        }
    }

    @Override public void setCuboid(Location pos1, Location pos2, PlotBlock block) {
        if (result != null && pos1.getX() == 0 && pos1.getZ() == 0 && pos2.getX() == 15
            && pos2.getZ() == 15) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                int layer = y >> 4;
                PlotBlock[] data = result[layer];
                if (data == null) {
                    result[layer] = data = new PlotBlock[4096];
                }
                int start = y << 8;
                int end = start + 256;
                Arrays.fill(data, start, end, block);
            }
        }
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        chunkData
            .setRegion(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1, block.to(Material.class));
    }

    @Override public boolean setBiome(int x, int z, String biome) {
        return setBiome(x, z, Biome.valueOf(biome.toUpperCase()));
    }

    public boolean setBiome(int x, int z, Biome biome) {
        if (this.biomeGrid != null) {
            this.biomeGrid.setBiome(x, z, biome);
            return true;
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, PlotBlock id) {
        if (this.result == null) {
            this.chunkData.setBlock(x, y, z, id.to(Material.class));
            return true;
        }
        this.chunkData.setBlock(x, y, z, id.to(Material.class));
        this.storeCache(x, y, z, id);
        return true;
    }

    private void storeCache(final int x, final int y, final int z, final PlotBlock id) {
        int i = MainUtil.CACHE_I[y][x][z];
        PlotBlock[] v = this.result[i];
        if (v == null) {
            this.result[i] = v = new PlotBlock[4096];
        }
        int j = MainUtil.CACHE_J[y][x][z];
        v[j] = id;
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        if (this.result == null) {
            this.chunkData.setBlock(x, y, z, BukkitAdapter.adapt(id));
            return true;
        }
        this.chunkData.setBlock(x, y, z, BukkitAdapter.adapt(id));
        this.storeCache(x, y, z, PlotBlock.get(id.getBlockType().getId()));
        return true;
    }

    @Override public PlotBlock getBlock(int x, int y, int z) {
        int i = MainUtil.CACHE_I[y][x][z];
        if (result == null) {
            return PlotBlock.get(chunkData.getType(x, y, z));
        }
        PlotBlock[] array = result[i];
        if (array == null) {
            return PlotBlock.get("");
        }
        int j = MainUtil.CACHE_J[y][x][z];
        return array[j];
    }

    public int getX() {
        return chunk == null ? chunkX : chunk.getX();
    }

    public int getZ() {
        return chunk == null ? chunkZ : chunk.getZ();
    }

    @Override public String getWorld() {
        return chunk == null ? world : chunk.getWorld().getName();
    }

    @Override public Location getMax() {
        return new Location(getWorld(), 15 + (getX() << 4), 255, 15 + (getZ() << 4));
    }

    @Override public Location getMin() {
        return new Location(getWorld(), getX() << 4, 0, getZ() << 4);
    }

    public GenChunk clone() {
        GenChunk toReturn = new GenChunk();
        if (this.result != null) {
            for (int i = 0; i < this.result.length; i++) {
                PlotBlock[] matrix = this.result[i];
                if (matrix != null) {
                    toReturn.result[i] = new PlotBlock[matrix.length];
                    System.arraycopy(matrix, 0, toReturn.result[i], 0, matrix.length);
                }
            }
        }
        toReturn.chunkData = this.chunkData;
        return toReturn;
    }
}
