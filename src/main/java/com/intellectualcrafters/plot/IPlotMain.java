package com.intellectualcrafters.plot;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.bukkit.listeners.APlotListener;
import com.plotsquared.bukkit.util.SetupUtils;

import java.io.File;
import java.util.UUID;

public interface IPlotMain {
    void log(String message);

    File getDirectory();
    
    void disable();

    int[] getPluginVersion();
    
    int[] getServerVersion();

    void handleKick(UUID uuid, C c);

    TaskManager getTaskManager();

    void runEntityTask();

    void registerCommands();

    void registerPlayerEvents();

    void registerInventoryEvents();

    void registerPlotPlusEvents();

    void registerForceFieldEvents();

    void registerWorldEditEvents();
    
    void registerTNTListener();

    EconHandler getEconomyHandler();

    BlockManager initBlockManager();
    
    EventUtil initEventUtil();

    ChunkManager initChunkManager();

    SetupUtils initSetupUtils();

    HybridUtils initHybridUtils();

    UUIDHandlerImplementation initUUIDHandler();
    
    InventoryUtil initInventoryUtil();

    boolean initPlotMeConverter();
    
    void unregister(PlotPlayer player);

    PlotGenerator<?> getGenerator(String world, String name);

    APlotListener initPlotListener();

    void registerChunkProcessor();

    void registerWorldEvents();

    PlayerManager initPlayerManager();
    
    String getServerName();
}
