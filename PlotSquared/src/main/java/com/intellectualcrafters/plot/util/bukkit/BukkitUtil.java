package com.intellectualcrafters.plot.util.bukkit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
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

import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.BlockManager;

public class BukkitUtil extends BlockManager {
    private static HashMap<String, World> worlds = new HashMap<>();
    private static String lastString = null;
    private static World lastWorld = null;

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;

    public static void removePlayer(final String plr) {
        if ((lastPlayer != null) && lastPlayer.getName().equals(plr)) {
            lastPlayer = null;
            lastPlotPlayer = null;
        }
        UUIDHandler.players.remove(plr);
    }

    @Override
    public boolean isWorld(final String world) {
        return getWorld(world) != null;
    }

    public static PlotPlayer getPlayer(final Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        final BukkitPlayer plr = (BukkitPlayer) UUIDHandler.players.get(player.getName());
        if (plr != null && plr.player == player) {
            return plr;
        }
        lastPlotPlayer = new BukkitPlayer(player);
        UUIDHandler.players.put(lastPlotPlayer.getName(), lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    @Override
    public String getBiome(final Location loc) {
        return getWorld(loc.getWorld()).getBiome(loc.getX(), loc.getZ()).name();
    }

    public static Location getLocation(final org.bukkit.Location loc) {
        return new Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    public static org.bukkit.Location getLocation(final Location loc) {
        return new org.bukkit.Location(getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
    }

    public static World getWorld(final String string) {
        if (string == lastString) {
            return lastWorld;
        }
        World world = worlds.get(string);
        if (world == null) {
            world = Bukkit.getWorld(string);
            worlds.put(string, world);
        }
        return world;
    }

    public static int getMaxHeight(final String world) {
        return getWorld(world).getMaxHeight();
    }

    public static int getHeighestBlock(final String world, final int x, final int z) {
        return getWorld(world).getHighestBlockYAt(x, z);
    }

    public static Chunk getChunkAt(final String worldname, final int x, final int z) {
        final World world = getWorld(worldname);
        return world.getChunkAt(x, z);
    }

//    public static void update(final String world, final int x, final int z) {
//        final ArrayList<Chunk> chunks = new ArrayList<>();
//        final int distance = Bukkit.getViewDistance();
//        for (int cx = -distance; cx < distance; cx++) {
//            for (int cz = -distance; cz < distance; cz++) {
//                final Chunk chunk = getChunkAt(world, (x >> 4) + cx, (z >> 4) + cz);
//                chunks.add(chunk);
//            }
//        }
//        BukkitSetBlockManager.setBlockManager.update(chunks);
//    }

    public static String getWorld(final Entity entity) {
        return entity.getWorld().getName();
    }

    public static void teleportPlayer(final Player player, final Location loc) {
        final org.bukkit.Location bukkitLoc = new org.bukkit.Location(getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
        player.teleport(bukkitLoc);
    }

    public static List<Entity> getEntities(final String worldname) {
        return getWorld(worldname).getEntities();
    }

    public static void setBlock(final World world, final int x, final int y, final int z, final int id, final byte data) {
        try {
            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        } catch (final Throwable e) {
            BukkitSetBlockManager.setBlockManager = new SetBlockSlow();
            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        }
    }

    public static void setBiome(final String worldname, final int pos1_x, final int pos1_z, final int pos2_x, final int pos2_z, final String biome) {
        final Biome b = Biome.valueOf(biome.toUpperCase());
        final World world = getWorld(worldname);
        for (int x = pos1_x; x <= pos2_x; x++) {
            for (int z = pos1_z; z <= pos2_z; z++) {
                final Block blk = world.getBlockAt(x, 0, z);
                final Biome c = blk.getBiome();
                if (c.equals(b)) {
                    x += 15;
                    continue;
                }
                blk.setBiome(b);
            }
        }
    }

    public static void refreshChunk(final String name, final int x, final int z) {
        World world = getWorld(name);
        world.refreshChunk(x, z);
        world.loadChunk(x, z);
    }

    public static void regenerateChunk(final String world, final int x, final int z) {
        World worldObj = getWorld(world);
        Chunk chunk = worldObj.getChunkAt(x, z);
        if (chunk.isLoaded() || chunk.load(false)) {
            worldObj.regenerateChunk(x, z);
        }
    }

    public static PlotBlock getBlock(final Location loc) {
        final World world = getWorld(loc.getWorld());
        final Block block = world.getBlockAt(loc.getX(), loc.getY(), loc.getZ());
        if (block == null) {
            return new PlotBlock((short) 0, (byte) 0);
        }
        return new PlotBlock((short) block.getTypeId(), block.getData());
    }

    public static Location getLocation(final Entity entity) {
        final org.bukkit.Location loc = entity.getLocation();
        final String world = loc.getWorld().getName();
        return new Location(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    public static Location getLocationFull(final Entity entity) {
        final org.bukkit.Location loc = entity.getLocation();
        final String world = loc.getWorld().getName();
        return new Location(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public void functionSetBlocks(final String worldname, final int[] x, final int[] y, final int[] z, final int[] id, final byte[] data) {
        final World world = getWorld(worldname);
        for (int i = 0; i < x.length; i++) {
            BukkitUtil.setBlock(world, x[i], y[i], z[i], id[i], data[i]);
        }
    }

    @Override
    public void functionSetSign(final String worldname, final int x, final int y, final int z, final String[] lines) {
        final World world = getWorld(worldname);
        final Block block = world.getBlockAt(x, y, z);
        block.setType(Material.AIR);
        block.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
        final BlockState blockstate = block.getState();
        if ((blockstate instanceof Sign)) {
            for (int i = 0; i < lines.length; i++) {
                ((Sign) blockstate).setLine(i, lines[i]);
            }
            ((Sign) blockstate).update(true);
        }
    }

    public static int getViewDistance() {
        return Bukkit.getViewDistance();
    }
    
    @Override
    public void functionSetBiomes(final String worldname, final int[] x, final int[] z, final int[] biome) {
        final World world = getWorld(worldname);
        final Biome[] biomes = Biome.values();
        for (int i = 0; i < x.length; i++) {
            world.setBiome(x[i], z[i], biomes[biome[i]]);
        }
    }
    
    @Override
    public void functionSetBlock(final String worldname, final int x, final int y, final int z, final int id, final byte data) {
        BukkitUtil.setBlock(getWorld(worldname), x, y, z, id, data);
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
        return new Location(world, temp.getBlockX(), temp.getBlockY(), temp.getBlockZ());
    }
    
    @Override
    public int getHeighestBlock(final Location loc) {
        return getWorld(loc.getWorld()).getHighestBlockAt(loc.getX(), loc.getZ()).getY();
    }
    
    @Override
    public int getBiomeFromString(final String biomeStr) {
        try {
            final Biome biome = Biome.valueOf(biomeStr.toUpperCase());
            if (biome == null) {
                return -1;
            }
            return Arrays.asList(Biome.values()).indexOf(biome);
        }
        catch (IllegalArgumentException e) {
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
    public int getBlockIdFromString(final String block) {
        final Material material = Material.valueOf(block.toUpperCase());
        if (material == null) {
            return -1;
        }
        return material.getId();
    }

    @Override
    public boolean addItems(String worldname, PlotItem items) {
        World world = getWorld(worldname);
        Block block = world.getBlockAt(items.x, items.y, items.z);
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (state != null && state instanceof InventoryHolder) {
            InventoryHolder holder = ((InventoryHolder) state);
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
            return material.isBlock() && material.isSolid() && material.isOccluding() && !material.hasGravity();
        }
        catch (Exception e) {
            return false;
        }
    }
}
