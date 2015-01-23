package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.commands.Auto;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotManager;

public class ExpireManager {
    
    private static long timestamp = 0;
    public static ConcurrentHashMap<String, ArrayList<Plot>> expiredPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> updatingPlots = new ConcurrentHashMap<>();
    public static int task;
    
    public static void updateExpired(final String world) {
        updatingPlots.put(world, true);
        long now = System.currentTimeMillis();
        if (now > timestamp) {
            timestamp = now + 86400000;
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Plot> plots = getOldPlots(world);
                    expiredPlots.put(world, plots);
                    updatingPlots.put(world, false);
                }
            });
        }
        else {
            updatingPlots.put(world, false);
        }
    }
    
    public static void runTask() {
        ExpireManager.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlotMain.getMain(), new Runnable() {
            @Override
            public void run() {
                for (String world : PlotMain.getPlotWorldsString()) {
                    if (!ExpireManager.updatingPlots.contains(world)) {
                        ExpireManager.updatingPlots.put(world, false);
                    }
                    Boolean updating = ExpireManager.updatingPlots.get(world);
                    if (updating) {
                        return;
                    }
                    ArrayList<Plot> plots = expiredPlots.get(world);
                    if (plots == null || plots.size() == 0) {
                        updateExpired(world);
                        return;
                    }
                    Plot plot = plots.get(0);
                    
                    if (plot.owner != null) {
                        if (UUIDHandler.uuidWrapper.getPlayer(plot.owner) != null) {
                            expiredPlots.get(world).remove(0);
                            return;
                        }
                    }
                    if (!isExpired(plot.owner)) {
                        expiredPlots.get(world).remove(0);
                        return;
                    }
                    final PlotDeleteEvent event = new PlotDeleteEvent(world, plot.id);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    
                    for (UUID helper : plot.helpers) {
                        Player player = UUIDHandler.uuidWrapper.getPlayer(helper);
                        if (player != null) {
                            PlayerFunctions.sendMessage(player, C.PLOT_REMOVED_HELPER, plot.id.toString());
                        }
                    }
                    
                    final World worldobj = Bukkit.getWorld(world);
                    final PlotManager manager = PlotMain.getPlotManager(world);
                    manager.clearPlot(worldobj, plot, false);
                    PlotHelper.removeSign(worldobj, plot);
                    DBFunc.delete(world, plot);
                    PlotMain.removePlot(world, plot.id, true);
                    expiredPlots.get(world).remove(0);
                    PlotMain.sendConsoleSenderMessage("&cDeleted expired plot: " + plot.id);
                    PlotMain.sendConsoleSenderMessage("&3 - World: "+plot.world);
                    if (plot.hasOwner()) {
                    	PlotMain.sendConsoleSenderMessage("&3 - Owner: "+UUIDHandler.getName(plot.owner));
                    }
                    else {
                    	PlotMain.sendConsoleSenderMessage("&3 - Owner: Unowned");
                    }
                    return;
                }
                
            }
        }, 2400, 2400);
    }
    
    public static boolean isExpired(UUID uuid) {
        OfflinePlayer op = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
        if (!op.hasPlayedBefore()) {
            return true;
        }
        long last = op.getLastPlayed();
        long compared = System.currentTimeMillis() - last;
        if (compared >= 86400000 * Settings.AUTO_CLEAR_DAYS) {
            return true;
        }
        return false;
    }
    
    public static ArrayList<Plot> getOldPlots(String world) {
        final Collection<Plot> plots = PlotMain.getPlots(world).values();
        final ArrayList<Plot> toRemove = new ArrayList<>();
        Set<UUID> remove = new HashSet<>();
        Set<UUID> keep = new HashSet<>();
        for (Plot plot : plots) {
            UUID uuid = plot.owner;
            if (uuid == null || remove.contains(uuid)) {
                toRemove.add(plot);
                continue;
            }
            if (keep.contains(uuid)) {
                continue;
            }
            Player player = UUIDHandler.uuidWrapper.getPlayer(uuid);
            if (player != null) {
                keep.add(uuid);
                continue;
            }
            OfflinePlayer op = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
            if (op==null || !op.hasPlayedBefore()) {
                continue;
            }
            long last = op.getLastPlayed();
            long compared = System.currentTimeMillis() - last;
            if (compared >= 86400000 * Settings.AUTO_CLEAR_DAYS) {
                toRemove.add(plot);
                remove.add(uuid);
            }
            keep.add(uuid);
        }
        return toRemove;
    }
    
}
