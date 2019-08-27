package com.plotsquared.nukkit.util;

import cn.nukkit.scheduler.TaskHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.nukkit.NukkitMain;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NukkitTaskManager extends TaskManager {

    private final NukkitMain plugin;

    public NukkitTaskManager(NukkitMain nukkitMain) {
        this.plugin = nukkitMain;
    }

    private AtomicInteger index = new AtomicInteger(0);
    private HashMap<Integer, Integer> tasks = new HashMap<>();

    @Override
    public int taskRepeat(Runnable r, int interval) {
        TaskHandler task = this.plugin.getServer().getScheduler().scheduleRepeatingTask(r, interval, false);
        return task.getTaskId();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int taskRepeatAsync(Runnable r, int interval) {
        TaskHandler task = this.plugin.getServer().getScheduler().scheduleRepeatingTask(r, interval, true);
        return task.getTaskId();
    }

    @Override
    public void taskAsync(Runnable r) {
        if (r == null) {
            return;
        }
        this.plugin.getServer().getScheduler().scheduleTask(r, true);
    }

    @Override
    public void task(Runnable r) {
        if (r == null) {
            return;
        }
        this.plugin.getServer().getScheduler().scheduleTask(r, false);
    }

    @Override
    public void taskLater(Runnable r, int delay) {
        if (r == null) {
            return;
        }
        this.plugin.getServer().getScheduler().scheduleDelayedTask(r, delay);
    }

    @Override
    public void taskLaterAsync(Runnable r, int delay) {
        this.plugin.getServer().getScheduler().scheduleDelayedTask(r, delay, true);
    }

    @Override
    public void cancelTask(int task) {
        if (task != -1) {
            this.plugin.getServer().getScheduler().cancelTask(task);
        }
    }
}
