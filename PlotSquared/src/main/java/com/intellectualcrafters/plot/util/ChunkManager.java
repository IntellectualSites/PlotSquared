package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.entity.EntityWrapper;

public class ChunkManager {
    
    public static RegionWrapper CURRENT_PLOT_CLEAR = null;
    public static HashMap<ChunkLoc, HashMap<Short, Short>> GENERATE_BLOCKS = new HashMap<>();
    public static HashMap<ChunkLoc, HashMap<Short, Byte>> GENERATE_DATA = new HashMap<>();
    public static MutableInt index = new MutableInt(0);
    public static HashMap<Integer, Integer> tasks = new HashMap<>();
    
    public static ChunkLoc getChunkChunk(Location loc) {
        int x = loc.getBlockX() >> 9;
        int z = loc.getBlockZ() >> 9;
        return new ChunkLoc(x, z);
    }
    
    public static ArrayList<ChunkLoc> getChunkChunks(World world) {
        String directory = new File(".").getAbsolutePath() + File.separator + world.getName() + File.separator + "region";
        
        File folder = new File(directory);
        File[] regionFiles = folder.listFiles();
        
        ArrayList<ChunkLoc> chunks = new ArrayList<>();
        
        for (File file : regionFiles) {
            String name = file.getName();
            if (name.endsWith("mca")) {
                String[] split = name.split("\\.");
                try {
                    int x = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    ChunkLoc loc = new ChunkLoc(x, z);
                    chunks.add(loc);
                }
                catch (Exception e) {  }
            }
        }
        
        for (Chunk chunk : world.getLoadedChunks()) {
            ChunkLoc loc = new ChunkLoc(chunk.getX() >> 5, chunk.getZ() >> 5);
            if (!chunks.contains(loc)) {
                chunks.add(loc);
            }
        }
        
        return chunks;
    }
    
