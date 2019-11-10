package com.github.intellectualsites.plotsquared.bukkit.util.block;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitBlockUtil;
import com.github.intellectualsites.plotsquared.bukkit.object.schematic.StateWrapper;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.BasicLocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.world.BlockUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.lib.PaperLib;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.concurrent.ExecutionException;

public class BukkitLocalQueue extends BasicLocalBlockQueue {

    public BukkitLocalQueue(String world) {
        super(world);
    }

    @Override public LocalChunk getLocalChunk(int x, int z) {
        return new BasicLocalChunk(this, x, z) {
            // Custom stuff?
        };
    }

    @Override public void optimize() {

    }

    @Override public BlockState getBlock(int x, int y, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj != null) {
            Block block = worldObj.getBlockAt(x, y, z);
            return BukkitBlockUtil.get(block);
        } else {
            return BlockUtil.get(0, 0);
        }
    }

    @Override public void refreshChunk(int x, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj != null) {
            worldObj.refreshChunk(x, z);
        } else {
            PlotSquared.debug("Error Refreshing Chunk");
        }
    }

    @Override public void fixChunkLighting(int x, int z) {
        // Do nothing
    }

    @Override public final void regenChunk(int x, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj != null) {
            worldObj.regenerateChunk(x, z);
        } else {
            PlotSquared.debug("Error Regenerating Chunk");
        }
    }

    @Override public final void setComponents(LocalChunk lc)
        throws ExecutionException, InterruptedException {
        setBaseBlocks(lc);
    }

    public void setBaseBlocks(LocalChunk localChunk) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj == null) {
            throw new NullPointerException("World cannot be null.");
        }
        PaperLib.getChunkAtAsync(worldObj, localChunk.getX(), localChunk.getZ(), true)
            .thenAccept(chunk -> {
                for (int layer = 0; layer < localChunk.baseblocks.length; layer++) {
                    BaseBlock[] blocksLayer = localChunk.baseblocks[layer];
                    if (blocksLayer != null) {
                        for (int j = 0; j < blocksLayer.length; j++) {
                            if (blocksLayer[j] != null) {
                                BaseBlock block = blocksLayer[j];
                                int x = MainUtil.x_loc[layer][j];
                                int y = MainUtil.y_loc[layer][j];
                                int z = MainUtil.z_loc[layer][j];

                                BlockData blockData = BukkitAdapter.adapt(block);

                                Block existing = chunk.getBlock(x, y, z);
                                if (BukkitBlockUtil.get(existing).equals(block) && existing
                                    .getBlockData().matches(blockData)) {
                                    continue;
                                }

                                existing.setType(BukkitAdapter.adapt(block.getBlockType()), false);
                                existing.setBlockData(blockData, false);
                                if (block.hasNbtData()) {
                                    CompoundTag tag = block.getNbtData();
                                    StateWrapper sw = new StateWrapper(tag);

                                    sw.restoreTag(worldObj.getName(), existing.getX(),
                                        existing.getY(), existing.getZ());
                                }
                            }
                        }
                    }
                }
            });
    }

    private void setMaterial(@NonNull final BlockState plotBlock, @NonNull final Block block) {
        Material material = BukkitAdapter.adapt(plotBlock.getBlockType());
        block.setType(material, false);
    }

    private boolean equals(@NonNull final BlockState plotBlock, @NonNull final Block block) {
        return plotBlock.equals(BukkitBlockUtil.get(block));
    }

    public void setBiomes(LocalChunk lc) {
        if (lc.biomes != null) {
            World worldObj = Bukkit.getWorld(getWorld());
            int bx = lc.getX() << 4;
            int bz = lc.getX() << 4;
            for (int x = 0; x < lc.biomes.length; x++) {
                BiomeType[] biomes2 = lc.biomes[x];
                if (biomes2 != null) {
                    for (BiomeType biomeStr : biomes2) {
                        if (biomeStr != null) {
                            Biome biome = BukkitAdapter.adapt(biomeStr);
                            worldObj.setBiome(bx, bz, biome);
                        }
                    }
                }
            }
        }
    }

}
