////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.api;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.*;
import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Set;

/**
 * PlotSquared API
 *
 * @author Citymonstret
 * @author Empire92
 * @version API 2.0
 */

@SuppressWarnings("unused") public class PlotAPI {

    /**
     * Permission that allows for admin access, this permission node will allow the player to use any part of the
     * plugin, without limitations.
     */
    public static final String ADMIN_PERMISSION = "plots.admin";

    /**
     * Plot Helper Class
     * <p/>
     * General functions involving plots, and the management of them
     *
     * @see com.intellectualcrafters.plot.util.PlotHelper
     */
    private static PlotHelper plotHelper;

    /**
     * Player Functions
     * <p/>
     * General functions involving players, and plot worlds
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions
     */
    private static PlayerFunctions playerFunctions;

    /**
     * Flag Manager
     * <p/>
     * The manager which handles all flags
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager
     */
    private static FlagManager flagManager;

    /**
     * Schematic Handler
     * <p/>
     * The handler which is used to create, and paste, schematics
     *
     * @see com.intellectualcrafters.plot.util.SchematicHandler
     */
    private static SchematicHandler schematicHandler;

    /**
     * The translation class.
     *
     * @see com.intellectualcrafters.plot.config.C
     */
    private static C c;

    /**
     * PlotMain instance
     * <p/>
     * This is the instance that allows for most methods to be used.
     *
     * @see com.intellectualcrafters.plot.PlotMain
     */
    private final PlotMain plotMain;

    /**
     * Constructor. Insert any Plugin. (Optimally the plugin that is accessing the method)
     *
     * @param plugin Plugin used to access this method
     *
     * @throws com.intellectualcrafters.plot.util.PlotSquaredException if the program fails to fetch the PlotMain
     *                                                                 instance
     * @see com.intellectualcrafters.plot.PlotMain
     */
    public PlotAPI(@NotNull final JavaPlugin plugin) {
        this.plotMain = PlotMain.getMain();
        if (this.plotMain == null) {
            throw new PlotSquaredException(PlotSquaredException.PlotError.PLOTMAIN_NULL, "Failed to fetch the plotmain instance, Plot API for " + plugin.getName() + " will be disabled");
        }
    }

    /**
     * Get all plots
     *
     * @return all plots
     *
     * @see com.intellectualcrafters.plot.PlotMain#getPlots()
     */
    public Set<Plot> getAllPlots() {
        return PlotMain.getPlots();
    }

    /**
     * Return all plots for a player
     *
     * @param player Player, whose plots to search for
     *
     * @return all plots that a player owns
     */
    public Set<Plot> getPlayerPlots(@NotNull final Player player) {
        return PlotMain.getPlots(player);
    }

    /**
     * Add a plot world
     *
     * @param world     World Name
     * @param plotWorld Plot World Object
     * @param manager   World Manager
     *
     * @see com.intellectualcrafters.plot.PlotMain#addPlotWorld(String, com.intellectualcrafters.plot.object.PlotWorld,
     * com.intellectualcrafters.plot.object.PlotManager)
     */
    public void addPlotWorld(@NotNull final String world, @NotNull final PlotWorld plotWorld, @NotNull final PlotManager manager) {
        PlotMain.addPlotWorld(world, plotWorld, manager);
    }

    /**
     * @return main configuration
     *
     * @see com.intellectualcrafters.plot.PlotMain#config
     */
    public YamlConfiguration getConfig() {
        return PlotMain.config;
    }

    /**
     * @return storage configuration
     *
     * @see com.intellectualcrafters.plot.PlotMain#storage
     */
    public YamlConfiguration getStorage() {
        return PlotMain.storage;
    }

    /**
     * Get the main class for this plugin <br> - Contains a lot of fields and methods - not very well organized <br>
     * Only use this if you really need it
     *
     * @return PlotMain PlotSquared Main Class
     *
     * @see com.intellectualcrafters.plot.PlotMain
     */
    public PlotMain getMain() {
        return this.plotMain;
    }

    /**
     * PlotHelper class contains useful methods relating to plots.
     *
     * @return PlotHelper
     *
     * @see com.intellectualcrafters.plot.util.PlotHelper
     */
    public PlotHelper getPlotHelper() {
        return plotHelper;
    }