    public static void deleteRegionFile(final String world, final ChunkLoc loc) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                String directory = world + File.separator + "region" + File.separator + "r." + loc.x + "." + loc.z + ".mca";
                File file = new File(directory);
                PlotMain.sendConsoleSenderMessage("&6 - Deleting file: " + file.getName() + " (max 1024 chunks)");
                if (file.exists()) {
                    file.delete();
                }
            }
        });
    }
    
    public static Plot hasPlot(World world, Chunk chunk) {
        int x1 = chunk.getX() << 4;
        int z1 = chunk.getZ() << 4;
        int x2 = x1 + 15;
        int z2 = z1 + 15;
        
        Location bot = new Location(world, x1, 0, z1);
        Plot plot;
        plot = PlotHelper.getCurrentPlot(bot); 
        if (plot != null && plot.owner != null) {
            return plot;
        }
        Location top = new Location(world, x2, 0, z2);
        plot = PlotHelper.getCurrentPlot(top); 
        if (plot != null && plot.owner != null) {
            return plot;
        }
        return null;
    }
    
    private static HashMap<BlockLoc, ItemStack[]> chestContents;
    private static HashMap<BlockLoc, ItemStack[]> furnaceContents;
    private static HashMap<BlockLoc, ItemStack[]> dispenserContents;
    private static HashMap<BlockLoc, ItemStack[]> dropperContents;
    private static HashMap<BlockLoc, ItemStack[]> brewingStandContents;
    private static HashMap<BlockLoc, ItemStack[]> beaconContents;
    private static HashMap<BlockLoc, ItemStack[]> hopperContents;
    private static HashMap<BlockLoc, Short[]> furnaceTime;
    private static HashMap<BlockLoc, Object[]> skullData;
    private static HashMap<BlockLoc, Short> jukeDisc;
    private static HashMap<BlockLoc, Short> brewTime;
    private static HashMap<BlockLoc, String> spawnerData;
    private static HashMap<BlockLoc, String> cmdData;
    private static HashMap<BlockLoc, String[]> signContents;
    private static HashMap<BlockLoc, Note> noteBlockContents;
    private static HashMap<BlockLoc, ArrayList<Byte[]>> bannerColors;
    private static HashMap<BlockLoc, Byte> bannerBase;
    
    private static HashSet<EntityWrapper> entities;
    
    /**
     * Copy a region to a new location (in the same world)
     */
    public static boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone) {
        index.increment();
        final int relX = newPos.getBlockX() - pos1.getBlockX();
        final int relZ = newPos.getBlockZ() - pos1.getBlockZ();
        final RegionWrapper region = new RegionWrapper(pos1.getBlockX(), pos2.getBlockX(), pos1.getBlockZ(), pos2.getBlockZ());
        
        final World world = pos1.getWorld();
        Chunk c1 = world.getChunkAt(pos1);
        Chunk c2 = world.getChunkAt(pos2);
        
        Chunk c3 = world.getChunkAt((pos1.getBlockX() + relX) >> 4, (pos1.getBlockZ() + relZ) >> 4);
        Chunk c4 = world.getChunkAt((pos2.getBlockX() + relX) >> 4, (pos2.getBlockZ() + relZ) >> 4);
        
        final int sx = pos1.getBlockX();
        final int sz = pos1.getBlockZ();
        final int ex = pos2.getBlockX();
        final int ez = pos2.getBlockZ();

        final int c1x = c1.getX();
        final int c1z = c1.getZ();
        final int c2x = c2.getX();
        final int c2z = c2.getZ();
        
        final int c3x = c3.getX();
        final int c3z = c3.getZ();
        final int c4x = c4.getX();
        final int c4z = c4.getZ();
        
        final ArrayList<Chunk> chunks = new ArrayList<>();
        final ArrayList<Chunk> toGenerate = new ArrayList<>();
        // Load chunks
        for (int x = c1x; x <= c2x; x ++) {
            for (int z = c1z; z <= c2z; z ++) {
                Chunk chunk = world.getChunkAt(x, z);
                toGenerate.add(chunk);
            }
        }
        
        final Plugin plugin = (Plugin) PlotMain.getMain();
        final Integer currentIndex = index.toInteger();
        final int loadTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 25) {
                    if (toGenerate.size() == 0) {
                        Bukkit.getScheduler().cancelTask(tasks.get(currentIndex));
                        tasks.remove(currentIndex);
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                index.increment();
                                // Copy entities
                                initMaps();
                                for (int x = c3x; x <= c4x; x ++) {
                                    for (int z = c3z; z <= c4z; z ++) {
                                        Chunk chunk = world.getChunkAt(x, z);
                                        chunks.add(chunk);
                                        chunk.load(false);
                                        saveEntitiesIn(chunk, region);
                                        restoreEntities(world, relX, relZ);
                                    }
                                }
                                // Copy blocks
                                final MutableInt mx = new MutableInt(sx);
                                final Integer currentIndex = index.toInteger();
                                final int maxY = world.getMaxHeight();
                                final Integer task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        long start = System.currentTimeMillis();
                                        while (System.currentTimeMillis() - start < 25) {
                                            int x = mx.intValue();
                                            for (int z = sz; z <= ez; z++) {
                                                saveBlocks(world, maxY, x, z);
                                                for (int y = 1; y <= maxY; y++) {
                                                    Block block = world.getBlockAt(x, y, z);
                                                    int id = block.getTypeId();
                                                    byte data = block.getData();
                                                    SetBlockManager.setBlockManager.set(world, x + relX, y, z + relZ, id, data);

                                                }
                                            }
                                            mx.increment();
                                            if (x == ex) { // done!
                                                restoreBlocks(world, relX, relZ);
                                                SetBlockManager.setBlockManager.update(chunks);

                                                for (Chunk chunk : chunks) {
                                                    chunk.unload(true, true);
                                                }
                                                TaskManager.runTaskLater(whenDone, 1);
                                                Bukkit.getScheduler().cancelTask(tasks.get(currentIndex));
                                                tasks.remove(currentIndex);
                                                return;
                                            }
                                        }
                                    };
                                }, 1, 1);
                                
                                tasks.put(currentIndex, task);
                            }
                        });
                        return;
                    }
                    Chunk chunk = toGenerate.get(0);
                    toGenerate.remove(0);
                    chunk.load(true);
                    chunks.add(chunk);
                }
            }
        }, 1l, 1l);
        tasks.put(currentIndex, loadTask);
        return true;
    }
    
    public static boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone) {
        index.increment();
        final Plugin plugin = (Plugin) PlotMain.getMain();
        
        final World world = pos1.getWorld();
        Chunk c1 = world.getChunkAt(pos1);
        Chunk c2 = world.getChunkAt(pos2);
        
        final int sx = pos1.getBlockX();
        final int sz = pos1.getBlockZ();
        final int ex = pos2.getBlockX();
        final int ez = pos2.getBlockZ();

        final int c1x = c1.getX();
        final int c1z = c1.getZ();
        final int c2x = c2.getX();
        final int c2z = c2.getZ();
        
        final ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        for (int x = c1x; x <= c2x; x ++) {
            for (int z = c1z; z <= c2z; z ++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunk.load(false);
                chunks.add(chunk);
            }
        }
        final int maxY = world.getMaxHeight();
        final Integer currentIndex = index.toInteger();
        final Integer task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (chunks.size() == 0) {
                    TaskManager.runTaskLater(whenDone, 1);
                    Bukkit.getScheduler().cancelTask(tasks.get(currentIndex));
                    tasks.remove(currentIndex);
                    return;
                }
                CURRENT_PLOT_CLEAR = new RegionWrapper(pos1.getBlockX(), pos2.getBlockX(), pos1.getBlockZ(), pos2.getBlockZ());
                Chunk chunk = chunks.get(0);
                chunks.remove(0);
                int x = chunk.getX();
                int z = chunk.getZ();
                
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
                    initMaps();
                    int absX = x << 4;
                    int absZ = z << 4;
                    boolean save = false;
                    if (x == c1x || z == c1z) {
                        save = true;
                        for (int X = 0; X < 16; X++) {
                            for (int Z = 0; Z < 16; Z++) {
                                if ((X + absX < sx || Z + absZ < sz) || (X + absX > ex || Z + absZ > ez)) {
                                    saveBlocks(world, maxY, X + absX, Z + absZ);
                                }
                            }
                        }
                    }
                    else if (x == c2x || z == c2z) {
                        for (int X = 0; X < 16; X++) {
                            save = true;
                            for (int Z = 0; Z < 16; Z++) {
                                if ((X + absX > ex || Z + absZ > ez) || (X + absX < sx || Z + absZ < sz)) {
                                    saveBlocks(world, maxY, X + absX, Z + absZ);
                                }
                            }
                        }
                    }
                    if (save) {
                        saveEntitiesOut(chunk, CURRENT_PLOT_CLEAR);
                    }
                    world.regenerateChunk(x, z);
                    if (save) {
                        restoreBlocks(world, 0, 0);
                        restoreEntities(world, 0, 0);
                    }
                    chunk.unload(true, true);
                    SetBlockManager.setBlockManager.update(Arrays.asList( new Chunk[] {chunk}));

                }
                CURRENT_PLOT_CLEAR = null;
            }
        }, 1, 1);
        tasks.put(currentIndex, task);
        return true;
    }
    
    public static void initMaps() {
        GENERATE_BLOCKS = new HashMap<>();
        GENERATE_DATA = new HashMap<>();
        
        chestContents = new HashMap<>();
        furnaceContents = new HashMap<>();
        dispenserContents = new HashMap<>();
        dropperContents = new HashMap<>();
        brewingStandContents = new HashMap<>();
        beaconContents = new HashMap<>();
        hopperContents = new HashMap<>();
        furnaceTime = new HashMap<>();
        skullData = new HashMap<>();
        brewTime = new HashMap<>();
        jukeDisc = new HashMap<>();
        spawnerData= new HashMap<>();
        noteBlockContents = new HashMap<>();
        signContents = new HashMap<>();
        cmdData = new HashMap<>();
        bannerBase= new HashMap<>();
        bannerColors = new HashMap<>();
        
        entities = new HashSet<>();
    }
    
    public static boolean isIn(RegionWrapper region, int x, int z) {
        return (x >= region.minX && x <= region.maxX && z >= region.minZ && z <= region.maxZ);
    }
    
    public static void saveEntitiesOut(Chunk chunk, RegionWrapper region) {
        for (Entity entity : chunk.getEntities()) {
            Location loc = entity.getLocation();
            int x = loc.getBlockX();
            int z = loc.getBlockZ();
            if (isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            entities.add(wrap);
        }
    }
    
    public static void saveEntitiesIn(Chunk chunk, RegionWrapper region) {
        for (Entity entity : chunk.getEntities()) {
            Location loc = entity.getLocation();
            int x = loc.getBlockX();
            int z = loc.getBlockZ();
            if (!isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            entities.add(wrap);
        }
    }
    
    public static void restoreEntities(World world, int x_offset, int z_offset) {
        for (EntityWrapper entity : entities) {
            try {
                entity.spawn(world, x_offset, z_offset);
            }
            catch (Exception e) {
                System.out.print("Failed to restore entity " + entity.x + "," + entity.y + "," + entity.z + " : " + entity.id +" : " + EntityType.fromId(entity.id));
                e.printStackTrace();
            }
        }
    }
    
    public static void restoreBlocks(World world, int x_offset, int z_offset) {
        for (BlockLoc loc: chestContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Chest) {
                Chest chest = (Chest) state;
                chest.getInventory().setContents(chestContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate chest: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        
        for (BlockLoc loc: signContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
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
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate sign: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        
        for (BlockLoc loc: dispenserContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Dispenser) {
                ((Dispenser) (state)).getInventory().setContents(dispenserContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate dispenser: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: dropperContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Dropper) {
                ((Dropper) (state)).getInventory().setContents(dropperContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate dispenser: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: beaconContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Beacon) {
                ((Beacon) (state)).getInventory().setContents(beaconContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate beacon: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: jukeDisc.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Jukebox) {
                ((Jukebox) (state)).setPlaying(Material.getMaterial(jukeDisc.get(loc)));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to restore jukebox: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: skullData.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Skull) {
                Object[] data = skullData.get(loc);
                if (data[0] != null) {
                    ((Skull) (state)).setOwner((String) data[0]);
                }
                if (((Integer) data[1]) != 0) {
                    ((Skull) (state)).setRotation(BlockFace.values()[(int) data[1]]);
                }
                if (((Integer) data[2]) != 0) {
                    ((Skull) (state)).setSkullType(SkullType.values()[(int) data[2]]);
                }
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to restore jukebox: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: hopperContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Hopper) {
                ((Hopper) (state)).getInventory().setContents(hopperContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate hopper: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        
        for (BlockLoc loc: noteBlockContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof NoteBlock) {
                ((NoteBlock) (state)).setNote(noteBlockContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate note block: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: brewTime.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof BrewingStand) {
                ((BrewingStand) (state)).setBrewingTime(brewTime.get(loc));
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to restore brewing stand cooking: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        
        for (BlockLoc loc: spawnerData.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof CreatureSpawner) {
                ((CreatureSpawner) (state)).setCreatureTypeId(spawnerData.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to restore spawner type: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: cmdData.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof CommandBlock) {
                ((CommandBlock) (state)).setCommand(cmdData.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to restore command block: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: brewingStandContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof BrewingStand) {
                ((BrewingStand) (state)).getInventory().setContents(brewingStandContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate brewing stand: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: furnaceTime.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Furnace) {
                Short[] time = furnaceTime.get(loc);
                ((Furnace) (state)).setBurnTime(time[0]);
                ((Furnace) (state)).setCookTime(time[1]);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to restore furnace cooking: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        for (BlockLoc loc: furnaceContents.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Furnace) {
                ((Furnace) (state)).getInventory().setContents(furnaceContents.get(loc));
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate furnace: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
        
        for (BlockLoc loc: bannerBase.keySet()) {
            Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            BlockState state = block.getState();
            if (state instanceof Banner) {
                Banner banner = (Banner) state;
                byte base = bannerBase.get(loc);
                ArrayList<Byte[]> colors = bannerColors.get(loc);
                banner.setBaseColor(DyeColor.values()[base]);
                for (Byte[] color : colors) {
                    banner.addPattern(new Pattern(DyeColor.getByDyeData(color[1]), PatternType.values()[color[0]]));
                }
                state.update(true);
            }
            else { PlotMain.sendConsoleSenderMessage("&c[WARN] Plot clear failed to regenerate banner: "+loc.x + x_offset+","+loc.y+","+loc.z + z_offset); }
        }
    }
    
    public static void saveBlocks(World world, int maxY, int x, int z) {
        HashMap<Short, Short> ids = new HashMap<>();
        HashMap<Short, Byte> datas = new HashMap<>();
        for (short y = 1; y < maxY; y++) {
            Block block = world.getBlockAt(x, y, z);
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
                        bl = new BlockLoc(x, y, z);
                        InventoryHolder chest = (InventoryHolder) block.getState();
                        ItemStack[] inventory = chest.getInventory().getContents().clone();
                        chestContents.put(bl, inventory);
                        break;
                    case 52:
                        bl = new BlockLoc(x, y, z);
                        CreatureSpawner spawner = (CreatureSpawner) block.getState();
                        String type = spawner.getCreatureTypeId();
                        if (type != null && type.length() != 0) {
                            spawnerData.put(bl, type);
                        }
                        break;
                    case 137:
                        bl = new BlockLoc(x, y, z);
                        CommandBlock cmd = (CommandBlock) block.getState();
                        String string = cmd.getCommand();
                        if (string != null && string.length() > 0) {
                            cmdData.put(bl, string);
                        }
                        break;
                    case 63: case 68: case 323:
                        bl = new BlockLoc(x, y, z);
                        Sign sign = (Sign) block.getState();
                        sign.getLines();
                        signContents.put(bl, sign.getLines().clone());
                        break;
                    case 61: case 62:
                        bl = new BlockLoc(x, y, z);
                        Furnace furnace = (Furnace) block.getState();
                        short burn = furnace.getBurnTime();
                        short cook = furnace.getCookTime();
                        ItemStack[] invFur = furnace.getInventory().getContents().clone();
                        furnaceContents.put(bl, invFur);
                        if (cook != 0) {
                            furnaceTime.put(bl, new Short[] {burn, cook});
                        }
                        break;
                    case 23:
                        bl = new BlockLoc(x, y, z);
                        Dispenser dispenser = (Dispenser) block.getState();
                        ItemStack[] invDis = dispenser.getInventory().getContents().clone();
                        dispenserContents.put(bl, invDis);
                        break;
                    case 158:
                        bl = new BlockLoc(x, y, z);
                        Dropper dropper = (Dropper) block.getState();
                        ItemStack[] invDro = dropper.getInventory().getContents().clone();
                        dropperContents.put(bl, invDro);
                        break;
                    case 117:
                        bl = new BlockLoc(x, y, z);
                        BrewingStand brewingStand = (BrewingStand) block.getState();
                        short time = (short) brewingStand.getBrewingTime();
                        if (time > 0) {
                            brewTime.put(bl, time);
                        }
                        ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                        brewingStandContents.put(bl, invBre);
                        break;
                    case 25:
                        bl = new BlockLoc(x, y, z);
                        NoteBlock noteBlock = (NoteBlock) block.getState();
                        Note note = noteBlock.getNote();
                        noteBlockContents.put(bl, note);
                        break;
                    case 138:
                        bl = new BlockLoc(x, y, z);
                        Beacon beacon = (Beacon) block.getState();
                        ItemStack[] invBea = beacon.getInventory().getContents().clone();
                        beaconContents.put(bl, invBea);
                        break;
                    case 84:
                        bl = new BlockLoc(x, y, z);
                        Jukebox jukebox = (Jukebox) block.getState();
                        Material playing = jukebox.getPlaying();
                        if (playing != null) {
                            jukeDisc.put(bl, (short) playing.getId());
                        }
                        break;
                    case 154:
                        bl = new BlockLoc(x, y, z);
                        Hopper hopper = (Hopper) block.getState();
                        ItemStack[] invHop = hopper.getInventory().getContents().clone();
                        hopperContents.put(bl, invHop);
                        break;
                    case 397:
                        bl = new BlockLoc(x, y, z);
                        Skull skull = (Skull) block.getState();
                        String o = skull.getOwner();
                        byte skulltype = getOrdinal(SkullType.values(),skull.getSkullType());
                        BlockFace te = skull.getRotation();
                        short rot = (short) getOrdinal(BlockFace.values(), skull.getRotation());
                        skullData.put(bl, new Object[] {o, rot, skulltype});
                        break;
                    case 176:
                    case 177:
                        bl = new BlockLoc(x, y, z);
                        Banner banner = (Banner) block.getState();
                        byte base = getOrdinal(DyeColor.values(), banner.getBaseColor());
                        ArrayList<Byte[]> types = new ArrayList<>();
                        
                        for (Pattern pattern : banner.getPatterns()) {  
                            types.add(new Byte[] {getOrdinal(PatternType.values(), pattern.getPattern()), pattern.getColor().getDyeData() });
                        }
                        bannerBase.put(bl, base);
                        bannerColors.put(bl, types);
                        break;
                }
            }
        }
        ChunkLoc loc = new ChunkLoc(x, z);
        GENERATE_BLOCKS.put(loc, ids);
        GENERATE_DATA.put(loc, datas);
    }
    
    private static byte getOrdinal(Object[] list, Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }
}
