package com.github.intellectualsites.plotsquared.bukkit.util.block;

import com.github.intellectualsites.plotsquared.bukkit.object.schematic.StateWrapper;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.LegacyPlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.StringPlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.BasicLocalBlockQueue;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.Locale;

public class BukkitLocalQueue<T> extends BasicLocalBlockQueue<T> {

    public BukkitLocalQueue(String world) {
        super(world);
    }

    @Override public LocalChunk<T> getLocalChunk(int x, int z) {
        return (LocalChunk<T>) new BasicLocalChunk(this, x, z) {
            // Custom stuff?
        };
    }

    @Override public void optimize() {

    }

    @Override public PlotBlock getBlock(int x, int y, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj != null) {
            Block block = worldObj.getBlockAt(x, y, z);
            return PlotBlock.get(block.getType().toString());
        } else {
            return PlotBlock.get(0, 0);
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

    @Override public final void setComponents(LocalChunk<T> lc) {
        setBaseBlocks(lc);
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(getWorld());
    }

    public Chunk getChunk(int x, int z) {
        return getBukkitWorld().getChunkAt(x, z);
    }

    public void setBaseBlocks(LocalChunk<T> lc) {
        World worldObj = Bukkit.getWorld(getWorld());
        Chunk chunk = worldObj.getChunkAt(lc.getX(), lc.getZ());
        chunk.load(true);
        for (int layer = 0; layer < lc.baseblocks.length; layer++) {
            BaseBlock[] blocksLayer = lc.baseblocks[layer];
            if (blocksLayer != null) {
                for (int j = 0; j < blocksLayer.length; j++) {
                    if (blocksLayer[j] != null) {
                        BaseBlock block = blocksLayer[j];
                        int x = MainUtil.x_loc[layer][j];
                        int y = MainUtil.y_loc[layer][j];
                        int z = MainUtil.z_loc[layer][j];

                        BlockData blockData = BukkitAdapter.adapt(block);

                        Block existing = chunk.getBlock(x, y, z);
                        if (equals(PlotBlock.get(block), existing) && existing.getBlockData()
                            .matches(blockData)) {
                            continue;
                        }

                        existing.setType(BukkitAdapter.adapt(block.getBlockType()), false);
                        existing.setBlockData(blockData, false);
                        if (block.hasNbtData()) {
                            CompoundTag tag = block.getNbtData();
                            StateWrapper sw = new StateWrapper(tag);

                            sw.restoreTag(worldObj.getName(), existing.getX(), existing.getY(),
                                existing.getZ());
                        }
                    }
                }
            }
        }
    }

    private void setMaterial(@NonNull final PlotBlock plotBlock, @NonNull final Block block) {
        final Material material;
        if (plotBlock instanceof StringPlotBlock) {
            material = Material
                .getMaterial(((StringPlotBlock) plotBlock).getItemId().toUpperCase(Locale.ENGLISH));
            if (material == null) {
                throw new IllegalStateException(String
                    .format("Could not find material that matches %s",
                        ((StringPlotBlock) plotBlock).getItemId()));
            }
        } else {
            final LegacyPlotBlock legacyPlotBlock = (LegacyPlotBlock) plotBlock;
            material = PlotSquared.get().IMP.getLegacyMappings()
                .fromLegacyToString(legacyPlotBlock.getId(), legacyPlotBlock.getData())
                .to(Material.class);
            if (material == null) {
                throw new IllegalStateException(String
                    .format("Could not find material that matches %s", legacyPlotBlock.toString()));
            }
        }
        block.setType(material, false);
    }

    private boolean equals(@NonNull final PlotBlock plotBlock, @NonNull final Block block) {
        if (plotBlock instanceof StringPlotBlock) {
            return ((StringPlotBlock) plotBlock).idEquals(block.getType().name());
        }
        final LegacyPlotBlock legacyPlotBlock = (LegacyPlotBlock) plotBlock;
        return Material.getMaterial(PlotSquared.get().IMP.getLegacyMappings()
            .fromLegacyToString(((LegacyPlotBlock) plotBlock).id,
                ((LegacyPlotBlock) plotBlock).data).toString()) == block.getType() && (
            legacyPlotBlock.id == 0 || legacyPlotBlock.data == block.getData());
    }

    public void setBiomes(LocalChunk<T> lc) {
        if (lc.biomes != null) {
            World worldObj = Bukkit.getWorld(getWorld());
            int bx = lc.getX() << 4;
            int bz = lc.getX() << 4;
            for (int x = 0; x < lc.biomes.length; x++) {
                String[] biomes2 = lc.biomes[x];
                if (biomes2 != null) {
                    for (String biomeStr : biomes2) {
                        if (biomeStr != null) {
                            Biome biome = Biome.valueOf(biomeStr.toUpperCase());
                            worldObj.setBiome(bx, bz, biome);
                        }
                    }
                }
            }
        }
    }

}
