package com.plotsquared.bukkit.util;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.BukkitMain;

public class BukkitTaskManager extends TaskManager {
    @Override
    public int taskRepeat(final Runnable r, final int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }
    
    @Override
    public int taskRepeatAsync(final Runnable r, final int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleAsyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }
    
    @Override
    public void taskAsync(final Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTaskAsynchronously(BukkitMain.THIS, r).getTaskId();
    }
    
    @Override
    public void task(final Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTask(BukkitMain.THIS, r).getTaskId();
    }
    
    @Override
    public void taskLater(final Runnable r, final int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLater(BukkitMain.THIS, r, delay).getTaskId();
    }
    
    @Override
    public void taskLaterAsync(final Runnable r, final int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLaterAsynchronously(BukkitMain.THIS, r, delay);
    }
    
    @Override
    public void cancelTask(final int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
