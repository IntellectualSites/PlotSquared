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
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.listeners.APlotListener;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.entity.EntityWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue.ChunkWrapper;
import com.intellectualcrafters.plot.util.TaskManager;

public class BukkitChunkManager extends ChunkManager {
    @Override
    public ArrayList<ChunkLoc> getChunkChunks(final String world) {
        final String directory = Bukkit.getWorldContainer() + File.separator + world + File.separator + "region";
        final File folder = new File(directory);
        final File[] regionFiles = folder.listFiles();
        final ArrayList<ChunkLoc> chunks = new ArrayList<>();
        if (regionFiles == null) {
            throw new RuntimeException("Could not find worlds folder.");
        }
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
        TaskManager.index.increment();
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();

        final RegionWrapper region = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World oldWorld = Bukkit.getWorld(pos1.getWorld());
        final World newWorld = Bukkit.getWorld(newPos.getWorld());

        final Chunk c1 = newWorld.getChunkAt(pos1.getX() >> 4, pos1.getZ() >> 4);
        final Chunk c2 = newWorld.getChunkAt(pos2.getX() >> 4, pos2.getZ() >> 4);

        final int sx = pos1.getX();
        final int sz = pos1.getZ();
        final int ex = pos2.getX();
        final int ez = pos2.getZ();

        final int c1x = c1.getX();
        final int c1z = c1.getZ();
        final int c2x = c2.getX();
        final int c2z = c2.getZ();

        final ArrayList<Chunk> chunks = new ArrayList<>();
        final ArrayList<Chunk> toGenerate = new ArrayList<>();
        // Load chunks
        for (int x = c1x; x <= c2x; x++) {
            for (int z = c1z; z <= c2z; z++) {
                final Chunk chunk = newWorld.getChunkAt(x, z);
                toGenerate.add(chunk);
            }
        }
        final Plugin plugin = BukkitMain.THIS;
        final Integer currentIndex = TaskManager.index.toInteger();
        final int loadTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < 25) {
                    if (toGenerate.size() == 0) {
                        Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                        TaskManager.tasks.remove(currentIndex);
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                TaskManager.index.increment();
                                // Copy entities
                                initMaps();
                                for (int x = c1x; x <= c2x; x++) {
                                    for (int z = c1z; z <= c2z; z++) {
                                        final Chunk chunk = oldWorld.getChunkAt(x, z);
                                        chunks.add(chunk);
                                        chunk.load(false);
                                    }
                                }
                                // Copy blocks
                                final MutableInt mx = new MutableInt(sx);
                                final Integer currentIndex = TaskManager.index.toInteger();
                                final int maxY = oldWorld.getMaxHeight();
                                final Integer task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        final long start = System.currentTimeMillis();
                                        while ((System.currentTimeMillis() - start) < 25) {
                                            final int xv = mx.intValue();
                                            for (int z = sz; z <= ez; z++) {
                                                saveBlocks(oldWorld, maxY, xv, z);
                                                for (int y = 1; y <= maxY; y++) {
                                                    final Block block = oldWorld.getBlockAt(xv, y, z);
                                                    final int id = block.getTypeId();
                                                    final byte data = block.getData();
                                                    BukkitSetBlockManager.setBlockManager.set(newWorld, xv + relX, y, z + relZ, id, data);
                                                }
                                            }
                                            mx.increment();
                                            if (xv == ex) { // done!
                                                for (int x = c1x; x <= c2x; x++) {
                                                    for (int z = c1z; z <= c2z; z++) {
                                                        final Chunk chunk = oldWorld.getChunkAt(x, z);
                                                        chunks.add(chunk);
                                                        chunk.load(false);
                                                        saveEntitiesIn(chunk, region);
                                                    }
                                                }
                                                restoreBlocks(newWorld, relX, relZ);
                                                restoreEntities(newWorld, relX, relZ);
                                                BukkitSetBlockManager.setBlockManager.update(chunks);
                                                TaskManager.runTask(whenDone);
                                                Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                                                TaskManager.tasks.remove(currentIndex);
                                                return;
                                            }
                                        }
                                    };
                                }, 1, 1);
                                TaskManager.tasks.put(currentIndex, task);
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
        TaskManager.tasks.put(currentIndex, loadTask);
        return true;
    }

    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone) {
        TaskManager.index.increment();
        final Plugin plugin = BukkitMain.THIS;
        final World world = Bukkit.getWorld(pos1.getWorld());
        final Chunk c1 = world.getChunkAt(pos1.getX() >> 4, pos1.getZ() >> 4);
        final Chunk c2 = world.getChunkAt(pos2.getX() >> 4, pos2.getZ() >> 4);
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
        final Integer currentIndex = TaskManager.index.toInteger();
        final Integer task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < 20) {
                    if (chunks.size() == 0) {
                        TaskManager.runTaskLater(whenDone, 1);
                        Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                        TaskManager.tasks.remove(currentIndex);
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
                        MainUtil.update(world.getName(), new ChunkLoc(chunk.getX(), chunk.getZ()));
                        BukkitSetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
                    }
                    CURRENT_PLOT_CLEAR = null;
                }
            }
        }, 1, 1);
        TaskManager.tasks.put(currentIndex, task);
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
        saveEntitiesIn(chunk, region, 0, 0, false);
    }

    public static void saveEntitiesIn(final Chunk chunk, final RegionWrapper region, final int offset_x, final int offset_z, final boolean delete) {
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
            wrap.x += offset_x;
            wrap.z += offset_z;
            entities.add(wrap);
            if (delete) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    public static void restoreEntities(final World world, final int x_offset, final int z_offset) {
        for (final EntityWrapper entity : entities) {
            try {
                entity.spawn(world, x_offset, z_offset);
            } catch (final Exception e) {
                PlotSquared.log("Failed to restore entity " + entity.x + "," + entity.y + "," + entity.z + " : " + entity.id + " : " + EntityType.fromId(entity.id));
                e.printStackTrace();
            }
        }
    }

    public static void restoreBlocks(final World world, final int x_offset, final int z_offset) {
        for (final BlockLoc loc : chestContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Chest) {
                    final Chest chest = (Chest) state;
                    chest.getInventory().setContents(chestContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate chest: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate chest: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : signContents.keySet()) {
            try {
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
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate sign: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : dispenserContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Dispenser) {
                    ((Dispenser) (state)).getInventory().setContents(dispenserContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate dispenser: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate dispenser: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : dropperContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Dropper) {
                    ((Dropper) (state)).getInventory().setContents(dropperContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate dispenser: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate dispenser: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : beaconContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Beacon) {
                    ((Beacon) (state)).getInventory().setContents(beaconContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate beacon: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate beacon: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : jukeDisc.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Jukebox) {
                    ((Jukebox) (state)).setPlaying(Material.getMaterial(jukeDisc.get(loc)));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to restore jukebox: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate jukebox: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : skullData.keySet()) {
            try {
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
                    PlotSquared.log("&c[WARN] Plot clear failed to restore skull: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate skull: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : hopperContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Hopper) {
                    ((Hopper) (state)).getInventory().setContents(hopperContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate hopper: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate hopper: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : noteBlockContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof NoteBlock) {
                    ((NoteBlock) (state)).setNote(noteBlockContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate note block: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate note block: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : brewTime.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof BrewingStand) {
                    ((BrewingStand) (state)).setBrewingTime(brewTime.get(loc));
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to restore brewing stand cooking: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to restore brewing stand cooking: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : spawnerData.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof CreatureSpawner) {
                    ((CreatureSpawner) (state)).setCreatureTypeId(spawnerData.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to restore spawner type: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to restore spawner type: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : cmdData.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof CommandBlock) {
                    ((CommandBlock) (state)).setCommand(cmdData.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to restore command block: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to restore command block: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : brewingStandContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof BrewingStand) {
                    ((BrewingStand) (state)).getInventory().setContents(brewingStandContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate brewing stand: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate brewing stand: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : furnaceTime.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Furnace) {
                    final Short[] time = furnaceTime.get(loc);
                    ((Furnace) (state)).setBurnTime(time[0]);
                    ((Furnace) (state)).setCookTime(time[1]);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to restore furnace cooking: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to restore furnace cooking: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : furnaceContents.keySet()) {
            try {
                final Block block = world.getBlockAt(loc.x + x_offset, loc.y, loc.z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Furnace) {
                    ((Furnace) (state)).getInventory().setContents(furnaceContents.get(loc));
                    state.update(true);
                } else {
                    PlotSquared.log("&c[WARN] Plot clear failed to regenerate furnace: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
                }
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate furnace: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
        for (final BlockLoc loc : bannerBase.keySet()) {
            try {
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
            } catch (final Exception e) {
                PlotSquared.log("&c[WARN] Plot clear failed to regenerate banner: " + loc.x + x_offset + "," + loc.y + "," + loc.z + z_offset);
            }
        }
    }

    public static void saveBlocks(final World world, final int maxY, final int x, final int z) {
        saveBlocks(world, maxY, x, z, 0, 0);
    }

    public static void saveBlocks(final World world, final int maxY, final int x, final int z, final int offset_x, final int offset_z) {
        final HashMap<Short, Short> ids = new HashMap<>();
        final HashMap<Short, Byte> datas = new HashMap<>();
        for (short y = 0; y < maxY; y++) {
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
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final InventoryHolder chest = (InventoryHolder) block.getState();
                        final ItemStack[] inventory = chest.getInventory().getContents().clone();
                        chestContents.put(bl, inventory);
                        break;
                    case 52:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
                        final String type = spawner.getCreatureTypeId();
                        if ((type != null) && (type.length() != 0)) {
                            spawnerData.put(bl, type);
                        }
                        break;
                    case 137:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final CommandBlock cmd = (CommandBlock) block.getState();
                        final String string = cmd.getCommand();
                        if ((string != null) && (string.length() > 0)) {
                            cmdData.put(bl, string);
                        }
                        break;
                    case 63:
                    case 68:
                    case 323:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Sign sign = (Sign) block.getState();
                        sign.getLines();
                        signContents.put(bl, sign.getLines().clone());
                        break;
                    case 61:
                    case 62:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
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
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Dispenser dispenser = (Dispenser) block.getState();
                        final ItemStack[] invDis = dispenser.getInventory().getContents().clone();
                        dispenserContents.put(bl, invDis);
                        break;
                    case 158:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Dropper dropper = (Dropper) block.getState();
                        final ItemStack[] invDro = dropper.getInventory().getContents().clone();
                        dropperContents.put(bl, invDro);
                        break;
                    case 117:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final BrewingStand brewingStand = (BrewingStand) block.getState();
                        final short time = (short) brewingStand.getBrewingTime();
                        if (time > 0) {
                            brewTime.put(bl, time);
                        }
                        final ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                        brewingStandContents.put(bl, invBre);
                        break;
                    case 25:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final NoteBlock noteBlock = (NoteBlock) block.getState();
                        final Note note = noteBlock.getNote();
                        noteBlockContents.put(bl, note);
                        break;
                    case 138:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Beacon beacon = (Beacon) block.getState();
                        final ItemStack[] invBea = beacon.getInventory().getContents().clone();
                        beaconContents.put(bl, invBea);
                        break;
                    case 84:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Jukebox jukebox = (Jukebox) block.getState();
                        final Material playing = jukebox.getPlaying();
                        if (playing != null) {
                            jukeDisc.put(bl, (short) playing.getId());
                        }
                        break;
                    case 154:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Hopper hopper = (Hopper) block.getState();
                        final ItemStack[] invHop = hopper.getInventory().getContents().clone();
                        hopperContents.put(bl, invHop);
                        break;
                    case 397:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
                        final Skull skull = (Skull) block.getState();
                        final String o = skull.getOwner();
                        final byte skulltype = getOrdinal(SkullType.values(), skull.getSkullType());
                        skull.getRotation();
                        final short rot = getOrdinal(BlockFace.values(), skull.getRotation());
                        skullData.put(bl, new Object[] { o, rot, skulltype });
                        break;
                    case 176:
                    case 177:
                        bl = new BlockLoc(x + offset_x, y, z + offset_z);
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
        final PlotLoc loc = new PlotLoc(x, z);
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
                    final PlotPlayer pp = BukkitUtil.getPlayer(player);
                    pp.teleport(MainUtil.getDefaultHome(plot));
                    APlotListener.manager.plotExit(pp, plot);
                } else {
                    entity.remove();
                }
            }
        }
    }

    @Override
    public boolean loadChunk(final String world, final ChunkLoc loc) {
        return BukkitUtil.getWorld(world).getChunkAt(loc.x, loc.z).load(false);
    }

    @Override
    public boolean unloadChunk(final String world, final ChunkLoc loc) {
        return BukkitUtil.getWorld(world).getChunkAt(loc.x, loc.z).unload(true, true);
    }

    public static void swapChunk(final World world, final Chunk pos1, final Chunk pos2, final RegionWrapper r1, final RegionWrapper r2) {
        initMaps();
        final int relX = (r2.minX - r1.minX);
        final int relZ = (r2.minZ - r1.minZ);

        saveEntitiesIn(pos1, r1, relX, relZ, true);
        saveEntitiesIn(pos2, r2, -relX, -relZ, true);

        final int sx = pos1.getX() << 4;
        final int sz = pos1.getZ() << 4;

        final int maxY = world.getMaxHeight();

        for (int x = Math.max(r1.minX, sx); x <= Math.min(r1.maxX, sx + 15); x++) {
            for (int z = Math.max(r1.minZ, sz); z <= Math.min(r1.maxZ, sz + 15); z++) {
                saveBlocks(world, maxY, sx, sz, relX, relZ);
                for (int y = 0; y < maxY; y++) {
                    final Block block1 = world.getBlockAt(x, y, z);
                    final int id1 = block1.getTypeId();
                    final byte data1 = block1.getData();
                    final int xx = x + relX;
                    final int zz = z + relZ;
                    final Block block2 = world.getBlockAt(xx, y, zz);
                    final int id2 = block2.getTypeId();
                    final byte data2 = block2.getData();
                    if (id1 == 0) {
                        if (id2 != 0) {
                            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, id2, data2);
                            BukkitSetBlockManager.setBlockManager.set(world, xx, y, zz, 0, (byte) 0);
                        }
                    } else if (id2 == 0) {
                        if (id1 != 0) {
                            BukkitSetBlockManager.setBlockManager.set(world, xx, y, zz, id1, data1);
                            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, 0, (byte) 0);
                        }
                    } else if (id1 == id2) {
                        if (data1 != data2) {
                            block1.setData(data2);
                            block2.setData(data1);
                        }
                    } else {
                        BukkitSetBlockManager.setBlockManager.set(world, x, y, z, id2, data2);
                        BukkitSetBlockManager.setBlockManager.set(world, xx, y, zz, id1, data1);
                    }

                }
            }
        }
        restoreBlocks(world, 0, 0);
        restoreEntities(world, 0, 0);
    }

    @Override
    public void swap(final String worldname, final PlotId pos1, final PlotId pos2) {
        final Location bot1 = MainUtil.getPlotBottomLoc(worldname, pos1).add(1, 0, 1);
        final Location top1 = MainUtil.getPlotTopLoc(worldname, pos1);

        final Location bot2 = MainUtil.getPlotBottomLoc(worldname, pos2).add(1, 0, 1);
        final Location top2 = MainUtil.getPlotTopLoc(worldname, pos2);
        swap(worldname, bot1, top1, bot2, top2);

        clearAllEntities(MainUtil.getPlot(worldname, pos1));
        clearAllEntities(MainUtil.getPlot(worldname, pos2));
    }

    @Override
    public void swap(final String worldname, final Location bot1, final Location top1, final Location bot2, final Location top2) {
        final RegionWrapper region1 = new RegionWrapper(bot1.getX(), top1.getX(), bot1.getZ(), top1.getZ());
        final RegionWrapper region2 = new RegionWrapper(bot2.getX(), top2.getX(), bot2.getZ(), top2.getZ());
        final World world = Bukkit.getWorld(bot1.getWorld());

        final int relX = bot2.getX() - bot1.getX();
        final int relZ = bot2.getZ() - bot1.getZ();

        for (int x = bot1.getX() >> 4; x <= (top1.getX() >> 4); x++) {
            for (int z = bot1.getZ() >> 4; z <= (top1.getZ() >> 4); z++) {
                final Chunk chunk1 = world.getChunkAt(x, z);
                final Chunk chunk2 = world.getChunkAt(x + (relX >> 4), z + (relZ >> 4));
                swapChunk(world, chunk1, chunk2, region1, region2);
            }
        }
        // FIXME swap plots
    }

    @Override
    public int[] countEntities(final Plot plot) {
        final int[] count = new int[5];
        final World world = BukkitUtil.getWorld(plot.world);

        final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        final int bx = bot.getX() >> 4;
                            final int bz = bot.getZ() >> 4;

                        final int tx = top.getX() >> 4;
                        final int tz = top.getZ() >> 4;

            final int size = (tx - bx) << 4;

            final HashSet<Chunk> chunks = new HashSet<>();
            for (int X = bx; X <= tx; X++) {
                for (int Z = bz; Z <= tz; Z++) {
                    chunks.add(world.getChunkAt(X, Z));
                }
            }

            boolean doWhole = false;
            List<Entity> entities = null;
            if (size > 200) {
                entities = world.getEntities();
                if (entities.size() < (16 + ((size * size) / 64))) {
                    doWhole = true;
                }
            }

            if (doWhole) {
                for (final Entity entity : entities) {
                    if (!((entity instanceof Creature) || (entity instanceof Vehicle))) {
                        continue;
                    }
                    final org.bukkit.Location loc = entity.getLocation();
                    final Chunk chunk = loc.getChunk();
                    if (chunks.contains(chunk)) {
                        final int X = chunk.getX();
                        final int Z = chunk.getX();
                        if ((X > bx) && (X < tx) && (Z > bz) && (Z < tz)) {
                            count(count, entity);
                        } else {
                            final PlotId id = MainUtil.getPlotId(BukkitUtil.getLocation(loc));
                            if (plot.id.equals(id)) {
                                count(count, entity);
                            }
                        }
                    }
                }
            } else {
                for (final Chunk chunk : chunks) {
                    final int X = chunk.getX();
                    final int Z = chunk.getX();
                    final Entity[] ents = chunk.getEntities();
                    for (final Entity entity : ents) {
                        if (!((entity instanceof Creature) || (entity instanceof Vehicle))) {
                            continue;
                        }
                        if ((X == bx) || (X == tx) || (Z == bz) || (Z == tz)) {
                            final PlotId id = MainUtil.getPlotId(BukkitUtil.getLocation(entity));
                            if (plot.id.equals(id)) {
                                count(count, entity);
                            }
                        } else {
                            count(count, entity);
                        }
                    }
                }
            }
            return count;
    }

    private void count(final int[] count, final Entity entity) {
        count[0]++;
        if (entity instanceof Creature) {
            count[3]++;
            if (entity instanceof Animals) {
                count[1]++;
            } else {
                count[2]++;
            }
        } else {
            count[4]++;
        }
    }

    @Override
    public void setChunk(final ChunkWrapper loc, final PlotBlock[][] blocks) {
        CURRENT_PLOT_CLEAR = new RegionWrapper(0, 0, 0, 0);
        final World world = Bukkit.getWorld(loc.world);
        final Chunk chunk = world.getChunkAt(loc.x, loc.z);
        final int cx = chunk.getX();
        final int cz = chunk.getZ();
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }
        initMaps();
        final int absX = cx << 4;
        final int absZ = cz << 4;
        final boolean save = false;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                saveBlocks(world, 255, absX + x, absZ + z);
                final PlotLoc pl = new PlotLoc(absX + x, absZ + z);
                final HashMap<Short, Short> ids = GENERATE_BLOCKS.get(pl);
                final HashMap<Short, Short> datas = GENERATE_BLOCKS.get(pl);
                for (int i = 0; i < blocks.length; i++) {
                    if (blocks[i] != null) {
                        final short y0 = (short) (i << 4);
                        for (short y = y0; y < (y0 + 16); y++) {
                            final int j = ((y & 0xF) << 8) | (z << 4) | x;
                            final PlotBlock block = blocks[i][j];
                            if (block != null) {
                                ids.put(y, block.id);
                                if (block.data != 0) {
                                    datas.put(y, block.id);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (save) {
            saveEntitiesOut(chunk, CURRENT_PLOT_CLEAR);
        }
        world.regenerateChunk(cx, cz);
        if (save) {
            restoreBlocks(world, 0, 0);
            restoreEntities(world, 0, 0);
        }
        MainUtil.update(world.getName(), new ChunkLoc(chunk.getX(), chunk.getZ()));
        BukkitSetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
        CURRENT_PLOT_CLEAR = null;
    }

}
