package com.intellectualcrafters.plot.util.bukkit;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.util.TaskManager;

public class BukkitTaskManager extends TaskManager {
    @Override
    public void taskRepeat(final Runnable r, final int interval) {
        BukkitMain.THIS.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }
    
    @Override
    public void taskAsync(final Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTaskAsynchronously(BukkitMain.THIS, r);
    }
    
    @Override
    public void task(final Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTask(BukkitMain.THIS, r);
    }
    
    @Override
    public void taskLater(final Runnable r, final int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLater(BukkitMain.THIS, r, delay);
    }
    
    @Override
    public void taskLaterAsync(final Runnable r, final int delay) {
        BukkitMain.THIS.getServer().getScheduler().runTaskLaterAsynchronously(BukkitMain.THIS, r, delay);
    }
}
