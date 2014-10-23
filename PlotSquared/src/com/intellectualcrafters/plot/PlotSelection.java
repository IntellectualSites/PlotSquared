package com.intellectualcrafters.plot;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

import com.sk89q.worldedit.blocks.TileEntityBlock;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Citymonstret on 2014-10-12.
 */
public class PlotSelection {

    public static HashMap<String, PlotSelection> currentSelection = new HashMap<>();

    private PlotBlock[] plotBlocks;

    private int width;

    private Plot plot;
    
    private Biome biome;
    
    private BlockState[] tiles = null;
    
    private Location origin = null;

    public PlotSelection(int width, World world, Plot plot) {
        this.width = width;
        this.plot = plot;

        plotBlocks = new PlotBlock[(width * width) * (world.getMaxHeight() - 1)];

        Location
                bot = PlotHelper.getPlotBottomLocAbs(world, plot.getId()),
                top = PlotHelper.getPlotTopLocAbs(world, plot.getId());
        int
                minX = bot.getBlockX(),
                maxX = top.getBlockX(),
                minZ = bot.getBlockZ(),
                maxZ = top.getBlockZ(),
                minY = 1,
                maxY = world.getMaxHeight();
        Block current;
        
        this.biome = world.getBiome(minX, minZ);
        
        ArrayList<BlockState> states = new ArrayList<BlockState>();
        
        for (int i = (bot.getBlockX() / 16) * 16; i < (16 + ((top.getBlockX() / 16) * 16)); i += 16) {
            for (int j = (bot.getBlockZ() / 16) * 16; j < (16 + ((top.getBlockZ() / 16) * 16)); j += 16) {
                Chunk chunk = world.getChunkAt(i, j);
                
                for (BlockState tile :chunk.getTileEntities()) {
                    PlotId id = PlayerFunctions.getPlot(tile.getLocation());
                    if ((id != null) && id.equals(plot.id)) {
                        states.add(tile);
                    }
                }
            }
        }
        if (states.size() > 0) {
            this.tiles = (BlockState[]) states.toArray();
            this.origin = bot;
        }
        

        int index = 0;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    current = world.getBlockAt(x + 1, y, z + 1);
                    plotBlocks[index++] = new PlotBlock(
                            (short) current.getTypeId(),
                            current.getData()
                    );
                }
            }
        }
        
        
        
        //Yay :D
    }

    public PlotBlock[] getBlocks() {
        return plotBlocks;
    }

    public int getWidth() {
        return width;
    }

    public Plot getPlot() {
        return plot;
    }
    
    public static boolean swap(World world, Plot plot1, Plot plot2) {
        
        Location bot2 = PlotHelper.getPlotBottomLocAbs(world, plot2.getId()).add(1, 0, 1);
        Location top2 = PlotHelper.getPlotTopLocAbs(world, plot2.getId());
        
        ArrayList<BlockState> states2 = new ArrayList<BlockState>();
        
        for (int i = (bot2.getBlockX() / 16) * 16; i < (16 + ((top2.getBlockX() / 16) * 16)); i += 16) {
            for (int j = (bot2.getBlockZ() / 16) * 16; j < (16 + ((top2.getBlockZ() / 16) * 16)); j += 16) {
                Chunk chunk = world.getChunkAt(i, j);
                
                boolean result = chunk.load(false);
                if (!result) {
                    return false;
                }
                
                for (BlockState tile :chunk.getTileEntities()) {
                    PlotId id = PlayerFunctions.getPlot(tile.getLocation());
                    if ((id != null) && id.equals(plot2.id)) {
                        states2.add(tile);
                    }
                }
            }
        }
        
        Location bot1 = PlotHelper.getPlotBottomLocAbs(world, plot1.getId()).add(1, 0, 1);
        Location top1 = PlotHelper.getPlotTopLocAbs(world, plot1.getId());
        
        ArrayList<BlockState> states1 = new ArrayList<BlockState>();
        
        for (int i = (bot1.getBlockX() / 16) * 16; i < (16 + ((top1.getBlockX() / 16) * 16)); i += 16) {
            for (int j = (bot1.getBlockZ() / 16) * 16; j < (16 + ((top1.getBlockZ() / 16) * 16)); j += 16) {
                Chunk chunk = world.getChunkAt(i, j);
                
                boolean result = chunk.load(false);
                if (!result) {
                    return false;
                }
                
                for (BlockState tile :chunk.getTileEntities()) {
                    PlotId id = PlayerFunctions.getPlot(tile.getLocation());
                    if ((id != null) && id.equals(plot1.id)) {
                        states1.add(tile);
                    }
                }
            }
        }
        
        int
        minX = bot1.getBlockX(),
        maxX = top1.getBlockX(),
        minZ = bot1.getBlockZ(),
        maxZ = top1.getBlockZ(),
        
        minX2 = bot2.getBlockX(),
        minZ2 = bot2.getBlockZ();
        
        boolean canSetFast = PlotHelper.canSetFast;
        
        for (int x = 0; x < maxX - minX; x++) {
            for (int z = 0; z < maxZ - minZ; z++) {
                for (int y = 1; y < world.getMaxHeight(); y++) {
                    
                    final Block block1 = world.getBlockAt(x + minX, y, z + minZ);
                    final Block block2 = world.getBlockAt(x + minX2, y, z + minZ2);
                    
                    
                    final BlockWrapper b1 = wrapBlock(block1);
                    final BlockWrapper b2 = wrapBlock(block2);
                    
                    if (b1.id != b2.id || b1.data != b2.data) {
                        if (canSetFast) {
                            try {
                                SetBlockFast.set(world, b1.x, b1.y, b1.z, b2.id, b2.data);
                                SetBlockFast.set(world, b2.x, b2.y, b2.z, b1.id, b1.data);
                            } catch (NoSuchMethodException e) {
                                PlotHelper.canSetFast = false;
                            }
                        }
                        else {
                            if (b1.id != b2.id && b1.data != b2.data) {
                                block1.setTypeIdAndData(b2.id, b2.data, false);
                                block2.setTypeIdAndData(b1.id, b1.data, false);
                            }
                            else if (b1.id != b2.id) {
                                block1.setTypeId(b2.id);
                                block2.setTypeId(b1.id);
                            }
                            else {
                                block1.setData(b2.data);
                                block2.setData(b1.data);
                            }
                        }
                    }
                }
            }
        }
        
        for (BlockState state : states1) {
            Location loc = new Location(world,state.getX() - minX + minX2, state.getY(), state.getZ() - minZ + minZ2);
            world.getBlockAt(loc).getState().setRawData(state.getRawData());
        }
        
        for (BlockState state : states2) {
            Location loc = new Location(world,state.getX() - minX2 + minX, state.getY(), state.getZ() - minZ2 + minZ);
            world.getBlockAt(loc).getState().setRawData(state.getRawData());
        }
        
        return true;
    }
    
    private static BlockWrapper wrapBlock(Block block) {
        return new BlockWrapper(block.getX(), block.getY(), block.getZ(), (short) block.getTypeId(), block.getData());
    }

    public void paste(World world, Plot plot) {
        
        
        Location
                bot = PlotHelper.getPlotBottomLocAbs(world, plot.getId()),
                top = PlotHelper.getPlotTopLocAbs(world, plot.getId());
        int
                minX = bot.getBlockX(),
                maxX = top.getBlockX(),
                minZ = bot.getBlockZ(),
                maxZ = top.getBlockZ(),
                minY = 1,
                maxY = world.getMaxHeight();
        
        if (this.biome != world.getBiome(minX, minZ)) {
            PlotHelper.setBiome(world, plot, this.biome);
        }
        
        if (this.origin!=null) {
            for (BlockState state : this.tiles) {
                Location loc = new Location(world,state.getX() - this.origin.getBlockX() + minX + 1, state.getY() - this.origin.getBlockY() + minY, state.getZ() - this.origin.getBlockZ() + minZ + 1);
                world.getBlockAt(loc).getState().setRawData(state.getRawData());
            }
        }
        
        int index = 0;
        PlotBlock current;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    current = plotBlocks[index++];
                    world.getBlockAt(x + 1, y, z + 1).setTypeIdAndData(current.id, current.data, true);
                }
            }
        }
    }
}
