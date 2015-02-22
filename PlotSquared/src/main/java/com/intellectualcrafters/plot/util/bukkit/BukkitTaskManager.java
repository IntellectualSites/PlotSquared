package com.intellectualcrafters.plot.util.bukkit;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.util.TaskManager;

public class BukkitTaskManager extends TaskManager {
    @Override
    public int taskRepeat(final Runnable r, final int interval) {
        return BukkitMain.THIS.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }
    
    @Override
    public int taskAsync(final Runnable r) {
        return BukkitMain.THIS.getServer().getScheduler().runTaskAsynchronously(BukkitMain.THIS, r).getTaskId();
    }
    
    @Override
    public int task(final Runnable r) {
        return BukkitMain.THIS.getServer().getScheduler().runTask(BukkitMain.THIS, r).getTaskId();
    }
    
    @Override
    public int taskLater(final Runnable r, final int delay) {
        return BukkitMain.THIS.getServer().getScheduler().runTaskLater(BukkitMain.THIS, r, delay).getTaskId();
    }
    
    @Override
    public int taskLaterAsync(final Runnable r, final int delay) {
        return BukkitMain.THIS.getServer().getScheduler().runTaskLaterAsynchronously(BukkitMain.THIS, r, delay).getTaskId();
    }

    @Override
    public void cancelTask(int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
