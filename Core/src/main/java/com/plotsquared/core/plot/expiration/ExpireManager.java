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
package com.plotsquared.core.plot.expiration;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.AnalysisFlag;
import com.plotsquared.core.plot.flag.implementations.KeepFlag;
import com.plotsquared.core.plot.flag.implementations.ServerPlotFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

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

    private final ConcurrentHashMap<UUID, Long> dates_cache;
    private final ConcurrentHashMap<UUID, Long> account_age_cache;
    private final EventDispatcher eventDispatcher;
    private final ArrayDeque<ExpiryTask> tasks;
    private volatile HashSet<Plot> plotsToDelete;
    /**
     * 0 = stopped, 1 = stopping, 2 = running
     */
    private int running;

    @Inject
    public ExpireManager(final @NonNull EventDispatcher eventDispatcher) {
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

    public void handleEntry(PlotPlayer<?> pp, Plot plot) {
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
     * @param uuid player uuid
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

    public void confirmExpiry(final PlotPlayer<?> pp) {
        TaskManager.runTask(() -> {
            try (final MetaDataAccess<Boolean> metaDataAccess = pp.accessTemporaryMetaData(
                    PlayerMetaDataKeys.TEMPORARY_IGNORE_EXPIRE_TASK)) {
                if (metaDataAccess.isPresent()) {
                    return;
                }
                if (plotsToDelete != null && !plotsToDelete.isEmpty() && pp.hasPermission("plots.admin.command.autoclear")) {
                    final int num = plotsToDelete.size();
                    while (!plotsToDelete.isEmpty()) {
                        Iterator<Plot> iter = plotsToDelete.iterator();
                        final Plot current = iter.next();
                        if (!isExpired(new ArrayDeque<>(tasks), current).isEmpty()) {
                            metaDataAccess.set(true);
                            current.getCenter(pp::teleport);
                            metaDataAccess.remove();
                            Caption msg = TranslatableCaption.of("expiry.expired_options_clicky");
                            TagResolver resolver = TagResolver.builder()
                                    .tag("num", Tag.inserting(Component.text(num)))
                                    .tag("are_or_is", Tag.inserting(Component.text(num > 1 ? "plots are" : "plot is")))
                                    .tag("list_cmd", Tag.preProcessParsed("/plot list expired"))
                                    .tag("plot", Tag.inserting(Component.text(current.toString())))
                                    .tag("cmd_del", Tag.preProcessParsed("/plot delete"))
                                    .tag("cmd_keep_1d", Tag.preProcessParsed("/plot flag set keep 1d"))
                                    .tag("cmd_keep", Tag.preProcessParsed("/plot flag set keep true"))
                                    .tag("cmd_no_show_expir", Tag.preProcessParsed("/plot toggle clear-confirmation"))
                                    .build();
                            pp.sendMessage(msg, resolver);
                            return;
                        } else {
                            iter.remove();
                        }
                    }
                    plotsToDelete.clear();
                }
            }
        });
    }


    public boolean cancelTask() {
        if (this.running != 2) {
            return false;
        }
        this.running = 1;
        return true;
    }

    public boolean runAutomatedTask() {
        return runTask(new RunnableVal3<>() {
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

        // Don't delete server plots
        if (plot.getFlag(ServerPlotFlag.class)) {
            return new ArrayList<>();
        }

        // Filter out non old plots
        boolean shouldCheckAccountAge = false;
        for (int i = 0; i < applicable.size(); i++) {
            ExpiryTask et = applicable.poll();
            if (et.applies(getAge(plot, et.shouldDeleteForUnknownOwner()))) {
                applicable.add(et);
                shouldCheckAccountAge |= et.getSettings().SKIP_ACCOUNT_AGE_DAYS != -1;
            }
        }
        if (applicable.isEmpty()) {
            return new ArrayList<>();
        }
        // Check account age
        if (shouldCheckAccountAge) {
            for (int i = 0; i < applicable.size(); i++) {
                ExpiryTask et = applicable.poll();
                long accountAge = getAge(plot, et.shouldDeleteForUnknownOwner());
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

    public void passesComplexity(
            PlotAnalysis analysis, Collection<ExpiryTask> applicable,
            RunnableVal<Boolean> success, Runnable failure
    ) {
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
        TaskManager.runTaskAsync(new Runnable() {
            private ConcurrentLinkedDeque<Plot> plots = null;

            @Override
            public void run() {
                final Runnable task = this;
                if (ExpireManager.this.running != 2) {
                    ExpireManager.this.running = 0;
                    return;
                }
                if (plots == null) {
                    plots = new ConcurrentLinkedDeque<>(PlotQuery.newQuery().allPlots().asList());
                }
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
                                    expiryTask.requiresConfirmation()
                            );
                            return;
                        }
                    }
                    final RunnableVal<PlotAnalysis> handleAnalysis =
                            new RunnableVal<>() {
                                @Override
                                public void run(final PlotAnalysis changed) {
                                    passesComplexity(changed, expired, new RunnableVal<>() {
                                        @Override
                                        public void run(Boolean confirmation) {
                                            expiredTask.run(
                                                    newPlot,
                                                    () -> TaskManager
                                                            .getPlatformImplementation()
                                                            .taskLaterAsync(task, TaskTime.ticks(1L)),
                                                    confirmation
                                            );
                                        }
                                    }, () -> {
                                        PlotFlag<?, ?> plotFlag = GlobalFlagContainer.getInstance()
                                                .getFlag(AnalysisFlag.class)
                                                .createFlagInstance(changed.asList());
                                        PlotFlagAddEvent event =
                                                eventDispatcher.callFlagAdd(plotFlag, plot);
                                        if (event.getEventResult() == Result.DENY) {
                                            return;
                                        }
                                        newPlot.setFlag(event.getFlag());
                                        TaskManager.runTaskLaterAsync(task, TaskTime.seconds(1L));
                                    });
                                }
                            };
                    final Runnable doAnalysis =
                            () -> PlotSquared.platform().hybridUtils().analyzePlot(newPlot, handleAnalysis);

                    PlotAnalysis analysis = newPlot.getComplexity(null);
                    if (analysis != null) {
                        passesComplexity(analysis, expired, new RunnableVal<>() {
                            @Override
                            public void run(Boolean value) {
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
                    }, TaskTime.ticks(86400000L));
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

    public HashSet<Plot> getPendingExpired() {
        return plotsToDelete == null ? new HashSet<>() : plotsToDelete;
    }

    public void deleteWithMessage(Plot plot, Runnable whenDone) {
        if (plot.isMerged()) {
            PlotUnlinkEvent event = this.eventDispatcher
                    .callUnlink(plot.getArea(), plot, true, false,
                            PlotUnlinkEvent.REASON.EXPIRE_DELETE
                    );
            if (event.getEventResult() != Result.DENY && plot.getPlotModificationManager().unlinkPlot(
                    event.isCreateRoad(),
                    event.isCreateSign()
            )) {
                this.eventDispatcher.callPostUnlink(plot, PlotUnlinkEvent.REASON.EXPIRE_DELETE);
            }
        }
        for (UUID helper : plot.getTrusted()) {
            PlotPlayer<?> player = PlotSquared.platform().playerManager().getPlayerIfExists(helper);
            if (player != null) {
                player.sendMessage(
                        TranslatableCaption.of("trusted.plot_removed_user"),
                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.toString())))
                );
            }
        }
        for (UUID helper : plot.getMembers()) {
            PlotPlayer<?> player = PlotSquared.platform().playerManager().getPlayerIfExists(helper);
            if (player != null) {
                player.sendMessage(
                        TranslatableCaption.of("trusted.plot_removed_user"),
                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.toString())))
                );
            }
        }
        plot.getPlotModificationManager().deletePlot(null, whenDone);
    }

    /**
     * Get the age (last play time) of the passed player
     *
     * @param uuid                     the uuid of the owner to check against
     * @param shouldDeleteUnknownOwner {@code true} if an unknown player should be counted as never online
     * @return the millis since the player was last online, or {@link Long#MAX_VALUE} if player was never online
     * @since 6.4.0
     */
    public long getAge(UUID uuid, final boolean shouldDeleteUnknownOwner) {
        if (PlotSquared.platform().playerManager().getPlayerIfExists(uuid) != null) {
            return 0;
        }
        Long last = this.dates_cache.get(uuid);
        if (last == null) {
            OfflinePlotPlayer opp = PlotSquared.platform().playerManager().getOfflinePlayer(uuid);
            if (opp != null && (last = opp.getLastPlayed()) != 0) {
                this.dates_cache.put(uuid, last);
            } else {
                return shouldDeleteUnknownOwner ? Long.MAX_VALUE : 0;
            }
        }
        if (last == 0) {
            return 0;
        }
        return System.currentTimeMillis() - last;
    }

    public long getAge(Plot plot, final boolean shouldDeleteUnknownOwner) {
        if (!plot.hasOwner() || Objects.equals(DBFunc.EVERYONE, plot.getOwner())
                || PlotSquared.platform().playerManager().getPlayerIfExists(plot.getOwner()) != null || plot.getRunning() > 0) {
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
            long age = getAge(owner, shouldDeleteUnknownOwner);
            if (age < min) {
                min = age;
            }
        }
        return min;
    }

}
