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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
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
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

    /**
     * {@return whether the given location is valid in the world}
     * @param location the location to check
     * @since 7.3.6
     */
    public static boolean isValidLocation(Location location) {
        return Math.abs(location.getX()) < 30000000 && Math.abs(location.getZ()) < 30000000;
    }

    /**
     * Set the biome in a region
     *
     * @param world  World name
     * @param region Region
     * @param biome  Biome
     * @since 6.6.0
     */
    public static void setBiome(String world, final CuboidRegion region, BiomeType biome) {
        PlotSquared.platform().worldUtil().setBiomes(world, region, biome);
    }

    /**
     * Check if a given world name corresponds to a real world
     *
     * @param worldName World name
     * @return {@code true} if there exists a world with the given world name,
     *         {@code false} if not
     */
    public abstract boolean isWorld(@NonNull String worldName);

    /**
     * @param location Sign location
     * @return Sign content (or an empty string array if the block is not a sign)
     * @deprecated May result in synchronous chunk loading
     */
    @Deprecated
    public @NonNull
    abstract String[] getSignSynchronous(@NonNull Location location);

    /**
     * Get the world spawn location
     *
     * @param world World name
     * @return World spawn location
     */
    public @NonNull
    abstract Location getSpawn(@NonNull String world);

    /**
     * Set the world spawn location
     *
     * @param location New spawn
     */
    public abstract void setSpawn(@NonNull Location location);

    /**
     * Save a world
     *
     * @param world World name
     */
    public abstract void saveWorld(@NonNull String world);

    /**
     * Get a string comparison with the closets block state matching a given string
     *
     * @param name Block name
     * @return Comparison result containing the closets matching block
     */
    public @NonNull
    abstract StringComparison<BlockState>.ComparisonResult getClosestBlock(@NonNull String name);

    /**
     * Set the block at the specified location to a sign, with given text
     *
     * @param location     Block location
     * @param lines        Sign text
     * @param replacements Text replacements
     */
    public abstract void setSign(
            @NonNull Location location,
            @NonNull Caption[] lines,
            @NonNull TagResolver... replacements
    );

    /**
     * Get the biome in a given chunk, asynchronously
     *
     * @param world  World
     * @param x      Chunk X coordinate
     * @param z      Chunk Z coordinate
     * @param result Result consumer
     */
    public abstract void getBiome(@NonNull String world, int x, int z, @NonNull Consumer<BiomeType> result);

    /**
     * Get the biome in a given chunk, asynchronously
     *
     * @param world World
     * @param x     Chunk X coordinate
     * @param z     Chunk Z coordinate
     * @return Biome
     * @deprecated Use {@link #getBiome(String, int, int, Consumer)}
     */
    @Deprecated
    public @NonNull
    abstract BiomeType getBiomeSynchronous(@NonNull String world, int x, int z);

    /**
     * Get the block at a given location (asynchronously)
     *
     * @param location Block location
     * @param result   Result consumer
     */
    public abstract void getBlock(@NonNull Location location, @NonNull Consumer<BlockState> result);

    /**
     * Checks if the block smaller as a slab
     * @param location Block location
     * @return true if it smaller as a slab
     */
    public abstract boolean isSmallBlock(@NonNull Location location);

    /**
     * Get the block at a given location (synchronously)
     *
     * @param location Block location
     * @return Result
     * @deprecated Use {@link #getBlock(Location, Consumer)}
     */
    @Deprecated
    public @NonNull
    abstract BlockState getBlockSynchronous(@NonNull Location location);

    /**
     * Get the Y coordinate of the highest non-air block in the world, asynchronously
     *
     * @param world  World name
     * @param x      X coordinate
     * @param z      Z coordinate
     * @param result Result consumer
     */
    public abstract void getHighestBlock(@NonNull String world, int x, int z, @NonNull IntConsumer result);

    /**
     * Get the Y coordinate of the highest non-air block in the world, synchronously
     *
     * @param world World name
     * @param x     X coordinate
     * @param z     Z coordinate
     * @return Result
     * @deprecated Use {@link #getHighestBlock(String, int, int, IntConsumer)}
     */
    @Deprecated
    @NonNegative
    public abstract int getHighestBlockSynchronous(@NonNull String world, int x, int z);

    /**
     * Set the biome in a region
     *
     * @param worldName World name
     * @param region    Region
     * @param biome     New biome
     */
    public void setBiomes(@NonNull String worldName, @NonNull CuboidRegion region, @NonNull BiomeType biome) {
        final World world = getWeWorld(worldName);
        region.forEach(bv -> world.setBiome(bv, biome));
    }

    /**
     * Get the WorldEdit {@link com.sk89q.worldedit.world.World} corresponding to a world name
     *
     * @param world World name
     * @return World object
     */
    public abstract com.sk89q.worldedit.world.@NonNull World getWeWorld(@NonNull String world);

    /**
     * Refresh (resend) chunk to player. Usually after setting the biome
     *
     * @param x     Chunk x location
     * @param z     Chunk z location
     * @param world World of the chunk
     */
    public abstract void refreshChunk(int x, int z, String world);

    /**
     * The legacy web interface is deprecated for removal in favor of Arkitektonika.
     */
    @Deprecated(forRemoval = true, since = "6.11.0")
    public void upload(
            final @NonNull Plot plot,
            final @Nullable UUID uuid,
            final @Nullable String file,
            final @NonNull RunnableVal<URL> whenDone
    ) {
        plot.getHome(home -> SchematicHandler.upload(uuid, file, "zip", new RunnableVal<>() {
            @Override
            public void run(OutputStream output) {
                try (final ZipOutputStream zos = new ZipOutputStream(output)) {
                    File dat = getDat(plot.getWorldName());
                    Location spawn = getSpawn(plot.getWorldName());
                    if (dat != null) {
                        ZipEntry ze = new ZipEntry("world" + File.separator + dat.getName());
                        zos.putNextEntry(ze);
                        try (NBTInputStream nis = new NBTInputStream(new GZIPInputStream(new FileInputStream(dat)))) {
                            Map<String, Tag> tag = ((CompoundTag) nis.readNamedTag().getTag()).getValue();
                            Map<String, Tag> newMap = new HashMap<>();
                            for (Map.Entry<String, Tag> entry : tag.entrySet()) {
                                if (!entry.getKey().equals("Data")) {
                                    newMap.put(entry.getKey(), entry.getValue());
                                    continue;
                                }
                                Map<String, Tag> data = new HashMap<>(((CompoundTag) entry.getValue()).getValue());
                                data.put("SpawnX", new IntTag(home.getX()));
                                data.put("SpawnY", new IntTag(home.getY()));
                                data.put("SpawnZ", new IntTag(home.getZ()));
                                newMap.put("Data", new CompoundTag(data));
                            }
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                try (NBTOutputStream out = new NBTOutputStream(new GZIPOutputStream(baos, true))) {
                                    //TODO Find what this should be called
                                    out.writeNamedTag("Schematic????", new CompoundTag(newMap));
                                }
                                zos.write(baos.toByteArray());
                            }
                        }
                    }
                    setSpawn(spawn);
                    byte[] buffer = new byte[1024];
                    Set<BlockVector2> added = new HashSet<>();
                    for (Plot current : plot.getConnectedPlots()) {
                        Location bot = current.getBottomAbs();
                        Location top = current.getTopAbs();
                        int brx = bot.getX() >> 9;
                        int brz = bot.getZ() >> 9;
                        int trx = top.getX() >> 9;
                        int trz = top.getZ() >> 9;
                        Set<BlockVector2> files = getChunkChunks(bot.getWorldName());
                        for (BlockVector2 mca : files) {
                            if (mca.getX() >= brx && mca.getX() <= trx && mca.getZ() >= brz && mca.getZ() <= trz && !added.contains(
                                    mca)) {
                                final File file = getMcr(plot.getWorldName(), mca.getX(), mca.getZ());
                                if (file != null) {
                                    //final String name = "r." + (x - cx) + "." + (z - cz) + ".mca";
                                    String name = file.getName();
                                    final ZipEntry ze = new ZipEntry("world" + File.separator + "region" + File.separator + name);
                                    zos.putNextEntry(ze);
                                    added.add(mca);
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

    final @Nullable File getDat(final @NonNull String world) {
        File file = new File(PlotSquared.platform().worldContainer() + File.separator + world + File.separator + "level.dat");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Nullable
    private File getMcr(final @NonNull String world, final int x, final int z) {
        final File file =
                new File(
                        PlotSquared.platform().worldContainer(),
                        world + File.separator + "region" + File.separator + "r." + x + '.' + z + ".mca"
                );
        if (file.exists()) {
            return file;
        }
        return null;
    }


    public Set<BlockVector2> getChunkChunks(String world) {
        File folder = new File(PlotSquared.platform().worldContainer(), world + File.separator + "region");
        File[] regionFiles = folder.listFiles();
        if (regionFiles == null) {
            throw new RuntimeException("Could not find worlds folder: " + folder + " ? (no read access?)");
        }
        HashSet<BlockVector2> chunks = new HashSet<>();
        for (File file : regionFiles) {
            String name = file.getName();
            if (name.endsWith("mca")) {
                String[] split = name.split("\\.");
                try {
                    int x = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    BlockVector2 loc = BlockVector2.at(x, z);
                    chunks.add(loc);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return chunks;
    }

    /**
     * Check if two blocks are the same type)
     *
     * @param block1 First block
     * @param block2 Second block
     * @return {@code true} if the blocks have the same type, {@code false} if not
     */
    public abstract boolean isBlockSame(@NonNull BlockState block1, @NonNull BlockState block2);

    /**
     * Get the player health
     *
     * @param player Player
     * @return Non-negative health
     */
    @NonNegative
    public abstract double getHealth(@NonNull PlotPlayer<?> player);

    /**
     * Set the player health
     *
     * @param player Player health
     * @param health Non-negative health
     */
    public abstract void setHealth(@NonNull PlotPlayer<?> player, @NonNegative double health);

    /**
     * Get the player food level
     *
     * @param player Player
     * @return Non-negative food level
     */
    @NonNegative
    public abstract int getFoodLevel(@NonNull PlotPlayer<?> player);

    /**
     * Set the player food level
     *
     * @param player    Player food level
     * @param foodLevel Non-negative food level
     */
    public abstract void setFoodLevel(@NonNull PlotPlayer<?> player, @NonNegative int foodLevel);

    /**
     * Get all entity types belonging to an entity category
     *
     * @param category Entity category
     * @return Set containing all entities belonging to the given category
     */
    public @NonNull
    abstract Set<EntityType> getTypesInCategory(@NonNull String category);

    /**
     * Get all recognized tile entity types
     *
     * @return Collection containing all known tile entity types
     */
    public @NonNull
    abstract Collection<BlockType> getTileEntityTypes();

    /**
     * Get the tile entity count in a chunk
     *
     * @param world World
     * @param chunk Chunk coordinates
     * @return Tile entity count
     */
    @NonNegative
    public abstract int getTileEntityCount(@NonNull String world, @NonNull BlockVector2 chunk);

}
