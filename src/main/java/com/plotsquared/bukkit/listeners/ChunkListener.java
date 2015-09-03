package com.plotsquared.bukkit.listeners;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class ChunkListener implements Listener {

    private Chunk lastChunk = null;
    
    final RefClass classChunk = getRefClass("{nms}.Chunk");
    final RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
    final RefMethod methodGetHandleChunk;
    final RefField mustSave = classChunk.getField("mustSave");
    
    public ChunkListener() {
        RefMethod method;
        try {
            method = classCraftChunk.getMethod("getHandle");
        }
        catch (Exception e) {
            method = null;
            e.printStackTrace();
        }
        methodGetHandleChunk = method;
        
        if (!Settings.CHUNK_PROCESSOR_GC) {
            return;
        }
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                int distance = Bukkit.getViewDistance() + 1;
                HashMap<String, HashMap<ChunkLoc, Integer>> players = new HashMap<>();
                for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                    PlotPlayer pp = entry.getValue();
                    Location loc = pp.getLocation();
                    String world = loc.getWorld();
                    if (!PS.get().isPlotWorld(world)) {
                        continue;
                    }
                    HashMap<ChunkLoc, Integer> map = players.get(world);
                    if (map == null) {
                        map = new HashMap<>();
                        players.put(world, map);
                    }
                    ChunkLoc origin = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
                    Integer val = map.get(origin);
                    int check;
                    if (val != null) {
                        if (val == distance) {
                            continue;
                        }
                        check = distance - val;
                    }
                    else {
                        check = distance;
                        map.put(origin, distance);
                    }
                    for (int x = -distance; x <= distance; x++) {
                        if (x >= check || -x >= check) {
                            continue;
                        }
                        for (int z = -distance; z <= distance; z++) {
                            if (z >= check || -z >= check) {
                                continue;
                            }
                            int weight = distance - Math.max(Math.abs(x), Math.abs(z));
                            ChunkLoc chunk = new ChunkLoc(x + origin.x, z + origin.z);
                            val = map.get(chunk);
                            if (val == null || val < weight) {
                                map.put(chunk, weight);
                            }
                            
                        }
                    }
                }
                for (World world : Bukkit.getWorlds()) {
                    String name = world.getName();
                    boolean autosave = world.isAutoSave();
                    if (!PS.get().isPlotWorld(name)) {
                        continue;
                    }
                    if (!Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE && autosave) {
                        world.setAutoSave(false);
                    }
                    HashMap<ChunkLoc, Integer> map = players.get(name);
                    if (map == null || map.size() == 0) {
                        continue;
                    }
                    for (Chunk chunk : world.getLoadedChunks()) {
                        int x = chunk.getX();
                        int z = chunk.getZ();
                        if (!map.containsKey(new ChunkLoc(x, z))) {
                            if (!Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE || !unloadChunk(name, chunk)) {
                                chunk.unload(true, false);
                            }
                        }
                    }
                    if (!Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE && autosave) {
                        world.setAutoSave(true);
                    }
                }
            }
        }, 300);
    }
    
    public boolean unloadChunk(String world, Chunk chunk) {
        int X = chunk.getX();
        int Z = chunk.getZ();
        int x = X << 4;
        int z = Z << 4;
        int x2 = x + 15;
        int z2 = z + 15;
        Plot plot;
        plot = MainUtil.getPlot(new Location(world, x, 1, z));
        if (plot != null && plot.owner != null) return false;
        plot = MainUtil.getPlot(new Location(world, x2, 1, z2));
        if (plot != null && plot.owner != null) return false;
        plot = MainUtil.getPlot(new Location(world, x2, 1, z));
        if (plot != null && plot.owner != null) return false;
        plot = MainUtil.getPlot(new Location(world, x, 1, z2));
        if (plot != null && plot.owner != null) return false;
        plot = MainUtil.getPlot(new Location(world, x + 7, 1, z + 7));
        if (plot != null && plot.owner != null) return false;
        Object c = methodGetHandleChunk.of(chunk).call();
        mustSave.of(c).set(false);
        chunk.unload(false, false);
        return true;
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE) {
            Chunk chunk = event.getChunk();
            String world = chunk.getWorld().getName();
            if (PS.get().isPlotWorld(world)) {
                if (unloadChunk(world, chunk)) {
                    return;
                }
            }
        }
        if (processChunk(event.getChunk(), true)) {
            event.setCancelled(true);
        }
    }
    

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        processChunk(event.getChunk(), false);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        if (chunk == lastChunk) {
            event.getEntity().remove();
            event.setCancelled(true);
            return;
        }
        if (!PS.get().isPlotWorld(chunk.getWorld().getName())) {
            return;
        }
        Entity[] entities = chunk.getEntities();
        if (entities.length > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            event.getEntity().remove();
            event.setCancelled(true);
            lastChunk = chunk;
        }
        else {
            lastChunk = null;
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (Settings.CHUNK_PROCESSOR_DISABLE_PHYSICS) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        if (chunk == lastChunk) {
            event.getEntity().remove();
            event.setCancelled(true);
            return;
        }
        if (!PS.get().isPlotWorld(chunk.getWorld().getName())) {
            return;
        }
        Entity[] entities = chunk.getEntities();
        if (entities.length > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            event.getEntity().remove();
            event.setCancelled(true);
            lastChunk = chunk;
        }
        else {
            lastChunk = null;
        }
    }

    public void cleanChunk(final Chunk chunk) {
        TaskManager.index.incrementAndGet();
        final Integer currentIndex = TaskManager.index.get();
        final Integer task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (!chunk.isLoaded()) {
                    Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                    TaskManager.tasks.remove(currentIndex);
                    PS.debug("[PlotSquared] &aSuccessfully processed and unloaded chunk!");
                    chunk.unload(true, true);
                    return;
                }
                BlockState[] tiles = chunk.getTileEntities();
                if (tiles.length == 0) {
                    Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                    TaskManager.tasks.remove(currentIndex);
                    PS.debug("[PlotSquared] &aSuccessfully processed and unloaded chunk!");
                    chunk.unload(true, true);
                    return;
                }
                long start = System.currentTimeMillis();
                int i = 0;
                while (System.currentTimeMillis() - start < 250) {
                    if (i >= tiles.length) {
                        Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(currentIndex));
                        TaskManager.tasks.remove(currentIndex);
                        PS.debug("[PlotSquared] &aSuccessfully processed and unloaded chunk!");
                        chunk.unload(true, true);
                        return;
                    }
                    tiles[i].getBlock().setType(Material.AIR, false);
                    i++;
                }
            }
        }, 5);
        TaskManager.tasks.put(currentIndex, task);
    }
    
    public boolean processChunk(Chunk chunk, boolean unload) {
        if (!PS.get().isPlotWorld(chunk.getWorld().getName())) {
            return false;
        }
        Entity[] entities = chunk.getEntities();
        BlockState[] tiles = chunk.getTileEntities();
        if (entities.length > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            for (Entity ent : entities) {
                if (!(ent instanceof Player)) {
                    ent.remove();
                }
            }
            PS.debug("[PlotSquared] &a detected unsafe chunk and processed: " + (chunk.getX() << 4) + "," + (chunk.getX() << 4));
        }
        if (tiles.length > Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES) {
            if (unload) {
                PS.debug("[PlotSquared] &c detected unsafe chunk: " + (chunk.getX() << 4) + "," + (chunk.getX() << 4));
                cleanChunk(chunk);
                return true;
            }
            for (BlockState tile : tiles) {
                tile.getBlock().setType(Material.AIR, false);
            }
        }
        return false;
    }
}
