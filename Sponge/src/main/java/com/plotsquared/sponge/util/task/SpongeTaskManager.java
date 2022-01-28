package com.plotsquared.sponge.util.task;

import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.sponge.SpongePlatform;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SpongeTaskManager extends TaskManager {

    private final SpongePlatform spongeMain;
    private final TaskTime.TimeConverter timeConverter;

    public SpongeTaskManager(SpongePlatform spongeMain, TaskTime.TimeConverter timeConverter) {
        this.spongeMain = spongeMain;
        this.timeConverter = timeConverter;
    }

    @Override
    public <T> T sync(final @NonNull Callable<T> function, final int timeout) throws Exception {
        return null; //TODO
    }

    @Override
    public <T> Future<T> callMethodSync(final @NonNull Callable<T> method) {
        return null; //TODO
    }

    @Override
    public PlotSquaredTask taskRepeat(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        return null; //TODO
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        //TODO input the current tick delay and interval using the Sponge Tick API.
        Task task =
                Task.builder().delay(Ticks.zero()).interval(Ticks.zero()).plugin(spongeMain.container).execute(runnable).build();
        Sponge.asyncScheduler().submit(task);
        return null;
    }

    @Override
    public void taskAsync(@NonNull final Runnable runnable) {
        //TODO
    }

    @Override
    public void task(@NonNull final Runnable runnable) {
        Task.builder().execute(runnable).plugin(spongeMain.container);
    }

    @Override
    public void taskLater(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        //TODO
    }

    @Override
    public void taskLaterAsync(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        //TODO
    }

}
