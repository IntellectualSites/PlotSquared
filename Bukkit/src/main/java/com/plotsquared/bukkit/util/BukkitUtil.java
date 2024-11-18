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
package com.plotsquared.bukkit.util;

import com.google.inject.Singleton;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.player.BukkitPlayerManager;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.BlockUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.StringComparison;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.WaterMob;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "WeakerAccess"})
@Singleton
public class BukkitUtil extends WorldUtil {

    public static final BukkitAudiences BUKKIT_AUDIENCES = BukkitAudiences.create(BukkitPlatform.getPlugin(BukkitPlatform.class));
    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + BukkitUtil.class.getSimpleName());
    private final Collection<BlockType> tileEntityTypes = new HashSet<>();

    /**
     * Turn a Bukkit {@link Player} into a PlotSquared {@link PlotPlayer}
     *
     * @param player Bukkit player
     * @return PlotSquared player
     */
    public static @NonNull BukkitPlayer adapt(final @NonNull Player player) {
        final PlayerManager<?, ?> playerManager = PlotSquared.platform().playerManager();
        return ((BukkitPlayerManager) playerManager).getPlayer(player);
    }

    /**
     * Turn a Bukkit {@link org.bukkit.Location} into a PlotSquared {@link Location}.
     * This only copies the 4-tuple (world,x,y,z) and does not include the yaw and the pitch
     *
     * @param location Bukkit location
     * @return PlotSquared location
     */
    public static @NonNull Location adapt(final org.bukkit.@NonNull Location location) {
        return Location
                .at(
                        com.plotsquared.bukkit.util.BukkitWorld.of(location.getWorld()),
                        MathMan.roundInt(location.getX()),
                        MathMan.roundInt(location.getY()),
                        MathMan.roundInt(location.getZ())
                );
    }

    /**
     * Turn a Bukkit {@link org.bukkit.Location} into a PlotSquared {@link Location}.
     * This copies the entire 6-tuple (world,x,y,z,yaw,pitch).
     *
     * @param location Bukkit location
     * @return PlotSquared location
     */
    public static @NonNull Location adaptComplete(final org.bukkit.@NonNull Location location) {
        return Location
                .at(
                        com.plotsquared.bukkit.util.BukkitWorld.of(location.getWorld()),
                        MathMan.roundInt(location.getX()),
                        MathMan.roundInt(location.getY()),
                        MathMan.roundInt(location.getZ()),
                        location.getYaw(),
                        location.getPitch()
                );
    }

    /**
     * Turn a PlotSquared {@link Location} into a Bukkit {@link org.bukkit.Location}.
     * This only copies the 4-tuple (world,x,y,z) and does not include the yaw and the pitch
     *
     * @param location PlotSquared location
     * @return Bukkit location
     */
    public static org.bukkit.@NonNull Location adapt(final @NonNull Location location) {
        return new org.bukkit.Location(
                (World) location.getWorld().getPlatformWorld(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    /**
     * Get a Bukkit {@link World} from its name
     *
     * @param string World name
     * @return World if it exists, or {@code null}
     */
    public static @Nullable World getWorld(final @NonNull String string) {
        return Bukkit.getWorld(string);
    }

    private static void ensureLoaded(
            final @NonNull String world,
            final int x,
            final int z,
            final @NonNull Consumer<Chunk> chunkConsumer
    ) {
        PaperLib.getChunkAtAsync(Objects.requireNonNull(getWorld(world)), x >> 4, z >> 4, true)
                .thenAccept(chunk -> ensureMainThread(chunkConsumer, chunk));
    }

    private static void ensureLoaded(final @NonNull Location location, final @NonNull Consumer<Chunk> chunkConsumer) {
        PaperLib.getChunkAtAsync(adapt(location), true).thenAccept(chunk -> ensureMainThread(chunkConsumer, chunk));
    }

    private static <T> void ensureMainThread(final @NonNull Consumer<T> consumer, final @NonNull T value) {
        if (Bukkit.isPrimaryThread()) {
            consumer.accept(value);
        } else {
            Bukkit.getScheduler().runTask(BukkitPlatform.getPlugin(BukkitPlatform.class), () -> consumer.accept(value));
        }
    }

    @Override
    public boolean isBlockSame(final @NonNull BlockState block1, final @NonNull BlockState block2) {
        if (block1.equals(block2)) {
            return true;
        }
        final Material mat1 = BukkitAdapter.adapt(block1.getBlockType());
        final Material mat2 = BukkitAdapter.adapt(block2.getBlockType());
        return mat1 == mat2;
    }

    @Override
    public boolean isWorld(final @NonNull String worldName) {
        return getWorld(worldName) != null;
    }

    @Override
    public void getBiome(final @NonNull String world, final int x, final int z, final @NonNull Consumer<BiomeType> result) {
        ensureLoaded(world, x, z, chunk -> result.accept(BukkitAdapter.adapt(getWorld(world).getBiome(x, z))));
    }

    @Override
    public @NonNull BiomeType getBiomeSynchronous(final @NonNull String world, final int x, final int z) {
        return BukkitAdapter.adapt(Objects.requireNonNull(getWorld(world)).getBiome(x, z));
    }

    @Override
    public void getHighestBlock(final @NonNull String world, final int x, final int z, final @NonNull IntConsumer result) {
        ensureLoaded(world, x, z, chunk -> {
            final World bukkitWorld = Objects.requireNonNull(getWorld(world));
            // Skip top and bottom block
            int air = 1;
            int maxY = com.plotsquared.bukkit.util.BukkitWorld.getMaxWorldHeight(bukkitWorld);
            int minY = com.plotsquared.bukkit.util.BukkitWorld.getMinWorldHeight(bukkitWorld);
            for (int y = maxY - 1; y >= minY; y--) {
                Block block = bukkitWorld.getBlockAt(x, y, z);
                Material type = block.getType();
                if (type.isSolid()) {
                    if (air > 1) {
                        result.accept(y);
                        return;
                    }
                    air = 0;
                } else {
                    if (block.isLiquid()) {
                        result.accept(y);
                        return;
                    }
                    air++;
                }
            }
            result.accept(bukkitWorld.getMaxHeight() - 1);
        });
    }

    @Override
    @NonNegative
    public int getHighestBlockSynchronous(final @NonNull String world, final int x, final int z) {
        final World bukkitWorld = Objects.requireNonNull(getWorld(world));
        // Skip top and bottom block
        int air = 1;
        int maxY = com.plotsquared.bukkit.util.BukkitWorld.getMaxWorldHeight(bukkitWorld);
        int minY = com.plotsquared.bukkit.util.BukkitWorld.getMinWorldHeight(bukkitWorld);
        for (int y = maxY - 1; y >= minY; y--) {
            Block block = bukkitWorld.getBlockAt(x, y, z);
            Material type = block.getType();
            if (type.isSolid()) {
                if (air > 1) {
                    return y;
                }
                air = 0;
            } else {
                if (block.isLiquid()) {
                    return y;
                }
                air++;
            }
        }
        return bukkitWorld.getMaxHeight() - 1;
    }

    @Override
    public @NonNull String[] getSignSynchronous(final @NonNull Location location) {
        Block block = Objects.requireNonNull(getWorld(location.getWorldName())).getBlockAt(
                location.getX(),
                location.getY(),
                location.getZ()
        );
        try {
            return TaskManager.getPlatformImplementation().sync(() -> {
                if (block.getState() instanceof Sign sign) {
                    return sign.getLines();
                }
                return new String[0];
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    @Override
    public @NonNull Location getSpawn(final @NonNull String world) {
        final org.bukkit.Location temp = getWorld(world).getSpawnLocation();
        return Location.at(world, temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(), temp.getYaw(), temp.getPitch());
    }

    @Override
    public void setSpawn(final @NonNull Location location) {
        final World world = getWorld(location.getWorldName());
        if (world != null) {
            world.setSpawnLocation(location.getX(), location.getY(), location.getZ());
        }
    }

    @Override
    public void saveWorld(final @NonNull String worldName) {
        final World world = getWorld(worldName);
        if (world != null) {
            world.save();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setSign(
            final @NonNull Location location, final @NonNull Caption[] lines,
            final @NonNull TagResolver... replacements
    ) {
        ensureLoaded(location.getWorldName(), location.getX(), location.getZ(), chunk -> {
            PlotArea area = location.getPlotArea();
            final World world = getWorld(location.getWorldName());
            final Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
            final Material type = block.getType();
            if (type != Material.LEGACY_SIGN && type != Material.LEGACY_WALL_SIGN) {
                BlockFace facing = BlockFace.NORTH;
                if (!world.getBlockAt(location.getX(), location.getY(), location.getZ() + 1).getType().isSolid()) {
                    if (world.getBlockAt(location.getX() - 1, location.getY(), location.getZ()).getType().isSolid()) {
                        facing = BlockFace.EAST;
                    } else if (world.getBlockAt(location.getX() + 1, location.getY(), location.getZ()).getType().isSolid()) {
                        facing = BlockFace.WEST;
                    } else if (world.getBlockAt(location.getX(), location.getY(), location.getZ() - 1).getType().isSolid()) {
                        facing = BlockFace.SOUTH;
                    }
                }
                if (PlotSquared.platform().serverVersion()[1] == 13) {
                    block.setType(Material.valueOf(area.legacySignMaterial()), false);
                } else {
                    block.setType(Material.valueOf(area.signMaterial()), false);
                }
                if (!(block.getBlockData() instanceof WallSign)) {
                    throw new RuntimeException("Something went wrong generating a sign");
                }
                final Directional sign = (Directional) block.getBlockData();
                sign.setFacing(facing);
                block.setBlockData(sign, false);
            }
            final org.bukkit.block.BlockState blockstate = block.getState();
            if (blockstate instanceof final Sign sign) {
                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, LEGACY_COMPONENT_SERIALIZER.serialize(
                            MINI_MESSAGE.deserialize(lines[i].getComponent(LocaleHolder.console()), replacements)
                    ));
                }
                sign.update(true, false);
            }
        });
    }

    @Override
    public @NonNull StringComparison<BlockState>.ComparisonResult getClosestBlock(@NonNull String name) {
        BlockState state = BlockUtil.get(name);
        return new StringComparison<BlockState>().new ComparisonResult(1, state);
    }

    @Override
    public com.sk89q.worldedit.world.@NonNull World getWeWorld(final @NonNull String world) {
        return new BukkitWorld(Bukkit.getWorld(world));
    }

    @Override
    public void refreshChunk(int x, int z, String world) {
        Bukkit.getWorld(world).refreshChunk(x, z);
    }

    @Override
    public void getBlock(final @NonNull Location location, final @NonNull Consumer<BlockState> result) {
        ensureLoaded(location, chunk -> {
            final World world = getWorld(location.getWorldName());
            final Block block = Objects.requireNonNull(world).getBlockAt(location.getX(), location.getY(), location.getZ());
            result.accept(Objects.requireNonNull(BukkitAdapter.asBlockType(block.getType())).getDefaultState());
        });
    }

    @Override
    public @NonNull BlockState getBlockSynchronous(final @NonNull Location location) {
        final World world = getWorld(location.getWorldName());
        final Block block = Objects.requireNonNull(world).getBlockAt(location.getX(), location.getY(), location.getZ());
        return Objects.requireNonNull(BukkitAdapter.asBlockType(block.getType())).getDefaultState();
    }

    @Override
    @NonNegative
    public double getHealth(final @NonNull PlotPlayer<?> player) {
        return Objects.requireNonNull(Bukkit.getPlayer(player.getUUID())).getHealth();
    }

    @Override
    @NonNegative
    public int getFoodLevel(final @NonNull PlotPlayer<?> player) {
        return Objects.requireNonNull(Bukkit.getPlayer(player.getUUID())).getFoodLevel();
    }

    @Override
    public void setHealth(final @NonNull PlotPlayer<?> player, @NonNegative final double health) {
        Objects.requireNonNull(Bukkit.getPlayer(player.getUUID())).setHealth(health);
    }

    @Override
    public void setFoodLevel(final @NonNull PlotPlayer<?> player, @NonNegative final int foodLevel) {
        Bukkit.getPlayer(player.getUUID()).setFoodLevel(foodLevel);
    }

    @Override
    public @NonNull Set<com.sk89q.worldedit.world.entity.EntityType> getTypesInCategory(final @NonNull String category) {
        final Collection<Class<?>> allowedInterfaces = new HashSet<>();
        final int[] version = PlotSquared.platform().serverVersion();
        switch (category) {
            case "animal" -> {
                allowedInterfaces.add(IronGolem.class);
                allowedInterfaces.add(Snowman.class);
                allowedInterfaces.add(Animals.class);
                allowedInterfaces.add(WaterMob.class);
                allowedInterfaces.add(Ambient.class);
                if (version[1] >= 19) {
                    allowedInterfaces.add(Allay.class);
                }
            }
            case "tameable" -> allowedInterfaces.add(Tameable.class);
            case "vehicle" -> allowedInterfaces.add(Vehicle.class);
            case "hostile" -> {
                allowedInterfaces.add(Shulker.class);
                allowedInterfaces.add(Monster.class);
                allowedInterfaces.add(Boss.class);
                allowedInterfaces.add(Slime.class);
                allowedInterfaces.add(Ghast.class);
                allowedInterfaces.add(Phantom.class);
                allowedInterfaces.add(EnderCrystal.class);
            }
            case "hanging" -> allowedInterfaces.add(Hanging.class);
            case "villager" -> allowedInterfaces.add(NPC.class);
            case "projectile" -> allowedInterfaces.add(Projectile.class);
            case "other" -> {
                allowedInterfaces.add(ArmorStand.class);
                allowedInterfaces.add(FallingBlock.class);
                allowedInterfaces.add(Item.class);
                allowedInterfaces.add(Explosive.class);
                allowedInterfaces.add(AreaEffectCloud.class);
                allowedInterfaces.add(EvokerFangs.class);
                allowedInterfaces.add(LightningStrike.class);
                allowedInterfaces.add(ExperienceOrb.class);
                allowedInterfaces.add(EnderSignal.class);
                allowedInterfaces.add(Firework.class);
            }
            case "player" -> allowedInterfaces.add(Player.class);
            case "interaction" -> {
                if ((version[1] > 19) || (version[1] == 19 && version[2] >= 4)) {
                    allowedInterfaces.add(Interaction.class);
                }
            }
            default -> LOGGER.error("Unknown entity category requested: {}", category);
        }
        final Set<com.sk89q.worldedit.world.entity.EntityType> types = new HashSet<>();
        outer:
        for (final EntityType bukkitType : EntityType.values()) {
            final Class<? extends Entity> entityClass = bukkitType.getEntityClass();
            if (entityClass == null) {
                continue;
            }
            for (final Class<?> allowedInterface : allowedInterfaces) {
                if (allowedInterface.isAssignableFrom(entityClass)) {
                    types.add(BukkitAdapter.adapt(bukkitType));
                    continue outer;
                }
            }
        }
        return types;
    }

    @Override
    public @NonNull Collection<BlockType> getTileEntityTypes() {
        if (this.tileEntityTypes.isEmpty()) {
            // Categories
            tileEntityTypes.addAll(BlockCategories.BANNERS.getAll());
            tileEntityTypes.addAll(BlockCategories.SIGNS.getAll());
            tileEntityTypes.addAll(BlockCategories.BEDS.getAll());
            tileEntityTypes.addAll(BlockCategories.FLOWER_POTS.getAll());
            // Individual Types
            // Add these from strings
            Stream.of(
                            "barrel",
                            "beacon",
                            "beehive",
                            "bee_nest",
                            "bell",
                            "blast_furnace",
                            "brewing_stand",
                            "campfire",
                            "chest",
                            "ender_chest",
                            "trapped_chest",
                            "command_block",
                            "end_gateway",
                            "hopper",
                            "jigsaw",
                            "jubekox",
                            "lectern",
                            "note_block",
                            "black_shulker_box",
                            "blue_shulker_box",
                            "brown_shulker_box",
                            "cyan_shulker_box",
                            "gray_shulker_box",
                            "green_shulker_box",
                            "light_blue_shulker_box",
                            "light_gray_shulker_box",
                            "lime_shulker_box",
                            "magenta_shulker_box",
                            "orange_shulker_box",
                            "pink_shulker_box",
                            "purple_shulker_box",
                            "red_shulker_box",
                            "shulker_box",
                            "white_shulker_box",
                            "yellow_shulker_box",
                            "smoker",
                            "structure_block",
                            "structure_void"
                    )
                    .map(BlockTypes::get).filter(Objects::nonNull).forEach(tileEntityTypes::add);
        }
        return this.tileEntityTypes;
    }

    @Override
    @NonNegative
    public int getTileEntityCount(final @NonNull String world, final @NonNull BlockVector2 chunk) {
        return Objects.requireNonNull(getWorld(world)).
                getChunkAt(chunk.getBlockX(), chunk.getBlockZ()).getTileEntities().length;
    }

    @Override
    public Set<BlockVector2> getChunkChunks(String world) {
        Set<BlockVector2> chunks = super.getChunkChunks(world);
        if (Bukkit.isPrimaryThread()) {
            for (Chunk chunk : Objects.requireNonNull(Bukkit.getWorld(world)).getLoadedChunks()) {
                BlockVector2 loc = BlockVector2.at(chunk.getX() >> 5, chunk.getZ() >> 5);
                chunks.add(loc);
            }
        } else {
            final Semaphore semaphore = new Semaphore(1);
            try {
                semaphore.acquire();
                Bukkit.getScheduler().runTask(BukkitPlatform.getPlugin(BukkitPlatform.class), () -> {
                    for (Chunk chunk : Objects.requireNonNull(Bukkit.getWorld(world)).getLoadedChunks()) {
                        BlockVector2 loc = BlockVector2.at(chunk.getX() >> 5, chunk.getZ() >> 5);
                        chunks.add(loc);
                    }
                    semaphore.release();
                });
                semaphore.acquireUninterruptibly();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return chunks;
    }

}
