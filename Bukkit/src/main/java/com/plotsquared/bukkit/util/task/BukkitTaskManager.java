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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Bukkit implementation of {@link TaskManager} using
 * by {@link org.bukkit.scheduler.BukkitScheduler} and {@link BukkitPlotSquaredTask}
 */
@Singleton public class BukkitTaskManager extends TaskManager {

    private final BukkitPlatform bukkitMain;
    private final TaskTime.TimeConverter timeConverter;

    @Inject public BukkitTaskManager(@Nonnull final BukkitPlatform bukkitMain,
                                     @Nonnull final TaskTime.TimeConverter timeConverter) {
        this.bukkitMain = bukkitMain;
        this.timeConverter = timeConverter;
    }

    @Override
    public PlotSquaredTask taskRepeat(@Nonnull final Runnable runnable,
                                      @Nonnull final TaskTime taskTime) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final BukkitPlotSquaredTask bukkitPlotSquaredTask = new BukkitPlotSquaredTask(runnable);
        bukkitPlotSquaredTask.runTaskTimer(this.bukkitMain, ticks, ticks);
        return bukkitPlotSquaredTask;
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(@Nonnull final Runnable runnable,
                                           @Nonnull final TaskTime taskTime) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final BukkitPlotSquaredTask bukkitPlotSquaredTask = new BukkitPlotSquaredTask(runnable);
        bukkitPlotSquaredTask.runTaskTimerAsynchronously(this.bukkitMain, ticks, ticks);
        return bukkitPlotSquaredTask;
    }

    @Override public void taskAsync(@Nonnull final Runnable runnable) {
        if (this.bukkitMain.isEnabled()) {
            new BukkitPlotSquaredTask(runnable).runTaskAsynchronously(this.bukkitMain);
        } else {
            runnable.run();
        }
    }

    @Override public <T> T sync(@Nonnull final Callable<T> function, final int timeout) throws Exception {
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            return function.call();
        }
        return this.callMethodSync(function).get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override public <T> Future<T> callMethodSync(@Nonnull final Callable<T> method) {
        return Bukkit.getScheduler().callSyncMethod(this.bukkitMain, method);
    }

    @Override public void task(@Nonnull final Runnable runnable) {
        new BukkitPlotSquaredTask(runnable).runTaskAsynchronously(this.bukkitMain);
    }

    @Override public void taskLater(@Nonnull final Runnable runnable,
                                    @Nonnull final TaskTime taskTime) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new BukkitPlotSquaredTask(runnable).runTaskLater(this.bukkitMain, delay);
    }

    @Override public void taskLaterAsync(@Nonnull final Runnable runnable,
                                         @Nonnull final TaskTime taskTime) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new BukkitPlotSquaredTask(runnable).runTaskLaterAsynchronously(this.bukkitMain, delay);
    }

}
