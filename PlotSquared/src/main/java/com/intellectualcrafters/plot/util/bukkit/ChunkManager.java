package com.intellectualcrafters.plot.util.bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.entity.EntityWrapper;
import com.intellectualcrafters.plot.util.AChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;

public class ChunkManager extends AChunkManager {
    public static MutableInt index = new MutableInt(0);
    public static HashMap<Integer, Integer> tasks = new HashMap<>();
    
    @Override
    public ArrayList<ChunkLoc> getChunkChunks(final String world) {
        final String directory = new File(".").getAbsolutePath() + File.separator + world + File.separator + "region";
        final File folder = new File(directory);
        final File[] regionFiles = folder.listFiles();
        final ArrayList<ChunkLoc> chunks = new ArrayList<>();
        for (final File file : regionFiles) {
            final String name = file.getName();
            if (name.endsWith("mca")) {
                final String[] split = name.split("\\.");
                try {
                    final int x = Integer.parseInt(split[1]);
                    final int z = Integer.parseInt(split[2]);
                    final ChunkLoc loc = new ChunkLoc(x, z);
                    chunks.add(loc);
                } catch (final Exception e) {
                }
            }
        }
        for (final Chunk chunk : Bukkit.getWorld(world).getLoadedChunks()) {
            final ChunkLoc loc = new ChunkLoc(chunk.getX() >> 5, chunk.getZ() >> 5);
            if (!chunks.contains(loc)) {
                chunks.add(loc);
            }
        }
        return chunks;
    }
    
