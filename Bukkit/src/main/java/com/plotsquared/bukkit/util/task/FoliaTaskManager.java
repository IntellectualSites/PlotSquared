package com.plotsquared.bukkit.util.task;

import com.google.inject.Singleton;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.dropReturn;
import static java.lang.invoke.MethodHandles.explicitCastArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

/**
 * Bukkit implementation of {@link TaskManager} using
 * by {@link org.bukkit.scheduler.BukkitScheduler} and {@link BukkitPlotSquaredTask}
 */
@Singleton
public class FoliaTaskManager extends TaskManager {

    private final BukkitPlatform bukkitMain;
    private final TaskTime.TimeConverter timeConverter;

    private final ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

    public FoliaTaskManager(final BukkitPlatform bukkitMain, final TaskTime.TimeConverter timeConverter) {
        this.bukkitMain = bukkitMain;
        this.timeConverter = timeConverter;
    }

    @Override
    public <T> T sync(final @NonNull Callable<T> function, final int timeout) throws Exception {
        return backgroundExecutor.schedule(function, timeout, TimeUnit.MILLISECONDS).get();
    }

    @Override
    public <T> Future<T> callMethodSync(final @NonNull Callable<T> method) {
        return backgroundExecutor.submit(method);
    }

    @Override
    public PlotSquaredTask taskRepeat(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        var time = switch (taskTime.getUnit()) {
            case TICKS -> timeConverter.ticksToMs(taskTime.getTime());
            case MILLISECONDS -> taskTime.getTime();
        };
        backgroundExecutor.scheduleAtFixedRate(runnable, 0, time, TimeUnit.MILLISECONDS);
        return PlotSquaredTask.nullTask();
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        var time = switch (taskTime.getUnit()) {
            case TICKS -> timeConverter.ticksToMs(taskTime.getTime());
            case MILLISECONDS -> taskTime.getTime();
        };
        Bukkit.getAsyncScheduler().runAtFixedRate(bukkitMain, scheduledTask -> runnable.run(), 0, time, TimeUnit.MILLISECONDS);
        return PlotSquaredTask.nullTask();
    }

    @Override
    public void taskAsync(@NonNull final Runnable runnable) {
        Bukkit.getAsyncScheduler().runNow(bukkitMain, scheduledTask -> runnable.run());
    }

    @Override
    public void task(@NonNull final Runnable runnable) {
        backgroundExecutor.submit(runnable);
    }

    @Override
    public void taskLater(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        var time = switch (taskTime.getUnit()) {
            case TICKS -> timeConverter.ticksToMs(taskTime.getTime());
            case MILLISECONDS -> taskTime.getTime();
        };
        backgroundExecutor.scheduleWithFixedDelay(runnable, 0, time, TimeUnit.MILLISECONDS);
    }

    @Override
    public void taskLaterAsync(@NonNull final Runnable runnable, @NonNull final TaskTime taskTime) {
        var time = switch (taskTime.getUnit()) {
            case TICKS -> timeConverter.ticksToMs(taskTime.getTime());
            case MILLISECONDS -> taskTime.getTime();
        };
        Bukkit.getAsyncScheduler().runDelayed(this.bukkitMain, scheduledTask -> runnable.run(), time, TimeUnit.MILLISECONDS);
    }

    private static class SchedulerAdapter {

        private static final MethodHandle EXECUTE_FOR_LOCATION;
        private static final MethodHandle EXECUTE_FOR_PLAYER;
        private static final Runnable THROW_IF_RETIRED = () -> throwRetired();

        private static final MethodType LOCATION_EXECUTE_TYPE = methodType(
                void.class,
                Plugin.class,
                org.bukkit.Location.class,
                Runnable.class
        );

        private static final MethodType ENTITY_EXECUTE_TYPE = methodType(
                boolean.class,
                Plugin.class,
                Runnable.class,
                Runnable.class,
                long.class
        );

        static {
            final Plugin pluginInstance = ((Plugin) PlotSquared.get());
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            MethodHandle executeForLocation;

            MethodHandle executeForPlayer;
            try {
                Class<?> regionisedSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.FoliaRegionScheduler");
                final Method method = Bukkit.class.getDeclaredMethod("getRegionScheduler");
                executeForLocation = lookup.findVirtual(
                        regionisedSchedulerClass,
                        "execute",
                        LOCATION_EXECUTE_TYPE
                );
                executeForLocation = executeForLocation.bindTo(method.invoke(null));
                executeForLocation = executeForLocation.bindTo(pluginInstance);

                Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                executeForPlayer = lookup.findVirtual(
                        entitySchedulerClass,
                        "execute",
                        ENTITY_EXECUTE_TYPE
                );
                // (ES, P, R, R, L)Z (ES, R, R, L)Z
                executeForPlayer = insertArguments(executeForPlayer, 1, pluginInstance);
                // (ES, R1, R2, L)Z -> (ES, R1)Z
                executeForPlayer = insertArguments(executeForPlayer, 2, THROW_IF_RETIRED, 0);
                // (ES, R1)Z -> (ES, R1)V
                executeForPlayer = dropReturn(executeForPlayer);
                MethodHandle getScheduler = lookup.findVirtual(
                        org.bukkit.entity.Entity.class,
                        "getScheduler",
                        methodType(entitySchedulerClass)
                );
                // (ES, R1)V -> (E, R1)V
                executeForPlayer = filterArguments(executeForPlayer, 0, getScheduler);
                MethodType finalType = methodType(void.class, org.bukkit.entity.Player.class, Runnable.class);
                // (ES, R1)V -> (P, R1)V
                executeForPlayer = explicitCastArguments(executeForPlayer, finalType);
            } catch (Throwable throwable) {
                throw new AssertionError(throwable);
            }
            EXECUTE_FOR_LOCATION = executeForLocation;
            EXECUTE_FOR_PLAYER = executeForPlayer;
        }

        static void executeForLocation(Location location, Runnable task) {
            try {
                EXECUTE_FOR_LOCATION.invokeExact(BukkitAdapter.adapt(location), task);
            } catch (Error | RuntimeException e) {
                throw e;
            } catch (Throwable other) {
                throw new RuntimeException(other);
            }
        }
        static void executeForEntity(Player player, Runnable task) {
            try {
                EXECUTE_FOR_PLAYER.invokeExact(BukkitAdapter.adapt(player), task);
            } catch (Error | RuntimeException e) {
                throw e;
            } catch (Throwable other) {
                throw new RuntimeException(other);
            }
        }

        private static void throwRetired() {
            throw new RuntimeException("Player retired");
        }

    }

}
