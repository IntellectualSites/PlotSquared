package com.intellectualcrafters.plot;

import java.io.File;
import java.util.UUID;

import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.plotsquared.bukkit.listeners.APlotListener;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.PlayerManager;
import com.plotsquared.bukkit.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public interface IPlotMain {
    public void log(String message);

    public File getDirectory();
    
    public void disable();

    public int[] getPluginVersion();
    
    public int[] getServerVersion();

    public void handleKick(UUID uuid, C c);

    public TaskManager getTaskManager();

    public void runEntityTask();

    public void registerCommands();

    public void registerPlayerEvents();

    public void registerInventoryEvents();

    public void registerPlotPlusEvents();

    public void registerForceFieldEvents();

    public void registerWorldEditEvents();
    
    public void registerTNTListener();

    public EconHandler getEconomyHandler();

    public BlockManager initBlockManager();
    
    public EventUtil initEventUtil();

    public ChunkManager initChunkManager();

    public SetupUtils initSetupUtils();

    public HybridUtils initHybridUtils();

    public UUIDWrapper initUUIDHandler();
    
    public InventoryUtil initInventoryUtil();

    public boolean initPlotMeConverter();
    
    public void unregister(PlotPlayer player);

    public ChunkGenerator getGenerator(String world, String name);

    public APlotListener initPlotListener();

    public void registerChunkProcessor();

    public void registerWorldEvents();

    public PlayerManager initPlayerManager();
    
    public String getServerName();
}
