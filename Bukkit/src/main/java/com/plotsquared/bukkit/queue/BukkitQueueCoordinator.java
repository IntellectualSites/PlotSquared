/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.queue;

import com.google.inject.Inject;
import com.plotsquared.bukkit.schematic.StateWrapper;
import com.plotsquared.bukkit.util.BukkitBlockUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.ChunkCoordinatorBuilderFactory;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.queue.BasicQueueCoordinator;
import com.plotsquared.core.queue.ChunkCoordinator;
import com.plotsquared.core.queue.LocalChunk;
import com.plotsquared.core.util.ChunkUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class BukkitQueueCoordinator extends BasicQueueCoordinator {

    private static final SideEffectSet NO_SIDE_EFFECT_SET;
    private static final SideEffectSet EDGE_SIDE_EFFECT_SET;
    private static final SideEffectSet LIGHTING_SIDE_EFFECT_SET;
    private static final SideEffectSet EDGE_LIGHTING_SIDE_EFFECT_SET;

    static {
        NO_SIDE_EFFECT_SET = enableNetworkIfNeeded()
                .with(SideEffect.LIGHTING, SideEffect.State.OFF)
                .with(SideEffect.NEIGHBORS, SideEffect.State.OFF);
        EDGE_SIDE_EFFECT_SET = NO_SIDE_EFFECT_SET
                .with(SideEffect.UPDATE, SideEffect.State.ON)
                .with(SideEffect.NEIGHBORS, SideEffect.State.ON);
        LIGHTING_SIDE_EFFECT_SET = NO_SIDE_EFFECT_SET
                .with(SideEffect.NEIGHBORS, SideEffect.State.OFF);
        EDGE_LIGHTING_SIDE_EFFECT_SET = NO_SIDE_EFFECT_SET
                .with(SideEffect.UPDATE, SideEffect.State.ON)
                .with(SideEffect.NEIGHBORS, SideEffect.State.ON);
    }

    // make sure block changes are sent
    private static SideEffectSet enableNetworkIfNeeded() {
        SideEffect network;
        try {
            network = SideEffect.valueOf("NETWORK");
        } catch (IllegalArgumentException ignored) {
            return SideEffectSet.none();
        }
        return SideEffectSet.none().with(network, SideEffect.State.ON);
    }

    private org.bukkit.World bukkitWorld;
    @Inject
    private ChunkCoordinatorBuilderFactory chunkCoordinatorBuilderFactory;
    @Inject
    private ChunkCoordinatorFactory chunkCoordinatorFactory;
    private ChunkCoordinator chunkCoordinator;

    @Inject
    public BukkitQueueCoordinator(@NonNull World world) {
        super(world);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        Block block = getBukkitWorld().getBlockAt(x, y, z);
        return BukkitBlockUtil.get(block);
    }

    @Override
    public void start() {
        chunkCoordinator.start();
    }

    @Override
    public void cancel() {
        chunkCoordinator.cancel();
    }

    @Override
    public boolean enqueue() {
        final Clipboard regenClipboard;
        if (isRegen()) {
            BlockVector3 start = BlockVector3.at(getRegenStart()[0] << 4, getMinY(), getRegenStart()[1] << 4);
            BlockVector3 end = BlockVector3.at((getRegenEnd()[0] << 4) + 15, getMaxY(), (getRegenEnd()[1] << 4) + 15);
            Region region = new CuboidRegion(start, end);
            regenClipboard = new BlockArrayClipboard(region);
            regenClipboard.setOrigin(start);
            getWorld().regenerate(region, regenClipboard);
        } else if (getRegenRegion() != null) {
            regenClipboard = new BlockArrayClipboard(getRegenRegion());
            regenClipboard.setOrigin(getRegenRegion().getMinimumPoint());
            getWorld().regenerate(getRegenRegion(), regenClipboard);
        } else {
            regenClipboard = null;
        }
        Consumer<BlockVector2> consumer = getChunkConsumer();
        if (consumer == null) {
            consumer = blockVector2 -> {
                LocalChunk localChunk = getBlockChunks().get(blockVector2);
                boolean isRegenChunk =
                        regenClipboard != null && blockVector2.getBlockX() > getRegenStart()[0] && blockVector2.getBlockZ() > getRegenStart()[1]
                                && blockVector2.getBlockX() < getRegenEnd()[0] && blockVector2.getBlockZ() < getRegenEnd()[1];
                int sx = blockVector2.getX() << 4;
                int sz = blockVector2.getZ() << 4;
                if (isRegenChunk) {
                    for (int layer = getMinLayer(); layer <= getMaxLayer(); layer++) {
                        for (int y = 0; y < 16; y++) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    x += sx;
                                    y += layer << 4;
                                    z += sz;
                                    BaseBlock block = regenClipboard.getFullBlock(BlockVector3.at(x, y, z));
                                    if (block != null) {
                                        boolean edge = Settings.QUEUE.UPDATE_EDGES && isEdgeRegen(x & 15, z & 15, blockVector2);
                                        setWorldBlock(x, y, z, block, blockVector2, edge);
                                    }
                                }
                            }
                        }
                    }
                }
                // Allow regen and then blocks to be placed (plot schematic etc)
                if (localChunk == null) {
                    return;
                }
                for (int layer = 0; layer < localChunk.getBaseblocks().length; layer++) {
                    BaseBlock[] blocksLayer = localChunk.getBaseblocks()[layer];
                    if (blocksLayer == null) {
                        continue;
                    }
                    for (int j = 0; j < blocksLayer.length; j++) {
                        if (blocksLayer[j] == null) {
                            continue;
                        }
                        BaseBlock block = blocksLayer[j];

                        if (block != null) {
                            int lx = ChunkUtil.getX(j);
                            int lz = ChunkUtil.getZ(j);
                            int x = sx + lx;
                            int y = ChunkUtil.getY(layer + localChunk.getMinSection(), j);
                            int z = sz + lz;
                            boolean edge = Settings.QUEUE.UPDATE_EDGES && isEdge(y >> 4, lx, y & 15, lz, blockVector2,
                                    localChunk
                            );
                            setWorldBlock(x, y, z, block, blockVector2, edge);
                        }
                    }
                }
                for (int layer = 0; layer < localChunk.getBiomes().length; layer++) {
                    BiomeType[] biomesLayer = localChunk.getBiomes()[layer];
                    if (biomesLayer == null) {
                        continue;
                    }
                    for (int j = 0; j < biomesLayer.length; j++) {
                        if (biomesLayer[j] == null) {
                            continue;
                        }
                        BiomeType biome = biomesLayer[j];
                        if (biome != null) {
                            int x = sx + ChunkUtil.getX(j);
                            int y = ChunkUtil.getY(layer, j);
                            int z = sz + ChunkUtil.getZ(j);
                            getWorld().setBiome(BlockVector3.at(x, y, z), biome);
                        }
                    }
                }
                if (localChunk.getTiles().size() > 0) {
                    localChunk.getTiles().forEach((blockVector3, tag) -> {
                        try {
                            BaseBlock block = getWorld().getBlock(blockVector3).toBaseBlock(tag);
                            getWorld().setBlock(blockVector3, block, getSideEffectSet(SideEffectState.NONE));
                        } catch (WorldEditException ignored) {
                            StateWrapper sw = new StateWrapper(tag);
                            sw.restoreTag(getWorld().getName(), blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
                        }
                    });
                }
                if (localChunk.getEntities().size() > 0) {
                    localChunk.getEntities().forEach((location, entity) -> getWorld().createEntity(location, entity));
                }
            };
        }
        Collection<BlockVector2> read = new ArrayList<>();
        if (getReadChunks().size() > 0) {
            read.addAll(getReadChunks());
        }
        chunkCoordinator =
                chunkCoordinatorBuilderFactory
                        .create(chunkCoordinatorFactory)
                        .inWorld(getWorld())
                        .withChunks(getBlockChunks().keySet())
                        .withChunks(read)
                        .withInitialBatchSize(3)
                        .withMaxIterationTime(40)
                        .withThrowableConsumer(Throwable::printStackTrace)
                        .withFinalAction(getCompleteTask())
                        .withConsumer(consumer)
                        .unloadAfter(isUnloadAfter())
                        .withProgressSubscribers(getProgressSubscribers())
                        .forceSync(isForceSync())
                        .build();
        return super.enqueue();
    }

    /**
     * Set a block to the world. First tries WNA but defaults to normal block setting methods if that fails
     */
    @SuppressWarnings("unused")
    private void setWorldBlock(int x, int y, int z, @NonNull BaseBlock block, @NonNull BlockVector2 blockVector2, boolean edge) {
        try {
            BlockVector3 loc = BlockVector3.at(x, y, z);
            boolean lighting = false;
            switch (getLightingMode()) {
                case NONE:
                    break;
                case PLACEMENT:
                    lighting = block.getBlockType().getMaterial().getLightValue() > 0;
                    break;
                case REPLACEMENT:
                    lighting = block.getBlockType().getMaterial().getLightValue() > 0
                            || getWorld().getBlock(loc).getBlockType().getMaterial().getLightValue() > 0;
                    break;
                default:
                    // Can only be "all"
                    lighting = true;
            }
            SideEffectSet sideEffectSet;
            if (lighting) {
                sideEffectSet = getSideEffectSet(edge ? SideEffectState.EDGE_LIGHTING : SideEffectState.LIGHTING);
            } else {
                sideEffectSet = getSideEffectSet(edge ? SideEffectState.EDGE : SideEffectState.NONE);
            }
            getWorld().setBlock(loc, block, sideEffectSet);
        } catch (WorldEditException ignored) {
            // Fallback to not so nice method
            BlockData blockData = BukkitAdapter.adapt(block);
            Block existing;
            // Assume a chunk object has been given only when it should have been.
            if (getChunkObject() instanceof Chunk chunkObject) {
                existing = chunkObject.getBlock(x & 15, y, z & 15);
            } else {
                existing = getBukkitWorld().getBlockAt(x, y, z);
            }
            final BlockState existingBaseBlock = BukkitAdapter.adapt(existing.getBlockData());
            if (BukkitBlockUtil.get(existing).equals(existingBaseBlock) && existing.getBlockData().matches(blockData)) {
                return;
            }

            if (existing.getState() instanceof Container) {
                ((Container) existing.getState()).getInventory().clear();
            }

            existing.setType(BukkitAdapter.adapt(block.getBlockType()), false);
            existing.setBlockData(blockData, false);
            if (block.hasNbtData()) {
                CompoundTag tag = block.getNbtData();
                StateWrapper sw = new StateWrapper(tag);

                sw.restoreTag(existing);
            }
        }
    }

    private org.bukkit.World getBukkitWorld() {
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(getWorld().getName());
        }
        return bukkitWorld;
    }

    private boolean isEdge(int layer, int x, int y, int z, BlockVector2 blockVector2, LocalChunk localChunk) {
        int layerIndex = (layer - localChunk.getMinSection());
        if (layer == localChunk.getMinSection() || layerIndex == localChunk.getBaseblocks().length - 1) {
            return false;
        }
        if (x == 0) {
            LocalChunk localChunkX = getBlockChunks().get(blockVector2.withX(blockVector2.getX() - 1));
            if (localChunkX == null || localChunkX.getBaseblocks()[layerIndex] == null ||
                    localChunkX.getBaseblocks()[layerIndex][ChunkUtil.getJ(15, y, z)] != null) {
                return true;
            }
        } else if (x == 15) {
            LocalChunk localChunkX = getBlockChunks().get(blockVector2.withX(blockVector2.getX() + 1));
            if (localChunkX == null || localChunkX.getBaseblocks()[layerIndex] == null ||
                    localChunkX.getBaseblocks()[layerIndex][ChunkUtil.getJ(0, y, z)] != null) {
                return true;
            }
        }
        if (z == 0) {
            LocalChunk localChunkZ = getBlockChunks().get(blockVector2.withZ(blockVector2.getZ() - 1));
            if (localChunkZ == null || localChunkZ.getBaseblocks()[layerIndex] == null ||
                    localChunkZ.getBaseblocks()[layerIndex][ChunkUtil.getJ(x, y, 15)] != null) {
                return true;
            }
        } else if (z == 15) {
            LocalChunk localChunkZ = getBlockChunks().get(blockVector2.withZ(blockVector2.getZ() + 1));
            if (localChunkZ == null || localChunkZ.getBaseblocks()[layerIndex] == null ||
                    localChunkZ.getBaseblocks()[layerIndex][ChunkUtil.getJ(x, y, 0)] != null) {
                return true;
            }
        }
        if (y == 0) {
            if (localChunk.getBaseblocks()[layerIndex - 1] == null ||
                    localChunk.getBaseblocks()[layerIndex][ChunkUtil.getJ(x, 15, z)] != null) {
                return true;
            }
        } else if (y == 15) {
            if (localChunk.getBaseblocks()[layerIndex + 1] == null ||
                    localChunk.getBaseblocks()[layerIndex][ChunkUtil.getJ(x, 0, z)] != null) {
                return true;
            }
        }
        BaseBlock[] baseBlocks = localChunk.getBaseblocks()[layerIndex];
        if (x > 0 && baseBlocks[ChunkUtil.getJ(x - 1, y, z)] == null) {
            return true;
        }
        if (x < 15 && baseBlocks[ChunkUtil.getJ(x + 1, y, z)] == null) {
            return true;
        }
        if (y > 0 && baseBlocks[ChunkUtil.getJ(x, y - 1, z)] == null) {
            return true;
        }
        if (y < 15 && baseBlocks[ChunkUtil.getJ(x, y + 1, z)] == null) {
            return true;
        }
        if (z > 0 && baseBlocks[ChunkUtil.getJ(x, y, z - 1)] == null) {
            return true;
        }
        return z < 15 && baseBlocks[ChunkUtil.getJ(x, y, z + 1)] == null;
    }

    private boolean isEdgeRegen(int x, int z, BlockVector2 blockVector2) {
        if (x == 0) {
            LocalChunk localChunkX = getBlockChunks().get(blockVector2.withX(blockVector2.getX() - 1));
            if (localChunkX == null) {
                return true;
            }
        } else if (x == 15) {
            LocalChunk localChunkX = getBlockChunks().get(blockVector2.withX(blockVector2.getX() + 1));
            if (localChunkX == null) {
                return true;
            }
        }
        if (z == 0) {
            return getBlockChunks().get(blockVector2.withZ(blockVector2.getZ() - 1)) == null;
        } else if (z == 15) {
            return getBlockChunks().get(blockVector2.withZ(blockVector2.getZ() + 1)) == null;
        }
        return false;
    }

    private SideEffectSet getSideEffectSet(SideEffectState state) {
        if (getSideEffectSet() != null) {
            return getSideEffectSet();
        }
        return switch (state) {
            case NONE -> NO_SIDE_EFFECT_SET;
            case EDGE -> EDGE_SIDE_EFFECT_SET;
            case LIGHTING -> LIGHTING_SIDE_EFFECT_SET;
            case EDGE_LIGHTING -> EDGE_LIGHTING_SIDE_EFFECT_SET;
        };
    }

    private enum SideEffectState {
        NONE,
        EDGE,
        LIGHTING,
        EDGE_LIGHTING
    }

}
