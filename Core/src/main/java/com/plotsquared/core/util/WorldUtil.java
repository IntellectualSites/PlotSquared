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
import com.sk89q.jnbt.CompoundTagBuilder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class WorldUtil {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + WorldUtil.class.getSimpleName());

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
        boolean modern = MinecraftVersion.current().isNewerOrEqualThan(MinecraftVersion.TINY_TAKEOVER);
        String relativeMcaRoot = modern ? "dimensions/minecraft/overworld/region" : "region";
        plot.getHome(home -> SchematicHandler.upload(uuid, file, "zip", new RunnableVal<>() {
            @Override
            public void run(OutputStream output) {
                try (final ZipOutputStream zos = new ZipOutputStream(output)) {
                    Path dat = getDat(plot.getWorldName());
                    Location spawn = getSpawn(plot.getWorldName());
                    if (dat != null) {
                        ZipEntry ze = new ZipEntry("level.dat");
                        zos.putNextEntry(ze);
                        try (NBTInputStream nis = new NBTInputStream(new GZIPInputStream(Files.newInputStream(dat)))) {
                            CompoundTag levelData = modifyLevelData((CompoundTag) nis.readNamedTag().getTag(), home);
                            try (NBTOutputStream out =
                                         new NBTOutputStream(new GZIPOutputStream(new CloseShieldOutputStream(zos), true))) {
                                out.writeNamedTag("", levelData);
                            }
                        }
                        zos.closeEntry();
                    }
                    setSpawn(spawn);
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
                                final Path path = getMca(plot.getWorldName(), mca.getX(), mca.getZ());
                                if (path != null) {
                                    final ZipEntry ze = new ZipEntry(relativeMcaRoot + "/" + path.getFileName().toString());
                                    zos.putNextEntry(ze);
                                    added.add(mca);
                                    try (InputStream in = Files.newInputStream(path)) {
                                        in.transferTo(zos);
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

    private @Nullable Path getDat(final @NonNull String world) {
        Path path;
        if (MinecraftVersion.current().isOlderOrEqualThan(MinecraftVersion.TINY_TAKEOVER)) {
            // 26.1+ only has a global level.dat
            path = PlotSquared.platform().worldContainer().toPath().resolve("world").resolve("level.dat");
        } else {
            path = PlotSquared.platform().worldContainer().toPath().resolve(world).resolve("level.dat");
        }
        return Files.exists(path) ? path : null;
    }

    @Nullable
    private Path getMca(final @NonNull String world, final int x, final int z) {
        Path path = PlotSquared.platform().getWorldPath(world).resolve("region").
                resolve(String.format("r.%s.%s.mca", x, z));
        return Files.exists(path) ? path : null;
    }

    private CompoundTag modifyLevelData(CompoundTag input, Location home) {
        Map<String, Tag> root = input.getValue();
        if (!(root.get("Data") instanceof CompoundTag data)) {
            return input;
        }
        CompoundTagBuilder dataBuilder = data.createBuilder();
        if (MinecraftVersion.current().isNewerOrEqualThan(MinecraftVersion.TINY_TAKEOVER)) {
            if (data.getValue().get("spawn") instanceof CompoundTag spawn) {
                dataBuilder.put(
                        "spawn", spawn.createBuilder()
                                .putString("dimension", "minecraft:overworld")
                                .putIntArray("pos", new int[]{home.getX(), home.getY(), home.getZ()})
                                .build()
                );
            }
        } else {
            // legacy
            dataBuilder
                    .putInt("SpawnX", home.getX())
                    .putInt("SpawnY", home.getY())
                    .putInt("SpawnZ", home.getZ());
        }
        return input.createBuilder().put("Data", dataBuilder.build()).build();
    }


    public Set<BlockVector2> getChunkChunks(String world) {
        Path regionRoot = PlotSquared.platform().getWorldPath(world).resolve("region");
        if (!Files.exists(regionRoot)) {
            throw new RuntimeException("Could not find regions folder: " + regionRoot + " ? (no read access?)");
        }
        try (Stream<Path> stream = Files.find(regionRoot, 1, WorldUtil::isMcaRegionFile)) {
            return stream.filter(Predicate.not(p -> p.equals(regionRoot))) // skip root
                    .map(Path::getFileName)
                    .map(this::fromMcaFileName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            LOGGER.error("Failed to traverse region directory", e);
            return Set.of();
        }
    }

    /**
     * checks if the given file, by its path and BasicFileAttributes, is a mca region file
     *
     * @param path full path to file
     * @param bfa attributes of the given file
     * @return {@code true} if the given file is a seemingly valid mca region file. {@code false} otherwise
     */
    private static boolean isMcaRegionFile(Path path, BasicFileAttributes bfa) {
        if (bfa.isDirectory()) {
            return false;
        }
        String name = path.getFileName().toString();
        return name.startsWith("r.") && name.endsWith(".mca");
    }

    /**
     * Retrieves the coordinates from a region mca file
     *
     * @param filename the filename part of the full path ({@link Path#getFileName()})
     * @return A BV2 containg the coordinates, or {@code null} if the filename does not match the expected format
     */
    private BlockVector2 fromMcaFileName(Path filename) {
        String[] parts = filename.toString().split("\\.");
        if (parts.length < 3) {
            return null;
        }
        try {
            return BlockVector2.at(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
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
