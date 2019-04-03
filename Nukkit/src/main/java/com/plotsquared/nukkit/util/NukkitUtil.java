package com.plotsquared.nukkit.util;

import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockWallSign;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.math.Vector3;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.nukkit.NukkitMain;
import com.plotsquared.nukkit.object.NukkitPlayer;
import java.util.ArrayList;

public class NukkitUtil extends WorldUtil {

    private static String lastString = null;
    private static Level lastWorld = null;

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;
    private static NukkitMain plugin;

    public NukkitUtil(NukkitMain plugin) {
        this.plugin = plugin;
    }

    public static void removePlayer(String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }

    public static PlotPlayer getPlayer(OfflinePlayer op) {
        if (op.isOnline()) {
            return getPlayer(op.getPlayer());
        }
        return null;
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
        lastPlotPlayer = new NukkitPlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Location getLocation(cn.nukkit.level.Location location) {
        return new Location(location.getLevel().getName(), MathMan.roundInt(location.getX()), MathMan.roundInt(location.getY()),
                MathMan.roundInt(location.getZ()));
    }

    public static Location getLocation(cn.nukkit.level.Position location) {
        return new Location(location.getLevel().getName(), MathMan.roundInt(location.getX()), MathMan.roundInt(location.getY()),
                MathMan.roundInt(location.getZ()));
    }

    public static cn.nukkit.level.Location getLocation(Location location) {
        return new cn.nukkit.level.Location(location.getX(), location.getY(), location.getZ(), 0, 0, getWorld(location.getWorld()));
    }

    public static Level getWorld(String string) {
        if (StringMan.isEqual(string, lastString)) {
            if (lastWorld != null) {
                return lastWorld;
            }
        }
        Level world = plugin.getServer().getLevelByName(string);
        lastString = string;
        lastWorld = world;
        return world;
    }

    public static String getWorld(Entity entity) {
        return entity.getLevel().getName();
    }

    public static Entity[] getEntities(String worldName) {
        return getWorld(worldName).getEntities();
    }

    public static Location getLocation(Entity entity) {
        cn.nukkit.level.Location location = entity.getLocation();
        String world = location.getLevel().getName();
        return new Location(world, location.getFloorX(), location.getFloorY(), location.getFloorZ());
    }

    public static Location getLocationFull(Entity entity) {
        cn.nukkit.level.Location location = entity.getLocation();
        return new Location(location.getLevel().getName(), MathMan.roundInt(location.getX()), MathMan.roundInt(location.getY()), MathMan.roundInt(location.getZ()),
                (float) location.getYaw(), (float) location.getPitch());
    }

    @Override
    public boolean isWorld(String worldName) {
        return getWorld(worldName) != null;
    }

    @Override
    public String getBiome(String world, int x, int z) {
        int id = getWorld(world).getBiomeId(x, z);
        return EnumBiome.getBiome(id).getName();
    }

    @Override
    public void setSign(String worldName, int x, int y, int z, String[] lines) {
        Level world = getWorld(worldName);
        BlockWallSign sign = new BlockWallSign(0);
        Vector3 pos = new Vector3(x, y, z);
        world.setBlock(pos, sign);
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof BlockEntitySign) {
            ((BlockEntitySign) tile).setText(lines[0], lines[1], lines[2], lines[3]);
            tile.scheduleUpdate();
        }
    }

    @Override
    public String[] getSign(Location location) {
        Level world = getWorld(location.getWorld());
        Vector3 pos = new Vector3(location.getX(), location.getY(), location.getZ());
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof BlockEntitySign) {
            return ((BlockEntitySign) tile).getText();
        }
        return null;
    }

    @Override
    public Location getSpawn(PlotPlayer player) {
        return getLocation(((NukkitPlayer) player).player.getSpawn());
    }

    @Override
    public Location getSpawn(String world) {
        Position loc = getWorld(world).getSpawnLocation();
        return new Location(world, loc.getFloorX(), loc.getFloorY(), loc.getFloorZ(), 0, 0);
    }

    @Override
    public void setSpawn(Location location) {
        Level world = getWorld(location.getWorld());
        if (world != null) {
            world.setSpawnLocation(new Vector3(location.getX(), location.getY(), location.getZ()));
        }
    }

    @Override
    public void saveWorld(String worldName) {
        Level world = getWorld(worldName);
        if (world != null) {
            world.save();
        }
    }

    @Override
    public int getHighestBlock(String world, int x, int z) {
        return getWorld(world).getHeightMap(x, z);
    }

    @Override
    public int getBiomeFromString(String biomeString) {
        try {
            Biome biome = EnumBiome.getBiome(biomeString.toUpperCase());
            return biome.getId();
        } catch (Throwable ignored) {
            return -1;
        }
    }

    @Override
    public String[] getBiomeList() {
        ArrayList<String> biomes = new ArrayList<>();
        for (Biome biome : Biome.biomes) {
            biomes.add(biome.getName());
        }
        return biomes.toArray(new String[biomes.size()]);
    }

    @Override
    public boolean addItems(String worldName, PlotItem items) {
        return false;
    }

    @Override
    public boolean isBlockSolid(PlotBlock block) {
        try {
            Item item = Item.get(block.id, (int) block.data);
            return (item != null && item.canBePlaced() && !Block.transparent[item.getId()] && Block.solid[item.getId()]);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return false;
        }
    }

    @Override
    public String getClosestMatchingName(PlotBlock block) {
        try {
            return Item.get(block.id, (int) block.data).getName();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name) {
        try {
            Item item = Item.fromString(name);
            return new StringComparison<PlotBlock>().new ComparisonResult(0, PlotBlock.get(item.getId(), item.getDamage()));
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
                StringComparison<Item>.ComparisonResult comparison = new StringComparison<>(name, Item.getCreativeItems()).getBestMatchAdvanced();
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
        Level world = getWorld(worldName);
        try {
            int biome = EnumBiome.getBiome(biomeString).getId();
            for (int x = region.minX; x <= region.maxX; x++) {
                for (int z = region.minZ; z <= region.maxZ; z++) {
                    world.setBiomeId(x, z, (byte) biome);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlotBlock getBlock(Location location) {
        Level world = getWorld(location.getWorld());
        int id = world.getBlockIdAt(location.getX(), location.getY(), location.getZ());
        if (id == 0) {
            return PlotBlock.get(0, 0);
        }
        int data = world.getBlockDataAt(location.getX(), location.getY(), location.getZ());
        return PlotBlock.get(id, data);
    }

    @Override
    public String getMainWorld() {
        return plugin.getServer().getDefaultLevel().getName();
    }
}
