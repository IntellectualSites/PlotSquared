package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.RegionWrapper;

public class ChunkManager {
    
    public static RegionWrapper CURRENT_PLOT_CLEAR = null;
    public static HashMap<ChunkLoc, HashMap<Short, Short>> GENERATE_BLOCKS = new HashMap<>();
    public static HashMap<ChunkLoc, HashMap<Short, Byte>> GENERATE_DATA = new HashMap<>();
    
    public static ArrayList<ChunkLoc> getChunkChunks(World world) {
        File[] regionFiles = new File(new File(".").getAbsolutePath() + File.separator + world.getName() + File.separator + "region").listFiles();
        ArrayList<ChunkLoc> chunks = new ArrayList<>();
        for (File file : regionFiles) {
            String name = file.getName();
            if (name.endsWith("mca")) {
                String[] split = name.split("\\.");
                try {
                    chunks.add(new ChunkLoc(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                } catch (Exception e) {  }
            }
        }
        return chunks;
    }
    
    public static void deleteRegionFile(final String world, final ChunkLoc loc) {
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                String directory = new File(".").getAbsolutePath() + File.separator + world + File.separator + "region" + File.separator + "r." + loc.x + "." + loc.z + ".mca";
                File file = new File(directory);
                PlotMain.sendConsoleSenderMessage("&6 - Deleted region "+file.getName()+" (max 256 chunks)");
                if (file.exists()) {
                    file.delete();
                }
            }
        });
    }
    
    public static boolean hasPlot(World world, Chunk chunk) {
        int x1 = chunk.getX() << 4;
        int z1 = chunk.getZ() << 4;
        int x2 = x1 + 15;
        int z2 = z1 + 15;
        
        Location bot = new Location(world, x1, 0, z1);
        Plot plot;
        plot = PlotHelper.getCurrentPlot(bot); 
        if (plot != null && plot.owner != null) {
            return true;
        }
        Location top = new Location(world, x2, 0, z2);
        plot = PlotHelper.getCurrentPlot(top); 
        if (plot != null && plot.owner != null) {
            return true;
        }
        return false;
    }
    
    public static boolean clearPlotExperimental(final World world, final Plot plot, final boolean isDelete) {
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
        
        Chunk c1 = world.getChunkAt(pos1);
        Chunk c2 = world.getChunkAt(pos2);

        CURRENT_PLOT_CLEAR = new RegionWrapper(pos1.getBlockX(), pos2.getBlockX(), pos1.getBlockZ(), pos2.getBlockZ());
        
        int sx = pos1.getBlockX();
        int sz = pos1.getBlockZ();
        int ex = pos2.getBlockX();
        int ez = pos2.getBlockZ();
        
        int c1x = c1.getX();
        int c1z = c1.getZ();
        int c2x = c2.getX();
        int c2z = c2.getZ();

        int maxY = world.getMaxHeight();
        
        for (int x = c1x; x <= c2x; x ++) {
            for (int z = c1z; z <= c2z; z ++) {
                Chunk chunk = world.getChunkAt(x, z);
                boolean loaded = true;
                
                if (!chunk.isLoaded()) {
                    boolean result = chunk.load(false);
                    if (!result) {
                        loaded = false;;
                    }
                    if (!chunk.isLoaded()) {
                        loaded = false;
                    }
                }
                
                if (loaded) {
                    int absX = x << 4;
                    int absZ = z << 4;
                    
                    GENERATE_BLOCKS = new HashMap<>();
                    GENERATE_DATA = new HashMap<>();
                    
                    HashMap<BlockLoc, ItemStack[]> chestContents = new HashMap<>();
                    HashMap<BlockLoc, ItemStack[]> furnaceContents = new HashMap<>();
                    HashMap<BlockLoc, ItemStack[]> dispenserContents = new HashMap<>();
                    HashMap<BlockLoc, ItemStack[]> brewingStandContents = new HashMap<>();
                    HashMap<BlockLoc, ItemStack[]> beaconContents = new HashMap<>();
                    HashMap<BlockLoc, ItemStack[]> hopperContents = new HashMap<>();
                    HashMap<BlockLoc, Note> noteBlockContents = new HashMap<>();
                    HashMap<BlockLoc, String[]> signContents = new HashMap<>();
                    
                    
                    if (x == c1x || z == c1z) {
                        for (int X = 0; X < 16; X++) {
                            for (int Z = 0; Z < 16; Z++) {
                                if ((X + absX < sx || Z + absZ < sz) || (X + absX > ex || Z + absZ > ez)) {
                                    HashMap<Short, Short> ids = new HashMap<>();
                                    HashMap<Short, Byte> datas = new HashMap<>();
                                    for (short y = 1; y < maxY; y++) {
                                        Block block = world.getBlockAt(X + absX, y, Z + absZ);
                                        short id = (short) block.getTypeId();
                                        if (id != 0) {
                                            ids.put(y, id);
                                            byte data = block.getData();
                                            if (data != 0) {
                                                datas.put(y, data);
                                            }
                                            BlockLoc bl;
                                            switch (id) {
                                                case 54:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Chest chest = (Chest) block.getState();
                                                    ItemStack[] inventory = chest.getBlockInventory().getContents().clone();
                                                    chestContents.put(bl, inventory);
                                                    break;
                                                case 63: case 68: case 323:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Sign sign = (Sign) block.getState();
                                                    sign.getLines();
                                                    signContents.put(bl, sign.getLines().clone());
                                                    break;
                                                case 61: case 62:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Furnace furnace = (Furnace) block.getState();
                                                    ItemStack[] invFur = furnace.getInventory().getContents().clone();
                                                    furnaceContents.put(bl, invFur);
                                                    break;
                                                case 23:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Dispenser dispenser = (Dispenser) block.getState();
                                                    ItemStack[] invDis = dispenser.getInventory().getContents().clone();
                                                    dispenserContents.put(bl, invDis);
                                                    break;
                                                case 117:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    BrewingStand brewingStand = (BrewingStand) block.getState();
                                                    ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                                                    brewingStandContents.put(bl, invBre);
                                                    break;
                                                case 25:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    NoteBlock noteBlock = (NoteBlock) block.getState();
                                                    Note note = noteBlock.getNote();
                                                    noteBlockContents.put(bl, note);
                                                    break;
                                                case 138:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Beacon beacon = (Beacon) block.getState();
                                                    ItemStack[] invBea = beacon.getInventory().getContents().clone();
                                                    beaconContents.put(bl, invBea);
                                                    break;
                                                case 154:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Hopper hopper = (Hopper) block.getState();
                                                    ItemStack[] invHop = hopper.getInventory().getContents().clone();
                                                    hopperContents.put(bl, invHop);
                                                    break;
                                            }
                                        }
                                    }
                                    ChunkLoc loc = new ChunkLoc(X + absX, Z + absZ);
                                    GENERATE_BLOCKS.put(loc, ids);
                                    GENERATE_DATA.put(loc, datas);
                                }
                            }
                        }
                    }
                    else if (x == c2x || z == c2z) {
                        for (int X = 0; X < 16; X++) {
                            for (int Z = 0; Z < 16; Z++) {
                                if ((X + absX > ex || Z + absZ > ez) || (X + absX < sx || Z + absZ < sz)) {
                                    HashMap<Short, Short> ids = new HashMap<>();
                                    HashMap<Short, Byte> datas = new HashMap<>();
                                    for (short y = 1; y < maxY; y++) {
                                        Block block = world.getBlockAt(X + absX, y, Z + absZ);
                                        short id = (short) block.getTypeId();
                                        if (id != 0) {
                                            ids.put(y, id);
                                            byte data = block.getData();
                                            if (data != 0) {
                                                datas.put(y, data);
                                            }
                                            BlockLoc bl;
                                            switch (id) {
                                                case 54:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Chest chest = (Chest) block.getState();
                                                    ItemStack[] inventory = chest.getBlockInventory().getContents().clone();
                                                    chestContents.put(bl, inventory);
                                                    break;
                                                case 63: case 68: case 323:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Sign sign = (Sign) block.getState();
                                                    sign.getLines();
                                                    signContents.put(bl, sign.getLines().clone());
                                                    break;
                                                case 61: case 62:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Furnace furnace = (Furnace) block.getState();
                                                    ItemStack[] invFur = furnace.getInventory().getContents().clone();
                                                    furnaceContents.put(bl, invFur);
                                                    break;
                                                case 23:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Dispenser dispenser = (Dispenser) block.getState();
                                                    ItemStack[] invDis = dispenser.getInventory().getContents().clone();
                                                    dispenserContents.put(bl, invDis);
                                                    break;
                                                case 117:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    BrewingStand brewingStand = (BrewingStand) block.getState();
                                                    ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                                                    brewingStandContents.put(bl, invBre);
                                                    break;
                                                case 25:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    NoteBlock noteBlock = (NoteBlock) block.getState();
                                                    Note note = noteBlock.getNote();
                                                    noteBlockContents.put(bl, note);
                                                    break;
                                                case 138:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Beacon beacon = (Beacon) block.getState();
                                                    ItemStack[] invBea = beacon.getInventory().getContents().clone();
                                                    beaconContents.put(bl, invBea);
                                                    break;
                                                case 154:
                                                    bl = new BlockLoc(X + absX, y, Z + absZ);
                                                    Hopper hopper = (Hopper) block.getState();
                                                    ItemStack[] invHop = hopper.getInventory().getContents().clone();
                                                    hopperContents.put(bl, invHop);
                                                    break;
                                            }
                                        }
                                    }
                                    ChunkLoc loc = new ChunkLoc(X + absX, Z + absZ);
                                    GENERATE_BLOCKS.put(loc, ids);
                                    GENERATE_DATA.put(loc, datas);
                                }
                            }
                        }
                    }
                    world.regenerateChunk(x, z);
                    
                    for (BlockLoc loc: chestContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof Chest) {
                            Chest chest = (Chest) state;
                            chest.getInventory().setContents(chestContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate chest: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: signContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof Sign) {
                            Sign sign = (Sign) state;
                            int i = 0;
                            for (String line : signContents.get(loc)) {
                                sign.setLine(i, line);
                                i++;
                            }
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate sign: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: dispenserContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof Dispenser) {
                            ((Dispenser) (state)).getInventory().setContents(dispenserContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate dispenser: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: beaconContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof Beacon) {
                            ((Beacon) (state)).getInventory().setContents(beaconContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate beacon: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: hopperContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof Hopper) {
                            ((Hopper) (state)).getInventory().setContents(hopperContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate hopper: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: noteBlockContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof NoteBlock) {
                            ((NoteBlock) (state)).setNote(noteBlockContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate note block: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: brewingStandContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof BrewingStand) {
                            ((BrewingStand) (state)).getInventory().setContents(brewingStandContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate brewing stand: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    
                    for (BlockLoc loc: furnaceContents.keySet()) {
                        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                        BlockState state = block.getState();
                        if (state instanceof Furnace) {
                            ((Furnace) (state)).getInventory().setContents(furnaceContents.get(loc));
                            state.update(true);
                        }
                        else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate furnace: "+loc.x+","+loc.y+","+loc.z); }
                    }
                    chunk.unload();
                    chunk.load();
                }
            }
        }
        CURRENT_PLOT_CLEAR = null;
        return true;
    }
}
