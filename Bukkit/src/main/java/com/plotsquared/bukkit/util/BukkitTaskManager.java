package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.BukkitMain;
import org.bukkit.Bukkit;

public class BukkitTaskManager extends TaskManager {

    @Override
    public int taskRepeat(Runnable runnable, int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, runnable, interval, interval);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int taskRepeatAsync(Runnable runnable, int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleAsyncRepeatingTask(BukkitMain.THIS, runnable, interval, interval);
    }

    @Override
    public void taskAsync(Runnable runnable) {
        BukkitMain.THIS.getServer().getScheduler().runTaskAsynchronously(BukkitMain.THIS, runnable).getTaskId();
    }

    @Override
    public void task(Runnable runnable) {
        BukkitMain.THIS.getServer().getScheduler().runTask(BukkitMain.THIS, runnable).getTaskId();
    }

    @Override
    public void taskLater(Runnable runnable, int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLater(BukkitMain.THIS, runnable, delay).getTaskId();
    }

    @Override
    public void taskLaterAsync(Runnable runnable, int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLaterAsynchronously(BukkitMain.THIS, runnable, delay);
    }

    @Override
    public void cancelTask(int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
