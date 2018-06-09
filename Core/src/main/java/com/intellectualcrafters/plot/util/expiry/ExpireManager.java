package com.intellectualcrafters.plot.util.expiry;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ExpireManager {

    public static ExpireManager IMP;
    private final ConcurrentHashMap<UUID, Long> dates_cache;
    private volatile HashSet<Plot> plotsToDelete;
    private ArrayDeque<ExpiryTask> tasks;
    /**
     * 0 = stopped, 1 = stopping, 2 = running
     */
    private int running;

    public ExpireManager() {
        tasks = new ArrayDeque<>();
        dates_cache = new ConcurrentHashMap<>();
    }

    public void addTask(ExpiryTask task) {
        PS.debug("Adding new expiry task!");
        this.tasks.add(task);
    }

    public void handleJoin(PlotPlayer pp) {
        storeDate(pp.getUUID(), System.currentTimeMillis());
        if (plotsToDelete != null && !plotsToDelete.isEmpty()) {
            for (Plot plot : pp.getPlots()) {
                plotsToDelete.remove(plot);
            }
        }
        confirmExpiry(pp);
    }

    public void handleEntry(PlotPlayer pp, Plot plot) {
        if (plotsToDelete != null && !plotsToDelete.isEmpty() && pp.hasPermission("plots.admin.command.autoclear") && plotsToDelete.contains(plot)) {
            if (!isExpired(new ArrayDeque<>(tasks), plot).isEmpty()) {
                confirmExpiry(pp);
            } else {
                plotsToDelete.remove(plot);
                confirmExpiry(pp);
            }
        }
    }

    public long getTimestamp(UUID uuid) {
        Long value = this.dates_cache.get(uuid);
        return value == null ? 0 : value;
    }

    public void updateExpired(Plot plot) {
        if (plotsToDelete != null && !plotsToDelete.isEmpty() && plotsToDelete.contains(plot)) {
            if (isExpired(new ArrayDeque<>(tasks), plot).isEmpty()) {
                plotsToDelete.remove(plot);
            }
        }
    }

    public void confirmExpiry(final PlotPlayer pp) {
        if (pp.getMeta("ignoreExpireTask") != null) {
            return;
        }
        if (plotsToDelete != null && !plotsToDelete.isEmpty() && pp.hasPermission("plots.admin.command.autoclear")) {
            final int num = plotsToDelete.size();
            while (!plotsToDelete.isEmpty()) {
                Iterator<Plot> iter = plotsToDelete.iterator();
                final Plot current = iter.next();
                if (!isExpired(new ArrayDeque<>(tasks), current).isEmpty()) {
                    TaskManager.runTask(new Runnable() {
                        @Override
                        public void run() {
                            pp.setMeta("ignoreExpireTask", true);
                            pp.teleport(current.getCenter());
                            pp.deleteMeta("ignoreExpireTask");
                            PlotMessage msg = new PlotMessage()
                                    .text(num + " " + (num > 1 ? "plots are" : "plot is") + " expired: ").color("$1").text(current.toString()).color("$2").suggest("/plot list expired")
                                    .tooltip("/plot list expired")
                                    //.text("\n - ").color("$3").text("Delete all (/plot delete expired)").color("$2").command("/plot delete expired")
                                    .text("\n - ").color("$3").text("Delete this (/plot delete)").color("$2").suggest("/plot delete")
                                    .tooltip("/plot delete")
                                    .text("\n - ").color("$3").text("Remind later (/plot set keep 1d)").color("$2").suggest("/plot set keep 1d")
                                    .tooltip("/plot set keep 1d")
                                    .text("\n - ").color("$3").text("Keep this (/plot set keep true)").color("$2").suggest("/plot set keep true")
                                    .tooltip("/plot set keep true")
                                    .text("\n - ").color("$3").text("Don't show me this").color("$2").suggest("/plot toggle clear-confirmation")
                                    .tooltip("/plot toggle clear-confirmation");
                            msg.send(pp);
                        }
                    });
                    return;
                } else {
                    iter.remove();
                }
            }
            plotsToDelete.clear();
        }
    }


    public boolean cancelTask() {
        if (this.running != 2) {
            return false;
        }
        this.running = 1;
        return true;
    }

    public boolean runAutomatedTask() {
        return runTask(new RunnableVal3<Plot, Runnable, Boolean>() {
            @Override
            public void run(Plot plot, Runnable runnable, Boolean confirm) {
                if (confirm) {
                    if (plotsToDelete == null) {
                        plotsToDelete = new HashSet<>();
                    }
                    plotsToDelete.add(plot);
                    runnable.run();
                } else {
                    deleteWithMessage(plot, runnable);
                }
            }
        });
    }

    public Collection<ExpiryTask> isExpired(ArrayDeque<ExpiryTask> applicable, Plot plot) {
        // Filter out invalid worlds
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask et = applicable.poll();
            if (et.applies(plot.getArea())) {
                applicable.add(et);
            }
        }
        if (applicable.isEmpty()) {
            return new ArrayList<>();
        }
        long diff = getAge(plot);
        if (diff == 0) {
            return new ArrayList<>();
        }
        // Filter out non old plots
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask et = applicable.poll();
            if (et.applies(diff)) {
                applicable.add(et);
            }
        }
        if (applicable.isEmpty()) {
            return new ArrayList<>();
        }
        // Run applicable non confirming tasks
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask expiryTask = applicable.poll();
            if (!expiryTask.needsAnalysis() || plot.getArea().TYPE != 0) {
                if (!expiryTask.requiresConfirmation()) {
                    return Collections.singletonList(expiryTask);
                }
            }
            applicable.add(expiryTask);
        }
        // Run applicable confirming tasks
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask expiryTask = applicable.poll();
            if (!expiryTask.needsAnalysis() || plot.getArea().TYPE != 0) {
                return Collections.singletonList(expiryTask);
            }
            applicable.add(expiryTask);
        }
        return applicable;
    }

    public ArrayDeque<ExpiryTask> getTasks(PlotArea area) {
        ArrayDeque<ExpiryTask> queue = new ArrayDeque<>(tasks);
        Iterator<ExpiryTask> iter = queue.iterator();
        while (iter.hasNext()) {
            if (!iter.next().applies(area)) {
                iter.remove();
            }
        }
        return queue;
    }

    public void passesComplexity(PlotAnalysis analysis, Collection<ExpiryTask> applicable, RunnableVal<Boolean> success, Runnable failure) {
        if (analysis != null) {
            // Run non confirming tasks
            for (ExpiryTask et : applicable) {
                if (!et.requiresConfirmation() && et.applies(analysis)) {
                    success.run(false);
                    return;
                }
            }
            for (ExpiryTask et : applicable) {
                if (et.applies(analysis)) {
                    success.run(true);
                    return;
                }
            }
            failure.run();
        }
    }

    public boolean runTask(final RunnableVal3<Plot, Runnable, Boolean> expiredTask) {
        if (this.running != 0) {
            return false;
        }
        this.running = 2;
        final ConcurrentLinkedDeque<Plot> plots = new ConcurrentLinkedDeque<Plot>(PS.get().getPlots());
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final Runnable task = this;
                if (ExpireManager.this.running != 2) {
                    ExpireManager.this.running = 0;
                    return;
                }
                long start = System.currentTimeMillis();
                while (!plots.isEmpty()) {
                    if (ExpireManager.this.running != 2) {
                        ExpireManager.this.running = 0;
                        return;
                    }
                    Plot plot = plots.poll();
                    PlotArea area = plot.getArea();
                    final Plot newPlot = area.getPlot(plot.getId());
                    final ArrayDeque<ExpiryTask> applicable = new ArrayDeque<>(tasks);
                    final Collection<ExpiryTask> expired = isExpired(applicable, newPlot);
                    if (expired.isEmpty()) {
                        continue;
                    }
                    for (ExpiryTask expiryTask : expired) {
                        if (!expiryTask.needsAnalysis()) {
                            expiredTask.run(newPlot, new Runnable() {
                                @Override
                                public void run() {
                                    TaskManager.IMP.taskLaterAsync(task, 1);
                                }
                            }, expiryTask.requiresConfirmation());
                            return;
                        }
                    }
                    final RunnableVal<PlotAnalysis> handleAnalysis = new RunnableVal<PlotAnalysis>() {
                        @Override
                        public void run(final PlotAnalysis changed) {
                            passesComplexity(changed, expired, new RunnableVal<Boolean>() {
                                @Override
                                public void run(Boolean confirmation) {
                                    expiredTask.run(newPlot, new Runnable() {
                                        @Override
                                        public void run() {
                                            TaskManager.IMP.taskLaterAsync(task, 1);
                                        }
                                    }, confirmation);
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    FlagManager.addPlotFlag(newPlot, Flags.ANALYSIS, changed.asList());
                                    TaskManager.runTaskLaterAsync(task, 20);
                                }
                            });
                        }
                    };
                    final Runnable doAnalysis = new Runnable() {
                        @Override
                        public void run() {
                            HybridUtils.manager.analyzePlot(newPlot, handleAnalysis);
                        }
                    };

                    PlotAnalysis analysis = newPlot.getComplexity(null);
                    if (analysis != null) {
                        passesComplexity(analysis, expired, new RunnableVal<Boolean>() {
                            @Override
                            public void run(Boolean value) {
                                doAnalysis.run();
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                TaskManager.IMP.taskLaterAsync(task, 1);
                            }
                        });
                    } else {
                        doAnalysis.run();
                    }
                    return;
                }
                if (plots.isEmpty()) {
                    ExpireManager.this.running = 3;
                    TaskManager.runTaskLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ExpireManager.this.running == 3) {
                                ExpireManager.this.running = 2;
                                runTask(expiredTask);
                            }
                        }
                    }, 86400000);
                } else {
                    TaskManager.runTaskLaterAsync(task, 20 * 10);
                }
            }
        });
        return true;
    }

    public void storeDate(UUID uuid, long time) {
        this.dates_cache.put(uuid, time);
    }

    public HashSet<Plot> getPendingExpired() {
        return plotsToDelete == null ? new HashSet<Plot>() : plotsToDelete;
    }

    public void deleteWithMessage(Plot plot, Runnable whenDone) {
        if (plot.isMerged()) {
            plot.unlinkPlot(true, false);
        }
        for (UUID helper : plot.getTrusted()) {
            PlotPlayer player = UUIDHandler.getPlayer(helper);
            if (player != null) {
                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.toString());
            }
        }
        for (UUID helper : plot.getMembers()) {
            PlotPlayer player = UUIDHandler.getPlayer(helper);
            if (player != null) {
                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.toString());
            }
        }
        Set<Plot> plots = plot.getConnectedPlots();
        plot.deletePlot(whenDone);
        PlotAnalysis changed = plot.getComplexity(null);
        int changes = changed == null ? 0 : changed.changes_sd;
        int modified = changed == null ? 0 : changed.changes;
        PS.debug("$2[&5Expire&dManager$2] &cDeleted expired plot: " + plot + " User:" + plot.owner + " Delta:" + changes + "/" + modified + " Connected: " + StringMan.getString(plots));
        PS.debug("$4 - Area: " + plot.getArea());
        if (plot.hasOwner()) {
            PS.debug("$4 - Owner: " + UUIDHandler.getName(plot.owner));
        } else {
            PS.debug("$4 - Owner: Unowned");
        }
    }

    public long getAge(UUID uuid) {
        if (UUIDHandler.getPlayer(uuid) != null) {
            return 0;
        }
        String name = UUIDHandler.getName(uuid);
        if (name != null) {
            Long last = this.dates_cache.get(uuid);
            if (last == null) {
                OfflinePlotPlayer opp;
                if (Settings.UUID.NATIVE_UUID_PROVIDER) {
                    opp = UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);
                } else {
                    opp = UUIDHandler.getUUIDWrapper().getOfflinePlayer(name);
                }
                if (opp != null && (last = opp.getLastPlayed()) != 0) {
                    this.dates_cache.put(uuid, last);
                } else {
                    return 0;
                }
            }
            if (last == 0) {
                return 0;
            }
            return System.currentTimeMillis() - last;
        }
        return 0;
    }

    public long getAge(Plot plot) {
        if (!plot.hasOwner() || Objects.equals(DBFunc.everyone, plot.owner) || UUIDHandler.getPlayer(plot.owner) != null || plot.getRunning() > 0) {
            return 0;
        }
        Optional<?> keep = plot.getFlag(Flags.KEEP);
        if (keep.isPresent()) {
            Object value = keep.get();
            if (value instanceof Boolean) {
                if (Boolean.TRUE.equals(value)) {
                    return 0;
                }
            } else if (value instanceof Long) {
                if ((Long) value > System.currentTimeMillis()) {
                    return 0;
                }
            } else { // Invalid?
                return 0;
            }
        }
        long min = Long.MAX_VALUE;
        for (UUID owner : plot.getOwners()) {
            long age = getAge(owner);
            if (age < min) {
                min = age;
            }
        }
        return min;
    }
}
