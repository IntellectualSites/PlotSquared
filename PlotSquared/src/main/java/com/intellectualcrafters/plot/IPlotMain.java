package com.intellectualcrafters.plot;

import java.io.File;

import net.milkbowl.vault.economy.Economy;

import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public interface IPlotMain {
    public void log(String message);

    public File getDirectory();

    public void disable();

    public String getVersion();

    public TaskManager getTaskManager();

    public void runEntityTask();

    public void registerCommands();

    public void registerPlayerEvents();

    public void registerInventoryEvents();

    public void registerPlotPlusEvents();

    public void registerForceFieldEvents();

    public void registerWorldEditEvents();

    public Economy getEconomy();

    public BlockManager initBlockManager();
    
    public EventUtil initEventUtil();

    public ChunkManager initChunkManager();

    public SetupUtils initSetupUtils();

    public HybridUtils initHybridUtils();

    public UUIDWrapper initUUIDHandler();

    public boolean initPlotMeConverter();

    public void getGenerator(String world, String name);
}
