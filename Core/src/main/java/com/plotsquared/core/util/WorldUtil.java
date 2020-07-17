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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class WorldUtil {

    private final RegionManager regionManager;

    public WorldUtil(@Nonnull final RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    /**
     * Check if a given world name corresponds to a real world
     *
     * @param worldName World name
     * @return {@code true} if there exists a world with the given world name,
     * {@code false} if not
     */
    public abstract boolean isWorld(@Nonnull String worldName);

    /**
     * @param location Sign location
     * @return Sign content (or an empty string array if the block is not a sign)
     * @deprecated May result in synchronous chunk loading
     */
    @Deprecated @Nonnull public abstract String[] getSignSynchronous(@Nonnull Location location);

    /**
     * Get the world spawn location
     *
     * @param world World name
     * @return World spawn location
     */
    @Nonnull public abstract Location getSpawn(@Nonnull String world);

    /**
     * Set the world spawn location
     *
     * @param location New spawn
     */
    public abstract void setSpawn(@Nonnull Location location);

    /**
     * Save a world
     *
     * @param world World name
     */
    public abstract void saveWorld(@Nonnull String world);

    /**
     * Get a string comparison with the closets block state matching a given string
     *
     * @param name Block name
     * @return Comparison result containing the closets matching block
     */
    @Nonnull public abstract StringComparison<BlockState>.ComparisonResult getClosestBlock(
        @Nonnull String name);

    /**
     * Get the biome in a given chunk, asynchronously
     *
     * @param world  World
     * @param x      Chunk X coordinate
     * @param z      Chunk Z coordinate
     * @param result Result consumer
     */
    public abstract void getBiome(@Nonnull String world, int x, int z,
        @Nonnull Consumer<BiomeType> result);

    /**
     * Get the biome in a given chunk, asynchronously
     *
     * @param world World
     * @param x     Chunk X coordinate
     * @param z     Chunk Z coordinate
     * @return Biome
     * @deprecated Use {@link #getBiome(String, int, int, Consumer)}
     */
    @Deprecated @Nonnull public abstract BiomeType getBiomeSynchronous(@Nonnull String world, int x,
        int z);

    /**
     * Get the block at a given location (asynchronously)
     *
     * @param location Block location
     * @param result   Result consumer
     */
    public abstract void getBlock(@Nonnull Location location, @Nonnull Consumer<BlockState> result);

    /**
     * Get the block at a given location (synchronously)
     *
     * @param location Block location
     * @return Result
     * @deprecated Use {@link #getBlock(Location, Consumer)}
     */
    @Deprecated @Nonnull public abstract BlockState getBlockSynchronous(@Nonnull Location location);

    /**
     * Get the Y coordinate of the highest non-air block in the world, asynchronously
     *
     * @param world  World name
     * @param x      X coordinate
     * @param z      Z coordinate
     * @param result Result consumer
     */
    public abstract void getHighestBlock(@Nonnull String world, int x, int z,
        @Nonnull IntConsumer result);


    /**
     * Get the Y coordinate of the highest non-air block in the world, synchronously
     *
     * @param world World name
     * @param x     X coordinate
     * @param z     Z coordinate
     * @return Result
     * @deprecated Use {@link #getHighestBlock(String, int, int, IntConsumer)}
     */
    @Deprecated @Nonnegative
    public abstract int getHighestBlockSynchronous(@Nonnull String world, int x, int z);

    /**
     * Set the text in a sign
     *
     * @param world World name
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @param lines Sign text
     */
    public abstract void setSign(@Nonnull String world, int x, int y, int z,
        @Nonnull String[] lines);

    /**
     * Set the biome in a region
     *
     * @param world  World name
     * @param region Region
     * @param biome  New biome
     */
    public abstract void setBiomes(@Nonnull String world, @Nonnull CuboidRegion region,
        @Nonnull BiomeType biome);

    /**
     * Get the WorldEdit {@link com.sk89q.worldedit.world.World} corresponding to a world name
     *
     * @param world World name
     * @return World object
     */
    @Nonnull public abstract com.sk89q.worldedit.world.World getWeWorld(@Nonnull String world);

    public void upload(@Nonnull final Plot plot, @Nullable final UUID uuid,
        @Nullable final String file, @Nonnull final RunnableVal<URL> whenDone) {
        plot.getHome(home -> MainUtil.upload(uuid, file, "zip", new RunnableVal<OutputStream>() {
            @Override public void run(OutputStream output) {
                try (final ZipOutputStream zos = new ZipOutputStream(output)) {
                    File dat = getDat(plot.getWorldName());
                    Location spawn = getSpawn(plot.getWorldName());
                    if (dat != null) {
                        ZipEntry ze = new ZipEntry("world" + File.separator + dat.getName());
                        zos.putNextEntry(ze);
                        try (NBTInputStream nis = new NBTInputStream(
                            new GZIPInputStream(new FileInputStream(dat)))) {
                            CompoundTag tag = (CompoundTag) nis.readNamedTag().getTag();
                            CompoundTag data = (CompoundTag) tag.getValue().get("Data");
                            Map<String, Tag> map = ReflectionUtils.getMap(data.getValue());
                            map.put("SpawnX", new IntTag(home.getX()));
                            map.put("SpawnY", new IntTag(home.getY()));
                            map.put("SpawnZ", new IntTag(home.getZ()));
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                try (NBTOutputStream out = new NBTOutputStream(
                                    new GZIPOutputStream(baos, true))) {
                                    //TODO Find what this should be called
                                    out.writeNamedTag("Schematic????", tag);
                                }
                                zos.write(baos.toByteArray());
                            }
                        }
                    }
                    setSpawn(spawn);
                    byte[] buffer = new byte[1024];
                    for (Plot current : plot.getConnectedPlots()) {
                        Location bot = current.getBottomAbs();
                        Location top = current.getTopAbs();
                        int brx = bot.getX() >> 9;
                        int brz = bot.getZ() >> 9;
                        int trx = top.getX() >> 9;
                        int trz = top.getZ() >> 9;
                        Set<BlockVector2> files = regionManager.getChunkChunks(bot.getWorldName());
                        for (BlockVector2 mca : files) {
                            if (mca.getX() >= brx && mca.getX() <= trx && mca.getZ() >= brz
                                && mca.getZ() <= trz) {
                                final File file =
                                    getMcr(plot.getWorldName(), mca.getX(), mca.getZ());
                                if (file != null) {
                                    //final String name = "r." + (x - cx) + "." + (z - cz) + ".mca";
                                    String name = file.getName();
                                    final ZipEntry ze = new ZipEntry(
                                        "world" + File.separator + "region" + File.separator
                                            + name);
                                    zos.putNextEntry(ze);
                                    try (FileInputStream in = new FileInputStream(file)) {
                                        int len;
                                        while ((len = in.read(buffer)) > 0) {
                                            zos.write(buffer, 0, len);
                                        }
                                    }
                                    zos.closeEntry();
                                }
                            }
                        }
                    }
                    zos.closeEntry();
                    zos.flush();
                    zos.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, whenDone));
    }

    @Nullable final File getDat(@Nonnull final String world) {
        File file = new File(
            PlotSquared.platform().getWorldContainer() + File.separator + world + File.separator
                + "level.dat");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Nullable private File getMcr(@Nonnull final String world, final int x, final int z) {
        final File file = new File(PlotSquared.platform().getWorldContainer(),
            world + File.separator + "region" + File.separator + "r." + x + '.' + z + ".mca");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * Check if two blocks are the same type)
     *
     * @param block1 First block
     * @param block2 Second block
     * @return {@code true} if the blocks have the same type, {@code false} if not
     */
    public abstract boolean isBlockSame(@Nonnull BlockState block1, @Nonnull BlockState block2);

    /**
     * Get a player object for the player with the given UUID
     *
     * @param uuid Player UUID
     * @return Player object
     */
    @Nonnull public abstract PlotPlayer<?> getPlayer(@Nonnull UUID uuid);

    /**
     * Get the player health
     *
     * @param player Player
     * @return Non-negative health
     */
    @Nonnegative public abstract double getHealth(@Nonnull PlotPlayer<?> player);

    /**
     * Set the player health
     *
     * @param player Player health
     * @param health Non-negative health
     */
    public abstract void setHealth(@Nonnull PlotPlayer<?> player, @Nonnegative double health);

    /**
     * Get the player food level
     *
     * @param player Player
     * @return Non-negative food level
     */
    @Nonnegative public abstract int getFoodLevel(@Nonnull PlotPlayer<?> player);

    /**
     * Set the player food level
     *
     * @param player    Player food level
     * @param foodLevel Non-negative food level
     */
    public abstract void setFoodLevel(@Nonnull PlotPlayer<?> player, @Nonnegative int foodLevel);

    /**
     * Get all entity types belonging to an entity category
     *
     * @param category Entity category
     * @return Set containing all entities belonging to the given category
     */
    @Nonnull public abstract Set<EntityType> getTypesInCategory(@Nonnull String category);

    /**
     * Get all recognized tile entity types
     *
     * @return Collection containing all known tile entity types
     */
    @Nonnull public abstract Collection<BlockType> getTileEntityTypes();

    /**
     * Get the tile entity count in a chunk
     *
     * @param world World
     * @param chunk Chunk coordinates
     * @return Tile entity count
     */
    @Nonnegative public abstract int getTileEntityCount(@Nonnull String world,
        @Nonnull BlockVector2 chunk);

}
