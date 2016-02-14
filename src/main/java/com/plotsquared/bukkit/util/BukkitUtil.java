package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.bukkit.object.BukkitPlayer;
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

import java.util.Arrays;
import java.util.List;

public class BukkitUtil extends WorldUtil {

    private static String lastString = null;
    private static World lastWorld = null;

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;

    public static void removePlayer(final String plr) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }

    public static PlotPlayer getPlayer(final OfflinePlayer op) {
        if (op.isOnline()) {
            return getPlayer(op.getPlayer());
        }
        final Player player = OfflinePlayerUtil.loadPlayer(op);
        player.loadData();
        return new BukkitPlayer(player, true);
    }

    public static PlotPlayer getPlayer(final Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        final String name = player.getName();
        final PlotPlayer pp = UUIDHandler.getPlayer(name);
        if (pp != null) {
            return pp;
        }
        lastPlotPlayer = new BukkitPlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Location getLocation(final org.bukkit.Location loc) {
        return new Location(loc.getWorld().getName(), MathMan.roundInt(loc.getX()), MathMan.roundInt(loc.getY()), MathMan.roundInt(loc.getZ()));
    }

    public static org.bukkit.Location getLocation(final Location loc) {
        return new org.bukkit.Location(getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
    }

    public static World getWorld(final String string) {
        if (StringMan.isEqual(string, lastString)) {
            if (lastWorld != null) {
                return lastWorld;
            }
        }
        final World world = Bukkit.getWorld(string);
        lastString = string;
        lastWorld = world;
        return world;
    }

    public static String getWorld(final Entity entity) {
        return entity.getWorld().getName();
    }

    public static List<Entity> getEntities(final String worldname) {
        return getWorld(worldname).getEntities();
    }

    public static Location getLocation(final Entity entity) {
        final org.bukkit.Location loc = entity.getLocation();
        final String world = loc.getWorld().getName();
        return new Location(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Location getLocationFull(final Entity entity) {
        final org.bukkit.Location loc = entity.getLocation();
        return new Location(loc.getWorld().getName(), MathMan.roundInt(loc.getX()), MathMan.roundInt(loc.getY()), MathMan.roundInt(loc.getZ()),
                loc.getYaw(), loc.getPitch());
    }

    @Override
    public boolean isWorld(final String world) {
        return getWorld(world) != null;
    }

    @Override
    public String getBiome(final String world, final int x, final int z) {
        return getWorld(world).getBiome(x, z).name();
    }

    @Override
    public void setSign(final String worldname, final int x, final int y, final int z, final String[] lines) {
        final World world = getWorld(worldname);
        final Block block = world.getBlockAt(x, y, z);
        //        block.setType(Material.AIR);
        block.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
        final BlockState blockstate = block.getState();
        if (blockstate instanceof Sign) {
            final Sign sign = (Sign) blockstate;
            for (int i = 0; i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    sign.update(true);
                }
            }, 20);
        }
    }

    @Override
    public String[] getSign(final Location loc) {
        final Block block = getWorld(loc.getWorld()).getBlockAt(loc.getX(), loc.getY(), loc.getZ());
        if (block != null) {
            if (block.getState() instanceof Sign) {
                final Sign sign = (Sign) block.getState();
                return sign.getLines();
            }
        }
        return null;
    }

    @Override
    public Location getSpawn(final String world) {
        final org.bukkit.Location temp = getWorld(world).getSpawnLocation();
        return new Location(world, temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(), temp.getYaw(), temp.getPitch());
    }

    @Override
    public int getHeighestBlock(final String world, final int x, final int z) {
        return getWorld(world).getHighestBlockAt(x, z).getY();
    }

    @Override
    public int getBiomeFromString(final String biomeStr) {
        try {
            final Biome biome = Biome.valueOf(biomeStr.toUpperCase());
            if (biome == null) {
                return -1;
            }
            return Arrays.asList(Biome.values()).indexOf(biome);
        } catch (final IllegalArgumentException e) {
            return -1;
        }
    }

    @Override
    public String[] getBiomeList() {
        final Biome[] biomes = Biome.values();
        final String[] list = new String[biomes.length];
        for (int i = 0; i < biomes.length; i++) {
            list[i] = biomes[i].name();
        }
        return list;
    }

    @Override
    public boolean addItems(final String worldname, final PlotItem items) {
        final World world = getWorld(worldname);
        final Block block = world.getBlockAt(items.x, items.y, items.z);
        if (block == null) {
            return false;
        }
        final BlockState state = block.getState();
        if (state instanceof InventoryHolder) {
            final InventoryHolder holder = (InventoryHolder) state;
            final Inventory inv = holder.getInventory();
            for (int i = 0; i < items.id.length; i++) {
                final ItemStack item = new ItemStack(items.id[i], items.amount[i], items.data[i]);
                inv.addItem(item);
            }
            state.update(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBlockSolid(final PlotBlock block) {
        try {
            final Material material = Material.getMaterial(block.id);
            if (material.isBlock() && material.isSolid() && !material.hasGravity()) {
                final Class<? extends MaterialData> data = material.getData();
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
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public String getClosestMatchingName(final PlotBlock block) {
        try {
            return Material.getMaterial(block.id).name();
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name) {
        try {
            final Material material = Material.valueOf(name.toUpperCase());
            return new StringComparison<PlotBlock>().new ComparisonResult(0, new PlotBlock((short) material.getId(), (byte) 0));
        } catch (IllegalArgumentException e) {
        }
        try {
            byte data;
            final String[] split = name.split(":");
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
                final StringComparison<Material>.ComparisonResult comparison = new StringComparison<>(name, Material.values()).getBestMatchAdvanced();
                match = comparison.match;
                id = (short) comparison.best.getId();
            }
            final PlotBlock block = new PlotBlock(id, data);
            final StringComparison<PlotBlock> outer = new StringComparison<>();
            return outer.new ComparisonResult(match, block);

        } catch (NumberFormatException e) {
        }
        return null;
    }

    @Override
    public void setBiomes(final String worldname, RegionWrapper region, final String biomeStr) {
        final World world = getWorld(worldname);
        final Biome biome = Biome.valueOf(biomeStr.toUpperCase());
        for (int x = region.minX; x <= region.maxX; x++) {
            for (int z = region.minZ; z <= region.maxZ; z++) {
                world.setBiome(x, z, biome);
            }
        }
    }

    @Override
    public PlotBlock getBlock(final Location loc) {
        final World world = getWorld(loc.getWorld());
        final Block block = world.getBlockAt(loc.getX(), loc.getY(), loc.getZ());
        if (block == null) {
            return new PlotBlock((short) 0, (byte) 0);
        }
        return new PlotBlock((short) block.getTypeId(), block.getData());
    }

    @Override
    public String getMainWorld() {
        return Bukkit.getWorlds().get(0).getName();
    }
}
