package com.github.intellectualsites.plotsquared;

import com.github.intellectualsites.plotsquared.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.util.logger.ILogger;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.ChatManager;
import com.github.intellectualsites.plotsquared.util.ChunkManager;
import com.github.intellectualsites.plotsquared.util.EconHandler;
import com.github.intellectualsites.plotsquared.util.InventoryUtil;
import com.github.intellectualsites.plotsquared.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.util.SetupUtils;
import com.github.intellectualsites.plotsquared.util.tasks.TaskManager;
import com.github.intellectualsites.plotsquared.util.uuid.UUIDHandlerImplementation;
import com.github.intellectualsites.plotsquared.util.WorldUtil;
import com.github.intellectualsites.plotsquared.queue.QueueProvider;
import com.sk89q.worldedit.extension.platform.Actor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IPlotMain extends ILogger {

    /**
     * Logs a message to console.
     *
     * @param message the message to log
     */
    void log(String message);

    /**
     * Gets the directory which contains PlotSquared files. The directory may not exist.
     *
     * @return the PlotSquared directory
     */
    File getDirectory();

    /**
     * Gets the folder where all world data is stored.
     *
     * @return the world folder
     */
    File getWorldContainer();

    /**
     * Wraps a player into a PlotPlayer object.
     *
     * @param player The player to convert to a PlotPlayer
     * @return A PlotPlayer
     */
    PlotPlayer wrapPlayer(Object player);

    /**
     * Completely shuts down the plugin.
     */
    void shutdown();

    /**
     * Gets the version of the PlotSquared being used.
     *
     * @return the plugin version
     */
    int[] getPluginVersion();

    /**
     * Gets the version of the PlotSquared being used as a string.
     *
     * @return the plugin version as a string
     */
    String getPluginVersionString();

    default String getPluginName() {
        return "PlotSquared";
    }

    /**
     * Gets the version of Minecraft that is running.
     */
    int[] getServerVersion();

    /**
     * Gets the server implementation name and version
     */
    String getServerImplementation();

    /**
     * Gets the NMS package prefix.
     *
     * @return The NMS package prefix
     */
    String getNMSPackage();

    /**
     * Gets the schematic handler.
     *
     * @return The {@link SchematicHandler}
     */
    SchematicHandler initSchematicHandler();

    /**
     * Starts the {@link ChatManager}.
     *
     * @return the ChatManager
     */
    ChatManager initChatManager();

    /**
     * The task manager will run and manage Minecraft tasks.
     *
     * @return the PlotSquared task manager
     */
    TaskManager getTaskManager();

    /**
     * Run the task that will kill road mobs.
     */
    void runEntityTask();

    /**
     * Registerss the implementation specific commands.
     */
    void registerCommands();

    /**
     * Register the protection system.
     */
    void registerPlayerEvents();

    /**
     * Register force field events.
     */
    void registerForceFieldEvents();

    /**
     * Registers the WorldEdit hook.
     */
    boolean initWorldEdit();

    /**
     * Gets the economy provider.
     *
     * @return the PlotSquared economy manager
     */
    EconHandler getEconomyHandler();

    /**
     * Gets the {@link QueueProvider} class.
     */
    QueueProvider initBlockQueue();

    /**
     * Gets the {@link WorldUtil} class.
     */
    WorldUtil initWorldUtil();

    /**
     * Gets the chunk manager.
     *
     * @return the PlotSquared chunk manager
     */
    ChunkManager initChunkManager();

    /**
     * Gets the {@link SetupUtils} class.
     */
    SetupUtils initSetupUtils();

    /**
     * Gets {@link HybridUtils} class.
     */
    HybridUtils initHybridUtils();

    /**
     * Start Metrics.
     */
    void startMetrics();

    /**
     * If a world is already loaded, set the generator (use NMS if required).
     *
     * @param world The world to set the generator
     */
    void setGenerator(String world);

    /**
     * Gets the {@link UUIDHandlerImplementation} which will cache and
     * provide UUIDs.
     */
    UUIDHandlerImplementation initUUIDHandler();

    /**
     * Gets the {@link InventoryUtil} class (used for implementation specific
     * inventory guis).
     */
    InventoryUtil initInventoryUtil();

    /**
     * Unregisters a {@link PlotPlayer} from cache e.g. if they have logged off.
     *
     * @param player the player to remove
     */
    void unregister(PlotPlayer player);

    /**
     * Gets the generator wrapper for a world (world) and generator (name).
     *
     * @param world the world to get the generator from
     * @param name the name of the generator
     * @return the generator being used for the provided world
     */
    GeneratorWrapper<?> getGenerator(String world, String name);

    GeneratorWrapper<?> wrapPlotGenerator(String world, IndependentPlotGenerator generator);

    /**
     * Register the chunk processor which will clean out chunks that have too
     * many block states or entities.
     */
    void registerChunkProcessor();

    /**
     * Register the world initialization events (used to keep track of worlds
     * being generated).
     */
    void registerWorldEvents();

    /**
     * Usually HybridGen
     *
     * @return Default implementation generator
     */
    @NotNull IndependentPlotGenerator getDefaultGenerator();

    List<Map.Entry<Map.Entry<String, String>, Boolean>> getPluginIds();

    Actor getConsole();
}
