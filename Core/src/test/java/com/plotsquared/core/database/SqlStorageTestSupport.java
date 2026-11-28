/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.database;

import com.google.inject.Injector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.plotsquared.core.PlotPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test-side replacements for the platform singletons that the SQL storage layer touches.
 *
 * <p>Nothing here changes production code. {@link PlotSquared} is a singleton whose constructor loads
 * configuration from disk, so the test allocates the instance without running the constructor and sets
 * only the fields the storage layer reads. Every installed value is returned by the matching
 * {@code uninstall} method, so tests can restore the previous static state.</p>
 */
final class SqlStorageTestSupport {

    private SqlStorageTestSupport() {
    }

    /**
     * Installs a {@link PlotSquared} instance whose platform reports {@code directory} as its data folder.
     *
     * @return the previous value of the singleton, to be passed to {@link #uninstallPlotSquared(Object)}
     */
    static Object installPlotSquared(File directory) throws Exception {
        Field instanceField = PlotSquared.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        Object previous = instanceField.get(null);

        PlotSquared plotSquared = allocateWithoutConstructor(PlotSquared.class);
        Field platformField = PlotSquared.class.getDeclaredField("platform");
        platformField.setAccessible(true);
        platformField.set(plotSquared, platform(directory));

        instanceField.set(null, plotSquared);
        return previous;
    }

