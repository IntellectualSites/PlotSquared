package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.BukkitMain;
import org.bukkit.Bukkit;

public class BukkitTaskManager extends TaskManager {

    @Override
    public int taskRepeat(Runnable r, int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int taskRepeatAsync(Runnable r, int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleAsyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }

    @Override
    public void taskAsync(Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTaskAsynchronously(BukkitMain.THIS, r).getTaskId();
    }

    @Override
    public void task(Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTask(BukkitMain.THIS, r).getTaskId();
    }

    @Override
    public void taskLater(Runnable r, int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLater(BukkitMain.THIS, r, delay).getTaskId();
    }

    @Override
    public void taskLaterAsync(Runnable r, int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLaterAsynchronously(BukkitMain.THIS, r, delay);
    }

    @Override
    public void cancelTask(int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
