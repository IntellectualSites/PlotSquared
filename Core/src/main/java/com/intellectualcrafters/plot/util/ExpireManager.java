package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.RunnableVal2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ExpireManager {

    public static ExpireManager IMP;

    private static HashSet<Plot> plotsToDelete;
    private final ConcurrentHashMap<UUID, Long> dates_cache = new ConcurrentHashMap<>();
    /**
     * 0 = stopped, 1 = stopping, 2 = running
     */
    private int running;

    public void handleJoin(PlotPlayer pp) {
        storeDate(pp.getUUID(), System.currentTimeMillis());
        confirmExpiry(pp);
    }

    public void handleEntry(PlotPlayer pp, Plot plot) {
        if (Settings.AUTO_CLEAR_CONFIRMATION && plotsToDelete != null && !plotsToDelete.isEmpty() && pp.hasPermission("plots.admin.command.autoclear") && plotsToDelete.contains(plot) && !isExpired(plot)) {
            plotsToDelete.remove(plot);
            confirmExpiry(pp);
        }
    }

    public void confirmExpiry(final PlotPlayer pp) {
        if (Settings.AUTO_CLEAR_CONFIRMATION && plotsToDelete != null && !plotsToDelete.isEmpty() && pp.hasPermission("plots.admin.command.autoclear")) {
            final int num = plotsToDelete.size();
            Iterator<Plot> iter = plotsToDelete.iterator();
            while (iter.hasNext()) {
                final Plot current = iter.next();
                if (isExpired(current)) {
                    TaskManager.runTask(new Runnable() {
                        @Override
                        public void run() {
                            pp.teleport(current.getCenter());
                            PlotMessage msg = new PlotMessage()
                                    .text(num + " " + (num > 1 ? "plots are" : "plot is") + " expired:").color("$1").command("/plot list expired").tooltip("/plot list expired")
                                    //.text("\n - ").color("$3").text("Delete all (/plot delete expired)").color("$2").command("/plot delete expired")
                                    .text("\n - ").color("$3").text("Delete this (/plot delete)").color("$2").command("/plot delete").tooltip("/plot delete")
                                    .text("\n - ").color("$3").text("Remind later (/plot set keep 1d)").color("$2").command("/plot set keep 1d").tooltip("/plot set keep 1d")
                                    .text("\n - ").color("$3").text("Keep this (/plot set keep true)").color("$2").command("/plot set keep true").tooltip("/plot set keep true");
                            msg.send(pp);
                        }
                    });
                    return;
                }
            }
            plotsToDelete.clear();
            return;
        }
    }


    public boolean cancelTask() {
        if (running != 2) {
            return false;
        }
        running = 1;
        return true;
    }

    public boolean runConfirmedTask() {
        if (plotsToDelete == null) {
            plotsToDelete = new HashSet<>();
        }
        return runTask(new RunnableVal2<Plot, Runnable>() {
            @Override
            public void run(Plot plot, Runnable runnable) {
                plotsToDelete.add(plot);
                runnable.run();
            }
        });
    }

    public boolean runAutomatedTask() {
        return runTask(new RunnableVal2<Plot, Runnable>() {
            @Override
            public void run(Plot plot, Runnable runnable) {
                deleteWithMessage(plot, runnable);
            }
        });
    }
    
    public boolean runTask(final RunnableVal2<Plot, Runnable> expiredTask) {
        if (running != 0) {
            return false;
        }
        running = 2;
        final Set<Plot> plots = PS.get().getPlots();
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                if (running != 2) {
                    running = 0;
                    return;
                }
                final Runnable task = this;
                long start = System.currentTimeMillis();
                Iterator<Plot> iter = plots.iterator();
                while (iter.hasNext() && System.currentTimeMillis() - start < 2) {
                    if (running != 2) {
                        running = 0;
                        return;
                    }
                    final Plot plot = iter.next();
                    iter.remove();
                    if (!isExpired(plot)) {
                        continue;
                    }
                    PlotArea area = plot.getArea();
                    if ((Settings.CLEAR_THRESHOLD != -1) && (area.TYPE == 0)) {
                        final PlotAnalysis analysis = plot.getComplexity();
                        if (analysis != null) {
                            if (analysis.getComplexity() > Settings.CLEAR_THRESHOLD) {
                                PS.debug("$2[&5Expire&dManager$2] &bSkipping modified: " + plot);
                                continue;
                            }
                        }
                        HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                            @Override
                            public void run(PlotAnalysis changed) {
                                if ((changed.changes != 0) && (changed.getComplexity() > Settings.CLEAR_THRESHOLD)) {
                                    PS.debug("$2[&5Expire&dManager$2] &bIgnoring modified plot: " + plot + " : " + changed.getComplexity() + " - " + changed.changes);
                                    FlagManager.addPlotFlag(plot, new Flag(FlagManager.getFlag("analysis"), changed.asList()));
                                    TaskManager.runTaskLaterAsync(task, Settings.CLEAR_INTERVAL * 20);
                                } else {
                                    expiredTask.run(plot, new Runnable() {
                                        @Override
                                        public void run() {
                                            TaskManager.runTaskLater(task, Settings.CLEAR_INTERVAL * 20);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        expiredTask.run(plot, this);
                    }
                    return;
                }
                if (plots.isEmpty()) {
                    running = 3;
                    TaskManager.runTaskLater(new Runnable() {
                        @Override
                        public void run() {
                            if (running == 3) {
                                running = 2;
                                runTask(expiredTask);
                            }
                        }
                    }, 86400000);
                } else {
                    TaskManager.runTaskLaterAsync(task, Settings.CLEAR_INTERVAL * 20);
                }
            }
        });
        return true;
    }

    public void storeDate(UUID uuid, long time) {
        dates_cache.put(uuid, time);
    }

    public HashSet<Plot> getPendingExpired() {
        return plotsToDelete == null ? new HashSet<Plot>() : plotsToDelete;
    }

    public void deleteWithMessage(Plot plot, Runnable whenDone) {
        if (plot.isMerged()) {
            plot.unlinkPlot(true, false);
        }
        for (final UUID helper : plot.getTrusted()) {
            final PlotPlayer player = UUIDHandler.getPlayer(helper);
            if (player != null) {
                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.toString());
            }
        }
        for (final UUID helper : plot.getMembers()) {
            final PlotPlayer player = UUIDHandler.getPlayer(helper);
            if (player != null) {
                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.toString());
            }
        }
        plot.deletePlot(whenDone);
        final PlotAnalysis changed = plot.getComplexity();
        final int complexity = changed == null ? 0 : changed.getComplexity();
        final int modified = changed == null ? 0 : changed.changes;
        PS.debug("$2[&5Expire&dManager$2] &cDeleted expired plot: " + plot + " : " + complexity + " - " + modified);
        PS.debug("$4 - Area: " + plot.getArea());
        if (plot.hasOwner()) {
            PS.debug("$4 - Owner: " + UUIDHandler.getName(plot.owner));
        } else {
            PS.debug("$4 - Owner: Unowned");
        }
    }
    
    public boolean isExpired(final UUID uuid) {
        if (UUIDHandler.getPlayer(uuid) != null) {
            return false;
        }
        final String name = UUIDHandler.getName(uuid);
        if (name != null) {
            Long last = dates_cache.get(uuid);
            if (last == null) {
                OfflinePlotPlayer opp;
                if (Settings.TWIN_MODE_UUID) {
                    opp = UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);
                } else {
                    opp = UUIDHandler.getUUIDWrapper().getOfflinePlayer(name);
                }
                if ((last = opp.getLastPlayed()) != 0) {
                    dates_cache.put(uuid, last);
                } else {
                    return false;
                }
            }
            if (last == 0) {
                return false;
            }
            final long compared = System.currentTimeMillis() - last;
            if (compared >= (TimeUnit.DAYS.toMillis(Settings.AUTO_CLEAR_DAYS))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isExpired(final Plot plot) {
        if (!plot.hasOwner() || DBFunc.everyone.equals(plot.owner) || UUIDHandler.getPlayer(plot.owner) != null || plot.getRunning() > 0) {
            return false;
        }
        Flag keep = plot.getFlag("keep");
        if (keep != null) {
            Object value = keep.getValue();
            if (value instanceof Boolean) {
                if (Boolean.TRUE.equals(value)) {
                    return false;
                }
            } else if (value instanceof Long) {
                if (((Long) value) > System.currentTimeMillis()) {
                    return false;
                }
            } else { // Invalid?
                return false;
            }
        }
        for (final UUID owner : plot.getOwners()) {
            if (!isExpired(owner)) {
                return false;
            }
        }
        return true;
    }
}