    static void uninstallPlotSquared(Object previous) throws Exception {
        Field instanceField = PlotSquared.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, previous);
    }

    /**
     * Marks WorldEdit's platform manager as initialised, so that its registries (block, item and biome types)
     * can be read. Plot and plot area field initialisers look up WorldEdit types. No platform is registered and
     * the registries stay empty, so those lookups return {@code null} and the storage layer never uses them.
     *
     * @return the previous flag value, to be passed to {@link #uninstallWorldEdit(boolean)}
     */
    static boolean installWorldEdit() throws Exception {
        return worldEditInitialized().getAndSet(true);
    }

    static void uninstallWorldEdit(boolean previous) throws Exception {
        worldEditInitialized().set(previous);
    }

    private static AtomicBoolean worldEditInitialized() throws Exception {
        PlatformManager platformManager = WorldEdit.getInstance().getPlatformManager();
        Field initialized = PlatformManager.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        return (AtomicBoolean) initialized.get(platformManager);
    }

    /**
     * Installs a {@link GlobalFlagContainer} that every plot area and plot flag container subscribes to.
     *
     * <p>The real constructor registers every default flag, and some of those flags initialise WorldEdit's item
     * registry, which needs a running platform. The storage layer never reads the flag registry (it writes flag
     * names and values as strings), so the container is allocated without running its constructor and only its
     * collections are created. Each test gets a new container, and {@link #uninstallGlobalFlagContainer} restores
     * the previous singleton and the previous flag name map.</p>
     *
     * @return the previous singleton and name map, to be passed to {@link #uninstallGlobalFlagContainer(Object)}
     */
    static Object installGlobalFlagContainer() throws Exception {
        Field instanceField = GlobalFlagContainer.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        Field nameMapField = GlobalFlagContainer.class.getDeclaredField("stringClassMap");
        nameMapField.setAccessible(true);
        Object previousInstance = instanceField.get(null);
        Object previousNameMap = nameMapField.get(null);

        GlobalFlagContainer container = allocateWithoutConstructor(GlobalFlagContainer.class);
        setField(FlagContainer.class, container, "unknownFlags", new HashMap<>());
        setField(FlagContainer.class, container, "flagMap", new HashMap<>());
        setField(FlagContainer.class, container, "updateSubscribers", new HashSet<>());
        nameMapField.set(null, new HashMap<>());
        instanceField.set(null, container);
        return new PreviousFlagState(previousInstance, previousNameMap);
    }

    static void uninstallGlobalFlagContainer(Object previous) throws Exception {
        PreviousFlagState state = (PreviousFlagState) previous;
        Field instanceField = GlobalFlagContainer.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, state.instance());
        Field nameMapField = GlobalFlagContainer.class.getDeclaredField("stringClassMap");
        nameMapField.setAccessible(true);
        nameMapField.set(null, state.nameMap());
    }

    private record PreviousFlagState(Object instance, Object nameMap) {
    }

    private static void setField(Class<?> declaringClass, Object target, String name, Object value)
            throws Exception {
        Field field = declaringClass.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Installs a {@link ManualTaskManager}. Synchronous tasks run inline. Asynchronous tasks are kept in a queue
     * that the test drains on its own thread, so no background thread touches the JDBC connection.
     *
     * @return the previously installed task manager, possibly {@code null}
     */
    static ManualTaskManager installTaskManager() {
        TaskManager previous = TaskManager.getPlatformImplementation();
        ManualTaskManager manager = new ManualTaskManager(previous);
        TaskManager.setPlatformImplementation(manager);
        return manager;
    }

    static void uninstallTaskManager(ManualTaskManager installed) {
        TaskManager.setPlatformImplementation(installed.previous());
    }

    /**
     * A {@link PlotArea} without a generator, block queue or plot manager, enough for the storage layer,
     * which only uses the area's world name and its string form.
     */
    static PlotArea newPlotArea(String worldName) {
        return new PlotArea(
                worldName, null, (IndependentPlotGenerator) null, null, null,
                (YamlConfiguration) null, null
        ) {
            @Override
            protected PlotManager createManager() {
                return null;
            }

            @Override
            public void loadConfiguration(ConfigurationSection config) {
            }

            @Override
            public ConfigurationNode[] getSettingNodes() {
                return new ConfigurationNode[0];
            }
        };
    }

    static PlotId id(int x, int z) {
        return PlotId.of(x, z);
    }

    private static PlotPlatform<?> platform(File directory) {
        Injector injector = injector();
        return (PlotPlatform<?>) Proxy.newProxyInstance(
                PlotPlatform.class.getClassLoader(),
                new Class<?>[]{PlotPlatform.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDirectory" -> directory;
                    case "injector" -> injector;
                    case "toString" -> "sql-storage-test-platform";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    /**
     * Plot objects inject their members on construction. Nothing the storage layer does needs those members,
     * so injection is a no-op.
     */
    private static Injector injector() {
        return (Injector) Proxy.newProxyInstance(
                Injector.class.getClassLoader(),
                new Class<?>[]{Injector.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "toString" -> "sql-storage-test-injector";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);
        Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
        return type.cast(allocate.invoke(unsafe, type));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        return null;
    }

    /**
     * Runs synchronous tasks inline and queues asynchronous tasks until the test asks for them.
     *
     * <p>The production {@link SQLManager} starts an endless background loop from its constructor. The tests
     * drive writes through {@link SQLManager#sendBatch()} themselves, so the loop is removed from the queue
     * right after construction and nothing else ever runs in the background.</p>
     */
    static final class ManualTaskManager extends TaskManager {

        private final TaskManager previous;
        private final Queue<Runnable> asyncTasks = new ArrayDeque<>();

        private ManualTaskManager(TaskManager previous) {
            this.previous = previous;
        }

        TaskManager previous() {
            return this.previous;
        }

        Queue<Runnable> asyncTasks() {
            return this.asyncTasks;
        }

        @Override
        public <T> T sync(final Callable<T> function, final int timeout) throws Exception {
            return function.call();
        }

        @Override
        public <T> Future<T> callMethodSync(final Callable<T> method) {
            CompletableFuture<T> future = new CompletableFuture<>();
            try {
                future.complete(method.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
            return future;
        }

        @Override
        public PlotSquaredTask taskRepeat(final Runnable runnable, final TaskTime taskTime) {
            return PlotSquaredTask.nullTask();
        }

        @Override
        public PlotSquaredTask taskRepeatAsync(final Runnable runnable, final TaskTime taskTime) {
            return PlotSquaredTask.nullTask();
        }

        @Override
        public void taskAsync(final Runnable runnable) {
            this.asyncTasks.add(runnable);
        }

        @Override
        public void task(final Runnable runnable) {
            runnable.run();
        }

        @Override
        public void taskLater(final Runnable runnable, final TaskTime taskTime) {
            runnable.run();
        }

        @Override
        public void taskLaterAsync(final Runnable runnable, final TaskTime taskTime) {
            this.asyncTasks.add(runnable);
        }

    }

}
