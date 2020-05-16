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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.Debug;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.util.MainUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.block.BaseBlock;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Local queue which makes use of WorldEdit's WorldNativeAccess
 */
public class WorldEditNativeAccessLocalQueue extends BukkitLocalQueue {

    private final BukkitWorld bukkitWorld;

    public WorldEditNativeAccessLocalQueue(final String world) throws Throwable {
        super(world);

        final World worldObject = Bukkit.getWorld(world);

        if ((this.bukkitWorld = canUse(worldObject)) == null) {
            PlotSquared.log(Captions.PREFIX + "Using Bukkit's API to set blocks");
            throw new RuntimeException("WorldNativeAccess not available");
        }

        PlotSquared.log(Captions.PREFIX + "Using WorldEdit's WorldNativeAccess to set blocks");
    }

    @Override public void setBaseBlocks(LocalChunk localChunk) {
        if (!Debug.enableNativeAccess) {
            super.setBaseBlocks(localChunk);
            return;
        }

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
                            int x = (chunk.getX() << 4) + MainUtil.x_loc[layer][j];
                            int y = MainUtil.y_loc[layer][j];
                            int z = (chunk.getZ() << 4) + MainUtil.z_loc[layer][j];

                            final BlockVector3 position = BlockVector3.at(x, y, z);

                            if (bukkitWorld.getFullBlock(position).equalsFuzzy(block)) {
                                continue;
                            }

                            // This won't do anything if it's not an inventory holder
                            bukkitWorld.clearContainerBlockContents(position);

                            // Update the blocks
                            bukkitWorld.setBlock(position, block, SideEffectSet.defaults());
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

    /**
     * This checks if we can make use of WorldNativeAccess
     *
     * @param world The world we're creating the queue for
     * @return BukkitWorld instance if the world can be created, else null
     */
    @Nullable public static BukkitWorld canUse(final World world) throws Throwable {
        final BukkitWorld bukkitWorld =
            BukkitAdapter.asBukkitWorld(BukkitAdapter.adapt(Objects.requireNonNull(world)));
        final Field field = BukkitWorld.class.getDeclaredField("worldNativeAccess");
        field.setAccessible(true);
        if (field.get(bukkitWorld) != null) {
            return bukkitWorld;
        }
        return null;
    }

}
