/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.queue;

import com.plotsquared.bukkit.schematic.StateWrapper;
import com.plotsquared.bukkit.util.BukkitBlockUtil;
import com.plotsquared.core.queue.BasicLocalBlockQueue;
import com.plotsquared.core.util.BlockUtil;
import com.plotsquared.core.util.MainUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.lib.PaperLib;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

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
        }
    }

    @Override public void fixChunkLighting(int x, int z) {
        // Do nothing
    }

    @Override public final void regenChunk(int x, int z) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj != null) {
            try {
                worldObj.regenerateChunk(x, z);
            } catch (UnsupportedOperationException e) {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(worldObj);
                try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                    .getEditSession(world, -1);) {
                    CuboidRegion region =
                        new CuboidRegion(world, BlockVector3.at((x << 4), 0, (z << 4)),
                            BlockVector3.at((x << 4) + 15, 255, (z << 4) + 15));
                    world.regenerate(region, editSession);
                }
            }
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
        final Consumer<Chunk> chunkConsumer = chunk -> {
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
                            final BlockState existingBaseBlock =
                                BukkitAdapter.adapt(existing.getBlockData());
                            if (BukkitBlockUtil.get(existing).equals(existingBaseBlock) && existing
                                .getBlockData().matches(blockData)) {
                                continue;
                            }

                            if (existing.getState() instanceof Container) {
                                ((Container) existing.getState()).getInventory().clear();
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
            if (setBiome() && localChunk.biomes != null) {
                for (int x = 0; x < localChunk.biomes.length; x++) {
                    BiomeType[] biomeZ = localChunk.biomes[x];
                    if (biomeZ != null) {
                        for (int z = 0; z < biomeZ.length; z++) {
                            if (biomeZ[z] != null) {
                                BiomeType biomeType = biomeZ[z];

                                Biome biome = BukkitAdapter.adapt(biomeType);
                                worldObj.setBiome((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z,
                                    biome);
                            }
                        }
                    }
                }
            }
        };
        if (isForceSync()) {
            chunkConsumer.accept(getChunk(worldObj, localChunk));
        } else {
            PaperLib.getChunkAtAsync(worldObj, localChunk.getX(), localChunk.getZ(), true)
                .thenAccept(chunkConsumer);
        }
    }

    private Chunk getChunk(final World world, final LocalChunk localChunk) {
        Chunk chunk = null;
        if (this.getChunkObject() != null && this.getChunkObject() instanceof Chunk) {
            chunk = (Chunk) this.getChunkObject();
        }
        if (chunk == null) {
            chunk = world.getChunkAt(localChunk.getX(), localChunk.getZ());
        }
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }
        return chunk;
    }

    private void setMaterial(@NonNull final BlockState plotBlock, @NonNull final Block block) {
        Material material = BukkitAdapter.adapt(plotBlock.getBlockType());
        block.setType(material, false);
    }

    private boolean equals(@NonNull final BlockState plotBlock, @NonNull final Block block) {
        return plotBlock.equals(BukkitBlockUtil.get(block));
    }

    public void setBiomes(LocalChunk lc) {
        World worldObj = Bukkit.getWorld(getWorld());
        if (worldObj == null) {
            throw new NullPointerException("World cannot be null.");
        }
        if (lc.biomes == null) {
            throw new NullPointerException("Biomes cannot be null.");
        }
        final Consumer<Chunk> chunkConsumer = chunk -> {
            for (int x = 0; x < lc.biomes.length; x++) {
                BiomeType[] biomeZ = lc.biomes[x];
                if (biomeZ != null) {
                    for (int z = 0; z < biomeZ.length; z++) {
                        if (biomeZ[z] != null) {
                            BiomeType biomeType = biomeZ[z];

                            Biome biome = BukkitAdapter.adapt(biomeType);
                            worldObj
                                .setBiome((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z, biome);
                        }
                    }
                }
            }
        };
        if (this.isForceSync()) {
            chunkConsumer.accept(getChunk(worldObj, lc));
        } else {
            PaperLib.getChunkAtAsync(worldObj, lc.getX(), lc.getZ(), true)
                .thenAccept(chunkConsumer);
        }
    }

}
