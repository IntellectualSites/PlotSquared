package com.intellectualcrafters.plot;

import java.io.File;

import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChatManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.sk89q.worldedit.WorldEdit;

public interface IPlotMain {
    
    /**
     * Log a message to console
     * @param message
     */
    void log(String message);

    /**
     * Get the `PlotSquared` directory (e.g. /plugins/PlotSquared or /mods/PlotSquared)
     * @return
     */
    File getDirectory();
    
    /**
     * Wrap a player into a PlotPlayer object
     * @param obj
     * @return
     */
    PlotPlayer wrapPlayer(Object obj);
    
    /**
     * Disable the implementation
     *  - If a full disable isn't feasibly, just disable what it can
     */
    void disable();

    /**
     * Get the version of the PlotSquared being used
     * @return
     */
    int[] getPluginVersion();
    
    /**
     * Get the version of Minecraft that is running
     * (used to check what protocols and such are supported)
     * @return
     */
    int[] getServerVersion();
    
    /**
     * Get the nms package prefix
     * @return
     */
    String getNMSPackage();
    
    /**
     * Get the schematic handler
     * @return
     */
    SchematicHandler initSchematicHandler();
    
    /**
     * Get the schematic handler
     * @return
     */
    ChatManager initChatManager();
    
    /**
     * The task manager will run and manage minecraft tasks
     * @return
     */
    TaskManager getTaskManager();

    /**
     * Run the task that will kill road mobs
     */
    void runEntityTask();

    /**
     * Register the implementation specific commands
     */
    void registerCommands();

    /**
     * Register the protection system (used to protect blocks and such)
     */
    void registerPlayerEvents();

    /**
     * Register inventory related events (used for inventory guis)
     */
    void registerInventoryEvents();

    /**
     * Register plot plus related events (whatever these are?)
     */
    void registerPlotPlusEvents();

    /**
     * Register force field events (why is this a thing?)
     */
    void registerForceFieldEvents();

    /**
     * Register the WorldEdit hook
     */
    boolean initWorldEdit();
    
    /**
     * Register TNT related events (if TNT protection is enabled)
     */
    void registerTNTListener();

    /**
     * Get the economy provider
     * @return
     */
    EconHandler getEconomyHandler();

    /**
     * Get the block manager
     * @return
     */
    BlockManager initBlockManager();
    
    /**
     * Get the EventUtil class
     * @return
     */
    EventUtil initEventUtil();

    /**
     * Get the chunk manager
     * @return
     */
    ChunkManager initChunkManager();

    /**
     * Get the setuputils class (used for world creation)
     * @return
     */
    SetupUtils initSetupUtils();

    /**
     * Get HybridUtils class (common functions useful for hybrid world generation)
     * @return
     */
    HybridUtils initHybridUtils();
    
    /**
     * Start the metrics task
     */
    void startMetrics();
    
    /**
     * If a world is already loaded, set the generator (use NMS if required)
     * @param world
     */
    void setGenerator(String world);

    /**
     * Get the UUIDHandlerImplementation which will cache and provide UUIDs
     * @return
     */
    UUIDHandlerImplementation initUUIDHandler();
    
    /**
     * Get the InventoryUtil class (used for implementation specific inventory guis)
     * @return
     */
    InventoryUtil initInventoryUtil();

    /**
     * Run the converter for the implementation (not necessarily PlotMe, just any plugin that we can convert from)
     * @return
     */
    boolean initPlotMeConverter();
    
    /**
     * Unregister a PlotPlayer from cache e.g. if they have logged off
     * @param player
     */
    void unregister(PlotPlayer player);

    /**
     * Get the generator wrapper for a world (world) and generator (name)
     * @param world
     * @param name
     * @return
     */
    PlotGenerator<?> getGenerator(String world, String name);

    /**
     * Register the chunk processor which will clean out chunks that have too many blockstates or entities
     */
    void registerChunkProcessor();

    /**
     * Register the world initialization events (used to keep track of worlds being generated)
     */
    void registerWorldEvents();

    /**
     * Get the name of the server
     * @return
     */
    String getServerName();
    
    /**
     * Get the class that will manage player titles
     * @return
     */
    AbstractTitle initTitleManager();
}
