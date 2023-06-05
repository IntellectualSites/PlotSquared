package com.plotsquared.bukkit.util.task;

import com.google.inject.Singleton;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.dropReturn;
import static java.lang.invoke.MethodHandles.explicitCastArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

/**
 * Bukkit implementation of {@link TaskManager} using
 * by {@link org.bukkit.scheduler.BukkitScheduler} and {@link BukkitPlotSquaredTask}
 */
@Singleton
public class FoliaTaskManager extends TaskManager {

    private final BukkitPlatform bukkitMain;
    private final TaskTime.TimeConverter timeConverter;

    private final ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

    public FoliaTaskManager(final BukkitPlatform bukkitMain, final TaskTime.TimeConverter timeConverter) {
        this.bukkitMain = bukkitMain;
        this.timeConverter = timeConverter;
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
        return backgroundExecutor.submit(method);
    }

    @Override
    public PlotSquaredTask taskRepeat(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final FoliaPlotSquaredTask foliaPlotSquaredTask = new FoliaPlotSquaredTask(runnable, this.bukkitMain);
        foliaPlotSquaredTask.runTaskTimer(ticks, ticks);
        return foliaPlotSquaredTask;
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        var time = switch (taskTime.getUnit()) {
            case TICKS -> timeConverter.ticksToMs(taskTime.getTime());
            case MILLISECONDS -> taskTime.getTime();
        };
        final FoliaPlotSquaredTask foliaPlotSquaredTask = new FoliaPlotSquaredTask(runnable, this.bukkitMain);
        foliaPlotSquaredTask.runTaskTimerAsynchronously(time, time);
        return foliaPlotSquaredTask;
    }

    @Override
    public void taskAsync(@NonNull final Runnable runnable) {
        if (this.bukkitMain.isEnabled()) {
            new FoliaPlotSquaredTask(runnable, this.bukkitMain).runTaskAsynchronously();
        } else {
            runnable.run();
        }

    }

    @Override
    public void task(@NonNull final Runnable runnable) {
        new FoliaPlotSquaredTask(runnable, this.bukkitMain).runTask();
    }

    @Override
    public void taskLater(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        new FoliaPlotSquaredTask(runnable, this.bukkitMain).runTaskLater(this.timeConverter.toTicks(taskTime));
    }

    @Override
    public void taskLaterAsync(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        var time = switch (taskTime.getUnit()) {
            case TICKS -> timeConverter.ticksToMs(taskTime.getTime());
            case MILLISECONDS -> taskTime.getTime();
        };
        new FoliaPlotSquaredTask(runnable, this.bukkitMain).runTaskLaterAsynchronously(time);
    }

}