    @Override
    public void deleteRegionFile(final String world, final ChunkLoc loc) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final String directory = world + File.separator + "region" + File.separator + "r." + loc.x + "." + loc.z + ".mca";
                final File file = new File(directory);
                PlotSquared.log("&6 - Deleting file: " + file.getName() + " (max 1024 chunks)");
                if (file.exists()) {
                    file.delete();
                }
            }
        });
    }
    
    @Override
    public Plot hasPlot(final String world, final ChunkLoc chunk) {
        final int x1 = chunk.x << 4;
        final int z1 = chunk.z << 4;
        final int x2 = x1 + 15;
        final int z2 = z1 + 15;
        final Location bot = new Location(world, x1, 0, z1);
        Plot plot;
        plot = MainUtil.getPlot(bot);
        if ((plot != null) && (plot.owner != null)) {
            return plot;
        }
        final Location top = new Location(world, x2, 0, z2);
        plot = MainUtil.getPlot(top);
        if ((plot != null) && (plot.owner != null)) {
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
    @Override
    public boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone) {
        index.increment();
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();
        final RegionWrapper region = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World world = Bukkit.getWorld(pos1.getWorld());
        final Chunk c1 = world.getChunkAt(pos1.getX(), pos1.getZ());
        final Chunk c2 = world.getChunkAt(pos2.getX(), pos2.getZ());
        final Chunk c3 = world.getChunkAt((pos1.getX() + relX), (pos1.getZ() + relZ));
        final Chunk c4 = world.getChunkAt((pos2.getX() + relX), (pos2.getZ() + relZ));
        final int sx = pos1.getX();
        final int sz = pos1.getZ();
        final int ex = pos2.getX();
        final int ez = pos2.getZ();
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
        for (int x = c1x; x <= c2x; x++) {
            for (int z = c1z; z <= c2z; z++) {
                final Chunk chunk = world.getChunkAt(x << 4, z << 4);
                toGenerate.add(chunk);
            }
        }
        final Plugin plugin = BukkitMain.THIS;
        final Integer currentIndex = index.toInteger();
        final int loadTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < 25) {
                    if (toGenerate.size() == 0) {
                        Bukkit.getScheduler().cancelTask(tasks.get(currentIndex));
                        tasks.remove(currentIndex);
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                index.increment();
                                // Copy entities
                                initMaps();
                                for (int x = c3x; x <= c4x; x++) {
                                    for (int z = c3z; z <= c4z; z++) {
                                        final Chunk chunk = world.getChunkAt(x, z);
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
                                        final long start = System.currentTimeMillis();
                                        while ((System.currentTimeMillis() - start) < 25) {
                                            final int x = mx.intValue();
                                            for (int z = sz; z <= ez; z++) {
                                                saveBlocks(world, maxY, x, z);
                                                for (int y = 1; y <= maxY; y++) {
                                                    final Block block = world.getBlockAt(x, y, z);
                                                    final int id = block.getTypeId();
                                                    final byte data = block.getData();
                                                    SetBlockManager.setBlockManager.set(world, x + relX, y, z + relZ, id, data);
                                                }
                                            }
                                            mx.increment();
                                            if (x == ex) { // done!
                                                restoreBlocks(world, relX, relZ);
                                                SetBlockManager.setBlockManager.update(chunks);
                                                for (final Chunk chunk : chunks) {
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
                    final Chunk chunk = toGenerate.get(0);
                    toGenerate.remove(0);
                    chunk.load(true);
                    chunks.add(chunk);
                }
            }
        }, 1l, 1l);
        tasks.put(currentIndex, loadTask);
        return true;
    }
    
    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone) {
        index.increment();
        final Plugin plugin = BukkitMain.THIS;
        final World world = Bukkit.getWorld(pos1.getWorld());
        final Chunk c1 = world.getChunkAt(pos1.getX(), pos1.getZ());
        final Chunk c2 = world.getChunkAt(pos2.getX(), pos2.getZ());
        final int sx = pos1.getX();
        final int sz = pos1.getZ();
        final int ex = pos2.getX();
        final int ez = pos2.getZ();
        final int c1x = c1.getX();
        final int c1z = c1.getZ();
        final int c2x = c2.getX();
        final int c2z = c2.getZ();
        final ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        for (int x = c1x; x <= c2x; x++) {
            for (int z = c1z; z <= c2z; z++) {
                final Chunk chunk = world.getChunkAt(x, z);
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
                CURRENT_PLOT_CLEAR = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
                final Chunk chunk = chunks.get(0);
                chunks.remove(0);
                final int x = chunk.getX();
                final int z = chunk.getZ();
                boolean loaded = true;
                if (!chunk.isLoaded()) {
                    final boolean result = chunk.load(false);
                    if (!result) {
                        loaded = false;
                        ;
                    }
                    if (!chunk.isLoaded()) {
                        loaded = false;
                    }
                }
                if (loaded) {
                    initMaps();
                    final int absX = x << 4;
                    final int absZ = z << 4;
                    boolean save = false;
                    if ((x == c1x) || (z == c1z)) {
                        save = true;
                        for (int X = 0; X < 16; X++) {
                            for (int Z = 0; Z < 16; Z++) {
                                if ((((X + absX) < sx) || ((Z + absZ) < sz)) || (((X + absX) > ex) || ((Z + absZ) > ez))) {
                                    saveBlocks(world, maxY, X + absX, Z + absZ);
                                }
                            }
                        }
                    } else if ((x == c2x) || (z == c2z)) {
                        for (int X = 0; X < 16; X++) {
                            save = true;
                            for (int Z = 0; Z < 16; Z++) {
                                if ((((X + absX) > ex) || ((Z + absZ) > ez)) || (((X + absX) < sx) || ((Z + absZ) < sz))) {
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
                    SetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
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
        spawnerData = new HashMap<>();
        noteBlockContents = new HashMap<>();
        signContents = new HashMap<>();
        cmdData = new HashMap<>();
        bannerBase = new HashMap<>();
        bannerColors = new HashMap<>();
        entities = new HashSet<>();
    }
    
    public static boolean isIn(final RegionWrapper region, final int x, final int z) {
        return ((x >= region.minX) && (x <= region.maxX) && (z >= region.minZ) && (z <= region.maxZ));
    }
    
    public static void saveEntitiesOut(final Chunk chunk, final RegionWrapper region) {
        for (final Entity entity : chunk.getEntities()) {
            final Location loc = BukkitUtil.getLocation(entity);
            final int x = loc.getX();
            final int z = loc.getZ();
            if (isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            final EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            entities.add(wrap);
        }
    }
    
    public static void saveEntitiesIn(final Chunk chunk, final RegionWrapper region) {
        for (final Entity entity : chunk.getEntities()) {
            final Location loc = BukkitUtil.getLocation(entity);
            final int x = loc.getX();
            final int z = loc.getZ();
            if (!isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            final EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            entities.add(wrap);
        }
    }
    
    public static void restoreEntities(final World world, final int x_offset, final int z_offset) {
        for (final EntityWrapper entity : entities) {
            try {
                entity.spawn(world, x_offset, z_offset);
            } catch (final Exception e) {
                System.out.print("Failed to restore entity " + entity.x + "," + entity.y + "," + entity.z + " : " + entity.id + " : " + EntityType.fromId(entity.id));
                e.printStackTrace();
            }
        }
    }
    
    public static void restoreBlocks(final World world, final int x_offset, final int z_offset) {
        for (final BlockLoc loc : chestContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Chest) {
                final Chest chest = (Chest) state;
                chest.getInventory().setContents(chestContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate chest: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : signContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Sign) {
                final Sign sign = (Sign) state;
                int i = 0;
                for (final String line : signContents.get(loc)) {
                    sign.setLine(i, line);
                    i++;
                }
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate sign: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : dispenserContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Dispenser) {
                ((Dispenser) (state)).getInventory().setContents(dispenserContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate dispenser: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : dropperContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Dropper) {
                ((Dropper) (state)).getInventory().setContents(dropperContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate dispenser: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : beaconContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Beacon) {
                ((Beacon) (state)).getInventory().setContents(beaconContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate beacon: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : jukeDisc.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Jukebox) {
                ((Jukebox) (state)).setPlaying(Material.getMaterial(jukeDisc.get(loc)));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to restore jukebox: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : skullData.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Skull) {
                final Object[] data = skullData.get(loc);
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
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to restore jukebox: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : hopperContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Hopper) {
                ((Hopper) (state)).getInventory().setContents(hopperContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate hopper: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : noteBlockContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof NoteBlock) {
                ((NoteBlock) (state)).setNote(noteBlockContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate note block: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : brewTime.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof BrewingStand) {
                ((BrewingStand) (state)).setBrewingTime(brewTime.get(loc));
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to restore brewing stand cooking: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : spawnerData.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof CreatureSpawner) {
                ((CreatureSpawner) (state)).setCreatureTypeId(spawnerData.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to restore spawner type: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : cmdData.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof CommandBlock) {
                ((CommandBlock) (state)).setCommand(cmdData.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to restore command block: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : brewingStandContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof BrewingStand) {
                ((BrewingStand) (state)).getInventory().setContents(brewingStandContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate brewing stand: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : furnaceTime.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Furnace) {
                final Short[] time = furnaceTime.get(loc);
                ((Furnace) (state)).setBurnTime(time[0]);
                ((Furnace) (state)).setCookTime(time[1]);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to restore furnace cooking: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : furnaceContents.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Furnace) {
                ((Furnace) (state)).getInventory().setContents(furnaceContents.get(loc));
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate furnace: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : bannerBase.keySet()) {
            final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
            final BlockState state = block.getState();
            if (state instanceof Banner) {
                final Banner banner = (Banner) state;
                final byte base = bannerBase.get(loc);
                final ArrayList<Byte[]> colors = bannerColors.get(loc);
                banner.setBaseColor(DyeColor.values()[base]);
                for (final Byte[] color : colors) {
                    banner.addPattern(new Pattern(DyeColor.getByDyeData(color[1]), PatternType.values()[color[0]]));
                }
                state.update(true);
            } else {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate banner: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
    }
    
    public static void saveBlocks(final World world, final int maxY, final int x, final int z) {
        final HashMap<Short, Short> ids = new HashMap<>();
        final HashMap<Short, Byte> datas = new HashMap<>();
        for (short y = 1; y < maxY; y++) {
            final Block block = world.getBlockAt(x, y, z);
            final short id = (short) block.getTypeId();
            if (id != 0) {
                ids.put(y, id);
                final byte data = block.getData();
                if (data != 0) {
                    datas.put(y, data);
                }
                BlockLoc bl;
                switch (id) {
                    case 54:
                        bl = new BlockLoc(x, y, z);
                        final InventoryHolder chest = (InventoryHolder) block.getState();
                        final ItemStack[] inventory = chest.getInventory().getContents().clone();
                        chestContents.put(bl, inventory);
                        break;
                    case 52:
                        bl = new BlockLoc(x, y, z);
                        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
                        final String type = spawner.getCreatureTypeId();
                        if ((type != null) && (type.length() != 0)) {
                            spawnerData.put(bl, type);
                        }
                        break;
                    case 137:
                        bl = new BlockLoc(x, y, z);
                        final CommandBlock cmd = (CommandBlock) block.getState();
                        final String string = cmd.getCommand();
                        if ((string != null) && (string.length() > 0)) {
                            cmdData.put(bl, string);
                        }
                        break;
                    case 63:
                    case 68:
                    case 323:
                        bl = new BlockLoc(x, y, z);
                        final Sign sign = (Sign) block.getState();
                        sign.getLines();
                        signContents.put(bl, sign.getLines().clone());
                        break;
                    case 61:
                    case 62:
                        bl = new BlockLoc(x, y, z);
                        final Furnace furnace = (Furnace) block.getState();
                        final short burn = furnace.getBurnTime();
                        final short cook = furnace.getCookTime();
                        final ItemStack[] invFur = furnace.getInventory().getContents().clone();
                        furnaceContents.put(bl, invFur);
                        if (cook != 0) {
                            furnaceTime.put(bl, new Short[] { burn, cook });
                        }
                        break;
                    case 23:
                        bl = new BlockLoc(x, y, z);
                        final Dispenser dispenser = (Dispenser) block.getState();
                        final ItemStack[] invDis = dispenser.getInventory().getContents().clone();
                        dispenserContents.put(bl, invDis);
                        break;
                    case 158:
                        bl = new BlockLoc(x, y, z);
                        final Dropper dropper = (Dropper) block.getState();
                        final ItemStack[] invDro = dropper.getInventory().getContents().clone();
                        dropperContents.put(bl, invDro);
                        break;
                    case 117:
                        bl = new BlockLoc(x, y, z);
                        final BrewingStand brewingStand = (BrewingStand) block.getState();
                        final short time = (short) brewingStand.getBrewingTime();
                        if (time > 0) {
                            brewTime.put(bl, time);
                        }
                        final ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                        brewingStandContents.put(bl, invBre);
                        break;
                    case 25:
                        bl = new BlockLoc(x, y, z);
                        final NoteBlock noteBlock = (NoteBlock) block.getState();
                        final Note note = noteBlock.getNote();
                        noteBlockContents.put(bl, note);
                        break;
                    case 138:
                        bl = new BlockLoc(x, y, z);
                        final Beacon beacon = (Beacon) block.getState();
                        final ItemStack[] invBea = beacon.getInventory().getContents().clone();
                        beaconContents.put(bl, invBea);
                        break;
                    case 84:
                        bl = new BlockLoc(x, y, z);
                        final Jukebox jukebox = (Jukebox) block.getState();
                        final Material playing = jukebox.getPlaying();
                        if (playing != null) {
                            jukeDisc.put(bl, (short) playing.getId());
                        }
                        break;
                    case 154:
                        bl = new BlockLoc(x, y, z);
                        final Hopper hopper = (Hopper) block.getState();
                        final ItemStack[] invHop = hopper.getInventory().getContents().clone();
                        hopperContents.put(bl, invHop);
                        break;
                    case 397:
                        bl = new BlockLoc(x, y, z);
                        final Skull skull = (Skull) block.getState();
                        final String o = skull.getOwner();
                        final byte skulltype = getOrdinal(SkullType.values(), skull.getSkullType());
                        skull.getRotation();
                        final short rot = getOrdinal(BlockFace.values(), skull.getRotation());
                        skullData.put(bl, new Object[] { o, rot, skulltype });
                        break;
                    case 176:
                    case 177:
                        bl = new BlockLoc(x, y, z);
                        final Banner banner = (Banner) block.getState();
                        final byte base = getOrdinal(DyeColor.values(), banner.getBaseColor());
                        final ArrayList<Byte[]> types = new ArrayList<>();
                        for (final Pattern pattern : banner.getPatterns()) {
                            types.add(new Byte[] { getOrdinal(PatternType.values(), pattern.getPattern()), pattern.getColor().getDyeData() });
                        }
                        bannerBase.put(bl, base);
                        bannerColors.put(bl, types);
                        break;
                }
            }
        }
        final ChunkLoc loc = new ChunkLoc(x, z);
        GENERATE_BLOCKS.put(loc, ids);
        GENERATE_DATA.put(loc, datas);
    }
    
    private static byte getOrdinal(final Object[] list, final Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }
    
    @Override
    public void clearAllEntities(final Plot plot) {
        final List<Entity> entities = BukkitUtil.getEntities(plot.world);
        for (final Entity entity : entities) {
            final PlotId id = MainUtil.getPlotId(BukkitUtil.getLocation(entity));
            if (plot.id.equals(id)) {
                if (entity instanceof Player) {
                    final Player player = (Player) entity;
                    MainUtil.teleportPlayer(BukkitUtil.getPlayer(player), BukkitUtil.getLocation(entity), plot);
                    PlotListener.plotExit(player, plot);
                } else {
                    entity.remove();
                }
            }
        }
    }

    @Override
    public boolean loadChunk(String world, ChunkLoc loc) {
        return BukkitUtil.getWorld(world).getChunkAt(loc.x << 4, loc.z << 4).load(false);
    }
}
