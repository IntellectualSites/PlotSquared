package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RunnableVal;

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
                        PS.debug("$2[&5Expire&dManager$2] $4Found " + plots.size() + " expired plots for " + world + "!");
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
                try {
                    for (final String world : PS.get().getPlotWorldsString()) {
                        if (!ExpireManager.updatingPlots.containsKey(world)) {
                            ExpireManager.updatingPlots.put(world, false);
                        }
                        final Boolean updating = ExpireManager.updatingPlots.get(world);
                        if (updating) {
                            PS.debug("$2[&5Expire&dManager$2] $4Waiting on fetch...");
                            return;
                        }
                        if (!expiredPlots.containsKey(world)) {
                            PS.debug("$2[&5Expire&dManager$2] $4Updating expired plots for: " + world);
                            updateExpired(world);
                            return;
                        }
                        final List<Plot> plots = expiredPlots.get(world);
                        if ((plots == null) || (plots.size() == 0)) {
                            if (updateExpired(world)) {
                                PS.debug("$2[&5Expire&dManager$2] $4Re-evaluating expired plots for: " + world);
                                return;
                            }
                            continue;
                        }
                        final Plot plot = plots.iterator().next();
                        if (!isExpired(plot)) {
                            expiredPlots.get(world).remove(plot);
                            PS.debug("$2[&5Expire&dManager$2] &bSkipping no longer expired: " + plot);
                            return;
                        }
                        for (final UUID helper : plot.getTrusted()) {
                            final PlotPlayer player = UUIDHandler.getPlayer(helper);
                            if (player != null) {
                                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.id.toString());
                            }
                        }
                        for (final UUID helper : plot.getMembers()) {
                            final PlotPlayer player = UUIDHandler.getPlayer(helper);
                            if (player != null) {
                                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.id.toString());
                            }
                        }
                        final PlotManager manager = PS.get().getPlotManager(world);
                        if (manager == null) {
                            PS.debug("$2[&5Expire&dManager$2] &cThis is a friendly reminder to create or delete " + world +" as it is currently setup incorrectly");
                            expiredPlots.get(world).remove(plot);
                            return;
                        }
                        final PlotWorld plotworld = PS.get().getPlotWorld(world);
                        RunnableVal<PlotAnalysis> run = new RunnableVal<PlotAnalysis>() {
                            @Override
                            public void run() {
                                PlotAnalysis changed = this.value;
                                if (Settings.CLEAR_THRESHOLD != -1 && plotworld.TYPE == 0 && changed != null) {
                                    if (changed.getComplexity() > Settings.CLEAR_THRESHOLD) {
                                        PS.debug("$2[&5Expire&dManager$2] &bIgnoring modified plot: " + plot + " : " + changed.getComplexity() + " - " + changed.changes);
                                        expiredPlots.get(world).remove(plot);
                                        FlagManager.addPlotFlag(plot, new Flag(FlagManager.getFlag("analysis"), changed.asList()));
                                        return;
                                    }
                                }
                                if (plot.isMerged()) {
                                    MainUtil.unlinkPlot(plot);
                                }
                                plot.deletePlot(null);
                                expiredPlots.get(world).remove(plot);
                                int complexity = changed == null ? 0 : changed.getComplexity();
                                int modified = changed == null ? 0 : changed.changes;
                                PS.debug("$2[&5Expire&dManager$2] &cDeleted expired plot: " + plot + " : " + complexity + " - " + modified);
                                PS.debug("$4 - World: " + plot.world);
                                if (plot.hasOwner()) {
                                    PS.debug("$4 - Owner: " + UUIDHandler.getName(plot.owner));
                                } else {
                                    PS.debug("$4 - Owner: Unowned");
                                }
                            }
                        };
                        if (MainUtil.runners.containsKey(plot)) {
                            PS.debug("$2[&5Expire&dManager$2] &bSkipping plot in use: " + plot);
                            expiredPlots.get(world).remove(plot);
                            this.run();
                            return;
                        }
                        if (Settings.CLEAR_THRESHOLD != -1 && plotworld.TYPE == 0) {
                            PlotAnalysis analysis = plot.getComplexity();
                            if (analysis != null) {
                                if (analysis.getComplexity() > Settings.CLEAR_THRESHOLD) {
                                    PS.debug("$2[&5Expire&dManager$2] &bSkipping modified: " + plot);
                                    expiredPlots.get(world).remove(plot);
                                    this.run();
                                    return;
                                }
                            }
                            HybridUtils.manager.analyzePlot(plot, run);
                        }
                        else {
                            run.value = null;
                            run.run();
                        }
                        return;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
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
                OfflinePlayer op;
                if (Settings.TWIN_MODE_UUID) {
                    op = Bukkit.getOfflinePlayer(uuid);
                }
                else {
                    op = Bukkit.getOfflinePlayer(name);
                }
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
        final ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlotsInWorld(world));
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
                toRemove.add(plot);
            }
        }
        return toRemove;
    }
}
