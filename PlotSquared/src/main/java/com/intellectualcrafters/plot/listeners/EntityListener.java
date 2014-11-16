package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class EntityListener implements Listener {

    public static HashMap<String, HashMap<Plot, HashSet<Integer>>> entityMap = new HashMap<String, HashMap<Plot, HashSet<Integer>>>();

    public EntityListener() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(PlotMain.getMain(), new Runnable() {
            @Override
            public void run() {
                Iterator<Entry<String, HashMap<Plot, HashSet<Integer>>>> worldIt = entityMap.entrySet().iterator();

                Set<Plot> plots = PlotMain.getPlots();

                while (worldIt.hasNext()) {
                    Entry<String, HashMap<Plot, HashSet<Integer>>> entry = worldIt.next();
                    String worldname = entry.getKey();
                    if (!PlotMain.isPlotWorld(worldname)) {
                        worldIt.remove();
                        continue;
                    }
                    World world = Bukkit.getWorld(worldname);
                    if (world == null || entry.getValue().size() == 0) {
                        worldIt.remove();
                        continue;
                    }
                    Iterator<Entry<Plot, HashSet<Integer>>> it = entry.getValue().entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<Plot, HashSet<Integer>> plotEntry = it.next();
                        Plot plot = plotEntry.getKey();
                        if (!plots.contains(plot)) {
                            it.remove();
                            continue;
                        }

                        boolean loaded = false;

                        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
                        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
                        try {
                            loops:
                            for (int i = (pos1.getBlockX() / 16) * 16; i < (16 + ((pos2.getBlockX() / 16) * 16)); i += 16) {
                                for (int j = (pos1.getBlockZ() / 16) * 16; j < (16 + ((pos2.getBlockZ() / 16) * 16)); j += 16) {
                                    final Chunk chunk = world.getChunkAt(i, j);
                                    if (chunk.isLoaded()) {
                                        loaded = true;
                                        break loops;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            it.remove();
                            continue;
                        }
                        if (!loaded) {
                            it.remove();
                        }
                    }
                }
            }
        }, 24000L, 48000L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Player p = e.getPlayer();
            World w = p.getWorld();
            String n = w.getName();
            if (e.getMaterial() == Material.MONSTER_EGG || e.getMaterial() == Material.MONSTER_EGGS) {
                if (entityMap.containsKey(n)) {
                    Location l = e.getClickedBlock().getLocation();
                    Plot plot = PlotHelper.getCurrentPlot(l);
                    if (plot != null && plot.hasRights(p)) {
                        int mobs;
                        if (entityMap.get(n).containsKey(plot)) {
                            mobs = entityMap.get(n).get(plot).size();
                        } else {
                            mobs = 0;
                        }
                        if (!(PlotMain.hasPermissionRange(p, "plots.mobcap", Settings.MOB_CAP) > mobs)) {
                            PlayerFunctions.sendMessage(p, C.NO_PLOTS, "plots.mobcap." + (mobs + 1));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onCreatureSpawn(CreatureSpawnEvent e) {
        Location l = e.getLocation();
        String w = l.getWorld().getName();
        if (PlotMain.isPlotWorld(w)) {
            Plot plot = PlotHelper.getCurrentPlot(l);
            if (plot != null && plot.hasOwner()) {
                addEntity(e.getEntity(), plot);
            }
        }
    }

    @EventHandler
    public static void onChunkLoad(final ChunkLoadEvent e) {
        if (PlotMain.isPlotWorld(e.getWorld())) {
            for (Entity entity : e.getChunk().getEntities()) {
                if (entity instanceof LivingEntity) {
                    if (!(entity instanceof Player)) {
                        Plot plot = PlotHelper.getCurrentPlot(entity.getLocation());
                        if (plot != null) {
                            if (plot.hasOwner()) {
                                addEntity(entity, plot);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void addEntity(Entity entity, Plot plot) {
        if (!entityMap.containsKey(plot.world)) {
            entityMap.put(plot.world, new HashMap<Plot, HashSet<Integer>>());
        }
        HashMap<Plot, HashSet<Integer>> section = entityMap.get(plot.world);
        if (section.containsKey(plot)) {
            section.put(plot, new HashSet<Integer>());
        }
        section.get(plot).add(entity.getEntityId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onEntityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        Location l = entity.getLocation();
        String w = l.getWorld().getName();
        if (entityMap.containsKey(w)) {
            int id = entity.getEntityId();
            Plot plot = PlotHelper.getCurrentPlot(entity.getLocation());
            if (plot != null) {
                if (entityMap.get(w).containsKey(plot)) {
                    entityMap.get(w).get(plot).remove(id);
                }
            } else {
                Iterator<Entry<Plot, HashSet<Integer>>> it = entityMap.get(w).entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Plot, HashSet<Integer>> n = it.next();
                    n.getValue().remove(id);
                }
            }
        }
    }

    @EventHandler
    public static void onChunkDespawn(final ChunkUnloadEvent e) {
        String w = e.getWorld().getName();
        if (entityMap.containsKey(w)) {
            for (Entity entity : e.getChunk().getEntities()) {
                if (entity instanceof LivingEntity) {
                    if (!(entity instanceof Player)) {
                        Plot plot = PlotHelper.getCurrentPlot(entity.getLocation());
                        if (plot != null) {
                            if (plot.hasOwner()) {
                                if (entityMap.get(w).containsKey(plot)) {
                                    if (entityMap.get(w).get(plot).size() == 1) {
                                        entityMap.get(w).remove(plot);
                                    } else {
                                        entityMap.get(w).get(plot).remove(entity.getEntityId());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
