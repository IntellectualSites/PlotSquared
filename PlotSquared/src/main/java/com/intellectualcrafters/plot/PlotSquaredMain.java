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

    /**
     * Get all plots belonging to a specified
     * player UUID
     *
     * @param uuid Player UUID
     * @return A set containing the plots
     * @see #getPlots(PlotPlayer) Using PlotPlayer rather than UUID
     * @see #getPlots(String, UUID) To limit the search to a specific world
     * @see #getPlots(String, String) To limit the search to a specific world, and use player names
     */
    Set<Plot> getPlots(UUID uuid);

    /**
     * Remove a plot from the specified plot world
     *
     * @param world World Name
     * @param id Plot ID
     * @param callEvent If the plot delete event should be called
     * @return true on success | false on failure
     */
    boolean removePlot(String world, PlotId id, boolean callEvent);

    /**
     * Load a world
     *
     * @param world World Name
     * @param generator PlotGenerator Implementation
     */
    void loadWorld(String world, PlotGenerator generator);

    /**
     * Setup a PlotWorld
     *
     * @param world World Name
     * @param id World ID
     * @return true on success | false on failure
     */
    boolean setupPlotWorld(String world, String id);

    /**
     * Get the database connection
     *
     * @see #getDatabase() Get the database implementation
     * @return Database connection
     */
    Connection getConnection();

    /**
     * Copy a file into the specified folder
     *
     * @param file File to copy
     * @param folder Folder to copy to
     */
    void copyFile(String file, String folder);

    /**
     * Disable the PlotSquared core
     */
    void disable();

    /**
     * Setup the database configuration file
     */
    void setupDatabase();

    /**
     * Setup the default flags
     */
    void setupDefaultFlags();

    /**
     * Setup the global configuration file
     */
    void setupConfig();

    /**
     * Setup all configuration classes
     *
     * @see #setupDatabase() Setup the database configuration file
     * @see #setupConfig() Setup the general configuration file
     * @see #setupDefaultFlags() Setup the default flags
     */
    void setupConfigs();

    void showDebug();

    /**
     * Get the current java version as
     * a double
     * @code {
     *  1.7 = Java 7
     *  1.8 = Java 8
     *  etc...
     * }
     *
     * @return Java version as a double
     */
    double getJavaVersion();

    /**
     * Get a set containing the names of
     * all PlotWorlds
     *
     * @see #getPlotWorldObjects() To get the actual objects
     *
     * @return A set containing the names of all PlotWorlds
     */
    Set<String> getPlotWorlds();

    /**
     * Get a collection containing the
     * PlotWorld objects
     *
     * @see #getPlotWorlds() To get the names of the worlds
     * @see PlotWorld The returned object
     *
     * @return Collection containing PlotWorld's
     */
    Collection<PlotWorld> getPlotWorldObjects();
}
