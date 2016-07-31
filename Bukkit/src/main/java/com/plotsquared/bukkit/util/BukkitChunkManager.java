package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.block.GlobalBlockQueue;
import com.intellectualcrafters.plot.util.block.LocalBlockQueue;
import com.intellectualcrafters.plot.util.block.ScopedLocalBlockQueue;
import com.plotsquared.bukkit.object.entity.EntityWrapper;
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
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BukkitChunkManager extends ChunkManager {

    public static boolean isIn(RegionWrapper region, int x, int z) {
        return x >= region.minX && x <= region.maxX && z >= region.minZ && z <= region.maxZ;
    }

    private static byte getOrdinal(Object[] list, Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public static ContentMap swapChunk(World world1, World world2, Chunk pos1, Chunk pos2, RegionWrapper r1, RegionWrapper r2) {
        ContentMap map = new ContentMap();
        int relX = r2.minX - r1.minX;
        int relZ = r2.minZ - r1.minZ;

        map.saveEntitiesIn(pos1, r1, relX, relZ, true);
        map.saveEntitiesIn(pos2, r2, -relX, -relZ, true);

        int sx = pos1.getX() << 4;
        int sz = pos1.getZ() << 4;

        String worldName1 = world1.getName();
        String worldName2 = world2.getName();

        LocalBlockQueue queue1 = GlobalBlockQueue.IMP.getNewQueue(worldName1, false);
        LocalBlockQueue queue2 = GlobalBlockQueue.IMP.getNewQueue(worldName2, false);

        for (int x = Math.max(r1.minX, sx); x <= Math.min(r1.maxX, sx + 15); x++) {
            for (int z = Math.max(r1.minZ, sz); z <= Math.min(r1.maxZ, sz + 15); z++) {
                map.saveBlocks(world1, 256, sx, sz, relX, relZ, false);
                for (int y = 0; y < 256; y++) {
                    Block block1 = world1.getBlockAt(x, y, z);
                    int id1 = block1.getTypeId();
                    byte data1 = block1.getData();
                    int xx = x + relX;
                    int zz = z + relZ;
                    Block block2 = world2.getBlockAt(xx, y, zz);
                    int id2 = block2.getTypeId();
                    byte data2 = block2.getData();
                    if (id1 == 0) {
                        if (id2 != 0) {
                            queue1.setBlock(x, y, z, (short) id2, data2);
                            queue2.setBlock(xx, y, zz, (short) 0, (byte) 0);
                        }
                    } else if (id2 == 0) {
                        queue1.setBlock(x, y, z, (short) 0, (byte) 0);
                        queue2.setBlock(xx, y, zz, (short) id1, data1);
                    } else if (id1 == id2) {
                        if (data1 != data2) {
                            block1.setData(data2);
                            block2.setData(data1);
                        }
                    } else {
                        queue1.setBlock(x, y, z, (short) id2, data2);
                        queue2.setBlock(xx, y, zz, (short) id1, data1);
                    }
                }
            }
        }
        queue1.enqueue();
        queue2.enqueue();
        return map;
    }

    @Override
    public Set<ChunkLoc> getChunkChunks(String world) {
        Set<ChunkLoc> chunks = super.getChunkChunks(world);
        for (Chunk chunk : Bukkit.getWorld(world).getLoadedChunks()) {
            ChunkLoc loc = new ChunkLoc(chunk.getX() >> 5, chunk.getZ() >> 5);
            if (!chunks.contains(loc)) {
                chunks.add(loc);
            }
        }
        return chunks;
    }

    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos, final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();
        Location pos4 = new Location(newPos.getWorld(), newPos.getX() + relX, 256, newPos.getZ() + relZ);

        final RegionWrapper region = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World oldWorld = Bukkit.getWorld(pos1.getWorld());
        final World newWorld = Bukkit.getWorld(newPos.getWorld());
        final String newWorldName = newWorld.getName();
        List<ChunkLoc> chunks = new ArrayList<>();
        final ContentMap map = new ContentMap();
        final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(newWorldName, false);
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override
            public void run(int[] value) {
                int bx = value[2];
                int bz = value[3];
                int tx = value[4];
                int tz = value[5];
                ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                int cxx = loc.x << 4;
                int czz = loc.z << 4;
                Chunk chunk = oldWorld.getChunkAt(loc.x, loc.z);
                map.saveEntitiesIn(chunk, region);
                for (int x = bx & 15; x <= (tx & 15); x++) {
                    for (int z = bz & 15; z <= (tz & 15); z++) {
                        map.saveBlocks(oldWorld, 256, cxx + x, czz + z, relX, relZ, true);
                    }
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                for (Entry<PlotLoc, PlotBlock[]> entry : map.allBlocks.entrySet()) {
                    PlotLoc loc = entry.getKey();
                    PlotBlock[] blocks = entry.getValue();
                    for (int y = 0; y < blocks.length; y++) {
                        PlotBlock block = blocks[y];
                        if (block != null) {
                            queue.setBlock(loc.x, y, loc.z, block);
                        }
                    }
                }
                queue.enqueue();
                GlobalBlockQueue.IMP.addTask(new Runnable() {
                    @Override
                    public void run() {
                        map.restoreBlocks(newWorld, 0, 0);
                        map.restoreEntities(newWorld, relX, relZ);
                        TaskManager.runTask(whenDone);
                    }
                });
            }
        }, 5);
        return true;
    }

    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final boolean ignoreAugment, final Runnable whenDone) {
        final String world = pos1.getWorld();

        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;

        final List<ChunkLoc> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        final World worldObj = Bukkit.getWorld(world);
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 5) {
                    final ChunkLoc chunk = chunks.remove(0);
                    int x = chunk.x;
                    int z = chunk.z;
                    int xxb = x << 4;
                    int zzb = z << 4;
                    int xxt = xxb + 15;
                    int zzt = zzb + 15;
                    Chunk chunkObj = worldObj.getChunkAt(x, z);
                    if (!chunkObj.load(false)) {
                        continue;
                    }
                    final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
                    RegionWrapper currentPlotClear = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
                    if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z) {
                        AugmentedUtils.bypass(ignoreAugment, new Runnable() {
                            @Override
                            public void run() {
                                queue.regenChunkSafe(chunk.x, chunk.z);
                            }
                        });
                        continue;
                    }
                    boolean checkX1 = false;

                    int xxb2;

                    if (x == bcx) {
                        xxb2 = p1x - 1;
                        checkX1 = true;
                    } else {
                        xxb2 = xxb;
                    }
                    boolean checkX2 = false;
                    int xxt2;
                    if (x == tcx) {
                        xxt2 = p2x + 1;
                        checkX2 = true;
                    } else {
                        xxt2 = xxt;
                    }
                    boolean checkZ1 = false;
                    int zzb2;
                    if (z == bcz) {
                        zzb2 = p1z - 1;
                        checkZ1 = true;
                    } else {
                        zzb2 = zzb;
                    }
                    boolean checkZ2 = false;
                    int zzt2;
                    if (z == tcz) {
                        zzt2 = p2z + 1;
                        checkZ2 = true;
                    } else {
                        zzt2 = zzt;
                    }
                    final ContentMap map = new ContentMap();
                    if (checkX1) {
                        map.saveRegion(worldObj, xxb, xxb2, zzb2, zzt2); //
                    }
                    if (checkX2) {
                        map.saveRegion(worldObj, xxt2, xxt, zzb2, zzt2); //
                    }
                    if (checkZ1) {
                        map.saveRegion(worldObj, xxb2, xxt2, zzb, zzb2); //
                    }
                    if (checkZ2) {
                        map.saveRegion(worldObj, xxb2, xxt2, zzt2, zzt); //
                    }
                    if (checkX1 && checkZ1) {
                        map.saveRegion(worldObj, xxb, xxb2, zzb, zzb2); //
                    }
                    if (checkX2 && checkZ1) {
                        map.saveRegion(worldObj, xxt2, xxt, zzb, zzb2); // ?
                    }
                    if (checkX1 && checkZ2) {
                        map.saveRegion(worldObj, xxb, xxb2, zzt2, zzt); // ?
                    }
                    if (checkX2 && checkZ2) {
                        map.saveRegion(worldObj, xxt2, xxt, zzt2, zzt); //
                    }
                    map.saveEntitiesOut(chunkObj, currentPlotClear);
                    AugmentedUtils.bypass(ignoreAugment, new Runnable() {
                        @Override
                        public void run() {
                            setChunkInPlotArea(null, new RunnableVal<ScopedLocalBlockQueue>() {
                                @Override
                                public void run(ScopedLocalBlockQueue value) {
                                    Location min = value.getMin();
                                    int bx = min.getX();
                                    int bz = min.getZ();
                                    for (int x = 0; x < 16; x++) {
                                        for (int z = 0; z < 16; z++) {
                                            PlotLoc loc = new PlotLoc(bx + x, bz + z);
                                            PlotBlock[] ids = map.allBlocks.get(loc);
                                            if (ids != null) {
                                                for (int y = 0; y < Math.min(128, ids.length); y++) {
                                                    PlotBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x, y, z, id);
                                                    } else {
                                                        value.setBlock(x, y, z, 0, (byte) 0);
                                                    }
                                                }
                                                for (int y = Math.min(128, ids.length); y < ids.length; y++) {
                                                    PlotBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x, y, z, id);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, world, chunk);
                        }
                    });
                    map.restoreBlocks(worldObj, 0, 0);
                    map.restoreEntities(worldObj, 0, 0);
                }
                if (!chunks.isEmpty()) {
                    TaskManager.runTaskLater(this, 1);
                } else {
                    TaskManager.runTaskLater(whenDone, 1);
                }
            }
        });
        return true;
    }

    @Override
    public void clearAllEntities(Location pos1, Location pos2) {
        String world = pos1.getWorld();
        List<Entity> entities = BukkitUtil.getEntities(world);
        int bx = pos1.getX();
        int bz = pos1.getZ();
        int tx = pos2.getX();
        int tz = pos2.getZ();
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) {
                org.bukkit.Location location = entity.getLocation();
                if (location.getX() >= bx && location.getX() <= tx && location.getZ() >= bz && location.getZ() <= tz) {
                    entity.remove();
                }
            }
        }
    }

    @Override
    public boolean loadChunk(String world, ChunkLoc loc, boolean force) {
        return BukkitUtil.getWorld(world).getChunkAt(loc.x, loc.z).load(force);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void unloadChunk(final String world, final ChunkLoc loc, final boolean save, final boolean safe) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    BukkitUtil.getWorld(world).unloadChunk(loc.x, loc.z, save, safe);
                }
            });
        } else {
            BukkitUtil.getWorld(world).unloadChunk(loc.x, loc.z, save, safe);
        }
    }

    @Override
    public void swap(Location bot1, Location top1, Location bot2, Location top2, final Runnable whenDone) {
        RegionWrapper region1 = new RegionWrapper(bot1.getX(), top1.getX(), bot1.getZ(), top1.getZ());
        RegionWrapper region2 = new RegionWrapper(bot2.getX(), top2.getX(), bot2.getZ(), top2.getZ());
        final World world1 = Bukkit.getWorld(bot1.getWorld());
        World world2 = Bukkit.getWorld(bot2.getWorld());

        int relX = bot2.getX() - bot1.getX();
        int relZ = bot2.getZ() - bot1.getZ();

        final ArrayDeque<ContentMap> maps = new ArrayDeque<>();

        for (int x = bot1.getX() >> 4; x <= top1.getX() >> 4; x++) {
            for (int z = bot1.getZ() >> 4; z <= top1.getZ() >> 4; z++) {
                Chunk chunk1 = world1.getChunkAt(x, z);
                Chunk chunk2 = world2.getChunkAt(x + (relX >> 4), z + (relZ >> 4));
                maps.add(swapChunk(world1, world2, chunk1, chunk2, region1, region2));
            }
        }
        GlobalBlockQueue.IMP.addTask(new Runnable() {
            @Override
            public void run() {
                for (ContentMap map : maps) {
                    map.restoreBlocks(world1, 0, 0);
                    map.restoreEntities(world1, 0, 0);
                    TaskManager.runTaskLater(whenDone, 1);
                }
            }
        });
    }

    @Override
    public int[] countEntities(Plot plot) {
        PlotArea area = plot.getArea();
        World world = BukkitUtil.getWorld(area.worldname);

        Location bot = plot.getBottomAbs();
        Location top = plot.getTopAbs();
        int bx = bot.getX() >> 4;
        int bz = bot.getZ() >> 4;

        int tx = top.getX() >> 4;
        int tz = top.getZ() >> 4;

        int size = tx - bx << 4;

        Set<Chunk> chunks = new HashSet<>();
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                if (world.isChunkLoaded(X, Z)) {
                    chunks.add(world.getChunkAt(X, Z));
                }
            }
        }

        boolean doWhole = false;
        List<Entity> entities = null;
        if (size > 200) {
            entities = world.getEntities();
            if (entities.size() < 16 + size * size / 64) {
                doWhole = true;
            }
        }

        int[] count = new int[6];
        if (doWhole) {
            for (Entity entity : entities) {
                org.bukkit.Location location = entity.getLocation();
                Chunk chunk = location.getChunk();
                if (chunks.contains(chunk)) {
                    int X = chunk.getX();
                    int Z = chunk.getZ();
                    if (X > bx && X < tx && Z > bz && Z < tz) {
                        count(count, entity);
                    } else {
                        Plot other = area.getPlot(BukkitUtil.getLocation(location));
                        if (plot.equals(other)) {
                            count(count, entity);
                        }
                    }
                }
            }
        } else {
            for (Chunk chunk : chunks) {
                int X = chunk.getX();
                int Z = chunk.getZ();
                Entity[] entities1 = chunk.getEntities();
                for (Entity entity : entities1) {
                    if (X == bx || X == tx || Z == bz || Z == tz) {
                        Plot other = area.getPlot(BukkitUtil.getLocation(entity));
                        if (plot.equals(other)) {
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

    private void count(int[] count, Entity entity) {
        switch (entity.getType()) {
            case PLAYER:
                // not valid
                return;
            case SMALL_FIREBALL:
            case FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case THROWN_EXP_BOTTLE:
            case SPLASH_POTION:
            case SNOWBALL:
            case ENDER_PEARL:
            case ARROW:
            case TIPPED_ARROW:
            case SHULKER_BULLET:
            case SPECTRAL_ARROW:
            case DRAGON_FIREBALL:
                // projectile
            case PRIMED_TNT:
            case FALLING_BLOCK:
                // Block entities
            case ENDER_CRYSTAL:
            case COMPLEX_PART:
            case FISHING_HOOK:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case LEASH_HITCH:
            case FIREWORK:
            case WEATHER:
            case LIGHTNING:
            case WITHER_SKULL:
            case UNKNOWN:
            case AREA_EFFECT_CLOUD:
            case LINGERING_POTION:
                // non moving / unremovable
                break;
            case ITEM_FRAME:
            case PAINTING:
            case ARMOR_STAND:
                count[5]++;
                break;
                // misc
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case BOAT:
                count[4]++;
                break;
            case POLAR_BEAR:
            case RABBIT:
            case SHEEP:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case HORSE:
            case SQUID:
            case VILLAGER:
            case IRON_GOLEM:
            case WOLF:
            case CHICKEN:
            case COW:
            case SNOWMAN:
            case BAT:
                // animal
                count[3]++;
                count[1]++;
                break;
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case ENDERMITE:
            case ENDER_DRAGON:
            case GHAST:
            case GIANT:
            case GUARDIAN:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WITCH:
            case WITHER:
            case ZOMBIE:
            case SHULKER:
                // monster
                count[3]++;
                count[2]++;
                break;
            default:
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
        count[0]++;
    }

    public static class ContentMap {

        public final Map<BlockLoc, ItemStack[]> chestContents;
        public final Map<BlockLoc, ItemStack[]> furnaceContents;
        public final Map<BlockLoc, ItemStack[]> dispenserContents;
        public final Map<BlockLoc, ItemStack[]> dropperContents;
        public final Map<BlockLoc, ItemStack[]> brewingStandContents;
        public final Map<BlockLoc, ItemStack[]> beaconContents;
        public final Map<BlockLoc, ItemStack[]> hopperContents;
        public final Map<BlockLoc, Short[]> furnaceTime;
        public final Map<BlockLoc, Object[]> skullData;
        public final Map<BlockLoc, Material> jukeboxDisc;
        public final Map<BlockLoc, Short> brewTime;
        public final Map<BlockLoc, EntityType> spawnerData;
        public final Map<BlockLoc, String> cmdData;
        public final Map<BlockLoc, String[]> signContents;
        public final Map<BlockLoc, Note> noteBlockContents;
        public final Map<BlockLoc, List<Pattern>> bannerPatterns;
        public final Map<BlockLoc, DyeColor> bannerBase;
        public final Set<EntityWrapper> entities;
        public final Map<PlotLoc, PlotBlock[]> allBlocks;

        public ContentMap() {
            this.chestContents = new HashMap<>();
            this.furnaceContents = new HashMap<>();
            this.dispenserContents = new HashMap<>();
            this.dropperContents = new HashMap<>();
            this.brewingStandContents = new HashMap<>();
            this.beaconContents = new HashMap<>();
            this.hopperContents = new HashMap<>();
            this.furnaceTime = new HashMap<>();
            this.skullData = new HashMap<>();
            this.brewTime = new HashMap<>();
            this.jukeboxDisc = new HashMap<>();
            this.spawnerData = new HashMap<>();
            this.noteBlockContents = new HashMap<>();
            this.signContents = new HashMap<>();
            this.cmdData = new HashMap<>();
            this.bannerBase = new HashMap<>();
            this.bannerPatterns = new HashMap<>();
            this.entities = new HashSet<>();
            this.allBlocks = new HashMap<>();
        }

        public void saveRegion(World world, int x1, int x2, int z1, int z2) {
            if (z1 > z2) {
                int tmp = z1;
                z1 = z2;
                z2 = tmp;
            }
            if (x1 > x2) {
                int tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    saveBlocks(world, 256, x, z, 0, 0, true);
                }
            }
        }

        public void saveEntitiesIn(Chunk chunk, RegionWrapper region) {
            saveEntitiesIn(chunk, region, 0, 0, false);
        }

        public void saveEntitiesOut(Chunk chunk, RegionWrapper region) {
            for (Entity entity : chunk.getEntities()) {
                Location loc = BukkitUtil.getLocation(entity);
                int x = loc.getX();
                int z = loc.getZ();
                if (isIn(region, x, z)) {
                    continue;
                }
                if (entity.getVehicle() != null) {
                    continue;
                }
                EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
                this.entities.add(wrap);
            }
        }

        public void saveEntitiesIn(Chunk chunk, RegionWrapper region, int offsetX, int offsetZ, boolean delete) {
            for (Entity entity : chunk.getEntities()) {
                Location loc = BukkitUtil.getLocation(entity);
                int x = loc.getX();
                int z = loc.getZ();
                if (!isIn(region, x, z)) {
                    continue;
                }
                if (entity.getVehicle() != null) {
                    continue;
                }
                EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
                wrap.x += offsetX;
                wrap.z += offsetZ;
                this.entities.add(wrap);
                if (delete) {
                    if (!(entity instanceof Player)) {
                        entity.remove();
                    }
                }
            }
        }

        public void restoreEntities(World world, int xOffset, int zOffset) {
            for (EntityWrapper entity : this.entities) {
                try {
                    entity.spawn(world, xOffset, zOffset);
                } catch (Exception e) {
                    PS.debug("Failed to restore entity (e): " + e.toString());
                    e.printStackTrace();
                }
            }
            this.entities.clear();
        }

        public void restoreBlocks(World world, int xOffset, int zOffset) {
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.chestContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof InventoryHolder) {
                        InventoryHolder chest = (InventoryHolder) state;
                        chest.getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate chest: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate chest (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, String[]> blockLocEntry : this.signContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Sign) {
                        Sign sign = (Sign) state;
                        int i = 0;
                        for (String line : blockLocEntry.getValue()) {
                            sign.setLine(i, line);
                            i++;
                        }
                        state.update(true);
                    } else {
                        PS.debug(
                                "&c[WARN] Plot clear failed to regenerate sign: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                        .getKey().y
                                        + ',' + (
                                        blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate sign: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry.getKey().y
                            + ',' + (
                            blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.dispenserContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Dispenser) {
                        ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate dispenser: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate dispenser (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.dropperContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Dropper) {
                        ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate dispenser: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate dispenser (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.beaconContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Beacon) {
                        ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate beacon: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate beacon (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, Material> blockLocMaterialEntry : this.jukeboxDisc.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocMaterialEntry.getKey().x + xOffset, blockLocMaterialEntry.getKey().y, blockLocMaterialEntry
                                    .getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Jukebox) {
                        ((Jukebox) state).setPlaying(blockLocMaterialEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to restore jukebox: " + (blockLocMaterialEntry.getKey().x + xOffset) + ','
                                + blockLocMaterialEntry
                                .getKey().y + ',' + (
                                blockLocMaterialEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate jukebox (e): " + (blockLocMaterialEntry.getKey().x + xOffset) + ','
                            + blockLocMaterialEntry
                            .getKey().y + ',' + (
                            blockLocMaterialEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, Object[]> blockLocEntry : this.skullData.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Skull) {
                        Object[] data = blockLocEntry.getValue();
                        if ((Boolean) data[0]) {
                            ((Skull) state).setOwner((String) data[1]);
                        }
                        ((Skull) state).setRotation((BlockFace) data[2]);
                        ((Skull) state).setSkullType((SkullType) data[3]);
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to restore skull: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (Exception e) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate skull (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    e.printStackTrace();
                }
            }
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.hopperContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Hopper) {
                        ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate hopper: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate hopper (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, Note> blockLocNoteEntry : this.noteBlockContents.entrySet()) {
                try {
                    Block block = world.getBlockAt(
                            blockLocNoteEntry.getKey().x + xOffset, blockLocNoteEntry.getKey().y, blockLocNoteEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof NoteBlock) {
                        ((NoteBlock) state).setNote(blockLocNoteEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate note block: " + (blockLocNoteEntry.getKey().x + xOffset) + ','
                                + blockLocNoteEntry
                                .getKey().y + ',' + (
                                blockLocNoteEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate note block (e): " + (blockLocNoteEntry.getKey().x + xOffset) + ','
                            + blockLocNoteEntry
                            .getKey().y + ',' + (
                            blockLocNoteEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, Short> blockLocShortEntry : this.brewTime.entrySet()) {
                try {
                    Block block = world.getBlockAt(
                            blockLocShortEntry.getKey().x + xOffset, blockLocShortEntry.getKey().y, blockLocShortEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof BrewingStand) {
                        ((BrewingStand) state).setBrewingTime(blockLocShortEntry.getValue());
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to restore brewing stand cooking: " + (blockLocShortEntry.getKey().x + xOffset) + ','
                                + blockLocShortEntry
                                .getKey().y + ',' + (
                                blockLocShortEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug("&c[WARN] Plot clear failed to restore brewing stand cooking (e): " + (blockLocShortEntry.getKey().x + xOffset) + ','
                            + blockLocShortEntry.getKey().y + ',' + (blockLocShortEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, EntityType> blockLocEntityTypeEntry : this.spawnerData.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntityTypeEntry.getKey().x + xOffset, blockLocEntityTypeEntry.getKey().y, blockLocEntityTypeEntry
                                    .getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof CreatureSpawner) {
                        ((CreatureSpawner) state).setSpawnedType(blockLocEntityTypeEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to restore spawner type: " + (blockLocEntityTypeEntry.getKey().x + xOffset) + ','
                                + blockLocEntityTypeEntry
                                .getKey().y + ',' + (
                                blockLocEntityTypeEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug("&c[WARN] Plot clear failed to restore spawner type (e): " + (blockLocEntityTypeEntry.getKey().x + xOffset) + ','
                            + blockLocEntityTypeEntry.getKey().y + ',' + (blockLocEntityTypeEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, String> blockLocStringEntry : this.cmdData.entrySet()) {
                try {
                    Block block = world.getBlockAt(
                            blockLocStringEntry.getKey().x + xOffset, blockLocStringEntry.getKey().y, blockLocStringEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof CommandBlock) {
                        ((CommandBlock) state).setCommand(blockLocStringEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to restore command block: " + (blockLocStringEntry.getKey().x + xOffset) + ','
                                + blockLocStringEntry
                                .getKey().y + ',' + (
                                blockLocStringEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug("&c[WARN] Plot clear failed to restore command block (e): " + (blockLocStringEntry.getKey().x + xOffset) + ','
                            + blockLocStringEntry
                            .getKey().y + ',' + (
                            blockLocStringEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.brewingStandContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof BrewingStand) {
                        ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate brewing stand: " + (blockLocEntry.getKey().x + xOffset) + ','
                                + blockLocEntry
                                .getKey().y + ',' + (
                                blockLocEntry.getKey().z
                                        + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate brewing stand (e): " + (blockLocEntry.getKey().x + xOffset) + ','
                            + blockLocEntry.getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, Short[]> blockLocEntry : this.furnaceTime.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Furnace) {
                        Short[] time = blockLocEntry.getValue();
                        ((Furnace) state).setBurnTime(time[0]);
                        ((Furnace) state).setCookTime(time[1]);
                    } else {
                        PS.debug(
                                "&c[WARN] Plot clear failed to restore furnace cooking: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                        .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug(
                            "&c[WARN] Plot clear failed to restore furnace cooking (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                    .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, ItemStack[]> blockLocEntry : this.furnaceContents.entrySet()) {
                try {
                    Block block =
                            world.getBlockAt(blockLocEntry.getKey().x + xOffset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Furnace) {
                        ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate furnace: " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                                .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                    }
                } catch (IllegalArgumentException ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate furnace (e): " + (blockLocEntry.getKey().x + xOffset) + ',' + blockLocEntry
                            .getKey().y + ',' + (blockLocEntry.getKey().z + zOffset));
                }
            }
            for (Entry<BlockLoc, DyeColor> blockLocByteEntry : this.bannerBase.entrySet()) {
                try {
                    Block block = world.getBlockAt(
                            blockLocByteEntry.getKey().x + xOffset, blockLocByteEntry.getKey().y, blockLocByteEntry.getKey().z + zOffset);
                    BlockState state = block.getState();
                    if (state instanceof Banner) {
                        Banner banner = (Banner) state;
                        DyeColor base = blockLocByteEntry.getValue();
                        List<Pattern> patterns = this.bannerPatterns.get(blockLocByteEntry.getKey());
                        banner.setBaseColor(base);
                        banner.setPatterns(patterns);
                        state.update(true);
                    } else {
                        PS.debug("&c[WARN] Plot clear failed to regenerate banner: " + (blockLocByteEntry.getKey().x + xOffset) + ','
                                + blockLocByteEntry.getKey().y + ',' + (blockLocByteEntry.getKey().z + zOffset));
                    }
                } catch (Exception ignored) {
                    PS.debug("&c[WARN] Plot clear failed to regenerate banner (e): " + (blockLocByteEntry.getKey().x + xOffset) + ','
                            + blockLocByteEntry.getKey().y + ',' + (blockLocByteEntry.getKey().z + zOffset));
                }
            }
        }

        public void saveBlocks(World world, int maxY, int x, int z, int offsetX, int offsetZ, boolean storeNormal) {
            maxY = Math.min(255, maxY);
            PlotBlock[] ids;
            if (storeNormal) {
                ids = new PlotBlock[maxY + 1];
            } else {
                ids = null;
            }
            for (short y = 0; y <= maxY; y++) {
                Block block = world.getBlockAt(x, y, z);
                Material id = block.getType();
                if (storeNormal) {
                    int typeId = id.getId();
                    if (typeId == 0) {
                        ids[y] = PlotBlock.EVERYTHING;
                    } else {
                        ids[y] = PlotBlock.get((short) typeId, block.getData());
                    }
                }
                if (!id.equals(Material.AIR)) {
                    try {
                        BlockLoc bl = new BlockLoc(x + offsetX, y, z + offsetZ);
                        if (block.getState() instanceof InventoryHolder) {
                            InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
                            ItemStack[] inventory = inventoryHolder.getInventory().getContents().clone();
                            switch (id) {
                                case CHEST:
                                    this.chestContents.put(bl, inventory);
                                    break;
                                case DISPENSER:
                                    this.dispenserContents.put(bl, inventory);
                                    break;
                                case BEACON:
                                    this.beaconContents.put(bl, inventory);
                                    break;
                                case DROPPER:
                                    this.dropperContents.put(bl, inventory);
                                    break;
                                case HOPPER:
                                    this.hopperContents.put(bl, inventory);
                                    break;
                                case BREWING_STAND:
                                    BrewingStand brewingStand = (BrewingStand) inventoryHolder;
                                    short time = (short) brewingStand.getBrewingTime();
                                    if (time > 0) {
                                        this.brewTime.put(bl, time);
                                    }
                                    ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                                    this.brewingStandContents.put(bl, invBre);
                                    break;
                                case FURNACE:
                                case BURNING_FURNACE:
                                    Furnace furnace = (Furnace) inventoryHolder;
                                    short burn = furnace.getBurnTime();
                                    short cook = furnace.getCookTime();
                                    ItemStack[] invFur = furnace.getInventory().getContents().clone();
                                    this.furnaceContents.put(bl, invFur);
                                    if (cook != 0) {
                                        this.furnaceTime.put(bl, new Short[]{burn, cook});
                                    }
                                    break;
                            }
                        } else if (block.getState() instanceof CreatureSpawner) {
                            CreatureSpawner spawner = (CreatureSpawner) block.getState();
                            EntityType type = spawner.getSpawnedType();
                            if (type != null) {
                                this.spawnerData.put(bl, type);
                            }
                        } else if (block.getState() instanceof CommandBlock) {
                            CommandBlock cmd = (CommandBlock) block.getState();
                            String string = cmd.getCommand();
                            if (string != null && !string.isEmpty()) {
                                this.cmdData.put(bl, string);
                            }
                        } else if (block.getState() instanceof NoteBlock) {
                            NoteBlock noteBlock = (NoteBlock) block.getState();
                            Note note = noteBlock.getNote();
                            this.noteBlockContents.put(bl, note);
                        } else if (block.getState() instanceof Jukebox) {
                            Jukebox jukebox = (Jukebox) block.getState();
                            Material playing = jukebox.getPlaying();
                            if (playing != null) {
                                this.jukeboxDisc.put(bl, playing);
                            }
                        } else if (block.getState() instanceof Skull) {
                            Skull skull = (Skull) block.getState();
                            this.skullData.put(bl, new Object[]{skull.hasOwner(), skull.getOwner(), skull.getRotation(), skull.getSkullType()});
                        } else if (block.getState() instanceof Banner) {
                            Banner banner = (Banner) block.getState();
                            DyeColor base = banner.getBaseColor();
                            this.bannerBase.put(bl, base);
                            this.bannerPatterns.put(bl, banner.getPatterns());

                        }
                    } catch (Exception e) {
                        PS.debug("------------ FAILED TO DO SOMETHING --------");
                        e.printStackTrace();
                        PS.debug("------------ but we caught it ^ --------");
                    }
                }
            }
            PlotLoc loc = new PlotLoc(x + offsetX, z + offsetZ);
            this.allBlocks.put(loc, ids);
        }
    }
}
