package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.bukkit.object.entity.EntityWrapper;
import com.github.intellectualsites.plotsquared.bukkit.object.entity.ReplicatingEntityWrapper;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.AugmentedUtils;
import com.github.intellectualsites.plotsquared.plot.listener.WEExtent;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.sk89q.worldedit.world.block.BlockState;
import com.github.intellectualsites.plotsquared.plot.object.PlotLoc;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BukkitChunkManager extends ChunkManager {

    public static boolean isIn(RegionWrapper region, int x, int z) {
        return x >= region.minX && x <= region.maxX && z >= region.minZ && z <= region.maxZ;
    }

    public static ContentMap swapChunk(World world1, World world2, Chunk pos1, Chunk pos2,
        RegionWrapper r1, RegionWrapper r2) {
        ContentMap map = new ContentMap();
        int relX = r2.minX - r1.minX;
        int relZ = r2.minZ - r1.minZ;

        map.saveEntitiesIn(pos1, r1, relX, relZ, true);
        map.saveEntitiesIn(pos2, r2, -relX, -relZ, true);

        int sx = pos1.getX() << 4;
        int sz = pos1.getZ() << 4;

        String worldName1 = world1.getName();
        String worldName2 = world2.getName();

        BukkitWorld bukkitWorld1 = new BukkitWorld(world1);
        BukkitWorld bukkitWorld2 = new BukkitWorld(world2);

        LocalBlockQueue queue1 = GlobalBlockQueue.IMP.getNewQueue(worldName1, false);
        LocalBlockQueue queue2 = GlobalBlockQueue.IMP.getNewQueue(worldName2, false);

        for (int x = Math.max(r1.minX, sx); x <= Math.min(r1.maxX, sx + 15); x++) {
            for (int z = Math.max(r1.minZ, sz); z <= Math.min(r1.maxZ, sz + 15); z++) {
                for (int y = 0; y < 256; y++) {
                    Block block1 = world1.getBlockAt(x, y, z);
                    BaseBlock baseBlock1 = bukkitWorld1.getFullBlock(BlockVector3.at(x, y, z));
                    BlockData data1 = block1.getBlockData();

                    int xx = x + relX;
                    int zz = z + relZ;

                    Block block2 = world2.getBlockAt(xx, y, zz);
                    BaseBlock baseBlock2 = bukkitWorld2.getFullBlock(BlockVector3.at(xx, y, zz));
                    BlockData data2 = block2.getBlockData();

                    if (block1.isEmpty()) {
                        if (!block2.isEmpty()) {
                            queue1.setBlock(x, y, z, baseBlock2);
                            queue2.setBlock(xx, y, zz, WEExtent.AIRBASE);
                        }
                    } else if (block2.isEmpty()) {
                        queue1.setBlock(x, y, z, WEExtent.AIRBASE);
                        queue2.setBlock(xx, y, zz, baseBlock1);
                    } else if (block1.equals(block2)) {
                        if (!data1.matches(data2)) {
                            block1.setBlockData(data2);
                            block2.setBlockData(data1);
                        }
                    } else {
                        queue1.setBlock(x, y, z, baseBlock2);
                        queue2.setBlock(xx, y, zz, baseBlock1);
                    }
                }
            }
        }
        queue1.enqueue();
        queue2.enqueue();
        return map;
    }

    @Override public Set<BlockVector2> getChunkChunks(String world) {
        Set<BlockVector2> chunks = super.getChunkChunks(world);
        for (Chunk chunk : Bukkit.getWorld(world).getLoadedChunks()) {
            BlockVector2 loc = BlockVector2.at(chunk.getX() >> 5, chunk.getZ() >> 5);
            chunks.add(loc);
        }
        return chunks;
    }

    @Override public int[] countEntities(Plot plot) {
        int[] existing = (int[]) plot.getMeta("EntityCount");
        if (existing != null && (System.currentTimeMillis() - (long) plot.getMeta("EntityCountTime")
            < 1000)) {
            return existing;
        }
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
        if (size > 200 && chunks.size() > 200) {
            entities = world.getEntities();
            if (entities.size() < 16 + size / 8) {
                doWhole = true;
            }
        }

        int[] count = new int[6];
        if (doWhole) {
            for (Entity entity : entities) {
                org.bukkit.Location location = entity.getLocation();
                PaperLib.getChunkAtAsync(location).thenAccept( chunk -> {
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
                });
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

    @Override public boolean copyRegion(Location pos1, Location pos2, Location newPos,
        final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();

        final RegionWrapper region =
            new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World oldWorld = Bukkit.getWorld(pos1.getWorld());
        final BukkitWorld oldBukkitWorld = new BukkitWorld(oldWorld);
        final World newWorld = Bukkit.getWorld(newPos.getWorld());
        assert newWorld != null;
        assert oldWorld != null;
        final String newWorldName = newWorld.getName();
        final ContentMap map = new ContentMap();
        final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(newWorldName, false);
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                int bx = value[2];
                int bz = value[3];
                int tx = value[4];
                int tz = value[5];
                BlockVector2 loc = BlockVector2.at(value[0], value[1]);
                int cxx = loc.getX() << 4;
                int czz = loc.getZ() << 4;
                PaperLib.getChunkAtAsync(oldWorld, loc.getX(), loc.getZ())
                    .thenAccept(chunk1 -> map.saveEntitiesIn(chunk1, region)).thenRun(() -> {
                    for (int x = bx & 15; x <= (tx & 15); x++) {
                        for (int z = bz & 15; z <= (tz & 15); z++) {
                            map.saveBlocks(oldBukkitWorld, 256, cxx + x, czz + z, relX, relZ);
                        }
                    }
                });
            }
        }, () -> {
            for (Entry<PlotLoc, BaseBlock[]> entry : map.allBlocks.entrySet()) {
                PlotLoc loc = entry.getKey();
                BaseBlock[] blocks = entry.getValue();
                for (int y = 0; y < blocks.length; y++) {
                    if (blocks[y] != null) {
                        BaseBlock block = blocks[y];
                        queue.setBlock(loc.getX(), y, loc.getZ(), block);
                    }
                }
            }
            queue.enqueue();
            GlobalBlockQueue.IMP.addEmptyTask(() -> {
                //map.restoreBlocks(newWorld, 0, 0);
                map.restoreEntities(newWorld, relX, relZ);
                TaskManager.runTask(whenDone);
            });
        }, 5);
        return true;
    }

    @Override public boolean regenerateRegion(final Location pos1, final Location pos2,
        final boolean ignoreAugment, final Runnable whenDone) {
        final String world = pos1.getWorld();

        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;

        final List<BlockVector2> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(BlockVector2.at(x, z));
            }
        }
        final World worldObj = Bukkit.getWorld(world);
        final BukkitWorld bukkitWorldObj = new BukkitWorld(worldObj);
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 5) {
                    final BlockVector2 chunk = chunks.remove(0);
                    int x = chunk.getX();
                    int z = chunk.getZ();
                    int xxb = x << 4;
                    int zzb = z << 4;
                    int xxt = xxb + 15;
                    int zzt = zzb + 15;
                    Chunk chunkObj = worldObj.getChunkAt(x, z);
                    if (!chunkObj.load(false)) {
                        continue;
                    }
                    final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
                    if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z
                        && PlotSquared.imp().getServerVersion()[1] == 13) {
                        AugmentedUtils
                            .bypass(ignoreAugment, () -> queue.regenChunkSafe(chunk.getX(), chunk.getZ()));
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
                        map.saveRegion(bukkitWorldObj, xxb, xxb2, zzb2, zzt2); //
                    }
                    if (checkX2) {
                        map.saveRegion(bukkitWorldObj, xxt2, xxt, zzb2, zzt2); //
                    }
                    if (checkZ1) {
                        map.saveRegion(bukkitWorldObj, xxb2, xxt2, zzb, zzb2); //
                    }
                    if (checkZ2) {
                        map.saveRegion(bukkitWorldObj, xxb2, xxt2, zzt2, zzt); //
                    }
                    if (checkX1 && checkZ1) {
                        map.saveRegion(bukkitWorldObj, xxb, xxb2, zzb, zzb2); //
                    }
                    if (checkX2 && checkZ1) {
                        map.saveRegion(bukkitWorldObj, xxt2, xxt, zzb, zzb2); // ?
                    }
                    if (checkX1 && checkZ2) {
                        map.saveRegion(bukkitWorldObj, xxb, xxb2, zzt2, zzt); // ?
                    }
                    if (checkX2 && checkZ2) {
                        map.saveRegion(bukkitWorldObj, xxt2, xxt, zzt2, zzt); //
                    }
                    RegionWrapper currentPlotClear =
                        new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
                    map.saveEntitiesOut(chunkObj, currentPlotClear);
                    AugmentedUtils.bypass(ignoreAugment,
                        () -> setChunkInPlotArea(null, new RunnableVal<ScopedLocalBlockQueue>() {
                            @Override public void run(ScopedLocalBlockQueue value) {
                                Location min = value.getMin();
                                int bx = min.getX();
                                int bz = min.getZ();
                                for (int x1 = 0; x1 < 16; x1++) {
                                    for (int z1 = 0; z1 < 16; z1++) {
                                        PlotLoc plotLoc = new PlotLoc(bx + x1, bz + z1);
                                        BaseBlock[] ids = map.allBlocks.get(plotLoc);
                                        if (ids != null) {
                                            for (int y = 0; y < Math.min(128, ids.length); y++) {
                                                BaseBlock id = ids[y];
                                                if (id != null) {
                                                    value.setBlock(x1, y, z1, id);
                                                } else {
                                                    value.setBlock(x1, y, z1, BlockUtil.get("air"));
                                                }
                                            }
                                            for (int y = Math.min(128, ids.length);
                                                 y < ids.length; y++) {
                                                BaseBlock id = ids[y];
                                                if (id != null) {
                                                    value.setBlock(x1, y, z1, id);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }, world, chunk));
                    //map.restoreBlocks(worldObj, 0, 0);
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

    @Override public CompletableFuture loadChunk(String world, BlockVector2 chunkLoc, boolean force) {
        return PaperLib.getChunkAtAsync(BukkitUtil.getWorld(world),chunkLoc.getX(), chunkLoc.getZ(), force);
    }

    @Override
    public void unloadChunk(final String world, final BlockVector2 chunkLoc, final boolean save) {
        if (!PlotSquared.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(
                () -> BukkitUtil.getWorld(world).unloadChunk(chunkLoc.getX(), chunkLoc.getZ(), save));
        } else {
            BukkitUtil.getWorld(world).unloadChunk(chunkLoc.getX(), chunkLoc.getZ(), save);
        }
    }

    @Override public void clearAllEntities(Location pos1, Location pos2) {
        String world = pos1.getWorld();
        List<Entity> entities = BukkitUtil.getEntities(world);
        int bx = pos1.getX();
        int bz = pos1.getZ();
        int tx = pos2.getX();
        int tz = pos2.getZ();
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) {
                org.bukkit.Location location = entity.getLocation();
                if (location.getX() >= bx && location.getX() <= tx && location.getZ() >= bz
                    && location.getZ() <= tz) {
                    if (entity.hasMetadata("ps-tmp-teleport")) {
                        continue;
                    }
                    entity.remove();
                }
            }
        }
    }

    @Override public void swap(Location bot1, Location top1, Location bot2, Location top2,
        final Runnable whenDone) {
        RegionWrapper region1 =
            new RegionWrapper(bot1.getX(), top1.getX(), bot1.getZ(), top1.getZ());
        RegionWrapper region2 =
            new RegionWrapper(bot2.getX(), top2.getX(), bot2.getZ(), top2.getZ());
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
        GlobalBlockQueue.IMP.addEmptyTask(() -> {
            for (ContentMap map : maps) {
                map.restoreEntities(world1, 0, 0);
                TaskManager.runTaskLater(whenDone, 1);
            }
        });
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
            case TRIDENT:
            case SHULKER_BULLET:
            case SPECTRAL_ARROW:
            case DRAGON_FIREBALL:
            case LLAMA_SPIT:
                // projectile
            case PRIMED_TNT:
            case FALLING_BLOCK:
                // Block entities
            case ENDER_CRYSTAL:
            case FISHING_HOOK:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case LEASH_HITCH:
            case FIREWORK:
            case LIGHTNING:
            case WITHER_SKULL:
            case UNKNOWN:
            case AREA_EFFECT_CLOUD:
            case EVOKER_FANGS:
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
            case DONKEY:
            case LLAMA:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case MULE:
            case DOLPHIN:
            case TURTLE:
            case COD:
            case PARROT:
            case SALMON:
            case PUFFERFISH:
            case TROPICAL_FISH:
            case CAT:
            case FOX:
            case PANDA:
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
            case ELDER_GUARDIAN:
            case STRAY:
            case HUSK:
            case EVOKER:
            case VEX:
            case WITHER_SKELETON:
            case ZOMBIE_VILLAGER:
            case VINDICATOR:
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

        final Set<EntityWrapper> entities;
        final Map<PlotLoc, BaseBlock[]> allBlocks;

        ContentMap() {
            this.entities = new HashSet<>();
            this.allBlocks = new HashMap<>();
        }

        public void saveRegion(BukkitWorld world, int x1, int x2, int z1, int z2) {
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
                    saveBlocks(world, 256, x, z, 0, 0);
                }
            }
        }

        void saveEntitiesOut(Chunk chunk, RegionWrapper region) {
            for (Entity entity : chunk.getEntities()) {
                Location location = BukkitUtil.getLocation(entity);
                int x = location.getX();
                int z = location.getZ();
                if (isIn(region, x, z)) {
                    continue;
                }
                if (entity.getVehicle() != null) {
                    continue;
                }
                EntityWrapper wrap = new ReplicatingEntityWrapper(entity, (short) 2);
                wrap.saveEntity();
                this.entities.add(wrap);
            }
        }

        void saveEntitiesIn(Chunk chunk, RegionWrapper region) {
            saveEntitiesIn(chunk, region, 0, 0, false);
        }

        void saveEntitiesIn(Chunk chunk, RegionWrapper region, int offsetX, int offsetZ,
            boolean delete) {
            for (Entity entity : chunk.getEntities()) {
                Location location = BukkitUtil.getLocation(entity);
                int x = location.getX();
                int z = location.getZ();
                if (!isIn(region, x, z)) {
                    continue;
                }
                if (entity.getVehicle() != null) {
                    continue;
                }
                EntityWrapper wrap = new ReplicatingEntityWrapper(entity, (short) 2);
                wrap.x += offsetX;
                wrap.z += offsetZ;
                wrap.saveEntity();
                this.entities.add(wrap);
                if (delete) {
                    if (!(entity instanceof Player)) {
                        entity.remove();
                    }
                }
            }
        }

        void restoreEntities(World world, int xOffset, int zOffset) {
            for (EntityWrapper entity : this.entities) {
                try {
                    entity.spawn(world, xOffset, zOffset);
                } catch (Exception e) {
                    PlotSquared.debug("Failed to restore entity (e): " + e.toString());
                    e.printStackTrace();
                }
            }
            this.entities.clear();
        }

        //todo optimize maxY
        void saveBlocks(BukkitWorld world, int maxY, int x, int z, int offsetX, int offsetZ) {
            maxY = Math.min(255, maxY);
            BaseBlock[] ids = new BaseBlock[maxY + 1];
            for (short y = 0; y <= maxY; y++) {
                BaseBlock block = world.getFullBlock(BlockVector3.at(x, y, z));
                ids[y] = block;
            }
            PlotLoc loc = new PlotLoc(x + offsetX, z + offsetZ);
            this.allBlocks.put(loc, ids);
        }
    }
}
