package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.ClassicPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class ExpireManager {
    public static ConcurrentHashMap<String, List<Plot>> expiredPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> updatingPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> timestamp = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, Long> dates = new ConcurrentHashMap<>();
    public static int task;

    public static long getTimeStamp(final String world) {
        if (timestamp.containsKey(world)) {
            return timestamp.get(world);
        } else {
            timestamp.put(world, 0l);
            return 0;
        }
    }

    public static boolean updateExpired(final String world) {
        updatingPlots.put(world, true);
        final long now = System.currentTimeMillis();
        if (now > getTimeStamp(world)) {
            timestamp.put(world, now + 86400000l);
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        final List<Plot> plots = getOldPlots(world);
                        PlotSquared.log("&cFound " + plots.size() + " expired plots for " + world + "!");
                        expiredPlots.put(world, plots);
                        updatingPlots.put(world, false);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        } else {
            updatingPlots.put(world, false);
            return false;
        }
    }

    public static void runTask() {
        ExpireManager.task = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                for (final String world : PlotSquared.getPlotWorldsString()) {
                    if (!ExpireManager.updatingPlots.containsKey(world)) {
                        ExpireManager.updatingPlots.put(world, false);
                    }
                    final Boolean updating = ExpireManager.updatingPlots.get(world);
                    if (updating) {
                        return;
                    }
                    if (!expiredPlots.containsKey(world)) {
                        updateExpired(world);
                        return;
                    }
                    final List<Plot> plots = expiredPlots.get(world);
                    if ((plots == null) || (plots.size() == 0)) {
                        if (updateExpired(world)) {
                            return;
                        }
                        continue;
                    }
                    final Plot plot = plots.iterator().next();
                    if (!isExpired(plot)) {
                        expiredPlots.get(world).remove(plot);
                        return;
                    }
                    for (final UUID helper : plot.trusted) {
                        final PlotPlayer player = UUIDHandler.getPlayer(helper);
                        if (player != null) {
                            MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.id.toString());
                        }
                    }
                    for (final UUID helper : plot.members) {
                        final PlotPlayer player = UUIDHandler.getPlayer(helper);
                        if (player != null) {
                            MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.id.toString());
                        }
                    }
                    final PlotManager manager = PlotSquared.getPlotManager(world);
                    if (manager == null) {
                        PlotSquared.log("&cThis is a friendly reminder to create or delete " + world +" as it is currently setup incorrectly");
                        expiredPlots.get(world).remove(plot);
                        return;
                    }
                    if (plot.settings.isMerged()) {
                        MainUtil.unlinkPlot(plot);
                    }
                    final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
                    RunnableVal run = new RunnableVal() {
                        @Override
                        public void run() {
                            int changed = (int) this.value;
                            if (changed >= Settings.MIN_BLOCKS_CHANGED) {
                                PlotSquared.log("&aKeep flag added to: " + plot.id + (changed != -1 ? " (changed " + value + ")" : ""));
                                FlagManager.addPlotFlag(plot, new Flag(FlagManager.getFlag("keep"), true));
                                expiredPlots.get(world).remove(plot);
                                return;
                            }
                            manager.clearPlot(plotworld, plot, false, null);
                            MainUtil.removeSign(plot);
                            DBFunc.delete(world, plot);
                            PlotSquared.removePlot(world, plot.id, false);
                            expiredPlots.get(world).remove(plot);
                            PlotSquared.log("&cDeleted expired plot: " + plot.id + (changed != -1 ? " (changed " + value + ")" : ""));
                            PlotSquared.log("&3 - World: " + plot.world);
                            if (plot.hasOwner()) {
                                PlotSquared.log("&3 - Owner: " + UUIDHandler.getName(plot.owner));
                            } else {
                                PlotSquared.log("&3 - Owner: Unowned");
                            }
                        }
                    };
                    if (Settings.MIN_BLOCKS_CHANGED > 0 && manager instanceof ClassicPlotManager) {
                        HybridUtils.manager.checkModified(plot, run);
                    }
                    else {
                        run.value = -1;
                        run.run();
                    }
                    
                    
                    return;
                }
            }
        }, Settings.CLEAR_INTERVAL * 20);
    }

    public static boolean isExpired(final UUID uuid) {
        if (UUIDHandler.getPlayer(uuid) != null) {
            return false;
        }
        final String name = UUIDHandler.getName(uuid);
        if (name != null) {
            long last;
            if (dates.contains(uuid)) {
                last = dates.get(uuid);
            }
            else {
                final OfflinePlayer op = Bukkit.getOfflinePlayer(name);
                if (op.hasPlayedBefore()) {
                    last = op.getLastPlayed();
                    dates.put(uuid, last);
                }
                else {
                    return false;
                }
            }
            if (last == 0) {
                return false;
            }
            final long compared = System.currentTimeMillis() - last;
            if (compared >= (86400000l * Settings.AUTO_CLEAR_DAYS)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isExpired(Plot plot) {
        for (UUID owner : PlotHandler.getOwners(plot)) {
            if (!isExpired(owner)) {
                return false;
            }
        }
        return true;
    }

    public static List<Plot> getOldPlots(final String world) {
        final Collection<Plot> plots = PlotSquared.getPlots(world).values();
        final List<Plot> toRemove = new ArrayList<>();
        Iterator<Plot> iter = plots.iterator();
        while (iter.hasNext()) {
            Plot plot = iter.next();
            final Flag keepFlag = FlagManager.getPlotFlag(plot, "keep");
            if (keepFlag != null && (Boolean) keepFlag.getValue()) {
                continue;
            }
            final UUID uuid = plot.owner;
            if (uuid == null) {
                toRemove.add(plot);
                continue;
            }
            final PlotPlayer player = UUIDHandler.getPlayer(uuid);
            if (player != null) {
                continue;
            }
            if (isExpired(plot)) {
                if (Settings.AUTO_CLEAR_CHECK_DISK) {
                    final String worldname = Bukkit.getWorlds().get(0).getName();
                    String foldername;
                    String filename = null;
                    if (BukkitMain.checkVersion(1, 7, 5)) {
                        foldername = "playerdata";
                        try {
                            final OfflinePlotPlayer op = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
                            filename = op.getUUID() + ".dat";
                        } catch (final Throwable e) {
                            filename = uuid.toString() + ".dat";
                        }
                    } else {
                        foldername = "players";
                        final String playername = UUIDHandler.getName(uuid);
                        if (playername != null) {
                            filename = playername + ".dat";
                        }
                    }
                    if (filename != null) {
                        final File playerFile = new File(worldname + File.separator + foldername + File.separator + filename);
                        if (!playerFile.exists()) {
                            PlotSquared.log("Could not find file: " + filename);
                        } else {
                            try {
                                long last = playerFile.lastModified();
                                long compared = System.currentTimeMillis() - last;
                                if (compared < (86400000l * Settings.AUTO_CLEAR_DAYS)) {
                                    continue;
                                }
                            } catch (final Exception e) {
                                PlotSquared.log("Please disable disk checking in old plot auto clearing; Could not read file: " + filename);
                            }
                        }
                    }
                }
                toRemove.add(plot);
            }
        }
        return toRemove;
    }
}
