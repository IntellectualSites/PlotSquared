package com.intellectualcrafters.plot;

import java.io.File;

import net.milkbowl.vault.economy.Economy;

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
}
