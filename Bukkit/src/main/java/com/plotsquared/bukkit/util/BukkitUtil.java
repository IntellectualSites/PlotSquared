package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.bukkit.object.BukkitPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sandstone;
import org.bukkit.material.Step;
import org.bukkit.material.Tree;
import org.bukkit.material.WoodenStep;
import org.bukkit.material.Wool;

public class BukkitUtil extends WorldUtil {

    private static String lastString = null;
    private static World lastWorld = null;

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;

    public static void removePlayer(String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }

    public static PlotPlayer getPlayer(OfflinePlayer op) {
        if (op.isOnline()) {
            return getPlayer(op.getPlayer());
        }
        Player player = OfflinePlayerUtil.loadPlayer(op);
        player.loadData();
        return new BukkitPlayer(player, true);
    }

    public static PlotPlayer getPlayer(Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        String name = player.getName();
        PlotPlayer plotPlayer = UUIDHandler.getPlayer(name);
        if (plotPlayer != null) {
            return plotPlayer;
        }
        lastPlotPlayer = new BukkitPlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Location getLocation(org.bukkit.Location location) {
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()), MathMan.roundInt(location.getY()),
                MathMan.roundInt(location.getZ()));
    }

    public static org.bukkit.Location getLocation(Location location) {
        return new org.bukkit.Location(getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ());
    }

    public static World getWorld(String string) {
        return Bukkit.getWorld(string);
    }

    public static String getWorld(Entity entity) {
        return entity.getWorld().getName();
    }

    public static List<Entity> getEntities(String worldName) {
        World world = getWorld(worldName);
        return world != null ? world.getEntities() : new ArrayList<Entity>();
    }

    public static Location getLocation(Entity entity) {
        org.bukkit.Location location = entity.getLocation();
        String world = location.getWorld().getName();
        return new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Location getLocationFull(Entity entity) {
        org.bukkit.Location location = entity.getLocation();
        return new Location(location.getWorld().getName(), MathMan.roundInt(location.getX()), MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()),
                location.getYaw(), location.getPitch());
    }

    @Override
    public boolean isWorld(String worldName) {
        return getWorld(worldName) != null;
    }

    @Override
    public String getBiome(String world, int x, int z) {
        return getWorld(world).getBiome(x, z).name();
    }

    @Override
    public void setSign(String worldName, int x, int y, int z, String[] lines) {
        World world = getWorld(worldName);
        Block block = world.getBlockAt(x, y, z);
        //        block.setType(Material.AIR);
        block.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
        BlockState blockstate = block.getState();
        if (blockstate instanceof Sign) {
            final Sign sign = (Sign) blockstate;
            for (int i = 0; i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true);
        }
    }

    @Override
    public String[] getSign(Location location) {
        Block block = getWorld(location.getWorld()).getBlockAt(location.getX(), location.getY(), location.getZ());
        if (block != null) {
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                return sign.getLines();
            }
        }
        return null;
    }

    @Override
    public Location getSpawn(PlotPlayer player) {
        return getLocation(((BukkitPlayer) player).player.getBedSpawnLocation());
    }

    @Override
    public Location getSpawn(String world) {
        org.bukkit.Location temp = getWorld(world).getSpawnLocation();
        return new Location(world, temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(), temp.getYaw(), temp.getPitch());
    }

    @Override
    public void setSpawn(Location location) {
        World world = getWorld(location.getWorld());
        if (world != null) {
            world.setSpawnLocation(location.getX(), location.getY(), location.getZ());
        }
    }

    @Override
    public void saveWorld(String worldName) {
        World world = getWorld(worldName);
        if (world != null) {
            world.save();
        }
    }

    @Override
    public int getHighestBlock(String world, int x, int z) {
        World bukkitWorld = getWorld(world);
        // Skip top and bottom block
        int air = 1;
        for (int y = bukkitWorld.getMaxHeight() - 1; y >= 0; y--) {
            Block block = bukkitWorld.getBlockAt(x, y, z);
            if (block != null) {
                Material type = block.getType();
                if (type.isSolid()) {
                    if (air > 1) return y + 1;
                    air = 0;
                } else {
                    switch (type) {
                        case WATER:
                        case LAVA:
                        case STATIONARY_LAVA:
                        case STATIONARY_WATER:
                            return y;
                    }
                    air++;
                }
            }
        }
        return bukkitWorld.getMaxHeight();
    }

    @Override
    public int getBiomeFromString(String biomeString) {
        try {
            Biome biome = Biome.valueOf(biomeString.toUpperCase());
            return Arrays.asList(Biome.values()).indexOf(biome);
        } catch (IllegalArgumentException ignored) {
            return -1;
        }
    }

    @Override
    public String[] getBiomeList() {
        Biome[] biomes = Biome.values();
        String[] list = new String[biomes.length];
        for (int i = 0; i < biomes.length; i++) {
            list[i] = biomes[i].name();
        }
        return list;
    }

    @Override
    public boolean addItems(String worldName, PlotItem items) {
        World world = getWorld(worldName);
        Block block = world.getBlockAt(items.x, items.y, items.z);
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (state instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) state;
            Inventory inv = holder.getInventory();
            for (int i = 0; i < items.id.length; i++) {
                ItemStack item = new ItemStack(items.id[i], items.amount[i], items.data[i]);
                inv.addItem(item);
            }
            state.update(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBlockSolid(PlotBlock block) {
        try {
            Material material = Material.getMaterial(block.id);
            if (material.isBlock() && material.isSolid() && !material.hasGravity()) {
                Class<? extends MaterialData> data = material.getData();
                if (data.equals(MaterialData.class) && !material.isTransparent() && material.isOccluding()
                        || data.equals(Tree.class)
                        || data.equals(Sandstone.class)
                        || data.equals(Wool.class)
                        || data.equals(Step.class)
                        || data.equals(WoodenStep.class)) {
                    switch (material) {
                        case NOTE_BLOCK:
                        case MOB_SPAWNER:
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

    @Override
    public String getClosestMatchingName(PlotBlock block) {
        try {
            return Material.getMaterial(block.id).name();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name) {
        try {
            Material material = Material.valueOf(name.toUpperCase());
            return new StringComparison<PlotBlock>().new ComparisonResult(0, PlotBlock.get((short) material.getId(), (byte) 0));
        } catch (IllegalArgumentException ignored) {}
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
                StringComparison<Material>.ComparisonResult comparison = new StringComparison<>(name, Material.values()).getBestMatchAdvanced();
                match = comparison.match;
                id = (short) comparison.best.getId();
            }
            PlotBlock block = PlotBlock.get(id, data);
            StringComparison<PlotBlock> outer = new StringComparison<>();
            return outer.new ComparisonResult(match, block);

        } catch (NumberFormatException ignored) {}
        return null;
    }

    @Override
    public void setBiomes(String worldName, RegionWrapper region, String biomeString) {
        World world = getWorld(worldName);
        Biome biome = Biome.valueOf(biomeString.toUpperCase());
        for (int x = region.minX; x <= region.maxX; x++) {
            for (int z = region.minZ; z <= region.maxZ; z++) {
                world.setBiome(x, z, biome);
            }
        }
    }

    @Override
    public PlotBlock getBlock(Location location) {
        World world = getWorld(location.getWorld());
        Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
        if (block == null) {
            return PlotBlock.EVERYTHING;
        }
        return PlotBlock.get((short) block.getTypeId(), block.getData());
    }

    @Override
    public String getMainWorld() {
        return Bukkit.getWorlds().get(0).getName();
    }
}
