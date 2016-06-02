package com.intellectualcrafters.plot.api;

import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * PlotSquared API.
 *
 * <p>Useful classes:
 * <ul>
 *     <li>{@link BukkitUtil}</li>
 *     <li>{@link PlotPlayer}</li>
 *     <li>{@link Plot}</li>
 *     <li>{@link com.intellectualcrafters.plot.object.Location}</li>
 *     <li>{@link PlotArea}</li>
 *     <li>{@link PS}</li>
 * </ul>
 * @version 3.3.3
 */
public class PlotAPI {

    /**
     * Deprecated, does nothing.
     * @param plugin not needed
     * @deprecated Not needed
     */
    @Deprecated
    public PlotAPI(JavaPlugin plugin) {}

    /**
     * Get all plots.
     *
     * @return all plots
     *
     * @see PS#getPlots()
     */
    public Set<Plot> getAllPlots() {
        return PS.get().getPlots();
    }

    /**
     * Return all plots for a player.
     *
     * @param player Player, whose plots to search for
     *
     * @return all plots that a player owns
     */
    public Set<Plot> getPlayerPlots(Player player) {
        return PS.get().getPlots(BukkitUtil.getPlayer(player));
    }

    /**
     * Add a plot world.
     *
     * @param plotArea Plot World Object
     * @see PS#addPlotArea(PlotArea)
     */
    public void addPlotArea(PlotArea plotArea) {
        PS.get().addPlotArea(plotArea);
    }

    /**
     * Returns the PlotSquared configurations file.
     * @return main configuration
     *
     * @see PS#config
     */
    public YamlConfiguration getConfig() {
        return PS.get().config;
    }

    /**
     * Get the PlotSquared storage file.
     * @return storage configuration
     *
     * @see PS#storage
     */
    public YamlConfiguration getStorage() {
        return PS.get().storage;
    }

    /**
     * Get the main class for this plugin. Only use this if you really need it.
     *
     * @return PlotSquared PlotSquared Main Class
     *
     * @see PS
     */
    public PS getMain() {
        return PS.get();
    }

    /**
     * ChunkManager class contains several useful methods.
     * <ul>
     *     <li>Chunk deletion</li>
     *     <li>Moving or copying regions</li>
     *     <li>Plot swapping</li>
     *     <li>Entity Tracking</li>
     *     <li>Region Regeneration</li>
     * </ul>
     *
     * @return ChunkManager
     *
     * @see ChunkManager
     */
    public ChunkManager getChunkManager() {
        return ChunkManager.manager;
    }

    /**
     * Get the block/biome set queue
     * @return SetQueue.IMP
     */
    public SetQueue getSetQueue() {
        return SetQueue.IMP;
    }

    /**
     * UUIDWrapper class has basic methods for getting UUIDS. It's recommended
     * to use the UUIDHandler class instead.
     *
     * @return UUIDWrapper
     *
     * @see UUIDWrapper
     */
    public UUIDWrapper getUUIDWrapper() {
        return UUIDHandler.getUUIDWrapper();
    }

    /**
     * Do not use this. Instead use FlagManager.[method] in your code.
     *  - Flag related stuff
     *
     * @return FlagManager
     *
     * @deprecated Use {@link FlagManager} directly
     */
    @Deprecated
    public FlagManager getFlagManager() {
        return new FlagManager();
    }

    /**
     * Do not use this. Instead use MainUtil.[method] in your code.
     *
     * @return MainUtil
     * @deprecated Use {@link MainUtil} directly
     */
    @Deprecated
    public MainUtil getMainUtil() {
        return new MainUtil();
    }

    /**
     * Do not use this. Instead use C.PERMISSION_[method] in your code.
     *
     * @return Array of strings
     *
     * @see Permissions
     * @deprecated Use {@link C} to list all the permissions
     */
    @Deprecated
    public String[] getPermissions() {
        ArrayList<String> perms = new ArrayList<>();
        for (C caption : C.values()) {
            if ("static.permissions".equals(caption.getCategory())) {
                perms.add(caption.s());
            }
        }
        return perms.toArray(new String[perms.size()]);
    }

