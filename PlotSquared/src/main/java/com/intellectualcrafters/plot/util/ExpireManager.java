package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.commands.Unlink;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;

public class ExpireManager {
    
    public static ConcurrentHashMap<String, HashMap<Plot, Long>> expiredPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> updatingPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> timestamp = new ConcurrentHashMap<>();
    public static int task;
    
    public static long getTimeStamp(final String world) {
        if (timestamp.containsKey(world)) {
            return timestamp.get(world);
        }
        else {
            timestamp.put(world, 0l);
            return 0;
        }
    }
    
    public static boolean updateExpired(final String world) {
        updatingPlots.put(world, true);
        long now = System.currentTimeMillis();
        if (now > getTimeStamp(world)) {
            timestamp.put(world, now + 86400000l);
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    HashMap<Plot, Long> plots = getOldPlots(world);
                    PlotMain.sendConsoleSenderMessage("&cFound " + plots.size() + " expired plots for " + world + "!");
                    expiredPlots.put(world, plots);
                    updatingPlots.put(world, false);
                }
            });
            return true;
        }
        else {
            updatingPlots.put(world, false);
            return false;
        }
    }
    
    public static void runTask() {
        ExpireManager.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlotMain.getMain(), new Runnable() {
            @Override
            public void run() {
                for (String world : PlotMain.getPlotWorldsString()) {
                    if (!ExpireManager.updatingPlots.containsKey(world)) {
                        ExpireManager.updatingPlots.put(world, false);
                    }
                    Boolean updating = ExpireManager.updatingPlots.get(world);
                    if (updating) {
                        return;
                    }
                    if (!expiredPlots.containsKey(world)) {
                        updateExpired(world);
                        return;
                    }
                    Set<Plot> plots = expiredPlots.get(world).keySet();
                    if (plots == null || plots.size() == 0) {
                        if (updateExpired(world)) {
                            return;
                        }
                        continue;
                    }
                    Plot plot = plots.iterator().next();
                    if (plot.owner != null) {
                        if (UUIDHandler.uuidWrapper.getPlayer(plot.owner) != null) {
                            expiredPlots.get(world).remove(plot);
                            return;
                        }
                    }
                    if (!isExpired(plot.owner)) {
                        expiredPlots.get(world).remove(plot);
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
                    if (plot.settings.isMerged()) {
                        Unlink.unlinkPlot(Bukkit.getWorld(world), plot);
                    }
                    PlotWorld plotworld = PlotMain.getWorldSettings(world);
                    manager.clearPlot(worldobj, plotworld, plot, false, null);
                    PlotHelper.removeSign(worldobj, plot);
                    DBFunc.delete(world, plot);
                    PlotMain.removePlot(world, plot.id, true);
                    expiredPlots.get(world).remove(plot);
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
    	String name = UUIDHandler.getName(uuid);
    	if (name != null) {
    		OfflinePlayer op = Bukkit.getOfflinePlayer(name);
    		if (op.hasPlayedBefore()) {
    			long last = op.getLastPlayed();
    	        long compared = System.currentTimeMillis() - last;
    	        if (compared >= 86400000l * Settings.AUTO_CLEAR_DAYS) {
    	            return true;
    	        } 
    		}
    	}
        return false;
    }
    
    public static HashMap<Plot, Long> getOldPlots(String world) {
        final Collection<Plot> plots = PlotMain.getPlots(world).values();
        final HashMap<Plot, Long> toRemove = new HashMap<>();
        HashMap<UUID, Long> remove = new HashMap<>();
        Set<UUID> keep = new HashSet<>();
        for (Plot plot : plots) {
            UUID uuid = plot.owner;
            if (uuid == null || remove.containsKey(uuid)) {
                Long stamp;
                if (uuid == null) {
                    stamp = 0l;
                }
                else {
                    stamp = remove.get(uuid);
                }
                toRemove.put(plot, stamp);
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
            if (compared >= 86400000l * Settings.AUTO_CLEAR_DAYS) {
                if (Settings.AUTO_CLEAR_CHECK_DISK) {
                    String worldname = Bukkit.getWorlds().get(0).getName();
                    String foldername;
                    String filename = null;
                    if (PlotMain.checkVersion(1, 7, 5)) {
                        foldername = "playerdata";
                        try {
                            filename = op.getUniqueId() +".dat";
                        }
                        catch (Throwable e) {
                            filename = uuid.toString() + ".dat";
                        }
                    }
                    else {
                        foldername = "players";
                        String playername = UUIDHandler.getName(uuid);
                        if (playername != null) {
                            filename = playername + ".dat";
                        }
                    }
                    if (filename != null) {
                        File playerFile = new File(worldname + File.separator + foldername + File.separator + filename);
                        if (!playerFile.exists()) {
                            PlotMain.sendConsoleSenderMessage("Could not find file: " + filename);
                        }
                        else {
                            try {
                                last = playerFile.lastModified();
                                compared = System.currentTimeMillis() - last;
                                if (compared < 86400000l * Settings.AUTO_CLEAR_DAYS) {
                                    keep.add(uuid);
                                    continue;
                                }
                            }
                            catch (Exception e) {
                                PlotMain.sendConsoleSenderMessage("Please disable disk checking in old plot auto clearing; Could not read file: " + filename);
                            }
                        }
                    }
                }
                toRemove.put(plot, last);
                remove.put(uuid, last);
                continue;
            }
            keep.add(uuid);
        }
        return toRemove;
    }
    
}
