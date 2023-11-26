/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.util.ReflectionUtils;
import com.plotsquared.core.util.ReflectionUtils.RefClass;
import com.plotsquared.core.util.ReflectionUtils.RefField;
import com.plotsquared.core.util.ReflectionUtils.RefMethod;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import io.papermc.lib.PaperLib;
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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;

import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

@SuppressWarnings("unused")
public class ChunkListener implements Listener {

    private final PlotAreaManager plotAreaManager;
    private final int version;

    private RefMethod methodSetUnsaved;
    private RefMethod methodGetHandleChunk;
    private RefMethod methodGetHandleWorld;
    private RefField mustNotSave;
    private Object objChunkStatusFull = null;
    /*
    private RefMethod methodGetFullChunk;
    private RefMethod methodGetBukkitChunk;
    private RefMethod methodGetChunkProvider;
    private RefMethod methodGetVisibleMap;
    private RefField worldServer;
    private RefField playerChunkMap;
    private RefField updatingChunks;
    private RefField visibleChunks;
    */
    private Chunk lastChunk;
    private boolean ignoreUnload = false;

    @Inject
    public ChunkListener(final @NonNull PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
        version = PlotSquared.platform().serverVersion()[1];
        if (!Settings.Chunk_Processor.AUTO_TRIM) {
            return;
        }
        try {
            RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
            RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
            ReflectionUtils.RefClass classChunkAccess = getRefClass("net.minecraft.world.level.chunk.IChunkAccess");
            this.methodSetUnsaved = classChunkAccess.getMethod("a", boolean.class);
            try {
                this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
            } catch (NoSuchMethodException ignored) {
                try {
                    RefClass classChunkStatus = getRefClass("net.minecraft.world.level.chunk.ChunkStatus");
                    this.objChunkStatusFull = classChunkStatus.getRealClass().getField("n").get(null);
                    this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle", classChunkStatus.getRealClass());
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                if (version < 17) {
                    RefClass classChunk = getRefClass("{nms}.Chunk");
                    this.mustNotSave = classChunk.getField("mustNotSave");
                } else {
                    RefClass classChunk = getRefClass("net.minecraft.world.level.chunk.Chunk");
                    this.mustNotSave = classChunk.getField("mustNotSave");
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } catch (Throwable ignored) {
            Settings.Chunk_Processor.AUTO_TRIM = false;
        }
        for (World world : Bukkit.getWorlds()) {
            world.setAutoSave(false);
        }
        if (version > 13) {
            return;
        }
        TaskManager.runTaskRepeat(() -> {
            try {
                HashSet<Chunk> toUnload = new HashSet<>();
                for (World world : Bukkit.getWorlds()) {
                    String worldName = world.getName();
                    if (!this.plotAreaManager.hasPlotArea(worldName)) {
                        continue;
                    }
                    Object craftWorld = methodGetHandleWorld.of(world).call();
                    if (version == 13) {
                        Object chunkMap = craftWorld.getClass().getDeclaredMethod("getPlayerChunkMap").invoke(craftWorld);
                        Method methodIsChunkInUse =
                                chunkMap.getClass().getDeclaredMethod("isChunkInUse", int.class, int.class);
                        Chunk[] chunks = world.getLoadedChunks();
                        for (Chunk chunk : chunks) {
                            if ((boolean) methodIsChunkInUse.invoke(chunkMap, chunk.getX(), chunk.getZ())) {
                                continue;
                            }
                            int x = chunk.getX();
                            int z = chunk.getZ();
                            if (!shouldSave(worldName, x, z)) {
                                unloadChunk(worldName, chunk, false);
                                continue;
                            }
                            toUnload.add(chunk);
                        }
                    }
                }
                if (toUnload.isEmpty()) {
                    return;
                }
                long start = System.currentTimeMillis();
                for (Chunk chunk : toUnload) {
                    if (System.currentTimeMillis() - start > 5) {
                        return;
                    }
                    chunk.unload(true);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, TaskTime.ticks(1L));
    }

    public boolean unloadChunk(String world, Chunk chunk, boolean safe) {
        if (safe && shouldSave(world, chunk.getX(), chunk.getZ())) {
            return false;
        }
        Object c = objChunkStatusFull != null
                ? this.methodGetHandleChunk.of(chunk).call(objChunkStatusFull)
                : this.methodGetHandleChunk.of(chunk).call();
        RefField.RefExecutor field = this.mustNotSave.of(c);
        methodSetUnsaved.of(c).call(false);
        if (!((Boolean) field.get())) {
            field.set(true);
            if (chunk.isLoaded()) {
                ignoreUnload = true;
                chunk.unload(false);
                ignoreUnload = false;
            }
        }
        return true;
    }

    public boolean shouldSave(String world, int chunkX, int chunkZ) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        int x2 = x + 15;
        int z2 = z + 15;
        Location loc = Location.at(world, x, 1, z);
        PlotArea plotArea = plotAreaManager.getPlotArea(loc);
        if (plotArea != null) {
            Plot plot = plotArea.getPlot(loc);
            if (plot != null && plot.hasOwner()) {
                return true;
            }
        }
        loc = Location.at(world, x2, 1, z2);
        plotArea = plotAreaManager.getPlotArea(loc);
        if (plotArea != null) {
            Plot plot = plotArea.getPlot(loc);
            if (plot != null && plot.hasOwner()) {
                return true;
            }
        }
        loc = Location.at(world, x2, 1, z);
        plotArea = plotAreaManager.getPlotArea(loc);
        if (plotArea != null) {
            Plot plot = plotArea.getPlot(loc);
            if (plot != null && plot.hasOwner()) {
                return true;
            }
        }
        loc = Location.at(world, x, 1, z2);
        plotArea = plotAreaManager.getPlotArea(loc);
        if (plotArea != null) {
            Plot plot = plotArea.getPlot(loc);
            if (plot != null && plot.hasOwner()) {
                return true;
            }
        }
        loc = Location.at(world, x + 7, 1, z + 7);
        plotArea = plotAreaManager.getPlotArea(loc);
        if (plotArea == null) {
            return false;
        }
        Plot plot = plotArea.getPlot(loc);
        return plot != null && plot.hasOwner();
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (ignoreUnload) {
            return;
        }
        Chunk chunk = event.getChunk();
        if (Settings.Chunk_Processor.AUTO_TRIM) {
            String world = chunk.getWorld().getName();
            if ((!Settings.Enabled_Components.WORLDS || !SinglePlotArea.isSinglePlotWorld(world)) && this.plotAreaManager.hasPlotArea(
                    world)) {
                if (unloadChunk(world, chunk, true)) {
                    return;
                }
            }
        }
        if (processChunk(event.getChunk(), true)) {
            chunk.setForceLoaded(true);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        processChunk(event.getChunk(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        PaperLib.getChunkAtAsync(event.getLocation()).thenAccept(chunk -> {
            if (chunk == this.lastChunk) {
                event.getEntity().remove();
                event.setCancelled(true);
                return;
            }
            if (!this.plotAreaManager.hasPlotArea(chunk.getWorld().getName())) {
                return;
            }
            Entity[] entities = chunk.getEntities();
            if (entities.length > Settings.Chunk_Processor.MAX_ENTITIES) {
                event.getEntity().remove();
                event.setCancelled(true);
                this.lastChunk = chunk;
            } else {
                this.lastChunk = null;
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (Settings.Chunk_Processor.DISABLE_PHYSICS) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        PaperLib.getChunkAtAsync(event.getLocation()).thenAccept(chunk -> {
            if (chunk == this.lastChunk) {
                event.getEntity().remove();
                event.setCancelled(true);
                return;
            }
            if (!this.plotAreaManager.hasPlotArea(chunk.getWorld().getName())) {
                return;
            }
            Entity[] entities = chunk.getEntities();
            if (entities.length > Settings.Chunk_Processor.MAX_ENTITIES) {
                event.getEntity().remove();
                event.setCancelled(true);
                this.lastChunk = chunk;
            } else {
                this.lastChunk = null;
            }
        });
    }

    private void cleanChunk(final Chunk chunk) {
        final int currentIndex = TaskManager.index.incrementAndGet();
        PlotSquaredTask task = TaskManager.runTaskRepeat(() -> {
            if (!chunk.isLoaded()) {
                Objects.requireNonNull(TaskManager.removeTask(currentIndex)).cancel();
                chunk.unload(true);
                return;
            }
            BlockState[] tiles = chunk.getTileEntities();
            if (tiles.length == 0) {
                Objects.requireNonNull(TaskManager.removeTask(currentIndex)).cancel();
                chunk.unload(true);
                return;
            }
            long start = System.currentTimeMillis();
            int i = 0;
            while (System.currentTimeMillis() - start < 250) {
                if (i >= tiles.length - Settings.Chunk_Processor.MAX_TILES) {
                    Objects.requireNonNull(TaskManager.removeTask(currentIndex)).cancel();
                    chunk.unload(true);
                    return;
                }
                tiles[i].getBlock().setType(Material.AIR, false);
                i++;
            }
        }, TaskTime.ticks(5L));
        TaskManager.addTask(task, currentIndex);
    }

    public boolean processChunk(Chunk chunk, boolean unload) {
        if (!this.plotAreaManager.hasPlotArea(chunk.getWorld().getName())) {
            return false;
        }
        Entity[] entities = chunk.getEntities();
        BlockState[] tiles = chunk.getTileEntities();
        if (entities.length > Settings.Chunk_Processor.MAX_ENTITIES) {
            int toRemove = entities.length - Settings.Chunk_Processor.MAX_ENTITIES;
            int index = 0;
            while (toRemove > 0 && index < entities.length) {
                final Entity entity = entities[index++];
                if (!(entity instanceof Player)) {
                    entity.remove();
                    toRemove--;
                }
            }
        }
        if (tiles.length > Settings.Chunk_Processor.MAX_TILES) {
            if (unload) {
                cleanChunk(chunk);
                return true;
            }

            for (int i = 0; i < (tiles.length - Settings.Chunk_Processor.MAX_TILES); i++) {
                tiles[i].getBlock().setType(Material.AIR, false);
            }
        }
        return false;
    }

}