    /**
     * PlayerFunctions class contains useful methods relating to players - Some player/plot methods are here as well
     *
     * @return PlayerFunctions
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions
     */
    public PlayerFunctions getPlayerFunctions() {
        return playerFunctions;
    }

    /**
     * FlagManager class contains methods relating to plot flags
     *
     * @return FlagManager
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager
     */
    public FlagManager getFlagManager() {
        return flagManager;
    }

    /**
     * SchematicHandler class contains methods related to pasting schematics
     *
     * @return SchematicHandler
     *
     * @see com.intellectualcrafters.plot.util.SchematicHandler
     */
    public SchematicHandler getSchematicHandler() {
        return schematicHandler;
    }

    /**
     * C class contains all the captions from the translations.yml file.
     *
     * @return C
     *
     * @see com.intellectualcrafters.plot.config.C
     */
    public C getCaptions() {
        return c;
    }

    /**
     * Get the plot manager for a world. - Most of these methods can be accessed through the PlotHelper
     *
     * @param world Which manager to get
     *
     * @return PlotManager
     *
     * @see com.intellectualcrafters.plot.object.PlotManager
     * @see PlotMain#getPlotManager(org.bukkit.World)
     */
    public PlotManager getPlotManager(@NotNull final World world) {
        return PlotMain.getPlotManager(world);
    }

    /**
     * Get the plot manager for a world. - Contains useful low level methods for plot merging, clearing, and
     * tessellation
     *
     * @param world Plot World
     *
     * @return PlotManager
     *
     * @see PlotMain#getPlotManager(String)
     * @see com.intellectualcrafters.plot.object.PlotManager
     */
    public PlotManager getPlotManager(@NotNull final String world) {
        return PlotMain.getPlotManager(world);
    }

    /**
     * Get the settings for a world (settings bundled in PlotWorld class) - You will need to downcast for the specific
     * settings a Generator has. e.g. DefaultPlotWorld class implements PlotWorld
     *
     * @param world (to get settings of)
     *
     * @return PlotWorld class for that world ! will return null if not a plot world world
     *
     * @see PlotMain#getWorldSettings(org.bukkit.World)
     * @see com.intellectualcrafters.plot.object.PlotWorld
     */
    public PlotWorld getWorldSettings(@NotNull final World world) {
        return PlotMain.getWorldSettings(world);
    }

    /**
     * Get the settings for a world (settings bundled in PlotWorld class)
     *
     * @param world (to get settings of)
     *
     * @return PlotWorld class for that world ! will return null if not a plot world world
     *
     * @see PlotMain#getWorldSettings(String)
     * @see com.intellectualcrafters.plot.object.PlotWorld
     */
    public PlotWorld getWorldSettings(@NotNull final String world) {
        return PlotMain.getWorldSettings(world);
    }

    /**
     * Send a message to a player.
     *
     * @param player Player that will receive the message
     * @param c      (Caption)
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions#sendMessage(org.bukkit.entity.Player,
     * com.intellectualcrafters.plot.config.C, String...)
     */
    public void sendMessage(@NotNull final Player player, @NotNull final C c) {
        PlayerFunctions.sendMessage(player, c);
    }

    /**
     * Send a message to a player. - Supports color codes
     *
     * @param player Player that will receive the message
     * @param string The message
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions#sendMessage(org.bukkit.entity.Player, String)
     */
    public void sendMessage(@NotNull final Player player, @NotNull final String string) {
        PlayerFunctions.sendMessage(player, string);
    }

    /**
     * Send a message to the console. - Supports color codes
     *
     * @param msg Message that should be sent to the console
     *
     * @see PlotMain#sendConsoleSenderMessage(String)
     */
    public void sendConsoleMessage(@NotNull final String msg) {
        PlotMain.sendConsoleSenderMessage(msg);
    }

    /**
     * Send a message to the console
     *
     * @param c (Caption)
     *
     * @see #sendConsoleMessage(String)
     * @see com.intellectualcrafters.plot.config.C
     */
    public void sendConsoleMessage(@NotNull final C c) {
        sendConsoleMessage(c.s());
    }

    /**
     * Register a flag for use in plots
     *
     * @param flag Flag that should be registered
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager#addFlag(com.intellectualcrafters.plot.flag.AbstractFlag)
     * @see com.intellectualcrafters.plot.flag.AbstractFlag
     */
    public void addFlag(@NotNull final AbstractFlag flag) {
        FlagManager.addFlag(flag);
    }

