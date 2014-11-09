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

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Set;

/**
 * The plotMain api class.
 *
 * @author Citymonstret, Empire92
 */
@SuppressWarnings({"unused", "javadoc"})
public class PlotAPI {

    /**
     * Admin Permission
     */
    public static final String ADMIN_PERMISSION = "plots.admin";
    private static PlotHelper plotHelper;
    private static PlayerFunctions playerFunctions;
    private static FlagManager flagManager;
    private static SchematicHandler schematicHandler;

    // Methods/fields in PlotMain class

    // PlotMain.checkForExpiredPlots(); #Ignore
    // PlotMain.killAllEntities(); #Ignore
    //
    // PlotMain.createConfiguration(plotworld);
    // PlotMain.getPlots(player)
    // PlotMain.getPlots(world)
    // PlotMain.getPlots(world, player)
    // PlotMain.getWorldPlots(world)
    // PlotMain.getPlotWorlds()
    // PlotMain.isPlotWorld(world)
    // PlotMain.removePlot(world, id, callEvent)
    // PlotMain.teleportPlayer(player, from, plot)
    // PlotMain.updatePlot(plot);
    private static C c;

    // Reference
    // To access plotMain stuff.
    private final PlotMain plotMain;

    /**
     * Constructor. Insert any Plugin.
     * (Optimally the plugin that is accessing the method)
     *
     * @param plugin Plugin used to access this method
     */
    public PlotAPI(final JavaPlugin plugin) {
        this.plotMain = JavaPlugin.getPlugin(PlotMain.class);
    }

    /**
     * Get all plots
     *
     * @return all plots
     */
    public Set<Plot> getAllPlots() {
        return PlotMain.getPlots();
    }

    /**
     * Return all plots for a player
     *
     * @param player
     * @return all plots that a player owns
     */
    public Set<Plot> getPlayerPlots(final Player player) {
        return PlotMain.getPlots(player);
    }

    /**
     * Add a plotoworld
     *
     * @param world     World Name
     * @param plotWorld Plot World Object
     * @param manager   World Manager
     * @see com.intellectualcrafters.plot.PlotMain#addPlotWorld(String,
     * com.intellectualcrafters.plot.PlotWorld,
     * com.intellectualcrafters.plot.PlotManager)
     */
    public void addPlotWorld(final String world, final PlotWorld plotWorld, final PlotManager manager) {
        PlotMain.addPlotWorld(world, plotWorld, manager);
    }

    /**
     * @return main configuration
     * @see com.intellectualcrafters.plot.PlotMain#config
     */
    public YamlConfiguration getConfig() {
        return PlotMain.config;
    }

    /**
     * @return storage configuration
     * @see com.intellectualcrafters.plot.PlotMain#storage
     */
    public YamlConfiguration getStorage() {
        return PlotMain.storage;
    }

    /**
     * Get the main class for this plugin <br>
     * - Contains a lot of fields and methods - not very well organized <br>
     * Only use this if you really need it
     *
     * @return PlotMain PlotSquared Main Class
     */
    public PlotMain getMain() {
        return this.plotMain;
    }

    /**
     * PlotHelper class contains useful methods relating to plots.
     *
     * @return PlotHelper
     */
    public PlotHelper getPlotHelper() {
        return plotHelper;
    }

    /**
     * PlayerFunctions class contains useful methods relating to players - Some
     * player/plot methods are here as well
     *
     * @return PlayerFunctions
     */
    public PlayerFunctions getPlayerFunctions() {
        return playerFunctions;
    }

    /**
     * FlagManager class contains methods relating to plot flags
     *
     * @return FlagManager
     */
    public FlagManager getFlagManager() {
        return flagManager;
    }

    /**
     * SchematicHandler class contains methods related to pasting schematics
     *
     * @return SchematicHandler
     */
    public SchematicHandler getSchematicHandler() {
        return schematicHandler;
    }

    /**
     * C class contains all the captions from the translations.yml file.
     *
     * @return C
     */
    public C getCaptions() {
        return c;
    }

    /**
     * Get the plot manager for a world. - Most of these methods can be accessed
     * through the PlotHelper
     *
     * @param world
     * @return PlotManager
     */
    public PlotManager getPlotManager(final World world) {
        return PlotMain.getPlotManager(world);
    }

    /**
     * Get the plot manager for a world. - Contains useful low level methods for
     * plot merging, clearing, and tessellation
     *
     * @param world
     * @return PlotManager
     */
    public PlotManager getPlotManager(final String world) {
        return PlotMain.getPlotManager(world);
    }

    /**
     * Get the settings for a world (settings bundled in PlotWorld class) - You
     * will need to downcast for the specific settings a Generator has. e.g.
     * DefaultPlotWorld class implements PlotWorld
     *
     * @param world (to get settings of)
     * @return PlotWorld class for that world ! will return null if not a plot
     * world world
     */
    public PlotWorld getWorldSettings(final World world) {
        return PlotMain.getWorldSettings(world);
    }

    /**
     * Get the settings for a world (settings bundled in PlotWorld class)
     *
     * @param world (to get settings of)
     * @return PlotWorld class for that world ! will return null if not a plot
     * world world
     */
    public PlotWorld getWorldSettings(final String world) {
        return PlotMain.getWorldSettings(world);
    }

    /**
     * Send a message to a player.
     *
     * @param player
     * @param c      (Caption)
     */
    public void sendMessage(final Player player, final C c) {
        PlayerFunctions.sendMessage(player, c);
    }

