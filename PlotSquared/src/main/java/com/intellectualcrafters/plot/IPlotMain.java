package com.intellectualcrafters.plot;

import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.TaskManager;
import net.milkbowl.vault.economy.Economy;

import java.io.File;

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
    
    public void initSetBlockManager();
    
    public boolean initPlotMeConverter();
    
    public void getGenerator(String world, String name);
    
    public boolean callRemovePlot(String world, PlotId id);

    public boolean hasPermission(final PlotPlayer player, final String permission);

    public void sendMessage(final PlotPlayer player, final String message);
}
