package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SpongeTaskManager extends TaskManager {

    private final AtomicInteger atomicInteger = new AtomicInteger();

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final SpongeMain spongeMain;

    public SpongeTaskManager(SpongeMain spongeMain) {
        this.spongeMain = spongeMain;
    }

    @Override
    public int taskRepeat(Runnable runnable, int interval) {
        int val = this.atomicInteger.incrementAndGet();
        Task.Builder builder = this.spongeMain.getGame().getScheduler().createTaskBuilder();
        Task.Builder built = builder.delayTicks(interval).intervalTicks(interval).execute(runnable);
        Task task = built.submit(this.spongeMain.getPlugin());
        this.tasks.put(val, task);
        return val;
    }

    @Override
    public int taskRepeatAsync(Runnable runnable, int interval) {
        int val = this.atomicInteger.incrementAndGet();
        Task.Builder built = this.spongeMain.getGame().getScheduler().createTaskBuilder().async().intervalTicks(interval).execute(runnable);
        Task task = built.submit(this.spongeMain.getPlugin());
        this.tasks.put(val, task);
        return val;
    }

    @Override
    public void taskAsync(Runnable runnable) {
        Task.Builder builder = this.spongeMain.getGame().getScheduler().createTaskBuilder();
        builder.async().execute(runnable).submit(this.spongeMain.getPlugin());
    }

    @Override
    public void task(Runnable runnable) {
        Task.Builder builder = this.spongeMain.getGame().getScheduler().createTaskBuilder();
        builder.execute(runnable).submit(this.spongeMain.getPlugin());
    }

    @Override
    public void taskLater(Runnable runnable, int delay) {
        Task.Builder builder = this.spongeMain.getGame().getScheduler().createTaskBuilder();
        builder.delayTicks(delay).execute(runnable).submit(this.spongeMain.getPlugin());
    }

    @Override
    public void taskLaterAsync(Runnable runnable, int delay) {
        Task.Builder builder = this.spongeMain.getGame().getScheduler().createTaskBuilder();
        builder.async().delayTicks(delay).execute(runnable).submit(this.spongeMain.getPlugin());
    }

    @Override
    public void cancelTask(int taskId) {
        Task task = this.tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

}