    /**
     * get all the currently registered flags
     *
     * @return array of Flag[]
     *
     * @see com.intellectualcrafters.plot.flag.FlagManager#getFlags()
     * @see com.intellectualcrafters.plot.flag.AbstractFlag
     */
    public AbstractFlag[] getFlags() {
        return FlagManager.getFlags().toArray(new AbstractFlag[FlagManager.getFlags().size()]);
    }

    /**
     * Get a plot based on the ID
     *
     * @param world World in which the plot is located
     * @param x     Plot Location X Co-ord
     * @param z     Plot Location Z Co-ord
     *
     * @return plot, null if ID is wrong
     *
     * @see PlotHelper#getPlot(org.bukkit.World, com.intellectualcrafters.plot.object.PlotId)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Plot getPlot(@NotNull final World world, final int x, final int z) {
        return PlotHelper.getPlot(world, new PlotId(x, z));
    }

    /**
     * Get a plot based on the location
     *
     * @param l The location that you want to to retrieve the plot from
     *
     * @return plot if found, otherwise it creates a temporary plot-
     *
     * @see PlotHelper#getCurrentPlot(org.bukkit.Location)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Plot getPlot(@NotNull final Location l) {
        return PlotHelper.getCurrentPlot(l);
    }

    /**
     * Get a plot based on the player location
     *
     * @param player Get the current plot for the player location
     *
     * @return plot if found, otherwise it creates a temporary plot
     *
     * @see #getPlot(org.bukkit.Location)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Plot getPlot(@NotNull final Player player) {
        return this.getPlot(player.getLocation());
    }

    /**
     * Check whether or not a player has a plot
     *
     * @param player Player that you want to check for
     *
     * @return true if player has a plot, false if not.
     *
     * @see #getPlots(org.bukkit.World, org.bukkit.entity.Player, boolean)
     */
    public boolean hasPlot(@NotNull final World world, @NotNull final Player player) {
        return (getPlots(world, player, true) != null) && (getPlots(world, player, true).length > 0);
    }

    /**
     * Get all plots for the player
     *
     * @param plr        to search for
     * @param just_owner should we just search for owner? Or with rights?
     *
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Plot[] getPlots(@NotNull final World world, @NotNull final Player plr, final boolean just_owner) {
        final ArrayList<Plot> pPlots = new ArrayList<>();
        for (final Plot plot : PlotMain.getPlots(world).values()) {
            if (just_owner) {
                if ((plot.owner != null) && (plot.owner == UUIDHandler.getUUID(plr))) {
                    pPlots.add(plot);
                }
            } else {
                if (plot.hasRights(plr)) {
                    pPlots.add(plot);
                }
            }
        }
        return (Plot[]) pPlots.toArray();
    }

    /**
     * Get all plots for the world
     *
     * @param world to get plots of
     *
     * @return Plot[] - array of plot objects in world
     *
     * @see PlotMain#getWorldPlots(org.bukkit.World)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Plot[] getPlots(@NotNull final World world) {
        return PlotMain.getWorldPlots(world);
    }

    /**
     * Get all plot worlds
     *
     * @return World[] - array of plot worlds
     *
     * @see com.intellectualcrafters.plot.PlotMain#getPlotWorlds()
     */
    public String[] getPlotWorlds() {
        return PlotMain.getPlotWorlds();
    }

    /**
     * Get if plot world
     *
     * @param world (to check if plot world)
     *
     * @return boolean (if plot world or not)
     *
     * @see com.intellectualcrafters.plot.PlotMain#isPlotWorld(org.bukkit.World)
     */
    public boolean isPlotWorld(@NotNull final World world) {
        return PlotMain.isPlotWorld(world);
    }

