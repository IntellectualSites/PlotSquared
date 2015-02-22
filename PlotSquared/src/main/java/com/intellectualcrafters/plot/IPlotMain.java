package com.intellectualcrafters.plot;

import java.io.File;

import net.milkbowl.vault.economy.Economy;

import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;

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
    
    public SetupUtils initSetupUtils();
    
    public HybridUtils initHybridUtils();
    
    public boolean initPlotMeConverter();
    
    public void getGenerator(String world, String name);
    
    public boolean callRemovePlot(String world, PlotId id);
}
