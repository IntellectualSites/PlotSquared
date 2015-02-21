package com.intellectualcrafters.plot.util.bukkit;

import java.util.ArrayList;
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

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.BlockManager;

public class BukkitUtil extends BlockManager {
    private static HashMap<String, World> worlds = new HashMap<>();
    private static String lastString = null;
    private static World lastWorld = null;
    
    public static boolean loadChunk(String world, ChunkLoc loc) {
        return getWorld(world).getChunkAt(loc.x << 4, loc.z << 4).load(false);
    }
    
    public static Biome getBiome(final Location loc) {
        return getWorld(loc.getWorld()).getBiome(loc.getX(), loc.getZ());
    }
    
    public static Location getLocation(org.bukkit.Location loc) {
        return new Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
    
    public static void update(final String world, final int x, final int z) {
        final ArrayList<Chunk> chunks = new ArrayList<>();
        final int distance = Bukkit.getViewDistance();
        for (int cx = -distance; cx < distance; cx++) {
            for (int cz = -distance; cz < distance; cz++) {
                final Chunk chunk = getChunkAt(world, (x >> 4) + cx, (z >> 4) + cz);
                chunks.add(chunk);
            }
        }
        SetBlockManager.setBlockManager.update(chunks);
    }
    
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
            SetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        } catch (final Throwable e) {
            SetBlockManager.setBlockManager = new SetBlockSlow();
            SetBlockManager.setBlockManager.set(world, x, y, z, id, data);
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
    
    public static void refreshChunk(final String world, final int x, final int z) {
        getWorld(world).refreshChunk(x, z);
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
    public void functionSetBiomes(String worldname, int[] x, int[] z, int[] biome) {
        World world = getWorld(worldname);
        Biome[] biomes = Biome.values();
        for (int i = 0; i < x.length; i++) {
            world.setBiome(x[i], z[i], biomes[biome[i]]);
        }
    }

    @Override
    public void functionSetBlock(String worldname, int x, int y, int z, int id, byte data) {
        BukkitUtil.setBlock(getWorld(worldname), x, y, z, id, data);
    }
}
