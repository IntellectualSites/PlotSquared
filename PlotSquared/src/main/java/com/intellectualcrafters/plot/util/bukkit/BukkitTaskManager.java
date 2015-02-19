package com.intellectualcrafters.plot.util.bukkit;

import java.util.HashSet;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.util.TaskManager;

public class BukkitTaskManager extends TaskManager {
    
    public void taskRepeat(final Runnable r, int interval) {
        BukkitMain.THIS.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, r, interval, interval);
    }
    
    public void taskAsync(final Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTaskAsynchronously(BukkitMain.THIS, r);
    }
    
    public void task(final Runnable r) {
        BukkitMain.THIS.getServer().getScheduler().runTask(BukkitMain.THIS, r);
    }
    
    public void taskLater(final Runnable r, int delay) {
        if (r == null) {
            return;
        }
        BukkitMain.THIS.getServer().getScheduler().runTaskLater(BukkitMain.THIS, r, delay);
    }
}
