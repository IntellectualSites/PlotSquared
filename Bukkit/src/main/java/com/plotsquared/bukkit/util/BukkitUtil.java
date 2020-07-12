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
package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.player.BukkitPlayerManager;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.BlockUtil;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.StringComparison;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.lib.PaperLib;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BukkitUtil extends WorldUtil {

    private static final Logger logger = LoggerFactory.getLogger(BukkitUtil.class);

    private static String lastString = null;
    private static World lastWorld = null;

    private static Player lastPlayer = null;
    private static BukkitPlayer lastPlotPlayer = null;

    public static void removePlayer(UUID uuid) {
        lastPlayer = null;
        lastPlotPlayer = null;
        // Make sure that it's removed internally
        PlotSquared.imp().getPlayerManager().removePlayer(uuid);
    }

    public static PlotPlayer<Player> getPlayer(@NonNull final OfflinePlayer op) {
        if (op.isOnline()) {
            return getPlayer(op.getPlayer());
        }
        final Player player = OfflinePlayerUtil.loadPlayer(op);
        player.loadData();
        return new BukkitPlayer(player, true);
    }

    /**
     * Get a plot based on the location.
     *
     * @param location the location to check
     * @return plot if found, otherwise it creates a temporary plot
     * @see Plot
     */
    public static Plot getPlot(org.bukkit.Location location) {
        if (location == null) {
            return null;
        }
        return getLocation(location).getPlot();
    }

    /**
     * Get a plot based on the player location.
     *
     * @param player the player to check
     * @return plot if found, otherwise it creates a temporary plot
     * @see #getPlot(org.bukkit.Location)
     * @see Plot
     */
    public static Plot getPlot(Player player) {
        return getPlot(player.getLocation());
    }

    /**
     * Get the PlotPlayer for an offline player.
     *
     * <p>Note that this will work if the player is offline, however not all
     * functionality will work.
     *
     * @param player the player to wrap
     * @return a {@code PlotPlayer}
     * @see PlotPlayer#wrap(Object)
     */
    public static PlotPlayer<?> wrapPlayer(OfflinePlayer player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Gets the PlotPlayer for a player. The PlotPlayer is usually cached and
     * will provide useful functions relating to players.
     *
     * @param player the player to wrap
     * @return a {@code PlotPlayer}
     * @see PlotPlayer#wrap(Object)
     */
    public static PlotPlayer<?> wrapPlayer(Player player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Gets the PlotPlayer for a UUID. The PlotPlayer is usually cached and
     * will provide useful functions relating to players.
     *
     * @param uuid the uuid to wrap
     * @return a {@code PlotPlayer}
     * @see PlotPlayer#wrap(Object)
     */
    @Override public PlotPlayer<?> wrapPlayer(UUID uuid) {
        return PlotPlayer.wrap(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Gets the number of plots, which the player is able to build in.
     *
     * @param player player, for whom we're getting the plots
     * @return the number of allowed plots
     */
    public static int getAllowedPlots(Player player) {
        PlotPlayer<?> plotPlayer = PlotPlayer.wrap(player);
        return plotPlayer.getAllowedPlots();
    }

    /**
     * Check whether or not a player is in a plot.
     *
     * @param player who we're checking for
     * @return true if the player is in a plot, false if not-
     */
    public static boolean isInPlot(Player player) {
        return getPlot(player) != null;
    }

    /**
     * Gets a collection containing the players plots.
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plots
     * @return a set containing the players plots
     * @see Plot
     */
    public static Set<Plot> getPlayerPlots(String world, Player player) {
        if (world == null) {
            return new HashSet<>();
        }
        return BukkitPlayer.wrap(player).getPlots(world);
    }

    /**
     * Send a message to a player. The message supports color codes.
     *
     * @param player the recipient of the message
     * @param string the message
     * @see MainUtil#sendMessage(PlotPlayer, String)
     */
    public static void sendMessage(Player player, String string) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), string);
    }

    /**
     * Gets the player plot count.
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plot count
     * @return the number of plots the player has
     */
    public static int getPlayerPlotCount(String world, Player player) {
        if (world == null) {
            return 0;
        }
        return BukkitUtil.getPlayer(player).getPlotCount(world);
    }

    /**
     * Send a message to a player.
     *
     * @param player  the recipient of the message
     * @param caption the message
     */
    public static void sendMessage(Player player, Captions caption) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), caption);
    }

    public static BukkitPlayer getPlayer(@NonNull final Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        final PlayerManager<?, ?> playerManager = PlotSquared.imp().getPlayerManager();
        return ((BukkitPlayerManager) playerManager).getPlayer(player);
    }

    public static Location getLocation(@NonNull final org.bukkit.Location location) {
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()),
            MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()));
    }

    public static Location getLocationFull(@NonNull final org.bukkit.Location location) {
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()),
            MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()), location.getYaw(),
            location.getPitch());
    }

    public static org.bukkit.Location getLocation(@NonNull final Location location) {
        return new org.bukkit.Location(getWorld(location.getWorld()), location.getX(),
            location.getY(), location.getZ());
    }

    public static World getWorld(@NonNull final String string) {
        return Bukkit.getWorld(string);
    }

    public static String getWorld(@NonNull final Entity entity) {
        return entity.getWorld().getName();
    }

    public static List<Entity> getEntities(@NonNull final String worldName) {
        World world = getWorld(worldName);
        if (world != null) {
            return world.getEntities();
        } else {
            return new ArrayList<>();
        }
    }

    public static Location getLocation(@NonNull final Entity entity) {
        final org.bukkit.Location location = entity.getLocation();
        String world = location.getWorld().getName();
        return new Location(world, location.getBlockX(), location.getBlockY(),
            location.getBlockZ());
    }

    @NotNull public static Location getLocationFull(@NonNull final Entity entity) {
        final org.bukkit.Location location = entity.getLocation();
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()),
            MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()), location.getYaw(),
            location.getPitch());
    }

    public static Material getMaterial(@NonNull final BlockState plotBlock) {
        return BukkitAdapter.adapt(plotBlock.getBlockType());
    }

    @Override public boolean isBlockSame(BlockState block1, BlockState block2) {
        if (block1.equals(block2)) {
            return true;
        }
        Material mat1 = getMaterial(block1), mat2 = getMaterial(block2);
        return mat1 == mat2;
    }

    @Override public boolean isWorld(@NonNull final String worldName) {
        return getWorld(worldName) != null;
    }

    @Override public void getBiome(String world, int x, int z, final Consumer<BiomeType> result) {
        ensureLoaded(world, x, z,
            chunk -> result.accept(BukkitAdapter.adapt(getWorld(world).getBiome(x, z))));
    }

    @Override public BiomeType getBiomeSynchronous(String world, int x, int z) {
        return BukkitAdapter.adapt(getWorld(world).getBiome(x, z));
    }

    @Override
    public void getHighestBlock(@NonNull final String world, final int x, final int z,
        final IntConsumer result) {
        ensureLoaded(world, x, z, chunk -> {
            final World bukkitWorld = getWorld(world);
            // Skip top and bottom block
            int air = 1;
            for (int y = bukkitWorld.getMaxHeight() - 1; y >= 0; y--) {
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

    @Override public int getHighestBlockSynchronous(String world, int x, int z) {
        final World bukkitWorld = getWorld(world);
        // Skip top and bottom block
        int air = 1;
        for (int y = bukkitWorld.getMaxHeight() - 1; y >= 0; y--) {
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
    public void getSign(@NonNull final Location location, final Consumer<String[]> result) {
        ensureLoaded(location, chunk -> {
            final Block block = chunk.getWorld().getBlockAt(getLocation(location));
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                result.accept(sign.getLines());
            }
        });
    }

    @Override @Nullable public String[] getSignSynchronous(@NonNull final Location location) {
        Block block = getWorld(location.getWorld())
            .getBlockAt(location.getX(), location.getY(), location.getZ());
        return TaskManager.IMP.sync(new RunnableVal<String[]>() {
            @Override public void run(String[] value) {
                if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    this.value = sign.getLines();
                }
            }
        });
    }

    @Override public Location getSpawn(@NonNull final String world) {
        final org.bukkit.Location temp = getWorld(world).getSpawnLocation();
        return new Location(world, temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(),
            temp.getYaw(), temp.getPitch());
    }

    @Override public void setSpawn(@NonNull final Location location) {
        final World world = getWorld(location.getWorld());
        if (world != null) {
            world.setSpawnLocation(location.getX(), location.getY(), location.getZ());
        }
    }

    @Override public void saveWorld(@NonNull final String worldName) {
        final World world = getWorld(worldName);
        if (world != null) {
            world.save();
        }
    }

    @Override @SuppressWarnings("deprecation")
    public void setSign(@NonNull final String worldName, final int x, final int y, final int z,
        @NonNull final String[] lines) {
        ensureLoaded(worldName, x, z, chunk -> {
            final World world = getWorld(worldName);
            final Block block = world.getBlockAt(x, y, z);
            //        block.setType(Material.AIR);
            final Material type = block.getType();
            if (type != Material.LEGACY_SIGN && type != Material.LEGACY_WALL_SIGN) {
                BlockFace facing = BlockFace.EAST;
                if (world.getBlockAt(x, y, z + 1).getType().isSolid()) {
                    facing = BlockFace.NORTH;
                } else if (world.getBlockAt(x + 1, y, z).getType().isSolid()) {
                    facing = BlockFace.WEST;
                } else if (world.getBlockAt(x, y, z - 1).getType().isSolid()) {
                    facing = BlockFace.SOUTH;
                }
                if (PlotSquared.get().IMP.getServerVersion()[1] == 13) {
                    block.setType(Material.valueOf("WALL_SIGN"), false);
                } else {
                    block.setType(Material.valueOf("OAK_WALL_SIGN"), false);
                }
                if (!(block.getBlockData() instanceof WallSign)) {
                    throw new RuntimeException("Something went wrong generating a sign");
                }
                final Directional sign = (Directional) block.getBlockData();
                sign.setFacing(facing);
                block.setBlockData(sign, false);
            }
            final org.bukkit.block.BlockState blockstate = block.getState();
            if (blockstate instanceof Sign) {
                final Sign sign = (Sign) blockstate;
                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                }
                sign.update(true);
            }
        });
    }

    @Override public boolean isBlockSolid(@NonNull final BlockState block) {
        return block.getBlockType().getMaterial().isSolid();
    }

    @Override public String getClosestMatchingName(@NonNull final BlockState block) {
        try {
            return getMaterial(block).name();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override @Nullable
    public StringComparison<BlockState>.ComparisonResult getClosestBlock(String name) {
        BlockState state = BlockUtil.get(name);
        return new StringComparison<BlockState>().new ComparisonResult(1, state);
    }

    @Override
    public void setBiomes(@NonNull final String worldName, @NonNull final CuboidRegion region,
        @NonNull final BiomeType biomeType) {
        final World world = getWorld(worldName);
        if (world == null) {
            logger.warn("An error occured while setting the biome because the world was null", new RuntimeException());
            return;
        }
        final Biome biome = BukkitAdapter.adapt(biomeType);
        for (int x = region.getMinimumPoint().getX(); x <= region.getMaximumPoint().getX(); x++) {
            for (int z = region.getMinimumPoint().getZ();
                 z <= region.getMaximumPoint().getZ(); z++) {
                if (world.getBiome(x, z) != biome) {
                    world.setBiome(x, z, biome);
                }
            }
        }
    }

    public com.sk89q.worldedit.world.World getWeWorld(String world) {
        return new BukkitWorld(Bukkit.getWorld(world));
    }

    @Override
    public void getBlock(@NonNull final Location location, final Consumer<BlockState> result) {
        ensureLoaded(location, chunk -> {
            final World world = getWorld(location.getWorld());
            final Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
            result.accept(BukkitAdapter.asBlockType(block.getType()).getDefaultState());
        });
    }

    @Override public BlockState getBlockSynchronous(@NonNull final Location location) {
        final World world = getWorld(location.getWorld());
        final Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
        return BukkitAdapter.asBlockType(block.getType()).getDefaultState();
    }

    @Override public String getMainWorld() {
        return Bukkit.getWorlds().get(0).getName();
    }

    @Override public double getHealth(PlotPlayer player) {
        return Bukkit.getPlayer(player.getUUID()).getHealth();
    }

    @Override public int getFoodLevel(PlotPlayer player) {
        return Bukkit.getPlayer(player.getUUID()).getFoodLevel();
    }

    @Override public void setHealth(PlotPlayer player, double health) {
        Bukkit.getPlayer(player.getUUID()).setHealth(health);
    }

    @Override public void setFoodLevel(PlotPlayer player, int foodLevel) {
        Bukkit.getPlayer(player.getUUID()).setFoodLevel(foodLevel);
    }

    @Override
    public Set<com.sk89q.worldedit.world.entity.EntityType> getTypesInCategory(
        final String category) {
        final Collection<Class<?>> allowedInterfaces = new HashSet<>();
        switch (category) {
            case "animal": {
                allowedInterfaces.add(IronGolem.class);
                allowedInterfaces.add(Snowman.class);
                allowedInterfaces.add(Animals.class);
                allowedInterfaces.add(WaterMob.class);
                allowedInterfaces.add(Ambient.class);
            }
            break;
            case "tameable": {
                allowedInterfaces.add(Tameable.class);
            }
            break;
            case "vehicle": {
                allowedInterfaces.add(Vehicle.class);
            }
            break;
            case "hostile": {
                allowedInterfaces.add(Shulker.class);
                allowedInterfaces.add(Monster.class);
                allowedInterfaces.add(Boss.class);
                allowedInterfaces.add(Slime.class);
                allowedInterfaces.add(Ghast.class);
                allowedInterfaces.add(Phantom.class);
                allowedInterfaces.add(EnderCrystal.class);
            }
            break;
            case "hanging": {
                allowedInterfaces.add(Hanging.class);
            }
            break;
            case "villager": {
                allowedInterfaces.add(NPC.class);
            }
            break;
            case "projectile": {
                allowedInterfaces.add(Projectile.class);
            }
            break;
            case "other": {
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
            break;
            case "player": {
                allowedInterfaces.add(Player.class);
            }
            break;
            default: {
                logger.error("Unknown entity category requested: {}", category);
            }
            break;
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

    private final Collection<BlockType> tileEntityTypes = new HashSet<>();
    @Override public Collection<BlockType> getTileEntityTypes() {
        if (this.tileEntityTypes.isEmpty()) {
            // Categories
            tileEntityTypes.addAll(BlockCategories.BANNERS.getAll());
            tileEntityTypes.addAll(BlockCategories.SIGNS.getAll());
            tileEntityTypes.addAll(BlockCategories.BEDS.getAll());
            tileEntityTypes.addAll(BlockCategories.FLOWER_POTS.getAll());
            // Individual Types
            // Add these from strings
            Stream.of("barrel", "beacon", "beehive", "bee_nest", "bell", "blast_furnace",
                "brewing_stand", "campfire", "chest", "ender_chest", "trapped_chest",
                "command_block", "end_gateway", "hopper", "jigsaw", "jubekox",
                "lectern", "note_block", "black_shulker_box", "blue_shulker_box",
                "brown_shulker_box", "cyan_shulker_box", "gray_shulker_box", "green_shulker_box",
                "light_blue_shulker_box", "light_gray_shulker_box", "lime_shulker_box",
                "magenta_shulker_box", "orange_shulker_box", "pink_shulker_box",
                "purple_shulker_box", "red_shulker_box", "shulker_box", "white_shulker_box",
                "yellow_shulker_box", "smoker", "structure_block", "structure_void")
                .map(BlockTypes::get)
                .filter(Objects::nonNull)
                .forEach(tileEntityTypes::add);
        }
        return this.tileEntityTypes;
    }

    @Override
    public int getTileEntityCount(String world, BlockVector2 chunk) {
        return Bukkit.getWorld(world).getChunkAt(chunk.getBlockX(), chunk.getBlockZ())
            .getTileEntities().length;
    }

    private static void ensureLoaded(final String world, final int x, final int z,
        final Consumer<Chunk> chunkConsumer) {
        PaperLib.getChunkAtAsync(getWorld(world), x >> 4, z >> 4, true)
            .thenAccept(chunk -> ensureMainThread(chunkConsumer, chunk));
    }

    private static void ensureLoaded(final Location location, final Consumer<Chunk> chunkConsumer) {
        PaperLib.getChunkAtAsync(getLocation(location), true)
            .thenAccept(chunk -> ensureMainThread(chunkConsumer, chunk));
    }

    private static <T> void ensureMainThread(final Consumer<T> consumer, final T value) {
        if (Bukkit.isPrimaryThread()) {
            consumer.accept(value);
        } else {
            Bukkit.getScheduler()
                .runTask(BukkitMain.getPlugin(BukkitMain.class), () -> consumer.accept(value));
        }
    }

}
