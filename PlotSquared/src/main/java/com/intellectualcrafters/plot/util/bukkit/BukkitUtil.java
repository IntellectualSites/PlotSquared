package com.intellectualcrafters.plot.util.bukkit;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.SetBlockManager;
import com.intellectualcrafters.plot.util.SetBlockSlow;

public class BukkitUtil extends BlockManager {
    
    private static HashMap<String, World> worlds = new HashMap<>();
    private static String lastString = null;
    private static World lastWorld = null;
    
    public static World getWorld(String string) {
        if (lastString == string) {
            return lastWorld;
        }
        World world = worlds.get(string);
        if (world == null) {
            world = Bukkit.getWorld(string);
            worlds.put(string, world);
        }
        return world;
    }
    
    public static Chunk getChunkAt(String worldname, int x, int z) {
        World world = getWorld(worldname);
        return world.getChunkAt(x, z);
    }
    
    public static void update(String world, int x, int z) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        final int distance = Bukkit.getViewDistance();
        for (int cx = -distance; cx < distance; cx++) {
            for (int cz = -distance; cz < distance; cz++) {
                final Chunk chunk = getChunkAt(world, (x >> 4) + cx, (z >> 4) + cz);
                chunks.add(chunk);
            }
        }
        SetBlockManager.setBlockManager.update(chunks);
    }
    
    public static void setBlock(World world, int x, int y, int z, int id, byte data) {
        try {
            SetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        }
        catch (Throwable e) {
            SetBlockManager.setBlockManager = new SetBlockSlow();
            SetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        }
    }

    @Override
    public void functionSetBlock(String worldname, int[] x, int[] y, int[] z, int[] id, byte[] data) {
        World world = getWorld(worldname);
      for (int i = 0; i < x.length; i++) {
          BukkitUtil.setBlock(world, x[i], y[i], z[i], id[i], data[i]);
      }
    }

    @Override
    public void setSign(String worldname, int x, int y, int z, String[] lines) {
        World world = getWorld(worldname);
        Block block = world.getBlockAt(x, y, z);
        block.setType(Material.AIR);
        block.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
        BlockState blockstate = block.getState();
        if ((blockstate instanceof Sign)) {
            for (int i = 0; i < lines.length; i++) {
                ((Sign) blockstate).setLine(i, lines[i]);
            }
        }
    }
    
}
