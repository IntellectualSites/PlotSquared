package com.plotsquared.bukkit.util.task;

import com.plotsquared.core.util.task.PlotSquaredTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class FoliaPlotSquaredTask implements PlotSquaredTask {

    @NonNull
    private final Runnable runnable;
    @NotNull
    private final JavaPlugin javaPlugin;

    private ScheduledTask task;

    public FoliaPlotSquaredTask(final @NonNull Runnable runnable, final JavaPlugin javaPlugin) {
        this.runnable = runnable;
        this.javaPlugin = javaPlugin;
    }

    @Override
    public void runTask() {
        this.task = Bukkit.getGlobalRegionScheduler().run(javaPlugin, scheduledTask -> this.runnable.run());
    }

    public void runTaskLater(long delay) {
        this.task = Bukkit.getGlobalRegionScheduler().runDelayed(javaPlugin, scheduledTask -> this.runnable.run(), delay);
    }

    public void runTaskLaterAsynchronously(long delay) {
        this.task = Bukkit.getAsyncScheduler().runDelayed(javaPlugin, scheduledTask -> this.runnable.run(), delay, TimeUnit.MILLISECONDS);
    }

    public void runTaskAsynchronously() {
        this.task = Bukkit.getAsyncScheduler().runNow(javaPlugin, scheduledTask -> this.runnable.run());
    }

    public void runTaskTimerAsynchronously(long delay, long period) {
        this.task = Bukkit.getAsyncScheduler().runAtFixedRate(javaPlugin, scheduledTask -> this.runnable.run(), delay, period, TimeUnit.MILLISECONDS);
    }

    public void runTaskTimer(long delay, long period) {
        this.task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(javaPlugin, scheduledTask -> this.runnable.run(), delay, period);
    }

    @Override
    public boolean isCancelled() {
        return this.task.isCancelled();
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }

}
