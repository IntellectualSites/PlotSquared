package com.intellectualcrafters.plot;

import com.intellectualcrafters.plot.database.Database;
import com.intellectualcrafters.plot.object.*;

import java.sql.Connection;
import java.util.*;

/**
 * Core of the PlotSquared Plugin
 */
public interface PlotSquaredMain {

    Database getDatabase();

    void updatePlot(Plot plot);

    PlotWorld getPlotWorld(String world);

    void addPlotWorld(String world, PlotWorld plotworld, PlotManager manager);

    void removePlotWorld(String world);

    void removePlotWorldAbs(String world);

    HashMap<String, HashMap<PlotId, Plot>> getAllPlotsRaw();

    void setAllPlotsRaw(LinkedHashMap<String, HashMap<PlotId, Plot>> plots);

    Set<Plot> getPlots();

    Set<Plot> getPlotsRaw();

    ArrayList<Plot> sortPlots(Collection<Plot> plots);

    ArrayList<Plot> sortPlots(Collection<Plot> plots, String priorityWorld);

    ArrayList<Plot> sortPlotsByWorld(Collection<Plot> plots);

    Set<Plot> getPlots(String world, String player);

    Set<Plot> getPlots(String world, PlotPlayer player);

    Set<Plot> getPlots(String world, UUID uuid);

    boolean isPlotWorld(String world);

    PlotManager getPlotManager(String world);

    String[] getPlotWorldsString();

    HashMap<PlotId, Plot> getPlots(String world);

    Set<Plot> getPlots(PlotPlayer player);

    Set<Plot> getPlots(UUID uuid);

    boolean removePlot(String world, PlotId id, boolean callEvent);

    void loadWorld(String world, PlotGenerator generator);

    boolean setupPlotWorld(String world, String id);

    Connection getConnection();

    void copyFile(String file, String folder);

    void disable();

    void setupDatabase();

    void setupDefaultFlags();

    void setupConfig();

    void setupConfigs();

    void showDebug();

    double getJavaVersion();

    Set<String> getPlotWorlds();

    Collection<PlotWorld> getPlotWorldObjects();
}
