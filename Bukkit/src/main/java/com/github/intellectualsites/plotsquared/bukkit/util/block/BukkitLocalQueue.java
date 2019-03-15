package com.github.intellectualsites.plotsquared.bukkit.util.block;

import com.github.intellectualsites.plotsquared.bukkit.object.schematic.StateWrapper;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.LegacyPlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.StringPlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class BukkitLocalQueue<T> extends BasicLocalBlockQueue<T> {

    private Field fieldNeighbors;
    private Method chunkGetHandle;

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
        Block block = worldObj.getBlockAt(x, y, z);
        if (block == null) {
            return PlotBlock.get(0, 0);
        }
        // int id = block.getTypeId();
        // if (id == 0) {
        //     return PlotBlock.get(0, 0);
        // }
        // return PlotBlock.get(id, block.getData());
        return PlotBlock.get(block.getType().toString());
    }

    @Override public void refreshChunk(int x, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        worldObj.refreshChunk(x, z);
    }

    @Override public void fixChunkLighting(int x, int z) {
        // Do nothing
    }

    @Override public final void regenChunk(int x, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        worldObj.regenerateChunk(x, z);
    }

    @Override public final void setComponents(LocalChunk<T> lc) {
        if (isBaseBlocks()) {
            setBaseBlocks(lc);
        } else {
            setBlocks(lc);
        }
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(getWorld());
    }

    public Chunk getChunk(int x, int z) {
        return getBukkitWorld().getChunkAt(x, z);
    }

    public void setBlocks(LocalChunk<T> lc) {
        World worldObj = Bukkit.getWorld(getWorld());
        Chunk chunk = worldObj.getChunkAt(lc.getX(), lc.getZ());
        chunk.load(true);
        for (int layer = 0; layer < lc.blocks.length; layer++) {
            PlotBlock[] blocksLayer = (PlotBlock[]) lc.blocks[layer];
            if (blocksLayer != null) {
                for (int j = 0; j < blocksLayer.length; j++) {
                    if (blocksLayer[j] != null) {
                        PlotBlock block = blocksLayer[j];
                        int x = MainUtil.x_loc[layer][j];
                        int y = MainUtil.y_loc[layer][j];
                        int z = MainUtil.z_loc[layer][j];
                        Block existing = chunk.getBlock(x, y, z);
                        if (equals(block, existing)) {
                            continue;
                        }
                        setMaterial(block, existing);
                    }
                }
            }
        }
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
            String last = null;
            Biome biome = null;
            for (int x = 0; x < lc.biomes.length; x++) {
                String[] biomes2 = lc.biomes[x];
                if (biomes2 != null) {
                    for (String biomeStr : biomes2) {
                        if (biomeStr != null) {
                            if (last == null || !StringMan.isEqual(last, biomeStr)) {
                                biome = Biome.valueOf(biomeStr.toUpperCase());
                            }
                            worldObj.setBiome(bx, bz, biome);
                        }
                    }
                }
            }
        }
    }

    /**
     * Exploiting a bug in the vanilla lighting algorithm for faster block placement
     * - Could have been achieved without reflection by force unloading specific chunks
     * - Much faster just setting the variable manually though
     *
     * @param chunk
     * @return
     */
    protected Object[] disableLighting(Chunk chunk) {
        try {
            if (chunkGetHandle == null) {
                chunkGetHandle = chunk.getClass().getDeclaredMethod("getHandle");
                chunkGetHandle.setAccessible(true);
            }
            Object nmsChunk = chunkGetHandle.invoke(chunk);
            if (fieldNeighbors == null) {
                fieldNeighbors = nmsChunk.getClass().getDeclaredField("neighbors");
                fieldNeighbors.setAccessible(true);
            }
            Object value = fieldNeighbors.get(nmsChunk);
            fieldNeighbors.set(nmsChunk, 0);
            return new Object[] {nmsChunk, value};
        } catch (Throwable ignore) {
        }
        return null;
    }

    protected void disableLighting(Object[] disableResult) {
        if (disableResult != null) {
            try {
                fieldNeighbors.set(disableResult[0], 0);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected void resetLighting(Object[] disableResult) {
        if (disableResult != null) {
            try {
                fieldNeighbors.set(disableResult[0], disableResult[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void enableLighting(Object[] disableResult) {
        if (disableResult != null) {
            try {
                fieldNeighbors.set(disableResult[0], 0x739C0);
            } catch (Throwable ignore) {
            }
        }
    }
}