    /**
     * Get plot locations
     *
     * @param p Plot that you want to get the locations for
     *
     * @return [0] = bottomLc, [1] = topLoc, [2] = home
     *
     * @see com.intellectualcrafters.plot.util.PlotHelper#getPlotBottomLoc(org.bukkit.World,
     * com.intellectualcrafters.plot.object.PlotId)
     * @see com.intellectualcrafters.plot.util.PlotHelper#getPlotTopLoc(org.bukkit.World,
     * com.intellectualcrafters.plot.object.PlotId)
     * @see com.intellectualcrafters.plot.util.PlotHelper#getPlotHome(org.bukkit.World,
     * com.intellectualcrafters.plot.object.Plot)
     * @see com.intellectualcrafters.plot.object.PlotHomePosition
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Location[] getLocations(@NotNull final Plot p) {
        final World world = Bukkit.getWorld(p.world);
        return new Location[]{PlotHelper.getPlotBottomLoc(world, p.id), PlotHelper.getPlotTopLoc(world, p.id), PlotHelper.getPlotHome(world, p.id)};
    }

    /**
     * Get home location
     *
     * @param p Plot that you want to get the location for
     *
     * @return plot bottom location
     *
     * @see com.intellectualcrafters.plot.util.PlotHelper#getPlotHome(org.bukkit.World,
     * com.intellectualcrafters.plot.object.Plot)
     * @see com.intellectualcrafters.plot.object.PlotHomePosition
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Location getHomeLocation(@NotNull final Plot p) {
        return PlotHelper.getPlotHome(p.getWorld(), p.id);
    }

    /**
     * Get Bottom Location (min, min, min)
     *
     * @param p Plot that you want to get the location for
     *
     * @return plot bottom location
     *
     * @see com.intellectualcrafters.plot.util.PlotHelper#getPlotBottomLoc(org.bukkit.World,
     * com.intellectualcrafters.plot.object.PlotId)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Location getBottomLocation(@NotNull final Plot p) {
        final World world = Bukkit.getWorld(p.world);
        return PlotHelper.getPlotBottomLoc(world, p.id);
    }

    /**
     * Get Top Location (max, max, max)
     *
     * @param p Plot that you want to get the location for
     *
     * @return plot top location
     *
     * @see PlotHelper#getPlotTopLoc(org.bukkit.World, com.intellectualcrafters.plot.object.PlotId)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Location getTopLocation(@NotNull final Plot p) {
        final World world = Bukkit.getWorld(p.world);
        return PlotHelper.getPlotTopLoc(world, p.id);
    }

    /**
     * Check whether or not a player is in a plot
     *
     * @param player who we're checking for
     *
     * @return true if the player is in a plot, false if not-
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions#isInPlot(org.bukkit.entity.Player)
     */
    public boolean isInPlot(@NotNull final Player player) {
        return PlayerFunctions.isInPlot(player);
    }

    /**
     * Register a subcommand
     *
     * @param c SubCommand, that we want to register
     *
     * @see com.intellectualcrafters.plot.commands.MainCommand#subCommands
     * @see com.intellectualcrafters.plot.commands.SubCommand
     */
    public void registerCommand(@NotNull final SubCommand c) {
        MainCommand.subCommands.add(c);
    }

    /**
     * Get the plotMain class
     *
     * @return PlotMain Class
     *
     * @see com.intellectualcrafters.plot.PlotMain
     */
    public PlotMain getPlotMain() {
        return this.plotMain;
    }

    /**
     * Get the player plot count
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plot count
     *
     * @return the number of plots the player has
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions#getPlayerPlotCount(org.bukkit.World,
     * org.bukkit.entity.Player)
     */
    public int getPlayerPlotCount(@NotNull final World world, @NotNull final Player player) {
        return PlayerFunctions.getPlayerPlotCount(world, player);
    }

    /**
     * Get a collection containing the players plots
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plots
     *
     * @return a set containing the players plots
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions#getPlayerPlots(org.bukkit.World,
     * org.bukkit.entity.Player)
     * @see com.intellectualcrafters.plot.object.Plot
     */
    public Set<Plot> getPlayerPlots(@NotNull final World world, @NotNull final Player player) {
        return PlayerFunctions.getPlayerPlots(world, player);
    }

    /**
     * Get the numbers of plots, which the player is able to build in
     *
     * @param player Player, for whom we're getting the plots (trusted, helper and owner)
     *
     * @return the number of allowed plots
     *
     * @see com.intellectualcrafters.plot.util.PlayerFunctions#getAllowedPlots(org.bukkit.entity.Player)
     */
    public int getAllowedPlots(@NotNull final Player player) {
        return PlayerFunctions.getAllowedPlots(player);
    }
}
