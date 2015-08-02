package com.plotsquared.sponge.util;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.service.scheduler.TaskBuilder;

import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.sponge.SpongeMain;

public class SpongeTaskManager extends TaskManager {

    private AtomicInteger i = new AtomicInteger();
    
    private HashMap<Integer, Task> tasks = new HashMap<>();
    
    @Override
    public int taskRepeat(Runnable r, int interval) {
        int val = i.incrementAndGet();
        TaskBuilder builder = SpongeMain.THIS.getGame().getScheduler().getTaskBuilder();
        TaskBuilder built = builder.delay(interval).interval(interval).execute(r);
        Task task = built.submit(SpongeMain.THIS.getPlugin());
        tasks.put(val, task);
        return val;
    }
    
    @Override
    public int taskRepeatAsync(Runnable r, int interval) {
        int val = i.incrementAndGet();
        TaskBuilder builder = SpongeMain.THIS.getGame().getScheduler().getTaskBuilder();
        TaskBuilder built = builder.delay(interval).async().interval(interval).execute(r);
        Task task = built.submit(SpongeMain.THIS.getPlugin());
        tasks.put(val, task);
        return val;
    }

    @Override
    public void taskAsync(Runnable r) {
        TaskBuilder builder = SpongeMain.THIS.getGame().getScheduler().getTaskBuilder();
        builder.async().execute(r).submit(SpongeMain.THIS.getPlugin());
    }

    @Override
    public void task(Runnable r) {
        TaskBuilder builder = SpongeMain.THIS.getGame().getScheduler().getTaskBuilder();
        builder.execute(r).submit(SpongeMain.THIS.getPlugin());
    }

    @Override
    public void taskLater(Runnable r, int delay) {
        TaskBuilder builder = SpongeMain.THIS.getGame().getScheduler().getTaskBuilder();
        builder.delay(delay).execute(r).submit(SpongeMain.THIS.getPlugin());
    }

    @Override
    public void taskLaterAsync(Runnable r, int delay) {
        TaskBuilder builder = SpongeMain.THIS.getGame().getScheduler().getTaskBuilder();
        builder.async().delay(delay).execute(r).submit(SpongeMain.THIS.getPlugin());
    }

    @Override
    public void cancelTask(int i) {
        Task task = tasks.remove(i);
        if (task != null) {
            task.cancel();
        }
    }
    
}
