package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.bukkit.BukkitMain;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import org.bukkit.Bukkit;

public class BukkitTaskManager extends TaskManager {

    private final BukkitMain bukkitMain;

    public BukkitTaskManager(BukkitMain bukkitMain) {
        this.bukkitMain = bukkitMain;
    }

    @Override public int taskRepeat(Runnable runnable, int interval) {
        return this.bukkitMain.getServer().getScheduler()
            .scheduleSyncRepeatingTask(this.bukkitMain, runnable, interval, interval);
    }

    @SuppressWarnings("deprecation") @Override
    public int taskRepeatAsync(Runnable runnable, int interval) {
        return this.bukkitMain.getServer().getScheduler()
            .scheduleAsyncRepeatingTask(this.bukkitMain, runnable, interval, interval);
    }

    @Override public void taskAsync(Runnable runnable) {
        this.bukkitMain.getServer().getScheduler().runTaskAsynchronously(this.bukkitMain, runnable)
            .getTaskId();
    }

    @Override public void task(Runnable runnable) {
        this.bukkitMain.getServer().getScheduler().runTask(this.bukkitMain, runnable).getTaskId();
    }

    @Override public void taskLater(Runnable runnable, int delay) {
        this.bukkitMain.getServer().getScheduler().runTaskLater(this.bukkitMain, runnable, delay)
            .getTaskId();
    }

    @Override public void taskLaterAsync(Runnable runnable, int delay) {
        this.bukkitMain.getServer().getScheduler()
            .runTaskLaterAsynchronously(this.bukkitMain, runnable, delay);
    }

    @Override public void cancelTask(int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
