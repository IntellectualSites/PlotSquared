/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.expiration;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.AnalysisFlag;
import com.plotsquared.core.plot.flag.implementations.KeepFlag;
import com.plotsquared.core.plot.message.PlotMessage;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ExpireManager {

    private final Logger logger = LoggerFactory.getLogger("P2/" + ExpireManager.class);

    public static ExpireManager IMP;
    private final ConcurrentHashMap<UUID, Long> dates_cache;
    private final ConcurrentHashMap<UUID, Long> account_age_cache;
    private final EventDispatcher eventDispatcher;
    private volatile HashSet<Plot> plotsToDelete;
    private ArrayDeque<ExpiryTask> tasks;

    /**
     * 0 = stopped, 1 = stopping, 2 = running
     */
    private int running;

    public ExpireManager(@Nonnull final EventDispatcher eventDispatcher) {
        this.tasks = new ArrayDeque<>();
        this.dates_cache = new ConcurrentHashMap<>();
        this.account_age_cache = new ConcurrentHashMap<>();
        this.eventDispatcher = eventDispatcher;
    }

    public void addTask(ExpiryTask task) {
        this.tasks.add(task);
    }

    public void handleJoin(PlotPlayer<?> pp) {
        storeDate(pp.getUUID(), System.currentTimeMillis());
        if (plotsToDelete != null && !plotsToDelete.isEmpty()) {
            for (Plot plot : pp.getPlots()) {
                plotsToDelete.remove(plot);
            }
        }
        confirmExpiry(pp);
    }

    public void handleEntry(PlotPlayer pp, Plot plot) {
        if (plotsToDelete != null && !plotsToDelete.isEmpty() && pp
            .hasPermission("plots.admin.command.autoclear") && plotsToDelete.contains(plot)) {
            if (!isExpired(new ArrayDeque<>(tasks), plot).isEmpty()) {
                confirmExpiry(pp);
            } else {
                plotsToDelete.remove(plot);
                confirmExpiry(pp);
            }
        }
    }

    /**
     * Gets the account last joined - first joined (or Long.MAX_VALUE)
     *
     * @param uuid
     * @return result
     */
    public long getAccountAge(UUID uuid) {
        Long value = this.account_age_cache.get(uuid);
        return value == null ? Long.MAX_VALUE : value;
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
        if (plotsToDelete != null && !plotsToDelete.isEmpty() && pp
            .hasPermission("plots.admin.command.autoclear")) {
            final int num = plotsToDelete.size();
            while (!plotsToDelete.isEmpty()) {
                Iterator<Plot> iter = plotsToDelete.iterator();
                final Plot current = iter.next();
                if (!isExpired(new ArrayDeque<>(tasks), current).isEmpty()) {
                    TaskManager.runTask(() -> {
                        pp.setMeta("ignoreExpireTask", true);
                        current.getCenter(pp::teleport);
                        pp.deleteMeta("ignoreExpireTask");
                        PlotMessage msg = new PlotMessage()
                            .text(num + " " + (num > 1 ? "plots are" : "plot is") + " expired: ")
                            .color("$1").text(current.toString()).color("$2")
                            .command("/plot list expired").tooltip("/plot list expired")
                            //.text("\n - ").color("$3").text("Delete all (/plot delete expired)").color("$2").command("/plot delete expired")
                            .text("\n - ").color("$3").text("Delete this (/plot delete)")
                            .color("$2").command("/plot delete").tooltip("/plot delete")
                            .text("\n - ").color("$3").text("Remind later (/plot flag set keep 1d)")
                            .color("$2").command("/plot flag set keep 1d").tooltip("/plot flag set keep 1d")
                            .text("\n - ").color("$3").text("Keep this (/plot flag set keep true)")
                            .color("$2").command("/plot flag set keep true")
                            .tooltip("/plot flag set keep true").text("\n - ").color("$3")
                            .text("Don't show me this").color("$2")
                            .command("/plot toggle clear-confirmation")
                            .tooltip("/plot toggle clear-confirmation");
                        msg.send(pp);
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
            @Override public void run(Plot plot, Runnable runnable, Boolean confirm) {
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

        if (MainUtil.isServerOwned(plot)) {
            return new ArrayList<>();
        }

        long diff = getAge(plot);
        if (diff == 0) {
            return new ArrayList<>();
        }
        // Filter out non old plots
        boolean shouldCheckAccountAge = false;
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask et = applicable.poll();
            if (et.applies(diff)) {
                applicable.add(et);
                shouldCheckAccountAge |= et.getSettings().SKIP_ACCOUNT_AGE_DAYS != -1;
            }
        }
        if (applicable.isEmpty()) {
            return new ArrayList<>();
        }
        // Check account age
        if (shouldCheckAccountAge) {
            long accountAge = getAge(plot);
            for (int i = 0; i < applicable.size(); i++) {
                ExpiryTask et = applicable.poll();
                if (et.appliesAccountAge(accountAge)) {
                    applicable.add(et);
                }
            }
            if (applicable.isEmpty()) {
                return new ArrayList<>();
            }
        }

        // Run applicable non confirming tasks
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask expiryTask = applicable.poll();
            if (!expiryTask.needsAnalysis() || plot.getArea().getType() != PlotAreaType.NORMAL) {
                if (!expiryTask.requiresConfirmation()) {
                    return Collections.singletonList(expiryTask);
                }
            }
            applicable.add(expiryTask);
        }
        // Run applicable confirming tasks
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask expiryTask = applicable.poll();
            if (!expiryTask.needsAnalysis() || plot.getArea().getType() != PlotAreaType.NORMAL) {
                return Collections.singletonList(expiryTask);
            }
            applicable.add(expiryTask);
        }
        return applicable;
    }

    public ArrayDeque<ExpiryTask> getTasks(PlotArea area) {
        ArrayDeque<ExpiryTask> queue = new ArrayDeque<>(tasks);
        queue.removeIf(expiryTask -> !expiryTask.applies(area));
        return queue;
    }

    public void passesComplexity(PlotAnalysis analysis, Collection<ExpiryTask> applicable,
        RunnableVal<Boolean> success, Runnable failure) {
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
        final ConcurrentLinkedDeque<Plot> plots =
            new ConcurrentLinkedDeque<>(PlotQuery.newQuery().allPlots().asList());
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
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
                            expiredTask.run(newPlot, () -> TaskManager.getPlatformImplementation()
                                    .taskLaterAsync(task, TaskTime.ticks(1L)),
                                expiryTask.requiresConfirmation());
                            return;
                        }
                    }
                    final RunnableVal<PlotAnalysis> handleAnalysis =
                        new RunnableVal<PlotAnalysis>() {
                            @Override public void run(final PlotAnalysis changed) {
                                passesComplexity(changed, expired, new RunnableVal<Boolean>() {
                                    @Override public void run(Boolean confirmation) {
                                        expiredTask.run(newPlot,
                                            () -> TaskManager.getPlatformImplementation().taskLaterAsync(task, TaskTime.ticks(1L)),
                                            confirmation);
                                    }
                                }, () -> {
                                    PlotFlag<?, ?> plotFlag = GlobalFlagContainer.getInstance()
                                        .getFlag(AnalysisFlag.class)
                                        .createFlagInstance(changed.asList());
                                    PlotFlagAddEvent event =
                                        new PlotFlagAddEvent(plotFlag, newPlot);
                                    if (event.getEventResult() == Result.DENY) {
                                        return;
                                    }
                                    newPlot.setFlag(event.getFlag());
                                    TaskManager.runTaskLaterAsync(task, TaskTime.seconds(1L));
                                });
                            }
                        };
                    final Runnable doAnalysis =
                        () -> PlotSquared.platform().getHybridUtils().analyzePlot(newPlot, handleAnalysis);

                    PlotAnalysis analysis = newPlot.getComplexity(null);
                    if (analysis != null) {
                        passesComplexity(analysis, expired, new RunnableVal<Boolean>() {
                            @Override public void run(Boolean value) {
                                doAnalysis.run();
                            }
                        }, () -> TaskManager.getPlatformImplementation().taskLaterAsync(task, TaskTime.ticks(1L)));
                    } else {
                        doAnalysis.run();
                    }
                    return;
                }
                if (plots.isEmpty()) {
                    ExpireManager.this.running = 3;
                    TaskManager.runTaskLater(() -> {
                        if (ExpireManager.this.running == 3) {
                            ExpireManager.this.running = 2;
                            runTask(expiredTask);
                        }
                    }, TaskTime.ticks(86400000));
                } else {
                    TaskManager.runTaskLaterAsync(task, TaskTime.seconds(10L));
                }
            }
        });
        return true;
    }

    public void storeDate(UUID uuid, long time) {
        Long existing = this.dates_cache.put(uuid, time);
        if (existing != null) {
            long diff = time - existing;
            if (diff > 0) {
                Long account_age = this.account_age_cache.get(uuid);
                if (account_age != null) {
                    this.account_age_cache.put(uuid, account_age + diff);
                }
            }
        }
    }

    public void storeAccountAge(UUID uuid, long time) {
        this.account_age_cache.put(uuid, time);
    }

    public HashSet<Plot> getPendingExpired() {
        return plotsToDelete == null ? new HashSet<>() : plotsToDelete;
    }

    public void deleteWithMessage(Plot plot, Runnable whenDone) {
        if (plot.isMerged()) {
            PlotUnlinkEvent event = this.eventDispatcher
                .callUnlink(plot.getArea(), plot, true, false,
                    PlotUnlinkEvent.REASON.EXPIRE_DELETE);
            if (event.getEventResult() != Result.DENY) {
                plot.unlinkPlot(event.isCreateRoad(), event.isCreateSign());
            }
        }
        for (UUID helper : plot.getTrusted()) {
            PlotPlayer player = PlotSquared.platform().getPlayerManager().getPlayerIfExists(helper);
            if (player != null) {
                MainUtil.sendMessage(player, Captions.PLOT_REMOVED_USER, plot.toString());
            }
        }
        for (UUID helper : plot.getMembers()) {
            PlotPlayer player = PlotSquared.platform().getPlayerManager().getPlayerIfExists(helper);
            if (player != null) {
                MainUtil.sendMessage(player, Captions.PLOT_REMOVED_USER, plot.toString());
            }
        }
        plot.deletePlot(whenDone);
    }

    public long getAge(UUID uuid) {
        if (PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid) != null) {
            return 0;
        }
        Long last = this.dates_cache.get(uuid);
        if (last == null) {
            OfflinePlotPlayer opp = PlotSquared.platform().getPlayerManager().getOfflinePlayer(uuid);
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

    public long getAge(Plot plot) {
        if (!plot.hasOwner() || Objects.equals(DBFunc.EVERYONE, plot.getOwner())
            || PlotSquared.platform().getPlayerManager().getPlayerIfExists(plot.getOwner()) != null || plot.getRunning() > 0) {
            return 0;
        }

        final Object value = plot.getFlag(KeepFlag.class);
        if (!value.equals(false)) {
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
