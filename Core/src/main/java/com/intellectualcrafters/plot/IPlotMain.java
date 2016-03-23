package com.intellectualcrafters.plot;

import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChatManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.PlotQueue;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.util.WorldUtil;

import java.io.File;
import java.util.List;

public interface IPlotMain {

    /**
     * Log a message to console.
     * @param message
     */
    void log(String message);

    /**
     * Get the `PlotSquared` directory.
     * @return
     */
    File getDirectory();

    /**
     * Get the directory containing all the worlds.
     * @return
     */
    File getWorldContainer();

    /**
     * Wrap a player into a PlotPlayer object.
     * @param player
     * @return
     */
    PlotPlayer wrapPlayer(Object player);

    /**
     * Disable the implementation.
     *
     * <ul>
     *     <li>If a full disable isn't feasibly, just disable what it can.</li>
     * </ul>
     */
    void disable();

    /**
     * Get the version of the PlotSquared being used.
     * @return
     */
    int[] getPluginVersion();

    /**
     * Get the version of Minecraft that is running.
     * @return
     */
    int[] getServerVersion();

    /**
     * Get the nms package prefix.
     * @return
     */
    String getNMSPackage();

    /**
     * Get the schematic handler.
     * @return
     */
    SchematicHandler initSchematicHandler();

    /**
     * Get the schematic handler.
     * @return
     */
    ChatManager initChatManager();

    /**
     * The task manager will run and manage Minecraft tasks.
     * @return
     */
    TaskManager getTaskManager();

    /**
     * Run the task that will kill road mobs.
     */
    void runEntityTask();

    /**
     * Register the implementation specific commands.
     */
    void registerCommands();

    /**
     * Register the protection system.
     */
    void registerPlayerEvents();

    /**
     * Register inventory related events.
     */
    void registerInventoryEvents();

    /**
     * Register plot plus related events.
     */
    void registerPlotPlusEvents();

    /**
     * Register force field events.
     */
    void registerForceFieldEvents();

    /**
     * Register the WorldEdit hook.
     */
    boolean initWorldEdit();

    /**
     * Get the economy provider.
     * @return
     */
    EconHandler getEconomyHandler();

    /**
     * Get the {@link PlotQueue} class.
     * @return
     */
    PlotQueue initPlotQueue();

    /**
     * Get the {@link WorldUtil} class.
     * @return
     */
    WorldUtil initWorldUtil();

    /**
     * Get the EventUtil class.
     * @return
     */
    EventUtil initEventUtil();

    /**
     * Get the chunk manager.
     * @return
     */
    ChunkManager initChunkManager();

    /**
     * Get the {@link SetupUtils} class.
     * @return
     */
    SetupUtils initSetupUtils();

    /**
     * Get {@link HybridUtils} class.
     * @return
     */
    HybridUtils initHybridUtils();

    /**
     * Start Metrics.
     */
    void startMetrics();

    /**
     * If a world is already loaded, set the generator (use NMS if required).
     * @param world
     */
    void setGenerator(String world);

    /**
     * Get the {@link UUIDHandlerImplementation} which will cache and provide UUIDs.
     * @return
     */
    UUIDHandlerImplementation initUUIDHandler();

    /**
     * Get the {@link InventoryUtil} class (used for implementation specific inventory guis).
     * @return
     */
    InventoryUtil initInventoryUtil();

    /**
     * Run the converter for the implementation (not necessarily PlotMe, just any plugin that we can convert from).
     * @return
     */
    boolean initPlotMeConverter();

    /**
     * Unregister a PlotPlayer from cache e.g. if they have logged off.
     * @param player
     */
    void unregister(PlotPlayer player);

    /**
     * Get the generator wrapper for a world (world) and generator (name).
     * @param world
     * @param name
     * @return
     */
    GeneratorWrapper<?> getGenerator(String world, String name);

    /**
     *
     * @param generator
     * @return
     */
    GeneratorWrapper<?> wrapPlotGenerator(IndependentPlotGenerator generator);

    /**
     * Register the chunk processor which will clean out chunks that have too many blockstates or entities.
     */
    void registerChunkProcessor();

    /**
     * Register the world initialization events (used to keep track of worlds being generated).
     */
    void registerWorldEvents();

    /**
     * Get the name of the server.
     * @return
     */
    String getServerName();

    /**
     * Get the class that will manage player titles.
     * @return
     */
    AbstractTitle initTitleManager();

    List<String> getPluginIds();
}
