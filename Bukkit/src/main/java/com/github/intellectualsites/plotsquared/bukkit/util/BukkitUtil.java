package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.object.schematic.PlotItem;
import com.github.intellectualsites.plotsquared.plot.util.*;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.*;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess"}) public class BukkitUtil extends WorldUtil {

    private static String lastString = null;
    private static World lastWorld = null;

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;

    public static void removePlayer(String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }

    public static PlotPlayer getPlayer(@NonNull final OfflinePlayer op) {
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

    @Override public boolean isBlockSame(PlotBlock block1, PlotBlock block2) {
        if (block1.equals(block2)) {
            return true;
        }
        Material mat1 = getMaterial(block1), mat2 = getMaterial(block2);
        return mat1 == mat2;
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
     * Get home location.
     *
     * @param plot Plot that you want to get the location for
     * @return plot bottom location
     * @see Plot
     */
    public static org.bukkit.Location getHomeLocation(Plot plot) {
        return BukkitUtil.getLocation(plot.getHome());
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
    public static PlotPlayer wrapPlayer(OfflinePlayer player) {
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
    public static PlotPlayer wrapPlayer(Player player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Gets the number of plots, which the player is able to build in.
     *
     * @param player player, for whom we're getting the plots
     * @return the number of allowed plots
     */
    public static int getAllowedPlots(Player player) {
        PlotPlayer plotPlayer = PlotPlayer.wrap(player);
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
        return PlotPlayer.wrap(player).getPlots(world);
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
     * @see MainUtil#sendMessage(PlotPlayer, C, String...)
     */
    public static void sendMessage(Player player, C caption) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), caption);
    }

    public static PlotPlayer getPlayer(@NonNull final Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        final String name = player.getName();
        final PlotPlayer plotPlayer = UUIDHandler.getPlayer(name);
        if (plotPlayer != null) {
            return plotPlayer;
        }
        lastPlotPlayer = new BukkitPlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Location getLocation(@NonNull final org.bukkit.Location location) {
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()),
            MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()));
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
        return world != null ? world.getEntities() : new ArrayList<Entity>();
    }

    public static Location getLocation(@NonNull final Entity entity) {
        final org.bukkit.Location location = entity.getLocation();
        String world = location.getWorld().getName();
        return new Location(world, location.getBlockX(), location.getBlockY(),
            location.getBlockZ());
    }

    public static Location getLocationFull(@NonNull final Entity entity) {
        final org.bukkit.Location location = entity.getLocation();
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()),
            MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()), location.getYaw(),
            location.getPitch());
    }

    @Override public boolean isWorld(@NonNull final String worldName) {
        return getWorld(worldName) != null;
    }

    @Override public String getBiome(String world, int x, int z) {
        return getWorld(world).getBiome(x, z).name();
    }

    @Override @SuppressWarnings("deprecation")
    public void setSign(@NonNull final String worldName, final int x, final int y, final int z,
        @NonNull final String[] lines) {
        final World world = getWorld(worldName);
        final Block block = world.getBlockAt(x, y, z);
        //        block.setType(Material.AIR);
        final Material type = block.getType();
        if (type != Material.SIGN && type != Material.WALL_SIGN) {
            int data = 2;
            if (world.getBlockAt(x, y, z + 1).getType().isSolid())
                data = 2;
            else if (world.getBlockAt(x + 1, y, z).getType().isSolid())
                data = 4;
            else if (world.getBlockAt(x, y, z - 1).getType().isSolid())
                data = 3;
            else if (world.getBlockAt(x - 1, y, z).getType().isSolid())
                data = 5;
            block.setType(Material.WALL_SIGN, false);
            final Sign sign = (Sign) block.getBlockData();
            sign.setRawData((byte) data);
        }
        final BlockState blockstate = block.getState();
        if (blockstate instanceof Sign) {
            final Sign sign = (Sign) blockstate;
            for (int i = 0; i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true);
        }
    }

    @Override @Nullable public String[] getSign(@NonNull final Location location) {
        Block block = getWorld(location.getWorld())
            .getBlockAt(location.getX(), location.getY(), location.getZ());
        if (block != null) {
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                return sign.getLines();
            }
        }
        return null;
    }

    @Override public Location getSpawn(@NonNull final PlotPlayer player) {
        return getLocation(((BukkitPlayer) player).player.getBedSpawnLocation());
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

    @Override public int getHighestBlock(@NonNull final String world, final int x, final int z) {
        final World bukkitWorld = getWorld(world);
        // Skip top and bottom block
        int air = 1;
        for (int y = bukkitWorld.getMaxHeight() - 1; y >= 0; y--) {
            Block block = bukkitWorld.getBlockAt(x, y, z);
            if (block != null) {
                Material type = block.getType();
                if (type.isSolid()) {
                    if (air > 1)
                        return y;
                    air = 0;
                } else {
                    switch (type) {
                        case WATER:
                        case LAVA:
                            return y;
                    }
                    air++;
                }
            }
        }
        return bukkitWorld.getMaxHeight() - 1;
    }

    @Override public int getBiomeFromString(@NonNull final String biomeString) {
        try {
            final Biome biome = Biome.valueOf(biomeString.toUpperCase());
            return Arrays.asList(Biome.values()).indexOf(biome);
        } catch (IllegalArgumentException ignored) {
            return -1;
        }
    }

    @Override public String[] getBiomeList() {
        final Biome[] biomes = Biome.values();
        final String[] list = new String[biomes.length];
        for (int i = 0; i < biomes.length; i++) {
            list[i] = biomes[i].name();
        }
        return list;
    }

    @Override
    public boolean addItems(@NonNull final String worldName, @NonNull final PlotItem items) {
        final World world = getWorld(worldName);
        final Block block = world.getBlockAt(items.x, items.y, items.z);
        if (block == null) {
            return false;
        }
        final BlockState state = block.getState();
        if (state instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) state;
            Inventory inv = holder.getInventory();
            for (int i = 0; i < items.id.length; i++) {
                ItemStack item = new ItemStack(LegacyMappings.fromLegacyId(items.id[i]).getMaterial(), items.amount[i], items.data[i]);
                inv.addItem(item);
            }
            state.update(true);
            return true;
        }
        return false;
    }

    public static Material getMaterial(@NonNull final PlotBlock plotBlock) {
        if (plotBlock instanceof StringPlotBlock) {
            return Material.getMaterial(((StringPlotBlock) plotBlock).getItemId().toUpperCase(Locale.ENGLISH));
        } else {
            final LegacyPlotBlock legacyPlotBlock = (LegacyPlotBlock) plotBlock;
            return LegacyMappings.fromLegacyId(legacyPlotBlock.getId()).getMaterial();
        }
    }

    @Override public boolean isBlockSolid(@NonNull final PlotBlock block) {
        try {
            final Material material = getMaterial(block);
            if (material.isBlock() && material.isSolid() && !material.hasGravity()) {
                Class<? extends MaterialData> data = material.getData();
                if (data.equals(MaterialData.class) && !material.isTransparent() && material
                    .isOccluding() || data.equals(Tree.class) || data.equals(Sandstone.class)
                    || data.equals(Wool.class) || data.equals(Step.class) || data
                    .equals(WoodenStep.class)) {
                    switch (material) {
                        case NOTE_BLOCK:
                        case SPAWNER:
                            return false;
                        default:
                            return true;
                    }
                }
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override public String getClosestMatchingName(@NonNull final PlotBlock block) {
        try {
            return getMaterial(block).name();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override @Nullable
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name) {
        try {
            final Material material = Material.valueOf(name.toUpperCase());
            return new StringComparison<PlotBlock>().new ComparisonResult(1,
                PlotBlock.get(name));
        } catch (IllegalArgumentException ignored) {
        }
        try {
            byte data;
            String[] split = name.split(":");
            if (split.length == 2) {
                data = Byte.parseByte(split[1]);
                name = split[0];
            } else {
                data = 0;
            }
            double match;
            short id;
            if (MathMan.isInteger(split[0])) {
                id = Short.parseShort(split[0]);
                match = 0;
            } else {
                StringComparison<Material>.ComparisonResult comparison =
                    new StringComparison<>(name, Material.values()).getBestMatchAdvanced();
                match = comparison.match;
                id = (short) comparison.best.getId();
            }
            PlotBlock block = PlotBlock.get(id, data);
            StringComparison<PlotBlock> outer = new StringComparison<>();
            return outer.new ComparisonResult(match, block);

        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public void setBiomes(@NonNull final String worldName, @NonNull final RegionWrapper region,
        @NonNull final String biomeString) {
        final World world = getWorld(worldName);
        final Biome biome = Biome.valueOf(biomeString.toUpperCase());
        for (int x = region.minX; x <= region.maxX; x++) {
            for (int z = region.minZ; z <= region.maxZ; z++) {
                world.setBiome(x, z, biome);
            }
        }
    }

    @Override public PlotBlock getBlock(@NonNull final Location location) {
        final World world = getWorld(location.getWorld());
        final Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
        if (block == null) {
            return StringPlotBlock.EVERYTHING;
        }
        return PlotBlock.get(block.getType().name());
    }

    @Override public String getMainWorld() {
        return Bukkit.getWorlds().get(0).getName();
    }
}