    /**
     * Send a message to a player. - Supports color codes
     *
     * @param player
     * @param string
     */
    public void sendMessage(final Player player, final String string) {
        PlayerFunctions.sendMessage(player, string);
    }

    /**
     * Send a message to the console. - Supports color codes
     *
     * @param msg
     */
    public void sendConsoleMessage(final String msg) {
        PlotMain.sendConsoleSenderMessage(msg);
    }

    /**
     * Send a message to the console
     *
     * @param c (Caption)
     */
    public void sendConsoleMessage(final C c) {
        sendConsoleMessage(c.s());
    }

    /**
     * Register a flag for use in plots
     *
     * @param flag
     */
    public void addFlag(final AbstractFlag flag) {
        FlagManager.addFlag(flag);
    }

    /**
     * get all the currently registered flags
     *
     * @return array of Flag[]
     */
    public AbstractFlag[] getFlags() {
        return FlagManager.getFlags().toArray(new AbstractFlag[FlagManager.getFlags().size()]);
    }

    /**
     * Get a plot based on the ID
     *
     * @param world
     * @param x
     * @param z
     * @return plot, null if ID is wrong
     */
    public Plot getPlot(final World world, final int x, final int z) {
        return PlotHelper.getPlot(world, new PlotId(x, z));
    }

    /**
     * Get a plot based on the location
     *
     * @param l
     * @return plot if found, otherwise it creates a temporary plot-
     */
    public Plot getPlot(final Location l) {
        return PlotHelper.getCurrentPlot(l);
    }

    /**
     * Get a plot based on the player location
     *
     * @param player
     * @return plot if found, otherwise it creates a temporary plot
     */
    public Plot getPlot(final Player player) {
        return this.getPlot(player.getLocation());
    }

    /**
     * Check whether or not a player has a plot
     *
     * @param player
     * @return true if player has a plot, false if not.
     */
    public boolean hasPlot(final World world, final Player player) {
        return (getPlots(world, player, true) != null) && (getPlots(world, player, true).length > 0);
    }

    /**
     * Get all plots for the player
     *
     * @param plr        to search for
     * @param just_owner should we just search for owner? Or with rights?
     */
    public Plot[] getPlots(final World world, final Player plr, final boolean just_owner) {
        final ArrayList<Plot> pPlots = new ArrayList<>();
        for (final Plot plot : PlotMain.getPlots(world).values()) {
            if (just_owner) {
                if ((plot.owner != null) && (plot.owner == plr.getUniqueId())) {
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
     * @return Plot[] - array of plot objects in world
     */
    public Plot[] getPlots(final World world) {
        return PlotMain.getWorldPlots(world);
    }

    /**
     * Get all plot worlds
     *
     * @return World[] - array of plot worlds
     */
    public String[] getPlotWorlds() {
        return PlotMain.getPlotWorlds();
    }

    /**
     * Get if plot world
     *
     * @param world (to check if plot world)
     * @return boolean (if plot world or not)
     */
    public boolean isPlotWorld(final World world) {
        return PlotMain.isPlotWorld(world);
    }

    /**
     * Get plot locations
     *
     * @param p
     * @return [0] = bottomLc, [1] = topLoc, [2] = home
     */
    public Location[] getLocations(final Plot p) {
        final World world = Bukkit.getWorld(p.world);
        return new Location[]{PlotHelper.getPlotBottomLoc(world, p.id), PlotHelper.getPlotTopLoc(world, p.id), PlotHelper.getPlotHome(world, p.id)};
    }

    /**
     * Get home location
     *
     * @param p
     * @return plot bottom location
     */
    public Location getHomeLocation(final Plot p) {
        return PlotHelper.getPlotHome(p.getWorld(), p.id);
    }

    /**
     * Get Bottom Location
     *
     * @param p
     * @return plot bottom location
     */
    public Location getBottomLocation(final Plot p) {
        final World world = Bukkit.getWorld(p.world);
        return PlotHelper.getPlotBottomLoc(world, p.id);
    }

    /**
     * Get Top Location
     *
     * @param p
     * @return plot top location
     */
    public Location getTopLocation(final Plot p) {
        final World world = Bukkit.getWorld(p.world);
        return PlotHelper.getPlotTopLoc(world, p.id);
    }

    /**
     * Check whether or not a player is in a plot
     *
     * @param player
     * @return true if the player is in a plot, false if not-
     */
    public boolean isInPlot(final Player player) {
        return PlayerFunctions.isInPlot(player);
    }

    /**
     * Register a subcommand
     *
     * @param c
     */
    public void registerCommand(final SubCommand c) {
        MainCommand.subCommands.add(c);
    }

    /**
     * Get the plotMain class
     *
     * @return PlotMain Class
     */
    public PlotMain getPlotMain() {
        return this.plotMain;
    }

    /**
     * Get the player plot count
     *
     * @param player
     * @return
     */
    public int getPlayerPlotCount(final World world, final Player player) {
        return PlayerFunctions.getPlayerPlotCount(world, player);
    }

    /**
     * Get a players plots
     *
     * @param player
     * @return a set containing the players plots
     */
    public Set<Plot> getPlayerPlots(final World world, final Player player) {
        return PlayerFunctions.getPlayerPlots(world, player);
    }

    /**
     * Get the allowed plot count for a player
     *
     * @param player
     * @return the number of allowed plots
     */
    public int getAllowedPlots(final Player player) {
        return PlayerFunctions.getAllowedPlots(player);
    }
}