    /**
     * SchematicHandler class contains methods related to pasting, reading
     * and writing schematics.
     *
     * @return SchematicHandler
     *
     * @see SchematicHandler
     */
    public SchematicHandler getSchematicHandler() {
        return SchematicHandler.manager;
    }

    /**
     * Use C.[caption] instead
     *
     * @return C
     * @deprecated Use {@link C}
     */
    @Deprecated
    public C[] getCaptions() {
        return C.values();
    }

    /**
     * Get the plot manager for a world. Most of these methods can be accessed
     * through the MainUtil.
     *
     * @param world Which manager to get
     *
     * @return PlotManager
     *
     * @see PlotManager
     * @see PS#getPlotManager(Plot)
     */
    @Deprecated
    public PlotManager getPlotManager(World world) {
        if (world == null) {
            return null;
        }
        return getPlotManager(world.getName());
    }

    /**
     * Get a list of PlotAreas in the world.
     * @param world The world to check for plot areas
     * @return A set of PlotAreas
     */
    public Set<PlotArea> getPlotAreas(World world) {
        if (world == null) {
            return Collections.emptySet();
        }
        return PS.get().getPlotAreas(world.getName());
    }

    /**
     * Get the plot manager for a world. Contains useful low level methods for
     * plot merging, clearing, and tessellation.
     *
     * @param world The world
     *
     * @return PlotManager
     *
     * @see PS#getPlotManager(Plot)
     * @see PlotManager
     */
    @Deprecated
    public PlotManager getPlotManager(String world) {
        Set<PlotArea> areas = PS.get().getPlotAreas(world);
        switch (areas.size()) {
            case 0:
                return null;
            case 1:
                return areas.iterator().next().manager;
            default:
                PS.debug("PlotAPI#getPlotManager(org.bukkit.World) is deprecated and doesn't support multi plot area worlds.");
                return null;
        }
    }

    /**
     * Get the settings for a world (settings bundled in PlotArea class). You
     * will need to downcast for the specific settings a Generator has. e.g.
     * DefaultPlotWorld class implements PlotArea
     *
     * @param world The World
     *
     * @return The {@link PlotArea} for the world or null if not in plotworld
     *
     * @see #getPlotAreas(World)
     * @see PlotArea
     */
    @Deprecated
    public PlotArea getWorldSettings(World world) {
        if (world == null) {
            return null;
        }
        return getWorldSettings(world.getName());
    }

    /**
     * Get the settings for a world.
     *
     * @param world the world to retrieve settings from
     *
     * @return The {@link PlotArea} for the world or null if not in plotworld
     *
     * @see PS#getPlotArea(String, String)
     * @see PlotArea
     */
    @Deprecated
    public PlotArea getWorldSettings(String world) {
        if (world == null) {
            return null;
        }
        Set<PlotArea> areas = PS.get().getPlotAreas(world);
        switch (areas.size()) {
            case 0:
                return null;
            case 1:
                return areas.iterator().next();
            default:
                PS.debug("PlotAPI#getWorldSettings(org.bukkit.World) is deprecated and doesn't support multi plot area worlds.");
                return null;
        }
    }

