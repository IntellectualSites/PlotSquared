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
package com.plotsquared.bukkit.util.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Bukkit implementation of {@link TaskManager} using
 * by {@link org.bukkit.scheduler.BukkitScheduler} and {@link BukkitPlotSquaredTask}
 */
@Singleton
public class BukkitTaskManager extends TaskManager {

    private final BukkitPlatform bukkitMain;
    private final TaskTime.TimeConverter timeConverter;

    @Inject
    public BukkitTaskManager(
            final @NonNull BukkitPlatform bukkitMain,
            final TaskTime.@NonNull TimeConverter timeConverter
    ) {
        this.bukkitMain = bukkitMain;
        this.timeConverter = timeConverter;
    }

    @Override
    public PlotSquaredTask taskRepeat(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final BukkitPlotSquaredTask bukkitPlotSquaredTask = new BukkitPlotSquaredTask(runnable);
        bukkitPlotSquaredTask.runTaskTimer(this.bukkitMain, ticks, ticks);
        return bukkitPlotSquaredTask;
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final BukkitPlotSquaredTask bukkitPlotSquaredTask = new BukkitPlotSquaredTask(runnable);
        bukkitPlotSquaredTask.runTaskTimerAsynchronously(this.bukkitMain, ticks, ticks);
        return bukkitPlotSquaredTask;
    }

    @Override
    public void taskAsync(final @NonNull Runnable runnable) {
        if (this.bukkitMain.isEnabled()) {
            new BukkitPlotSquaredTask(runnable).runTaskAsynchronously(this.bukkitMain);
        } else {
            runnable.run();
        }
    }

    @Override
    public <T> T sync(final @NonNull Callable<T> function, final int timeout) throws Exception {
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            return function.call();
        }
        return this.callMethodSync(function).get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> Future<T> callMethodSync(final @NonNull Callable<T> method) {
        return Bukkit.getScheduler().callSyncMethod(this.bukkitMain, method);
    }

    @Override
    public void task(final @NonNull Runnable runnable) {
        new BukkitPlotSquaredTask(runnable).runTask(this.bukkitMain);
    }

    @Override
    public void taskLater(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new BukkitPlotSquaredTask(runnable).runTaskLater(this.bukkitMain, delay);
    }

    @Override
    public void taskLaterAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new BukkitPlotSquaredTask(runnable).runTaskLaterAsynchronously(this.bukkitMain, delay);
    }

}