    /**
     * Send a message to a player.
     *
     * @param player the recipient of the message
     * @param caption the message
     *
     * @see MainUtil#sendMessage(PlotPlayer, C, String...)
     */
    public void sendMessage(Player player, C caption) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), caption);
    }

    /**
     * Send a message to a player. The message supports color codes.
     *
     * @param player the recipient of the message
     * @param string the message
     *
     * @see MainUtil#sendMessage(PlotPlayer, String)
     */
    public void sendMessage(Player player, String string) {
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), string);
    }

    /**
     * Send a message to the console. The message supports color codes.
     *
     * @param message the message
     *
     * @see MainUtil#sendConsoleMessage(C, String...)
     */
    public void sendConsoleMessage(String message) {
        PS.log(message);
    }

    /**
     * Send a message to the console.
     *
     * @param caption the message
     *
     * @see #sendConsoleMessage(String)
     * @see C
     */
    public void sendConsoleMessage(C caption) {
        sendConsoleMessage(caption.s());
    }

    /**
     * Register a flag for use in plots.
     *
     * @param flag the flag being registered
     *
     */
    public void addFlag(Flag<?> flag) {
        Flags.registerFlag(flag);
    }

    /**
     * Get a plot based on the ID.
     *
     * @param world the world the plot is located in
     * @param x The PlotID x coordinate
     * @param z The PlotID y coordinate
     *
     * @return plot, null if ID is wrong
     *
     * @see PlotArea#getPlot(PlotId)
     */
    @Deprecated
    public Plot getPlot(World world, int x, int z) {
        if (world == null) {
            return null;
        }
        PlotArea area = getWorldSettings(world);
        if (area == null) {
            return null;
        }
        return area.getPlot(new PlotId(x, z));
    }

    /**
     * Get a plot based on the location.
     *
     * @param location the location to check
     *
     * @return plot if found, otherwise it creates a temporary plot
     *
     * @see Plot
     */
    public Plot getPlot(Location location) {
        if (location == null) {
            return null;
        }
        return BukkitUtil.getLocation(location).getPlot();
    }

    /**
     * Get a plot based on the player location.
     *
     * @param player the player to check
     *
     * @return plot if found, otherwise it creates a temporary plot
     *
     * @see #getPlot(Location)
     * @see Plot
     */
    public Plot getPlot(Player player) {
        return this.getPlot(player.getLocation());
    }

    /**
     * Check whether or not a player has a plot.
     *
     * @param player Player that you want to check for
     * @param world The world to check
     * @return true if player has a plot, false if not.
     *
     * @see #getPlots(World, Player, boolean)
     */
    @Deprecated
    public boolean hasPlot(World world, Player player) {
        return getPlots(world, player, true).length > 0;
    }

    /**
     * Get all plots for the player.
     *
     * @param world The world to retrieve plots from
     * @param player The player to search for
     * @param justOwner should we just search for owner? Or with rights?
     * @return An array of plots for the player
     */
    @Deprecated
    public Plot[] getPlots(World world, Player player, boolean justOwner) {
        ArrayList<Plot> pPlots = new ArrayList<>();
        UUID uuid = BukkitUtil.getPlayer(player).getUUID();
        for (Plot plot : PS.get().getPlots(world.getName())) {
            if (justOwner) {
                if (plot.hasOwner() && plot.isOwner(uuid)) {
                    pPlots.add(plot);
                }
            } else if (plot.isAdded(uuid)) {
                pPlots.add(plot);
            }
        }
        return pPlots.toArray(new Plot[pPlots.size()]);
    }

    /**
     * Get all plots for the world.
     *
     * @param world to get plots of
     *
     * @return Plot[] - array of plot objects in world
     *
     * @see PS#getPlots(String)
     * @see Plot
     */
    @Deprecated
    public Plot[] getPlots(World world) {
        if (world == null) {
            return new Plot[0];
        }
        Collection<Plot> plots = PS.get().getPlots(world.getName());
        return plots.toArray(new Plot[plots.size()]);
    }

    /**
     * Get all plot worlds.
     *
     * @return World[] - array of plot worlds
     *
     */
    @Deprecated
    public String[] getPlotWorlds() {
        Set<String> plotWorldStrings = PS.get().getPlotWorldStrings();
        return plotWorldStrings.toArray(new String[plotWorldStrings.size()]);
    }

    /**
     * Get if plotworld.
     *
     * @param world The world to check
     *
     * @return boolean (if plot world or not)
     *
     * @see PS#hasPlotArea(String)
     */
    @Deprecated
    public boolean isPlotWorld(World world) {
        return PS.get().hasPlotArea(world.getName());
    }

    /**
     * Get plot locations.
     *
     * @param plot Plot to get the locations for
     *
     * @return [0] = bottomLc, [1] = topLoc, [2] = home
     *
     * @deprecated As merged plots may not have a rectangular shape
     *
     * @see Plot
     */
    @Deprecated
    public Location[] getLocations(Plot plot) {
        Location bukkitBottom = BukkitUtil.getLocation(plot.getCorners()[0]);
        Location bukkitTop = BukkitUtil.getLocation(plot.getCorners()[1]);
        Location bukkitHome = BukkitUtil.getLocation(plot.getHome());
        return new Location[]{bukkitBottom, bukkitTop, bukkitHome};
    }

    /**
     * Get home location.
     *
     * @param plot Plot that you want to get the location for
     *
     * @return plot bottom location
     *
     * @see Plot
     */
    public Location getHomeLocation(Plot plot) {
        return BukkitUtil.getLocation(plot.getHome());
    }

    /**
     * Get Bottom Location (min, min, min).
     *
     * @param plot Plot that you want to get the location for
     *
     * @return plot bottom location
     *
     * @deprecated As merged plots may not have a rectangular shape
     *
     * @see Plot
     */
    @Deprecated
    public Location getBottomLocation(Plot plot) {
        return BukkitUtil.getLocation(plot.getBottom());
    }

    /**
     * Get Top Location (max, max, max).
     *
     * @param plot Plot that you want to get the location for
     *
     * @return plot top location
     *
     * @deprecated As merged plots may not have a rectangular shape
     *
     * @see Plot
     */
    @Deprecated
    public Location getTopLocation(Plot plot) {
        return BukkitUtil.getLocation(plot.getTop());
    }

    /**
     * Check whether or not a player is in a plot.
     *
     * @param player who we're checking for
     *
     * @return true if the player is in a plot, false if not-
     *
     */
    public boolean isInPlot(Player player) {
        return getPlot(player) != null;
    }

    /**
     * Register a subcommand.
     * @deprecated Command registration is done on object creation
     * @param c SubCommand, that we want to register
     * @see SubCommand
     */
    @Deprecated
    public void registerCommand(SubCommand c) {
        PS.debug("SubCommands are now registered on creation");
    }

    /**
     * Get the PlotSquared class.
     *
     * @return PlotSquared Class
     *
     * @see PS
     */
    public PS getPlotSquared() {
        return PS.get();
    }

    /**
     * Get the player plot count.
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plot count
     *
     * @return the number of plots the player has
     *
     */
    public int getPlayerPlotCount(World world, Player player) {
        if (world == null) {
            return 0;
        }
        return BukkitUtil.getPlayer(player).getPlotCount(world.getName());
    }

    /**
     * Get a collection containing the players plots.
     *
     * @param world  Specify the world we want to select the plots from
     * @param player Player, for whom we're getting the plots
     *
     * @return a set containing the players plots
     *
     * @see PS#getPlots(String, PlotPlayer)
     *
     * @see Plot
     */
    public Set<Plot> getPlayerPlots(World world, Player player) {
        if (world == null) {
            return new HashSet<>();
        }
        return PlotPlayer.wrap(player).getPlots(world.getName());
    }

    /**
     * Get the numbers of plots, which the player is able to build in.
     *
     * @param player player, for whom we're getting the plots
     *
     * @return the number of allowed plots
     *
     */
    public int getAllowedPlots(Player player) {
        PlotPlayer plotPlayer = PlotPlayer.wrap(player);
        return plotPlayer.getAllowedPlots();
    }

    /**
     * Get the PlotPlayer for a player. The PlotPlayer is usually cached and
     * will provide useful functions relating to players.
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param player the player to wrap
     * @return a {@code PlotPlayer}
     */
    public PlotPlayer wrapPlayer(Player player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Get the PlotPlayer for a UUID.
     *
     * <p><i>Please note that PlotSquared can be configured to provide
     * different UUIDs than bukkit</i>
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param uuid the uuid of the player to wrap
     * @return a {@code PlotPlayer}
     */
    public PlotPlayer wrapPlayer(UUID uuid) {
        return PlotPlayer.wrap(uuid);
    }

    /**
     * Get the PlotPlayer for a username.
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param player the player to wrap
     * @return a {@code PlotPlayer}
     */
    public PlotPlayer wrapPlayer(String player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Get the PlotPlayer for an offline player.
     *
     * <p>Note that this will work if the player is offline, however not all
     * functionality will work.
     *
     * @see PlotPlayer#wrap(Object)
     *
     * @param player the player to wrap
     * @return a {@code PlotPlayer}
     */
    public PlotPlayer wrapPlayer(OfflinePlayer player) {
        return PlotPlayer.wrap(player);
    }
}
